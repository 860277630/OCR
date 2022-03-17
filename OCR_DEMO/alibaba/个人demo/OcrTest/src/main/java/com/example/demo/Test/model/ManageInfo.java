package com.example.demo.Test.model;

//这个是用来管理  实体的解析  保证  数据能成功的被录入进来
public class ManageInfo {
    //当发现页面的标志时  记录该标志  并且进行解析
    private TitleEnum title;
    //开关
    private String switchMark;
    //存储临时变量String类型
    private String tempStr;

    public ManageInfo(TitleEnum title, String switchMark, String tempStr) {
        this.title = title;
        this.switchMark = switchMark;
        this.tempStr = tempStr;
    }

    public TitleEnum getTitle() {
        return title;
    }

    public void setTitle(TitleEnum title) {
        this.title = title;
    }

    public String getSwitchMark() {
        return switchMark;
    }

    public void setSwitchMark(String switchMark) {
        this.switchMark = switchMark;
    }

    public String getTempStr() {
        return tempStr;
    }

    public void setTempStr(String tempStr) {
        this.tempStr = tempStr;
    }
}
