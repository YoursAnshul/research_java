package com.dimetyd.bot.process;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendMailToClient(String sendTo, String sendCC, String subject, String body) {
		try {

			// Creating a simple mail message
			SimpleMailMessage mailMessage = new SimpleMailMessage();

			// Setting up necessary details
			mailMessage.setFrom("botreport@dimetyd.com");
			mailMessage.setTo(sendTo);
			mailMessage.setCc(sendCC);
			mailMessage.setText(body);
			mailMessage.setSubject(subject);

			// Sending the mail
			javaMailSender.send(mailMessage);
			logger.info("Mail Sent Successfully...");
		}

		// Catch block to handle the exceptions
		catch (Exception e) {

			logger.error("Error while Sending Mail");
		}
	}

	public void sendMailToClientWithAttchment(String [] sendTo, String [] sendCC, String subject, String body,
			File attachment) {
		try {
			// Create a MimeMessage using the javaMailSender instance
			MimeMessage message = javaMailSender.createMimeMessage();

			// 'true' indicates the multipart flag (enables attachments)
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			// Set up necessary email details
			helper.setFrom("botreport@dimetyd.com");
			helper.setTo(sendTo);
			helper.setCc(sendCC);
			helper.setSubject(subject);
			helper.setText(body, false); // Set false if your text is not HTML

			// Check if the attachment exists and add it to the email
			if (attachment != null && attachment.exists()) {
				helper.addAttachment(attachment.getName(), attachment);
			}

			// Send the email
			javaMailSender.send(message);
			logger.info("Mail Sent Successfully with attachment.");
		} catch (Exception e) {
			logger.error("Error while Sending Mail", e);
		}
	}

}
