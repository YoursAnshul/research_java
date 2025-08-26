package com.dimetyd.bot.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class Invoice_DFinvoiceDetails {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
    public static final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat sqlDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public boolean processPage(Page page, String vendorId, String vendorName, String startDate, String endDate, Path downloadPath) {
        try {
            logger.info("VendorId : " + vendorId);
            logger.info("StartDate: " + startDate);
            logger.info("EndDate: " + endDate);

            File paymentsFile = new File(Paths.get(downloadPath.toString(), "dfinvoice.csv").toString());
            if (paymentsFile.exists()) {
                paymentsFile.delete();
            }

            page.click("//div[@aria-label='Navigation menu']");
            try {
                page.waitForSelector("//span[contains(text(), 'Payments')]", new Page.WaitForSelectorOptions().setTimeout(5000)).click();
            } catch (Exception e) {
                logger.warn("Initial click failed, reloading page...");
                page.reload();
                page.waitForSelector("//span[contains(text(), 'Payments')]", new Page.WaitForSelectorOptions().setTimeout(5000)).click();
            }

            Locator dfinvoice = page.locator("//span[text()='Direct Fulfillment Invoices']");
            if (dfinvoice.count() > 0) {
                dfinvoice.click();
            } else {
                return true;
            }

            page.waitForTimeout(1500);

            Locator countriesDropDown = page.locator("//kat-dropdown[@id='countriesDropDown']");
            if (countriesDropDown.count() > 0) {
                countriesDropDown.click();
                List<Locator> listOfCountries = page.locator("//kat-option[@role='option']").all();
                for (Locator country : listOfCountries) {
                    String countryName = country.innerText().replaceAll("[\\[\\](){}\\s]", "");
                    String cntry = countryName.substring(countryName.length() - 2);
                    logger.info("Country: " + cntry);
                    if (vendorName.substring(0, 2).equals(cntry)) {
                        country.click();
                        page.waitForTimeout(3000);
                        break;
                    }
                }
            }

            Date startDatePase = sqlDateFormatter.parse(startDate);
            Date endDatePase = sqlDateFormatter.parse(endDate);

            String startDateAsString = vendorName.startsWith("CA") || vendorName.startsWith("US")
                    ? formatterForUS.format(startDatePase)
                    : formatterForOthers.format(startDatePase);

            String endDateAsString = vendorName.startsWith("CA") || vendorName.startsWith("US")
                    ? formatterForUS.format(endDatePase)
                    : formatterForOthers.format(endDatePase);

            Locator fromDate = page.locator("//div[@id='date_filter_start']//input[@data-input='start']");
            fromDate.fill(startDateAsString);
            Locator toDate = page.locator("//div[@id='date_filter_end']//input[@data-input='start']");
            toDate.fill(endDateAsString);

            page.locator("//span[text()='All']").first().click();

            page.waitForSelector("//table[@class='a-bordered a-horizontal-stripes']", new Page.WaitForSelectorOptions().setTimeout(60000));

            // TRY DOWNLOAD
            Locator downloadSpan = page.locator("//span[text()='Download']").first();
            if (downloadSpan.count() > 0) {
                Download download = page.waitForDownload(() -> {
                    downloadSpan.click();
                    logger.info("Download clicked!");
                });

                Path savePath = Paths.get("C:\\Playwright File\\OPERATION_Files");
                if (!savePath.toFile().exists()) {
                    savePath.toFile().mkdirs();
                }
                Path filePath = savePath.resolve("dfinvoice.csv");
                download.saveAs(filePath);
                logger.info("Downloaded file saved at: " + filePath);

                insertInvoiceDataIntoDatabase(filePath.toString(), vendorId);
            } else {
                logger.warn("Download button not found. Falling back to scraping...");
                scrapeAndInsertFromPage(page, vendorId);
            }

            return true;

        } catch (Exception e) {
            logger.error("Error processing page: ", e);
            return false;
        }
    }

    private void insertInvoiceDataIntoDatabase(String filePath, String vendorId) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            Date now = new Date();

            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",");
                if (row.length < 11) continue;

                String orderId = row[0].trim();
                String invoiceId = row[1].trim();
                String shipmentDateStr = row[2].trim();
                String invoiceDateStr = row[3].trim();
                String asin = row[4].trim();
                String sku = row[5].trim();
                String itemTitle = row[6].trim();
                int quantity = Integer.parseInt(row[7].trim());
                String itemCost = row[8].trim().replace("$", "").replace("USD", "");
                String warehouseCode = row[9].trim();
                String status = row[10].trim();

                String formattedInvoiceDate = convertToSqlDateFormat(invoiceDateStr);
                String formattedShippedDate = convertToSqlDateFormat(shipmentDateStr);
                String createdDate = sqlDateFormatter.format(now);
                String uKey = vendorId + invoiceId + orderId;

                String sql = "INSERT IGNORE INTO DirectFullFillmntInvoices " +
                        "(`vendorId`, `status`, `invoiceId`, `orderId`, `shipmentDate`,`invoiceDate`, `ASIN`, `SKU`, `warehouseCode`, `quantity`, `itemCost`, `createdDate`, `itemTitle`, `uKey`) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

                jdbcTemplate.update(sql, vendorId, status, invoiceId, orderId, formattedShippedDate, formattedInvoiceDate,
                        asin, sku, warehouseCode, quantity, itemCost, createdDate, itemTitle, uKey);

                logger.info("Inserted Order ID: " + orderId);
            }
        } catch (IOException e) {
            logger.error("Error reading invoice file: ", e);
        }
    }

    private void scrapeAndInsertFromPage(Page page, String vendorId) {
        try {
            boolean hasNextPage = true;

            while (hasNextPage) {
                page.waitForSelector("//table[@class='a-bordered a-horizontal-stripes']//tr[position()>1]",
                        new Page.WaitForSelectorOptions().setTimeout(10000));

                List<Locator> rows = page.locator("//table[@class='a-bordered a-horizontal-stripes']//tr[position()>1]").all();
                for (Locator row : rows) {
                    List<Locator> cols = row.locator("td").all();
                    if (cols.size() < 6) continue;

                    String status = cols.get(0).innerText().trim();
                    String invoiceId = cols.get(1).innerText().trim();
                    String invoiceDateRaw = cols.get(2).innerText().trim();
                    String warehouseCode = cols.get(3).innerText().trim();
                    String quantityStr = cols.get(4).innerText().trim();
                    String amountStr = cols.get(5).innerText().trim().replace("$", "").replace(",", "");

                    String invoiceDate = convertInvoiceDate(invoiceDateRaw);
                    int quantity = Integer.parseInt(quantityStr);
                    double amount = Double.parseDouble(amountStr);
                    String createdDate = sqlDateFormatter.format(new Date());
                    String uKey = vendorId + invoiceId;

                    String sql = "INSERT IGNORE INTO DirectFullFillmntInvoices " +
                            "(`vendorId`, `status`, `invoiceId`, `invoiceDate`, `warehouseCode`, `quantity`, `itemCost`, `createdDate`, `uKey`) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    jdbcTemplate.update(sql, vendorId, status, invoiceId, invoiceDate,
                            warehouseCode, quantity, amount, createdDate, uKey);

                    logger.info("Inserted Invoice ID: " + invoiceId);
                }

                Locator nextBtn = page.locator("//li[@class='a-last']/a");
                if (nextBtn.count() > 0 && nextBtn.isVisible()) {
                    nextBtn.click();
                    page.waitForTimeout(2000);
                } else {
                    hasNextPage = false;
                }
            }

        } catch (Exception e) {
            logger.error("Error scraping data from table: ", e);
        }
    }

    private String convertToSqlDateFormat(String invoiceDateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MMM dd yyyy hh:mm:ss a z");
            Date date = inputFormat.parse(invoiceDateStr);
            return sqlDateFormatter.format(date);
        } catch (ParseException e) {
            logger.error("Error parsing CSV date: " + invoiceDateStr, e);
            return null;
        }
    }

    private String convertInvoiceDate(String rawDate) {
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            Date date = sdfIn.parse(rawDate);
            return sqlDateFormatter.format(date);
        } catch (ParseException e) {
            logger.error("Failed to parse scraped date: " + rawDate, e);
            return null;
        }
    }
}
