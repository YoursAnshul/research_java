package com.dimetyd.bot.helper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlobalSessionName {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
