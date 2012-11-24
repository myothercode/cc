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
		ds.setAcquireIncrement(2);  //当连接池耗尽的时候一次性获取的数量
		ds.setIdleConnectionTestPeriod(60);  //每60秒检查连接池的空闲连接
		ds.setMaxIdleTime(250);//2500面临未使用则被丢弃，为0为永不丢弃
		//连接关闭时默认将所有未提交的操作回滚。Default: false autoCommitOnClose  
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
