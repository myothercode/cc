package com.tdt.unicom.spsvr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

import javax.net.ServerSocketFactory;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


import com.tdt.unicom.domains.Submit;
import com.tdt.unicom.sgip.svr.SPSender;

/**
 * @project UNICOM
 * @author sunnylocus
 * @vresion 1.0 2009-8-14
 * @description ҵ�����������
 */
public class ClientReqMonitor {
	private static final int LISTEN_PORT = 8805;
	private ServerSocket reqsvrSocket = null;
	private Logger log = Logger.getLogger(this.getClass());
	private SPSender sender;

	public ClientReqMonitor(Map<String, Submit> savedMap) {
		try {
			reqsvrSocket = ServerSocketFactory.getDefault().createServerSocket(LISTEN_PORT);
			sender =SPSender.getInstance(savedMap);
			log.info("ҵ��������������,�����˿�:" + LISTEN_PORT);
		} catch (IOException e) {
			log.error("launch local server error!", e);
			throw new ExceptionInInitializerError(e);
		}
	}
	public void recsvr() {
		while (true) {
			Socket reqSocket = null;
			try {
				reqSocket = reqsvrSocket.accept();
				log.info("New connection accepted" + reqSocket.getInetAddress()+ ":" + reqSocket.getPort());
				Thread workThread = new Thread(new Handler(reqSocket));
				workThread.start();
			} catch (IOException e) {
				log.error("�ȴ��ͻ�������ʱ����", e);
			}
		}
	}
	// ����MT�·������߳�
	class Handler implements Runnable {
		private Socket socket;
		private InputStream socketIn;
		private OutputStream socketout;

		public Handler(Socket socket) {
			this.socket = socket;
		}
		public void run() {
			try {
				socketIn = this.socket.getInputStream();
				socketout = this.socket.getOutputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(socketIn));
				int character = 0;
				StringBuffer xmlBuffer = new StringBuffer();
				ArrayList<String> mtreqList = new ArrayList<String>();
				boolean startFlag = false; //��ʼ��־
				boolean endFlag = false;   //������־
				
				while(socket.isConnected() && !socket.isClosed()) {// ���socketû�йر�
					if( !reader.ready()) {
							Thread.sleep(5);
							continue;
					} 
					while ( reader.ready() &&(character = reader.read()) != -1) {// ������������ȡ�������������ݿɶ�ȡ
						char c = (char) character;
						if(c=='{') {
							startFlag = true;
							continue;
						}
						if(c=='}') { //xml�ַ�������λ��
							endFlag = true;
							mtreqList.add(xmlBuffer.toString());
							startFlag = false;
							endFlag = false;
							//���buffer
							xmlBuffer.setLength(0);
						}
						if(startFlag ==true && endFlag ==false) {
							xmlBuffer.append(c);
						} 
					}
					if (mtreqList.size() >0) {
						// ��֤����xml��ʽ�Ƿ�Ϸ�
						SAXBuilder builder = new SAXBuilder();
						Document doc = null;
						for(int i=0; i < mtreqList.size(); i ++) {
							String xmldoc = mtreqList.get(i);
							try {
								log.debug("MT�����:\n"+xmldoc);
								doc = builder.build(new ByteArrayInputStream(xmldoc.getBytes()));
							} catch (IOException e) {
								throw new RuntimeException(e);
							} catch (JDOMException e) {
								sendDocerr(e.getMessage());
								throw new RuntimeException(e);
							}
							MTReq mtreq = new MTReq(doc);
							// ��MT�����������Ͷ���
							sender.addTask(mtreq);
						}
						//���ArrayList
						mtreqList.clear();
					}
				}
			} catch (IOException e) {
				log.error("���տͻ�MT����ʱIO���쳣",e);
			} catch(InterruptedException e){
				log.error("���տͻ�MT������߳��ж�",e);
			}
		}
		/**
		 * ����xml�ĵ�����֪ͨ
		 */
		public void sendDocerr(String errorInfo) {
			try {
				socketout.write(errorInfo.getBytes());
				socketout.flush();
			} catch (IOException e) {
				log.error("����xml�ĵ���ʽ�������", e);
			}
		}
	}
}
