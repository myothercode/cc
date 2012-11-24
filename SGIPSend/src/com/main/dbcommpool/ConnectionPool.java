package com.main.dbcommpool;



import java.sql.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author java 连接池
 */
public class ConnectionPool {
    private int minConn;        //最少连接数
    private int maxConn;        //最大连接数
    private String user;        //用户名
    private String password;    //数据库密码
    private int connAmount;     //现有连接个数
    private Stack connStack;    //使用Stack保存数据库连接
    private String drv;         //驱动
    private String connString;  //连接字符串   
    private String httpServerAdd;   //hppt服务器地址
    private static ConnectionPool  connectionPool;
    private int waitTime;       //忙时的等告待时间
   
    private String config_file_name ="config//dbcp.properties";
    
    public  static synchronized ConnectionPool getInstance(){
        if (connectionPool==null) {
            connectionPool = new ConnectionPool();              
        }
        return connectionPool;         
    }
    /**
    *读属性文件得到数据库连接信息及连接池信息
    */
    public void readProperties(){

    	Properties prop = new Properties();
        InputStream is = null;
        try
        {
            is = new FileInputStream(config_file_name);
            prop.load(is);
        }
        catch (Exception e)
        {
            System.out.println(e + " file "+config_file_name+" not found");
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        
        try{
            this.user      = prop.getProperty("user");
            this.password  = prop.getProperty("pwd");
            this.connString= prop.getProperty("jdbcurl");
            this.drv       = prop.getProperty("driverclassname");
            
            this.minConn       = Integer.parseInt (prop.getProperty("minConn"));
            System.out.println("Database connection pool："+this.minConn);
            this.maxConn       = Integer.parseInt(prop.getProperty("maxConn"));
            this.waitTime      = Integer.parseInt(prop.getProperty("waitTime")); 
            this.httpServerAdd = prop.getProperty("httpServerAdd");
        }catch(Exception e){
        	System.exit(0);
        	System.out.println(e.toString());
        }
    }
    
    private ConnectionPool()
    {
        readProperties();
        this.connStack = new Stack();
        //System.out.println(this.drv);
        try{
            Class.forName(drv);            
        }catch(Exception e){
           e.printStackTrace();
        }   
        for (int i=0 ; i < minConn;i++){
            //System.out.println("myPool:"+i);
            connStack.push(newConnection());
        }
        System.out.println("Database connection pool initialization ok！");
    }
   /**
   *从连接池得到连接
   */
    public synchronized  Connection getConnection(){
        Connection conn = null;
        System.out.println("user connection:"+this.connStack.size());
        if(!this.connStack.empty()){
             conn = (Connection)connStack.pop();
             System.out.println("得到一个连接");
        }else if(this.connAmount<this.maxConn){
            conn = newConnection();
        }else{
            try{
                wait(this.waitTime);
                System.out.println("等待");
                return getConnection();
            }catch(Exception e){
                e.printStackTrace();
            }//end try
        }
        return conn;
    }
    /**
     * 释放连接
     */
    
    public synchronized void freeConnection(Connection conn){
        this.connStack.push(conn);
        System.out.println("归还连接");
        notifyAll();        
    }
   /**
   *创建新连接
   */  
    public Connection  newConnection(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(this.connString,this.user,this.password);
            this.connAmount++;
            //System.out.println("连接池创建一个连接"+conn.toString());
            return conn;      
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public String gethttpServerAdd(){
    	return httpServerAdd;
    }
}
