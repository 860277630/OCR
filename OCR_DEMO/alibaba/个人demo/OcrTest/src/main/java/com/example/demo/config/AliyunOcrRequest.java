package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.ocr.request")
public class AliyunOcrRequest {
    private String host;
    private String path;
    private String appcode;
    private String method;
}
