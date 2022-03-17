package com.example.demo.entitys;

import com.example.demo.enums.ScanInputType;
import lombok.Data;

@Data
public class ScanTypeDict {
    private String id;

    private ScanInputType typename;

    private String description;

}