package com.tdt.unicom.sgip.svr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import org.apache.log4j.Logger;

import com.tdt.unicom.domains.Bind;
import com.tdt.unicom.domains.BindResp;
import com.tdt.unicom.domains.SGIPCommandDefine;
import com.tdt.unicom.domains.Submit;
import com.tdt.unicom.domains.SubmitResp;
import com.tdt.unicom.domains.Unbind;
import com.tdt.unicom.domains.UnbindResp;
import com.tdt.unicom.spsvr.MTReq;

/**
 * @project UNICOM
 * @author sunnylocus
 * @vresion 1.0 2009-8-21
 * @description  短信下发类
 */
public final class SPSender {
	private final LinkedList<MTReq> mtReqQueue = new LinkedList<MTReq>(); // 发送队列
	private Logger log = Logger.getLogger(getClass());
	private final static int WORKER_NUM = 1;
	private volatile boolean isSendUnbind = true;// 是否已向SMG发送 unbind命令的标志
	private volatile boolean isBind = false;     //是否已与SMG建立bind
	
	private static SPSender instance = null;

	private Socket socket = null;
	private DataOutputStream out = null;
	private DataInputStream in = null;
	
	private long bindstartTime;
	private long currentTime;
	private static Map<String, Submit> mtsendedMap;  //已发送的下发实例
	//以下信息在productionConf.xml里配置
	protected static String unicomIp;     //联通SMG的IP地址
	protected static int unicomPort;      //联通SMG监听的端口号
	protected static String spLoginName;  //登陆SMG所用到的用户名
	protected static String spLogPassword;//登陆SMG所用到的密码

	private SPSender() {
		for (int i = 0; i < WORKER_NUM; i++) {
			Thread workerThrader = new Thread(new WorkerHandler());
			workerThrader.setName("MT-thread-" + i);
			workerThrader.start();
		}
		log.info("【 MT下发线程启动，线程数:" + WORKER_NUM + " 】");
	}

	/**
	 * 单态模式，在获取实例时作双重检测，防止多线程情况下产生多个实例
	 * @return　SPSender
	 */
	public static SPSender getInstance(Map<String, Submit> map) {
		if (instance == null) {
			synchronized (SPSender.class) {
				if (instance == null) {
					instance = new SPSender();
					if(map!=null){
						mtsendedMap=map;
					}
				}
			}
		}
		return instance;
	}
	/**
	 * 向队列添加任务
	 * @param mtreq 短信下发请求
	 */
	public void addTask(MTReq mtreq) {
		synchronized (this) {
			mtReqQueue.add(mtreq);
			log.info("收到MT下发请求，当前队列数:" + mtReqQueue.size());
			this.notifyAll();// 唤醒发送线程
		}
	}

	/**
	 * 从队列中获取一个短信下发实例
	 * @return MTReq
	 * @throws InterruptedException
	 */
	public synchronized MTReq getTask() throws InterruptedException {
		while (mtReqQueue.isEmpty()) {
			log.info(Thread.currentThread().getName() + "等待任务");
			wait();
		}
		return mtReqQueue.removeFirst();

	}
	//短信下发线程
	class WorkerHandler implements Runnable {
		MTReq mtreq = null;
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					mtreq = getTask();
					if (mtreq != null) {
						log.info("【" + Thread.currentThread().getName()+ "获得一个短信下发请求实例】");
						Thread.currentThread();
						Thread.yield();
						sendMTReq(mtreq);
					}
				}
			} catch (InterruptedException e) {
				log.warn("短信下发线程中断!", e);
			} catch (IOException e) {
				mtReqQueue.add(mtreq);    //如果发送失败，重新放回队列
				log.error("短信下发时IO流异常!",e);
			}
		}
	}

	//向联通递交短信下发
	//此地方用synchronized防护的原因是防止计时器线程因空闲时间已到向SMG发送unbind命令
	//将SMG与SP连接断开，而此时刚好有下发实例进入队列，如果不加同步控制，那么计时器请求断开连接，而下发线程则向断开的连接发送
	//数据导致IO流异常
	public synchronized void sendMTReq(MTReq mtreq) throws IOException {
		if (isSendUnbind || (!socket.isConnected() && socket.isClosed())) { // 如果连接已断开，则重新与SMG建立连接
			// --------------------连接
			int connectnum =0 ;//连接次数
			while(true) {
				try {
					socket = SocketFactory.getDefault().createSocket(unicomIp,unicomPort);
					socket.setTcpNoDelay(true);// 数据立即发送
					socket.setTrafficClass(0x04 | 0x10);
					if(socket!=null) break;
				} catch(ConnectException e) {
					try {
						Thread.currentThread();
						Thread.sleep(1000 * 1);
					} catch(InterruptedException e1){log.error("线程中断!",e1);}
					connectnum ++;
					if(connectnum >2) { //如果连接次数超过3次，放弃连接，报告重大错误
						log.fatal("未能与SMG建立连接，连接超时");
						mtReqQueue.addFirst(mtreq);//未发送，放回队列
						return;
					}
					continue;
				}
			}
			if(!isBind) { //与SMG建立bind
				bindstartTime = System.currentTimeMillis();
				out = new DataOutputStream(socket.getOutputStream());
				in = new DataInputStream(socket.getInputStream());
				log.info(Thread.currentThread().getName() + "与SMG建立连接");
				// --------------------绑定
				Bind bind = new Bind();
				bind.setLoginType((byte) 1);
				bind.setLoginName(spLoginName);
				bind.setLoginPassword(spLogPassword);
				bind.write(out);
				//--------------------绑定响应
				isSendUnbind = false;
				BindResp res = (BindResp) bind.read(in);
				if (res.getResult() != 0) {
					log.fatal("SMG拒绝连接。错误码：" + res.getResult());
					return;
				}
				this.launchTimer();//启动计时器
			}
		}
		// --------------------下发
		for (int i = 0; i < mtreq.getPhoneList().size(); i++) {
			Submit submit = new Submit();
			submit.setSPNumber(mtreq.getSpNumber());
			submit.setChargeNumber("000000000000000000000");// 长度21,如果全0表示由SP支付该条短信费用
			submit.setUserNumber(new String[] { mtreq.getPhoneList().get(i) });
			submit.setCorpId("41211");
			submit.setFeeType((byte) 0);
			submit.setFeeValue("0");
			submit.setGivenValue("0");
			submit.setAgentFlag((byte) 0);
			submit.setMorelatetoMTFlag((byte) 2); // 0-MO点播引起的第一条MT信息
			// 1-MO点播引起的非第一条信息
			// 2-非MO点播引引起的MT消息(定购业务)
			// 3系统反馈引起的MT消息
			submit.setPriority((byte) 0);
			submit.setExpireTime("");
			submit.setScheduleTime("");
			submit.setReportFlag(Byte.valueOf(mtreq.getReportFlag())); // 是否向SP报告状态
			submit.setTP_pid((byte) 0);
			submit.setTP_udhi((byte) 0);
			submit.setMessageCoding((byte) 15);
			submit.setMessageType((byte) 0);
			submit.setMessageContent(mtreq.getMessageContent());
			submit.setUserCoun((byte) 1); // 根据sgip1.2扩展协议必须填1,否则视为业务非法包处理
			submit.setServiceType(mtreq.getServiceType());
			submit.setLinkID(mtreq.getLinkId());
			submit.write(out);
			SubmitResp submitres = (SubmitResp) submit.read(in);
			if (submitres.getResult() == 0) {
				log.info("【" + Thread.currentThread().getName()+ " 发送的MT请求成功递交到SMG 】");
				//将下发实例添加到已发送容器中
				mtsendedMap.put(submit.header.getSequenceNumber(),submit);
				continue ; //继续发送下一条短信
			}
			log.warn("【" + Thread.currentThread().getName()+ " 发送的MT请求递交到SMG失败!,错误码 " + submitres.getResult() + "】");
		}
	}
	
	/**
	 * 该计时器用于检测与SMG建立的时间，如果短消息发送队列为空，且空闲时间超过30秒
	 * 则向SMG发送unbind命令，在收到SMG的unbind_resp响应后SP断开连接
	 * 
	 * 修改：2011-04-15
	 *         将Timer改为ScheduledExecutorService,如果Timer出错，会将问题传染给倒霉的调用者
	 * 　　　导致下发线程全部中断
	 * 
	 */
	public void launchTimer() {
		//线程池能按时间计划来执行任务，允许用户设定计划执行任务的时间，int类型的参数是设定   
	    //线程池中线程的最小数目。当任务较多时，线程池可能会自动创建更多的工作线程来执行任务   
	    final ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);   

		Runnable task = new Runnable() {
			public void run() {
				if(mtReqQueue.isEmpty()) {
					currentTime = System.currentTimeMillis();
					int passedTime = (int) ((currentTime - bindstartTime) / 1000);
					if(passedTime > 30) {
						synchronized (this) { //持有对象锁，防止在拆掉SMG链路时，其它线程与SMG建立连接,导致
							                  //SMG认为用户状态不正常并拒绝短信下发请求
							// 向SMG发送unbind命令
							Unbind unbind = new Unbind();
							unbind.write(out);
							log.info(Thread.currentThread().getName()+" 向SMG发送unbind命令");
							UnbindResp resp = (UnbindResp) unbind.read(in);
							if (Arrays.equals(resp.header.getCommandId(),SGIPCommandDefine.SGIP_UNBIND_RESP)) {
								isSendUnbind = true; //标记已发送unbind命令
								log.info("SMG收到unbind命令，SP关闭连接");
								scheduExec.shutdown(); //计时停止
								//释放socket资源
								try {
									if(in !=null) in.close();
									if(out !=null) out.close();
									if(socket !=null) socket.close();
								} catch(IOException e){
									log.warn("释放socket资源发生异常");
								}
							}
						}
					}
				}
			}
		};
		scheduExec.scheduleWithFixedDelay(task, 0, 1,TimeUnit.SECONDS);   //1秒钟检测一次
	}
}