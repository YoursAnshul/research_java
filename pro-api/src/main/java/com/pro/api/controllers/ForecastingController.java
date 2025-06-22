package com.pro.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;
import com.pro.api.service.ForecastingService;

@RestController
@RequestMapping("/forecasting")
public class ForecastingController {

	@Autowired
	private ForecastingService forecastingService;

	@GetMapping("/list")
	public ResponseEntity<PageResponse<ForecastingResponse>> getAnnouncementList() {
		PageResponse<ForecastingResponse> response = forecastingService.getList();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
