package com.main;

import java.nio.ByteBuffer;
import java.util.Calendar;



public class Head extends SGIPAbstractStruct{
	public int Msglen;      //4B Integer ��Ϣ���ܳ���(�ֽ�)
	public int Cmd_id;      //4B Integer ����ID
	
	//��Ϣͷ��Ľڵ��ţ��̶�ֵ����3037142185(ʮ����)=B5071CA9��ʮ������)
	public final int JieDian=0xB47CB398;
	public int CreateTime;  ////4B  Integer ����ʱ���ʽΪʮ���Ƶ�mmddhhmmss
	public int Seq_Number;  //4B  Integer ���к�,��0��ʼ��ѭ����λ��ֱ����λ����֮�������㣬���¿�ʼ����
	
	public static int SeqNo=0;                        //��Ϣ���
	
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
      int jdianTemp = buffer.getInt();//JieDian�����ձ���
      CreateTime = buffer.getInt();
      Seq_Number = buffer.getInt();
	}

  }
