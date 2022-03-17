package com.example.demo.Test.model;

import java.util.List;

//银行收据的存储实体
public class BankReceipt {
    //银行名称
    private String bankName;

    //截至日期  先存成String 等导出到excel中再格式化
    private String deadline;

    //账户名称/单位名称/客户名称
    private String accountName;

    //表格中的数据列表
    private List<BankReceiptTable> tableList;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public List<BankReceiptTable> getTableList() {
        return tableList;
    }

    public void setTableList(List<BankReceiptTable> tableList) {
        this.tableList = tableList;
    }

    @Override
    public String toString() {
        return "BankReceipt{" +
                "bankName='" + bankName + '\'' +
                ", deadline='" + deadline + '\'' +
                ", accountName='" + accountName + '\'' +
                ", tableList=" + tableList +
                '}';
    }
}
