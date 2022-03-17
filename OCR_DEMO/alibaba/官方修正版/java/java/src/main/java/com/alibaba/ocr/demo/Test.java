package com.alibaba.ocr.demo;

import com.alibaba.ocr.demo.model.TableNode;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        Map<String,String> map = new HashMap<>();
        String s = "2021年02月02日";
        String s1 = "oooooooo";
        String s2 = "787988787982021/02/02";
        String s3 = "854545646546452021年02月02日";
        String regx = "\\d{4}-\\d{2}-\\d{2}";
        String regx1 = "\\d{4}年\\d{2}月\\d{2}日";
        String regx2 = ".+?\\d.+?";
        List<String> list = new ArrayList<>();
        System.out.println((s1.matches(regx2)));
        System.out.println(s.contains("2"));
        System.out.println(list.isEmpty());
        System.out.println(map.get("key"));
        //System.out.println(list.get(-1));
        System.out.println(Integer.valueOf('1'));

        System.out.println("==================替换字符串第N个字符===========================");
        double f = (double) (1*100)/3;
        // 四舍五入
        BigDecimal value = new BigDecimal(f).setScale(2,BigDecimal.ROUND_HALF_UP);
        // 不足两位小数补0
        DecimalFormat decimalFormat = new DecimalFormat("0.00#");
        String strVal = decimalFormat.format(value);
        System.out.println(strVal);

    }


}
