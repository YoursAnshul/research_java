package com.pro.api.service;

import org.springframework.stereotype.Service;

import com.pro.api.controllers.GeneralResponse;
import com.pro.api.response.ShiftScheduleRequest;

@Service
public interface ScheduleService {
	public GeneralResponse saveSchedule(ShiftScheduleRequest request);
}
