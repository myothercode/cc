package com.tdt.unicom.sgip.svr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.tdt.unicom.domains.Bind;
import com.tdt.unicom.domains.BindResp;
import com.tdt.unicom.domains.Deliver;
import com.tdt.unicom.domains.DeliverResp;
import com.tdt.unicom.domains.Report;
import com.tdt.unicom.domains.ReportResp;
import com.tdt.unicom.domains.SGIPCommand;
import com.tdt.unicom.domains.SGIPCommandDefine;
import com.tdt.unicom.domains.SGIPHeader;
import com.tdt.unicom.domains.Submit;
import com.tdt.unicom.domains.UnbindResp;
import com.tdt.unicom.domains.UserRpt;
import com.tdt.unicom.domains.UserRptResp;
import com.tdt.unicom.spsvr.MOResp;

/**
 * @project UNICOM
 * @author sunnylocus
 * @vresion 1.0 2009-8-17
 * @description  服务端监听器，监听来自SMG的数据
 */
public class UnicomSPMonitor {
	
	private static final int LISTEN_PORT = 8801;
	private ServerSocket spsvrSocket = null;
	private final static Logger log = Logger.getLogger(UnicomSPMonitor.class);
	
	private Map<String, Submit> savedmap;
	private final static Map<String,ProductConfBean> TRANSMIT_MAP=new HashMap<String, ProductConfBean>();
	private final static LinkedList<Thread> THREAD_LSIT = new LinkedList<Thread>();
	
	public UnicomSPMonitor(Map<String, Submit> map) {
		try {
			this.savedmap = map;
			Class.forName("com.tdt.unicom.sgip.svr.UnicomSPMonitor$ProductConfBean");
			spsvrSocket = ServerSocketFactory.getDefault().createServerSocket(LISTEN_PORT);
			log.info("本地unicom服务端启动,监听端口:"+LISTEN_PORT);
		} catch (IOException e) {
			log.error("launch local server error!",e);
			throw new ExceptionInInitializerError(e);
		} catch(ClassNotFoundException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public void recsvr() {
		while(true) {
			Socket unicomSocket = null;
			try {
				unicomSocket = spsvrSocket.accept();
				unicomSocket.setSoLinger(true, 0);   	//socket关闭时，不再发送缓冲区里的数据，立即释放低层资源
				unicomSocket.setTcpNoDelay(true);   	//不使用缓冲区，立即发送数据
				unicomSocket.setTrafficClass(0x04|0x10);
				//unicomSocket.setSoTimeout(1000 * 60);
				Thread workThread = new Thread(new Handler(unicomSocket));
				workThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	//负责与联通的服务器的通信并将MO信息转发相应的业务层
	class Handler extends SGIPCommand implements Runnable {  
		private Socket socket = null;
		private DataInputStream unicomIn = null;
		private DataOutputStream spout = null;
		
		public Handler(Socket socket) {
			this.socket = socket;
			log.info("New connection accepted from "+ socket.getInetAddress()+":"+socket.getPort());
		}
		public void run() {
			try {
				boolean isunbind = false;  //收到unbind命令后，退出循环
				unicomIn = new DataInputStream(socket.getInputStream());
				spout=new DataOutputStream(socket.getOutputStream());
				//读取联通发送来的字节流
				while(!isunbind && !socket.isInputShutdown()){
					SGIPCommand command=read(unicomIn);
					log.info("【"+Thread.currentThread().getName()+"收到SMG "+SGIPCommandDefine.getCommandName(this.header.getCommandId())+"命令】,{长度="+command.header.getTotalmsglen()+",类型= "+SGIPCommand.Bytes4ToInt(command.header.getCommandId())+",序列="+command.header.getSequenceNumber()+"}");
					switch (Bytes4ToInt(command.header.getCommandId())) {
					    //-----------------------------------
						case 0x1:  //联通向SP发送的绑定命令
							log.info("收到SMG ->Bind命令");
							Bind bind = (Bind)command;
							log.info("LoginType:"+bind.getLoginType());
							log.info("LoginName:"+bind.getLoginName());
							log.info("LoginPassword:"+bind.getLoginPassword());
							if(bind.getLoginType()==2) { // 登陆类型2为SMG向SP建立的连接，用于发送命令
								BindResp bindresp = new BindResp(command.header.getUnicomSN()); //绑定响应命令
								bindresp.setResult((byte)1);
								if(bind.getLoginName().equals("10628365") && bind.getLoginPassword().equalsIgnoreCase("10628365")) {
									bindresp.setResult((byte) 0);
								}
								bindresp.write(spout);
							}
							break;
					    //------------------------------------
						case 0x2: //联通向SP发送的注销绑定命令
							//响应
							log.info("收到SMG ->Unbind命令");
							UnbindResp resp = new UnbindResp(command.header.getUnicomSN());
							resp.write(spout);
							isunbind = true;
							break;
						//------------------------------------
						case 0x4: //联通向SP上行一条用户短信
							log.info("收到SMG ->Deliver命令");
							Deliver deliver = (Deliver)command;
							log.info("SPNumber:"+deliver.getSPNumber());
							log.info("UserNumber:"+deliver.getUserNumber());
							log.info("MessageContent:"+deliver.getMessageContent());
							log.info("LinkID:"+deliver.getLinkID());
							//收到响应
							DeliverResp deliverresp = new DeliverResp(command.header.getUnicomSN());
							deliverresp.setResult((byte)0);
							deliverresp.write(spout);
							transmitInfo(deliver); //上行转发
							break;
						//-------------------------------------
						case 0x5: //联通向SP报告之前一条MT的状态
							log.info("收到SMG ->Report命令");
							final Report report =(Report) command;
							log.info("ReportType:"+report.getReportType());
							log.info("UserNumber:"+report.getUserNumber());
							log.info("State:"+report.getState());
							log.info("ErrorCode:"+report.getErrorCode());
							//返回响应
							ReportResp reportResp = new ReportResp(command.header.getUnicomSN());
							reportResp.setResult((byte)0);
							reportResp.write(spout);
							if(report.getReportType()==0) {//对先前的一条Submit命令的状态报告
								transmitInfo(report);
							}
							break;
						//--------------------------------------
						case 0x11: //联通向SP报告一条手机用户的状态信息
							log.info("收到SMG ->UserRpt命令");
							UserRpt userRpt = (UserRpt) command;
							log.info("SPNumber:"+userRpt.getSPNumber());
							log.info("UserNumber:"+userRpt.getUserNumber());
							log.info("UserCondition:"+userRpt.getUserCondition());
							//响应
							UserRptResp userRptresp = new UserRptResp(command.header.getUnicomSN());
							userRptresp.setResult((byte)0);
							break;
						default:
							log.error("error!! -->default:"+Bytes4ToInt(command.header.getCommandId()));
							break;
					}
				}
			} catch(RuntimeException e) {
				log.warn("SMG强制关闭通信链路");
			} catch(IOException e){
				log.error("IO流异常", e);
			} finally {
				try {
					if(socket !=null) {
						spout.close();
						unicomIn.close();
						socket.close();
						log.info("SMG与SP通信结束,链路关闭.\n");
					}
				}catch(IOException e) {e.printStackTrace();}
			}
		}
		/**
		 * 转发信息
		 * @param command
		 */
		public void transmitInfo(final SGIPCommand command) {
			if(THREAD_LSIT.size() !=0) {
				for(int i=0; i < THREAD_LSIT.size(); i++) {
					if(!THREAD_LSIT.get(i).isAlive()||!THREAD_LSIT.get(i).isInterrupted()) {
						THREAD_LSIT.remove(i);
					}
				}
				if(THREAD_LSIT.size() > 10) {
					log.warn("警告！当前转发线程数量过多,当前数量:"+THREAD_LSIT.size());
					THREAD_LSIT.removeFirst().interrupt(); //中断转发线程运行时间最长的一个线程
				}
			}
			//开启线程转发
			Thread transThrad= new Thread(new Runnable() {
				public void run() {
					ProductConfBean bean = null;
					String info = null;
					String destIp="";
					int destPort =0;
					if(command instanceof Report) {
						Report report = (Report)command;
						Submit submit=savedmap.get(report.getSubmitSequenceNumber());  
						if(submit ==null) {
							return ;
						}
						//根据服务商号转发
						bean = TRANSMIT_MAP.get(submit.getSPNumber().trim());
						if(bean ==null) {
							return ;
						}
					    destIp = bean.getTransmitIp();
					    destPort = bean.getDestPort();
						//参数说明见 TDT协议 短信平台中间格式 .doc
						MOResp moresp = new MOResp();
						moresp.setLinkId(submit.getLinkID());
						moresp.setMessageCoding(15);
						moresp.setSpNumber(submit.getSPNumber());
						moresp.setUserNumberType(2);//1 、移动 2、联通 3、电信
						moresp.setReportFlag(1);   
						moresp.setServiceType(submit.getServiceType());
						moresp.setUserNumber(submit.getUserNumber()[0]);
						switch (report.getState()) {
						case 0:
							moresp.setMessageContent("发送成功 ");
							break;
						case 1:
							moresp.setMessageContent("等待发送");
							break;
						case 2:
							moresp.setMessageContent("发送失败,错误码:"+report.getErrorCode());
							break;
						default:
							moresp.setMessageContent("状态未知");
							break;
						}
						info = moresp.toXmlStr();
					} else if(command instanceof Deliver) {
						Deliver deliver = (Deliver) command;
						bean = TRANSMIT_MAP.get(deliver.getSPNumber());
						if(bean ==null) {
							return ;
						}
						destIp = bean.getTransmitIp();
					    destPort = bean.getDestPort();
						MOResp moresp = new MOResp();
						//参数说明见 TDT协议 短信平台中间格式 .doc
						moresp.setLinkId(deliver.getLinkID());
						moresp.setMessageCoding(deliver.getMessageCoding());
						moresp.setMessageContent(deliver.getMessageContent());
						moresp.setReportFlag(0);
						moresp.setServiceType("00000000");
						moresp.setSpNumber(deliver.getSPNumber());
						moresp.setUserNumber(deliver.getUserNumber());
						moresp.setUserNumberType(2);
						info = moresp.toXmlStr();
					} else {
						log.warn("未知的命令");
						return;
					}
					log.debug("转向地址:"+destIp+":"+destPort);
					Socket mosocket = null;
					OutputStream out = null;
					boolean isconnected = false; //是否已与业务层建立连接
					boolean issuccess = false;   //信息是否成功转发至业务层
					int connectNum =0;
					while(!issuccess && !Thread.currentThread().isInterrupted()) {
						while(!isconnected && !Thread.currentThread().isInterrupted()) {
							try {
								mosocket = SocketFactory.getDefault().createSocket(destIp, destPort);
								mosocket.setTcpNoDelay(true);//数据不作缓冲
								isconnected = true;
							} catch (SocketException e) {
								connectNum ++;
								if(connectNum >3600 * 24) {
									return; //24小时未连接成功，转发信息丢弃。
								}
								log.warn(Thread.currentThread().getName()+"第"+connectNum+"次连接超时,IP="+destIp+",Port="+destPort+"."+e.getMessage());
								try {
									Thread.currentThread();
									Thread.sleep( 1000 * 5);
								} catch(InterruptedException ex) {log.warn("转发线程中断!");}
							} catch (IOException e) {
								log.error("向业务层建立连接时IO流异常",e);
							} 
						}
						try {
							log.debug(Thread.currentThread().getName()+" MO转发:\n"+info);
							out = mosocket.getOutputStream();
							out.write(info.getBytes());
							out.flush();
							mosocket.shutdownOutput();
							issuccess = true;
						} catch (IOException e) {
							log.error("向业务层转发信息时发生IO流异常",e);
							issuccess = false;
						} finally {
							try {
								if(out !=null) {
									out.close();
								}
								if(mosocket!=null) {
									mosocket.close();
								}
							} catch(IOException e) {log.error("释放socket资源出错",e);}
						}
					}
				}
			});
			transThrad.start();
			THREAD_LSIT.add(transThrad);
		} // end transmit method
	}
	//产品配置类
	static class ProductConfBean {
		static {
			try {
				SAXBuilder builder = new SAXBuilder();
				Document doc = null;
				String osname = System.getProperty("os.name");
				Pattern ospn = Pattern.compile("^windows.*$",Pattern.CASE_INSENSITIVE); //大小写不敏感
				String confpath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
				if(ospn.matcher(osname).matches()) { //windows操作系统
					log.info("操作系统名称:"+osname+",配置文件路径:"+System.getProperty("project.home")+"\\productionConf.xml");
					doc=builder.build(confpath+"\\productionConf.xml");
				} else {//Linux系统
					log.info("操作系统名称:"+osname+",配置文件路径:"+System.getProperty("project.home")+"/productionConf.xml");
					doc=builder.build(confpath+"/productionConf.xml");
				}
				//判断操作系统 
				//根目录
				Element productionConf = doc.getRootElement();
				Element uncomConf = productionConf.getChild("unicomconf");
				//获取联通短信网关ip及port
				SPSender.unicomIp = uncomConf.getChildText("ipaddr");
				SPSender.unicomPort = Integer.valueOf(uncomConf.getChildText("addrport"));
				SGIPHeader.setSrcNodeId(uncomConf.getChildText("spNodeid"));
				SPSender.spLoginName = uncomConf.getChildText("spUserName");
				SPSender.spLogPassword = uncomConf.getChildText("spPassword");
				//配置实例
				List<Element> productconfList = productionConf.getChildren("productconf");
				log.info("***************** 加载网关配置  ********************");
				log.info("联通SMG地址:"+SPSender.unicomIp);
				log.info("联通SMG端口:"+SPSender.unicomPort);
				log.info("源节点编号："+SGIPHeader.getSrcNodeId());
				log.info("登陆用户名："+SPSender.spLoginName);
				log.info("登陆密码："+SPSender.spLogPassword);
				log.info("***************** 网关配置完成  ********************\n");
				log.info("***************** 加载转发配置  ********************");
				for(Element productconf : productconfList) {
					//--------------------------产品配置类实例化
					ProductConfBean productbean = new ProductConfBean();
					productbean.setName(productconf.getChildTextTrim("name"));
					productbean.setSpnumber(productconf.getChildTextTrim("spnumber"));
					productbean.setServiceType(productconf.getChildTextTrim("servicetype"));
					productbean.setTransmitIp(productconf.getChildTextTrim("transmitip"));
					productbean.setDestPort(Integer.valueOf(productconf.getChildTextTrim("destport")));
					//--------------------------添加到PRODUCT_REPLY_MAP中
					TRANSMIT_MAP.put(productconf.getChildTextTrim("spnumber"),productbean);          //以spnumber为key,当收到上行或着状态报告时，根据 spnumber取出对应的转发地址
					log.info("产品名称:"+productbean.getName());
					log.info("服务商号:"+productbean.getSpnumber());
					log.info("服务代码:"+productbean.getServiceType());
					log.info("转发地址:"+productbean.getTransmitIp()+":"+productbean.getDestPort()+"\n");
				}
				log.info("***************** 转发配置结束  ********************");
			} catch(JDOMException e){
				log.error("解析转发配置文件出错!",e);
			} catch(IOException e) {
				log.error("解析转发配置文件发生IO流异常",e);
			}
		}
		private String name;            //产品名称
		private String spnumber;        //产品服务商号
		private String serviceType;     //产品服务代码
		private String transmitIp;      //短信上行及状态报告转发地址
		private int destPort;           //目的端口

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getSpnumber() {
			return spnumber;
		}
		public void setSpnumber(String spnumber) {
			this.spnumber = spnumber;
		}
		public String getServiceType() {
			return serviceType;
		}
		public void setServiceType(String serviceType) {
			this.serviceType = serviceType;
		}
		public String getTransmitIp() {
			return transmitIp;
		}
		public void setTransmitIp(String transmitIp) {
			this.transmitIp = transmitIp;
		}
		public int getDestPort() {
			return destPort;
		}
		public void setDestPort(int destPort) {
			this.destPort = destPort;
		}
    }
}
