package com.dimetyd.bot.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.POInvoiceDetails;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

@Component
public class MissingShortageInvoiceDetails {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;


	public boolean processPage(Page page, String parentInvoices, String vendorId,
			String vendorName, Path downloadPath) {

		try {
			Download download;
			String vName = vendorName.substring(0, 2);
			Thread.sleep(2000);

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

			try {
				Locator newUi = page.locator("//div[@class='a-alert-content']//button");// got to new UI click btn
				if (newUi.count() > 0) {
					newUi.click();
				}
				reFreshPage(page);
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
					inputPo.fill(parentInvoices);
					inputPo.press("Enter");
					 page.click("//kat-icon[@name='search']");

					List<Locator> pageLoaded = page.locator("//kat-data-table[@class='kat-data-table']").all();
					int counter = 0;
					
					
					while(pageLoaded.size()== 0 && counter <100) {
						reFreshPage(page);
						Thread.sleep(3000);
						inputPo.fill(parentInvoices);
						inputPo.press("Enter");
						 page.click("//kat-icon[@name='search']");
							Thread.sleep(2000);
						pageLoaded = page.locator("//kat-data-table[@class='kat-data-table']").all();
						counter = counter +1;
					}
					reFreshPage(page);

					
					Locator elementToScroll = page.locator("#download-csv");
					try {
						elementToScroll.scrollIntoViewIfNeeded(); // Scroll into view if needed
					} catch (PlaywrightException e) {
						// Handle the case where the element is not found
						Locator alternativeElement = page.locator("#download-csv-details");
						alternativeElement.scrollIntoViewIfNeeded(); // Scroll the alternative element into view
					}
					Thread.sleep(1500);

					download = page.waitForDownload(() -> {
						try {
							page.click("#download-csv");// download Btn
						} catch (PlaywrightException e) {
							page.click("#download-csv-details");
						} catch (Exception e) {
							try {
								Locator parentLocator = page.locator("#hmd2f-trigger-tab");
								Locator cancelButtonLocator = parentLocator.locator(".vc-footer-hmd-feedback-link");
								cancelButtonLocator.click();
							} catch (Exception e1) {

							}
							try {
								page.click("#download-csv");// download Btn

							} catch (PlaywrightException e1) {
								page.click("#download-csv-details");
							}
						}
						try {
							// Locate the error message element and get its text
							Locator errorElement = page.locator("//div[@id='download-csv']//p[@class='left-align']");
							String errorText = errorElement.innerText();

							// Check if the error message matches the expected text
							if (errorText.equalsIgnoreCase("Error Occurred. Please Try Again Later.")) {
								// Locate the close icon inside the shadow DOM
								page.click("[class='close']");
								Locator searchIcon = page.locator("//kat-icon[@name='search']");
								searchIcon.click();
								Locator downloadBtn = page.locator("#download-csv");
								if (downloadBtn.isVisible()) {
									downloadBtn.click();
								} else {
									// If the download button is not found, try the alternative button
									Locator downloadBtnAlt = page.locator("#download-csv-details");
									if (downloadBtnAlt.isVisible()) {
										downloadBtnAlt.click();
									}
								}
							}
						} catch (PlaywrightException e) {
							// Handle the case where the error element or other elements are not found
							System.out.println("An error occurred: " + e.getMessage());
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					return false;

				}
				Path filePath = downloadPath.resolve(download.suggestedFilename());
				System.out.println("Downloading file to: " + filePath);
				download.saveAs(filePath);
				File tempFile = filePath.toFile();
				FileReader reader = null;
				try {
					reader = new FileReader(tempFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
				CSVParser csvParser = null;
				try {
					csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader() // Specify the first
																									// record as the
																									// header
							.withIgnoreHeaderCase().withTrim());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Map<String, String> headerMap = new HashMap<>();
				for (String header : csvParser.getHeaderMap().keySet()) {
					headerMap.put(header.replaceAll("[^\\p{Print}]", "").trim().toLowerCase(), header);
				}
				System.out.println("headerMap : " + headerMap);
				List<POInvoiceDetails> CBPoInvoiceDetailsList = new ArrayList<POInvoiceDetails>();

				try {
					for (CSVRecord csvRecord : csvParser) {

						POInvoiceDetails CBPoInvoiceDetailsObj = new POInvoiceDetails();
						String InvoiceNumber=null;
						try {
							CBPoInvoiceDetailsObj.setPoInvoice(csvRecord.get(headerMap.get("invoice number")));
							InvoiceNumber=csvRecord.get(headerMap.get("invoice number"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						
						SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
						String paymentDueDateStr = null;
						try {
							paymentDueDateStr = csvRecord.get(headerMap.get("payment due date"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							if (paymentDueDateStr.matches("\\d{2}-\\d{2}-\\d{4}")) {
								// If matches, convert to the desired output format
								String paymentDate = convertAndPrint(paymentDueDateStr, outputFormat);
								logger.info(InvoiceNumber+" "+paymentDate);
								CBPoInvoiceDetailsObj.setPaymentDueDate(paymentDate);
							} else {
								// If not matches, just print the original date
								System.out.println("Input Date (no conversion needed): " + paymentDueDateStr);
								CBPoInvoiceDetailsObj.setPaymentDueDate(paymentDueDateStr);
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						String updateQuery="update invoice_paymentDuedate set paymentDueDate="
								+ "STR_TO_DATE('"+CBPoInvoiceDetailsObj.getPaymentDueDate()+"','%m/%d/%Y') "
								+ "where invoiceNumber='"+InvoiceNumber+"' and vendorId='"+vendorId+"'";
					 logger.info(updateQuery);
					 jdbcTemplate.execute(updateQuery);


					}
				
				} catch (Exception e) {
					e.printStackTrace();
				
				
					
				}
				
				tempFile.delete();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public String replaceCommaWithDot(String amt) {
		if (amt.contains("€") || amt.contains("KR") || amt.contains("ZŁ")) {
			amt = CommonUtil.replaceCurrency(amt).trim();
			int amtCharatcerLength = amt.length();
			if (amtCharatcerLength > 3) {
				char thirdLastChar = amt.charAt(amtCharatcerLength - 3);
				if (thirdLastChar == ',') {
					amt = amt.substring(0, amtCharatcerLength - 3) + '.' + amt.substring(amtCharatcerLength - 2);
				}
				int lastDotIndex = amt.lastIndexOf('.');

				if (lastDotIndex != -1) { // Check if a dot exists in the string
					amt = amt.replaceAll("\\.(?![^.]*$)", "");
					logger.info("Modified string: " + amt);
				} else {
					logger.info("No dot found in the string.");
				}
			}
		}
		return amt;
	}

	private static String convertAndPrint(String inputDateStr, SimpleDateFormat outputFormat) throws ParseException {
		// Define input date format
		SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy");

		// Parse the input string to a Date object
		Date inputDate = inputFormat.parse(inputDateStr);

		// Format the Date object to the desired output format
		String outputDateStr = outputFormat.format(inputDate);

		// Print the output
		System.out.println("Input Date: " + inputDateStr);
		System.out.println("Output Date: " + outputDateStr);
		return outputDateStr;
	}

	public void reFreshPage(Page page) {
		while (true) {

			boolean isLoginPage = page.isVisible("//*[@id='ap_email']");
			if (isLoginPage) {
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
						Locator previousUiPageLaoded = page
								.locator("//span[text()='Available Actions' and @class='bold']");
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
