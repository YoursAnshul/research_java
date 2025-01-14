package com.proep.api.controllers;

import com.proep.api.models.business.SessionUserEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.proep.api.models.business.GeneralResponse;
import com.proep.api.models.dataaccess.Log;
import com.proep.api.models.dataaccess.repos.LogRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/logs")
public class LogsController {
	@Autowired
	private LogRepository logRepository;

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
			log.setNetId(netId);
			log  = logRepository.save(log);
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
