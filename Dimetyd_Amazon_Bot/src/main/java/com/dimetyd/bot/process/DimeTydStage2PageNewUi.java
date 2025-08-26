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

import com.dimetyd.bot.model.CBPoInvoiceDetails;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

@Component
public class DimeTydStage2PageNewUi {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private CommonUtil commonUtil;

	public boolean processPage(Page page, String po, String vendorId, String requestId, Integer retry,
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
					try {
						page.click(
								"//div[@class='search-dropdown']//kat-dropdown[@id='submitted-invoice-search-criteria']");// dropdwon
					} catch (Exception e) {
						try {
							Locator parentLocator = page.locator("#hmd2f-trigger-tab");

							Locator cancelButtonLocator = parentLocator.locator(".vc-footer-hmd-feedback-link");

							cancelButtonLocator.click();
						} catch (Exception e1) {

						}
						page.click(
								"//div[@class='search-dropdown']//kat-dropdown[@id='submitted-invoice-search-criteria']");
					}
					page.click("[value='PO_NUMBER']");// select po number
					logger.info("PO : " + po);
					page.click("//kat-icon[@name='search']");// search btn
					reFreshPage(page);
					Locator inputPo = page
							.locator("kat-input[class='submitted-invoice-input touched'] input[part='input']");
					inputPo.fill(po);
					page.click("//kat-icon[@name='search']");
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
							if (valueSelected.equals("PO_NUMBER")) {
								return true;
							} else {
								dropDown.click();
								page.click("[value='PO_NUMBER']");// select po number
								logger.info("PO : " + po);
								page.click("//kat-icon[@name='search']");
							}

						}
					}

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
				page.click("[class='close']");
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
				List<CBPoInvoiceDetails> CBPoInvoiceDetailsList = new ArrayList<CBPoInvoiceDetails>();

				try {
					for (CSVRecord csvRecord : csvParser) {

						CBPoInvoiceDetails CBPoInvoiceDetailsObj = new CBPoInvoiceDetails();
						//
//							if (csvRecord.getRecordNumber() == 1L) {
//								logger.info("GetRecoredNumber " + csvRecord.getRecordNumber());
//								continue;
//							}
						// CbPoDetailsObj.setInvoiceDate(invoiceDt);
						try {
							CBPoInvoiceDetailsObj.setPoInvoice(csvRecord.get(headerMap.get("invoice number")));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}

						try {
							CBPoInvoiceDetailsObj.setInvoiceAmount(Double.parseDouble(
									csvRecord.get(headerMap.get("invoice amount")).replaceAll("[^\\d.]", "")));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}

						try {
							CBPoInvoiceDetailsObj.setActualAmountPaid(Double.parseDouble(
									csvRecord.get(headerMap.get("actual paid amount")).replaceAll("[^\\d.]", "")));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						String invoiceDateStr = null;
						try {
							invoiceDateStr = csvRecord.get(headerMap.get("invoice date"));
						} catch (NullPointerException | IllegalArgumentException e) {
							try {
								invoiceDateStr = csvRecord.get(headerMap.get("invoicedate"));
							} catch (NullPointerException | IllegalArgumentException e1) {

							}
						}
						SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");

						try {
							if (invoiceDateStr.matches("\\d{2}-\\d{2}-\\d{4}")) {
								// If matches, convert to the desired output format
								String invoiceDate = convertAndPrint(invoiceDateStr, outputFormat);
								CBPoInvoiceDetailsObj.setInvoiceDate(invoiceDate);
							} else {
								// If not matches, just print the original date
								System.out.println("Input Date (no conversion needed): " + invoiceDateStr);
								CBPoInvoiceDetailsObj.setInvoiceDate(invoiceDateStr);
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
						String paymentDateStr = null;
						try {
							paymentDateStr = csvRecord.get(headerMap.get("payment due date"));
						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
						try {
							if (paymentDateStr.matches("\\d{2}-\\d{2}-\\d{4}")) {
								// If matches, convert to the desired output format
								String paymentDate = convertAndPrint(paymentDateStr, outputFormat);
								CBPoInvoiceDetailsObj.setPaymentDueDate(paymentDate);
							} else {
								// If not matches, just print the original date
								System.out.println("Input Date (no conversion needed): " + paymentDateStr);
								CBPoInvoiceDetailsObj.setPaymentDueDate(paymentDateStr);
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
						String uniqueKey = CBPoInvoiceDetailsObj.getPoInvoice() + vendorId;

						CBPoInvoiceDetailsObj.setUkey(uniqueKey);
						String status = "Pending";
						String comment = ""; // No data found
						String machineName = "";
						CBPoInvoiceDetailsList.add(CBPoInvoiceDetailsObj);
						StringBuilder sb = new StringBuilder(
								"INSERT IGNORE INTO CBMissingInvPOInvoice (requestId,vendorId,PO,POinvoice,Status,retry,comment,invoiceAmount,invoiceDate,paymentDate,machineName,createdDate,UniqueKey,actualAmountPaid) VALUES");
						if (CBPoInvoiceDetailsList.size() == 100) {
							for (int row1 = 0; row1 < CBPoInvoiceDetailsList.size(); row1++) {

								if (vName.startsWith("CA") || vName.startsWith("US")) {
									sb.append("('" + requestId + "','" + vendorId + "','" + po + "','"
											+ CBPoInvoiceDetailsList.get(row1).getPoInvoice() + "','" + status + "','"
											+ retry + "','" + comment + "','"
											+ CBPoInvoiceDetailsList.get(row1).getInvoiceAmount() + "',STR_TO_DATE('"
											+ CBPoInvoiceDetailsList.get(row1).getInvoiceDate()
											+ "','%m/%d/%Y')',STR_TO_DATE('"
											+ CBPoInvoiceDetailsList.get(row1).getPaymentDueDate() + "','%m/%d/%Y'),'"
											+ machineName + "',NOW(),'" + CBPoInvoiceDetailsList.get(row1).getUkey()
											+ "','" + CBPoInvoiceDetailsList.get(row1).getActualAmountPaid() + "')");
								} else {
									sb.append("('" + requestId + "','" + vendorId + "','" + po + "','"
											+ CBPoInvoiceDetailsList.get(row1).getPoInvoice() + "','" + status + "','"
											+ retry + "','" + comment + "','"
											+ CBPoInvoiceDetailsList.get(row1).getInvoiceAmount() + "',STR_TO_DATE('"
											+ CBPoInvoiceDetailsList.get(row1).getInvoiceDate()
											+ "','%d/%m/%Y')',STR_TO_DATE('"
											+ CBPoInvoiceDetailsList.get(row1).getPaymentDueDate() + "','%d/%m/%Y'),'"
											+ machineName + "',NOW(),'" + CBPoInvoiceDetailsList.get(row1).getUkey()
											+ "','" + CBPoInvoiceDetailsList.get(row1).getActualAmountPaid() + "')");
								}
								sb.append(",");
							}
							String query = sb.substring(0, sb.length() - 1) + ";";
							logger.info("Query " + query);
							jdbcTemplate.execute(query);
						}

					}
					StringBuilder sb = new StringBuilder(
							"INSERT IGNORE INTO CBMissingInvPOInvoice (requestId,vendorId,PO,POinvoice,Status,retry,comment,invoiceAmount,invoiceDate,paymentDate,machineName,createdDate,UniqueKey,actualAmountPaid) VALUES");

					for (int row1 = 0; row1 < CBPoInvoiceDetailsList.size(); row1++) {

						if (vName.startsWith("CA") || vName.startsWith("US")) {
							sb.append("('" + requestId + "','" + vendorId + "','" + po + "','"
									+ CBPoInvoiceDetailsList.get(row1).getPoInvoice() + "','PENDING','" + retry
									+ "','','" + CBPoInvoiceDetailsList.get(row1).getInvoiceAmount() + "',STR_TO_DATE('"
									+ CBPoInvoiceDetailsList.get(row1).getInvoiceDate() + "','%m/%d/%Y'),STR_TO_DATE('"
									+ CBPoInvoiceDetailsList.get(row1).getPaymentDueDate() + "','%m/%d/%Y'),'',NOW(),'"
									+ CBPoInvoiceDetailsList.get(row1).getUkey() + "','"
									+ CBPoInvoiceDetailsList.get(row1).getActualAmountPaid() + "')");
						} else {
							sb.append("('" + requestId + "','" + vendorId + "','" + po + "','"
									+ CBPoInvoiceDetailsList.get(row1).getPoInvoice() + "','PENDING','" + retry
									+ "','','" + CBPoInvoiceDetailsList.get(row1).getInvoiceAmount() + "',STR_TO_DATE('"
									+ CBPoInvoiceDetailsList.get(row1).getInvoiceDate() + "','%d/%m/%Y'),STR_TO_DATE('"
									+ CBPoInvoiceDetailsList.get(row1).getPaymentDueDate() + "','%d/%m/%Y'),'',NOW(),'"
									+ CBPoInvoiceDetailsList.get(row1).getUkey() + "','"
									+ CBPoInvoiceDetailsList.get(row1).getActualAmountPaid() + "')");
						}
						sb.append(",");
					}
					String query = sb.substring(0, sb.length() - 1) + ";";
					logger.info("Query " + query);
					jdbcTemplate.execute(query);
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
			amt = commonUtil.replaceCurrency(amt).trim();
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
						Locator previousUiPageLaoded = page.locator("text='View all Invoices'");
						if (previousUiPageLaoded.count() > 0) {
							logger.info("Previous UI Page properly Loaded");
							try {
								page.click("//div[@class='a-alert-content']//button");// got to new UI click btn
							} catch (PlaywrightException e) {

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
