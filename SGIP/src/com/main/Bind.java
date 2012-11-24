package com.main;

import java.nio.ByteBuffer;




public class Bind  extends SGIPAbstractStruct{
	/*1B 登录类型。
	  1：SP向SMG建立的连接，用于发送命令
	  2：SMG向SP建立的连接，用于发送命令
	  3：SMG之间建立的连接，用于转发命令
	  4：SMG向GNS建立的连接，用于路由表的检索和维护
	  5：GNS向SMG建立的连接，用于路由表的更新
	  6：主备GNS之间建立的连接，用于主备路由表的一致性
	  11：SP与SMG以及SMG之间建立的测试连接，用于跟踪测试其它：保留*/
	public byte Login_Type=1;
	public String Login_Name;   //16B 服务器端给客户端分配的登录名
	public String Login_Passowrd;  //16B 服务器端和Login Name对应的密码
	public String Reserve;//8B 保留，扩展用
	
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

