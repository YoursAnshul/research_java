package com.dimetyd.bot.process;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class QACompletedJobs {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	DataUpdation dataupdate;

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	

	public boolean CheckInprogressJobs(String in_requestId)
	{
		String requestDetailsQuery = "SELECT count(*) as count FROM `CBAgreementRequestDetails` WHERE requestId = '"
				+ in_requestId + "' and Status!='COMPLETED'";
		List<Long> NotCompletedJobs = jdbcTemplate.query(requestDetailsQuery.toString(),
				(rs, rowNum) -> rs.getLong("count"));
		Long AgreementsNotCompletedCount = NotCompletedJobs != null && NotCompletedJobs.size() > 0
				? NotCompletedJobs.get(0)
				: 0L;
		
		logger.info("Inporgress or Pending jobs count: " + AgreementsNotCompletedCount);
		
		if (AgreementsNotCompletedCount <= 0) {

			
			String UpdateRequestStatus = "UPDATE `CBRequest` SET `requestStatus` = 'QAPENDING' WHERE id = '"
					+ in_requestId + "'";
			jdbcTemplate.execute(UpdateRequestStatus);
			logger.info("Request Status QA Pending Updated of RequestId : " + in_requestId);
			
			
	}
		return true;
	}

	public void startUpdate(String RequestType, String requestId, String vendorId,String jobType) {
		switch (RequestType) {
		case "DROPSHIP": {

			boolean status;
			try {
				status =  dataupdate.UpdateDataDropShip(vendorId, requestId, RequestType);
			} catch (Exception ex) {
				status = false;
				logger.info("---------------------------------------");
				logger.error(ex.toString());
				logger.info("---------------------------------------");

			}

			if (status) {
				/* UPDATE REQUEST STATUS COMPLETED */
				String UpdateRequestStatus = "UPDATE `CBRequest` SET `requestStatus` = 'COMPLETED' ,processedEndDate = now() WHERE id = '"
						+ requestId + "'";
				jdbcTemplate.execute(UpdateRequestStatus);
				logger.info(UpdateRequestStatus);

			}

		
			break;
		}

		case "QTY_MISMATCH":
		{
		
			boolean status;
			try {
				status = dataupdate.UpdateData_QuantityMismatch(vendorId, requestId, RequestType,jobType);
			} catch (Exception ex) {
				status = false;
				logger.info("---------------------------------------");
				logger.error(ex.toString());
				logger.info("---------------------------------------");

			}

			if (status) {
				/* UPDATE REQUEST STATUS COMPLETED */
				String UpdateRequestStatus = "UPDATE `CBRequest` SET `requestStatus` = 'COMPLETED' ,processedEndDate = now() WHERE id = '"
						+ requestId + "'";
				jdbcTemplate.execute(UpdateRequestStatus);
				logger.info(UpdateRequestStatus);

			}

		
			break;
		}

		case "FRIEGHT_CHECK": {

			

			boolean status;
			try {
				status = dataupdate.UpdateDataFreighthCheck(vendorId, requestId, RequestType);
			} catch (Exception ex) {
				status = false;
				logger.info("---------------------------------------");
				logger.error(ex.toString());
				logger.info("---------------------------------------");

			}

			if (status) {
				/* UPDATE REQUEST STATUS COMPLETED */
				String UpdateRequestStatus = "UPDATE `CBRequest` SET `requestStatus` = 'COMPLETED' ,processedEndDate = now() WHERE id = '"
						+ requestId + "'";
				jdbcTemplate.execute(UpdateRequestStatus);
				logger.info(UpdateRequestStatus);

			}

			break;

		}

		case "SHORTAGE_RECONCILIATION": {

		
			boolean status;
			try {
				status = dataupdate.UpdateDataShortage();
			} catch (Exception ex) {
				status = false;
				logger.info("---------------------------------------");
				logger.error(ex.toString());
				logger.info("---------------------------------------");

			}


			if (status) {
				/* UPDATE REQUEST STATUS COMPLETED */
				String UpdateRequestStatus = "UPDATE `CBRequest` SET `requestStatus` = 'COMPLETED' ,processedEndDate = now() WHERE id = '"
						+ requestId + "'";
				jdbcTemplate.execute(UpdateRequestStatus);
				logger.info(UpdateRequestStatus);
			}

		
			break;
		}
		
		case "PROMOTIONAL_AGREEMENT_CHECK": {

			
			boolean status;
			try {
				status = dataupdate.UpdateDataPromationalAgreementCheck(vendorId, requestId, RequestType);
			} catch (Exception ex) {
				status = false;
				logger.info("---------------------------------------");
				logger.error(ex.toString());
				logger.info("---------------------------------------");

			}


			if (status) {
				/* UPDATE REQUEST STATUS COMPLETED */
				String UpdateRequestStatus = "UPDATE `CBRequest` SET `requestStatus` = 'COMPLETED' ,processedEndDate = now() WHERE id = '"
						+ requestId + "'";
				jdbcTemplate.execute(UpdateRequestStatus);
				logger.info(UpdateRequestStatus);
			}

		
			break;
		}
		
		
		case "NOTFEATURED_PROMOTIONAL_AGREEMENT": {
			
			

			boolean status;
			try {
				status = dataupdate.UpdateDataPromationalAgreementCheck(vendorId, requestId, RequestType);
			} catch (Exception ex) {
				status = false;
				logger.info("---------------------------------------");
				logger.error(ex.toString());
				logger.info("---------------------------------------");

			}


			if (status) {
				/* UPDATE REQUEST STATUS COMPLETED */
				String UpdateRequestStatus = "UPDATE `CBRequest` SET `requestStatus` = 'COMPLETED' ,processedEndDate = now() WHERE id = '"
						+ requestId + "'";
				jdbcTemplate.execute(UpdateRequestStatus);
				logger.info(UpdateRequestStatus);
			}

			break;
		}
		
		
		case "KILLED_PROMOTIONAL_AGREEMENT": {
			
			
			

			boolean status;
			try {
				status = dataupdate.UpdateDataPromationalAgreementCheck(vendorId, requestId, RequestType);
			} catch (Exception ex) {
				status = false;
				logger.info("---------------------------------------");
				logger.error(ex.toString());
				logger.info("---------------------------------------");

			}


			if (status) {
				/* UPDATE REQUEST STATUS COMPLETED */
				String UpdateRequestStatus = "UPDATE `CBRequest` SET `requestStatus` = 'COMPLETED' ,processedEndDate = now() WHERE id = '"
						+ requestId + "'";
				jdbcTemplate.execute(UpdateRequestStatus);
				logger.info(UpdateRequestStatus);
			}

			break;
		}
		
		

		}


	}
	
	

}
