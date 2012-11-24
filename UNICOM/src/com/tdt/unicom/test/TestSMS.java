package com.tdt.unicom.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.apache.xerces.impl.dv.util.Base64;


public class TestSMS {
	public static void send() {
		try {
			String xmlbody = "{<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<gwsmip>\n" + "  <message_header>\n"
					+ "    <command_id>0x3</command_id>\n"
					+ "    <sequence_number/>\n" + "  </message_header>\n"
					+ "  <message_body>\n" + "    <pk_total>1</pk_total>\n"
					+ "    <pk_number>1</pk_number>\n" + "    <user_numbers>\n"
					+ "       <user_number>13075364025</user_number>\n"
					+ "    </user_numbers>\n"
					+ "    <sp_number>1062836516</sp_number>\n"
					+ "    <service_type>DXAC</service_type>\n"
					+ "    <message_content>" +Base64.encode("(CTR,000)".getBytes())
					+ "</message_content>\n"
					+ "    <report_flag>0</report_flag>\n"
					+ "   </message_body>\n" 
					+ "</gwsmip>\n}";

			Socket socket = SocketFactory.getDefault().createSocket("192.168.0.23", 8805);
			OutputStream out = socket.getOutputStream();
			while (true) {
				System.out.println(xmlbody);
				out.write(xmlbody.getBytes());
				Thread.currentThread();
				Thread.sleep(1000 * 30);
			}
		} catch (ConnectException e) {
			System.err.println("ConnectException:"+e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch(InterruptedException e) {
			System.err.println(e.getMessage());
		} 
	}
	
	public static void recResp() throws IOException{
		ServerSocket server = ServerSocketFactory.getDefault().createServerSocket(7750);
		
		class Worker implements Runnable {
			private Socket socket;
			
			public Worker(Socket socket) {
				this.socket = socket;
				
			}
			public void run() {
				try {
					InputStream in = socket.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					String temp ="";
					String result="";
					while((temp = reader.readLine())!=null) {
						System.out.println(temp);
						result += temp;
					}
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		while(!Thread.interrupted()) {
			Socket socket = server.accept();
			System.out.println("New connection accepted from "+socket.getInetAddress()+":"+socket.getPort());
			new Thread(new Worker(socket)).start();
		}
		
	}
	public static void main(String[] args)  {
		new Thread(new Runnable() {
			public void run() {
				send();//∑¢ÀÕ
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				try {
					recResp();
				} catch (IOException e) {
					e.printStackTrace();
				}//Ω” ‹
			}
		}).start();
	}
}
