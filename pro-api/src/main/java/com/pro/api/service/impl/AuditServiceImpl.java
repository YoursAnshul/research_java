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
            String sqlUpdate = "UPDATE core.auditusertemp SET audituser = ? WHERE auditusertempid = 1";
            this.jdbcTemplate.update(sqlUpdate, netId);
            response.Status="Successfully Updated";
            response.Message = "Updated audituser on core.auditusertemp successfully!!";
        } catch (Exception e) {
            response.Status = "Error Updating audituser on core.auditusertemp";
            response.Message = e.getMessage();
        }
        return response;
    }
}
