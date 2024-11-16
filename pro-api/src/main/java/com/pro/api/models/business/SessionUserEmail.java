package com.pro.api.models.business;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(
		value = WebApplicationContext.SCOPE_SESSION,
		proxyMode = ScopedProxyMode.TARGET_CLASS
		)
public class SessionUserEmail {
	private String UserEmail;

	public String getUserEmail() {
		if (UserEmail != null) {
			if (UserEmail == null) {
				UserEmail = "";
			}
		}

		return UserEmail;
	}

	public void setUserEmail(String userEmail) {
		UserEmail = userEmail;
	}

	public SessionUserEmail() {
		super();
	}

	public SessionUserEmail(String userEmail) {
		super();
		UserEmail = userEmail;
	}
		
}
