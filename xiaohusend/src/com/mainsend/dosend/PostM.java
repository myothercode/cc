package com.mainsend.dosend;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import com.mainsend.SendMPOJO;

public class PostM {
	
	public String PostSend(SendMPOJO sendMPOJO)throws Exception{
		String url1="http://www.cdxintu.com/objmsg/XT_dt00006.aspx?";
		StringBuffer sb=new StringBuffer();
		sb.append(url1);
		sb.append("linkid=").append(sendMPOJO.getLinkId()+"x"+sendMPOJO.getMobile());
		sb.append("&&mobile=").append(sendMPOJO.getMobile());
		sb.append("&&msg=").append(sendMPOJO.getMsg());
		sb.append("&&spport=").append(sendMPOJO.getSpcode());
	    sb.append("&&stat=").append(sendMPOJO.getStat());
	    sb.append("&&province=sc&&city=chengdu");
	    
	    URL url=new URL(sb.toString());
	    URLConnection conn=url.openConnection();
	    conn.setDoOutput(true);
	    
	    OutputStreamWriter opw=new OutputStreamWriter(conn.getOutputStream(),"utf-8");
	    InputStreamReader read=new InputStreamReader(conn.getInputStream(),"utf-8");
	    BufferedReader reader=new BufferedReader(read);
	    String result = reader.readLine();//得到的执行后返回的结果
	    System.out.println(result);
	    return result;
	}

}
