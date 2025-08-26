package com.dimetyd.bot.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class BulkShortageMailService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void startService() {
        if (GlobalSession.getGlobalSession().getName().equals("BulkShortageMailService")) {
            logger.info("BulkShortageMailService bot started.");
            generateAndSendReport();
        }
    }

    public void generateAndSendReport() {
        logger.info("Starting Bulk Shortage Case Status Report...");

        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String directoryPath = "C:\\PlaywrightFiles\\";
        String filePath = directoryPath + "Bulk_Shortage_Case_Status_" + currentDate + ".xlsx";

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

        String subject = "Bulk Shortage Case Status";
        String body = "Hello Team,<br><br> Please find attached the sheet for the updated bulk shortage cases.<br><br>Regards,<br>Dimetyd Robotics Team";
        String ccEmail = "gabhandari@threecolts.com,pshinde@threecolts.com";
        String toEmail = "shortages@dimetyd.com";

        deletePreviousEmailAlert(subject);
        insertEmailAlertToDb(subject, body, ccEmail, toEmail, uploadedFileName);
    }

    private String getBulkShortageQuery() {
        return "SELECT vendorName,vendorId,caseId, oldCaseStatus,currentCaseStatus,lastUpdatedDateTime ,'COMPLETED' AS comment " +
               "FROM CaseIdUpdationLog " +
               "WHERE oldCaseStatus!=currentCaseStatus AND jobStatus='COMPLETED' " +
               "UNION " +
               "SELECT v.vendorName,c.vendorId,c.caseId, c.oldCaseStatus,c.currentCaseStatus,c.lastUpdatedDateTime ,'OTP ISSUE' AS comment " +
               "FROM CaseIdUpdationLog c " +
               "JOIN Vendor v ON (c.vendorId=v.id) " +
               "JOIN CBShortageSummaryPayeeCode s ON (c.caseId=s.caseId AND c.vendorId=s.vendorId) " +
               "WHERE jobStatus IN('PENDING','INPROGRESS') AND (s.isSettle = 0 OR s.isSettle IS NULL) " +
               "AND v.isVendorMenuAccess='1' AND v.active='Y' AND v.isPaused='N'";
    }

    private File createExcelReport(String filePath, List<Map<String, Object>> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            writeSheet(workbook, "BulkShortageCases", data);

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
                String responseBody = response.getBody();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(responseBody);

                if (jsonNode.has("url")) {
                    String uploadedFileName = jsonNode.get("url").asText();
                    logger.info("File uploaded successfully: " + uploadedFileName);
                    return uploadedFileName;
                } else {
                    logger.error("No 'url' field found in response: {}", responseBody);
                }
            }
        } catch (Exception e) {
            logger.error("File upload failed", e);
        }
        return null;
    }

    private void deletePreviousEmailAlert(String subject) {
        try {
            String sql = "DELETE FROM EmailAlerts WHERE Subject = ?";
            jdbcTemplate.update(sql, subject);
            logger.info("Deleted previous email alert for subject: {}", subject);
        } catch (Exception e) {
            logger.error("Error deleting old email alerts", e);
        }
    }

    private void insertEmailAlertToDb(String subject, String body, String ccEmail, String toEmail, String filePath) {
        try {
            long id = System.currentTimeMillis(); // simple unique id generator

            String sql = "INSERT INTO EmailAlerts (id, Subject, Body, ccEmail, filePath, emailStatus, isAttachment, toEmail) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(sql,
                id,
                subject,
                body,
                ccEmail,
                filePath,
                "PENDING",
                true,
                toEmail
                //Timestamp.valueOf(LocalDateTime.now())
            );

            logger.info("Inserted new email alert into DB with id {}", id);
        } catch (Exception e) {
            logger.error("Error inserting email alert into DB", e);
        }
    }

    public List<Map<String, Object>> getAllEmailAlerts() {
        String sql = "SELECT * FROM EmailAlerts ORDER BY created_at DESC";
        return jdbcTemplate.queryForList(sql);
    }
}
