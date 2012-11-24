package com.main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.main.dbcommpool.c3p0Pool;


public class SendServices {
	private c3p0Pool cpPool = c3p0Pool.getInstance();
	private Connection conn=null;
	
	public String[] getSerInfo(){
		Statement stmt=null;
        ResultSet rs=null;
        String serviceNumber="";  //业务号码
        String userNumber="";     //用户号码
        String messageContent=""; //消息内容
        String reserve="";        //linkid
        String idnum="";
        String[] info=new String[5];
        
        conn=cpPool.getConnection();
        try {
        	stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery(
            		"select top 1 idnum, serviceid,mobile_no,msg,reserve from send where flag='0'"
            		);
            rs.first();
            serviceNumber=rs.getString("serviceid");
            userNumber=rs.getString("mobile_no");
            messageContent=rs.getString("msg");
            reserve=rs.getString("reserve");
            idnum=String.valueOf(rs.getInt("idnum"));
            info[0]=serviceNumber;
            info[1]=userNumber;
            info[2]=messageContent;
            info[3]=reserve;
            info[4]=idnum;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			
			try {
				if(rs!=null){rs.close();}
				stmt.close();
				conn.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return info;
        
	}
	
	/**
	 * 发送成功后修改状态
	 * @param serviceNumber
	 * @param userNumber
	 * @param reserve
	 * @return
	 */
	public int updateFlag(String idnum){
		if(idnum==null||"".equals(idnum)){
			System.out.println("idnum is null");
			return 9000;
		}
		
		int c=0;
		Statement stmt=null;
		conn=cpPool.getConnection();
		try {
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			 c = stmt.executeUpdate("update send set flag='1' where flag='0' and idnum='"+idnum+"'" );
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			try {
				stmt.close();
				conn.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return c;
	}
	/**
	 * 插入日志记录
	 * @param mobile_no
	 * @param time
	 * @param spcode
	 * @param message
	 * @return
	 */
	public int insertLog(String mobile_no,String time,String spcode, String message,String reserve){
				
		int c=0;
		Statement stmt=null;
		conn=cpPool.getConnection();
		try {
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			 c = stmt.executeUpdate("insert into sms_log_new(mobile_no,time,spcode,message,reserve) values('"+mobile_no+"','"+time+"','"+spcode+"','"+message+"','"+reserve+"')" );
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			try {
				stmt.close();
				conn.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return c;
	}

	public int keepCheck(){
		int c=0;
		Statement stmt=null;
		ResultSet rs=null;
		conn=cpPool.getConnection();
		try {
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("select count(1) as co from send where flag='0'");
            rs.first();
            c=rs.getInt("co");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			try {
				if(rs!=null){rs.close();}
				stmt.close();
				conn.close();
				//System.out.println("目前需要发送的记录有");
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return c;
		
	}
	
	
}
