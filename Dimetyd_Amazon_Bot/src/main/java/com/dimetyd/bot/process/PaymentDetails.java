package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

@Component
public class PaymentDetails {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    CommonUtil Payment_CommonUtil;
    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat formatterForUS = new SimpleDateFormat("MM/dd/yyyy");
    public static final SimpleDateFormat formatterForOthers = new SimpleDateFormat("dd/MM/yyyy");

    public boolean processPage(Page page, String vendorId, String vendorName, String startDate, String endDate,
            Path downloadPath) {
        try {
            Download download;
            File tempFile = null;
            String InsertQuery = null;
            StringBuilder sb1 = null;
            String vName = vendorName.substring(0, 2);
            logger.info("VendorId : " + vendorId);

            logger.info("StrtDate " + startDate);
            logger.info("EndDate " + endDate);
            File Payments = new File(downloadPath + "Payments.xlsx");
            if (Payments.exists()) {
                Payments.delete();
            }

            page.click("//div[@aria-label='Navigation menu']");
            try {
                page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
            } catch (Exception e) {
                page.reload();
                page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
            }

            page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Remittance')]");
            try {
                Thread.sleep(1500);
                Locator countriesDropDown = page.locator("//kat-dropdown[@id='countriesDropDown']");
                if (countriesDropDown.count() > 0) {
                    countriesDropDown.click();
                    List<Locator> listofCountries = page.locator("//kat-option[@role='option']").all();
                    for (Locator country : listofCountries) {
                        logger.info("CountryName : " + country.innerText());
                        String countryName = country.innerText().replaceAll("[\\[\\](){}\\s]", "");
                        String cntry = countryName.substring(countryName.length() - 2);
                        logger.info("Country " + cntry);
                        if (vName.equals(cntry)) {
                            country.click();
                            Thread.sleep(3000);
                            break;
                        }
                    }
                }

            } catch (Exception e) {

            }

            String startDateAsString = null;
            String endDateAsString = null;
            Date startdateASDate = inputFormat.parse(startDate);
		    Date endDateAsDate = inputFormat.parse(endDate);
            if (vName.startsWith("CA") || vName.startsWith("US")) {
                startDateAsString = formatterForUS.format(startdateASDate);
                endDateAsString = formatterForUS.format(endDateAsDate);
            } else {
                startDateAsString = formatterForOthers.format(startdateASDate);
                endDateAsString = formatterForOthers.format(endDateAsDate);
            }

            Locator fromDateParent = page.locator("#from-date-wrap");
            Locator fromDate = fromDateParent.locator("#from-date");
            Locator toDateParent = page.locator("#to-date-wrap");
            Locator toDate = toDateParent.locator("#to-date");
            Locator serachButton = page.locator("#remittanceSearchForm-submit");
            try {
                fromDate.click();
            } catch (Exception e) {
                Payment_CommonUtil.closePopup(page);
                fromDate.click();
            }
            fromDate.fill(startDateAsString);
            Locator randomClick = null;
            try {
                randomClick = page.locator("#selected-remittance-count");
                randomClick.click();
            } catch (Exception e) {
                Payment_CommonUtil.closePopup(page);
                randomClick.click();
            }

            Thread.sleep(1000);
            toDate.fill(endDateAsString);
            try {
                randomClick = page.locator("#selected-remittance-count");
                randomClick.click();
            } catch (Exception e) {
                Payment_CommonUtil.closePopup(page);
                randomClick.click();
            }

            try {
                serachButton.click();
            } catch (Exception e) {
                Payment_CommonUtil.closePopup(page);
                serachButton.click();
            }
            Thread.sleep(3000);
            try {
                Locator selectAll = page.locator("#remittance-home-select-all");
                selectAll.click();
            } catch (PlaywrightException e) {
                logger.info("No data between Start Date and End Date");
                return true;
            } catch (Exception e1) {
                Payment_CommonUtil.closePopup(page);
                page.click("#remittance-home-select-all");

            }
            try {
                Locator exportAll = page.locator("#remittance-home-export-link");
                download = page.waitForDownload(() -> {
                    exportAll.click();
                });
            } catch (Exception e) {
                Payment_CommonUtil.closePopup(page);
                Locator exportAll = page.locator("#remittance-home-export-link");
                download = page.waitForDownload(() -> {
                    exportAll.click();
                });
            }

            try {
                Path filePath = downloadPath.resolve(download.suggestedFilename());
                System.out.println("Downloading file to: " + filePath);

                // Save the downloaded file to the specified path
                download.saveAs(filePath);
                tempFile = filePath.toFile();

                boolean fileExists = Payment_CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));

                if (fileExists) {
                    System.out.println("File exists!");
                } else {
                    System.out.println("File does not exist within the timeout period.");

                }
                FileInputStream file = new FileInputStream(tempFile);
                @SuppressWarnings("resource")
                XSSFWorkbook workbook = new XSSFWorkbook(file);
                XSSFSheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rows = sheet.iterator(); // iterating over excel file
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                int rowNumberForPaymentNumber = 0;
                int BlankCellCount = 0;
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    boolean paymentNumberRowFlag = false;
                    while (rows.hasNext() && paymentNumberRowFlag == false) {
                        Row currentRow = rows.next();
                        Iterator<Cell> cellIterator = currentRow.cellIterator();

                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();

                            if (cell == null || cell.getCellType() == CellType.BLANK) {
                                // Skip empty cells
                                BlankCellCount++;
                                if (BlankCellCount == 4) {
                                    break;
                                }
                                continue;
                            }
                            switch (cell.getCellType()) {
                            case STRING: // For string cell type
                                String cellValue = cell.getStringCellValue().trim();
                                if (cellValue.equals("Payment Number")) {
                                    paymentNumberRowFlag = true;
                                    rowNumberForPaymentNumber = currentRow.getRowNum();
                                }
                                break;

                            case NUMERIC: // For numeric cell type
                                System.out.println("Numeric Value: " + cell.getNumericCellValue());
                                break;

                            case BOOLEAN: // For boolean cell type
                                System.out.println("Boolean Value: " + cell.getBooleanCellValue());
                                break;

                            case ERROR: // For error cell type
                                System.out.println("Error Cell Value: " + cell.getErrorCellValue());
                                break;

                            default: // For other cell types (if any)
                                System.out.println("Unknown Cell Type");
                                break;
                            }
                        }
                    }
                }

                BlankCellCount = 0;
                HashMap<String, String> paymentDateMap = new HashMap<>();
                int remittanceDataEndRow = 0;
                sb1 = new StringBuilder(
                        "INSERT IGNORE INTO `PaymentDetails` (`uniqueKey`,`vendorId`,`paymentNumber`,`paymentDate`,`paymentAmount`,`paymentStatus`,`createdDate`) VALUES");
                int RowCount = 0;
                String paymentNumber = null;
                int consecutiveEmptyCount = 0;  // To track consecutive empty or null cells
                int rowCount = sheet.getPhysicalNumberOfRows();
                System.out.println(rowCount);
                for (int r = 4; r < rowCount; r++) {
                    // Read the paymentNumber column (column index 0)
                    Row currentRow = sheet.getRow(r);
                    if (currentRow != null) {  // Check if the row is not null
                        paymentNumber = currentRow.getCell(0) != null ? currentRow.getCell(0).getStringCellValue() : null;
                        if (paymentNumber == null || paymentNumber.trim().isEmpty()) {
                            consecutiveEmptyCount++;
                        } else {
                            consecutiveEmptyCount = 0;  // Reset if data is found
                        }

                        if (consecutiveEmptyCount == 2) {
                            logger.info("Stopping processing due to two consecutive empty cells in paymentNumber column.");
                            break;  // Stop processing if two consecutive empty cells are encountered
                        }

                        // Read the paymentDate column (column index 1)
                        String paymentDate = currentRow.getCell(1) != null ? currentRow.getCell(1).getStringCellValue() : null;
                        paymentDate = paymentDate != null ? convertDateString(paymentDate, "MM/dd/yyyy", "yyyy-MM-dd") : null;

                        if (paymentDate == null || paymentDate.trim().isEmpty()) {
                            consecutiveEmptyCount++;
                        } else {
                            consecutiveEmptyCount = 0;
                        }

                        if (consecutiveEmptyCount == 2) {
                            logger.info("Stopping processing due to two consecutive empty cells in paymentDate column.");
                            break;  // Stop processing if two consecutive empty cells are encountered
                        }

                        // Read the paymentAmount column (column index 3)
                        String paymentAmount = currentRow.getCell(3) != null ? currentRow.getCell(3).getStringCellValue() : null;

                        if (paymentAmount == null || paymentAmount.trim().isEmpty()) {
                            consecutiveEmptyCount++;
                        } else {
                            consecutiveEmptyCount = 0;
                        }

                        if (consecutiveEmptyCount == 2) {
                            logger.info("Stopping processing due to two consecutive empty cells in paymentAmount column.");
                            break;  // Stop processing if two consecutive empty cells are encountered
                        }

                        // Read the paymentStatus column (column index 8)
                        String paymentStatus = currentRow.getCell(8) != null ? currentRow.getCell(8).getStringCellValue() : null;

                        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
                            consecutiveEmptyCount=consecutiveEmptyCount+1;
                        } else {
                            consecutiveEmptyCount = 0;
                        }

                        if (consecutiveEmptyCount == 2) {
                            logger.info("Stopping processing due to two consecutive empty cells in paymentStatus column.");
                            break;  // Stop processing if two consecutive empty cells are encountered
                        }

                        sb1.append("('" + paymentNumber + vendorId + "','" + vendorId + "','" + paymentNumber
                                + "','" + paymentDate + "','" + paymentAmount + "','" + paymentStatus
                                + "',NOW()),");

                        if (RowCount == 100) {
                            logger.info(sb1.toString());
                            InsertQuery = sb1.toString().substring(0, sb1.length() - 1);
                            logger.info("Executing Query: " + InsertQuery);
                            jdbcTemplate.execute(InsertQuery);
                            sb1 = new StringBuilder(
                                    "INSERT IGNORE INTO `PaymentDetails` (`uniqueKey`,`vendorId`,`paymentNumber`,`paymentDate`,`paymentAmount`,`paymentStatus`,`createdDate`) VALUES");
                            RowCount = 0;
                        } else {
                            logger.info("Row Count " + RowCount);
                            RowCount++;
                        }
                    } else {
                        logger.info("Skipping row " + r + " as it is null.");
                    }
                }
                logger.info("Last Batch Inserting...");
                logger.info(sb1.toString());
                InsertQuery = sb1.toString().substring(0, sb1.length() - 1);
                logger.info("Executing Query: " + InsertQuery);
                jdbcTemplate.execute(InsertQuery);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
      
        return true;
    }

    private String convertDateString(String dateString, String inputFormat, String outputFormat) {
        try {
            SimpleDateFormat inputFormatter = new SimpleDateFormat(inputFormat);
            Date date = inputFormatter.parse(dateString);
            SimpleDateFormat outputFormatter = new SimpleDateFormat(outputFormat);
            return outputFormatter.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
