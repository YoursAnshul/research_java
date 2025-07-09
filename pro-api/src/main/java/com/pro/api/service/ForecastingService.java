package com.pro.api.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;

import jakarta.servlet.http.HttpServletResponse;

@Service
public interface ForecastingService {

	public PageResponse<ForecastingResponse> getList(Integer codeValues);

	public GeneralResponse updateForeCastingHours(List<CoreHoursRequest> requests);

	public PageResponse<ForecastingResponse> getProjectCoreHoursList(Integer codeValues);

	public List<Long> getUserTotalHours(Integer codeValues);

	public List<Long> getProjectTotalHours();

	public GeneralResponse updateCoreHours(List<CoreHoursRequest> requests);

	public void exportForecastingExcel(Integer codeValues, HttpServletResponse response) throws IOException;
}
