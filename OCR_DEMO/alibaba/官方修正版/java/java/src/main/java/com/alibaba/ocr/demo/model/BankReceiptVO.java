package com.alibaba.ocr.demo.model;


import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

//银行收据解析后的可见实体
public class BankReceiptVO {

    private final static String msg = "重要提示：人为的遮挡、涂抹，以及重要数据上面的盖章，收据模板的变更，将会导致识别的误差！！！";

    //银行名称
    @ColumnWidth(15)
    @ExcelProperty(index = 0,value = {msg,"银行收据信息", "银行名称"})
    private String bankName;

    //截至日期  先存成String 等导出到excel中再格式化
    @ColumnWidth(20)
    @ExcelProperty(index = 1,value = {msg,"银行收据信息", "截至日期"})
    private String deadline;

    //账户名称/单位名称/客户名称
    @ColumnWidth(30)
    @ExcelProperty(index = 2,value = {msg,"银行收据信息", "账户名称"})
    private String accountName;

    //账号
    @ColumnWidth(30)
    @ExcelProperty(index = 3,value = {msg,"银行收据信息", "账号"})
    private String accountNum;

    //账户类别
    @ColumnWidth(15)
    @ExcelProperty(index = 4,value = {msg,"银行收据信息", "账户类别"})
    private String accountType;

    //账户余额
    @ColumnWidth(30)
    @ExcelProperty(index = 5,value = {msg,"银行收据信息", "账户余额"})
    private String accountBalance;



    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = Objects.isNull(bankName)? StringUtils.EMPTY:bankName;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = Objects.isNull(deadline)? StringUtils.EMPTY:deadline;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = Objects.isNull(accountName)? StringUtils.EMPTY:accountName;
    }

    public String getAccountNum() {
        return accountNum;
    }

    public void setAccountNum(String accountNum) {
        this.accountNum = Objects.isNull(accountNum)? StringUtils.EMPTY:accountNum;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = Objects.isNull(accountType)? StringUtils.EMPTY:accountType;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String accountBalance) {
        this.accountBalance = Objects.isNull(accountBalance)? StringUtils.EMPTY:accountBalance;
    }
}
