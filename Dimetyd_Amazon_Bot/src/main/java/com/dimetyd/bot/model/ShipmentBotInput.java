package com.dimetyd.bot.model;

public class ShipmentBotInput {

	private long id;
	private String aSN;
	private String vendorId;
	private String vendorName;
	private String aRN;
	private String pOs;
	private String aSNStatus;
	private String date_Details;
	private String uniqueKey;
	private String shipfrom;
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getaSN() {
		return aSN;
	}

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

	public void setaSN(String aSN) {
		this.aSN = aSN;
	}

	public String getaRN() {
		return aRN;
	}

	public void setaRN(String aRN) {
		this.aRN = aRN;
	}

	public String getpOs() {
		return pOs;
	}

	public void setpOs(String pOs) {
		this.pOs = pOs;
	}

	public String getaSNStatus() {
		return aSNStatus;
	}

	public void setaSNStatus(String aSNStatus) {
		this.aSNStatus = aSNStatus;
	}

	public String getDate_Details() {
		return date_Details;
	}

	public void setDate_Details(String date_Details) {
		this.date_Details = date_Details;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public String getShipfrom() {
		return shipfrom;
	}

	public void setShipfrom(String shipfrom) {
		this.shipfrom = shipfrom;
	}

}
