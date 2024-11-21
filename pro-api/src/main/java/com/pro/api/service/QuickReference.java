package com.pro.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pro.api.response.QuickResponse;

@Service
public interface QuickReference {

	public List<QuickResponse> getQuikGeneralResponse(String type);
}