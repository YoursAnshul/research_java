package com.dimetyd.bot.model;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class CBAgreementInvoiceDetails {

	// @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	
	private Integer requestId;
	public Integer getRequestId() {
		return requestId;
	}
	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public Long getAgreementID() {
		return agreementID;
	}
	public void setAgreementID(Long agreementID) {
		this.agreementID = agreementID;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
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
	public String getPOASIN() {
		return POASIN;
	}
	public void setPOASIN(String pOASIN) {
		POASIN = pOASIN;
	}
	public Integer getQty() {
		return Qty;
	}
	public void setQty(Integer qty) {
		Qty = qty;
	}
	public String getPODataStatus() {
		return PODataStatus;
	}
	public void setPODataStatus(String pODataStatus) {
		PODataStatus = pODataStatus;
	}
	public String getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}
	public Double getNetReceipts() {
		return netReceipts;
	}
	public void setNetReceipts(Double netReceipts) {
		this.netReceipts = netReceipts;
	}
	public Double getRebate() {
		return rebate;
	}
	public void setRebate(Double rebate) {
		this.rebate = rebate;
	}
	public String getDistributor() {
		return distributor;
	}
	public void setDistributor(String distributor) {
		this.distributor = distributor;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getInvoiceDataStatus() {
		return invoiceDataStatus;
	}
	public void setInvoiceDataStatus(String invoiceDataStatus) {
		this.invoiceDataStatus = invoiceDataStatus;
	}
	public String getUniqueKey() {
		return uniqueKey;
	}
	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	public String getReceiveDate() {
		return receiveDate;
	}
	public void setReceiveDate(String receiveDate) {
		this.receiveDate = receiveDate;
	}
	public String getProductGroup() {
		return productGroup;
	}
	public void setProductGroup(String productGroup) {
		this.productGroup = productGroup;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getPoCurrency() {
		return poCurrency;
	}
	public void setPoCurrency(String poCurrency) {
		this.poCurrency = poCurrency;
	}
	private String vendorId;
	private Long agreementID;
	private String invoiceNumber;
	private String PO;
	private String ASIN;
	private String POASIN;
	private Integer Qty;
	private String PODataStatus;
	private String transactionType;
	private Double netReceipts;
	private Double rebate;
	private String distributor;
	private String currency;
	private Date createdDate;
	private String comments;
	private String invoiceDataStatus;
	private String uniqueKey;
	private String receiveDate;
	private String productGroup;
	private String category;
	private String subCategory;
	private String manufacturer;
	private String poCurrency;
	private Double revisedInvoiceQty;
	public Double getRevisedInvoiceQty() {
		return revisedInvoiceQty;
	}
	public void setRevisedInvoiceQty(Double revisedInvoiceQty) {
		this.revisedInvoiceQty = revisedInvoiceQty;
	}


	
}
