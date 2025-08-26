package com.dimetyd.bot.model;

public class ShippmentDataOutput {
	private String pO;
	private String aSIN;
	private String pickupDate;
	private int qty;
	private String carrier;
	private String trackingId;
	private String uniqueKey;
	public String getpO() {
		return pO;
	}
	public void setpO(String pO) {
		this.pO = pO;
	}
	public String getaSIN() {
		return aSIN;
	}
	public void setaSIN(String aSIN) {
		this.aSIN = aSIN;
	}
	public String getPickupDate() {
		return pickupDate;
	}
	public void setPickupDate(String pickupDate) {
		this.pickupDate = pickupDate;
	}
	public int getQty() {
		return qty;
	}
	public void setQty(int qty) {
		this.qty = qty;
	}
	public String getCarrier() {
		return carrier;
	}
	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}
	public String getTrackingId() {
		return trackingId;
	}
	public void setTrackingId(String trackingId) {
		this.trackingId = trackingId;
	}
	public String getUniqueKey() {
		return uniqueKey;
	}
	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	
}
