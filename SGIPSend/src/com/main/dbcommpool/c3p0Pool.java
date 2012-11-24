package com.main.dbcommpool;

import java.sql.Connection;



import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;


public class c3p0Pool {
	private static c3p0Pool instance;
	private ComboPooledDataSource ds;
	private c3p0Pool() {
		ds=new ComboPooledDataSource();
		try {
			ds.setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}		
		ds.setJdbcUrl("jdbc:sqlserver://localhost:1433;DatabaseName=SGIP");
		ds.setUser("cncsp");
		ds.setPassword("cncsp123");
		ds.setInitialPoolSize(5);
		ds.setMaxPoolSize(60);
		ds.setAcquireIncrement(2);  //�����ӳغľ���ʱ��һ���Ի�ȡ������
		ds.setIdleConnectionTestPeriod(60);  //ÿ60�������ӳصĿ�������
		ds.setMaxIdleTime(250);//2500����δʹ���򱻶�����Ϊ0Ϊ��������
		//���ӹر�ʱĬ�Ͻ�����δ�ύ�Ĳ����ع���Default: false autoCommitOnClose  
		//ds.setAutoCommitOnClose(true); 
		

	}
	
	public static synchronized c3p0Pool getInstance(){
		if(null==instance){
			try {
				instance=new c3p0Pool();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
		}
		return instance;
	}
	public synchronized final Connection getConnection(){
		try {
			return ds.getConnection();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
	
	protected void finalize() throws Throwable{
		DataSources.destroy(ds);
		super.finalize();  
	}

}
