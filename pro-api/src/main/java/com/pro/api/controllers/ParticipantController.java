package com.pro.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pro.api.models.dataaccess.repos.ViPartLookupListRepository;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {
	
	@Autowired
	private ViPartLookupListRepository viPartLookupListRepository;
	
	@GetMapping("/lookup")
	public GeneralResponse getPartLookups() {
		GeneralResponse response = new GeneralResponse();
		try {

			response.Status = "Success";
			response.Message = "Successfully retrieved participant lookups";
			response.Subject = viPartLookupListRepository.findAll();
		} catch (Exception ex) {
			response.Status = "Failure";
			response.Message = ex.getMessage();
		}

		return response;
	}

}
