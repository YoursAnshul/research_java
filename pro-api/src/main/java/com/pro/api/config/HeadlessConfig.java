package com.pro.api.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class HeadlessConfig {

    @PostConstruct
    public void setHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }
}