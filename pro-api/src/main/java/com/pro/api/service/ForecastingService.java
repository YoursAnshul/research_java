package com.pro.api.service;

import org.springframework.stereotype.Service;

import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;

@Service
public interface ForecastingService {

	public PageResponse<ForecastingResponse> getList();
}
