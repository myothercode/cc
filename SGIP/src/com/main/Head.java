package com.main;

import java.nio.ByteBuffer;
import java.util.Calendar;



public class Head extends SGIPAbstractStruct{
	public int Msglen;      //4B Integer 消息的总长度(字节)
	public int Cmd_id;      //4B Integer 命令ID
	
	//消息头里的节点编号，固定值比如3037142185(十进制)=B5071CA9（十六进制)
	public final int JieDian=0xB47CB398;
	public int CreateTime;  ////4B  Integer 创建时间格式为十进制的mmddhhmmss
	public int Seq_Number;  //4B  Integer 序列号,由0开始，循环进位，直到进位满了之后再清零，重新开始计数
	
	public static int SeqNo=0;                        //消息序号
	
	public static final int STRUCT_SIZE=20;
	
	public Head() {
		SetTime();
		Seq_Number=GetSeqNo();
	}
	
	public Head(int SeqNo) {
		SetTime();
		Seq_Number=SeqNo;
	}
	
	public Head(int crtTime,int SeqNo){
		CreateTime = crtTime;
		Seq_Number = SeqNo;
	}
	
	private static int GetSeqNo() {
		  
		switch ( SeqNo ) {
			case 0x7FFFFFFF :
				SeqNo=0x80000000;
			    break;
			case 0xFFFFFFFF :
				SeqNo=0;
				break;
			default :
				SeqNo++;
			    break;
		}
		return SeqNo;
	}
	
	private void SetTime() {
		Calendar rightNow = Calendar.getInstance();
		CreateTime=rightNow.get(Calendar.MONTH);
		CreateTime=CreateTime*100+rightNow.get(Calendar.DAY_OF_MONTH);
		CreateTime=CreateTime*100+rightNow.get(Calendar.HOUR_OF_DAY);
		CreateTime=CreateTime*100+rightNow.get(Calendar.MINUTE);
		CreateTime=CreateTime*100+rightNow.get(Calendar.SECOND);
	}
	
	@Override
	public ByteBuffer getBuffer() {
	  ByteBuffer buffer = ByteBuffer.allocate(STRUCT_SIZE);
	  buffer.order(byteOrder);
	  buffer.putInt(Msglen);
	  buffer.putInt(Cmd_id);
	  buffer.putInt(JieDian);
	  buffer.putInt(CreateTime);
	  buffer.putInt(Seq_Number);
	  buffer.flip();
	  return buffer;
    }

	@Override
	public void setBuffer(ByteBuffer buffer) {
	  buffer.order(byteOrder);
      Msglen = buffer.getInt();
      Cmd_id = buffer.getInt();
      int jdianTemp = buffer.getInt();//JieDian是最终变量
      CreateTime = buffer.getInt();
      Seq_Number = buffer.getInt();
	}

  }
