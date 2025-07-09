package com.pro.api.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;
import com.pro.api.service.CoreHoursRequest;
import com.pro.api.service.ForecastingService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/forecasting")
public class ForecastingController {

	@Autowired
	private ForecastingService forecastingService;

	@GetMapping("/list")
	public ResponseEntity<PageResponse<ForecastingResponse>> getList(
			@RequestParam(value = "codeValues", required = false) Integer codeValues) {
		PageResponse<ForecastingResponse> response = forecastingService.getList(codeValues);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/update")
	public ResponseEntity<GeneralResponse> updateCoreHours(@RequestBody List<CoreHoursRequest> requests) {
		GeneralResponse response = forecastingService.updateForeCastingHours(requests);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/project-list")
	public ResponseEntity<PageResponse<ForecastingResponse>> geProjectList(
			@RequestParam(value = "codeValues", required = false) Integer codeValues) {
		PageResponse<ForecastingResponse> response = forecastingService.getProjectCoreHoursList(codeValues);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/user-total-hours")
	public ResponseEntity<List<Long>> getUserTotalHours(
			@RequestParam(value = "codeValues", required = false) Integer codeValues) {
		List<Long> response = forecastingService.getUserTotalHours(codeValues);
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

	@GetMapping("/export")
	public void exportForecastingExcel(@RequestParam(required = false) Integer codeValues,
			HttpServletResponse response) {
		try {
			forecastingService.exportForecastingExcel(codeValues, response);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
