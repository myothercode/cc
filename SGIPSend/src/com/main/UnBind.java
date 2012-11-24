package com.main;

import java.nio.ByteBuffer;

public class UnBind {
	public Head head=null;
	public UnBind(){
		Head head1=new Head();
		head1.cmdId=SGIPCmd.SGIP_UNBIND;
		head1.msgLen = 20;
		this.head=head1;
	}
	public ByteBuffer getUnBindBuffer(){
		return this.head.getBuffer();
	}

}
