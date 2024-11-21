package com.pro.api.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pro.api.response.QuickResponse;
import com.pro.api.service.QuickReference;

@Service
public class QuickReferenceImpl implements QuickReference {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public List<QuickResponse> getQuikGeneralResponse(String type) {
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT projectname,projectcolor,projectcolor_iview, ");
		sql.append(" voicemailnumber,voicemailpin,active ");
		sql.append(" FROM core.projects WHERE active = 1 order by projectname asc ");

		List<QuickResponse> list = this.jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			QuickResponse quickResponse = new QuickResponse();
			quickResponse.setProjectName(rs.getString("projectname"));
			quickResponse.setProjectColor(rs.getString("projectcolor"));
			quickResponse.setVoiceMailNumber(rs.getString("voicemailnumber"));
			quickResponse.setVoiceMailPin(rs.getString("voicemailpin"));
			quickResponse.setActive(rs.getString("active"));
			return quickResponse;

		});
		return list;
	}
	
}
