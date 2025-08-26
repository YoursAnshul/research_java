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
import com.dimetyd.bot.model.JobRequest;
import com.dimetyd.bot.process.QACompletedJobs;

import jakarta.annotation.PostConstruct;

@Service
public class OBDataPublishService {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	QACompletedJobs CompletedJobs;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("OB_Data_Publish_Bot")) {

			StringBuilder sql = new StringBuilder();
			sql.append(
					"	SELECT r.`id`,`RequestType`,`vendorId`,`vendorName`, `isSent`,IFNULL(createdBy,0)  as createdBy , `isReRunOrNewJob` as jobType"
							+ "		FROM CBRequest r join Vendor v on (v.id=r.vendorId)  "
							+ "WHERE `requestStatus` = 'QACOMPLETED' "
							+ "and r.RequestType IN ('SHORTAGE_RECONCILIATION','PROMOTIONAL_AGREEMENT_CHECK','NOTFEATURED_PROMOTIONAL_AGREEMENT','KILLED_PROMOTIONAL_AGREEMENT','QTY_MISMATCH','FRIEGHT_CHECK','DROPSHIP')");

			List<JobRequest> jobRequestList = this.jdbcTemplate.query(sql.toString(), new RowMapper<JobRequest>() {
				@Override
				public JobRequest mapRow(ResultSet rs, int rowNum) throws SQLException {

					JobRequest jr = new JobRequest();

					jr.setId(rs.getString("id"));
					jr.setRequestType(rs.getString("RequestType"));
					jr.setVendorId(rs.getString("vendorId"));
					jr.setIsSent(rs.getString("isSent"));
					jr.setCreatedBy(rs.getString("createdBy"));
					jr.setVendorName(rs.getString("vendorName"));
					jr.setJobType(rs.getString("jobType"));
					return jr;
				}
			}, new Object[] {});

			if (jobRequestList.size() == 0) {
				logger.info("No Job Request is QA COMPLETED");
				sql = new StringBuilder();
				sql.append("SELECT `id`,`RequestType`,`vendorId`, `isSent`,IFNULL(createdBy,0)  as createdBy "
						+ "FROM CBRequest WHERE `requestStatus` = 'INPROGRESS' and "
						+ "RequestType IN ('SHORTAGE_RECONCILIATION','PROMOTIONAL_AGREEMENT_CHECK','NOTFEATURED_PROMOTIONAL_AGREEMENT','KILLED_PROMOTIONAL_AGREEMENT')");
				jobRequestList = this.jdbcTemplate.query(sql.toString(), new RowMapper<JobRequest>() {
					public JobRequest mapRow(ResultSet rs, int rowNum) throws SQLException {

						JobRequest jr = new JobRequest();

						jr.setId(rs.getString("id"));
						jr.setRequestType(rs.getString("RequestType"));
						jr.setVendorId(rs.getString("vendorId"));
						jr.setIsSent(rs.getString("isSent"));
						jr.setCreatedBy(rs.getString("createdBy"));
						jr.setVendorName(rs.getString("vendorId"));
						return jr;
					}
				}, new Object[] {});
				if (jobRequestList.size() == 0) {
					logger.info("No Job Status is in INPROGRESS");
				} else {
					logger.info("INPROGRESS Jobs : " + jobRequestList.size());
					for (JobRequest jobrequestlist : jobRequestList) {
						String RequestId = jobrequestlist.getId();

						logger.info("-----------------------*******************----------------------------");
						logger.info("Checking for venodr: " + jobrequestlist.getVendorName() + " and job id: "
								+ RequestId + " Job type: " + jobrequestlist.getRequestType());
						// Status INPROGRESS of Jobs
						CompletedJobs.CheckInprogressJobs(RequestId);
					}
				}
			} else {
				logger.info("QA Completed Jobs : " + jobRequestList.size());
				for (JobRequest jobrequestlist : jobRequestList) {
					String RequestType = jobrequestlist.getRequestType();
					String RequestId = jobrequestlist.getId();
					String VendorId = jobrequestlist.getVendorId();

					// QA Completed Data updation
					CompletedJobs.startUpdate(RequestType, RequestId, VendorId, jobrequestlist.getJobType());

				}
			}

			// retry update for error and inprogress jobs -
			String updateAgreementQuery =
					"UPDATE `CBAgreementRequestDetails` b " +
					"JOIN `CBRequest` a ON (a.id=b.`requestId`) " +
					"SET b.`status` = 'PENDING', b.machineName= NULL , b.retry = 0 " +
					"WHERE a.`RequestType` = 'SHORTAGE_RECONCILIATION' " +
					"AND DATE(a.createdDate) > '2025-07-05' " +
					"AND a.`requestStatus` = 'INPROGRESS' " +
					"AND b.`status` IN ('ERROR','INPROGRESS') " +
					"AND b.`processedStartDate` < NOW() - INTERVAL 30 MINUTE";

			int rowsAffected = jdbcTemplate.update(updateAgreementQuery);
			logger.info("Updated CBAgreementRequestDetails rows: " + rowsAffected);
		}
	}
		

}
