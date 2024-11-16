package com.pro.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProApiApplication {
	private static final Logger logger = LoggerFactory.getLogger(ProApiApplication.class);
	public static void main(String[] args) {

		SpringApplication.run(ProApiApplication.class, args);
		 logger.info("Hello PRO!");
	}
}
