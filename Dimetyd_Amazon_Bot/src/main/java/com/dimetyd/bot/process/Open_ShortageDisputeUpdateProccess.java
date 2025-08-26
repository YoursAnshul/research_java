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
public class Open_ShortageDisputeUpdateProccess {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	Open_Shortage_Dispute_Update_ReadExcelFile readExcel;

	@Autowired
	CommonUtil commonobj;

	@Autowired
	DisputeMergeProcess disputeMerge;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String resolvedDate;

	public void processPage(Page page, String disputeId, String vendorId, String vendorName, String currency,
			String reason, String isLookbackOrReRun, double disputeAmount, int clientId)

	{

		String invoiceNumber=null;
		
		Locator createDisputeBtn = page.locator(
				"//button[@id='create-new-dispute-button-announce' and contains(text(), 'Create new dispute')]");
		if (createDisputeBtn.isVisible()) {
			System.out.println("Create new dispute button is visible");
		} else {
			System.out.println("Create new dispute button is not visible");
			String URL = commonobj.getCountryUrl(vendorName);
			logger.info("Country URL : " + URL);
			page.navigate(URL + "/hz/vendor/members/disputes?ref_=vc_xx_favb");

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

		}

		// Navigate Page to

		try {
			Path filePath;

			commonobj.killLoader(page);

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

			String selectedValue;
			try {
				Locator selectDropdown = page.locator("select[name='searchCriterion']");
				selectedValue = selectDropdown.inputValue(); // Gets the selected <option>'s value

				System.out.println("Selected dropdown value: " + selectedValue);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				selectedValue = "Not Selcted";
			}

			if ("DISPUTE_ID".equals(selectedValue)) {
				System.out.println("Dispute ID is selected");
			} else {
				System.out.println("Selected value is: " + selectedValue);

				page.locator("span").filter(new Locator.FilterOptions().setHasText("Dispute date range")).nth(3)
						.click();
				page.getByLabel("Dispute ID").getByText("Dispute ID").click();
				page.locator("#dispute-id").click();
			}

			page.locator("#dispute-id").fill(disputeId);
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();

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
			sql.append("SELECT * FROM `CB_Open_Shortage_Dispute`  WHERE  disputeId = '" + disputeId + "'");

			List<Dispute> jobList = this.jdbcTemplate.query(sql.toString(), new RowMapper<Dispute>() {
				@Override
				public Dispute mapRow(ResultSet rs, int rowNum) throws SQLException {

					Dispute cd = new Dispute();
					return cd;
				}

			}, new Object[] {});

			logger.info("job List Size: " + jobList.size());

			if (jobList.size() > 0) {

				jdbcTemplate.execute("DELETE FROM `CB_Open_Shortage_Dispute`  WHERE  disputeId = '" + disputeId + "'");

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
						"INSERT IGNORE INTO `CB_Open_Shortage_Dispute` (`disputeId`,`vendorId`,`disputeType`,`disputeReason`,`disputeDate`,`disputeStatus`,`totalDisputedAmount`,`approvedAmount`)\r\n"
								+ "VALUES ('" + disputeId + "','" + vendorId + "','" + disputeType + "','"
								+ disputeReason + "',STR_TO_DATE('" + disputeDate + "','%m/%d/%Y'),'" + disputeStatus
								+ "','" + disputedAmountDouble + "','" + approvedAmountDouble + "'); ");
			} else {

				logger.info("approvedAmount: " + approvedAmountDouble);

				sql = new StringBuilder();
				sql.append(
						"INSERT IGNORE INTO `CB_Open_Shortage_Dispute` (`disputeId`,`vendorId`,`disputeType`,`disputeReason`,`disputeDate`,`disputeStatus`,`totalDisputedAmount`,`approvedAmount`)\r\n"
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

					/*
					 * Page page2 = page.waitForPopup(() -> { page.getByText(disputeId).click();
					 * 
					 * });
					 */

					Page page2 = page.waitForPopup(() -> {
						page.locator("a", new Page.LocatorOptions().setHasText(disputeId)).click();
					});
					// get Dispute resolved date
					resolvedDate = page2.locator("//*[@id='dispute-details']/div[2]/div/div/div[2]/div[2]/div/div[1]")
							.innerText();

					invoiceNumber = page2.locator("//div[contains(@class,'selected-invoice')]").innerText();

					
					resolvedDate = resolvedDate.replace("DISPUTE RESOLUTION DATE", "");

					
					String messageBody = disputeMerge.MailMessage(page2, vendorName);

					if (vendorName.contains("US -") || vendorName.contains("CA -")) {
						jdbcTemplate.execute("UPDATE `CB_Open_Shortage_Dispute` SET `resolvedDate` = STR_TO_DATE('"
								+ resolvedDate + "','%m/%d/%Y') , comment = '" + messageBody.replace("'", "''")
								+ "' WHERE  disputeId = '" + disputeId + "';");

					} else {
						jdbcTemplate.execute("UPDATE `CB_Open_Shortage_Dispute` SET `resolvedDate` =STR_TO_DATE('"
								+ resolvedDate + "','%d/%m/%Y') , comment = '" + messageBody.replace("'", "''")
								+ "' WHERE  disputeId = '" + disputeId + "';");

					}

					
					
					if ((approvedAmount == null || approvedAmount.trim().isEmpty())) {
						logger.info(
								"******************APPROVED AMOUNT IS NULL OR EMPTY FILE DOWNLOADING******************");
						try {
							
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
							
						
							
							

						}

						catch (Exception e) {
							Download download = page2.waitForDownload(() -> {
								// Perform the action that initiates download

								Locator exportAsSpreadSheet = page2.getByText("Export as spreadsheet");
								if (exportAsSpreadSheet.count() > 0) {
									exportAsSpreadSheet.click();
								} else {
									page2.reload();
									try {
										Thread.sleep(2500);
									} catch (InterruptedException ex) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								// TODO Auto-generated catch block
								// e.printStackTrace();

							});
							filePath = Paths.get("C:\\Playwright File\\", download.suggestedFilename());
							download.saveAs(filePath);
					
							
							
							// TODO: handle exception
						}

						// Wait for the download process to complete and save the downloaded file
						// somewhere

						try {
							readExcel.readExcel(filePath, disputeId, vendorId, "open_shortage", "Open_CBItemizedShortages");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					
				
		



					try
					{
					// PO NUMBER UPDATE BY INVOICE NUMBER
					logger.info("SHORTAGE PAYMENT NUMBER UPDATE --->STARTED");

					while (true) {
						try {
							Locator navigationMenu = page2.getByLabel("Navigation menu");
							if (navigationMenu.count() > 0) {
								break;
							} else {
								page2.reload();
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

					page2.getByLabel("Navigation menu").click();
					page2.waitForTimeout(1000);

					while (true) {
						Locator paymentOption = page2
								.locator("//span[@class='side-nav-tab-label' and contains(text(), 'Payments')]");
						if (paymentOption.count() > 0) {
							logger.info("payments Tab found");
							break;
						} else {
							logger.info("payments Tab not found");
							page2.reload();
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								page2.getByLabel("Navigation menu").click();
								logger.info("click done again on navigation tab");
							} catch (Exception e) {
								logger.info("click not done again on navigation tab");
								page2.reload();
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
						page2.getByText("Payments").click();
					} catch (Exception e) {
						page2.locator("//div[@class='side-nav-tab']/span[normalize-space(text())='Payments']").click();

						// TODO Auto-generated catch block
						// e.printStackTrace();
					}

				//	page2.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Remittance")).click();
					page2.locator("//div[@class='flyout-menu-item-nav-link']/span[text()='Remittance']").click();

				page2.locator("//span[@class='a-button-text a-declarative']").nth(0).click();
			

					List<Locator> liList = page2.locator("//ul[@role='listbox']//li[@role='option']").all();
					for (Locator li : liList) {
						if (li.innerText().trim().equalsIgnoreCase("Invoice Number")) {
							li.click();
							break;
						}
					}

					logger.info("Searching Invoice Number in Remmitance Tab : " + invoiceNumber);
					page2.locator("//input[@id='invoice-number']").fill(invoiceNumber);
					page2.waitForTimeout(1000);
					page2.click("//input[@id='remittanceSearchForm-submit']");

					String paymentStatus = null;
					String paymentNumber = null;
					String paymentDate=null;
					page2.waitForTimeout(1000);
					Locator isNoResultLocator = page2
							.locator("//td[contains(@id,'header-payment-checkbox')]");
					if (isNoResultLocator.count() == 0) {
						logger.info("No result Found");
						logger.info("Payment Number : NOT FOUND Invoice Number : " + invoiceNumber);
						
						
						logger.info("UPDATE CBClientDispute SET `paymentUniqueId` ='-',paymentReceivedDate='-' WHERE  disputeId = '" + disputeId + "'");
						jdbcTemplate.execute("UPDATE CBClientDispute  SET `paymentUniqueId` ='-',paymentReceivedDate='-' WHERE  disputeId = '" + disputeId + "'");
						
						
					} else {

						paymentStatus = page2.locator("//td[contains(@id,'payment-status')]").innerText();
						paymentDate = page2.locator("//td[contains(@id,'payment-date')]").innerText();

						logger.info("Payment Status : " + paymentStatus);

							paymentNumber = page2.locator("//span[contains(@id,'payment-number-input-wrap')]")
									.innerText();
							logger.info("Payment Number : " + paymentNumber + " Invoice Number : " + invoiceNumber);
							
							logger.info("UPDATE CBClientDispute SET `paymentUniqueId` ='" + paymentNumber
									+ "',paymentReceivedDate='" + paymentDate + "' WHERE  disputeId = '" + disputeId + "'");
							
							jdbcTemplate.execute("UPDATE CBClientDispute  SET `paymentUniqueId` ='" + paymentNumber
									+ "',paymentReceivedDate='" + paymentDate + "' WHERE  disputeId = '" + disputeId + "'");
							

				
					
					}

				
					logger.info("SHORTAGE PAYMENT NUMBER UPDATE ---> DONE");
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
					
					
					
				
					page2.close();
					logger.info("Dispute Details Page SECOND---> CLOSED");
					page.locator("//*[@id='sc-content-container']").nth(0).click();
					
					jdbcTemplate.execute("UPDATE `CB_Open_Shortage_Dispute` os JOIN `Open_CBItemizedShortages` iso \r\n"
							+ "ON(os.disputeId=iso.disputeId AND os.vendorId=iso.vendorId) \r\n"
							+ "SET os.`approvedAmount`=iso.`resolvedAmount`\r\n"
							+ "WHERE os.disputeId='"+disputeId+"' AND (os.approvedAmount IS NULL OR  os.approvedAmount='0.0')");
					
					jdbcTemplate.execute(
							"UPDATE `CBClientDispute` SET `requestStatus` = 'RESOLVED', `modifiedDate` = NOW() "
									+ "WHERE `disputeId` = '" + disputeId + "'");
				}

			}
		} catch (

		Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
