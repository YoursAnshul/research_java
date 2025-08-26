package com.dimetyd.bot.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.model.CBPOJobData;
import com.dimetyd.bot.model.JobData;
import com.dimetyd.bot.model.Vendor;
import com.dimetyd.bot.process.IRDataVerification;

import jakarta.annotation.PostConstruct;

@Service
public class IRDataVerificationService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	IRDataVerification DataVerification;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {
		if (GlobalSession.getGlobalSession().getName().equals("IRDataverification")) {

			logger.info("IRDataverification started ...");

			StringBuilder sql = new StringBuilder();

			logger.info("Checking for the job ...");

			sql.append(
					"SELECT a.`id`,v.vendorName,a.`vendorId`,a. `jobStatus`,'MainType' as tableType FROM `CbMissingInvoiceRequest` a join Vendor v on (a.vendorid = v.id) \r\n"
					+ "WHERE `jobStatus` IN ('INPROGRESS','QACOMPLETED','CREATEING_INVOICE','CREATING_INVOICE') AND (a.`is_IRsplitView` = 0 OR a.`is_IRsplitView` IS NULL)  "
					+ "union "
					+ "SELECT a.`id`,v.vendorName,a.`vendorId`,a. `jobStatus`,'SplitType' AS tableType  FROM `CbMissingInvoiceRequestSplit` a JOIN Vendor v ON (a.vendorid = v.id) \r\n"
					+ "WHERE `jobStatus` IN ('CREATEING_INVOICE','CREATING_INVOICE') ");

			List<Vendor> result = this.jdbcTemplate.query(sql.toString(), new RowMapper<Vendor>() {
				@Override
				public Vendor mapRow(ResultSet rs, int rowNum) throws SQLException {
					Vendor jobData = new Vendor();
					jobData.setId(rs.getString("id"));
					jobData.setVendorId(rs.getString("vendorId"));
					jobData.setDataStatus(rs.getString("jobStatus"));
					jobData.setVendorName(rs.getString("vendorName"));
					jobData.setTableType(rs.getString("tableType"));

					return jobData;
				}
			}, new Object[] {});
			
			
			for (Vendor results1 : result) {
				
				logger.info("-----------------**************----------------------");
				logger.info("Checking inprogress job: "+results1.getVendorName());
				
				
				DataVerification.checkJobStatus(results1.getVendorId(), results1.getId(), results1.getDataStatus(),
						results1.getVendorName(),results1.getTableType());

			}

		

		}

	}

}
