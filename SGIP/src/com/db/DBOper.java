package com.db;

import java.io.*;
import java.util.*;
import java.sql.*;



/**
 * 数据库操作
 * @author Administrator
 *
 */
public class DBOper {
	private ConnectionPool cp = ConnectionPool.getInstance();
	private static DBOper db; 
	
	public DBOper(){
		
	}
	
    public  static synchronized DBOper getInstance(){
        if (db==null) {
        	db = new DBOper();              
        }
        return db;         
    }
    
    public void opSaveDeliver(String SPNumber,String Mobile_no,int TP_pid,int TP_udhi,int MessageCoding
    		,int MessageLength,String msg,String Reserve,int seq_num,int Createtime){
 
    	Connection conn = cp.getConnection();   //从连接池里面获得1个连接
        try {            
            CallableStatement cstmt = conn.prepareCall("{call dbo.proc_insDeliver(?,?,?,?,?,?,?,?,?,?)}");
            cstmt.setString(1, SPNumber);
            cstmt.setString(2, Mobile_no);
            cstmt.setInt(3,TP_pid);
            cstmt.setInt(4,TP_udhi);
            cstmt.setInt(5,MessageCoding);
            cstmt.setInt(6,MessageLength);
            cstmt.setString(7, msg);
            cstmt.setString(8, Reserve);
            cstmt.setInt(9,seq_num);
            cstmt.setInt(10,Createtime);
            cstmt.execute(); 
            

        } catch (SQLException e) {
            e.printStackTrace();
        }        
        cp.freeConnection(conn);              //释放连接 
    }
    
    public void savelog(String mobile_no,int stat,int time,String reserve){
    	Connection conn=cp.getConnection();
    	try {
    		CallableStatement cstmt = conn.prepareCall("{call dbo.proc_inslog(?,?,?)}");
            cstmt.setString(1, mobile_no);
            cstmt.setInt(2, stat);
            cstmt.setInt(3, time);
            //cstmt.setString(4, reserve);
            cstmt.execute();  
    		
    		//Statement stmt=conn.createStatement(); 
    		//String sql="insert into sms_log(mobile_no,stat) values( "+mobile_no+","+stat+")";
    		//ResultSet rs=stmt.executeQuery(sql);
    		//rs.close();
    	}catch (SQLException e) {
            e.printStackTrace();
        }        
        cp.freeConnection(conn);
    }
    
}
