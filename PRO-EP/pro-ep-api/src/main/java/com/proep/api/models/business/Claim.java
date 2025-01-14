package com.proep.api.models.business;

public class Claim {
	private String Type;
	private String Value;
	
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getValue() {
		return Value;
	}
	public void setValue(String value) {
		Value = value;
	}
	
	public Claim() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Claim(String type, String value) {
		super();
		Type = type;
		Value = value;
	}
	
}
