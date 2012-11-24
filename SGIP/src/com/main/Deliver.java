package com.main;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

//import com.zzxy.common.Logger;


  public class Deliver  extends SGIPAbstractStruct{
	
	public String Mobile_no;   //21B ���Ͷ���Ϣ���û��ֻ��ţ��ֻ�����ǰ��"86"�����־
	public String SPNumber;  //21B SP�Ľ������
	
	public byte TP_pid;   //1B GSMЭ�����͡���ϸ������ο�GSM03.40�е�9.2.3.9
	public byte TP_udhi;   //1B GSMЭ�����͡���ϸ������ο�GSM03.40�е�9.2.3.23,��ʹ��1λ���Ҷ���
	public byte MessageCoding;   //1B ����Ϣ�ı����ʽ��0����ASCII�ַ���3��д������4�������Ʊ���8��UCS2����15: GBK���������μ�GSM3.38��4�ڣ�SMS Data Coding Scheme
	public int MessageLength;//4B Integer ����Ϣ�ĳ���
	public String msg;//Message Length  ����Ϣ������
	public String Reserve;//8B ��������չ��
	
	public static final int STRUCT_SIZE=49;//��������Ϣ���ݵĳ���
	
	@Override
	public ByteBuffer getBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(STRUCT_SIZE+MessageLength);
		buffer.order(byteOrder);
		
		buffer.put(Mobile_no.getBytes());
		for (int i = Mobile_no.getBytes().length; i < 21; i++) {
			buffer.put((byte)0);
		}
		
		buffer.put(SPNumber.getBytes());
		for (int i = SPNumber.getBytes().length; i < 21; i++) {
			buffer.put((byte)0);
		}
		
		buffer.put(TP_pid);
		buffer.put(TP_udhi);
		buffer.put(MessageCoding);
		buffer.putInt(MessageLength);
		buffer.put(msg.getBytes());
		
		for (int i = 0; i < 8; i++) {
			buffer.put((byte)0);
		}
		buffer.flip();
		return buffer;
	}

	@Override
	public void setBuffer(ByteBuffer buffer) throws Exception {
		buffer.order(byteOrder);
		
		byte temp[] = new byte[21];
		buffer.get(temp, 0, 21);
		Mobile_no = new String(temp, "GB2312");
		Mobile_no = Mobile_no.trim();
		buffer.get(temp, 0, 21);
		SPNumber = new String(temp, "GB2312");
		SPNumber = SPNumber.trim();
		
		TP_pid = buffer.get();
		TP_udhi = buffer.get();
		MessageCoding = buffer.get();
		MessageLength=buffer.getInt();
		
		byte ls[] = new byte[MessageLength];
		buffer.get(ls);
		if (MessageCoding==8){
			msg=new String(ls,"UnicodeBigUnmarked");
			System.out.println("��Ϣ������ucs2===::");
		}
		else{
		msg = new String(ls);
		System.out.println("������Ϣ������GB2312====::");
		}
	
		byte tempReserve[] = new byte[8];
		buffer.get(tempReserve,0,8);
		
		Reserve = new String(tempReserve,"GB2312");
	}
	
	  private String UCS2String(byte[] msg) {
/*
		    int zhong, zhong1;
		    String snr = "";
		    for (int j = 0; j < this.MessageLength; j += 2) {
		      zhong = msg[j];
		      if (zhong < 0)
		        zhong += 256;
		      zhong1 = msg[j + 1];
		      if (zhong1 < 0)
		        zhong1 += 256;

		      zhong = zhong * 256 + zhong1;
		      if (zhong == 0)
		        break;

		      snr = snr + (char) zhong;
		    }
		    */
		  String snr = "";
		  try {
			  snr=new String(msg,"UnicodeBigUnmarked");
		} catch (UnsupportedEncodingException e) {
			// TODO: handle exception
		}
		  

		    return snr;
		  }
	
	public void setBuffermsg(ByteBuffer buffer) throws Exception {
		buffer.order(byteOrder);
		
		byte temp[] = new byte[this.MessageLength];
		buffer.get(temp, 0, this.MessageLength);
		
	    // �жϽ�����Ϣ��ʽ
	    switch ( MessageCoding ) {
	      case 0 :// ASCII��
	    	msg = new String(temp).trim();
	        break;
	      case 3 :// ����д������
	        break;
	      case 4 :// ��������Ϣ
	        break;
	      case 8 :// UCS2����
	    	//msg = UCS2String(temp);
	    	msg = new String(temp,"UnicodeBigUnmarked");
	    	
	        break;
	      case 15 :// ��GB����
	        break;
	      default :
	        break;
	    }
		
	}
	
	public void read(SocketChannel channel) throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate(STRUCT_SIZE);
		buffer.order(byteOrder);
		channel.read(buffer);
		buffer.flip();
		this.setBuffer(buffer);
		buffer = ByteBuffer.allocate(this.MessageLength);
		buffer.order(ByteOrder.nativeOrder());
		channel.read(buffer);
		buffer.flip();
		msg = new String(buffer.array(), charset);
	}
  }

