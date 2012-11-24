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
 * @description  ����˼���������������SMG������
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
			log.info("����unicom���������,�����˿�:"+LISTEN_PORT);
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
				unicomSocket.setSoLinger(true, 0);   	//socket�ر�ʱ�����ٷ��ͻ�����������ݣ������ͷŵͲ���Դ
				unicomSocket.setTcpNoDelay(true);   	//��ʹ�û�������������������
				unicomSocket.setTrafficClass(0x04|0x10);
				//unicomSocket.setSoTimeout(1000 * 60);
				Thread workThread = new Thread(new Handler(unicomSocket));
				workThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	//��������ͨ�ķ�������ͨ�Ų���MO��Ϣת����Ӧ��ҵ���
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
				boolean isunbind = false;  //�յ�unbind������˳�ѭ��
				unicomIn = new DataInputStream(socket.getInputStream());
				spout=new DataOutputStream(socket.getOutputStream());
				//��ȡ��ͨ���������ֽ���
				while(!isunbind && !socket.isInputShutdown()){
					SGIPCommand command=read(unicomIn);
					log.info("��"+Thread.currentThread().getName()+"�յ�SMG "+SGIPCommandDefine.getCommandName(this.header.getCommandId())+"���,{����="+command.header.getTotalmsglen()+",����= "+SGIPCommand.Bytes4ToInt(command.header.getCommandId())+",����="+command.header.getSequenceNumber()+"}");
					switch (Bytes4ToInt(command.header.getCommandId())) {
					    //-----------------------------------
						case 0x1:  //��ͨ��SP���͵İ�����
							log.info("�յ�SMG ->Bind����");
							Bind bind = (Bind)command;
							log.info("LoginType:"+bind.getLoginType());
							log.info("LoginName:"+bind.getLoginName());
							log.info("LoginPassword:"+bind.getLoginPassword());
							if(bind.getLoginType()==2) { // ��½����2ΪSMG��SP���������ӣ����ڷ�������
								BindResp bindresp = new BindResp(command.header.getUnicomSN()); //����Ӧ����
								bindresp.setResult((byte)1);
								if(bind.getLoginName().equals("10628365") && bind.getLoginPassword().equalsIgnoreCase("10628365")) {
									bindresp.setResult((byte) 0);
								}
								bindresp.write(spout);
							}
							break;
					    //------------------------------------
						case 0x2: //��ͨ��SP���͵�ע��������
							//��Ӧ
							log.info("�յ�SMG ->Unbind����");
							UnbindResp resp = new UnbindResp(command.header.getUnicomSN());
							resp.write(spout);
							isunbind = true;
							break;
						//------------------------------------
						case 0x4: //��ͨ��SP����һ���û�����
							log.info("�յ�SMG ->Deliver����");
							Deliver deliver = (Deliver)command;
							log.info("SPNumber:"+deliver.getSPNumber());
							log.info("UserNumber:"+deliver.getUserNumber());
							log.info("MessageContent:"+deliver.getMessageContent());
							log.info("LinkID:"+deliver.getLinkID());
							//�յ���Ӧ
							DeliverResp deliverresp = new DeliverResp(command.header.getUnicomSN());
							deliverresp.setResult((byte)0);
							deliverresp.write(spout);
							transmitInfo(deliver); //����ת��
							break;
						//-------------------------------------
						case 0x5: //��ͨ��SP����֮ǰһ��MT��״̬
							log.info("�յ�SMG ->Report����");
							final Report report =(Report) command;
							log.info("ReportType:"+report.getReportType());
							log.info("UserNumber:"+report.getUserNumber());
							log.info("State:"+report.getState());
							log.info("ErrorCode:"+report.getErrorCode());
							//������Ӧ
							ReportResp reportResp = new ReportResp(command.header.getUnicomSN());
							reportResp.setResult((byte)0);
							reportResp.write(spout);
							if(report.getReportType()==0) {//����ǰ��һ��Submit�����״̬����
								transmitInfo(report);
							}
							break;
						//--------------------------------------
						case 0x11: //��ͨ��SP����һ���ֻ��û���״̬��Ϣ
							log.info("�յ�SMG ->UserRpt����");
							UserRpt userRpt = (UserRpt) command;
							log.info("SPNumber:"+userRpt.getSPNumber());
							log.info("UserNumber:"+userRpt.getUserNumber());
							log.info("UserCondition:"+userRpt.getUserCondition());
							//��Ӧ
							UserRptResp userRptresp = new UserRptResp(command.header.getUnicomSN());
							userRptresp.setResult((byte)0);
							break;
						default:
							log.error("error!! -->default:"+Bytes4ToInt(command.header.getCommandId()));
							break;
					}
				}
			} catch(RuntimeException e) {
				log.warn("SMGǿ�ƹر�ͨ����·");
			} catch(IOException e){
				log.error("IO���쳣", e);
			} finally {
				try {
					if(socket !=null) {
						spout.close();
						unicomIn.close();
						socket.close();
						log.info("SMG��SPͨ�Ž���,��·�ر�.\n");
					}
				}catch(IOException e) {e.printStackTrace();}
			}
		}
		/**
		 * ת����Ϣ
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
					log.warn("���棡��ǰת���߳���������,��ǰ����:"+THREAD_LSIT.size());
					THREAD_LSIT.removeFirst().interrupt(); //�ж�ת���߳�����ʱ�����һ���߳�
				}
			}
			//�����߳�ת��
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
						//���ݷ����̺�ת��
						bean = TRANSMIT_MAP.get(submit.getSPNumber().trim());
						if(bean ==null) {
							return ;
						}
					    destIp = bean.getTransmitIp();
					    destPort = bean.getDestPort();
						//����˵���� TDTЭ�� ����ƽ̨�м��ʽ .doc
						MOResp moresp = new MOResp();
						moresp.setLinkId(submit.getLinkID());
						moresp.setMessageCoding(15);
						moresp.setSpNumber(submit.getSPNumber());
						moresp.setUserNumberType(2);//1 ���ƶ� 2����ͨ 3������
						moresp.setReportFlag(1);   
						moresp.setServiceType(submit.getServiceType());
						moresp.setUserNumber(submit.getUserNumber()[0]);
						switch (report.getState()) {
						case 0:
							moresp.setMessageContent("���ͳɹ� ");
							break;
						case 1:
							moresp.setMessageContent("�ȴ�����");
							break;
						case 2:
							moresp.setMessageContent("����ʧ��,������:"+report.getErrorCode());
							break;
						default:
							moresp.setMessageContent("״̬δ֪");
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
						//����˵���� TDTЭ�� ����ƽ̨�м��ʽ .doc
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
						log.warn("δ֪������");
						return;
					}
					log.debug("ת���ַ:"+destIp+":"+destPort);
					Socket mosocket = null;
					OutputStream out = null;
					boolean isconnected = false; //�Ƿ�����ҵ��㽨������
					boolean issuccess = false;   //��Ϣ�Ƿ�ɹ�ת����ҵ���
					int connectNum =0;
					while(!issuccess && !Thread.currentThread().isInterrupted()) {
						while(!isconnected && !Thread.currentThread().isInterrupted()) {
							try {
								mosocket = SocketFactory.getDefault().createSocket(destIp, destPort);
								mosocket.setTcpNoDelay(true);//���ݲ�������
								isconnected = true;
							} catch (SocketException e) {
								connectNum ++;
								if(connectNum >3600 * 24) {
									return; //24Сʱδ���ӳɹ���ת����Ϣ������
								}
								log.warn(Thread.currentThread().getName()+"��"+connectNum+"�����ӳ�ʱ,IP="+destIp+",Port="+destPort+"."+e.getMessage());
								try {
									Thread.currentThread();
									Thread.sleep( 1000 * 5);
								} catch(InterruptedException ex) {log.warn("ת���߳��ж�!");}
							} catch (IOException e) {
								log.error("��ҵ��㽨������ʱIO���쳣",e);
							} 
						}
						try {
							log.debug(Thread.currentThread().getName()+" MOת��:\n"+info);
							out = mosocket.getOutputStream();
							out.write(info.getBytes());
							out.flush();
							mosocket.shutdownOutput();
							issuccess = true;
						} catch (IOException e) {
							log.error("��ҵ���ת����Ϣʱ����IO���쳣",e);
							issuccess = false;
						} finally {
							try {
								if(out !=null) {
									out.close();
								}
								if(mosocket!=null) {
									mosocket.close();
								}
							} catch(IOException e) {log.error("�ͷ�socket��Դ����",e);}
						}
					}
				}
			});
			transThrad.start();
			THREAD_LSIT.add(transThrad);
		} // end transmit method
	}
	//��Ʒ������
	static class ProductConfBean {
		static {
			try {
				SAXBuilder builder = new SAXBuilder();
				Document doc = null;
				String osname = System.getProperty("os.name");
				Pattern ospn = Pattern.compile("^windows.*$",Pattern.CASE_INSENSITIVE); //��Сд������
				String confpath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
				if(ospn.matcher(osname).matches()) { //windows����ϵͳ
					log.info("����ϵͳ����:"+osname+",�����ļ�·��:"+System.getProperty("project.home")+"\\productionConf.xml");
					doc=builder.build(confpath+"\\productionConf.xml");
				} else {//Linuxϵͳ
					log.info("����ϵͳ����:"+osname+",�����ļ�·��:"+System.getProperty("project.home")+"/productionConf.xml");
					doc=builder.build(confpath+"/productionConf.xml");
				}
				//�жϲ���ϵͳ 
				//��Ŀ¼
				Element productionConf = doc.getRootElement();
				Element uncomConf = productionConf.getChild("unicomconf");
				//��ȡ��ͨ��������ip��port
				SPSender.unicomIp = uncomConf.getChildText("ipaddr");
				SPSender.unicomPort = Integer.valueOf(uncomConf.getChildText("addrport"));
				SGIPHeader.setSrcNodeId(uncomConf.getChildText("spNodeid"));
				SPSender.spLoginName = uncomConf.getChildText("spUserName");
				SPSender.spLogPassword = uncomConf.getChildText("spPassword");
				//����ʵ��
				List<Element> productconfList = productionConf.getChildren("productconf");
				log.info("***************** ������������  ********************");
				log.info("��ͨSMG��ַ:"+SPSender.unicomIp);
				log.info("��ͨSMG�˿�:"+SPSender.unicomPort);
				log.info("Դ�ڵ��ţ�"+SGIPHeader.getSrcNodeId());
				log.info("��½�û�����"+SPSender.spLoginName);
				log.info("��½���룺"+SPSender.spLogPassword);
				log.info("***************** �����������  ********************\n");
				log.info("***************** ����ת������  ********************");
				for(Element productconf : productconfList) {
					//--------------------------��Ʒ������ʵ����
					ProductConfBean productbean = new ProductConfBean();
					productbean.setName(productconf.getChildTextTrim("name"));
					productbean.setSpnumber(productconf.getChildTextTrim("spnumber"));
					productbean.setServiceType(productconf.getChildTextTrim("servicetype"));
					productbean.setTransmitIp(productconf.getChildTextTrim("transmitip"));
					productbean.setDestPort(Integer.valueOf(productconf.getChildTextTrim("destport")));
					//--------------------------��ӵ�PRODUCT_REPLY_MAP��
					TRANSMIT_MAP.put(productconf.getChildTextTrim("spnumber"),productbean);          //��spnumberΪkey,���յ����л���״̬����ʱ������ spnumberȡ����Ӧ��ת����ַ
					log.info("��Ʒ����:"+productbean.getName());
					log.info("�����̺�:"+productbean.getSpnumber());
					log.info("�������:"+productbean.getServiceType());
					log.info("ת����ַ:"+productbean.getTransmitIp()+":"+productbean.getDestPort()+"\n");
				}
				log.info("***************** ת�����ý���  ********************");
			} catch(JDOMException e){
				log.error("����ת�������ļ�����!",e);
			} catch(IOException e) {
				log.error("����ת�������ļ�����IO���쳣",e);
			}
		}
		private String name;            //��Ʒ����
		private String spnumber;        //��Ʒ�����̺�
		private String serviceType;     //��Ʒ�������
		private String transmitIp;      //�������м�״̬����ת����ַ
		private int destPort;           //Ŀ�Ķ˿�

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
