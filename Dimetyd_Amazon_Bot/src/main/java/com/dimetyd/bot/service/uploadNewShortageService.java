package com.dimetyd.bot.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dimetyd.bot.helper.GlobalSession;
import com.dimetyd.bot.process.FileUpload;

import jakarta.annotation.PostConstruct;

@Service
public class uploadNewShortageService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	FileUpload fileupload;
	

	@PostConstruct
	public void startService() {

		if (GlobalSession.getGlobalSession().getName().equals("New_Shortage_Upload")) {
			
			logger.info("New shortage update bot started.");
			 List<String> result = fileupload.insertData();
			 
			 logger.info(result.toString());
			
			
			

		}

	}

}
