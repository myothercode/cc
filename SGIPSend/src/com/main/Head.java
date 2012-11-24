package com.main;

import java.nio.ByteBuffer;
import java.util.Calendar;
import static com.main.BusinessInfo.*;

public class Head {
	public int msgLen;  //4B 消息的总长度（字节）
	public int cmdId;   //4B 命令ID表示是 类SGIPCmd中定义的动作
		
	//消息头的seqnum部分，分为3部分
	public static int JD = jieDian;  //4B 分配的节点号
	public int createTime;           //4B
	public int Seq_Number;              // 4B序列号
	
	//public int seqNum = 0;        //为3部分组成
	public static int STRUCT_SIZE = 20;  //消息头最大为20B长度
	
	//构造一个head
	public Head(){
		setTime();
		setMsecond();
	}
	
	
	public ByteBuffer getBuffer(){
		ByteBuffer buffer = ByteBuffer.allocate(STRUCT_SIZE);
		buffer.order(byteOrder);
		buffer.putInt(msgLen);
		buffer.putInt(cmdId);
		buffer.putInt(JD);
		buffer.putInt(createTime);
		buffer.putInt(Seq_Number);
		buffer.flip();
		return buffer;
	}
	
	/**
	 * 生成日期时分秒字符串
	 */
	private void setTime(){
		Calendar now = Calendar.getInstance();
		createTime = now.get(Calendar.MONTH);
		createTime = createTime*100+now.get(Calendar.DAY_OF_MONTH);
		createTime = createTime*100+now.get(Calendar.HOUR_OF_DAY);
		createTime = createTime*100+now.get(Calendar.MINUTE);
		createTime = createTime*100+now.get(Calendar.SECOND);		
	}
	/**
	 * 得到毫秒
	 */
	private void setMsecond(){
		Calendar now = Calendar.getInstance();
		Seq_Number = now.get(Calendar.MILLISECOND);
	}

}
