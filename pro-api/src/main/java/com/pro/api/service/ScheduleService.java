package com.pro.api.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.ScheduleResponse;
import com.pro.api.response.ShiftScheduleRequest;

@Service
public interface ScheduleService {
	public GeneralResponse saveSchedule(List<ShiftScheduleRequest> request);

	public List<ScheduleResponse> getList(String dempoId, Integer projectId, LocalDate scheduleDate, String tabValue,
			LocalDate startDate, LocalDate endDate, int year, int month);

	public GeneralResponse updateSchedule(ShiftScheduleRequest request);

	public GeneralResponse deleteSchedule(Long id);

	public GeneralResponse getOptionValue();

	public GeneralResponse getCoreHours(LocalDate scheduleDate, String dempoId);
}
