package com.dimetyd.bot.model;

public class T24CoopRowData {
	


    private String invoiceId;
    private String invoiceDate;
    private String agreementId;
    private String agreementTitle;
    private String fundingType;
    private double originalBalance;

    public void RowData(String invoiceId, String invoiceDate, String agreementId, String agreementTitle, String fundingType, double originalBalance) {
        this.invoiceId = invoiceId;
        this.invoiceDate = invoiceDate;
        this.agreementId = agreementId;
        this.agreementTitle = agreementTitle;
        this.fundingType = fundingType;
        this.originalBalance = originalBalance;
    }

    public String getInvoiceId() { return invoiceId; }
    public String getInvoiceDate() { return invoiceDate; }
    public String getAgreementId() { return agreementId; }
    public String getAgreementTitle() { return agreementTitle; }
    public String getFundingType() { return fundingType; }
    public double getOriginalBalance() { return originalBalance; }



}
