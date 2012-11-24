package com.main;


import java.io.*;
import java.net.*;

public class trsMain {
	private Socket mySocket;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	public trsMain(){
		try{
			//mySocket = new Socket("192.168.1.9",8801);
			//mySocket = new Socket("10.143.4.71",8801);
			//System.out.println("正在连接到"+mySocket.getInetAddress().getHostAddress());
			//dis = new DataInputStream(mySocket.getInputStream());
			//dos = new DataOutputStream(mySocket.getOutputStream());			
			
			//发送线程
			//Send send = new Send(dis,dos);
			//send.start();
			//接收线程
			//Accept accept =  new Accept(dis,dos);
			//accept.start();
			
			//启动服务器线程
			ServerSocket s = new ServerSocket(16336);
    		for (;;){
				Socket incoming = s.accept();    			
				Thread t = new Processor(incoming);   
				t.start();
    		} 
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		new trsMain();
	}

}
