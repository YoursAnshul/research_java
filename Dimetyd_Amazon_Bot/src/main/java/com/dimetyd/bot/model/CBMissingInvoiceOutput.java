package com.dimetyd.bot.model;

public class CBMissingInvoiceOutput {

	private Long id;
	private String vendorId;
	private String vendorName;
	private String PO;
	private String ASIN;
	private int quantityReceived;
	private int quantityInvoiced;
	private int needToInvoicedQty;
	private String invoiceNumber;
	private double unitCost;
	private double total;
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	public Integer getRetry() {
		return retry;
	}

	public void setRetry(Integer retry) {
		this.retry = retry;
	}

	public String getBusinessUnit() {
		return businessUnit;
	}

	public void setBusinessUnit(String businessUnit) {
		this.businessUnit = businessUnit;
	}

	private Integer retry;
	private String businessUnit;
	private Double amountFromDb;
	
	public Double getAmountFromDb() {
		return amountFromDb;
	}

	public void setAmountFromDb(Double amountFromDb) {
		this.amountFromDb = amountFromDb;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getPO() {
		return PO;
	}

	public void setPO(String pO) {
		PO = pO;
	}

	public String getASIN() {
		return ASIN;
	}

	public void setASIN(String aSIN) {
		ASIN = aSIN;
	}

	public int getQuantityReceived() {
		return quantityReceived;
	}

	public void setQuantityReceived(int quantityReceived) {
		this.quantityReceived = quantityReceived;
	}

	public int getQuantityInvoiced() {
		return quantityInvoiced;
	}

	public void setQuantityInvoiced(int quantityInvoiced) {
		this.quantityInvoiced = quantityInvoiced;
	}

	public int getNeedToInvoicedQty() {
		return needToInvoicedQty;
	}

	public void setNeedToInvoicedQty(int needToInvoicedQty) {
		this.needToInvoicedQty = needToInvoicedQty;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public double getUnitCost() {
		return unitCost;
	}

	public void setUnitCost(double unitCost) {
		this.unitCost = unitCost;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

}
