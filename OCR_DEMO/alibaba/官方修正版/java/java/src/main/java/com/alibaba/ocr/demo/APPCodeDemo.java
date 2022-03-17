package com.alibaba.ocr.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.ocr.demo.model.*;
import com.aliyun.api.gateway.demo.util.HttpUtils;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;




/**
 *
 * 不能转化成word  因为图片太多  容易使表格串行  只能进行OCR识别
 *
 * 使用APPCODE进行云市场ocr服务接口调用
 */

public class APPCodeDemo {

    private static ManageInfo info = new ManageInfo(null,"","");

    private static List<List<TableNode>> listNodes = new ArrayList<>();

    /*
     * 获取参数的json对象
     */
    public static JSONObject getParam(int type, String dataValue) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("dataType", type);
            obj.put("dataValue", dataValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static void main(String[] args) throws IOException {
        //存储变量
        CustomerConfir confir = new CustomerConfir();

        //先将PDF转为图片  网上有很多办法  这种办法是免费的 针对纯图片的  PDF没有问题 但是  可编辑的PDF有时会失真  需要其他方法
        //对每页进行图片格式的转化   然后转base 64  进行识别即可
        String filePath = "D:\\1.pdf";
        String host = "https://tysbgpu.market.alicloudapi.com";
        String path = "/api/predict/ocr_general";
        String appcode = "3df43acfef7c4de6959b100dcf29dc12";
        String method = "POST";


        // 拼装请求body的json字符串
        JSONObject requestObj = new JSONObject();
        File file = new File(filePath);
        //请根据线上文档修改configure字段
        JSONObject configObj = new JSONObject();
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> headers = new HashMap<String, String>();

        configObj.put("side", "face");
        String config_str = configObj.toString();
        //            configObj.put("min_size", 5);
        //            String config_str = "";

        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359d73e9498385570ec139105
        headers.put("Authorization", "APPCODE " + appcode);
        try {
            PDDocument doc = PDDocument.load(file);
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            String imgBase64 = "";
            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 296);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", stream);
                //在这里进行图片的转换  以及OCR识别
                byte[] bytes = stream.toByteArray();
                BASE64Encoder encoder = new BASE64Encoder();
                imgBase64 = encoder.encodeBuffer(bytes).trim();
                try {
                    requestObj.put("image", imgBase64);
                    if(config_str.length() > 0) {
                        requestObj.put("configure", config_str);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String bodys = requestObj.toString();

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
                    int stat = response.getStatusLine().getStatusCode();
                    if(stat != 200){
                        System.out.println("Http code: " + stat);
                        System.out.println("http header error msg: "+ response.getFirstHeader("X-Ca-Error-Message"));
                        System.out.println("Http body error msg:" + EntityUtils.toString(response.getEntity()));
                        return;
                    }

                    String res = EntityUtils.toString(response.getEntity());
                    JSONObject res_obj = JSON.parseObject(res);
                    Iterator<Map.Entry<String, Object>> iterator = res_obj.entrySet().iterator();
                    while (iterator.hasNext()){
                        Map.Entry<String, Object> next = iterator.next();
                        //然后将map转为  实体类型
                        ResultModel model = JSON.parseObject(JSON.toJSONString(next), ResultModel.class);
                        //然后将不空的值提取出来进行输出
                        if (Objects.nonNull(model.getRet())){
                            File fileText = new File("D:\\第" + i + "页PDF内容.txt"); // 创建文件对象
                            FileWriter fileWriter = new FileWriter(fileText); // 向文件写入对象写入信息
                            for (RectVal val : model.getRet()) {
                                String s = val.getWord().trim();
                                //System.out.println(val.getWord());
                                //然后把相应的值存入到 文本文件中  在同一行的写在同一行
                                //目的是抽取表格中的东西  所以有严格的行列意识  即
                                //"angle" : 0,   #文字区域角度
                                //"left" : 50,   #文字区域左上角x坐标
                                //"top" : 50,   #文字区域左上角y坐标
                                //"width" : 100,  #文字区域宽度
                                //"height" : 40     #文字区域高度
                                //因为是不同文件的扫描结果  所以  不同文件的表格间距都不同  具体的常见差距和允许误差  见文件

                                //发现关键字  并进行提取
                                if(s.startsWith(TitleEnum.KHXZH.getValue())){
                                    //把关键字存进去
                                    info.setTitle(TitleEnum.KHXZH);
                                } else if (info.getTitle() == TitleEnum.KHXZH) {
                                    //然后进行相关数据的提取
                                    if (s.startsWith("函证编号")) {
                                        confir.setCorrespondenceNo(getStr(s));
                                    }
                                    //从回函地址开始  抽取表格及标题的信息
                                    else if(s.startsWith("回函地址")){
                                        //抽取回函地址  并且打开开关
                                        confir.setAddress(getStr(s));
                                        info.setSwitchMark("HHDZ");
                                    }
                                    //然后就开始抽取 联系人等信息
                                    else if(info.getSwitchMark().equals("HHDZ")){
                                        if(s.startsWith("联系人")){
                                            confir.setContacts(getStr(s));
                                        }
                                        else if(s.startsWith("电话")){
                                            confir.setTelNum(getStr(s));
                                        }
                                        else if(s.startsWith("邮编")){
                                            confir.setPostcode(getStr(s));
                                            //然后关闭开关
                                            info.setSwitchMark("");
                                        }
                                    }
                                    else if(s.contains("贵公司在本银行存款信息列示如下")){
                                        info.setSwitchMark("GGSZBYH");
                                    }
                                    //然后开始抽取表格中的内容
                                    else if(info.getSwitchMark().equals("GGSZBYH")){
                                        //第一行是表头  我们需要记录下纵坐标的位置  以及单列行的横坐标位置
                                        //列中点 误差控制在  2%以内   行中点  误差控制在  0.3%以内
                                        //直到遇到结束标识符  统计结束  本次表格的结束标识符是“合计”
                                        //第一行数据  是表头  不用于进行统计
                                        //思路是建立一个二维数组  进行初始的统计
                                        //首先是比较中点的纵坐标   误差在2%以外   就会认为是另外一个坐标
                                        //然后当在2%以内  首先查看hight  如果上面的除以下面的约等于2 并且小于等于行的个数
                                        //就会认为是同一列  并且得出该行的  纵坐标
                                        //遇到合计结束  并且恢复所有的开关
                                        if(s.contains("合计")){
                                            info.setSwitchMark("HJ");
                                            //到了这里说明表格提取已经完毕  然后就可以
                                            fillEntity(confir);
                                        }else{
                                            analysisData(val,6);
                                        }
                                    }
                                    else if(info.getSwitchMark().equals("HJ")){
                                        //如果是合计的话 就提取下一个  字符  然后  关闭总开关
                                        confir.setSumMoney(s);
                                        info.setTitle(null);
                                        //到这里该PDF取值完毕
                                        listNodes.clear();
                                        System.out.println(confir.toString());
                                    }

                                }
                                //将解析后的结果写入文件中
                                fileWriter.write("("+val.getRect().toString()+")"+"==="+val.getWord()+"\n"); // 写文件
                            }
                            fileWriter.close();
                            //然后清空全局变量
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //私有的  抽取冒号后的方法
    private static String getStr(String s){
        //中文的冒号
        String[] split = s.split("：");
        if(split.length == 2){
            return split[1];
        }
        return null;
    }

    //装填实体
    private static void fillEntity(CustomerConfir cus){
        List<CustomerConfirInfo> infos = new ArrayList<>();
        //如果list行数小于 等于1  就直接返回
        if(listNodes.size()<=1){
            return ;
        }
        //list的第一行  不要  因为这是标题  从第二行开始  按照 按照纵坐标进行录入
        //先进行排序
        for (int order = 1; order < listNodes.size(); order++) {
            CustomerConfirInfo info = new CustomerConfirInfo();
            List<TableNode> list = listNodes.get(order);
            //对list按照横坐标 从小到大排序
            list.sort(Comparator.comparing(TableNode::getxAxis));
            //排序之后就进行填充
            for (int num = 0; num < list.size(); num++) {
                switch (num){
                    case 0:
                        info.setCustomerName(list.get(num).getVal());
                    case 1:
                        info.setDepositType(list.get(num).getVal());
                    case 2:
                        info.setAccountNum(list.get(num).getVal());
                    case 3:
                        info.setDateSumMoney(list.get(num).getVal());
                    case 4:
                        info.setAnnualRates(list.get(num).getVal());
                    case 5:
                        info.setCurrency(list.get(num).getVal());

                }
            }
            infos.add(info);
        }
        cus.setTableInfo(infos);

    }

    /**
     *表格内容统计分析
     * @param val
     * @param paramNum  列元素的个数
     * @return
     */
    private static void   analysisData(RectVal val,Integer paramNum){
        //这里是用来记录下标值
        Integer tempNum = null;

        //算出  横纵坐标  以及内部包含的值
        TableNode data = getData(val);

        //拿到横纵坐标  以及值之后  先和该list的其他值进行比较
        //如果list没满，且X坐标和其他不同，那么直接往里面塞
        //如果list满了，就遍历该list，检查是否存在和该新值在误差范围内同一列 （不在同一列则直接舍弃）
        //并且和该列的纵坐标加起来  平均值至少和list中一个元素相同   否则就创建新列

        //如果是空的  就新建一个塞进去  如果不是空的就进行遍历
        if(listNodes.size() == 0){
            List<TableNode> list = new ArrayList<>();
            list.add(data);
            listNodes.add(list);
        }else{
        List<TableNode> nodes = listNodes.get(listNodes.size() - 1);
        //如果list没满  就直接塞进去
        if(nodes.size()<paramNum){nodes.add(data);}
        else {
            //满了的话  就进行遍历  找到和同一列的数据  进行高度的判断
            for (int num = 0; num < nodes.size(); num++) {
                //如果找到相等的就直接break;
                //横坐标的误差范围是0.02
                if (judgeNode(nodes.get(num), data, 0.02, Boolean.TRUE)) {
                    tempNum = num;
                    break;
                }
            }
            //找到num后  取出对应的值  进行高度的判断  如果是该列表中值的一半  就进行合并  如果不是  就创建新列
            if(Objects.nonNull(tempNum)){
                if (sameColumn(nodes,data,tempNum)) {
                    //如果是true的话  就把新值和旧值进行融合
                    TableNode node = nodes.get(tempNum);
                    node.setVal(node.getVal()+data.getVal());
                }else{
                    //如果是false的话  就新建一列  然后将该值放入
                    List<TableNode> list = new ArrayList<>();
                    list.add(data);
                    listNodes.add(list);
                }
            }
        }}

    }
    private static TableNode getData(RectVal val){
        TableNode node = new TableNode();
        Integer left = val.getRect().getLeft();
        Integer width = val.getRect().getWidth();
        Integer top = val.getRect().getTop();
        Integer height = val.getRect().getHeight();
        node.setVal(val.getWord());
        //横坐标就是left + width/2
        node.setxAxis(left+(float)width/2);
        //纵坐标是TOP + height/2
        node.setyAxis(top+(float)height/2);
        return node;
    }

    //计算两个node是否在同一行/列  Boolean是 true 表示计算是否在同一列  反之计算同一行    如果是空的话  就判断复合列
    private static Boolean judgeNode(TableNode listNode,TableNode newNode,double error,boolean flag) {
        if (flag) {
            //如果是true  则计算两个列值是否在误差范围内  两个数相差的绝对值初一两个值最小的值
            double result = Math.abs(listNode.getxAxis() - newNode.getxAxis()) / Math.min(listNode.getxAxis(), newNode.getxAxis());
            return result > error ? Boolean.FALSE : Boolean.TRUE;
        } else {
            //如果是false  则计算两个行值是否在误差范围内  两个数相差的绝对值初一两个值最小的值
            double result = Math.abs(listNode.getyAxis() - newNode.getyAxis()) / Math.min(listNode.getyAxis(), newNode.getyAxis());
            return result > error ? Boolean.FALSE : Boolean.TRUE;
        }
    }

    //复合列的判断
    //如果和选定的值 行坐标  加起来的平均值  和该行至少有一个  元素在误差范围内  则进行融合  如果不在  就创建新列  将值存进去
    private static Boolean sameColumn(List<TableNode> list,TableNode newNode,Integer num){
       double average  =  (list.get(num).getyAxis()+newNode.getyAxis())/2;
       //这里传的是引用  不能这么写
        TableNode tempNode = new TableNode();
        tempNode.setyAxis(average);
       //遍历list 循环list找到在误差范围内的值  就返回true 否则 返回false
        for (TableNode node : list) {
            if(judgeNode(node,tempNode,0.003,false))
            {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }




}

