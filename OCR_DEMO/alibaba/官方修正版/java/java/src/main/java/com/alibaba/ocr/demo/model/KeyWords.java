package com.alibaba.ocr.demo.model;

import java.util.HashSet;

//银行收据 提取的关键字
//可以做一个接口  来读取外部文件   将关键字读入
public class KeyWords {


    //提取银行  所需要的关键字  暂时是按照遍历的方式来提取的  如果需要将这个应用到代码中
    public static HashSet<String> banks(){
        HashSet<String> banks = new HashSet<>();
        banks.add("厦门国际银行");
       // banks.add("")
        return null;
    }


}
