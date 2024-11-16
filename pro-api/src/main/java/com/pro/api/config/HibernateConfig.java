package com.pro.api.config;

import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Bean
    public PhysicalNamingStrategy physicalNamingStrategy() {
    	
        return new ExactPhysicalNamingStrategy();
    }
}
