package com.main;

import java.nio.*;
public class Report extends SGIPAbstractStruct{
	
	public int JieDian=0xB5071CA9;
	public int CreateTime;  ////4B  Integer ����ʱ���ʽΪʮ���Ƶ�mmddhhmmss
	public int Seq_Number;  //4B  Integer ���к�,��0��ʼ��ѭ����λ��ֱ����λ����֮�������㣬���¿�ʼ����
	
	public byte ReportType;    //1B��������
	public String UserNumber; //21B �û�����
	public byte State;         //1B ״̬
	public byte ErrorCode;     //1B�������
	public String Reserve;    //8B����
	public Report(){
		
	}
	
	@Override
	public ByteBuffer getBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(20);
		
		return buffer;
	}

	@Override
	public void setBuffer(ByteBuffer buffer) throws Exception {
		buffer.order(byteOrder);
		JieDian     = buffer.getInt();
		CreateTime  = buffer.getInt();
		Seq_Number  = buffer.getInt();
		ReportType  = buffer.get();
		
		byte temp[] = new byte[21];
	    buffer.get(temp, 0, 21);
		//buffer.get(temp, 0, 20);
		UserNumber = new String(temp, "GB2312");
		UserNumber = UserNumber.trim();
		
		State      = buffer.get();
		ErrorCode  = buffer.get();
		
		byte temp1[] = new byte[8];
		buffer.get(temp1, 0, 8);
		//buffer.get(temp1, 0, 7);		

		Reserve = new String(temp1, "GB2312");
		Reserve = Reserve.trim();
	}
}
