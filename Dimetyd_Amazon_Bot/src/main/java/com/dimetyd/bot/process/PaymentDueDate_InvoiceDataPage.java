package com.dimetyd.bot.process;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

@Component
public class PaymentDueDate_InvoiceDataPage {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String processPage(Page page, String invoiceNumber, String vendorName) {
        try {
            logger.info("invoiceNumber : " + invoiceNumber);
            String vName = vendorName.substring(0, 2);

            // Navigate to Payments -> Invoices
            page.click("//div[@aria-label='Navigation menu']");
            try {
                page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
            } catch (Exception e) {
                page.reload();
                page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
            }

            List<ElementHandle> subMenuList = page.querySelectorAll(".flyout-menu-item-label");
            for (ElementHandle subMenu : subMenuList) {
                String htmlContent = subMenu.innerHTML();
                if ("Invoices".equals(htmlContent)) {
                    subMenu.click();
                    break;
                }
            }

            reFreshPage(page);
            reFreshPage(page);

            // Search invoice
            Locator invoiceNumberInput = page.locator("#invoiceNumberPOInput");
            invoiceNumberInput.fill(invoiceNumber.trim().replaceAll(" ", ""));
            page.click("#invoiceNumberPOSearchButton");

            while (true) {
                Locator invoicesText = page.locator("//h1[contains(text(),'Invoices')]");
                if (invoicesText.count() > 0) break;
                else {
                    page.reload();
                    Thread.sleep(1500);
                }
            }

            String paymentDueDate = null;

            // Check if invoice not found
            try {
                Locator dataNotFound = page.locator(
                    "//div[@id='advancedsearchresponsemelodictable']//div[@class='a-box a-spacing-large a-spacing-top-large']//div[@class='a-box-inner']"
                );
                if (dataNotFound.count() > 0) {
                    return "NULL";
                }
            } catch (Exception ignored) {}

            // Get payment due date
            try {
                paymentDueDate = page.locator("#r0-DUE_DATE").innerText().trim();
            } catch (Exception e) {
                logger.error("In catch block of paymentDueDate");
            }

            // Update only paymentDueDate in DB
            if (paymentDueDate != null && !paymentDueDate.isEmpty()) {
                if (vName.startsWith("CA") || vName.startsWith("US")) {
                    logger.info("Query : UPDATE invoice_paymentDuedate SET paymentDueDate = STR_TO_DATE('"
                            + paymentDueDate + "','%m/%d/%Y') WHERE invoiceNumber = '" + invoiceNumber + "'");
                    jdbcTemplate.execute(
                        "UPDATE invoice_paymentDuedate " +
                        "SET paymentDueDate = STR_TO_DATE('" + paymentDueDate + "','%m/%d/%Y') " +
                        "WHERE invoiceNumber = '" + invoiceNumber + "'"
                    );
                } else {
                    logger.info("Query : UPDATE invoice_paymentDuedate SET paymentDueDate = STR_TO_DATE('"
                            + paymentDueDate + "','%d/%m/%Y') WHERE invoiceNumber = '" + invoiceNumber + "'");
                    jdbcTemplate.execute(
                        "UPDATE invoice_paymentDuedate " +
                        "SET paymentDueDate = STR_TO_DATE('" + paymentDueDate + "','%d/%m/%Y') " +
                        "WHERE invoiceNumber = '" + invoiceNumber + "'"
                    );
                }
                return "true";
            } else {
                logger.warn("No payment due date found for invoice: {}", invoiceNumber);
                return "false";
            }

        } catch (Exception e) {
            logger.error("Error in processPage", e);
            return "false";
        }
    }

    public void reFreshPage(Page page) {
        while (true) {
            Locator locatorError = page.locator("//div[@class='a-box a-alert a-alert-error a-spacing-top-mini']");
            if (locatorError.count() > 0) {
                page.reload();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {}
            } else {
                logger.info("NoSuchElemenExcpn in a-box a-alert a-alert-error a-spacing-top-mini page ");
                locatorError = page.locator("mons-error-page-template");
                if (locatorError.count() > 0) {
                    page.reload();
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ignored) {}
                } else {
                    Locator pageLoaded = page.locator("#input-box, #invoiceNumberPOInput");
                    if (pageLoaded.count() > 0) {
                        logger.info("New UI Page properly Loaded");
                        try {
                            page.locator("//*[@label='Click here to return to the previous experience']")
                                .click(new Locator.ClickOptions().setTimeout(2000));
                            break;
                        } catch (PlaywrightException e) {
                            try {
                                page.locator("//*[text()='Click here']")
                                    .click(new Locator.ClickOptions().setTimeout(2000));
                            } catch (Exception ex) {
                                logger.info("Click here not found....");
                            }
                        }
                    } else {
                        Locator previousUiPageLoaded = page.locator("text='View all Invoices'");
                        if (previousUiPageLoaded.count() > 0) {
                            logger.info("Previous UI Page properly Loaded");
                            break;
                        } else {
                            page.reload();
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException ignored) {}
                        }
                    }
                }
            }
        }
    }
}
