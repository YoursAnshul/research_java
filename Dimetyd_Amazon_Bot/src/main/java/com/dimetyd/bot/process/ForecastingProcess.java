package com.dimetyd.bot.process;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.process.ForecastFileImport;
import com.microsoft.playwright.Page;

@Component
public class ForecastingProcess {

	
	@Autowired
	private ForecastFileImport filedownload;
	
	
	 public void navigateForecast(Page page,String vendorName,String vendorId) throws IOException {
		 
		   /* System.out.println("vendor Switch");
			page.getByText(vendorName).click();
			System.out.println("vendor Selected");
			*/
			
			System.out.println("Now go through the navigation Reports forecasting");
			
			
			page.waitForTimeout(1000);
			System.out.println("click on Main menu");
			page.click("//*[@id=\"navbar\"]/div[1]/div[1]/div/img");
			System.out.println("click on Reports");
			page.click("//span[text()='Reports']");
			page.waitForTimeout(2000);
			
			page.click("//span[@class='flyout-menu-item-label' and contains(text(), 'Retail Analytics')]");
			page.waitForTimeout(2000);
			
			
			System.out.println("click on FORECASTING");
			page.click("//div[@class='ltr-1acepui']/a[text()='Forecasting']");
			
			page.waitForTimeout(2000);
			
			System.out.println("after forecasting click go through StatView of forecast");

			boolean isVisible = page.isVisible("//div[@class='ltr-1acepui']/a[text()='Forecasting']");
			if (isVisible) {
			System.out.println("selector is visible now");
					
			}
			else
			{
			System.out.println("Selector not visible Refresh Page...........");
			page.reload();
			page.waitForTimeout(1000);
				
			}
			 String[] dropdownItems = {"Mean forecast", "P70 Forecast", "P80 Forecast", "P90 Forecast"};
			 for (String dropdownItem : dropdownItems) {
	              System.out.println("Selecting: " + dropdownItem);
	              
	              
			
		page.click("#statistic");
			 page.locator(".standard-option-content").getByText(dropdownItem).click();
			page.locator(".button").getByText("Apply").click();
			
			
			page.locator(".button").getByText("csv").click();
			page.click("//a[text()='View and manage your downloads.']");
			
		
			filedownload.IndexFiledownLoaddown(page, vendorId,dropdownItem);
	 }
}


}
