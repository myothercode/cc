package com.tdt.unicom.spsvr;

import java.io.IOException;
import java.io.InputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @project UNICOM
 * @author sunnylocus
 * @vresion 1.0 2009-8-15
 * @description  MO上行xml格式封装类，用于将SMG向SP递交的上行转发至客户端
 */
public class MOResp {

	private String userNumber = "";     //手机号码
	private int userNumberType = 0;     //号码类型(无用字段，保留)
	private String spNumber = "";       //产品服务商号
	private int messageCoding = 15;     //编码方式 (0:ASCII　3:短信写卡操作 4:二进制信息 8:ucs2编码 15:gbk编码)
	private String messageContent = ""; //短消息内容
	private String serviceType = "";	//业务代码
	private int reportFlag = 0;			//是否需要状态报告(0 :不需要 1:需要状态报告)
	private String linkId = "";         //点播业务使用LinkID，用于对下行短信的鉴权。非点播业务为"00000000";

	public MOResp() {
	}

	public void loadFromXmlDoc(Document doc) {
		/*
		 * <?xml version="1.0" encoding="UTF-8"?> <gwsmip> <message_header>
		 * <command_id>0x4</command_id> <sequence_number>27</sequence_number>
		 * </message_header> <message_body>
		 * <user_number>13912345678</user_number> 
		 * <user_number_type>0</user_number_type>
		 * <sp_number>042251</sp_number> 
		 * <message_coding>15</message_coding>
		 * <message_content>111</message_content>
		 * <service_type>tdtlbs</service_type> 
		 * <report_flag>0</report_flag>
		 * <link_id>0123050608105031</link_id> </message_body> </gwsmip>
		 */

		// 获得根元素
		Element root = doc.getRootElement();
		Element body = root.getChild("message_body");
		//---------------------------------字段赋值　
		setUserNumber(body.getChildText("user_number"));
		setUserNumberType(Integer.valueOf(body.getChildText("user_number_type")).intValue());
		setSpNumber(body.getChildText("sp_number"));
		setMessageCoding(Integer.valueOf(body.getChildText("message_coding")).intValue());
		setMessageContent(body.getChildText("message_content"));
		setServiceType(body.getChildText("service_type"));
		setReportFlag(Integer.valueOf(body.getChildText("report_flag")).intValue());
		setLinkId(body.getChildText("link_id"));
	}

	public void loadFromInputStream(InputStream in) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc=null;
		doc = builder.build(in);
		loadFromXmlDoc(doc);
	}

	/**
	 * 通过从xml格式字符串装入的方式，给该bean类赋值。
	 * 
	 * @param xmlStr
	 * @throws JDOMException
	 * @throws IOException 
	 */
	public void loadFromXmlStr(String xmlStr) throws JDOMException, IOException {

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xmlStr);
		loadFromXmlDoc(doc);

	}

	/**
	 * 封装上行xml格式
	 * @return String 按协议封装后的MO上行格式
	 */
	public String toXmlStr() {
		StringBuffer xmlstr = new StringBuffer();
		xmlstr.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
			  .append( "<gwsmip>\n" + "  <message_header>\n")
			  .append("    <command_id>0x4</command_id>\n")
			  .append("    <sequence_number>27</sequence_number>\n")
			  .append("  </message_header>\n" + "  <message_body>\n")
			  .append("    <user_number>")
			  .append(getUserNumber())
			  .append("</user_number>\n")
			  .append("    <user_number_type>")
			  .append(getUserNumberType())
			  .append("</user_number_type>\n")
			  .append("    <sp_number>")
			  .append(getSpNumber())
			  .append("</sp_number>\n")
			  .append("    <message_coding>")
			  .append(getMessageCoding())
			  .append("</message_coding>\n")
			  .append("    <message_content>")
			  .append(getMessageContent())
			  .append("</message_content>\n")
			  .append("    <service_type>")
			  .append(getServiceType())
			  .append("</service_type>\n")
			  .append("    <report_flag>")
			  .append(getReportFlag())
			  .append("</report_flag>\n")
			  .append("    <link_id>")
			  .append(getLinkId())
			  .append("</link_id>\n")
			  .append("</message_body>\n")
			  .append("</gwsmip>\n");
		return xmlstr.toString();
	}

	/**
	 * @return linkId
	 */
	public String getLinkId() {
		return linkId;
	}

	/**
	 * @param linkId
	 *            要设置的 linkId
	 */
	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	/**
	 * @return messageCoding
	 */
	public int getMessageCoding() {
		return messageCoding;
	}

	/**
	 * @param messageCoding
	 *            要设置的 messageCoding
	 */
	public void setMessageCoding(int messageCoding) {
		this.messageCoding = messageCoding;
	}

	/**
	 * @return messageContent
	 */
	public String getMessageContent() {
		return messageContent;
	}

	/**
	 * @param messageContent
	 *            要设置的 messageContent
	 */
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	/**
	 * @return reportFlag
	 */
	public int getReportFlag() {
		return reportFlag;
	}

	/**
	 * @param reportFlag
	 *            要设置的 reportFlag
	 */
	public void setReportFlag(int reportFlag) {
		this.reportFlag = reportFlag;
	}

	/**
	 * @return serviceType
	 */
	public String getServiceType() {
		return serviceType;
	}

	/**
	 * @param serviceType
	 *            要设置的 serviceType
	 */
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	/**
	 * @return spNumber
	 */
	public String getSpNumber() {
		return spNumber;
	}

	/**
	 * @param spNumber
	 *            要设置的 spNumber
	 */
	public void setSpNumber(String spNumber) {
		this.spNumber = spNumber;
	}

	/**
	 * @return userNumber
	 */
	public String getUserNumber() {
		return userNumber;
	}

	/**
	 * @param userNumber
	 *            要设置的 userNumber
	 */
	public void setUserNumber(String userNumber) {
		this.userNumber = userNumber;
	}

	/**
	 * @return userNumberType
	 */
	public int getUserNumberType() {
		return userNumberType;
	}

	/**
	 * @param userNumberType
	 *            要设置的 userNumberType
	 */
	public void setUserNumberType(int userNumberType) {
		this.userNumberType = userNumberType;
	}

}
