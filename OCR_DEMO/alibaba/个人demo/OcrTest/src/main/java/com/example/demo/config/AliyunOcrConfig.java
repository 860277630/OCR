package com.example.demo.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class AliyunOcrConfig {

    @Value("${aliyun.ocr.config.min_size}")
    private Integer minSize;

    @Value("${aliyun.ocr.config.output_prob}")
    private Boolean outputProb;

    @Value("${aliyun.ocr.config.output_keypoints}")
    private Boolean outputKeypoints;

    @Value("${aliyun.ocr.config.skip_detection}")
    private Boolean skipDetection;

    @Value("${aliyun.ocr.config.without_predicting_direction}")
    private Boolean withoutPredictingDirection;

    @Value("${aliyun.ocr.config.language}")
    private String language;
}
