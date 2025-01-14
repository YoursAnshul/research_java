package com.proep.api.models.business;

public class KeyValuePair {

	private Integer key;
	private String value;

	public KeyValuePair(Integer key, String value) {
		this.key = key;
		this.value = value;
	}

	// Getters and Setters

	public Integer getKey() {
		return key;
	}

	public void setKey(Integer key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
