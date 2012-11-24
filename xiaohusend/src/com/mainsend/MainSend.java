package com.mainsend;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mainsend.service.ServiceMod;



public class MainSend {
	public ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);
	
	/**
	 * 初始化ctx，返回需要的bean
	 */
	public static ApplicationContext ctx= new ClassPathXmlApplicationContext(
			"applicationContext.xml"		
			);

			public static Object getBean(String beanName){
				return ctx.getBean(beanName);
			}
//======================================================================================================			
	/**
	 * 启动定时器
	 */
	public void lanuchTimer(){                                                            
        Runnable task = new Runnable() {                                                  
            public void run() {                                                           
                throw new RuntimeException();                                             
            }                                                                             
        };                                                                                
        scheduExec.scheduleWithFixedDelay(task, 1000*5, 1000*15, TimeUnit.MILLISECONDS);  
    }  
	
//==============================================================================================================	
	/**
	 * 添加一个任务
	 */
	public void addOneTask(){                                                             
        Runnable task = new Runnable() {                                                  
            public void run() { 
            	ServiceMod s=new ServiceMod();
            	int st=s.getNowRecord();            	
                if (st==0){                  	
             	   return;
             	   }
                else if(st>=1){
                	try {
                		s.startDo(st);                		
					} catch (Exception e) {						
						e.printStackTrace();
					}
				SimpleDateFormat sf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				
                 System.out.println("目前需要发送的记录有"+st+"条!"+sf.format(new Date()));
                }                                   
            }                                                                             
        };                                                                                
        scheduExec.scheduleWithFixedDelay(task, 1000*1, 1000*10, TimeUnit.MILLISECONDS);     
    } 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		MainSend test = new MainSend();                         
        test.lanuchTimer();                                                               
        Thread.sleep(1000*5);//5秒钟之后添加新任务                                        
        test.addOneTask();

	}



}
