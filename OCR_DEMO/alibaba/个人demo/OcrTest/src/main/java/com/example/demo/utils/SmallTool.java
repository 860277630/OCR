package com.example.demo.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * 小工具
 */
public class SmallTool {

    public static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void printTimeAndThread(String tag) {
        SimpleDateFormat sdf = new SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE);
        sdf.applyPattern("yyyy年MM月dd日 HH时mm分ss秒sss毫秒");
        String result = new StringJoiner("\t|\t")
                .add(sdf.format(System.currentTimeMillis()))
                .add(String.valueOf(Thread.currentThread().getId()))
                .add(Thread.currentThread().getName())
                .add(tag)
                .toString();
        System.out.println(result);
    }

}
