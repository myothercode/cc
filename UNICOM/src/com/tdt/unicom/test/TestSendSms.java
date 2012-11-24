package com.tdt.unicom.test;

import java.io.OutputStream;
import java.net.Socket;

import org.apache.xerces.impl.dv.util.Base64;


public class TestSendSms {
	public static void main(String[] args) throws Exception {
			String xmlbody = "{<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<gwsmip>\n" + "  <message_header>\n"
					+ "    <command_id>0x3</command_id>\n"
					+ "    <sequence_number/>\n" + "  </message_header>\n"
					+ "  <message_body>\n" + "    <pk_total>1</pk_total>\n"
					+ "    <pk_number>1</pk_number>\n" + "    <user_numbers>\n"
					+ "       <user_number>13075364025</user_number>\n"
					+ "    </user_numbers>\n"
					+ "    <sp_number>1062836516</sp_number>\n"
					+ "    <service_type>DXAC</service_type>\n"
					+ "    <message_content>" + Base64.encode("你好，这是一个网络测试！".getBytes())
					+ "</message_content>\n"
					+ "    <report_flag>1</report_flag>\n"
					+ "   </message_body>\n" + "</gwsmip>\n}";

			Socket socket = new Socket("127.0.0.1", 8805);
			OutputStream out = socket.getOutputStream();
				System.out.println(xmlbody);
				out.write(xmlbody.getBytes());
				out.write(xmlbody.getBytes());
	}
}
