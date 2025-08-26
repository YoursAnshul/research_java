package com.dimetyd.bot.process;

import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

@Component
public class InvoiceDataUpdatePage {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;


	public String processPage(Page page, String invoiceNumber, String vendorName) {

		try {
			logger.info("invoiceNumber : " + invoiceNumber);
			String vName = vendorName.substring(0, 2);

			page.click("//div[@aria-label='Navigation menu']");
			try {
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
			} catch (Exception e) {
				page.reload();
				page.click("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
			}
			List<ElementHandle> subMenuList = page.querySelectorAll(".flyout-menu-item-label");

			for (ElementHandle subMenu : subMenuList) {
				// Retrieve HTML content
				String htmlContent = subMenu.innerHTML();

				if ("Invoices".equals(htmlContent)) {
					// Click the element
					subMenu.click();
					System.out.println("Clicked element with Playwright");
					break; // Exit the loop after clicking
				}
			}
			reFreshPage(page);
			reFreshPage(page);
			Locator invoiceNumberInput = page.locator("#invoiceNumberPOInput");
			invoiceNumberInput.fill(invoiceNumber.trim().replaceAll(" ", ""));
			page.click("#invoiceNumberPOSearchButton");
			while(true) {
				Locator invoicesText=page.locator("//h1[contains(text(),'Invoices')]");
				if(invoicesText.count()>0)
					break;
				else {
					page.reload();
					Thread.sleep(1500);
				}
			}
			String invoiceCreationDate = null;
			String invoiceDate = null;
			String paymentDueDate = null;
			String invoiceAmount = null;

			try {
			Locator dataNotFound=	page.locator(
						"//div[@id='advancedsearchresponsemelodictable']//div[@class='a-box a-spacing-large a-spacing-top-large']//div[@class='a-box-inner']");
				if(dataNotFound.count()>0){
					return "NULL";
				}
				
			} catch (Exception e) {

			}

			try {
				invoiceCreationDate = page.locator("#r0-CREATION_DATE").innerText().trim();
			} catch (Exception e) {
				logger.error("In catch block of invoiceCreationDate");
				Thread.sleep(5000);
				invoiceCreationDate = page.locator("#r0-CREATION_DATE").innerText().trim();
			}
			try {
				invoiceDate = page.locator("#r0-INVOICE_DATE").innerText().trim();
			} catch (Exception e) {
				logger.error("In catch block of invoiceDate");
			}
			try {
				paymentDueDate = page.locator("#r0-DUE_DATE").innerText().trim();
			} catch (Exception e) {
				logger.error("In catch block of paymentDueDate");
			}
			try {
				invoiceAmount = page.locator("#r0-INVOICE_AMOUNT").innerText();
				if (invoiceAmount.contains("€") || invoiceAmount.contains("EUR")) {
					// invoiceAmount = invoiceAmount.replace(",", ".");
					invoiceAmount = CommonUtil.replaceCurrency(invoiceAmount);
					String[] invoiceAmountArray = invoiceAmount.split(",");
					invoiceAmountArray[0] = invoiceAmountArray[0].replace(" ", "").replace(".", "");
					invoiceAmount = "";
					invoiceAmount = invoiceAmountArray[0] + "." + invoiceAmountArray[1];
				} else {
					invoiceAmount = CommonUtil.replaceCurrency(invoiceAmount);
					invoiceAmount = invoiceAmount.replace(",", "");
				}
			} catch (Exception e) {
				logger.error("In catch block of invoiceAmount");
			}

			if (vName.startsWith("CA") || vName.startsWith("US")) {
				logger.info("Query : update CBMissingInvoiceOutput  set invoiceCreatedDate = STR_TO_DATE('"
						+ invoiceCreationDate + "','%m/%d/%Y'), invoiceDate = STR_TO_DATE('" + invoiceDate
						+ "','%m/%d/%Y'), paymentDueDate= STR_TO_DATE('" + paymentDueDate
						+ "','%m/%d/%Y') , invoicedAmount = " + Double.parseDouble(invoiceAmount)
						+ " where invoiceNumber=" + invoiceNumber);
				jdbcTemplate.execute("update CBMissingInvoiceOutput  set invoiceCreatedDate = STR_TO_DATE('"
						+ invoiceCreationDate + "','%m/%d/%Y'), invoiceDate = STR_TO_DATE('" + invoiceDate
						+ "','%m/%d/%Y'), paymentDueDate= STR_TO_DATE('" + paymentDueDate
						+ "','%m/%d/%Y') , invoicedAmount = " + Double.parseDouble(invoiceAmount)
						+ " where invoiceNumber='" + invoiceNumber + "'");
			} else {
				logger.info("Query : update CBMissingInvoiceOutput  set invoiceCreatedDate = STR_TO_DATE('"
						+ invoiceCreationDate + "','%d/%m/%Y'), invoiceDate = STR_TO_DATE('" + invoiceDate
						+ "','%d/%m/%Y'), paymentDueDate= STR_TO_DATE('" + paymentDueDate
						+ "','%d/%m/%Y') , invoicedAmount = " + Double.parseDouble(invoiceAmount)
						+ " where invoiceNumber=" + invoiceNumber);
				jdbcTemplate.execute("update CBMissingInvoiceOutput  set invoiceCreatedDate = STR_TO_DATE('"
						+ invoiceCreationDate + "','%d/%m/%Y'), invoiceDate = STR_TO_DATE('" + invoiceDate
						+ "','%d/%m/%Y'), paymentDueDate= STR_TO_DATE('" + paymentDueDate
						+ "','%d/%m/%Y') , invoicedAmount = " + Double.parseDouble(invoiceAmount)
						+ " where invoiceNumber='" + invoiceNumber + "'");
			}

			return "true";
		} catch (Exception e) {
			e.printStackTrace();
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
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				logger.info("NoSuchElemenExcpn in a-box a-alert a-alert-error a-spacing-top-mini page ");
				locatorError = page.locator("mons-error-page-template");
				if (locatorError.count() > 0) {
					page.reload();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Locator pageLoaded = page.locator("#input-box, #invoiceNumberPOInput");
					if (pageLoaded.count() > 0) {
						logger.info("New UI Page properly Loaded");
						try {
							
							page.locator("//*[@label='Click here to return to the previous experience']")
						    .click(new Locator.ClickOptions().setTimeout(2000));
							break;
						} catch (PlaywrightException e) {
						
							try
							{
							page.locator("//*[text()='Click here']")
						    .click(new Locator.ClickOptions().setTimeout(2000));
							}
							catch(Exception ex)
							{
								logger.info("Click here not found....");
								ex.printStackTrace();
							}

						}

					} else {
						Locator previousUiPageLaoded = page.locator("text='View all Invoices'");
						if (previousUiPageLaoded.count() > 0) {
							logger.info("Previous UI Page properly Loaded");
							break;
						} else {
							page.reload();
							try {
								Thread.sleep(1500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}

				}
			}
		}
	}

}
