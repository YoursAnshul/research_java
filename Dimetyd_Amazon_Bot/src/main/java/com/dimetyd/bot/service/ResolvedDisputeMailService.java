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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class ResolvedDisputeMailService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("MPG_Alerts")) {
            logger.info("MPG_Alerts dispute bot started.");
            generateAndSendReport();
        }
    }

    public void generateAndSendReport() {
        logger.info("Starting MPG Alert Status Report...");

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String id = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String directoryPath = "C:\\PlaywrightFiles\\";
        String filePath = directoryPath + "MPG_Alert_Status_" + currentDate + ".xlsx";

        File directory = new File(directoryPath);
        if (!directory.exists()) directory.mkdirs();

        List<Map<String, Object>> reportData = jdbcTemplate.queryForList(getBulkShortageQuery());

        if (reportData.isEmpty()) {
            logger.info("No rows to send. Skipping.");
            return;
        }

        File reportFile = createExcelReport(filePath, reportData);
        if (reportFile == null) {
            logger.error("Failed to generate report.");
            return;
        }

        String uploadedFileName = uploadFileToApi(reportFile);
        if (uploadedFileName == null) {
            logger.error("Upload failed, aborting.");
            return;
        }

        String subject = "Daily Dispute Recovery Update of MPG Accounts [" + currentDate + "]";
        String body = "Hi Team,<br><br>" +
                "We have attached today’s summary of disputes resolved across all MPG accounts. " +
                "The report includes vendor-level dispute and refund details.<br><br>" +
                "No action required — just keeping you updated as part of our daily alerts. " +
                "And, if you have any questions, please feel free to reach out to us directly.<br><br>" +
                "Thanks,<br><br>" +
                "3CG DimeTyd Dispute Team";

        String ccEmail = "gabhandari@threecolts.com";
        String toEmail = "pshinde@threecolts.com";

        insertEmailAlertToDb(id, subject, body, ccEmail, toEmail, uploadedFileName);
    }

    private String getBulkShortageQuery() {
        return "SELECT a.type AS `Dispute Type`, b.`companyName` AS `Vendor Name`, a.disputeId AS `Dispute ID`, " +
                "a.disputeAmount AS `Dispute Amount`, c.approvedAmount AS `Refund Amount (VC)`, " +
                "c.resolvedDate AS `Resolved Date` " +
                "FROM CBClientDispute a " +
                "JOIN `Client` b ON a.vendorId = b.vendorId " +
                "JOIN CBDispute c ON a.disputeId = c.disputeId " +
                "WHERE DATE(c.`resolvedDate`) = '2025-08-06' " +
                "AND a.type = 'Coop' " +
                "AND a.status = 'ACTIVE' " +
                "AND c.disputeStatus = 'RESOLVED'";
    }

    private File createExcelReport(String filePath, List<Map<String, Object>> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            writeSheet(workbook, "MPGResolvedDisputes", data);

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
            Cell cell = headerRow.createCell(colIndex++);
            cell.setCellValue(key);
        }

        int rowIndex = 1;
        for (Map<String, Object> row : data) {
            Row dataRow = sheet.createRow(rowIndex++);
            colIndex = 0;
            for (Object value : row.values()) {
                Cell cell = dataRow.createCell(colIndex++);
                cell.setCellValue(value != null ? value.toString() : "");
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
                    String uploadedFileName = jsonNode.get("url").asText();
                    logger.info("File uploaded successfully: " + uploadedFileName);
                    return uploadedFileName;
                } else {
                    logger.error("No 'url' field found in response: {}", response.getBody());
                }
            }
        } catch (Exception e) {
            logger.error("File upload failed", e);
        }
        return null;
    }

    private void insertEmailAlertToDb(String id, String subject, String body, String ccEmail, String toEmail, String filePath) {
        try {
            // Step 1: Delete existing entry with same ID
            String deleteSql = "DELETE FROM EmailAlerts WHERE id = ?";
            jdbcTemplate.update(deleteSql, id);
            logger.info("Deleted existing alert with ID {}", id);

            // Step 2: Insert new entry
            String insertSql = "INSERT INTO EmailAlerts (id, Subject, Body, ccEmail, filePath, emailStatus, isAttachment, toEmail) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, id, subject, body, ccEmail, filePath, "PENDING", true, toEmail);

            logger.info("Inserted new email alert into DB with ID {}", id);
        } catch (Exception e) {
            logger.error("Error inserting email alert into DB", e);
        }
    }

    public List<Map<String, Object>> getAllEmailAlerts() {
        String sql = "SELECT * FROM EmailAlerts ORDER BY created_at DESC";
        return jdbcTemplate.queryForList(sql);
    }
}
