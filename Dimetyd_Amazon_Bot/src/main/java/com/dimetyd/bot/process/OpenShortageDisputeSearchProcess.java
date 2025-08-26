package com.dimetyd.bot.process;

import java.sql.Timestamp;
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
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;

@Component
public class OpenShortageDisputeSearchProcess {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	CommonUtil commonobj;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public void InvoicedisputeProcess(Page page, String disputeInvoice, String vendorId, String vendorName,
			int CurrentIndex) {

		
		
		logger.info("Current Batch Job Id : "+(CurrentIndex+1)+" Dispute Invoice : "+disputeInvoice);
		if (CurrentIndex == 0) {
			while (true) {
				try {
					Locator navigationMenu = page.getByLabel("Navigation menu");
					if (navigationMenu.count() > 0)
						break;
					else {
						page.reload();
						Thread.sleep(2000);
					}
				} catch (Exception e) {
					// Retry
				}
			}

			try {
				page.getByLabel("Navigation menu").click();
				Thread.sleep(1500);

				while (true) {
					Locator paymentOption = page
							.locator("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
					if (paymentOption.count() > 0) {
						logger.info("Payments Tab found");
						break;
					} else {
						logger.info("Payments Tab not found");
						page.reload();
						Thread.sleep(2000);
						try {
							page.getByLabel("Navigation menu").click();
						} catch (Exception e) {
							page.reload();
							Thread.sleep(2000);
						}
					}
				}

				try {
					page.getByText("Payments").click(new Locator.ClickOptions().setTimeout(6000));
				} catch (Exception e) {
					page.locator("//div[@class='side-nav-tab']/span[normalize-space(text())='Payments']")
							.click(new Locator.ClickOptions().setTimeout(6000));
				}

				try {
					page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute Management Remove"))
							.click(new Locator.ClickOptions().setTimeout(6000));
				} catch (Exception e) {
					logger.info("Dispute page error in catch");
					page.locator("//span[text()='Dispute Management']")
							.click(new Locator.ClickOptions().setTimeout(6000));
				}

				boolean loadingPage;
				do {
					Thread.sleep(1000);
					loadingPage = page
							.isVisible("//div[@class='melodic-loading-overlay' and @style='display: block;']");
				} while (loadingPage);

				while (true) {
					try {
						if (page.locator("//option[@value='DISPUTE_ID']").count() > 0)
							break;
						page.reload();
						Thread.sleep(2000);
					} catch (Exception e) {
						page.reload();
						Thread.sleep(2000);
					}
				}

				boolean isMarketPlace = page.locator("//span[normalize-space(text())='Select marketplace']").nth(0)
						.isVisible();
				if (isMarketPlace) {
					String marketPlace = vendorName.substring(0, 2);
					Locator dropdown = page.locator("//select[@id='default-search-marketplace']");
					dropdown.selectOption(new SelectOption().setValue(marketPlace));
				}

				page.locator("span").filter(new Locator.FilterOptions().setHasText("Dispute date range")).nth(3)
						.click();
				page.getByLabel("Disputed shortage invoice(s)").getByText("Disputed shortage invoice(s)").click();

				page.locator("#dispute-shortage-invoice").click();
			} catch (Exception ex) {
				ex.printStackTrace();

			}

		}

		
		page.locator("#dispute-shortage-invoice").fill(disputeInvoice);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
		page.waitForTimeout(2000);

		Locator disputeResult = page
				.locator("//form[@id='disputes-search-results-form']//td[contains(@id,'dispute-id')]");
		int resultCount = disputeResult.count();

		if (resultCount > 0) {
			logger.info("Dispute(s) found with original invoice: " + disputeInvoice);
			handleMostRecentDispute(page, disputeInvoice, vendorId, false, null);
		} else {
			logger.info("Dispute not found. Trying fallback options...");

			String modifiedInvoice = disputeInvoice;
			boolean disputeFound = false;

			if (disputeInvoice.endsWith("SC-")) {
				modifiedInvoice = disputeInvoice.replace("SC-", "SCR");
				disputeFound = retrySearchWithNewInvoice(modifiedInvoice, page);
			}

			if (!disputeFound && disputeInvoice.endsWith("SC")) {
				modifiedInvoice = disputeInvoice + "R";
				disputeFound = retrySearchWithNewInvoice(modifiedInvoice, page);
			}

			if (!disputeFound) {
				for (int i = 1; i <= 5; i++) {
					modifiedInvoice = modifiedInvoice + "SCR";
					disputeFound = retrySearchWithNewInvoice(modifiedInvoice, page);
					if (disputeFound)
						break;
				}
			}

			if (disputeFound) {
				handleMostRecentDispute(page, disputeInvoice, vendorId, true, modifiedInvoice);
			} else {
				String sql = "update CBClientShortageInvoiceDispute set `createdDate`=NOW(), status='COMPLETED' where vendorId='"
						+ vendorId + "' and InvoiceNumber='" + disputeInvoice + "'";
				jdbcTemplate.execute(sql);
			}
		}

	}

	private boolean retrySearchWithNewInvoice(String invoiceNumber, Page page) {
		try {
			page.locator("#dispute-shortage-invoice").fill(invoiceNumber);
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
			Thread.sleep(3000);

			Locator disputeResult = page
					.locator("//form[@id='disputes-search-results-form']//td[contains(@id,'dispute-id')]");
			return disputeResult.count() > 0;
		} catch (Exception e) {
			logger.error("Error while retrying search with invoice: " + invoiceNumber, e);
			return false;
		}
	}

	private void handleMostRecentDispute(Page page, String disputeInvoice, String vendorId, boolean isFallback,
			String modifiedInvoice) {
		try {
			Locator disputeIdLinks = page.locator("//td[contains(@id,'dispute-id-link')]");
			Locator disputeTypes = page.locator("//td[contains(@id,'dispute-type')]");
			Locator disputeStatuses = page.locator("//td[contains(@id,'dispute-status')]");
			Locator disputeDates = page.locator("//td[contains(@id,'dispute-creation-date')]");

			class DisputeRow {
				String id, type, status, date;

				DisputeRow(String id, String type, String status, String date) {
					this.id = id;
					this.type = type;
					this.status = status;
					this.date = date;
				}
			}

			List<DisputeRow> list = new ArrayList<>();

			for (int i = 0; i < disputeIdLinks.count(); i++) {
				String id = disputeIdLinks.nth(i).innerText().trim();
				String type = disputeTypes.nth(i).innerText().trim();
				String status = disputeStatuses.nth(i).innerText().trim();
				String date = disputeDates.nth(i).innerText().trim();
				list.add(new DisputeRow(id, type, status, date));
			}

			List<SimpleDateFormat> dateFormats = Arrays.asList(new SimpleDateFormat("d/M/yyyy"),
					new SimpleDateFormat("dd/M/yyyy"), new SimpleDateFormat("d/MM/yyyy"),
					new SimpleDateFormat("dd/MM/yyyy"));

			list.sort((a, b) -> {
				try {
					Date dateA = parseFlexibleDate(a.date, dateFormats);
					Date dateB = parseFlexibleDate(b.date, dateFormats);
					return dateB.compareTo(dateA); // Descending
				} catch (Exception e) {
					return 0;
				}
			});

			if (!list.isEmpty()) {
				DisputeRow latest = list.get(0);
				String sql = "update CBClientShortageInvoiceDispute set `createdDate`=NOW(), `requestType`='"
						+ latest.type + "', `comment`='FOUND', `disputeId`='" + latest.id + "', `disputeStatus`='"
						+ latest.status + "', status='COMPLETED'";

				if (isFallback) {
					sql += ", new_disputeInvoice='" + modifiedInvoice + "'";
				}

				sql += " where vendorId='" + vendorId + "' and InvoiceNumber='" + disputeInvoice + "'";
				jdbcTemplate.execute(sql);
			}

		} catch (Exception e) {
			logger.error("Failed to extract most recent dispute info", e);
		}
	}

	private Date parseFlexibleDate(String dateStr, List<SimpleDateFormat> formats) throws Exception {
		for (SimpleDateFormat format : formats) {
			try {
				return format.parse(dateStr);
			} catch (Exception ignored) {
			}
		}
		throw new Exception("Unrecognized date format: " + dateStr);
	}
}
