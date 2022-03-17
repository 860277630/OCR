package com.example.demo.Test.model;

import lombok.Data;

import java.util.List;

@Data
public class BankResult {
    private final static String msg = "重要提示：人为的遮挡、涂抹，以及重要数据上面的盖章，收据模板的变更，将会导致识别的误差！！！";
    //提取出的有效数据
    private List<BankReceiptVO> dataList;
    //提取出的问题页面
    private List<Integer> errorPage;
    //提示信息
    private String tipMsg;
    //重写入参构造
    public BankResult(){
        this.tipMsg  = msg;
    }

}
