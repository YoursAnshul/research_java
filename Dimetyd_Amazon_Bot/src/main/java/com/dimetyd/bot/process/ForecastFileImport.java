package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.AsinForecast_Import;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class ForecastFileImport {


    @Autowired
    private JdbcTemplate jdbcTemplate;
 

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void IndexFiledownLoaddown(Page page, String vendorId, String dropdown) throws IOException {
        Locator firstReport;
        while (true) {
            try {
                List<Locator> reportNameList = page
                        .locator("//kat-table-body[@role='rowgroup']//kat-table-row[@role='row']").all();
                firstReport = reportNameList.get(0);
                break;
            } catch (Exception e) {
                // Retry if the locator is not ready
            }
        }

        Locator downloadAvailable = firstReport.locator("text='Download'");
        Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(120000), () -> {
            downloadAvailable.click(new Locator.ClickOptions().setTimeout(120000));
        });

        if (download != null) {
            System.out.println("Download started: " + download.path());
        } else {
            System.out.println("Download did not start.");
        }

        // Save downloaded file
        String downloadDirectory = "c:\\java codes\\Forecast_DownloadFiles";
        Path targetPath = Paths.get(downloadDirectory, download.suggestedFilename());
        download.saveAs(targetPath);
        System.out.println("Path " + targetPath);

        // Process CSV file
        File csvFile = targetPath.toFile();
        System.out.print(csvFile);
        try (FileReader reader = new FileReader(csvFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<AsinForecast_Import> invoiceList = new ArrayList<>();
            int skipFirstRow = 0;

            // After download, click on the cancel button
            page.locator(".ltr-1upyiy5").click();

            int count = 0;
            int limit = 100;
            String startDate = null;
            String endDate = null;

            for (CSVRecord csvRecord : csvParser) {
            	AsinForecast_Import invoice = new AsinForecast_Import();
                if (skipFirstRow == 0) {
                    skipFirstRow++;

                    // Get week start date & end date from this
                    String week_0_header = csvRecord.get(3); // "Week 0 (1 Dec - 7 Dec)" from CSV
                    LocalDate[] weekDates = extractStartAndEndDatesFromHeader(week_0_header);
                    startDate = weekDates[0].toString();
                    endDate = weekDates[1].toString();
                    System.out.print("week_0 header line is " + week_0_header);

                    continue;
                }
                // Using column indexing instead of names
                try {
                    invoice.setasin(csvRecord.get(0)); // ASIN
                    invoice.setproductTitle(csvRecord.get(1)); // Product Title
                    invoice.setbrand(csvRecord.get(2)); // Brand

                    invoice.setweek_0(getValidWeekValue(csvRecord.get(3))); // Week 0
                    invoice.setweek_1(getValidWeekValue(csvRecord.get(4))); // Week 1
                    invoice.setweek_2(getValidWeekValue(csvRecord.get(5))); // Week 2
                    System.out.println("Week 2 Value: " + invoice.getweek_2());
                    invoice.setweek_3(getValidWeekValue(csvRecord.get(6))); // Week 3
                    System.out.println("Week 3 Value: " + invoice.getweek_3());
                    invoice.setweek_4(getValidWeekValue(csvRecord.get(7))); // Week 4
                    System.out.println("Week 4 Value: " + invoice.getweek_4());
                    invoice.setweek_5(getValidWeekValue(csvRecord.get(8))); // Week 5
                    System.out.println("Week 5 Value: " + invoice.getweek_5());
                    invoice.setweek_6(getValidWeekValue(csvRecord.get(9))); // Week 6
                    invoice.setweek_7(getValidWeekValue(csvRecord.get(10))); // Week 7
                    invoice.setweek_8(getValidWeekValue(csvRecord.get(11))); // Week 8
                    invoice.setweek_9(getValidWeekValue(csvRecord.get(12))); // Week 9
                    invoice.setweek_10(getValidWeekValue(csvRecord.get(13))); // Week 10
                    invoice.setweek_11(getValidWeekValue(csvRecord.get(14))); // Week 11
                    invoice.setweek_12(getValidWeekValue(csvRecord.get(15))); // Week 12
                    invoice.setweek_13(getValidWeekValue(csvRecord.get(16))); // Week 13
                    invoice.setweek_14(getValidWeekValue(csvRecord.get(17))); // Week 14
                    invoice.setweek_15(getValidWeekValue(csvRecord.get(18))); // Week 15
                    invoice.setweek_16(getValidWeekValue(csvRecord.get(19))); // Week 16
                    invoice.setweek_17(getValidWeekValue(csvRecord.get(20))); // Week 17
                    invoice.setweek_18(getValidWeekValue(csvRecord.get(21))); // Week 18
                    invoice.setweek_19(getValidWeekValue(csvRecord.get(22))); // Week 19
                    invoice.setweek_20(getValidWeekValue(csvRecord.get(23))); // Week 20
                    invoice.setweek_21(getValidWeekValue(csvRecord.get(24))); // Week 21
                    invoice.setweek_22(getValidWeekValue(csvRecord.get(25))); // Week 22
                    invoice.setweek_23(getValidWeekValue(csvRecord.get(26))); // Week 23
                    invoice.setweek_24(getValidWeekValue(csvRecord.get(27))); // Week 24
                    invoice.setweek_25(getValidWeekValue(csvRecord.get(28))); // Week 25
                } catch (IndexOutOfBoundsException e) {
                    logger.error("Column index out of bounds. Please check the CSV structure.", e);
                }

                invoice.setUniqueKey(invoice.getasin() + vendorId + dropdown+startDate); // Unique Key
                invoiceList.add(invoice);
                System.out.println("Unique Key: " +invoice. getUniqueKey());


                if (count == limit) {
                    count = 0;
                    saveToDatabase(invoiceList, vendorId, dropdown, startDate, endDate); // Pass dropdown (statView)
                    invoiceList.clear();
                } else {
                    count++;
                }
            }

            // Save remaining records
            if (!invoiceList.isEmpty()) {
                saveToDatabase(invoiceList, vendorId, dropdown, startDate, endDate); // Pass dropdown (statView)
            }

        } catch (IOException e) {
            logger.error("Error processing CSV file", e);
        }
        // Delete the file after processing
        if (targetPath != null && Files.exists(targetPath)) {
            try {
                Files.delete(targetPath);
                System.out.println("File deleted: " + targetPath);
            } catch (IOException e) {
                logger.error("Failed to delete file: " + targetPath, e);
            }
        }
    }

    // Get week data & matching pattern
    private LocalDate[] extractStartAndEndDatesFromHeader(String week_0_header) {
        try {
            // Regex to match the format "Week 0 (1 Dec - 7 Dec)"
            Pattern pattern = Pattern.compile("\\((\\d{1,2}\\s[\\w]+)\\s*-\\s*(\\d{1,2}\\s[\\w]+)\\)");
            Matcher matcher = pattern.matcher(week_0_header);
            System.out.println("Week header: " + week_0_header);


            if (matcher.find()) {
                String startDateStr = matcher.group(1).trim();  // Example: "1 Dec"
                String endDateStr = matcher.group(2).trim();    // Example: "7 Dec"

                int currentYear = LocalDate.now().getYear(); // Add year to make it parsable
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.ENGLISH);

                LocalDate startDate = LocalDate.parse(startDateStr + " " + currentYear, formatter);
                LocalDate endDate = LocalDate.parse(endDateStr + " " + currentYear, formatter);

                return new LocalDate[] { startDate, endDate };
            } else {
                logger.error("No matching date pattern found in header: {}", week_0_header);
            }
        } catch (Exception e) {
            logger.error("Error parsing week header: {}", week_0_header, e);
        }

        // Return default dates in case of failure
        return new LocalDate[] { LocalDate.now(), LocalDate.now() };
    }

    private void saveToDatabase(List<AsinForecast_Import> invoiceList, String vendorId, String dropdown, String startDate, String endDate) {
		StringBuilder sb = new StringBuilder(
				"INSERT IGNORE INTO AsinForecast_import (vendorId, asin, productTitle, brand, week_0, week_1, week_2, week_3, week_4, "
						+ "`week_5`, `week_6`, `week_7`, `week_8`, `week_9`, `week_10`, `week_11`, `week_12`, `week_13`, `week_14`, `week_15`, "
						+ "`week_16`, `week_17`, `week_18`, `week_19`, `week_20`, `week_21`, `week_22`, `week_23`, `week_24`, `week_25`, "
						+ "statView, startDate, endDate, UniqueKey, createdDate) VALUES ");

		 for (AsinForecast_Import invoice : invoiceList) {
            sb.append("('").append(cleanValue(vendorId)).append("', '").append(cleanValue(invoice.getasin()))
            .append("', '").append(cleanValue(invoice.getproductTitle().replace("''", ""))).append("', '")
            .append(cleanValue(invoice.getbrand().replace("''", ""))).append("', '").append(cleanValue(invoice.getweek_0()))
            .append("', '").append(cleanValue(invoice.getweek_1())).append("', '")
            .append(cleanValue(invoice.getweek_2())).append("', '").append(cleanValue(invoice.getweek_3()))
            .append("', '").append(cleanValue(invoice.getweek_4())).append("', '")
            .append(cleanValue(invoice.getweek_5())).append("', '").append(cleanValue(invoice.getweek_6()))
            .append("', '").append(cleanValue(invoice.getweek_7())).append("', '")
            .append(cleanValue(invoice.getweek_8())).append("', '").append(cleanValue(invoice.getweek_9()))
            .append("', '").append(cleanValue(invoice.getweek_10())).append("', '")
            .append(cleanValue(invoice.getweek_11())).append("', '").append(cleanValue(invoice.getweek_12()))
            .append("', '").append(cleanValue(invoice.getweek_13())).append("', '")
            .append(cleanValue(invoice.getweek_14())).append("', '").append(cleanValue(invoice.getweek_15()))
            .append("', '").append(cleanValue(invoice.getweek_16())).append("', '")
            .append(cleanValue(invoice.getweek_17())).append("', '").append(cleanValue(invoice.getweek_18()))
            .append("', '").append(cleanValue(invoice.getweek_19())).append("', '")
            .append(cleanValue(invoice.getweek_20())).append("', '").append(cleanValue(invoice.getweek_21()))
            .append("', '").append(cleanValue(invoice.getweek_22())).append("', '")
            .append(cleanValue(invoice.getweek_23())).append("', '").append(cleanValue(invoice.getweek_24()))
            .append("', '").append(cleanValue(invoice.getweek_25())).append("', '").append(cleanValue(dropdown))
            .append("', '").append(startDate).append("', '").append(endDate).append("', '")
            .append(invoice.getUniqueKey()).append("', '").append(LocalDate.now())
            .append("'), ");
}

		String sql = sb.substring(0, sb.length() - 2); // Remove last comma
		jdbcTemplate.update(sql); // Execute the SQL query

	}

	private String cleanValue(String value) {
		return value == null ? "" : value.replace("'", "").replace(",", "");
	}

	private String getValidWeekValue(String value) {
		if (value == null || value.trim().isEmpty()) {
			return "0"; // Default value if null or empty
		}
		return value;
	}
	
	
	



}
