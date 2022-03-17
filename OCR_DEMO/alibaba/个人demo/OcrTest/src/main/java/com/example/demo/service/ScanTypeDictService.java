package com.example.demo.service;

import com.example.demo.enums.ScanInputType;
import org.springframework.web.multipart.MultipartFile;

public interface ScanTypeDictService {

    //PDF  的转化
    String GetPdfData(MultipartFile file, ScanInputType scanInputType);
}
