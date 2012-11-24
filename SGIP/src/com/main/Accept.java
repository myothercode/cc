package com.main;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * 接受线程。可以将接受到的数据输出到屏幕或写入数据库
 * @author Administrator
 *
 */
public class Accept extends Thread{
	private DataInputStream dis ;
	private DataOutputStream dos;

	public Accept(DataInputStream dis,DataOutputStream dos){
		this.dis = dis;
		this.dos = dos;
	}
	
	public void run(){
		while (true){
			try{
				int messagelength = dis.readInt();
				byte b[] = new byte[messagelength-4];
				dis.readFully(b, 0, messagelength-4);
				
				ByteBuffer headbuffer = ByteBuffer.allocate(20);
				headbuffer.putInt(messagelength);
				for (int i=0;i<20-4;i++){
					headbuffer.put(b[i]);
				}
				headbuffer.flip();
				
				Head head = new Head();
				head.setBuffer(headbuffer);
				
				System.out.println("len="+head.Msglen);
				System.out.println("cmd="+head.Cmd_id);
				//System.out.println(head.JieDian);
				//System.out.println(head.CreateTime);
				//System.out.println(head.Seq_Number);
				switch (head.Cmd_id) {
				case SGIP.SGIP_BIND_RESP:
					System.out.println("绑定回应");
					break;
				case SGIP.SGIP_SUBMIT_RESP:
					System.out.println("submit 回应");
					break;
				default:
					System.out.println("未知消息");
					break;
				}				
				
				ByteBuffer bodybuffer = ByteBuffer.allocate(messagelength-20);
				for (int i=20-4;i<messagelength-4;i++){
					bodybuffer.put(b[i]);
				}
				
				bodybuffer.flip();				
				
				System.out.println("消息体");				
			
				Resp res = new Resp();
				res.setBuffer(bodybuffer);
				System.out.println("操作结果："+res.Result);
				//System.out.println(res.Reserve);
				
			}catch(Exception e){
				//e.printStackTrace();
				System.out.println("被服务器断开连接");
				break;
			}			
		}
	}
}
