package com.pro.api.service;

import com.pro.api.controllers.GeneralResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuditService {
    public GeneralResponse updateNetId(String netId);
}
