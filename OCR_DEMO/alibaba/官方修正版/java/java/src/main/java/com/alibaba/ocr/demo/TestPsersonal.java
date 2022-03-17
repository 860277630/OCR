package com.alibaba.ocr.demo;

import com.aliyun.api.gateway.demo.util.HttpUtils;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.codec.binary.Base64.encodeBase64;

public class TestPsersonal {
    public static void main(String[] args) throws IOException {
        String host = "https://tysbgpu.market.alicloudapi.com";
        String path = "/api/predict/ocr_general";
        String method = "POST";
        String appcode = "3df43acfef7c4de6959b100dcf29dc12";
        String imgFile = "D:\\1.jpg";

        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/json; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        String bodys = "{\"image\":\""+getBase64(imgFile)+"\",\"configure\":{\"min_size\":16,\"output_prob\":true,\"output_keypoints\":false,\"skip_detection\":false,\"without_predicting_direction\":false}}";


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  static  String getBase64(String filePath) throws IOException {

        File file = new File(filePath);
        byte[] content = new byte[(int) file.length()];
        FileInputStream finputstream = new FileInputStream(file);
        finputstream.read(content);
        finputstream.close();
        return new String(encodeBase64(content));
    }

}
