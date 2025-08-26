package com.dimetyd.bot.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@Component
public class PO_CommonUtil {

	private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

	public static final SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	//Administrator
	//sphin
	public static String BasePath = "C:\\Users\\sphin\\Downloads\\";
	public static String basePath = "C:\\Users\\sphin\\Documents\\";

	
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
	 public static void deleteFile(Path filePath) throws IOException {
	        if (Files.exists(filePath)) {
	            try {
	                Files.delete(filePath);
	                logger.info("File Path deleted successfully.");
	            } catch (IOException e) {
	            	  logger.info("Failed to delete the file path.");
	                throw e; // Re-throw exception to handle it in the calling method if needed
	            }
	        } else {
	            System.out.println("File path does not exist.");
	        }
	    }
	 
	 public Integer searchResults(Page page) {
			int Limit = 0;
			try {

				try {
					page.locator("//div[@id='root']//kat-button[@label='Submit']").click();
				} catch (Exception e) {
					// logger.info("Unable to click on submit button");
					try {
						page.locator("//div[@id='root']//kat-button[@label='Submit']").click();
					} catch (Exception e1) {
						page.reload();
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						page.click("//div[@id='root']//kat-button[@label='Submit']");

					}
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Locator searchResults = page.locator("//div[@id='orderHistory']//*[contains(text(),'Search Results')]");
				String limit = null;
				if (searchResults.count() > 0) {
					limit = searchResults.innerText().replace("Search Results ", "").replaceAll("[(){}]", "");
				} else {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					searchResults = page.locator("//div[@id='orderHistory']//*[contains(text(),'Search Results')]");
					limit = searchResults.innerText().replace("Search Results ", "").replaceAll("[(){}]", "");
				}

				Limit = Integer.valueOf(limit);
				if (Limit == 0) {
					logger.info("In limit loop");
					page.click("//kat-button[@label='Submit']");
					try {
						Thread.sleep(15000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					limit = searchResults.innerText().replace("Search Results ", "").replaceAll("[(){}]", "");
					Limit = Integer.valueOf(limit);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return Limit;
		}

	 public static int convertMonthToInt(String monthName) {
			// Normalize input (case-insensitive match)
			monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1).toLowerCase();
			for (Month month : Month.values()) {
				if (month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).equals(monthName)
						|| month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).equals(monthName)) {
					return month.getValue(); // January = 1
				}
			}
			throw new IllegalArgumentException("Invalid month name: " + monthName);
		}

	
}

