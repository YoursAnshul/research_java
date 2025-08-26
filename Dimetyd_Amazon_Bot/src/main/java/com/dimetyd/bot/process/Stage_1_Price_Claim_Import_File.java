package com.dimetyd.bot.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Stage_1_Price_Claim_Import_File {
	
	

	
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public void importFile(Path filepath, String vendorId) {

		try (BufferedReader br = new BufferedReader(new FileReader(filepath.toString()))) {
			String line;
			
			int counter = 0;
			
			
			while ((line = br.readLine()) != null) {
				
				counter = 	counter + 1;
				
				if (counter == 1 )
					continue;
					
				String[] values = line.split("\",\""); // Split line by commas

				
				logger.info(Arrays.toString(values));
				
				
				
				String paymentDueDate = values[2].replace("\"", "");
				String invoiceStatus = values[3].replace("\"", "");
				String actualPaidAmount = values[4].replace("\"", "");
				String qtyVarianceAmount = null;
				String payee = values[5].replace("\"", "");
				String invoiceCreationDate = values[6].replace("\"", "");
				String invoiceNumber = values[7].replace("\"", "");
				String marketplace = values[0].replace("\"", "");
				String invoiceDate = values[1].replace("\"", "");
				String invoiceAmount = values[8].replace("\"", "");
				String anyDeductions = values[9].replace("\"", "");
				String disputeStatus =  null;
				String alreadyDisputed =  null;
				
				StringBuilder sql = new StringBuilder();
				sql.append(
						"INSERT INTO `CBPriceClaimRequestDetails` (`vendorId`,`paymentDueDate`,`invoiceStatus`,`actualPaidAmount`,`qtyVarianceAmount`,`Payee`,`invoiceCreationDate`,`InvoiceNumber`,`marketplace`,"
						+ "`invoiceDate`,`invoiceAmount`,`anyDeductions`,`disputeStatus`,`alreadyDisputed`) "
						+ "VALUES('" + vendorId + "','" + paymentDueDate + "','" + invoiceStatus + "','" + actualPaidAmount
								+ "','" + qtyVarianceAmount + "','" + payee + "','" + invoiceCreationDate + "','"
								+ invoiceNumber + "','" + marketplace + "','" + invoiceDate +"','"+invoiceAmount+"','"+anyDeductions+"','"+disputeStatus+"','"+alreadyDisputed+ "');");

				
				logger.info(sql.toString());;
			
				if(invoiceStatus.contains("Price discrepancy identified"))
					
				{
					logger.info(sql.toString());;
					jdbcTemplate.execute(sql.toString());
				}
				
				
				

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	

}
