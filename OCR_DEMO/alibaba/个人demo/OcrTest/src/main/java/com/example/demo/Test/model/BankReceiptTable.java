package com.example.demo.Test.model;

public class BankReceiptTable {

    //账户名称/单位名称/客户名称
    private String accountName;

    //账号
    private String accountNum;

    //账户类别
    private String accountType;

    //账户余额
    private String accountBalance;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNum() {
        return accountNum;
    }

    public void setAccountNum(String accountNum) {
        this.accountNum = accountNum;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String accountBalance) {
        this.accountBalance = accountBalance;
    }

    @Override
    public String toString() {
        return "BankReceiptTable{" +
                "accountName='" + accountName + '\'' +
                ", accountNum='" + accountNum + '\'' +
                ", accountType='" + accountType + '\'' +
                ", accountBalance='" + accountBalance + '\'' +
                '}';
    }
}
