package com.tdt.unicom.spsvr;

import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.impl.dv.util.Base64;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @project UNICOM
 * @author sunnylocus
 * @vresion 1.0 2009-8-15
 * @description ������������  
 */
public class MTReq{
	private  String spNumber;             //��Ʒ�����̺�
	private  String serviceType;          //��Ʒ�������
	private  ArrayList<String> phoneList; //�����ֻ���
	private  String messageContent;       //����Ϣ����
	private  String reportFlag;           //�Ƿ���Ҫ������ŷ��͵�״̬
	private  String linkId;               //linkid,���û��·�����ʱ��ǩȨ
	
	public MTReq() {
	}
	public MTReq(Document doc) {
		Element root = doc.getRootElement();
		Element message_body =root.getChild("message_body");
		//---------------------------------�ֶθ�ֵ��
		setSpNumber(message_body.getChildTextTrim("sp_number"));
		setServiceType(message_body.getChildTextTrim("service_type"));
		setReportFlag(message_body.getChildTextTrim("report_flag"));
		String messageContent = message_body.getChildTextTrim("message_content");
		setMessageContent(new String(Base64.decode(messageContent)));
		//----------------------------------����ֻ�����
		List<Element> list= message_body.getChild("user_numbers").getChildren();
		ArrayList<String> phoneList = new ArrayList<String>();
		for(Element element : list ) {
			phoneList.add("86"+element.getTextTrim());
		}
		setPhoneList(phoneList);
		setLinkId("00000000");
		if(message_body.getChildTextTrim("link_id")!=null) {
			setLinkId(message_body.getChildTextTrim("link_id"));
		}
	}
	
	public String getLinkId() {
		return linkId;
	}
	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	
	public String getSpNumber() {
		return spNumber;
	}

	public void setSpNumber(String spNumber) {
		this.spNumber = spNumber;
	}

	public ArrayList<String> getPhoneList() {
		return phoneList;
	}

	public void setPhoneList(ArrayList<String> phoneList) {
		this.phoneList = phoneList;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	public String getReportFlag() {
		return reportFlag;
	}

	public void setReportFlag(String reportFlag) {
		if(reportFlag.equals("0")) //����Ҫ״̬����
			this.reportFlag ="2";
		this.reportFlag = reportFlag;
	}
	
	/**
	 * * �޸ģ�2011-11-04
	 *        ��StringBuilder��append���������ַ������
	 */
	public String toXmlstr() {
		String phonexml="";
		for(int i=0; i < this.getPhoneList().size(); i++) {
			 phonexml += "       <user_number>"+this.getPhoneList().get(i)+"</user_number>\n";
		}
		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("{<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		xmlBuilder.append("<gwsmip>\n");
		xmlBuilder.append("  <message_header>\n");
		xmlBuilder.append( "    <command_id>0x3</command_id>\n");
		xmlBuilder.append( "    <sequence_number/>\n"); 
		xmlBuilder.append("  </message_header>\n");
		xmlBuilder.append("  <message_body>\n"); 
		xmlBuilder.append("    <pk_total>1</pk_total>\n");
		xmlBuilder.append("    <pk_number>1</pk_number>\n");
		xmlBuilder.append("    <user_numbers>\n");
		xmlBuilder.append(         phonexml);
		xmlBuilder.append("    </user_numbers>\n");
		xmlBuilder.append("    <sp_number>"+this.getSpNumber()+"</sp_number>\n");
		xmlBuilder.append("    <service_type>"+this.getServiceType()+"</service_type>\n");
		xmlBuilder.append(     (this.getLinkId()!=null) ? "    <link_id>"+this.getLinkId()+"</link_id>\n" : "");
		xmlBuilder.append("    <message_content>" +Base64.encode(this.getMessageContent().getBytes()));
		xmlBuilder.append("</message_content>\n");
		xmlBuilder.append("    <report_flag>"+this.getReportFlag()+"</report_flag>\n");
		xmlBuilder.append("   </message_body>\n"); 
		xmlBuilder.append("</gwsmip>\n}");
		return xmlBuilder.toString();
	}
}
