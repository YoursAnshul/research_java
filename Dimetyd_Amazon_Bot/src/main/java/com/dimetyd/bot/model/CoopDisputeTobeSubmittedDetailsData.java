package com.dimetyd.bot.model;

public class CoopDisputeTobeSubmittedDetailsData {
    
    private String agreementId;
    private String ASIN;
    private String PO;
    private int amazonBilledQty;
    private int vendorInvQty;
    private int excessUnitsBilled;
    private double excessNetReceipts;
    private double overbilledRebate;
    private String agreementCurrency;

    // Getter and Setter for agreementId
    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    // Getter and Setter for ASIN
    public String getASIN() {
        return ASIN;
    }

    public void setASIN(String ASIN) {
        this.ASIN = ASIN;
    }

    // Getter and Setter for PO
    public String getPO() {
        return PO;
    }

    public void setPO(String PO) {
        this.PO = PO;
    }

    // Getter and Setter for amazonBilledQty
    public int getAmazonBilledQty() {
        return amazonBilledQty;
    }

    public void setAmazonBilledQty(int amazonBilledQty) {
        this.amazonBilledQty = amazonBilledQty;
    }

    // Getter and Setter for vendorInvQty
    public int getVendorInvQty() {
        return vendorInvQty;
    }

    public void setVendorInvQty(int vendorInvQty) {
        this.vendorInvQty = vendorInvQty;
    }

    // Getter and Setter for excessUnitsBilled
    public int getExcessUnitsBilled() {
        return excessUnitsBilled;
    }

    public void setExcessUnitsBilled(int excessUnitsBilled) {
        this.excessUnitsBilled = excessUnitsBilled;
    }

    // Getter and Setter for excessNetReceipts
    public double getExcessNetReceipts() {
        return excessNetReceipts;
    }

    public void setExcessNetReceipts(double excessNetReceipts) {
        this.excessNetReceipts = excessNetReceipts;
    }

    // Getter and Setter for overbilledRebate
    public double getOverbilledRebate() {
        return overbilledRebate;
    }

    public void setOverbilledRebate(double overbilledRebate) {
        this.overbilledRebate = overbilledRebate;
    }

    // Getter and Setter for agreementCurrency
    public String getAgreementCurrency() {
        return agreementCurrency;
    }

    public void setAgreementCurrency(String agreementCurrency) {
        this.agreementCurrency = agreementCurrency;
    }

}

