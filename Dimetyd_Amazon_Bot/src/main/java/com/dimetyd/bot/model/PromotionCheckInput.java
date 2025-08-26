package com.dimetyd.bot.model;

import org.springframework.stereotype.Component;

@Component
public class PromotionCheckInput {
 public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPromotionId() {
		return promotionId;
	}
	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}
private String id;
 private String promotionId;
 private String vendorId;
 public String getVendorId() {
	return vendorId;
}
public void setVendorId(String vendorId) {
	this.vendorId = vendorId;
}
public String getVendorName() {
	return vendorName;
}
public void setVendorName(String vendorName) {
	this.vendorName = vendorName;
}
private String vendorName;
 
}
