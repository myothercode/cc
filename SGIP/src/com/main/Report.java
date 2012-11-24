package com.main;

import java.nio.*;
public class Report extends SGIPAbstractStruct{
	
	public int JieDian=0xB5071CA9;
	public int CreateTime;  ////4B  Integer 创建时间格式为十进制的mmddhhmmss
	public int Seq_Number;  //4B  Integer 序列号,由0开始，循环进位，直到进位满了之后再清零，重新开始计数
	
	public byte ReportType;    //1B报告类型
	public String UserNumber; //21B 用户号码
	public byte State;         //1B 状态
	public byte ErrorCode;     //1B错误代码
	public String Reserve;    //8B保留
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
