package com.main;
import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import com.db.*;

public class Processor extends Thread {

	private Boolean isRun = true;
	private DataOutputStream dos;
	private DataInputStream dis;
	private DBOper db = DBOper.getInstance();

	public Processor(Socket socket) {		
		try{
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("对方ip地址："+socket.getInetAddress().getHostAddress());
		System.out.println("对方端口："+socket.getPort());
	}
	public void run(){	

		while (isRun) {
			try {
				int messagelength = dis.readInt();

				byte b[] = new byte[messagelength - 4];
				dis.readFully(b, 0, messagelength - 4);

				//消息头
				ByteBuffer headbuffer = ByteBuffer.allocate(20);
				headbuffer.putInt(messagelength);
				for (int i = 0; i < 20 - 4; i++) {
					headbuffer.put(b[i]);
				}
				headbuffer.flip();

				Head head = new Head();
				head.setBuffer(headbuffer);

				switch (head.Cmd_id) {
				case SGIP.SGIP_BIND: // 绑定请求
					ServerResBind(b,messagelength);
					break;
				case SGIP.SGIP_DELIVER: // 短信上行请求
					ServerResDeliver(b,messagelength,head.CreateTime,head.Seq_Number);
					break;
				case SGIP.SGIP_REPORT: // 报告请求
					ServerResReport(b,messagelength);
					break;
				case SGIP.SGIP_UNBIND: // 反绑定请求
					ServerResUnbind();
					break;
				default:
					break;
				}

			} catch (Exception e) {

				isRun = false;
				break;
			}
		}
	}

	private void ServerResReport(byte b[],int messagelength)throws Exception{
		
		ByteBuffer headbuff = ByteBuffer.allocate(16);//获取消息头（本来是20，但是省去了消息长途位）
		for(int l=0;l<16;l++){
			headbuff.put(b[l]);
		}
		headbuff.flip();
		int cmd=headbuff.getInt();
		int jd=headbuff.getInt();
		int ctime=headbuff.getInt();
		int sq=headbuff.getInt();
		System.out.println("reportRes回应消息头: "+jd+";"+ctime+";"+sq);
		
		ByteBuffer bodybuffer = ByteBuffer.allocate(messagelength - 20);
		for (int i = 20 - 4; i < messagelength - 4; i++) {
			bodybuffer.put(b[i]);
		}
		bodybuffer.flip();
		
		Report report = new Report();
		report.setBuffer(bodybuffer);
		
		System.out.println("收到报告");
		System.out.println("节点="+report.JieDian);
		System.out.println("time="+report.CreateTime);
		System.out.println("序号="+report.Seq_Number);
		System.out.println("用户号码="+report.UserNumber);
		System.out.println("state="+report.State);
		System.out.println("ErrorCode="+report.ErrorCode);
		System.out.println("Reserve="+report.Reserve);
		
		db.savelog(report.UserNumber, report.State,report.CreateTime,report.Reserve);
		
		ByteBuffer resp2=ByteBuffer.allocate(29);
		resp2.putInt(29);
		resp2.putInt(SGIP.SGIP_REPORT_RESP);
		resp2.putInt(jd);		
		resp2.putInt(ctime);
		resp2.putInt(sq);
		resp2.put((byte)0);
		for(int i=0;i<8;i++){
			resp2.put((byte)0);					
		}
		resp2.flip();
		dos.write(resp2.array());
		dos.flush();
		System.out.println("reportResp回应完毕==========================================");
	}
	/**
	 * 反绑定请求
	 * @throws Exception
	 */
	private void ServerResUnbind() throws Exception{
		Head head = new Head();
		head.Msglen = 20;
		head.Cmd_id = SGIP.SGIP_UNBIND_RESP;
		
		ByteBuffer sendBuffer = head.getBuffer();
		
		dos.write(sendBuffer.array());
		dos.flush();

	}
	/**
	 * 回应deliver
	 * @param buffer
	 * @throws Exception
	 */
	private void ServerResDeliver(byte b[],int messagelength,int Createtime,int Seq_No) throws Exception{
		
		ByteBuffer headbuff = ByteBuffer.allocate(16);//获取消息头（本来是20，但是省去了消息长途位）
		for(int l=0;l<16;l++){
			headbuff.put(b[l]);
		}
		headbuff.flip();
		int cmd=headbuff.getInt();
		int jd=headbuff.getInt();
		int ctime=headbuff.getInt();
		int sq=headbuff.getInt();
		System.out.println("deliverResp回应的消息头: "+jd+";"+ctime+";"+sq);
		
		ByteBuffer bodybuffer = ByteBuffer.allocate(messagelength - 20);
		for (int i = 20 - 4; i < messagelength - 4; i++) {
			//System.out.println(b[i]);
			bodybuffer.put(b[i]);
		}
		bodybuffer.flip();			
		
		Deliver deliver = new Deliver();
		deliver.setBuffer(bodybuffer);
		
		//System.out.println(deliver.SPNumber);
		//System.out.println(deliver.Mobile_no);
		//System.out.println(deliver.TP_pid);
		//System.out.println(deliver.TP_udhi);
		System.out.println("msgCoding:"+deliver.MessageCoding);
		System.out.println("MessageLength:"+deliver.MessageLength);
		System.out.println("msg+"+deliver.msg);
		System.out.println("Reserve:"+deliver.Reserve.trim());
		System.out.println("");
		String msg="0";
		//if(deliver.MessageCoding==8){
		//	msg=new String(MessageByte,"UnicodeBigUnmarked");
		//}
		db.opSaveDeliver(deliver.SPNumber, deliver.Mobile_no, deliver.TP_pid, deliver.TP_udhi,deliver.MessageCoding, deliver.MessageLength, deliver.msg, deliver.Reserve,Seq_No,Createtime);
		
		/*deliver回应部分*/
		ByteBuffer resp2=ByteBuffer.allocate(29);
		resp2.putInt(29);
		resp2.putInt(SGIP.SGIP_DELIVER_RESP);		
		resp2.putInt(jd);		
		resp2.putInt(ctime);
		resp2.putInt(sq);
		resp2.put((byte)0);
		for(int i=0;i<8;i++){
			resp2.put((byte)0);					
		}
		resp2.flip();		
		dos.write(resp2.array());
		dos.flush();
		System.out.println("deliver回应完毕=================================================================");
		
	}

	private void test(ByteBuffer buffer){
		try{
			System.out.println("*****************");
			System.out.println("len="+buffer.getInt());
			System.out.println("cmd="+buffer.getInt());
			System.out.println("Jiedian="+buffer.getInt());
			System.out.println("Createtime="+buffer.getInt());
			System.out.println("Seq_no="+buffer.getInt());
			System.out.println("result="+buffer.get());
			byte temp[] = new byte[8];
			buffer.get(temp, 0, 8);
			
			String tmp = new String(temp,"GB2312");
			System.out.println("Reserve="+tmp.trim());
			System.out.println("*****************");
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 回应绑定请求
	 * @param ByteBuffer       字节数组
	 */
	private void ServerResBind(byte b[],int messagelength) throws Exception{
		
		ByteBuffer bodybuffer = ByteBuffer.allocate(messagelength - 20);
		for (int i = 20 - 4; i < messagelength - 4; i++) {
			bodybuffer.put(b[i]);
		}
		bodybuffer.flip();
		
		Bind bind = new Bind();
		bind.setBuffer(bodybuffer);
		
		System.out.println(bind.Login_Name);
		System.out.println(bind.Login_Passowrd);
		System.out.println(bind.Login_Type);
		
		Resp resp = new Resp();
		resp.head.Cmd_id = SGIP.SGIP_BIND_RESP;
		resp.Result = 0;
		System.out.println("登陆回应ok1");
		ByteBuffer sendBuffer = resp.getAllBuffer();
		System.out.println("登陆回应ok2");
		dos.write(sendBuffer.array());
		dos.flush();	
		System.out.println("登陆回应ok");
	}

}
