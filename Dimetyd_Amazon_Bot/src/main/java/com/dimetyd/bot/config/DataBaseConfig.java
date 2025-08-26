package com.dimetyd.bot.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Configuration
public class DataBaseConfig {

	@Value("${secret.name}")
	private String secretName;

	@Value("${is_db_local}")
	private Boolean isDBLocal;

	private static final Region REGION = Region.US_EAST_1;
	
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Bean
	public DataSource dataSource() throws StreamReadException, DatabindException, IOException {
		Map<String, String> dbCredentials = new HashMap<String, String>();

		if (isDBLocal) {

			File file = new File("C:\\Java_Config_Files\\db_crd.json");

			// Create ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();

			// Read JSON into a Map
			Map<String, String> dbCredentialfromFile = objectMapper.readValue(file, Map.class);

			// Access values
			String host = dbCredentialfromFile.get("host");
			String username = dbCredentialfromFile.get("username");
			String password = dbCredentialfromFile.get("password");
			String databaseName = dbCredentialfromFile.get("database");

			dbCredentials.put("host", host);

			dbCredentials.put("username", username);
			dbCredentials.put("password", password);
			dbCredentials.put("dbInstanceIdentifier", databaseName);

		} else {
			dbCredentials = getDatabaseCredentials();

			if (dbCredentials == null || dbCredentials.isEmpty()) {
				throw new RuntimeException("Failed to retrieve database credentials.");
			}
		}

		String jdbcUrl = String.format("jdbc:mysql://%s/%s?enabledTLSProtocols=TLSv1.2", dbCredentials.get("host"),
				dbCredentials.get("dbInstanceIdentifier"));

		

		DataSourceBuilder<?> dataSource = DataSourceBuilder.create();
		dataSource.driverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.url(jdbcUrl);
		dataSource.username(dbCredentials.get("username"));
		dataSource.password(dbCredentials.get("password"));
		return dataSource.build();
	}

	private Map<String, String> getDatabaseCredentials() {
		SecretsManagerClient secretsClient = null;
		try {
			logger.info("Initializing SecretsManagerClient...");

			secretsClient = SecretsManagerClient.builder().region(REGION).build();

			logger.info("SecretsManagerClient initialized.");
			GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();

			logger.info("Sending request to Secrets Manager...");
			GetSecretValueResponse getSecretValueResponse = secretsClient.getSecretValue(getSecretValueRequest);
			logger.info("Received response from Secrets Manager.");

			String secretString = getSecretValueResponse.secretString();

			if (secretString == null || secretString.isEmpty()) {
				throw new RuntimeException("Secret string is empty or null.");
			}

			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(secretString, new TypeReference<Map<String, String>>() {
			});

		} catch (Exception e) {
			logger.info("Error occurred while retrieving database credentials: " + e.getMessage());
			e.printStackTrace();
			return null;
		} finally {
			if (secretsClient != null) {
				secretsClient.close();
			}
		}
	}

}