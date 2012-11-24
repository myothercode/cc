package com.main;

import java.nio.ByteBuffer;




public class Bind  extends SGIPAbstractStruct{
	/*1B ��¼���͡�
	  1��SP��SMG���������ӣ����ڷ�������
	  2��SMG��SP���������ӣ����ڷ�������
	  3��SMG֮�佨�������ӣ�����ת������
	  4��SMG��GNS���������ӣ�����·�ɱ�ļ�����ά��
	  5��GNS��SMG���������ӣ�����·�ɱ�ĸ���
	  6������GNS֮�佨�������ӣ���������·�ɱ��һ����
	  11��SP��SMG�Լ�SMG֮�佨���Ĳ������ӣ����ڸ��ٲ�������������*/
	public byte Login_Type=1;
	public String Login_Name;   //16B �������˸��ͻ��˷���ĵ�¼��
	public String Login_Passowrd;  //16B �������˺�Login Name��Ӧ������
	public String Reserve;//8B ��������չ��
	
	public static final int STRUCT_SIZE=41;
	
	public Bind() {
		Head head = new Head();
		head.Msglen=Head.STRUCT_SIZE+this.STRUCT_SIZE;
		head.Cmd_id=SGIP.SGIP_BIND;
		this.setHead(head);
	}
	
	@Override
	public ByteBuffer getBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(STRUCT_SIZE);
		buffer.order(byteOrder);
		buffer.put(Login_Type);
		
		buffer.put(Login_Name.getBytes());
		for (int i = Login_Name.getBytes().length; i < 16; i++) {
			buffer.put((byte)0);
		}
		
		buffer.put(Login_Passowrd.getBytes());
		for (int i = Login_Passowrd.getBytes().length; i < 16; i++) {
			buffer.put((byte)0);
		}
		
		for (int i = 0; i < 8; i++) {
			buffer.put((byte)0);
		}
		buffer.flip();
		return buffer;
	}

	@Override
	public void setBuffer(ByteBuffer buffer) throws Exception {
		buffer.order(byteOrder);
		Login_Type = buffer.get();
		byte temp[] = new byte[16];
		buffer.get(temp, 0, 16);
		Login_Name = new String(temp, "GB2312");
		Login_Name = Login_Name.trim();
		buffer.get(temp, 0, 16);
		Login_Passowrd = new String(temp, "GB2312");
		Login_Passowrd = Login_Passowrd.trim();
	}
  }

