package com.dimetyd.bot.process;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.CBMissingInvoiceOutput;
import com.dimetyd.bot.util.CommonUtil;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

@Component
public class InvoiceCreationPage {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final DecimalFormat decfor = new DecimalFormat("0.00");

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private CommonUtil commonUtilObj;

	public String processPage(Page page, String PO, String invoiceNumber, int retry, String vendorName,
			String vendorId) {

		try {
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
			page.waitForTimeout(3000);
			reFreshPage(page);
			Locator newUiInvoicesPage = page.locator("#input-box");
			if (newUiInvoicesPage.count() > 0) {
				Locator returntoPreviousUi = page
						.locator("//kat-badge[@label='Click here to return to the previous experience']");
				returntoPreviousUi.click();
				page.waitForTimeout(3000);
				reFreshPage(page);
			}
			Locator createInvoice = page.locator("#CREATE_INVOICE_ENABLED_BUTTON_ID");
			createInvoice.click();
			page.waitForTimeout(2000);
			Locator checkpage = page.locator("//div[@class='a-popover a-popover-modal a-declarative']");

			if (checkpage.count() > 0) {
				Locator vendorOptions = page
						.locator("//div[@class='a-popover a-popover-modal a-declarative']//label//span");
				int count = vendorOptions.count();

				for (int i = 0; i < count; i++) {
					String currentVendorName = vendorOptions.nth(i).innerText().trim().toLowerCase();
					if (currentVendorName.equals(vendorName.trim().toLowerCase())) {
						logger.info("vendor switching page popup found at the time of invoicing: " + currentVendorName);
						vendorOptions.nth(i).click(new Locator.ClickOptions().setTimeout(3000));
						break;
					}
				}

				page.locator("#create-dispute-button").click(new Locator.ClickOptions().setTimeout(3000));
			}

			else {
				logger.info("vendor switching page popup not found at the time of invoicing");
			}

			page.selectOption("select#po-search-key", new SelectOption().setValue("PURCHASE_ORDER"));

			Locator inputPo = page.locator("#po-number");
			inputPo.fill(PO);
			Locator searchBtn = page.locator("#poSearchTableForm-submit");
			try {
				searchBtn.click();
				page.waitForTimeout(2000);
				
			} catch (PlaywrightException e) {
				Locator parentLocator = page.locator("#hmd2f-trigger-tab");
				Locator cancelButtonLocator = parentLocator.locator(".vc-footer-hmd-feedback-link");
				cancelButtonLocator.click();
				searchBtn.click();
			}
			boolean isresultfound=page.isVisible("//div[@class='a-box-inner' and contains(text(),'No result')]");
			if(isresultfound)
			{
				return "NOT FOUND";
			}
			else
			{
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Select the checkbox using CSS selectors
			try {
				page.locator("#posearchtable #r" + PO + " #r" + PO + "-purchase_order_checkbox-input-wrap")
						.locator(".a-icon.a-icon-checkbox").click();
				try {
					page.waitForTimeout(1500);
					// Locator vatCheckbox = page.locator("#create-inv-vendor-vat-id
					// #create-inv-vat-id-available");

					Locator vatCheckbox = page.locator("#create-inv-vendor-vat-id #create-inv-no-vat-id");

					// Wait up to 3 seconds for it to be visible
					vatCheckbox.waitFor(new Locator.WaitForOptions().setTimeout(3000));

					// Now click it
					vatCheckbox.click();
				} catch (PlaywrightException e) {
					logger.info("No VAT option present!!!");
				}

				page.waitForTimeout(1000); // Wait for 1 second
				page.locator("#inv-action-bar #create-invoice-submit").click();

			} catch (PlaywrightException e) {
				logger.error("Element not found or other error: " + e.getMessage());
				return "ERROR";
			}

			StringBuilder sql = new StringBuilder();
			sql.append(
					"SELECT v.vendorName,`PO`,`ASIN`,`quantityReceived`,`quantityInvoiced`,`needToInvoicedQty`,`invoiceNumber`,`unitCost`,`total` FROM `CBMissingInvoiceOutput` a JOIN Vendor v ON (a.vendorId=v.id) WHERE invoiceNumber = '"
							+ invoiceNumber + "' AND v.id='" + vendorId + "'");

			List<CBMissingInvoiceOutput> catlogList = this.jdbcTemplate.query(sql.toString(),
					new RowMapper<CBMissingInvoiceOutput>() {
						@Override
						public CBMissingInvoiceOutput mapRow(ResultSet rs, int rowNum) throws SQLException {
							CBMissingInvoiceOutput res = new CBMissingInvoiceOutput();
							res.setASIN(rs.getString("ASIN"));
							res.setInvoiceNumber(rs.getString("invoiceNumber"));
							res.setNeedToInvoicedQty(rs.getInt("needToInvoicedQty"));
							res.setPO(rs.getString("PO"));
							res.setQuantityInvoiced(rs.getInt("quantityInvoiced"));
							res.setQuantityReceived(rs.getInt("quantityReceived"));
							res.setTotal(rs.getDouble("total"));
							res.setUnitCost(rs.getDouble("unitCost"));
							res.setVendorName(rs.getString("vendorName"));

							return res;
						}
					}, new Object[] {});

			/*
			 * while (true) { try { List<Locator> listPagesPagination = page
			 * .locator("//div[@role='navigation']//ul[@class='a-pagination']//li").all();
			 * 
			 * if (listPagesPagination.size() != 1) { break; } } catch (Exception e) {
			 * logger.info("No Pagination"); break; }
			 * 
			 * Thread.sleep(2000); }
			 */

			Locator invoiceNumberElement = page.locator("#invoice-header-box-left #inv-number-data #invoice-number");

			String invoiceNumberFromDB = catlogList.get(0).getInvoiceNumber().replaceAll(" ", "");
			invoiceNumberElement.fill(invoiceNumberFromDB);

			Locator invoiceDetails = page.locator("#invoice-item-details");
			invoiceDetails.click();
			try {
				Locator errorText = page.locator("#inv-number-data").locator(".error-text");

				// Wait up to 3 seconds for the element to appear
				errorText.waitFor(new Locator.WaitForOptions().setTimeout(3000));

				// Now safely get the text
				String text = errorText.innerText();
				if (text.contains("has already been used.")) {
					return "COMPLETED";
				}
			} catch (Exception e) {
				logger.info("Error Text not Found...");
			}

//			while (true) {
//				Boolean checkbox = page
//						.isVisible("//div[@class='melodic-loading-overlay' and contains(@style, 'display: block;')]");
//
//				if (checkbox) {
//					logger.info("Loading Spinner....");
//					page.waitForTimeout(2000); // wait a short time before checking again
//				} else {
//					break;
//				}
//			}
			commonUtilObj.killLoader(page);

			commonUtilObj.clsoeFeedbackPopup(page);
			logger.info("POP END");
			page.waitForTimeout(1000);
			// boolean breakloop = true;

			boolean isSelectAllClick = false;
			boolean isDeselectAllClick = false;
			
			/*while (true) {
				try {
					if (isSelectAllClick == false) {
						if (page.isVisible("#line-items-select-all")) {
							try {
								page.click("#line-items-select-all");
							} catch (Exception ex) {

							}
							logger.info("Clicked SELECT");
							isSelectAllClick = true;

						}		
			else
						{
							logger.info("Select All is not visible..");
						}
					} else if (isDeselectAllClick == false && isSelectAllClick) {
						if (page.isVisible("#line-items-deselect-all")) {
							try {
								page.click("#line-items-deselect-all");
						} catch (Exception ex) {
						}
						logger.info("Clicked DESELECT");
						isDeselectAllClick = true;						}

					} else {
						if (page.isVisible("#line-items-select-all") && isSelectAllClick && isDeselectAllClick) {
							{
								logger.info("Select and Deselect All Done");
								break;
							}
						}
					}

				} catch (Exception e) {
			}

			}
		
			page.waitForTimeout(1000);
			*/
			int selectAllNotVisibleCount = 0; // Counter for tracking log messages

			while (true) {
			    try {
			        if (!isSelectAllClick) {
			            if (page.isVisible("#line-items-select-all")) {
			                try {
			                    page.click("#line-items-select-all");
			                } catch (Exception ex) {
			                    logger.warn("Error clicking SELECT: " + ex.getMessage());
			                }
			                logger.info("Clicked SELECT");
			                isSelectAllClick = true;

			            } else {
			                logger.info("Select All is not visible..");
			                selectAllNotVisibleCount++;

			                if (selectAllNotVisibleCount >= 10) {
			                    logger.info("Select All not visible after 10 attempts. Attempting to DESELECT and breaking loop...");
			                    try {
			                        page.click("#line-items-deselect-all");
			                        logger.info("Clicked DESELECT after 10 failed SELECT attempts");
			                    } catch (Exception ex) {
			                        logger.warn("Error clicking DESELECT after 10 attempts: " + ex.getMessage());
			                    }
			                    break; // Exit loop after trying DESELECT
			                }
			            }

			        } else if (!isDeselectAllClick && isSelectAllClick) {
			            if (page.isVisible("#line-items-deselect-all")) {
			                try {
			                    page.click("#line-items-deselect-all");
			                } catch (Exception ex) {
			                    logger.warn("Error clicking DESELECT: " + ex.getMessage());
			                }
			                logger.info("Clicked DESELECT");
			                isDeselectAllClick = true;
			            }

			        } else {
			            if (page.isVisible("#line-items-select-all") && isSelectAllClick && isDeselectAllClick) {
			                logger.info("Select and Deselect All Done");
			                break;
			            }
			        }

			    } catch (Exception e) {
			        logger.warn("Exception in loop: " + e.getMessage());
			    }
			}


			int asinCount = catlogList.size();

			logger.info("Asin's Found :" + asinCount);

			for (int i = 0; i < asinCount; i++) {
				String AsinNumber = catlogList.get(i).getASIN();

				int needToInvoiceQty = catlogList.get(i).getNeedToInvoicedQty();
				Locator asinInput = page.locator("//input[@id='line-items-table-filter']");

				asinInput.fill(AsinNumber);
				page.waitForTimeout(1000);

				String checkCorrectAsin = page.locator("//td[contains(@id,'asin')]").innerText();
				if (checkCorrectAsin.toLowerCase().trim().equals(AsinNumber.toLowerCase().trim())) {
					logger.info("Match Searched Asin : " + AsinNumber);
					Locator qtyInput = page.locator("//input[contains(@id,'qty-input')]");
					page.waitForTimeout(1000);
					qtyInput.fill(Integer.toString(needToInvoiceQty));
					logger.info("Values entered for PO : " + PO + " and ASIN : " + AsinNumber);
					page.waitForTimeout(1000);

					page.click("//input[contains(@id,'cost_price-input')]");

					Thread.sleep(2000);

					DialogueBox(page);

					page.waitForTimeout(2000);
					Locator cost_priceInput = page.locator("//input[contains(@id,'cost_price-input')]");
					String cost_price = cost_priceInput.getAttribute("value").replace(",", "");
					cost_price = commonUtilObj.replaceCurrency(cost_price);
					logger.info("cost_price :" + cost_price);
					double unitCostFromSite = Double.parseDouble(cost_price);
					double unitCostFromDB = catlogList.get(i).getUnitCost();
					String unitCostFromDBString = Double.toString(unitCostFromDB);
					logger.info("unitCostFromDB :" + unitCostFromDB);
					logger.info("unitCostFromSite :" + unitCostFromSite);

					if (cost_priceInput.innerText().trim().contains("EUR")) {
						double unitCostFromDBForEUR = unitCostFromDB * 100;
						if (unitCostFromDBForEUR != unitCostFromSite) {
							unitCostFromDBString = unitCostFromDBString.replace(".", ",");

							logger.info("Value entered for unit cost : " + unitCostFromDBString);
							cost_priceInput.fill(unitCostFromDBString);

							Thread.sleep(2000);

							try {
								invoiceDetails.click(new Locator.ClickOptions().setTimeout(1000));

							} catch (Exception e) {

								try {
									Locator cancelfeedBackButton = page.locator("#hmd2f-trigger-tab")
											.locator(".vc-footer-hmd-feedback-link");
									cancelfeedBackButton.click(new Locator.ClickOptions().setTimeout(1000));
								} catch (Exception e1) {
								}

								// invoiceDetails.click(new Locator.ClickOptions().setTimeout(1000));
							}

							DialogueBox(page);

						}
					} else {
						if (unitCostFromDB != unitCostFromSite) {
							cost_priceInput.fill(Double.toString(unitCostFromDB));

							Thread.sleep(2000);
							try {
								page.locator("//td[@data-column='tax_rate']")
										.click(new Locator.ClickOptions().setTimeout(1000));

								DialogueBox(page);

							} catch (Exception ex) {
								logger.info("Tax Rate Option not found");
							}
							try {

								invoiceDetails.click(new Locator.ClickOptions().setTimeout(1000));
							} catch (Exception e) {
								try {
									Locator cancelfeedBackButton = page.locator("#hmd2f-trigger-tab")
											.locator(".vc-footer-hmd-feedback-link");
									cancelfeedBackButton.click(new Locator.ClickOptions().setTimeout(1000));
								} catch (Exception ex) {

								}

								invoiceDetails.click(new Locator.ClickOptions().setTimeout(1000));
							}

							DialogueBox(page);
						}
					}

					page.waitForTimeout(1000);

					try {
						Locator taxRate = page.locator("//td[@data-column='tax_rate']");
						taxRate.waitFor(new Locator.WaitForOptions().setTimeout(1000));
						if (taxRate.count() > 0) {
							Locator selectElement = taxRate.locator("select"); // Find the <select> element
																				// inside the row
							selectElement.selectOption(new SelectOption().setIndex(1));
						}

						invoiceDetails.click(new Locator.ClickOptions().setTimeout(1000));
						Thread.sleep(5000);
					} catch (Exception ex) {
						logger.info("tax rate not found");
					}
					asinInput.clear();

				} else {
					logger.info(
							"Not Matched ASINS " + checkCorrectAsin.toLowerCase() + "---" + AsinNumber.toLowerCase());
				}
			}

			String amountFromSite = page.locator("#inv-total-amount-data").innerText().trim();

			if (amountFromSite.contains("€") || amountFromSite.contains("EUR")) {
				amountFromSite = commonUtilObj.processAmount(amountFromSite);
				// amountFromSite = amountFromSite.replace(",", ".");
//				String[] amountFromSiteArray = amountFromSite.split(",");
//				amountFromSiteArray[0] = amountFromSiteArray[0].replace(" ", "").replace(".", "");
//				amountFromSite = "";
//				amountFromSite = amountFromSiteArray[0] + "." + amountFromSiteArray[1];
			} else {
				amountFromSite = commonUtilObj.processAmount(amountFromSite);
	
			}

			if (amountFromSite.isBlank()) {
				Thread.sleep(5000);
				amountFromSite = page.locator("#inv-total-amount-data").innerText().trim();
				if (amountFromSite.contains("€") || amountFromSite.contains("EUR")) {
					// amountFromSite = amountFromSite.replace(",", ".");
					amountFromSite = commonUtilObj.processAmount(amountFromSite);
//					String[] amountFromSiteArray = amountFromSite.split(",");
//					amountFromSiteArray[0] = amountFromSiteArray[0].replace(" ", "").replace(".", "");
//					amountFromSite = "";
//					amountFromSite = amountFromSiteArray[0] + "." + amountFromSiteArray[1];
				} else {
					amountFromSite = commonUtilObj.processAmount(amountFromSite);
					
				}

			}

			logger.info(
					"Query : SELECT SUM(`unitCost`*`needToInvoicedQty`) AS totalAmount FROM `CBMissingInvoiceOutput`  WHERE `invoiceNumber` = '"
					+ catlogList.get(0).getInvoiceNumber() + "' AND  vendorId='" + vendorId + "'");
			StringBuilder sql1 = new StringBuilder();
			sql1.append(
					"SELECT SUM(`unitCost`*`needToInvoicedQty`) AS totalAmount FROM `CBMissingInvoiceOutput` WHERE `invoiceNumber` = '"
							+ catlogList.get(0).getInvoiceNumber() + "' AND  vendorId='" + vendorId + "'");

			List<CBMissingInvoiceOutput> catlogList1 = this.jdbcTemplate.query(sql1.toString(),
					new RowMapper<CBMissingInvoiceOutput>() {

						@Override
						public CBMissingInvoiceOutput mapRow(ResultSet rs, int rowNum) throws SQLException {
							CBMissingInvoiceOutput res = new CBMissingInvoiceOutput();
							res.setAmountFromDb(rs.getDouble("totalAmount"));
							return res;
						}
					}, new Object[] {});

			logger.info("Total amount on site : " + amountFromSite);

			String amountFromDB = decfor.format(catlogList1.get(0).getAmountFromDb());
			logger.info("Total amount from database : " + amountFromDB);

			if (catlogList1.get(0).getAmountFromDb() == 0.00) {
				return "ALREADY INVOICED";
			}
			DialogueBox(page);

			// if (Double.parseDouble(amountFromSite.trim()) ==
			// Double.parseDouble(amountFromDB.trim())) {
			double siteAmount = cleanAndParse(amountFromSite);
			double dbAmount = cleanAndParse(amountFromDB);

			int siteAmountInt = (int) siteAmount;
			int dbAmountInt = (int) dbAmount;

			logger.info("Site: " + siteAmountInt + " Db:  " + dbAmountInt);

			if (Math.abs(siteAmountInt - dbAmountInt) <= 5) {

				try {
					Locator confirmCheckBox = page.locator(
							"//div[@id='inv-action-bar']//div[@id='cross-border-check']//input[@id='cross-border-agree-checkbox']");
					confirmCheckBox.waitFor(new Locator.WaitForOptions().setTimeout(2000));
					confirmCheckBox.click();
				} catch (Exception e) {
					logger.info("No Confirm CheckBox Present");
				}

				try {
					Locator agreeCheckbox = page.locator("#inv-action-bar").locator("#inv-agree-checkbox");
					agreeCheckbox.waitFor(new Locator.WaitForOptions().setTimeout(2000));
					agreeCheckbox.click();
				} catch (Exception e) {
				}

				//
				try {
					Locator iunderstandExist = page
							.locator("//div[@id='nedrcReasonCodeRadioButtons']//span[contains(text(),'I confirm')]");

					iunderstandExist.waitFor(new Locator.WaitForOptions().setTimeout(2000));
					if (iunderstandExist.count() > 0) {
						List<Locator> listClick = page.locator("//span[@class='a-label a-radio-label']").all();
						listClick.get(0).click();
					}
				} catch (Exception ex) {

				}

				//

				Locator submitButton = page.locator("#action-buttons").locator("#inv-submit")
						.locator(".a-button-input");
				submitButton.click();
				// ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
				// submitButton);

				// For vat other countries found one more click need to submit
				// SC031: Your VAT and/or address information indicates you are subject to SDI
				// invoicing and hence Amazon expects to receive your invoices via SDI. If you
				// are not subject to SDI invoicing, please update your SDI eligibility in
				// Vendor central -> Settings -> Tax Registration -> Add a new tax number or
				// Edit existing
				try {
					Locator isvatExist = page.locator("//*[@id='action-buttons']/span[contains(text(),'SC031')]");

					isvatExist.waitFor(new Locator.WaitForOptions().setTimeout(2000));
					if (isvatExist.count() > 0) {
						List<Locator> listClick = page.locator("//input[@id='VATExempt']").all();
						listClick.get(1).click();
						Locator submitButton1 = page.locator("#action-buttons").locator("#inv-submit")
								.locator(".a-button-input");
						submitButton1.click();
					}
				} catch (Exception ex) {

				}

				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				String text = page.locator(".a-alert-content").locator("h3").innerText();
				if (text.contains("successfully submitted.")) {
					return "COMPLETED";
				} else {
					return "ERROR";
				}

			} else {
				String comments = "Amount from database : " + dbAmountInt + " , Amount from site : " + siteAmountInt;
				logger.info("Comments : " + comments);
				logger.info("Query : update CBMissingInvoiceOutput  set comments='" + comments + " where invoiceNumber='"
						+ catlogList.get(0).getInvoiceNumber() + "' AND  vendorId='" + vendorId + "'");
				jdbcTemplate
						.execute("update CBMissingInvoiceOutput  set comments='" + comments + "' where invoiceNumber='"
								+ catlogList.get(0).getInvoiceNumber() + "' AND  vendorId='" + vendorId + "'");

				return "ERROR";
			}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			if (retry < 3) {
				return "PENDING";
			} else {
				return "ERROR";
			}
		}
		

	}

	public void DialogueBox(Page page) {
		page.waitForTimeout(3000);
		try {
			int checkCount = 1;
			while (true) {
				Locator dialogBox = page.locator("[role='dialog']").locator(".a-popover-wrapper")
						.locator("[class=' a-button-close a-declarative']").locator("[class='a-icon a-icon-close']");
				if (checkCount == 5) {
					break;
				} else {
					if (dialogBox.count() > 0) {
						dialogBox.click(new Locator.ClickOptions().setTimeout(1000));
						break;
					} else {
						logger.info("Waiting for Dialogue box to close....");
						page.waitForTimeout(2000);
						checkCount++;
					}
				}
			}
		} catch (Exception e) {
			logger.info("No dialog box !!!");
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
						logger.info("Page properly Loaded");
						break;
					} else {
						// Locator oldUi=page.locator("text='View all Invoices'");

						Locator oldUi = page.locator("text='Available Actions'");
						if (oldUi.count() > 0) {
							break;
						}
						Locator newUiPageLaoded = page.locator(
								"//*[@label='Click here to return to the previous experience' or text()='Click here']");
						if (newUiPageLaoded.count() > 0) {
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
									ex.printStackTrace();
								}

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

	public static double cleanAndParse(String amount) {
		return Double.parseDouble(amount.replaceAll("[^\\d.\\-]", ""));
	}
}
