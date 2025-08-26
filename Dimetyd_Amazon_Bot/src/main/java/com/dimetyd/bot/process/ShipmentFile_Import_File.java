package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShipmentFile_Import_File {

	@Autowired
	JdbcTemplate jdbcTemplate;


		public static String getInvoiceIdsFromExcel(String filePath) {
	        StringBuilder invoiceIdString = new StringBuilder();
	      
	        try (FileInputStream fis = new FileInputStream(new File(filePath));
	             Workbook workbook = new HSSFWorkbook(fis)) {
	             
	            Sheet sheet = workbook.getSheetAt(0);  // Get the first sheet (you can change the index as needed)
	            
	            // Assuming the first row contains headers
	            Row headerRow = sheet.getRow(0);
	            int invoiceIdColumnIndex = -1;
	            
	            // Find the "Invoice ID" column index
	            for (Cell cell : headerRow) {
	                if (cell.getStringCellValue().equalsIgnoreCase("Invoice ID")) {
	                    invoiceIdColumnIndex = cell.getColumnIndex();
	                    break;
	                }
	            }

	            // If the "Invoice ID" column is found
	            if (invoiceIdColumnIndex != -1) {
	                // Iterate through all rows in the sheet (starting from row 1 to skip headers)
	                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
	                    Row row = sheet.getRow(i);
	                    Cell invoiceIdCell = row.getCell(invoiceIdColumnIndex);
	                    
	                    if (invoiceIdCell != null) {
	                        String invoiceId = invoiceIdCell.getStringCellValue();
	                        invoiceIdString.append(invoiceId).append("\n");  // Add to string with new line
	                    }
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
	        return invoiceIdString.toString();
	    }


}
