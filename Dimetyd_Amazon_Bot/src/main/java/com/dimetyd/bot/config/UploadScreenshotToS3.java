package com.dimetyd.bot.config;

import java.io.File;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;

@Component
public class UploadScreenshotToS3 {

	@Value("${bucketname}")
	private String bucketName;

	@Autowired
	private AmazonS3 amazonS3;

	public void uploadJobsFile(File file, String filename) {
		try {
			amazonS3.putObject(new PutObjectRequest(bucketName, filename, file));
		} catch (Exception e) {

			System.out.println("Error occurred" + e);

		}
	}

}
