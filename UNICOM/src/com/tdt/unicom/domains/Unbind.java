package com.tdt.unicom.domains;


/**
 * @project UNICOM
 * @author sunnylocus
 * @vresion 1.0 2009-8-21
 * @description 用于通知server端，拆掉已建立的链接
 */
public class Unbind extends SGIPCommand{
	public Unbind() {
		this.header = new SGIPHeader(SGIPCommandDefine.SGIP_UNBIND);
	}
	
	public Unbind(SGIPCommand command) {
		this.header = command.header;
		this.bodybytes = command.bodybytes;
	}
}
