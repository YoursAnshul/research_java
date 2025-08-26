package com.dimetyd.bot.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import com.dimetyd.bot.model.MailDetails;

@Component
@Configuration
public class MailConfig {

	@Autowired
	JdbcTemplate jdbcTemplate;

	StringBuilder sql = new StringBuilder();

    @Bean
    JavaMailSender javaMailSender() {

		sql.append("SELECT `host`,`port`,`username`,`password` FROM `BotEmailDetails` WHERE id = 1");

		List<MailDetails> vcList = this.jdbcTemplate.query(sql.toString(), new RowMapper<MailDetails>() {
			public MailDetails mapRow(ResultSet rs, int rowNum) throws SQLException {

				MailDetails vc = new MailDetails();

				vc.setUsername(rs.getString("username"));
				vc.setPassword(rs.getString("password"));
				vc.setPort(rs.getInt("port"));
				vc.setHost(rs.getString("host"));

				return vc;
			}

		}, new Object[] {});

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(vcList.get(0).getHost()); // Set SMTP server
		mailSender.setPort(vcList.get(0).getPort()); // Set SMTP port (587 is commonly used for TLS)
		mailSender.setUsername(vcList.get(0).getUsername()); // Set the email account username
		mailSender.setPassword(vcList.get(0).getPassword()); // Set the email account password

		// Optional properties
		mailSender.getJavaMailProperties().put("mail.smtp.auth", "true");
		mailSender.getJavaMailProperties().put("mail.smtp.starttls.enable", "true");

		return mailSender;
	}

}
