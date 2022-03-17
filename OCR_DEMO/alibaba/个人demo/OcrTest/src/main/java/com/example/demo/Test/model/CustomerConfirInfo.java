package com.example.demo.Test.model;

//客户询证实体  table内容
public class CustomerConfirInfo {

    //客户名称
    private String customerName;
    //存款类型
    private String depositType;
    //账号
    private String accountNum;
    //xxx年xx月xx日金额
    private String dateSumMoney;
    //年利率
    private String annualRates;
    //币种
    private String currency;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDepositType() {
        return depositType;
    }

    public void setDepositType(String depositType) {
        this.depositType = depositType;
    }

    public String getAccountNum() {
        return accountNum;
    }

    public void setAccountNum(String accountNum) {
        this.accountNum = accountNum;
    }

    public String getDateSumMoney() {
        return dateSumMoney;
    }

    public void setDateSumMoney(String dateSumMoney) {
        this.dateSumMoney = dateSumMoney;
    }

    public String getAnnualRates() {
        return annualRates;
    }

    public void setAnnualRates(String annualRates) {
        this.annualRates = annualRates;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "CustomerConfir{" +
                "customerName='" + customerName + '\'' +
                ", depositType='" + depositType + '\'' +
                ", accountNum='" + accountNum + '\'' +
                ", dateSumMoney='" + dateSumMoney + '\'' +
                ", annualRates='" + annualRates + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
