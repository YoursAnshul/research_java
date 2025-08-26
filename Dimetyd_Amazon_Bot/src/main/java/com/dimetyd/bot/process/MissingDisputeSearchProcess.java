package com.dimetyd.bot.process;

import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;

@Component
public class MissingDisputeSearchProcess {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CommonUtil commonobj;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public String InvoicedisputeProcess(Page page, String disputeInvoice, String vendorId, String vendorName, int currentIndex, String jobType) {
        logger.info("Current Batch Job Id : " + (currentIndex + 1) + " Dispute Invoice : " + disputeInvoice);

        Locator createDisputeBtn = page.locator("//button[@id='create-new-dispute-button-announce' and contains(text(), 'Create new dispute')]");
        if (!createDisputeBtn.isVisible()) {
            String URL = commonobj.getCountryUrl(vendorName);
            logger.info("Country URL : " + URL);
            page.navigate(URL + "/hz/vendor/members/disputes?ref_=vc_xx_favb");

            commonobj.killLoader(page);

            while (true) {
                page.waitForTimeout(1500);
                Locator helpBtn = page.locator("//*[@class='utility-bar-button-link']//span[text()='Help']");
                if (helpBtn.count() > 0) break;
                page.reload();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        while (true) {
            try {
                Locator disputeIdPage = page.locator("//option[@value='DISPUTE_SHORTAGE_INVOICES']");
                if (disputeIdPage.count() > 0) break;
                page.reload();
                Thread.sleep(2000);
            } catch (Exception e) {
                page.reload();
            }
        }

        boolean isMarketPlace = page.locator("//span[normalize-space(text())='Select marketplace']").nth(0).isVisible();
        if (isMarketPlace) {
            String marketPlace = vendorName.substring(0, 2);
            logger.info("Market Place: " + marketPlace);
            page.locator("//select[@id='default-search-marketplace']")
                .selectOption(new SelectOption().setValue(marketPlace));
        }

        String selectedValue;
        try {
            Locator selectDropdown = page.locator("select[name='searchCriterion']");
            selectedValue = selectDropdown.inputValue();
        } catch (Exception e) {
            selectedValue = "Not Selected";
        }
        commonobj.reminemepopup(page);

        // Choose search type based on jobType
        if (jobType.equalsIgnoreCase("disputesearch")) {
            page.locator("select[name='searchCriterion']")
                .selectOption(new SelectOption().setValue("DISPUTE_ID"));
            page.locator("input[name='disputeId']").fill(disputeInvoice);

        } else {
            if (!"DISPUTE_SHORTAGE_INVOICES".equals(selectedValue)) {
                page.locator("span")
                    .filter(new Locator.FilterOptions().setHasText("Dispute date range"))
                    .nth(3).click();
                page.getByLabel("Disputed shortage invoice(s)").click();
            }
            page.locator("#dispute-shortage-invoice").fill(disputeInvoice);
        }

        // Search
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
        page.waitForTimeout(5000);

        Locator disputeResults = page.locator("//form[@id='disputes-search-results-form']//td[contains(@id,'dispute-id')]");
        Locator noResultMessage = page.locator("//div[contains(@class,'a-box-inner') and contains(text(),'No results were found')]");

        try {
            page.waitForCondition(() ->
                disputeResults.count() > 0 || noResultMessage.count() > 0,
                new Page.WaitForConditionOptions().setTimeout(30000)
            );

            if (disputeResults.count() > 0) {
                System.out.println("Dispute results found.");
            } else {
                System.out.println("No results were found.");
            }
        } catch (PlaywrightException e) {
            System.out.println("Timeout waiting for dispute results or no-result message.");
        }

        Locator disputeResult = page.locator("//form[@id='disputes-search-results-form']//td[contains(@id,'dispute-id')]");
        int resultCount = disputeResult.count();

        if (resultCount > 0) {
            logger.info("Dispute(s) found with original invoice : " + disputeInvoice);
            handleMostRecentDispute(page, disputeInvoice, vendorId, jobType, vendorName);
        } else {
            logger.info("Dispute not found. Marking as COMPLETED in DB with comment 'no invoice found'");
            return "No results were found.";
        }
        return "Successfully processed with Invoice";
    }

    // UPDATED: This now always picks the most recent dispute by date
    private void handleMostRecentDispute(Page page, String disputeInvoice, String vendorId, String jobType, String vendorName) {
        try {
            Locator disputeIdLinks = page.locator("//td[contains(@id,'dispute-id-link')]");
            Locator disputeTypes = page.locator("//td[contains(@id,'dispute-type')]");
            Locator disputeStatuses = page.locator("//td[contains(@id,'dispute-status')]");
            Locator disputeDates = page.locator("//td[contains(@id,'dispute-creation-date')]");
            Locator disputedAmounts = page.locator("//td[contains(@id,'dispute-amount')]");
            Locator approvedAmounts = page.locator("//td[contains(@id,'approved-amount')]");
            Locator disputeReason = page.locator("//td[contains(@id,'dispute-reason')]");

            class DisputeRow {
                String id, type, status, date, disputedAmt, approvedAmt, getDisputeReason;
                DisputeRow(String id, String type, String status, String date, String disputedAmt, String approvedAmt, String getDisputeReason) {
                    this.id = id;
                    this.type = type;
                    this.status = status;
                    this.date = date;
                    this.disputedAmt = disputedAmt;
                    this.approvedAmt = approvedAmt;
                    this.getDisputeReason = getDisputeReason;
                }
            }

            List<DisputeRow> list = new ArrayList<>();
            for (int i = 0; i < disputeIdLinks.count(); i++) {
                String approvedAmtText = approvedAmounts.nth(i).innerText().trim().replace("$", "").replace(",", "");
                String approvedAmtParsed = approvedAmtText.isEmpty() ? "0" : approvedAmtText;

                list.add(new DisputeRow(
                    disputeIdLinks.nth(i).innerText().trim(),
                    disputeTypes.nth(i).innerText().trim(),
                    disputeStatuses.nth(i).innerText().trim(),
                    disputeDates.nth(i).innerText().trim(),
                    disputedAmounts.nth(i).innerText().trim().replaceAll("CAD|\\$AD|£|€|MXN|AED|\\$|ZŁ|PLN|KR|SEK|AUD|EUR|GBP|USD", "").replaceAll("\\s+", ""),
                    approvedAmtParsed,
                    disputeReason.nth(i).innerText().trim()
                ));
            }

            // Decide date format based on vendor
            List<SimpleDateFormat> dateFormats;
            if (vendorName.startsWith("US") || vendorName.startsWith("CA") || vendorName.startsWith("AU")) {
                dateFormats = Arrays.asList(new SimpleDateFormat("M/d/yyyy"), new SimpleDateFormat("MM/dd/yyyy"));
            } else {
                dateFormats = Arrays.asList(new SimpleDateFormat("d/M/yyyy"), new SimpleDateFormat("dd/MM/yyyy"));
            }

            // Sort by dispute date descending
            list.sort((a, b) -> {
                try {
                    Date dateA = parseFlexibleDate(a.date, dateFormats);
                    Date dateB = parseFlexibleDate(b.date, dateFormats);
                    return dateB.compareTo(dateA);
                } catch (Exception e) {
                    return 0;
                }
            });

            if (!list.isEmpty()) {
                // Pick latest by date
                DisputeRow latest = list.get(0);
                Date latestDate = parseFlexibleDate(latest.date, dateFormats);

                // Prefer "Shortage invoice" if same latest date exists
                for (DisputeRow row : list) {
                    Date rowDate = parseFlexibleDate(row.date, dateFormats);
                    if (rowDate.equals(latestDate) && row.type.equalsIgnoreCase("Shortage invoice")) {
                        latest = row;
                        break;
                    }
                }

                logger.info("Most Recent Dispute: " + latest.id + " || " + latest.date + " || " + latest.disputedAmt);

                String currency = commonobj.getCurrency(disputeInvoice);
                logger.info("currency: " + currency);

                Date parsedDate = parseFlexibleDate(latest.date, dateFormats);
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(parsedDate);

                String cleanedAmount = commonobj.processAmount(latest.disputedAmt.trim());

                String updateQuery;
                if (jobType.equalsIgnoreCase("missing")) {
                    updateQuery = String.format(
                        "UPDATE Missing_CBClientShortageInvoiceDispute " +
                        "SET createdDate = NOW(), status = 'COMPLETED', disputeId = '%s', disputeDate = '%s', disputeAmount = '%s', disputeType = '%s', disputeReason = '%s' " +
                        "WHERE vendorId = '%s' AND InvoiceNumber = '%s'",
                        latest.id, formattedDate, cleanedAmount, latest.type, latest.getDisputeReason, vendorId, disputeInvoice
                    );
                } else if (jobType.equalsIgnoreCase("disputesearch")) {
                    updateQuery = String.format(
                        "UPDATE DisputeSearch_CBClientShortageInvoiceDispute " +
                        "SET createdDate = NOW(), status = 'COMPLETED', comment = 'Dispute found', disputeId = '%s', disputeDate = '%s', disputeAmount = '%s', disputeType = '%s', disputeReason = '%s' " +
                        "WHERE vendorId = '%s' AND InvoiceNumber = '%s'",
                        latest.id, formattedDate, cleanedAmount, latest.type, latest.getDisputeReason, vendorId, disputeInvoice
                    );
                } else {
                    updateQuery = String.format(
                        "UPDATE Parent_CBClientShortageInvoiceDispute " +
                        "SET createdDate = NOW(), status = 'COMPLETED', disputeId = '%s', disputeDate = '%s', disputeAmount = '%s', disputeType = '%s', disputeReason = '%s' " +
                        "WHERE vendorId = '%s' AND InvoiceNumber = '%s'",
                        latest.id, formattedDate, cleanedAmount, latest.type, latest.getDisputeReason, vendorId, disputeInvoice
                    );
                }

                logger.info("Update Query: " + updateQuery);
                jdbcTemplate.execute(updateQuery);
            }
        } catch (Exception e) {
            logger.error("Failed to extract most recent dispute info", e);
        }
    }

    private Date parseFlexibleDate(String dateStr, List<SimpleDateFormat> formats) throws Exception {
        for (SimpleDateFormat format : formats) {
            try {
                return format.parse(dateStr);
            } catch (Exception ignored) {}
        }
        throw new Exception("Unrecognized date format: " + dateStr);
    }

    public String processAmount(String input) {
        StringBuilder result = new StringBuilder();
        boolean foundSpecialChar = false;
        for (int i = input.length() - 1; i >= 0; i--) {
            char currentChar = input.charAt(i);
            if ((currentChar == '.' || currentChar == ',') && !foundSpecialChar) {
                result.append('.');
                foundSpecialChar = true;
            } else if (currentChar == '-') {
                result.append(currentChar);
            } else if (Character.isDigit(currentChar)) {
                result.append(currentChar);
            }
        }
        return result.reverse().toString();
    }
}
