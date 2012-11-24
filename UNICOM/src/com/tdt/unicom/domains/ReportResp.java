package com.tdt.unicom.domains;

/**
 * @project UNICOM
 * @author sunnylocus
 * @vresion 1.0 2009-8-21
 * @description  report√¸¡ÓµƒœÏ”¶°°
 */
public class ReportResp extends SGIPCommand {
	private byte result;
	
	public ReportResp(byte[] unicomSN) {
		this.header = new SGIPHeader(SGIPCommandDefine.SGIP_REPORT_RESP);
		this.header.setUnicomSN(unicomSN);
		this.bodybytes = new byte[SGIPCommandDefine.LEN_SGIP_REPORT_RESP];
	}

	public void setResult(byte result) {
		this.result=result;
		this.bodybytes[0] = result;
	}

	public byte getResult() {
		return result;
	}
}
