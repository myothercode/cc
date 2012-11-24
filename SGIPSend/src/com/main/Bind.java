package com.main;
import static com.main.BusinessInfo.*;

import java.nio.ByteBuffer;

public class Bind {
	/*1B 登录类型。
	  1：SP向SMG建立的连接，用于发送命令
	  2：SMG向SP建立的连接，用于发送命令
	  3：SMG之间建立的连接，用于转发命令
	  4：SMG向GNS建立的连接，用于路由表的检索和维护
	  5：GNS向SMG建立的连接，用于路由表的更新
	  6：主备GNS之间建立的连接，用于主备路由表的一致性
	  11：SP与SMG以及SMG之间建立的测试连接，用于跟踪测试其它：保留*/
	public byte loginType=1;
	public String bLoginName=loginName;
	public String bLoginPassWord=loginPassWord;
	public String Reserve;
	public Head head= null;
	
	public static int STRUCT_SIZE=41;
	
	public Bind(){
		Head head1=new Head();
		head1.cmdId=SGIPCmd.SGIP_BIND;
		head1.msgLen=head1.STRUCT_SIZE+this.STRUCT_SIZE;
		this.head=head1;
	}
	
	public ByteBuffer getBuffer(){
		ByteBuffer buffer=ByteBuffer.allocate(STRUCT_SIZE);
		buffer.order(byteOrder);
		buffer.put(loginType);
		buffer.put(bLoginName.getBytes());
		for(int i=bLoginName.getBytes().length;i<16;i++){
			buffer.put((byte)0);
		}
		buffer.put(bLoginPassWord.getBytes());
		for(int i=bLoginPassWord.getBytes().length;i<16;i++){
			buffer.put((byte)0);
		}
		for(int i=0;i<8;i++){
			buffer.put((byte)0);
		}
		buffer.flip();
		return buffer;
	}
	
	public ByteBuffer getAllBindBuffer(){
		//ByteBuffer buffer = ByteBuffer.allocate(this.head.getBuffer().capacity()+this.getBuffer().capacity());
		ByteBuffer buffer = ByteBuffer.allocate(this.head.msgLen);
		buffer.order(byteOrder);
		buffer.put(this.head.getBuffer());
		buffer.put(this.getBuffer());
		buffer.flip();
		return buffer;
	}

}
