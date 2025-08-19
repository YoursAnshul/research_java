package com.pro.api.service.impl;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public GeneralResponse updateNetId(String netId) {
		GeneralResponse response = new GeneralResponse();
		try {
			String checkSql = "SELECT COUNT(*) FROM core.auditusertemp WHERE auditusertempid = 1";
			Integer count = this.jdbcTemplate.queryForObject(checkSql, Integer.class);

			if (count != null && count > 0) {
				String sqlUpdate = "UPDATE core.auditusertemp SET audituser = ? WHERE auditusertempid = 1";
				this.jdbcTemplate.update(sqlUpdate, netId);
				response.Status = "Successfully Updated";
				response.Message = "Updated audituser on core.auditusertemp successfully!!";
			} else {
				String sqlInsert = "INSERT INTO core.auditusertemp (auditusertempid, audituser) VALUES (1, ?)";
				this.jdbcTemplate.update(sqlInsert, netId);
				response.Status = "Successfully Inserted";
				response.Message = "Inserted audituser into core.auditusertemp successfully!!";
			}

		} catch (Exception e) {
			response.Status = "Error Updating/Inserting audituser on core.auditusertemp";
			response.Message = e.getMessage();
		}
		return response;
	}

}
