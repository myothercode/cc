package com.mainsend;

import com.mainsend.dosend.PostM;
import com.mainsend.service.ServiceMod;

public class ThreadClass extends Thread{
	public int numnow;
	public  ThreadClass(int i){
		this.numnow=i;
	}
	
	public void run(){
		System.out.println("Thread:"+this.numnow+"isstart!");
		ServiceMod sm=new ServiceMod();
		SendMPOJO spo= sm.getPojo(this.numnow);	
		
		PostM pm=new PostM();
		try {			
			if("oko".equals(pm.PostSend(spo))){
				int c=sm.successUpdate(spo);
				System.out.println("执行成功！"+c+"条");
			}
		} catch (Exception e) {
			int c=sm.errorUpdate(spo);
			System.out.println("出现错误，记录错误状态！"+c+"条");
			e.printStackTrace();
		}
	}
	

}
