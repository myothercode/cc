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
 * @description  �����·���
 */
public final class SPSender {
	private final LinkedList<MTReq> mtReqQueue = new LinkedList<MTReq>(); // ���Ͷ���
	private Logger log = Logger.getLogger(getClass());
	private final static int WORKER_NUM = 1;
	private volatile boolean isSendUnbind = true;// �Ƿ�����SMG���� unbind����ı�־
	private volatile boolean isBind = false;     //�Ƿ�����SMG����bind
	
	private static SPSender instance = null;

	private Socket socket = null;
	private DataOutputStream out = null;
	private DataInputStream in = null;
	
	private long bindstartTime;
	private long currentTime;
	private static Map<String, Submit> mtsendedMap;  //�ѷ��͵��·�ʵ��
	//������Ϣ��productionConf.xml������
	protected static String unicomIp;     //��ͨSMG��IP��ַ
	protected static int unicomPort;      //��ͨSMG�����Ķ˿ں�
	protected static String spLoginName;  //��½SMG���õ����û���
	protected static String spLogPassword;//��½SMG���õ�������

	private SPSender() {
		for (int i = 0; i < WORKER_NUM; i++) {
			Thread workerThrader = new Thread(new WorkerHandler());
			workerThrader.setName("MT-thread-" + i);
			workerThrader.start();
		}
		log.info("�� MT�·��߳��������߳���:" + WORKER_NUM + " ��");
	}

	/**
	 * ��̬ģʽ���ڻ�ȡʵ��ʱ��˫�ؼ�⣬��ֹ���߳�����²������ʵ��
	 * @return��SPSender
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
	 * ������������
	 * @param mtreq �����·�����
	 */
	public void addTask(MTReq mtreq) {
		synchronized (this) {
			mtReqQueue.add(mtreq);
			log.info("�յ�MT�·����󣬵�ǰ������:" + mtReqQueue.size());
			this.notifyAll();// ���ѷ����߳�
		}
	}

	/**
	 * �Ӷ����л�ȡһ�������·�ʵ��
	 * @return MTReq
	 * @throws InterruptedException
	 */
	public synchronized MTReq getTask() throws InterruptedException {
		while (mtReqQueue.isEmpty()) {
			log.info(Thread.currentThread().getName() + "�ȴ�����");
			wait();
		}
		return mtReqQueue.removeFirst();

	}
	//�����·��߳�
	class WorkerHandler implements Runnable {
		MTReq mtreq = null;
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					mtreq = getTask();
					if (mtreq != null) {
						log.info("��" + Thread.currentThread().getName()+ "���һ�������·�����ʵ����");
						Thread.currentThread();
						Thread.yield();
						sendMTReq(mtreq);
					}
				}
			} catch (InterruptedException e) {
				log.warn("�����·��߳��ж�!", e);
			} catch (IOException e) {
				mtReqQueue.add(mtreq);    //�������ʧ�ܣ����·Żض���
				log.error("�����·�ʱIO���쳣!",e);
			}
		}
	}

	//����ͨ�ݽ������·�
	//�˵ط���synchronized������ԭ���Ƿ�ֹ��ʱ���߳������ʱ���ѵ���SMG����unbind����
	//��SMG��SP���ӶϿ�������ʱ�պ����·�ʵ��������У��������ͬ�����ƣ���ô��ʱ������Ͽ����ӣ����·��߳�����Ͽ������ӷ���
	//���ݵ���IO���쳣
	public synchronized void sendMTReq(MTReq mtreq) throws IOException {
		if (isSendUnbind || (!socket.isConnected() && socket.isClosed())) { // ��������ѶϿ�����������SMG��������
			// --------------------����
			int connectnum =0 ;//���Ӵ���
			while(true) {
				try {
					socket = SocketFactory.getDefault().createSocket(unicomIp,unicomPort);
					socket.setTcpNoDelay(true);// ������������
					socket.setTrafficClass(0x04 | 0x10);
					if(socket!=null) break;
				} catch(ConnectException e) {
					try {
						Thread.currentThread();
						Thread.sleep(1000 * 1);
					} catch(InterruptedException e1){log.error("�߳��ж�!",e1);}
					connectnum ++;
					if(connectnum >2) { //������Ӵ�������3�Σ��������ӣ������ش����
						log.fatal("δ����SMG�������ӣ����ӳ�ʱ");
						mtReqQueue.addFirst(mtreq);//δ���ͣ��Żض���
						return;
					}
					continue;
				}
			}
			if(!isBind) { //��SMG����bind
				bindstartTime = System.currentTimeMillis();
				out = new DataOutputStream(socket.getOutputStream());
				in = new DataInputStream(socket.getInputStream());
				log.info(Thread.currentThread().getName() + "��SMG��������");
				// --------------------��
				Bind bind = new Bind();
				bind.setLoginType((byte) 1);
				bind.setLoginName(spLoginName);
				bind.setLoginPassword(spLogPassword);
				bind.write(out);
				//--------------------����Ӧ
				isSendUnbind = false;
				BindResp res = (BindResp) bind.read(in);
				if (res.getResult() != 0) {
					log.fatal("SMG�ܾ����ӡ������룺" + res.getResult());
					return;
				}
				this.launchTimer();//������ʱ��
			}
		}
		// --------------------�·�
		for (int i = 0; i < mtreq.getPhoneList().size(); i++) {
			Submit submit = new Submit();
			submit.setSPNumber(mtreq.getSpNumber());
			submit.setChargeNumber("000000000000000000000");// ����21,���ȫ0��ʾ��SP֧���������ŷ���
			submit.setUserNumber(new String[] { mtreq.getPhoneList().get(i) });
			submit.setCorpId("41211");
			submit.setFeeType((byte) 0);
			submit.setFeeValue("0");
			submit.setGivenValue("0");
			submit.setAgentFlag((byte) 0);
			submit.setMorelatetoMTFlag((byte) 2); // 0-MO�㲥����ĵ�һ��MT��Ϣ
			// 1-MO�㲥����ķǵ�һ����Ϣ
			// 2-��MO�㲥�������MT��Ϣ(����ҵ��)
			// 3ϵͳ���������MT��Ϣ
			submit.setPriority((byte) 0);
			submit.setExpireTime("");
			submit.setScheduleTime("");
			submit.setReportFlag(Byte.valueOf(mtreq.getReportFlag())); // �Ƿ���SP����״̬
			submit.setTP_pid((byte) 0);
			submit.setTP_udhi((byte) 0);
			submit.setMessageCoding((byte) 15);
			submit.setMessageType((byte) 0);
			submit.setMessageContent(mtreq.getMessageContent());
			submit.setUserCoun((byte) 1); // ����sgip1.2��չЭ�������1,������Ϊҵ��Ƿ�������
			submit.setServiceType(mtreq.getServiceType());
			submit.setLinkID(mtreq.getLinkId());
			submit.write(out);
			SubmitResp submitres = (SubmitResp) submit.read(in);
			if (submitres.getResult() == 0) {
				log.info("��" + Thread.currentThread().getName()+ " ���͵�MT����ɹ��ݽ���SMG ��");
				//���·�ʵ����ӵ��ѷ���������
				mtsendedMap.put(submit.header.getSequenceNumber(),submit);
				continue ; //����������һ������
			}
			log.warn("��" + Thread.currentThread().getName()+ " ���͵�MT����ݽ���SMGʧ��!,������ " + submitres.getResult() + "��");
		}
	}
	
	/**
	 * �ü�ʱ�����ڼ����SMG������ʱ�䣬�������Ϣ���Ͷ���Ϊ�գ��ҿ���ʱ�䳬��30��
	 * ����SMG����unbind������յ�SMG��unbind_resp��Ӧ��SP�Ͽ�����
	 * 
	 * �޸ģ�2011-04-15
	 *         ��Timer��ΪScheduledExecutorService,���Timer�����Ὣ���⴫Ⱦ����ù�ĵ�����
	 * �����������·��߳�ȫ���ж�
	 * 
	 */
	public void launchTimer() {
		//�̳߳��ܰ�ʱ��ƻ���ִ�����������û��趨�ƻ�ִ�������ʱ�䣬int���͵Ĳ������趨   
	    //�̳߳����̵߳���С��Ŀ��������϶�ʱ���̳߳ؿ��ܻ��Զ���������Ĺ����߳���ִ������   
	    final ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);   

		Runnable task = new Runnable() {
			public void run() {
				if(mtReqQueue.isEmpty()) {
					currentTime = System.currentTimeMillis();
					int passedTime = (int) ((currentTime - bindstartTime) / 1000);
					if(passedTime > 30) {
						synchronized (this) { //���ж���������ֹ�ڲ��SMG��·ʱ�������߳���SMG��������,����
							                  //SMG��Ϊ�û�״̬���������ܾ������·�����
							// ��SMG����unbind����
							Unbind unbind = new Unbind();
							unbind.write(out);
							log.info(Thread.currentThread().getName()+" ��SMG����unbind����");
							UnbindResp resp = (UnbindResp) unbind.read(in);
							if (Arrays.equals(resp.header.getCommandId(),SGIPCommandDefine.SGIP_UNBIND_RESP)) {
								isSendUnbind = true; //����ѷ���unbind����
								log.info("SMG�յ�unbind���SP�ر�����");
								scheduExec.shutdown(); //��ʱֹͣ
								//�ͷ�socket��Դ
								try {
									if(in !=null) in.close();
									if(out !=null) out.close();
									if(socket !=null) socket.close();
								} catch(IOException e){
									log.warn("�ͷ�socket��Դ�����쳣");
								}
							}
						}
					}
				}
			}
		};
		scheduExec.scheduleWithFixedDelay(task, 0, 1,TimeUnit.SECONDS);   //1���Ӽ��һ��
	}
}