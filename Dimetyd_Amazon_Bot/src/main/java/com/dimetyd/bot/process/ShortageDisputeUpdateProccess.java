package com.dimetyd.bot.process;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.Dispute;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

@Component
public class ShortageDisputeUpdateProccess {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	Shortage_Dsipute_Update_ReadExcelFile readExcel;

	@Autowired
	CommonUtil commonobj;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String resolvedDate;

	public void processPage(Page page, String disputeId, String vendorId, String vendorName, String currency,
			String reason, String isLookbackOrReRun, double disputeAmount, int clientId)

	{

		while (true) {
			try {
				Locator navigationMenu = page.getByLabel("Navigation menu");
				if (navigationMenu.count() > 0) {
					break;
				} else {
					page.reload();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			} catch (Exception e) {

			}
		}

		try {
			Path filePath;
			page.getByLabel("Navigation menu").click();
			Thread.sleep(1500);
			;
			while (true) {
				Locator paymentOption = page
						.locator("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
				if (paymentOption.count() > 0) {
					logger.info("payments Tab found");
					break;
				} else {
					logger.info("payments Tab not found");
					page.reload();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						page.getByLabel("Navigation menu").click();
						logger.info("click done again on navigation tab");
					} catch (Exception e) {
						logger.info("click not done again on navigation tab");
						page.reload();
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				}
			}

			try {
				page.getByText("Payments").click(new Locator.ClickOptions().setTimeout(6000));
			} catch (Exception e) {
				page.locator("//div[@class='side-nav-tab']/span[normalize-space(text())='Payments']")
						.click(new Locator.ClickOptions().setTimeout(6000));

			}

			// TODO Auto-generated catch block
			// e.printStackTrace();

			// page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute
			// Management Remove")).click();
			try {

				System.out.println(
						page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute Management Remove"))
								.count());
				page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute Management Remove"))
						.click(new Locator.ClickOptions().setTimeout(6000));
			} catch (Exception e) {

				logger.info("Dispute page error in catach");
				logger.info(e.getMessage());
				page.locator("//span[text()='Dispute Management']").click(new Locator.ClickOptions().setTimeout(6000));
			}

			// Locator loader = page.locator(".a-popover-loading").first();
			// loader.waitFor(new
			// Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
			page.waitForTimeout(4000);
			page.evaluate("() => {" + "const loader = document.querySelector('.melodic-loading-overlay');"
					+ "if (loader) loader.remove();" + "}");
			logger.info("POP UP END");

			while (true) {
				try {
					Locator disputeIdPage = page.locator("//option[@value='DISPUTE_ID']");
					if (disputeIdPage.count() > 0) {
						break;
					} else {
						page.reload();
						Thread.sleep(2000);
					}
				} catch (Exception e) {
					page.reload();
					Thread.sleep(2000);
				}
			}
			boolean isMarketPlace = page.locator("//span[normalize-space(text())='Select marketplace']").nth(0)
					.isVisible();

			if (isMarketPlace)

			{
				String marketPlace = vendorName.substring(0, 2);
				logger.info("Market Place: " + marketPlace);

				Locator dropdown = page.locator("//select[@id='default-search-marketplace']");

				dropdown.selectOption(new SelectOption().setValue(marketPlace));

			}

			page.locator("span").filter(new Locator.FilterOptions().setHasText("Dispute date range")).nth(3).click();
			page.getByLabel("Dispute ID").getByText("Dispute ID").click();
			page.locator("#dispute-id").click();
			page.locator("#dispute-id").fill(disputeId);
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();

			while (true) {
				page.waitForTimeout(1500);
				Locator selectAccountPageLoaded = page
						.locator("//*[@class='utility-bar-button-link']//span[text()='Help']");
				if (selectAccountPageLoaded.count() > 0) {
					break;
				} else {
					page.reload();
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			String disputeType = page.locator("//*[@id=\"r" + disputeId + "-dispute-type\"]").innerText();
			String disputeReason = page.locator("//*[@id=\"r" + disputeId + "-dispute-reason\"]").innerText();
			String disputeDate = page.locator("//*[@id=\"r" + disputeId + "-dispute-creation-date\"]").innerText();
			String disputeStatus = page.locator("//*[@id=\"r" + disputeId + "-dispute-status\"]").innerText();
			String disputedAmount = page.locator("//*[@id=\"r" + disputeId + "-dispute-amount\"]").innerText();
			String approvedAmount = page.locator("//*[@id=\"r" + disputeId + "-approved-amount\"]").innerText();

			logger.info("disputeType : " + disputeType + " disputeStatus: " + disputeStatus + " approvedAmount "
					+ approvedAmount);

			logger.info("Insert Query Running");
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * FROM `CBDispute`  WHERE  disputeId = '" + disputeId + "'");

			List<Dispute> jobList = this.jdbcTemplate.query(sql.toString(), new RowMapper<Dispute>() {
				@Override
				public Dispute mapRow(ResultSet rs, int rowNum) throws SQLException {

					Dispute cd = new Dispute();
					return cd;
				}

			}, new Object[] {});

			logger.info("job List Size: " + jobList.size());

			if (jobList.size() > 0) {

				jdbcTemplate.execute("DELETE FROM `CBDispute`  WHERE  disputeId = '" + disputeId + "'");

			}

			if (disputeStatus.equals("Pending Amazon action")) {
				approvedAmount = "0";
			}

			// String disputedAmountDouble = commonobj.processAmount(disputedAmount);
			// String approvedAmountDouble = commonobj.processAmount(approvedAmount);
			String disputedAmountString = commonobj.processAmount(disputedAmount);
			double disputedAmountDouble = (disputedAmountString == null || disputedAmountString.trim().isEmpty()) ? 0.0
					: Double.parseDouble(disputedAmountString);

			String approvedAmountString = commonobj.processAmount(approvedAmount);
			double approvedAmountDouble = (approvedAmountString == null || approvedAmountString.trim().isEmpty()) ? 0.0
					: Double.parseDouble(approvedAmountString);

			if (vendorName.contains("US -") || vendorName.contains("CA -")) {

				logger.info("approvedAmount: " + approvedAmountDouble);

				sql = new StringBuilder();
				sql.append(
						"INSERT IGNORE INTO `CBDispute` (`disputeId`,`vendorId`,`disputeType`,`disputeReason`,`disputeDate`,`disputeStatus`,`totalDisputedAmount`,`approvedAmount`)\r\n"
								+ "VALUES ('" + disputeId + "','" + vendorId + "','" + disputeType + "','"
								+ disputeReason + "',STR_TO_DATE('" + disputeDate + "','%m/%d/%Y'),'" + disputeStatus
								+ "','" + disputedAmountDouble + "','" + approvedAmountDouble + "'); ");
			} else {

				logger.info("approvedAmount: " + approvedAmountDouble);

				sql = new StringBuilder();
				sql.append(
						"INSERT IGNORE INTO `CBDispute` (`disputeId`,`vendorId`,`disputeType`,`disputeReason`,`disputeDate`,`disputeStatus`,`totalDisputedAmount`,`approvedAmount`)\r\n"
								+ "VALUES ('" + disputeId + "','" + vendorId + "','" + disputeType + "','"
								+ disputeReason + "',STR_TO_DATE('" + disputeDate + "','%d/%m/%Y'),'" + disputeStatus
								+ "','" + disputedAmountDouble + "','" + approvedAmountDouble + "'); ");

			}
			logger.info(sql.toString());

			jdbcTemplate.execute(sql.toString());

			if (disputeType.equalsIgnoreCase("Price claim")) {

			}

			else {
				if (disputeStatus.equalsIgnoreCase("Resolved")) {

					Page page2 = page.waitForPopup(() -> {
						page.getByText(disputeId).click();
					});

					// get Dispute resolved date
					resolvedDate = page2.locator("//*[@id='dispute-details']/div[2]/div/div/div[2]/div[2]/div/div[1]")
							.innerText();

					resolvedDate = resolvedDate.replace("DISPUTE RESOLUTION DATE", "");

					if (vendorName.contains("US -") || vendorName.contains("CA -")) {
						jdbcTemplate.execute("UPDATE `CBDispute` SET `resolvedDate` = STR_TO_DATE('" + disputeDate
								+ "','%m/%d/%Y') WHERE  disputeId = '" + disputeId + "';");

						jdbcTemplate.execute(
								"UPDATE `CBClientDispute` SET `requestStatus` = 'RESOLVED', `modifiedDate` = NOW() "
										+ "WHERE `disputeId` = '" + disputeId + "'");
					} else {
						jdbcTemplate.execute("UPDATE `CBDispute` SET `resolvedDate` =STR_TO_DATE('" + disputeDate
								+ "','%d/%m/%Y') WHERE  disputeId = '" + disputeId + "';");
						jdbcTemplate.execute(
								"UPDATE `CBClientDispute` SET `requestStatus` = 'RESOLVED', `modifiedDate` = NOW() "
										+ "WHERE `disputeId` = '" + disputeId + "'");

					}

					page2.locator("//ul/li[@id='dispute-items']/a[normalize-space(text())='Dispute details']").click();

					try {
						Download download = page2.waitForDownload(() -> {
							// Perform the action that initiates download

							Locator exportAsSpreadSheet = page2.getByText("Export as spreadsheet");
							if (exportAsSpreadSheet.count() > 0) {
								exportAsSpreadSheet.click();
							} else {
								page2.reload();
								try {
									Thread.sleep(2500);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							// TODO Auto-generated catch block
							// e.printStackTrace();

						});
						filePath = Paths.get("C:\\Playwright File\\", download.suggestedFilename());
						download.saveAs(filePath);
						page2.close();

					}

					catch (Exception e) {

						Download download = page2.waitForDownload(() -> {
							// Perform the action that initiates download

							Locator downloadBtn = page2.getByText("Download");
							if (downloadBtn.count() > 0) {
								downloadBtn.click();
							} else {
								page2.reload();
								try {
									Thread.sleep(2500);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							// TODO Auto-generated catch block
							// e.printStackTrace();

						});
						filePath = Paths.get("C:\\Playwright File\\", download.suggestedFilename());
						download.saveAs(filePath);
						page2.close();
						// TODO: handle exception
					}

					// Wait for the download process to complete and save the downloaded file
					// somewhere

					try {
						readExcel.readExcel(filePath, disputeId, vendorId);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					jdbcTemplate.execute("UPDATE `CBClientDispute` SET `requestStatus` = 'RESOLVED' "
							+ "WHERE disputeId = '" + disputeId + "' ");

				}

			}
		} catch (

		Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
