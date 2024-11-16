package com.pro.api.controllers;

import com.pro.api.models.business.SessionUserEmail;
import com.pro.api.models.dataaccess.Log;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class LogsController {
/*	@Autowired
	private LogRepository logRepository;*/
private static final Logger logger = LoggerFactory.getLogger(LogsController.class);

	@Autowired
	private SessionUserEmail UserEmail;

	@ModelAttribute("UserEmail")
	public SessionUserEmail getUserEmail() {
		return UserEmail;
	}

	@PostMapping
	public GeneralResponse logMessage(HttpServletRequest request, @RequestBody Log log) {
		GeneralResponse response = new GeneralResponse();
		String netId = "Unknown";
		// Retrieve NetId from session if available
		Object netIdObj = request.getSession().getAttribute("NetId");
		if (netIdObj != null) {
			netId = (String) netIdObj;
		}
		try {
			//log.setNetId(netId);
			//log  = logRepository.save(log);
			logger.info("Log Message: " + log.getMessage());
			response.Status = "Success";
			response.Message = "Successfully logged message";
			response.Subject = log;
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}
}
