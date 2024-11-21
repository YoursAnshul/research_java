package com.pro.api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pro.api.response.QuickResponse;
import com.pro.api.service.QuickReference;

@RestController
@RequestMapping("/quick-reference")
public class QuickReferenceController {

	@Autowired
	private QuickReference quickReference;

	@GetMapping("/list")
	public ResponseEntity<GeneralResponse> getQuikResponseData(
			@RequestParam(name = "type", defaultValue = "false") String type) {
		List<QuickResponse> list = quickReference.getQuikGeneralResponse(type);
		GeneralResponse response = new GeneralResponse();
		response.Subject = list;
		return ResponseEntity.ok(response);
	}
}
