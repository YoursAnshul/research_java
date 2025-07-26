package com.pro.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;

@Service
public interface ForecastingService {

	public PageResponse<ForecastingResponse> getList(String codeValues);

	public PageResponse<ForecastingResponse> getProjectCoreHoursList(String codeValues);

	public List<Long> getUserTotalHours(String codeValues);

	public List<Long> getProjectTotalHours();
	

}
