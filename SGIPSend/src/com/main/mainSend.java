package com.main;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class mainSend {
	public ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);
	private Socket mySocket;
	private DataInputStream Dis;
	private DataOutputStream Dos;
	
	public void mainSend2() throws Exception{
		
		mySocket = new Socket("10.143.4.71",8801);
		//mySocket = new Socket("127.0.0.1",8801);
		System.out.println("�������ӵ�"+mySocket.getInetAddress().getHostAddress());
		Dis=new DataInputStream(mySocket.getInputStream());
		Dos=new DataOutputStream(mySocket.getOutputStream());
		
		Thread t1=new Send(Dos,Dis,mySocket);		
		t1.start();
		
	}
	
	/**
	 * ������ʱ��
	 */
	public void lanuchTimer(){                                                            
        Runnable task = new Runnable() {                                                  
            public void run() {                                                           
                throw new RuntimeException();                                             
            }                                                                             
        };                                                                                
        scheduExec.scheduleWithFixedDelay(task, 1000*5, 1000*15, TimeUnit.MILLISECONDS);  
    }     
	
	/**
	 * ���һ������
	 */
	public void addOneTask(){                                                             
        Runnable task = new Runnable() {                                                  
            public void run() { 
            	SendServices s=new SendServices();
            	int st=s.keepCheck();
            	//int st=1;
                if (st==0){  
                	//System.out.println("Ŀǰ��Ҫ���͵ļ�¼��"+st+"��!");
             	   return;
             	   }
                else if(st>=1){
                	try {
                		mainSend w=new mainSend();
                		w.mainSend2();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
                 Date d=new Date();
                 System.out.println("Ŀǰ��Ҫ���͵ļ�¼��"+st+"��!"+d.getTime());
                }                                   
            }                                                                             
        };                                                                                
        scheduExec.scheduleWithFixedDelay(task, 1000*1, 1000*10, TimeUnit.MILLISECONDS);     
    }     

	public static void main(String[] args) throws Exception {
		mainSend s=new mainSend();
		s.lanuchTimer();
		Thread.sleep(1000*5);//5����֮�����������                                        
        s.addOneTask(); 
        System.out.println("���ӷ�������...");
	}
	
}
