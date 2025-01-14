package com.proep.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProepApiApplication {
	private static final Logger logger = LoggerFactory.getLogger(ProepApiApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(ProepApiApplication.class, args);
		logger.info("Pro-ep-api Application started successfully ...........");
	}
}
