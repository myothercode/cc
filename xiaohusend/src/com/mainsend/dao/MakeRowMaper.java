package com.mainsend.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.mainsend.SendMPOJO;

/*
 * 
 */
public class MakeRowMaper implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		SendMPOJO sm=new SendMPOJO();
		sm.setLinkId(rs.getString(""));
		
		return null;
	}

}
