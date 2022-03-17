package com.example.demo.Test.model;


import java.util.List;

//客户询证函实体
public class CustomerConfir {
    //函证编号
    private String correspondenceNo;

    //回函地址
    private String address;

    //联系人
    private  String contacts;

    //电话
    private String telNum;

    //邮编
    private String postcode;

    //表格数据
    private List<CustomerConfirInfo> tableInfo;

    //表格数据金额合计
    private String sumMoney;

    public String getCorrespondenceNo() {
        return correspondenceNo;
    }

    public void setCorrespondenceNo(String correspondenceNo) {
        this.correspondenceNo = correspondenceNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getTelNum() {
        return telNum;
    }

    public void setTelNum(String telNum) {
        this.telNum = telNum;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public List<CustomerConfirInfo> getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(List<CustomerConfirInfo> tableInfo) {
        this.tableInfo = tableInfo;
    }

    public String getSumMoney() {
        return sumMoney;
    }

    public void setSumMoney(String sumMoney) {
        this.sumMoney = sumMoney;
    }

    @Override
    public String toString() {
        return "CustomerConfir{" +
                "correspondenceNo='" + correspondenceNo + '\'' +"\n"+
                ", address='" + address + '\'' +"\n"+
                ", contacts='" + contacts + '\'' +"\n"+
                ", telNum='" + telNum + '\'' +"\n"+
                ", postcode='" + postcode + '\'' +"\n"+
                ", tableInfo=" + tableInfo +"\n"+
                ", sumMoney='" + sumMoney + '\'' +"\n"+
                '}';
    }
}
