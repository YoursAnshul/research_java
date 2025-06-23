package com.pro.api.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.ForecastingResponse;
import com.pro.api.response.PageResponse;

@Service
public interface ForecastingService {

	public PageResponse<ForecastingResponse> getList();

	public GeneralResponse updateCoreHours(String dempoId, LocalDate date, int coreHours);
}
