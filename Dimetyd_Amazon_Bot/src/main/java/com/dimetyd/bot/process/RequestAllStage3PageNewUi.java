package com.dimetyd.bot.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.PODetails;
import com.dimetyd.bot.model.ProcessStatus;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;

@Component
public class RequestAllStage3PageNewUi {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JdbcTemplate jdbcTemplate;

	static class PageWrapper {
		private Page page;

		public PageWrapper(Page page) {
			this.page = page;
		}

		public Page getPage() {
			return page;
		}

		public void setPage(Page page) {
			this.page = page;
		}
	}

	public ProcessStatus processPage(Page page, String Invoice, String vendorId, String vendorName, Path downloadPath,
			BrowserContext context,long id) throws InterruptedException {

		logger.info("NewUi");
		String invoiceDt = null;
		Download download = null;

		String vnderNameTrimed = vendorName.substring(0, 2);
		String vName = vendorName.substring(0, 2).toLowerCase();
		logger.info("PoInvoice :" + Invoice);
		logger.info("Current URL :" + page.url());
		Page initialPage = page;
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
		CommonUtil.deleteFileOnPath(downloadPath);
		try {

			Locator newUi = page.locator("//div[@class='a-alert-content']//button");// got to new UI click btn
			if (newUi.count() > 0) {
				newUi.click();
			}

			reFreshPage(page);

			page.waitForTimeout(1000);
			Locator invoiePageLoaded = page.locator("#invoices-label");
			if (invoiePageLoaded.count() == 0) {
				page.reload();
			}

			try {
				try {
					page.click("#input-box");
				} catch (Exception e) {
					try {
						Locator parentLocator = page.locator("#hmd2f-trigger-tab");

						Locator cancelButtonLocator = parentLocator.locator(".vc-footer-hmd-feedback-link");

						cancelButtonLocator.click();
					} catch (Exception e1) {

					}
					page.click("#input-box");
				}

				page.click("//kat-icon[@name='search']");// search btn

				reFreshPage(page);
				while (true) {

					Locator locator = page.locator("[class='submitted-invoice-input touched']");
					if (locator.count() > 0) {
						break;
					} else {
						logger.info("Data not loaded");
						Thread.sleep(1000);
					}

				}

				Locator inputPo = page
						.locator("kat-input[class='submitted-invoice-input touched'] input[part='input']");
				inputPo.fill(Invoice);
				inputPo.press("Enter");
				// page.click("//kat-icon[@name='search']");
				reFreshPage(page);

				Locator noResult = page.locator("//kat-box[@variant='azure']");
				if (noResult.count() > 0) {
					String noResultFound = noResult.innerText().trim();
					System.out.println("NoResult : " + noResultFound);
					if (noResultFound.equals("No results found!!!")) {
						Locator dropDown = page.locator(
								"//div[@class='search-dropdown']//kat-dropdown[@id='submitted-invoice-search-criteria']");
						String valueSelected = dropDown.getAttribute("value").trim();
						System.out.println("Selected value: " + valueSelected);
						if (valueSelected.equals("INVOICE_NUMBER")) {
							return new ProcessStatus(true, "");
						} else {
							dropDown.click();
							page.click("[value='INVOICE_NUMBER']");// select po number
							logger.info("PO : " + Invoice);
							page.click("//kat-icon[@name='search']");
						}

					}
				}
				page.waitForTimeout(4000);
				List<Locator> statusList = page.locator("//span[@class='gray-color right-align min-width-140']").all();

				String invoiceStatus = statusList.get(0).innerText().trim().replace(":", "");
				System.out.println("Status : " + invoiceStatus);
				if (invoiceStatus.equals("Incomplete")) {
					return new ProcessStatus(true, "");
					// break;
				}

				try {
				   
					page.waitForTimeout(1000);
					Locator shadowHost = page.locator("kat-link[class='link-class']");
					shadowHost.evaluate("element => element.scrollIntoView()");
					// Wait for the popup to appear, then click the link
					page.waitForPopup(new Page.WaitForPopupOptions().setPredicate(p -> p.context().pages().size() == 2),
							() -> {
								// Use the evaluate method to click the link
								shadowHost.hover();
								shadowHost.click(); // Directly click the link
							});

				} catch (Exception e) {
					try {
						Locator link = page.locator("kat-link[class='link-class'] .link");

						System.out.println("Waiting for link to be visible...");
						link.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

						page.waitForPopup(
								new Page.WaitForPopupOptions().setPredicate(p -> p.context().pages().size() == 2),
								() -> {
									if (link.isVisible()) {
										System.out.println("Link is visible, attempting to click...");
										page.waitForTimeout(1000);
										link.click();
									} else {
										System.out.println("Link not visible at the time of click");
									}
								});
					} catch (Exception e1) {// page.waitForTimeout(1000);

					}

				}

				PageWrapper pageWrapper = new PageWrapper(null);

				List<Page> pages = page.context().pages();
				for (Page tabs : pages) {
					tabs.waitForLoadState();
					System.out.println("Url : " + tabs.url());
					if (tabs.url().contains("invoiceNumber")) { // Adjust condition based on your needs
						pageWrapper.setPage(tabs);
						break;
					}
				}
				Page targetPage = pageWrapper.getPage();
				if (targetPage != null) {
					// Switch to the found page
					// System.out.println("Switching to the existing page with URL: " +
					// targetPage.url());
					targetPage.bringToFront(); // Bring the tab to the foreground

					// Perform operations on the target page
					targetPage.waitForLoadState();
					Thread.sleep(5000);
					Locator invoiceDtParent = targetPage.locator(
							"//kat-workflowtracker-step[@label='Invoice Date']//span[@class='secondary-step-label']");
					if (invoiceDtParent.count() > 0) {
						invoiceDt = invoiceDtParent.innerText();
					} else {
						invoiceDtParent = targetPage.locator(
								"//kat-workflowtracker-step[@label='Invoice date']//span[@class='secondary-step-label']");
						if (invoiceDtParent.count() > 0) {
							invoiceDt = invoiceDtParent.innerText();
						}
					}

					logger.info("invoiceDt : " + invoiceDt);
					Locator elementToScroll = targetPage.locator("#download-csv");
					try {
						elementToScroll.scrollIntoViewIfNeeded(); // Scroll into view if needed
					} catch (PlaywrightException e) {
						// Handle the case where the element is not found
						Locator alternativeElement = targetPage.locator("#download-csv-details");
						alternativeElement.scrollIntoViewIfNeeded(); // Scroll the alternative element into view
					}
					try {
						// Attempt to download using the primary button
						download = targetPage.waitForDownload(() -> targetPage.click("#download-csv"));
					} catch (PlaywrightException e) {
						// Fallback to the alternative button if the primary one fails
						download = targetPage.waitForDownload(() -> targetPage.click("#download-csv-details"));
					} catch (Exception e) {
						// Handle unexpected exceptions
						try {
							Locator parentLocator = targetPage.locator("#hmd2f-trigger-tab");
							Locator cancelButtonLocator = parentLocator.locator(".vc-footer-hmd-feedback-link");
							cancelButtonLocator.click();
						} catch (Exception e1) {
							e1.printStackTrace(); // Log or handle the secondary exception
						}
						// Retry download after handling unexpected exceptions
						try {
							download = targetPage.waitForDownload(() -> targetPage.click("#download-csv"));
						} catch (PlaywrightException e2) {
							download = targetPage.waitForDownload(() -> targetPage.click("#download-csv-details"));
						}
					}

					Locator closeIcon = targetPage.locator("[class='close']");
					closeIcon.click();

					targetPage.close();
					initialPage.bringToFront();
					System.out.println("Switched back to the original page");

					// Perform operations on the original page
					initialPage.waitForLoadState();
					System.out.println("Original page title: " + initialPage.title());
					initialPage.evaluate("console.log('Performing actions in the original tab')");
				} else {
					System.out.println("Target page not found.");
				}

			} catch (Exception e) {
				e.printStackTrace();
				return new ProcessStatus(false, "");

			}
			Path filePath = downloadPath.resolve("default_filename.csv");
			System.out.println("Downloading file to: " + filePath);
			download.saveAs(filePath);
			File tempFile = filePath.toFile();
			boolean fileExists = CommonUtil.waitForFile(tempFile, Duration.ofSeconds(25));

			if (fileExists) {
				System.out.println("File exists!");
			} else {
				System.out.println("File does not exist within the timeout period.");

			}
			Path directory = filePath.getParent();
			String outputCsvFileName = "output.csv";

			// Combine the directory with the output file name
			Path outputCsvFilePath = directory.resolve(outputCsvFileName);
			// Path to your CSV file
			processCsv(filePath, outputCsvFilePath);

			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(outputCsvFilePath.toFile()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			CSVParser parser = null;
			try {
				parser = CSVFormat.DEFAULT.withDelimiter(',').withHeader().parse(br);
			} catch (IOException e) {
				e.printStackTrace();
			}

			List<PODetails> CbPoDetailsList = new ArrayList<PODetails>();
			int index = 0;
			try {
				for (CSVRecord csvRecord : parser) {

					PODetails CbPoDetailsObj = new PODetails();
					CbPoDetailsObj.setInvoiceDate(invoiceDt);
					try {
						CbPoDetailsObj.setPo(csvRecord.get(0));
						logger.info("PO" + CbPoDetailsObj.getPo());
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}

					try {
						CbPoDetailsObj.setAsin(csvRecord.get("ASIN"));
						logger.info("ASIN" + CbPoDetailsObj.getAsin());
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						CbPoDetailsObj.setPoAsin(CbPoDetailsObj.getPo() + CbPoDetailsObj.getAsin());
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}

					try {
						CbPoDetailsObj.setQty(Integer.parseInt(csvRecord.get("Invoiced Quantity").replaceAll(",", "")));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}

					try {
						CbPoDetailsObj
								.setUnitCost(Double.parseDouble(csvRecord.get("Unit Cost").replaceAll("[^\\d.]", "")));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						CbPoDetailsObj.setAmountReceived(
								Double.parseDouble(csvRecord.get("Processed Amount").replaceAll("[^\\d.]", "")));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						CbPoDetailsObj.setCurrency(CommonUtil.getCurrency(csvRecord.get("Unit Cost")));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					try {
						CbPoDetailsObj.setQtyReceived(Integer.parseInt(csvRecord.get("Matched Quantity")));
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					String uniqueKey = vendorId + CbPoDetailsObj.getPoAsin() + Invoice + CbPoDetailsObj.getQty()
							+ CbPoDetailsObj.getUnitCost() + index;
					index = index + 1;
					CbPoDetailsObj.setUkey(uniqueKey);
					CbPoDetailsList.add(CbPoDetailsObj);
					StringBuilder sb = new StringBuilder(
							"INSERT IGNORE INTO CBPODetailsV2 (vendorId,PO,ASIN,POASIN,Qty,unitCost,freightTerm,POInvoice,currency,invoiceDate,Ukey,ASINReceived,QtyReceived,createdDate,amountReceived,invoicedUnitCost) VALUES");
					if (CbPoDetailsList.size() == 100) {
						for (int row1 = 0; row1 < CbPoDetailsList.size(); row1++) {

							if (vnderNameTrimed.startsWith("CA") || vnderNameTrimed.startsWith("US")) {
								sb.append("('" + vendorId + "','" + CbPoDetailsList.get(row1).getPo() + "','"
										+ CbPoDetailsList.get(row1).getAsin() + "','"
										+ CbPoDetailsList.get(row1).getPoAsin() + "','"
										+ CbPoDetailsList.get(row1).getQty() + "','"
										+ CbPoDetailsList.get(row1).getUnitCost() + "','"
										+ CbPoDetailsList.get(row1).getFreightTerm() + "','" + Invoice + "','"
										+ CbPoDetailsList.get(row1).getCurrency() + "',STR_TO_DATE('"
										+ CbPoDetailsList.get(row1).getInvoiceDate() + "','%m/%d/%Y'),'"
										+ CbPoDetailsList.get(row1).getUkey() + "','"
										+ CbPoDetailsList.get(row1).getAsinReceived() + "','"
										+ CbPoDetailsList.get(row1).getQtyReceived() + "',NOW(),'"
										+ CbPoDetailsList.get(row1).getAmountReceived() + "','"
										+ CbPoDetailsList.get(row1).getInvoicedUnitCost() + "')");
							} else {
								sb.append("('" + vendorId + "','" + CbPoDetailsList.get(row1).getPo() + "','"
										+ CbPoDetailsList.get(row1).getAsin() + "','"
										+ CbPoDetailsList.get(row1).getPoAsin() + "','"
										+ CbPoDetailsList.get(row1).getQty() + "','"
										+ CbPoDetailsList.get(row1).getUnitCost() + "','"
										+ CbPoDetailsList.get(row1).getFreightTerm() + "','" + Invoice + "','"
										+ CbPoDetailsList.get(row1).getCurrency() + "',STR_TO_DATE('"
										+ CbPoDetailsList.get(row1).getInvoiceDate() + "','%d/%m/%Y'),'"
										+ CbPoDetailsList.get(row1).getUkey() + "','"
										+ CbPoDetailsList.get(row1).getAsinReceived() + "','"
										+ CbPoDetailsList.get(row1).getQtyReceived() + "',NOW(),'"
										+ CbPoDetailsList.get(row1).getAmountReceived() + "','"
										+ CbPoDetailsList.get(row1).getInvoicedUnitCost() + "')");
							}
							sb.append(",");
						}
						String query = sb.substring(0, sb.length() - 1) + ";";
						logger.info("Query " + query);
						jdbcTemplate.execute(query);
					}

				}
				StringBuilder sb = new StringBuilder(
						"INSERT IGNORE INTO CBPODetailsV2 (vendorId,PO,ASIN,POASIN,Qty,unitCost,freightTerm,POInvoice,currency,invoiceDate,Ukey,ASINReceived,QtyReceived,createdDate,amountReceived,invoicedUnitCost) VALUES");
				for (int row1 = 0; row1 < CbPoDetailsList.size(); row1++) {

					if (vnderNameTrimed.startsWith("CA") || vnderNameTrimed.startsWith("US")) {
						sb.append("('" + vendorId + "','" + CbPoDetailsList.get(row1).getPo() + "','"
								+ CbPoDetailsList.get(row1).getAsin() + "','" + CbPoDetailsList.get(row1).getPoAsin()
								+ "','" + CbPoDetailsList.get(row1).getQty() + "','"
								+ CbPoDetailsList.get(row1).getUnitCost() + "','"
								+ CbPoDetailsList.get(row1).getFreightTerm() + "','" + Invoice + "','"
								+ CbPoDetailsList.get(row1).getCurrency() + "',STR_TO_DATE('"
								+ CbPoDetailsList.get(row1).getInvoiceDate() + "','%m/%d/%Y'),'"
								+ CbPoDetailsList.get(row1).getUkey() + "','"
								+ CbPoDetailsList.get(row1).getAsinReceived() + "','"
								+ CbPoDetailsList.get(row1).getQtyReceived() + "',NOW(),'"
								+ CbPoDetailsList.get(row1).getAmountReceived() + "','"
								+ CbPoDetailsList.get(row1).getInvoicedUnitCost() + "')");
					} else {
						sb.append("('" + vendorId + "','" + CbPoDetailsList.get(row1).getPo() + "','"
								+ CbPoDetailsList.get(row1).getAsin() + "','" + CbPoDetailsList.get(row1).getPoAsin()
								+ "','" + CbPoDetailsList.get(row1).getQty() + "','"
								+ CbPoDetailsList.get(row1).getUnitCost() + "','"
								+ CbPoDetailsList.get(row1).getFreightTerm() + "','" + Invoice + "','"
								+ CbPoDetailsList.get(row1).getCurrency() + "',STR_TO_DATE('"
								+ CbPoDetailsList.get(row1).getInvoiceDate() + "','%d/%m/%Y'),'"
								+ CbPoDetailsList.get(row1).getUkey() + "','"
								+ CbPoDetailsList.get(row1).getAsinReceived() + "','"
								+ CbPoDetailsList.get(row1).getQtyReceived() + "',NOW(),'"
								+ CbPoDetailsList.get(row1).getAmountReceived() + "','"
								+ CbPoDetailsList.get(row1).getInvoicedUnitCost() + "')");
					}
					sb.append(",");
				}
				String query = sb.substring(0, sb.length() - 1) + ";";
				logger.info("Query " + query);
				jdbcTemplate.execute(query);
			} catch (Exception e) {
				logger.info("update CBPOInvoiceDetailsV2 set comment='NO Invoice Data Found'  where id='"
						+ id + "'");
				jdbcTemplate.execute("update CBPOInvoiceDetailsV2 set comment='NO Invoice Data Found'  where id='"
						+ id + "'");
				e.printStackTrace();
			}
			tempFile.delete();
			parser.close();
			br.close();
			File file = new File(outputCsvFilePath.toString());

			if (file.exists()) {
				if (file.delete()) {
					logger.info("File deleted successfully.");
				} else {
					logger.info("Failed to delete the file.");
				}
			} else {
				logger.info("File does not exist.");
			}

			return new ProcessStatus(true, "");
		} catch (Exception e) {
			e.printStackTrace();
			return new ProcessStatus(false, "");
		}
	}

	public static void processCsv(Path tempFile, Path outputCsvFilePath) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(tempFile.toFile()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputCsvFilePath.toFile()));

			String line;

			while ((line = br.readLine()) != null) {
				// Split the CSV line by ", and remove leading/trailing quotes
				if (line.contains("PO#") && line.contains("ASIN")) {
					// Write the line directly to the output CSV file
					bw.write(line);
					bw.newLine();
				} else {
					String[] parts = line.split("\",\"");
					for (int i = 0; i < parts.length; i++) {
						parts[i] = "\"" + parts[i].replace("\"", "") + "\"";
						// Write the part to the output CSV file
						bw.write(parts[i]);
						// Add comma separator if it's not the last part
						if (i < parts.length - 1) {
							bw.write(",");
						}
					}
					bw.newLine();
					bw.flush();
				}
			}

			br.close();
			bw.close();

			System.out.println("CSV file parsed successfully at: " + outputCsvFilePath.toAbsolutePath());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reFreshPage(Page page) {
		while (true) {

			boolean isLoginPage=page.isVisible("//*[@id='ap_email']");
			if(isLoginPage)
			{
				logger.info("------------------- Login Page Found -------------------");
				break;
			}
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
					Locator pageLoaded = page.locator("#input-box");
					if (pageLoaded.count() > 0) {
						logger.info("Page properly Loaded");
						break;
					} else {
						Locator previousUiPageLaoded = page.locator("//span[text()='Available Actions' and @class='bold']");
						if (previousUiPageLaoded.count() > 0) {
							logger.info("Previous UI Page properly Loaded");
							try {
						
								page.click("//div[@class='a-alert-content']//button");// got to new UI click btn
							} catch (PlaywrightException e) {
								logger.info("New UI Click Button not Found");
								page.reload();
							}
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
