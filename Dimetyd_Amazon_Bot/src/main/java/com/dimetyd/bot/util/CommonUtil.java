package com.dimetyd.bot.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

@Component
public class CommonUtil {
	
	private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	
	
	public static final SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	
	public static String userHome = System.getProperty("user.home");

    // Dynamic paths
    public static String BasePath = Paths.get(userHome, "Downloads").toString();
    public static String basePath = Paths.get(userHome, "Documents").toString();
	
	
	
	public void closePopup(Page page) {
	   	 Locator parentLocator = page.locator("#hmd2f-trigger-tab");

				Locator cancelButtonLocator = parentLocator.locator(".vc-footer-hmd-feedback-link");

				cancelButtonLocator.click();
	    }
		 public void handleDismisPopup(Page page,String vendorName)
		 {
			 
			 String currentURL=getCountryUrl(vendorName);
			 
			 page.navigate(currentURL+"/home/vc");
		/*	// Close pop-up
				Locator dismissButton = page.locator("casino-simple-button:has-text('Dismiss')");

				if (dismissButton.count() > 0) {
					try {
						dismissButton.waitFor(
								new Locator.WaitForOptions().setTimeout(10000).setState(WaitForSelectorState.VISIBLE));
						dismissButton.click();
					} catch (Exception e) {
						System.out.println("Dismiss button was found but did not become visible in time.");
					}
				}*/
		 }
		 
		 public void switchpagenewPopup(Page page)
		 {
			 try {
					int retry = 0;
					while (true) {
						ElementHandle shroudElement = page.waitForSelector(
								"casino-generic-knowhere-wrapper.hydrated div[class*='casino-tour-shroud']",
								new Page.WaitForSelectorOptions().setTimeout(1000));

						if (shroudElement != null) {

							String currentClass = shroudElement.getAttribute("class");
							logger.info(currentClass);

							if (currentClass != null && currentClass.contains("enabled")) {
								Locator isEnabledTour = page
										.locator("casino-generic-knowhere-wrapper.hydrated div.casino-tour-shroud");
								if (isEnabledTour.count() > 0) {
									isEnabledTour.evaluate("element => element.classList.remove('enabled')");
									logger.info("Enabled Tour Option DISABLED");
								}
							} else {
								logger.info("Enabled Tour Option DISABLED");
								logger.info(currentClass);
							}
							page.waitForTimeout(4000);

						} else {
							logger.info("Element not found within 1s.");
						}
						retry = retry + 1;
						if (retry <= 3) {
							logger.info("loop Break");
							break;
						}
					}
				} catch (PlaywrightException e) {
					logger.info("Tour POP up not Found");
				}
		 }
		  
		 public void killLoader(Page page)
		 {
				try {
					page.evaluate("() => {" + "const loader = document.querySelector('.melodic-loading-overlay');"
							+ "if (loader) loader.remove();" + "}");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }
		 
		 public void reminemepopup(Page page)
		 {
			 
			 try {
					Locator isFeedbackEnabled= page.locator("div.vibes-modal-visible");
					if(isFeedbackEnabled.count()>0)
					{
						isFeedbackEnabled.evaluate("element => element.classList.remove('-visible')");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }
		 
		 public void clsoeFeedbackPopup(Page page)
		 {
			 
				String popupXPath = "//div[contains(@class, 'a-section') and contains(@class, 'fc-left') and contains(@class, 'expand')]";

				// Locator for the expanded popup
				Locator popup = page.locator(popupXPath);

				// Check if the popup is visible
				if (popup.count() > 0 && popup.first().isVisible()) {
				    logger.info("Popup is expanded. Attempting to close...");

				    // Close button inside the popup (adjust selector as needed)
				    Locator closeBtn = page.locator("//*[@id=\"hmd2f-trigger-tab\"]/a");

				    if (closeBtn.count() > 0 && closeBtn.first().isVisible()) {
				        closeBtn.click();
				        logger.info("Popup closed successfully.");
				    } else {
				        logger.warn("Close button not found or not visible.");
				    }
				} else {
				    logger.info("Popup not expanded or not present.");
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
						Locator pageLoaded = page.locator("#input-box");
						if (pageLoaded.count() > 0) {
							logger.info("Page properly Loaded");
							break;
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
	
		
		public static boolean waitForFile(File file, Duration timeout) {
	        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	        long endTime = System.currentTimeMillis() + timeout.toMillis();
 
	        while (System.currentTimeMillis() < endTime) {
	            if (file.exists()) {
	                executor.shutdown();
	                return true;
	            }
	            try {
	                // Polling interval
	                Thread.sleep(100);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
 
	        executor.shutdown();
	        return false;
	    }
		public static void deleteFileOnPath(Path downloadPath) {
			try {
				// Check if the directory exists
				if (Files.exists(downloadPath) && Files.isDirectory(downloadPath)) {
					// List all files in the directory
					Files.list(downloadPath).forEach(path -> {
						try {
							// Delete each file
							Files.delete(path);
							System.out.println("Deleted: " + path);
						} catch (IOException e) {
							System.err.println("Failed to delete: " + path + " due to: " + e.getMessage());
						}
					});
				} else {
					System.err.println("The path is not a directory or does not exist.");
				}
			} catch (IOException e) {
				System.err.println("An error occurred while listing or deleting files: " + e.getMessage());
			}
		}
		public String processAmount(String input) {
		    if (input == null || input.trim().isEmpty()) {
		        return "0.0";
		    }

		    input = input.trim();

		    // Remove currency symbols and whitespace
		    input = input.replaceAll("[^\\d.,-]", "");

		    int lastDot = input.lastIndexOf('.');
		    int lastComma = input.lastIndexOf(',');

		    char decimalSeparator;
		    char thousandSeparator;

		    if (lastComma > lastDot) {
		        // European format: 1.2515,6 → decimal = ','; thousand = '.'
		        decimalSeparator = ',';
		        thousandSeparator = '.';
		    } else {
		        // US/UK format: 12,515.60 → decimal = '.'; thousand = ','
		        decimalSeparator = '.';
		        thousandSeparator = ',';
		    }

		    // Remove thousand separators
		    input = input.replace(String.valueOf(thousandSeparator), "");

		    // Replace decimal separator with dot
		    if (decimalSeparator != '.') {
		        input = input.replace(decimalSeparator, '.');
		    }

		    // Final cleanup in case there's still anything unexpected
		    input = input.replaceAll("[^\\d.-]", "");

		    // Handle inputs like ".60" or "-.60"
		    if (input.startsWith(".")) {
		        input = "0" + input;
		    } else if (input.startsWith("-.")) {
		        input = "-0" + input.substring(1);
		    }

		    return input;
		}

		public static String getCurrency(String data) {

			if (data.contains("CAD") || data.contains("$AD"))
				return "CAD";
			else if (data.contains("£"))
				return "GBP";
			else if (data.contains("€"))
				return "EUR";
			else if (data.contains("MXN"))
				return "MXN";
			else if (data.contains("AED"))
				return "AED";
			else if (data.contains("$"))
				return "USD";
			else if(data.contains("AUD"))
				return "AUD";
			else if(data.contains("EUR"))
				return "EUR";
			else if(data.contains("GBP"))
				return "GBP";
			else if(data.contains("ZŁ"))
				return "PLN";
			else if(data.contains("KR"))
				return "SEK";
			else if(data.contains("PLN"))
				return "PLN";
			else if(data.contains("SEK"))
				return "SEK";

			return "USD";
		}
		public static  String replaceCurrency(String data) {

			return data.replace("CAD", "").replace("$AD", "").replace("£", "").replace("€", "").replace("MXN", "")
					.replace("AED", "").replace("$", "").replace("ZŁ", "").replace("PLN", "").replace("KR", "")
					.replace("SEK", "").replace("AUD", "").replace("EUR", "").replace("GBP", "").replace("USD", "");
		}
	
		public static void updateTimeStamp() throws IOException {

			File updateFile = new File(BasePath + "UpdateTimestamp.txt");
			if (!updateFile.exists()) {
				try {
					updateFile.createNewFile();
				} catch (IOException e) {
					logger.error("Error while creating new update timestamp file");
					e.printStackTrace();
				}
			}

			FileWriter fw = new FileWriter(updateFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			String timeStamp = formatter.format(new Date());

			bw.write("Timestamp : " + timeStamp);
			bw.close();

			logger.info("Timestamp updated");

		}
		public String getCountryUrl(String inVendorName) {
			String[] switchStrings = { "CA -", "ES -", "DE -", "GB -", "FR -", "AE -", "AU -", "IT -", "MX -", "BE -",
					"PL -", "SE -", "NL -" };
			String outCountry = "https://vendorcentral.amazon.com"; // Default value

			// Convert inVendorName to uppercase for case-insensitive comparison
			String message = inVendorName.toUpperCase();

			// Check if the message contains any of the country codes and set the
			// corresponding URL
			for (String countryCode : switchStrings) {
				if (message.contains(countryCode)) {
					switch (countryCode) {
					case "GB -":
						outCountry = "https://vendorcentral.amazon.co.uk";
						break;
					case "CA -":
						outCountry = "https://vendorcentral.amazon.com";
						break;
					case "DE -":
						outCountry = "https://vendorcentral.amazon.de";
						break;
					case "FR -":
						outCountry = "https://vendorcentral.amazon.fr";
						break;
					case "ES -":
						outCountry = "https://vendorcentral.amazon.es";
						break;
					case "AU -":
						outCountry = "https://vendorcentral.amazon.com.au";
						break;
					case "IT -":
						outCountry = "https://vendorcentral.amazon.it";
						break;
					case "MX -":
						outCountry = "https://vendorcentral.amazon.com";
						break;
					case "BE -":
						outCountry = "https://vendorcentral.amazon.com.be";
						break;
					case "PL -":
						outCountry = "https://vendorcentral.amazon.pl";
						break;
					case "NL -":
						outCountry = "https://vendorcentral.amazon.nl";
						break;
					case "SE -":
						outCountry = "https://vendorcentral.amazon.se";
						break;
					default:
						outCountry = "https://vendorcentral.amazon.com"; // Default case (already initialized)
						break;
					}
					break; // Exit the loop once a match is found
				}
			}
			return outCountry;
		}
	

}

