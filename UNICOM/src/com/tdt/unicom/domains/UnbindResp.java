package com.tdt.unicom.domains;

/**
 * @project UNICOM
 * @author sunnylocus
 * @vresion 1.0 2009-8-21
 * @description¡¡unindÃüÁîµÄÏìÓ¦ Àà
 */
public class UnbindResp extends SGIPCommand{
	public UnbindResp(byte[] unicomSN) {
		this.header = new SGIPHeader(SGIPCommandDefine.SGIP_UNBIND_RESP);
		this.header.setUnicomSN(unicomSN);
	}
	public UnbindResp(SGIPCommand command) {
		this.header = command.header;
	}
}
