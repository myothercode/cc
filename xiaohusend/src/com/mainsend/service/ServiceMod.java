package com.mainsend.service;

import com.mainsend.SendMPOJO;
import com.mainsend.ThreadClass;
import com.mainsend.dao.AllDao;

public class ServiceMod {
	/**
	 * 返回是否有需要处理的记录
	 * @return
	 */
	public int getNowRecord(){
	AllDao ad=new AllDao();	
	return ad.checkStatNum();
	}
	
	/**
	 * 开始线程
	 */
	public void startDo(int i){
		Thread t=new ThreadClass(1);
		t.start();
		if(i>=10){
			Thread t1=new ThreadClass(2);
			t1.start();
		}
	}
	
	/**
	 * 获取记录
	 * @return
	 */
	public SendMPOJO getPojo(int i){
		AllDao ad=new AllDao();
		return ad.getOnePOJO(i);
	}
	
	/**
	 * 出错后更新状态
	 * @param sendMPOJO
	 */
	public int errorUpdate(SendMPOJO sendMPOJO){
		AllDao ad=new AllDao();
		return ad.updateErrStat(sendMPOJO);
	}
	
	/**
	 * 成功后更新状态
	 */
	public int successUpdate(SendMPOJO sendMPOJO){
		AllDao ad=new AllDao();
		return ad.updateStat(sendMPOJO);
	}
	
}
