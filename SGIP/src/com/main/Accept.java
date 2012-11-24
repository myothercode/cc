package com.main;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * �����̡߳����Խ����ܵ��������������Ļ��д�����ݿ�
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
					System.out.println("�󶨻�Ӧ");
					break;
				case SGIP.SGIP_SUBMIT_RESP:
					System.out.println("submit ��Ӧ");
					break;
				default:
					System.out.println("δ֪��Ϣ");
					break;
				}				
				
				ByteBuffer bodybuffer = ByteBuffer.allocate(messagelength-20);
				for (int i=20-4;i<messagelength-4;i++){
					bodybuffer.put(b[i]);
				}
				
				bodybuffer.flip();				
				
				System.out.println("��Ϣ��");				
			
				Resp res = new Resp();
				res.setBuffer(bodybuffer);
				System.out.println("���������"+res.Result);
				//System.out.println(res.Reserve);
				
			}catch(Exception e){
				//e.printStackTrace();
				System.out.println("���������Ͽ�����");
				break;
			}			
		}
	}
}
