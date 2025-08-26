package com.dimetyd.bot.model;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class CBPromotionalAgreementInvoiceDetails {

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
	public String getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	public String getShipDate() {
		return shipDate;
	}
	public void setShipDate(String shipDate) {
		this.shipDate = shipDate;
	}
	public String getReturnDate() {
		return returnDate;
	}
	public void setReturnDate(String returnDate) {
		this.returnDate = returnDate;
	}
	public String getCostDate() {
		return costDate;
	}
	public void setCostDate(String costDate) {
		this.costDate = costDate;
	}
	public String getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}
	public Integer getQty() {
		return Qty;
	}
	public void setQty(Integer qty) {
		Qty = qty;
	}
	public Double getNetSales() {
		return netSales;
	}
	public void setNetSales(Double netSales) {
		this.netSales = netSales;
	}
	public String getNetSalesCurency() {
		return netSalesCurency;
	}
	
	public void setNetSalesCurency(Double netSales) {
		this.netSales = netSales;
	}
	public double getListPrice() {
		return listPrice;
	}
	public void setListPrice(double listPrice) {
		this.listPrice = listPrice;
	}
	public String getPurchaseOrder() {
		return purchaseOrder;
	}
	
	public double getRebate() {
		return rebate;
	}
	public void setRebate(double rebate) {
		this.rebate = rebate;
	}
	public void setNetSalesCurency(String netSalesCurency) {
		this.netSalesCurency = netSalesCurency;
	}
	public void setPurchaseOrder(String purchaseOrder) {
		this.purchaseOrder = purchaseOrder;
	}
	public String getAsin() {
		return Asin;
	}
	public void setAsin(String asin) {
		Asin = asin;
	}
	public String getUPC() {
		return UPC;
	}
	public void setUPC(String uPC) {
		UPC = uPC;
	}
	public String getEAN() {
		return EAN;
	}
	public void setEAN(String eAN) {
		EAN = eAN;
	}
	public String getManufacturer() {
		return Manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		Manufacturer = manufacturer;
	}
	public String getDistributor() {
		return distributor;
	}
	public void setDistributor(String distributor) {
		this.distributor = distributor;
	}
	public String getProductGroup() {
		return ProductGroup;
	}
	public void setProductGroup(String productGroup) {
		ProductGroup = productGroup;
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getProductDescription() {
		return productDescription;
	}
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	public String getBinding() {
		return Binding;
	}
	public void setBinding(String binding) {
		Binding = binding;
	}
	public String getPromotionId() {
		return promotionId;
	}
	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}
	public String getCostType() {
		return costType;
	}
	public void setCostType(String costType) {
		this.costType = costType;
	}
	public String getOrderCountry() {
		return orderCountry;
	}
	public void setOrderCountry(String orderCountry) {
		this.orderCountry = orderCountry;
	}
	public String getUniqueKey() {
		return uniqueKey;
	}
	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	private String vendorId;
	private Long agreementID;
	private String orderDate;
	private String shipDate;
	private String returnDate;
	
	private String costDate;
	private String transactionType;
	private Integer Qty;
	private Double netSales;
	private String netSalesCurency;
	private double listPrice;
	private double rebate;
	

	private String purchaseOrder;
	private String Asin;
	
	private String UPC;
	private String EAN;
	private String Manufacturer;
	private String distributor;
	private String ProductGroup;
	private String category;
	private String subCategory;
	private String title;
	private String productDescription;
	private String Binding;
	private String promotionId;
	private String costType;
	private String orderCountry;
	private String uniqueKey;


	
}
