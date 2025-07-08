package com.pro.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;

@Service
public interface ForecastingService {

	public PageResponse<ForecastingResponse> getProjectCoreHoursList();

	public List<Long> getUserTotalHours();

	public List<Long> getProjectTotalHours();

}
