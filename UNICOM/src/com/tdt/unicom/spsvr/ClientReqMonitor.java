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
 * @description 业务请求监听器
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
			log.info("业务请求服务端启动,监听端口:" + LISTEN_PORT);
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
				log.error("等待客户端连接时错误", e);
			}
		}
	}
	// 处理MT下发请求线程
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
				boolean startFlag = false; //开始标志
				boolean endFlag = false;   //结束标志
				
				while(socket.isConnected() && !socket.isClosed()) {// 如果socket没有关闭
					if( !reader.ready()) {
							Thread.sleep(5);
							continue;
					} 
					while ( reader.ready() &&(character = reader.read()) != -1) {// 缓冲输入流读取就绪，已有数据可读取
						char c = (char) character;
						if(c=='{') {
							startFlag = true;
							continue;
						}
						if(c=='}') { //xml字符串结束位置
							endFlag = true;
							mtreqList.add(xmlBuffer.toString());
							startFlag = false;
							endFlag = false;
							//清空buffer
							xmlBuffer.setLength(0);
						}
						if(startFlag ==true && endFlag ==false) {
							xmlBuffer.append(c);
						} 
					}
					if (mtreqList.size() >0) {
						// 验证请求xml格式是否合法
						SAXBuilder builder = new SAXBuilder();
						Document doc = null;
						for(int i=0; i < mtreqList.size(); i ++) {
							String xmldoc = mtreqList.get(i);
							try {
								log.debug("MT请求包:\n"+xmldoc);
								doc = builder.build(new ByteArrayInputStream(xmldoc.getBytes()));
							} catch (IOException e) {
								throw new RuntimeException(e);
							} catch (JDOMException e) {
								sendDocerr(e.getMessage());
								throw new RuntimeException(e);
							}
							MTReq mtreq = new MTReq(doc);
							// 将MT请求放入待发送队列
							sender.addTask(mtreq);
						}
						//清空ArrayList
						mtreqList.clear();
					}
				}
			} catch (IOException e) {
				log.error("接收客户MT请求时IO流异常",e);
			} catch(InterruptedException e){
				log.error("接收客户MT请求的线程中断",e);
			}
		}
		/**
		 * 发送xml文档错误通知
		 */
		public void sendDocerr(String errorInfo) {
			try {
				socketout.write(errorInfo.getBytes());
				socketout.flush();
			} catch (IOException e) {
				log.error("发送xml文档格式错误出错", e);
			}
		}
	}
}
