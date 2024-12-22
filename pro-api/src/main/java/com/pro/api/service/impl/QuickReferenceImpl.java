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
		sql.append(" SELECT projectname,projectcolor, ");
		sql.append(" voicemailnumber,voicemailpin ");
		sql.append(" FROM core.projects WHERE active = 1 and projectType <> 4 order by projectname asc ");

		List<QuickResponse> list = this.jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			QuickResponse quickResponse = new QuickResponse();
			quickResponse.setProjectName(rs.getString("projectname"));
			quickResponse.setProjectColor(rs.getString("projectcolor"));
			quickResponse.setVoiceMailNumber(rs.getString("voicemailnumber"));
			quickResponse.setVoiceMailPin(rs.getString("voicemailpin"));
			return quickResponse;

		});
		return list;
	}

	@Override
	public List<QuickResponse> getQuikProjectInfo(String type) {
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT projectname,projectcolor, ");
		sql.append(" tollfreenumber,studyemailaddress, projectinfo ");
		sql.append(" FROM core.projects WHERE active = 1 and projectType <> 4 order by projectname asc ");

		List<QuickResponse> list = this.jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			QuickResponse quickResponse = new QuickResponse();
			quickResponse.setProjectName(rs.getString("projectname"));
			quickResponse.setProjectColor(rs.getString("projectcolor"));
			quickResponse.setTollfreenumber(rs.getString("tollfreenumber"));
			quickResponse.setStudyemailaddress(rs.getString("studyemailaddress"));
			quickResponse.setProjectinfo(rs.getString("projectinfo"));
			return quickResponse;

		});
		return list;
	}

	@Override
	public List<QuickResponse> getQuikTeamContact(String type) {
		StringBuilder sql = new StringBuilder();
		sql.append("  select userid,u.dempoid,  u.phonenumber, emailaddr, f.dropdownitem as roletext, ");
		sql.append("  u.role , coalesce(nullif(u.preferredfname, ''), u.fname) ||' '||  ");
		sql.append("  coalesce (nullif(u.preferredlname, ''), u.lname) as fullname from core.users u ");
		sql.append("  join (select codevalues, dropdownitem from core.dropdownvalues where formfieldid = 10)f ");
		sql.append("  on u.role = f.codevalues where  u.role <> 4 order by fullname asc ");
		List<QuickResponse> list = this.jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
			QuickResponse quickResponse = new QuickResponse();
			quickResponse.setUsername(rs.getString("fullname"));
			quickResponse.setEscalationphone(rs.getString("phonenumber"));
			quickResponse.setEmailAddress(rs.getString("emailaddr"));
			quickResponse.setRole(rs.getString("roletext"));
			return quickResponse;

		});
		return list;
	}

	@Override
	public String getMainVmPhone() {
	    String sql = "SELECT a.optionvalue FROM adminoptions a " +
	                 "JOIN formfields f ON a.formfieldid = f.formfieldid " +
	                 "WHERE f.columnname = 'mainvmphone'";
	    List<String> results = this.jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("optionvalue"));
	    return results.isEmpty() ? null : results.get(0);
	}


}
