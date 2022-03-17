package com.example.demo.Test.model;

//PDF标题  枚举类
public enum TitleEnum {
    KHXZH("01","客户询证函");
    private String code;
    private String value;

    TitleEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
