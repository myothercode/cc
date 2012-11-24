package com.db;

import java.sql.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author java ���ӳ�
 */
public class ConnectionPool {
    private int minConn;        //����������
    private int maxConn;        //���������
    private String user;        //�û���
    private String password;    //���ݿ�����
    private int connAmount;     //�������Ӹ���
    private Stack connStack;    //ʹ��Stack�������ݿ�����
    private String drv;         //����
    private String connString;  //�����ַ���   
    private String httpServerAdd;   //hppt��������ַ
    private static ConnectionPool  connectionPool;
    private int waitTime;       //æʱ�ĵȸ��ʱ��
   
    private String config_file_name ="config//dbcp.properties";
    
    public  static synchronized ConnectionPool getInstance(){
        if (connectionPool==null) {
            connectionPool = new ConnectionPool();              
        }
        return connectionPool;         
    }
    /**
    *�������ļ��õ����ݿ�������Ϣ�����ӳ���Ϣ
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
            System.out.println("Database connection pool��"+this.minConn);
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
        System.out.println("Database connection pool initialization ok��");
    }
   /**
   *�����ӳصõ�����
   */
    public synchronized  Connection getConnection(){
        Connection conn = null;
        //System.out.println("user connection:"+this.connStack.size());
        if(!this.connStack.empty()){
             conn = (Connection)connStack.pop();
            // System.out.println("�õ�һ������");
        }else if(this.connAmount<this.maxConn){
            conn = newConnection();
        }else{
            try{
                wait(this.waitTime);
                System.out.println("�ȴ�");
                return getConnection();
            }catch(Exception e){
                e.printStackTrace();
            }//end try
        }
        return conn;
    }
    /**
     * �ͷ�����
     */
    
    public synchronized void freeConnection(Connection conn){
        this.connStack.push(conn);
        //System.out.println("�黹����");
        notifyAll();        
    }
   /**
   *����������
   */  
    public Connection  newConnection(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(this.connString,this.user,this.password);
            this.connAmount++;
            //System.out.println("���ӳش���һ������"+conn.toString());
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
