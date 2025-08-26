package com.dimetyd.bot.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.dimetyd.bot.helper.GlobalSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class ItemizedShortageReportService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void startService() {
        if (!GlobalSession.getGlobalSession().getName().equals("ItemizedReportService")) return;

        logger.info("ItemizedReportService bot started.");

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String directoryPath = "C:\\PlaywrightFiles\\";
        String filePath = directoryPath + "Itemized_Shortage_Report_" + currentDate + ".xlsx";

        File directory = new File(directoryPath);
        if (!directory.exists()) directory.mkdirs();

        List<Map<String, Object>> sheet1Data = jdbcTemplate.queryForList(getFirstQuery());
        List<Map<String, Object>> sheet2Data = jdbcTemplate.queryForList(getSecondQuery());

        File reportFile = createExcelReport(filePath, sheet1Data, sheet2Data);
        if (reportFile == null) {
            logger.error("Failed to generate report.");
            return;
        }

        // Step 1: Upload to API
        String uploadedFileUrl = uploadFileToApi(reportFile);
        if (uploadedFileUrl == null) {
            logger.error("Upload failed. Aborting.");
            return;
        }

        // Step 2: Delete previous email alert with same subject
        String subject = "Itemized Shortage Creation Report - " + currentDate;
        deletePreviousEmailAlert(subject);

        // Step 3: Insert into email alerts DB table
        String body = "Hello Team,\n\nPlease find the attachment for the itemized shortage creation report.\n\nRegards,\nDimetyd Team";
        String ccEmail = "gabhandari@threecolts.com,pshinde@threecolts.com";
        String toEmail = "shortages@dimetyd.com";

        insertEmailAlertToDb(subject, body, ccEmail, toEmail, uploadedFileUrl);
    }

    private String getFirstQuery() {
        return "SELECT a.vendorId,v.vendorName,DATE(a.createdDate) AS createdDate,a.status,a.comment, " +
               "SUM(CASE WHEN b.status IS NOT NULL THEN 1 ELSE 0 END) AS Total, " +
               "SUM(CASE WHEN b.status = 'Disputed' THEN 1 ELSE 0 END) AS disputed, " +
               "SUM(CASE WHEN b.status != 'Disputed' THEN 1 ELSE 0 END) AS NotDisputed " +
               "FROM CBitemizedshortage_input a " +
               "LEFT JOIN CBitemizedshortageInvoicesToBeCreated b ON (a.vendorId=b.vendorId AND DATE(a.createdDate) = DATE(b.createdDate)) " +
               "JOIN Vendor v ON (a.vendorId=v.id) " +
               "WHERE DATE(a.createdDate) = DATE(NOW() - INTERVAL 1 DAY) " +
               "GROUP BY a.vendorId, DATE(a.createdDate)";
    }

    private String getSecondQuery() {
        return "SELECT v.vendorName,`paymentDueDate`,`invoiceStatus`,`actualPaidAmount`,`qtyVarianceAmount`,`payee`,`invoiceCreationDate`, " +
               "`invoiceNumber`,`marketplace`,`invoiceDate`,`invoiceAmount`,`anyDeductions`,`disputeStatus`,`alreadyDisputed`, " +
               "a.`createdDate`,`status`,`comments` " +
               "FROM CBitemizedshortageInvoicesToBeCreated a " +
               "JOIN Vendor v ON (a.vendorId=v.id) " +
               "WHERE DATE(a.createdDate) = DATE(NOW() - INTERVAL 1 DAY)";
    }

    private File createExcelReport(String filePath, List<Map<String, Object>> sheet1Data, List<Map<String, Object>> sheet2Data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            writeSheet(workbook, "Summary", sheet1Data);
            writeSheet(workbook, "Details", sheet2Data);

            File file = new File(filePath);
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

            return file;
        } catch (IOException e) {
            logger.error("Error while creating Excel report", e);
            return null;
        }
    }

    private void writeSheet(Workbook workbook, String sheetName, List<Map<String, Object>> data) {
        Sheet sheet = workbook.createSheet(sheetName);
        if (data.isEmpty()) return;

        Row headerRow = sheet.createRow(0);
        int colIndex = 0;
        for (String key : data.get(0).keySet()) {
            headerRow.createCell(colIndex++).setCellValue(key);
        }

        int rowIndex = 1;
        for (Map<String, Object> row : data) {
            Row dataRow = sheet.createRow(rowIndex++);
            colIndex = 0;
            for (Object value : row.values()) {
                dataRow.createCell(colIndex++).setCellValue(value != null ? value.toString() : "");
            }
        }
    }

    private String uploadFileToApi(File file) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            String uploadUrl = "https://api-dimetyd.threecolts.com/cron/s3-file-upload";

            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(response.getBody());
                if (jsonNode.has("url")) {
                    String uploadedFileUrl = jsonNode.get("url").asText();
                    logger.info("File uploaded: " + uploadedFileUrl);
                    return uploadedFileUrl;
                }
            }
        } catch (Exception e) {
            logger.error("File upload failed", e);
        }
        return null;
    }

    private void deletePreviousEmailAlert(String subject) {
        try {
            jdbcTemplate.update("DELETE FROM EmailAlerts WHERE Subject = ?", subject);
            logger.info("Deleted old alert for subject: {}", subject);
        } catch (Exception e) {
            logger.error("Error deleting email alert", e);
        }
    }

    private void insertEmailAlertToDb(String subject, String body, String ccEmail, String toEmail, String filePath) {
        try {
            String sql = "INSERT INTO EmailAlerts (id, Subject, Body, ccEmail, filePath, emailStatus, isAttachment, toEmail) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, "113", subject, body, ccEmail, filePath, "PENDING", true, toEmail);
            logger.info("Inserted email alert for: {}", subject);
        } catch (Exception e) {
            logger.error("Error inserting alert to DB", e);
        }
    }
}
