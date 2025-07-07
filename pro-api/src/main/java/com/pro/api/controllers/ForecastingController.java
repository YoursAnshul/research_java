package com.pro.api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;
import com.pro.api.service.CoreHoursRequest;
import com.pro.api.service.ForecastingService;

@RestController
@RequestMapping("/forecasting")
public class ForecastingController {

	@Autowired
	private ForecastingService forecastingService;

	@GetMapping("/list")
	public ResponseEntity<PageResponse<ForecastingResponse>> getList() {
		PageResponse<ForecastingResponse> response = forecastingService.getList();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/update")
	public ResponseEntity<GeneralResponse> updateCoreHours(@RequestBody List<CoreHoursRequest> requests) {
		GeneralResponse response = forecastingService.updateForeCastingHours(requests);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/project-list")
	public ResponseEntity<PageResponse<ForecastingResponse>> geProjectList() {
		PageResponse<ForecastingResponse> response = forecastingService.getProjectCoreHoursList();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/user-total-hours")
	public ResponseEntity<List<Long>> getUserTotalHours() {
		List<Long> response = forecastingService.getUserTotalHours();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/project-total-hours")
	public ResponseEntity<List<Long>> getProjectTotalHours() {
		List<Long> response = forecastingService.getProjectTotalHours();
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/user-core-update")
	public ResponseEntity<GeneralResponse> updateUserCoreHours(@RequestBody List<CoreHoursRequest> requests) {
		GeneralResponse response = forecastingService.updateCoreHours(requests);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
