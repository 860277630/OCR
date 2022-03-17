package com.example.demo.Controller;

import com.example.demo.enums.ScanInputType;
import com.example.demo.service.ScanTypeDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/ocr")
public class TestController {


    @Autowired
    private ScanTypeDictService ScanTypeDictService;



    @PostMapping("/test")
    public String createAccountFieldType() {
        log.info("you have comeing");
        return "come in";
    }

    //只返回json格式
    //做成多线程的  等待时间控制在一分钟以内
    @PostMapping("analysisPdf")
    public String analysisPdf(@RequestParam("file") MultipartFile file,
                              @RequestParam("scanType") ScanInputType scanInputType){
        return   ScanTypeDictService.GetPdfData(file,scanInputType);
    }

}
