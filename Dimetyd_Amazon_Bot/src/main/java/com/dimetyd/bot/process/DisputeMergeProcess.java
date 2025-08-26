package com.dimetyd.bot.process;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DisputeMergeProcess {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public String MailMessage(Page page2, String vendorName) {

		String message = null;

		page2.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Correspondence details")).click();
		while (true) {
			try {
				page2.waitForTimeout(2000);
				// Check if "Correspondence history" is visible
				
				boolean isCorrespondenceVisible = page2.locator("text=Correspondence history").isVisible();
				if (isCorrespondenceVisible) {

					try {

						boolean isFoundMessage = page2.isVisible("//DIV[@id='commBox']");
						int RetryMessageLoading=0;
						while (true) {
						
							isFoundMessage = page2.isVisible("//DIV[@id='commBox']");
							if (isFoundMessage) {
								logger.info("Message Now Visible");
	
								break;
							} else {
								page2.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute details")).click();
								page2.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Correspondence details"))
										.click();								
								logger.info("Message Loading..."+RetryMessageLoading);
								RetryMessageLoading=RetryMessageLoading+1;
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					List<Locator> messageBox;
					try {
						Locator element = page2
								.locator("//div[@id='communication-box']//div[@class='a-box discussionBlockCss']");
						element.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

						messageBox = page2
								.locator("//div[@id='communication-box']//div[@class='a-box discussionBlockCss']")
								.all();
					} catch (Exception e) {

						messageBox = page2.locator(
								"//div[@id='communication-box']//div[@class='a-box-group discussionAmazonBlockCss']")
								.all();

						// TODO Auto-generated catch block
						// e.printStackTrace();
					}

					Locator messageBody = messageBox.get(0);

					message = messageBody.innerText();

					logger.info(message);

					page2.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute details")).click();
					page2.waitForTimeout(2000);
					break;

				} else {
					logger.info("Correspondence history not found for vendor: " + vendorName);
					page2.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute details")).click();
					page2.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Correspondence details"))
							.click();

				}
			} catch (Exception ex) {

				return null;
			}
		}
		return message;
	}

	public String DisputeMerge(Page page2, String vendorName) {

		List<Locator> messageBox;
		String newDisputeId = null;
		vendorName = vendorName.substring(0, 2).trim();

		page2.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Correspondence details")).click();
		page2.waitForTimeout(2000);
		// Check if "Correspondence history" is visibleh
		while(true)
		{
			try
			{
		boolean isCorrespondenceVisible = page2.locator("text=Correspondence history").isVisible();
		if (isCorrespondenceVisible) {

			try {

				boolean isFoundMessage = page2.isVisible("//DIV[@id='commBox']");

				while (true) {
					isFoundMessage = page2.isVisible("//DIV[@id='commBox']");
					if (isFoundMessage) {
						logger.info("Message Now Visible");
						break;
					} else {
						logger.info("Message Loading...");
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				Locator element = page2
						.locator("//div[@id='communication-box']//div[@class='a-box discussionBlockCss']");
				element.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

				messageBox = page2.locator("//div[@id='communication-box']//div[@class='a-box discussionBlockCss']")
						.all();
			} catch (Exception e) {

				messageBox = page2
						.locator("//div[@id='communication-box']//div[@class='a-box-group discussionAmazonBlockCss']")
						.all();

				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

			logger.info("VendorName: " + vendorName + " messageBox Count: " + messageBox.size());

			int index = 0;

			for (Locator message : messageBox) {

				if (index == 1) {
					break;
				}

				switch (vendorName) {
				case "ES": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("tus otras disputas en el ID de disputa")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "FR": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("litiges sous le numéro de litige")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "DE": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("Widersprüche zur Widerspruchsnummer")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "IT": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("nellID di contestazione")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "US": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("To process your disputes more quickly, Dispute ID")
							|| messageBody.contains("process your disputes more quickly")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "CA": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("disputes more quickly")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "MX": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("tus otras disputas en el ID de disputa")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "GB": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("disputes more quickly")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "AE": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("disputes more quickly")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "SE": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("dina andra bestridanden till")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "NL": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("Om je geschillen sneller te verwerken")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "PL": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("Om je geschillen sneller te verwerken")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				case "BR": {

					String messageBody = message.innerText();

					logger.info(messageBody);

					if (messageBody.contains("Om je geschillen sneller te verwerken")) {
						String regex = "DSPT\\d+";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(messageBody);

						// Find and print the second occurrence of the dispute ID
						int count = 0;
						while (matcher.find()) {
							count++;
							if (count == 2) {
								logger.info("Second dispute ID: " + matcher.group());
								newDisputeId = matcher.group();
								break;
							}
						}

					}

				}
					break;

				default:

					logger.info("Unexpected value: " + vendorName);

				}
				index = index + 1;

			}

			return newDisputeId;

		} else {
			logger.info("Correspondence history not found for vendor: " + vendorName);
			page2.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Dispute details")).click();
			page2.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Correspondence details")).click();

		}
		
		}
			catch(Exception ex1)
			{
				break;
			}
		}
		return null;

	}
}
