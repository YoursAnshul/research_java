package com.dimetyd.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.dimetyd.bot.helper.GlobalSession;

@SpringBootApplication
public class DimetydAmazonBotApplication {

	public static void main(String[] args) {
		GlobalSession.getGlobalSession().setName("Open_Shortage_Dispute_update");
		
		SpringApplication.run(DimetydAmazonBotApplication.class, args);
		
		
		
		
		
	}

}
