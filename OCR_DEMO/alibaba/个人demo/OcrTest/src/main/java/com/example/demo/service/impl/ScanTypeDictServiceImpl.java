package com.example.demo.service.impl;


import com.example.demo.config.AliyunOcrConfig;
import com.example.demo.config.AliyunOcrRequest;
import com.example.demo.enums.ScanInputType;
import com.example.demo.service.ScanTypeDictService;
import com.example.demo.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class ScanTypeDictServiceImpl  implements ScanTypeDictService {


    @Autowired
    private AliyunOcrRequest request;

    @Autowired
    private AliyunOcrConfig aliyunOcrConfig;



    @Override
    public String GetPdfData(MultipartFile file, ScanInputType scanInputType) {
        String result = null;
        if(scanInputType == ScanInputType.BANK_RECEIPT){
            result = FileUtils.GetPdfData(file,request,aliyunOcrConfig);
        }
        return   result;

    }
}
