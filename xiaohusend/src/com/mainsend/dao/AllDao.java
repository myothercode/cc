package com.mainsend.dao;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.mainsend.SendMPOJO;
import com.mainsend.c3p0jdbctemple.JdbctempleManager;

public class AllDao {
	
	
	/**
	 * 得到复合条件的第一条记录
	 * @return
	 */
	public SendMPOJO getOnePOJO(int i){
		SendMPOJO sendMPOJO=new SendMPOJO();;
		JdbctempleManager s=JdbctempleManager.getInstance();
		JdbcTemplate jtp=(JdbcTemplate)s.getJdbctem();
		String sql="select top 1 [time],mobile_no,message,spcode,stat,reserve from sms_log_new where flag='3' and stat is not null and reserve is not null and (spcode in('9081112400','9081234001')) order by stime asc";
		if(i==2){
			sql="select top 1 [time],mobile_no,message,spcode,stat,reserve from sms_log_new where flag='3' and stat is not null and reserve is not null and (spcode in('9081112400','9081234001')) order by stime desc";
		}
		List rs= jtp.queryForList(sql);
		sendMPOJO.setLinkId((String)((Map)rs.get(0)).get("time") );
		sendMPOJO.setMobile((String)((Map)rs.get(0)).get("mobile_no"));
		sendMPOJO.setMsg((String)((Map)rs.get(0)).get("message"));
		sendMPOJO.setSpcode((String)((Map)rs.get(0)).get("spcode"));
		sendMPOJO.setStat((String)((Map)rs.get(0)).get("stat"));
		
		List reserve=jtp.queryForList("select msg from deliver where reserve='"+(String)((Map)rs.get(0)).get("reserve")+"' and mobile_no='"+sendMPOJO.getMobile()+"'" );
		sendMPOJO.setMsg((String)((Map)reserve.get(0)).get("msg"));
		System.out.println("找到需要发送的条目:"+sendMPOJO.getMobile()+"::"+sendMPOJO.getMsg()+"::"+sendMPOJO.getStat()+"thread is "+i);
		return sendMPOJO;
	}
	
	/**
	 * 更新状态为4，为已经发送成功
	 * @param sendMPOJO
	 */
	public int updateStat(SendMPOJO sendMPOJO){
		JdbctempleManager s=JdbctempleManager.getInstance();
		JdbcTemplate jtp=(JdbcTemplate)s.getJdbctem();
		int c=jtp.update("update sms_log_new set flag='4' where flag='3' and [time]='"+sendMPOJO.getLinkId()+"' and mobile_no='"+sendMPOJO.getMobile()+"'");
		System.out.println("更新了"+c+"条!");
		return c;
	}
	
	/**
	 * 出错后更新为5，表示没有发送成功
	 * @param sendMPOJO
	 */
	public int updateErrStat(SendMPOJO sendMPOJO){
		JdbctempleManager s=JdbctempleManager.getInstance();
		JdbcTemplate jtp=(JdbcTemplate)s.getJdbctem();
		
		int c=jtp.update("update sms_log_new set flag='5' where flag='3' and [time]='"+sendMPOJO.getLinkId()+"' and mobile_no='"+sendMPOJO.getMobile()+"'");
		System.out.println("更新了"+c+"条!");
		return c;
	}
	
	/**
	 * 检查当前是否有需要发送的记录
	 * @return
	 */
	public int checkStatNum(){
		
		JdbctempleManager s=JdbctempleManager.getInstance();
		JdbcTemplate jtp=(JdbcTemplate)s.getJdbctem();
		int c=jtp.queryForInt("select count(1) num from sms_log_new where flag='3' and stat is not null and reserve is not null and (spcode in('9081112400','9081234001'))");
		return c;
	}
	

}
