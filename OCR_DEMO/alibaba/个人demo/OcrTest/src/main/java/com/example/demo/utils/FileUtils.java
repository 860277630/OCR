package com.example.demo.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.Test.aliyun.api.gateway.demo.util.HttpUtils;
import com.example.demo.Test.model.*;
import com.example.demo.config.AliyunOcrConfig;
import com.example.demo.config.AliyunOcrRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

//解析PDF的工具类
@Slf4j
public class FileUtils {


    //解析PDF文件
    public static String GetPdfData(MultipartFile file, AliyunOcrRequest request, AliyunOcrConfig aliyunOcrConfig) {
        SmallTool.printTimeAndThread("开始时间");
        String resultStr = StringUtils.EMPTY;
        List<CompletableFuture<BankReceipt>> asyncReslut = null;
        try {
            PDDocument doc = PDDocument.load(file.getInputStream());
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            log.info("总页数为,"+pageCount);
            //多线程完成后进行统一的结果处理
            asyncReslut = new ArrayList<>();
            //线程池  所有的异步操作在该线程池里进行
            ExecutorService executor = ThreadPoolUtil.getThreadPoolExecutor();
            SmallTool.printTimeAndThread("整合开始时间");
            for (int pageNUm = 0; pageNUm < pageCount; pageNUm++) {
                SmallTool.printTimeAndThread("解析开始时间");
                BufferedImage image = renderer.renderImageWithDPI(pageNUm, 296);
                //把每个image对象  放入到异步的多线程中  进行异步操作
                asyncReslut.add(perPageData(image, executor, request, aliyunOcrConfig));
            }
            SmallTool.printTimeAndThread("整合结束时间");
            //处理完后统一进行整合
            CompletableFuture.allOf(asyncReslut.toArray(new CompletableFuture[pageCount]));
            resultStr = getResultJson(asyncReslut);
            doc.close();
            //executor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SmallTool.printTimeAndThread("结束时间");
        return resultStr;
    }

    //对每个页面进行异步操作
    private static CompletableFuture<BankReceipt> perPageData(BufferedImage image,ExecutorService executor,AliyunOcrRequest request,AliyunOcrConfig aliyunOcrConfig){
        CompletableFuture<BankReceipt> result = CompletableFuture.supplyAsync(()->{
            SmallTool.printTimeAndThread("分操作开始时间");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "PNG", stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            SmallTool.printTimeAndThread("分操作转换图片");
            byte[] bytes = stream.toByteArray();
            BASE64Encoder encoder = new BASE64Encoder();
            String imgBase64 = encoder.encodeBuffer(bytes).trim();
            //然后进行请求信息填充
            HttpResponse response = null;
            try {
                response = getAliyunOcr(imgBase64, request, aliyunOcrConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
            SmallTool.printTimeAndThread("分操作服务器申请");
            String res = null;
            try {
                res = EntityUtils.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject res_obj = JSON.parseObject(res);
            Iterator<Map.Entry<String, Object>> iterator = res_obj.entrySet().iterator();
            BankReceipt receipt = new BankReceipt();
            while (iterator.hasNext()){
                Map.Entry<String, Object> next = iterator.next();
                //然后将map转为  实体类型
                ResultModel model = JSON.parseObject(JSON.toJSONString(next), ResultModel.class);
                //然后将不空的值提取出来进行输出
                if (Objects.nonNull(model.getRet())){
                    SmallTool.printTimeAndThread("分操作准备时间");
                    //每一页  提取一次  所以在这里创建变量
                    List<RectVal> vals = new ArrayList<>();
                    List<RectVal> valsList = new ArrayList<>();
                    //用来累计厦门银行特殊的日期字符
                    List<String> deadline = new ArrayList<>();
                    Map<String,String> cutOffDate = new HashMap<>();
                    //用来  开启统计不在一行的日期字段
                    String switchMarkForDeadline = "";
                    String switchMarkForAccountName = "";
                    String switchMarkForTable = "";
                    String switchMarkForTableSelf = "";
                    String switchMarkForAccount = "";
                    for (RectVal val : model.getRet()) {
                        String s = val.getWord().trim().replace(" ", StringUtils.EMPTY).replace(":",StringUtils.EMPTY).replace("：",StringUtils.EMPTY);
                        //在这里进行文件的提取  每一页提取一次
                        //遍历到相关的数据  就进行存储  最后导出到excel中
                        //首先处理  非表格的数据
                        //如果实体已经填满  就跳出循环
                        if(isFilled(receipt)){break;}
                        if(s.contains("银行")&&Objects.isNull(receipt.getBankName())){
                            //提取出来  并加入实体
                            receipt.setBankName(s.substring(0, s.indexOf("银行")+2));
                        }
                        if(s.contains("截止日期")&&Objects.isNull(receipt.getDeadline())&& StringUtils.isEmpty(switchMarkForDeadline)){
                            //提取截至日期
                            deadline.add(s);
                            String time = getTime(deadline,cutOffDate);
                            if(Objects.isNull(time)){
                                //就打开开关  并把该字符串  加入到临时变量中
                                switchMarkForDeadline = "deadline";
                            }else{
                                receipt.setDeadline(time);
                            }
                        }else if(switchMarkForDeadline.equals("deadline")){
                            deadline.add(s);
                            String time = getTime(deadline,cutOffDate);
                            //如果长度超过50  还没找到  就把该位置置为空字符串  直接跳过   其实这里可以做一个redo_log 万一 舍弃的50个字符里面 有需要的信息呢  不过现在还没发生
                            //如果长度小于50  没找到 就继续找 找到了  就关闭开关  并且  把该位置存进去
                            if(Objects.isNull(time)){
                                if(deadline.size()>15){
                                    receipt.setDeadline(StringUtils.EMPTY);
                                    switchMarkForDeadline = StringUtils.EMPTY;
                                }else{
                                    continue;
                                }
                            }else{
                                receipt.setDeadline(time);
                                switchMarkForDeadline = StringUtils.EMPTY;
                            }
                        }
                        if(isKeyWord(s)&&Objects.isNull(receipt.getAccountName())&& StringUtils.isEmpty(switchMarkForAccountName)){
                            //然后抽取对应内容  如果后面的内容  字符超多两个  就直接  截取
                            //反之  就打开开关  收集（包括在内）2个左右的  字符串片段
                            //如果出现了   账号  余额中的任何一个字符   说明  这是一行的数据   （就会进入到表格提取程序）
                            //如果没有出现 账号  余额中的任何一个字符   那就提取它跟随的最近的一个字段作为  内容
                            String s1 = getName(s);
                            if(Objects.isNull(s1)){
                                //那就打开开关  并且将该节点  加入到  list中
                                switchMarkForAccountName = "accountname";
                                vals.add(val);
                            }else{
                                receipt.setAccountName(s1);
                            }

                        }else if(switchMarkForAccountName.equals("accountname")){
                            //如果打开了开关  就考虑  是异行  的  或者是  表格中
                            //不断的收集  传入的字符  直到   2个  出现账号  余额的任何一个字符
                            vals.add(val);
                            String name = getName(vals);
                            //null值  说明是进入表格了
                            //空字符串  说明也没有进入表格  但是还有没到6个字符串
                            //非空非null  说明满足了6个或者找到了关键字  并且返回了对应的值
                            if(StringUtils.isNotEmpty(name)&&Objects.isNull(receipt.getAccountName())){
                                receipt.setAccountName(name);
                                switchMarkForAccountName = StringUtils.EMPTY;
                            }
                            //然后进行map的判断  不为空说明进入了表格的提取
                            //存在key值为accountname  说明是单位名称并没有参与到  表格中
                            //否则单位名称参与到了  表格中
                            if(vals.size()>0&&(!StringUtils.EMPTY.equals(name))){
                                // 说明 表格的解析已经开始了  就继续收集  继续解析数据  直到遇到结束标志
                                // 结束标志往往是  两个或两个连续的  相符  不相符  字段或者单个字符串超过15 ，且不含数字
                                //就关闭  账号名称开关  打开表格的开关
                                switchMarkForAccountName = StringUtils.EMPTY;
                                switchMarkForTable = "table";
                            }
                        }else if(switchMarkForTable.equals("table")&&Objects.isNull(receipt.getTableList())){
                            //继续的收集  继续解析   直到遇到结束标志
                            vals.add(val);
                            if(isTableEnd(vals)){
                                //到了结束标志  就进行解析填充  并且关闭开关
                                receipt.setTableList(getTableData(vals));
                                switchMarkForTable = StringUtils.EMPTY;
                                //然后就是table数据  不为空  就填充  非list中的  数据
                                if(!CollectionUtils.isEmpty(receipt.getTableList())
                                        &&Objects.nonNull(receipt.getTableList().get(0))
                                        &&Objects.nonNull(receipt.getTableList().get(0).getAccountName())){
                                    receipt.setAccountName(receipt.getTableList().get(0).getAccountName());
                                }
                            }
                        }
                        //如果单位名称提取完了  并且没有进入到  表格模式   就需要  继续收集搜寻  找到 表格的所在位置
                        //表格大概是  以账号  开头  并且  6个字段以内  拥有关键字
                        if(switchMarkForAccount.equals("zhang")){
                            if(s.contains("号")){
                                switchMarkForTableSelf = "table";
                                val.setWord("账号");
                            }
                            switchMarkForAccount = StringUtils.EMPTY;
                        }

                        if((!switchMarkForTable.equals("table"))&&Objects.isNull(receipt.getTableList())&&StringUtils.isEmpty(switchMarkForTableSelf)){
                            //如果上面  没有进入表格模式    并且发现了  账号  关键字  就连续  连续收集五个  发现了  关键字  就进入表格模式
                            if(s.contains("账号")){
                                switchMarkForTableSelf = "table";
                                valsList.add(val);
                            }else if(s.contains("账")){
                                switchMarkForAccount  = "zhang";
                            }
                            //账号  两个字  很多情况下  解析出来是分开的


                        }else if(switchMarkForTableSelf.equals("table")&&Objects.isNull(receipt.getTableList())){
                            // 继续收集  发现表格 就进入到表格模式  如果不是  就重置  开关
                            valsList.add(val);
                            if(valsList.size()>4){
                                //校验是否含有  关键字如果含有  就表示  进入到了  表格中解析  如果不含有    则重置开关  重新统计
                                if (isKeyWord(valsList)) {
                                    //那就去找结束标志 找到了  就解析
                                    if(isTableEnd(valsList)){
                                        receipt.setTableList(getTableData(valsList));
                                        switchMarkForTableSelf = StringUtils.EMPTY;
                                        if(!CollectionUtils.isEmpty(receipt.getTableList())
                                                &&Objects.nonNull(receipt.getTableList().get(0))
                                                &&Objects.nonNull(receipt.getTableList().get(0).getAccountName())){
                                            receipt.setAccountName(receipt.getTableList().get(0).getAccountName());
                                        }
                                    }
                                }else{
                                    switchMarkForTableSelf = StringUtils.EMPTY;
                                }
                            }
                        }
                    }
                }
            }
            SmallTool.printTimeAndThread("分操作结束时间");
            return receipt;
        },executor).handle((outCome,exception)->{
            if(Objects.nonNull(outCome)){return outCome;}
            if(Objects.nonNull(exception)){return new BankReceipt();}
            return new BankReceipt();
        });
        return result;
    }

    //组装请求信息请求并返回值
    public static HttpResponse getAliyunOcr(String imgBase64,AliyunOcrRequest request,AliyunOcrConfig aliyunOcrConfig) throws Exception {
        //这里有一个for循环反复加载的问题但是由于对性能影响不大  故  先掠过
        //设置配置项
        JSONObject configObj = new JSONObject();
        configObj.put("min_size", aliyunOcrConfig.getMinSize());//图片中文字的最小高度，单位像素
        configObj.put("output_prob", aliyunOcrConfig.getOutputProb());//是否输出文字框的概率
        configObj.put("output_keypoints", aliyunOcrConfig.getOutputKeypoints());//是否输出文字框角点
        configObj.put("skip_detection", aliyunOcrConfig.getSkipDetection());//是否跳过文字检测步骤直接进行文字识别
        configObj.put("without_predicting_direction", aliyunOcrConfig.getWithoutPredictingDirection());//是否关闭文字行方向预测
        configObj.put("language", aliyunOcrConfig.getLanguage());//当skip_detection为true时，该字段才生效，做单行手写识别。
        String config_str = configObj.toString();

        //header
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359d73e9498385570ec139105
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + request.getAppcode());

        //设置body
        JSONObject requestObj = new JSONObject();
        requestObj.put("image", imgBase64);
        if(config_str.length() > 0) {
            requestObj.put("configure", config_str);
        }
        String bodys = requestObj.toString();

        //设置querys
        Map<String, String> querys = new HashMap<String, String>();

        //进行请求
        HttpResponse response = HttpUtils.doPost(request.getHost(), request.getPath(), request.getMethod(), headers, querys, bodys);

        //进行状态值判断
        int stat = response.getStatusLine().getStatusCode();
        if(stat!=HttpStatus.SC_OK){
            throw new RuntimeException("Http code: " + stat+"\n http header error msg: "
                    + response.getFirstHeader("X-Ca-Error-Message")
                    +"\n Http body error msg:" + EntityUtils.toString(response.getEntity()));
        }

        return response;
    }


    //判断实体是否被填满
    private static boolean isFilled(BankReceipt receipt){
        //填满的判定条件是  所有的属性都不能是null
        if(Objects.isNull(receipt.getAccountName())){
            return Boolean.FALSE;
        }else if(Objects.isNull(receipt.getBankName())){
            return Boolean.FALSE;
        }else if(Objects.isNull(receipt.getDeadline())){
            return Boolean.FALSE;
        }else if(Objects.isNull(receipt.getTableList())){
            return Boolean.FALSE;
        }else{
            return Boolean.TRUE;
        }
    }
    //分析字符串中  是否包含日期信息  并返回日期信息或者null
    //map有3个key值  分别是  year  month  day

    private static String getTime(List<String> s,Map<String,String> map){


        Boolean chineseExpressions = null;
        //日期有两种格式：
        // YYYY年MM月DD日  长度为 11
        // 或者
        // YYYY-MM-DD    长度为 10
        String regx = "\\d{4}-\\d{2}-\\d{2}";
        String regx1 = "\\d{4}年\\d{2}月\\d{2}日";


        //首先是需要  去杂质  将一些不需要的 符号  进行剔除
        for (String str : s) {
            if(str.contains("年")){
                chineseExpressions = Boolean.TRUE;
                break;
            }else if (str.contains("-")){
                chineseExpressions = Boolean.FALSE;
                break;
            }
        }
        //循环完毕之后  如果  还有没  对标志  进行赋值  就  返回  继续收集
        if(Objects.isNull(chineseExpressions)){
            return null;
        }

        //如果确立了值  就进行去杂质  操作

        //通过 ascii  来进行排除

        //-年月日09的  ascii  分别是45 24180 26376 26085 48 57
        Set<Integer> integerSet = new HashSet<>();
        integerSet.add(24180);
        integerSet.add(26376);
        integerSet.add(26085);

        List<String> afterAnay = new ArrayList<>();
        //前提一：  假设   日期   两个字符是  一起解析出来的
        for (String str : s) {
            str = str.replace("日期",StringUtils.EMPTY);
            for (int order = 0; order < str.length(); order++) {
                char charAt = str.charAt(order);
                Integer value = Integer.valueOf(charAt);
                if(chineseExpressions){
                    if(!(integerSet.contains(value)||(value>47&&value<58))){
                        str = str.replace(charAt, '*');
                    }
                }else{
                    if(!(45==value||(value>47&&value<58))){
                        str = str.replace(charAt, '*');
                    }
                }
            }
            str = str.replace("*",StringUtils.EMPTY);
            afterAnay.add(str);
        }

        //剔除杂质  之后  就进行排列组合
        List<String> afterArray = new ArrayList<>();
        arrangeAll(afterAnay, "",afterArray);
        //排序完后就进行遍历  然后
        for (String arrayStr : afterArray) {
            //去掉截止日期字符
            arrayStr = arrayStr.replace("截止日期",StringUtils.EMPTY);
            if(arrayStr.matches(regx)||arrayStr.matches(regx1))
            {
                return arrayStr;
            }
        }
        return null;
    }

    //账户名称/单位名称/客户名称  的验证
    //  也就是说  输入的s  是否包含  以上字段
    private static Boolean isKeyWord(String s){
        if(s.contains("账户名称")||s.contains("单位名称")||s.contains("客户名称")){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private static boolean isKeyWord(List<RectVal> vals){
        for (RectVal val : vals) {
            String s = val.getWord().trim().replace(" ", StringUtils.EMPTY);
            if(s.contains("余额")||s.contains("金额")){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    //截取账户名称/单位名称/客户名称 后的内容   小于2个字符就返回null
    private static String getName(String s){
        //截取相应的内容
        if(s.contains("账户名称")){
            String temp = s.substring(s.indexOf("账户名称")+4,s.length());
            return temp.length()>2?temp:null;
        }
        else if(s.contains("单位名称")){
            String temp = s.substring(s.indexOf("单位名称")+4,s.length());
            return temp.length()>2?temp:null;
        }
        else{
            String temp = s.substring(s.indexOf("客户名称")+4,s.length());
            return temp.length()>2?temp:null;
        }
    }


    //分析传入的数据  是否是异行或者是表格中的数据
    //如果6个元素进来还没有 出现账号  这个关键字  就  提取第二个元素的值  返回值
    //如果6个元素中 出现了账号  关键字    并且  在误差范围内是一行数据  那么就填充map记录对应的坐标  并且  打开表格开关  返回null
    //否则同样提取第二个元素的值
    private static String getName(List<RectVal> vals){
        if(vals.size()>2){
            //  返回第二个元素的val值   并且  把list清空
            String s = vals.get(1).getWord().trim();
            vals.clear();
            return s;
        }else{
            //进行最新一个字符的读取  如果包含账号  这个关键字  说明  是  在同一行  就存储对应的序号
            RectVal lastVal = vals.get(vals.size() - 1);
            String word = lastVal.getWord().trim();
            if(word.contains("账号")){
                //进行误差范围计算  如果在同一行  就记录对应的坐标   行误差控制在0.3％
                //也就是需要  取出第一个元素 与该元素进行纵坐标比对
                RectVal firstVal = vals.get(0);
                double firstTop = firstVal.getRect().getTop() + ((double)firstVal.getRect().getHeight() / 2);
                double lastTop = lastVal.getRect().getTop() + ((double)lastVal.getRect().getHeight() / 2);
                double result = Math.abs(firstTop - lastTop) / Math.min(firstTop, lastTop);
                if(result>0.003){
                    //说明不是同一行   直接提取第二个元素进行返回   并且6个以内出现账号关键字  说明表格的开始
                    String str = vals.get(1).getWord().trim();
                    //list只保存最后一个元素
                    vals.subList(vals.size()-1,vals.size());
                    return str;
                }else{
                    //说明是表格中同一行
                    return null;
                }
            }
            return StringUtils.EMPTY;
        }
    }


    //判断收据中的表格　　是否录入完毕
    private static Boolean isTableEnd(List<RectVal> vals){
        //结束的标志是  后三个里面连续出现两个相符  或者最后一个是一个不包含数字的超多15个字符长度的字符串
        //或者含有  “以下空白”  字样
        //遇到结束标志  需要将  结束标志截取掉
        RectVal val = vals.get(vals.size() - 1);
        String replace = val.getWord().trim().replace(" ", StringUtils.EMPTY);
        //正则表达  如果包含数字就会返回  true  如果不包含数字  就会返回false
        String reg = ".+?\\d.+?";
        if ((replace.contains("以下空白"))||(replace.length()>15&&(!replace.matches(reg)))){
            //将  结束标志  去掉
            vals.subList(0,vals.size() - 1);
            return true;
        }
        //然后进行  相符不相符重合度判断   取最后三个字符串  观察倒数第一个和第三个是否含有相符的字符串
        if(vals.size()>=3){
            if(replace.contains("相符")){
                //倒数第三个是否包含  “相符”
                RectVal rectVal = vals.get(vals.size() - 3);
                String word = rectVal.getWord().trim().replace(" ", StringUtils.EMPTY);
                if(word.contains("相符")){
                    vals.subList(0,vals.size() - 1);
                    return true;
                }
            }
        }
        return false;
    }


    //到了结束标志后  就可以进行解析了    解析为表格里面的数据
    private static  List<BankReceiptTable>  getTableData(List<RectVal> vals){
        //默认是以      账号类型/客户名称/单位名称  或者账号开头   来进行解析
        //因为这是表格数据  所以需要统计行和列   装入临时的  容器中  去除第一列 （标题列）  然后把实际数据  转为  表格数据
        //并且  记录  关键数据  以及  对应的list中的位置
        Map<String,Integer> map = new HashMap<>();
        List<List<RectVal>> list = new ArrayList<>();
        List<BankReceiptTable> tableList = new ArrayList<>();
        //表格中数据  行的 偏移量
        Integer offset = null;



        //1.首先把他整理为  多列  存入临时的容器中  并且按照横坐标进行正向排序     包含核对结果的列   相符/不相符的列 直接放弃
        //行误差暂定为0.003   列误差暂定为0.02
        if(CollectionUtils.isEmpty(vals)){
            return null;
        }
        for (int i = 0; i < vals.size(); i++) {
            RectVal rectVal = vals.get(i);
            //如果容器是空的
            if(CollectionUtils.isEmpty(list)){
                List<RectVal> tempList = new ArrayList<>();
                tempList.add(rectVal);
                list.add(tempList);
                continue;
            }
            //取出最近的一条记录
            List<RectVal> valList = list.get(list.size() - 1);
            //如果容器不是空的  且最近的一条记录不是空的  那么就进行判断
            if((!CollectionUtils.isEmpty(list))  && (!CollectionUtils.isEmpty(valList))){
                RectVal val = valList.get(valList.size() - 1);
                if(judgeNode(getData(val),getData(rectVal),0.035,false)){
                    //如果是同一行  并且不含排斥字句  就加入到  该列中
                    String replace = rectVal.getWord().trim().replace(" ", StringUtils.EMPTY);
                    if((!replace.contains("核对结果")||(!replace.contains("相符")))){
                        //将新元素  加入到list中
                        valList.add(rectVal);
                        continue;
                    }
                }else{
                    //如果不在同一行  就需要创建新的list并且加入到  总体的list中
                    List<RectVal> tempList = new ArrayList<>();
                    tempList.add(rectVal);
                    list.add(tempList);
                    continue;
                }
            }else{
                //其实这是一种错误的情况   但是还是按照正确的情况来处理吧
                List<RectVal> tempList = new ArrayList<>();
                tempList.add(rectVal);
                list.add(tempList);
                continue;
            }
        }
        //  统计完毕之后      就需要对  统计后的 数据  进行处理
        //首先按照列  进行排序  然后第一  行进行关键列的筛选
        for (int num = 0; num < list.size(); num++) {
            List<RectVal> valList = list.get(num);
            valList.sort(new Comparator<RectVal>() {
                @Override
                public int compare(RectVal o1, RectVal o2) {
                    return (int)(getData(o1).getxAxis() - getData(o2).getxAxis());
                }
            });
            //排序之后  还需要对第一个进行关键字确认
            //账号是10位以上的纯数字  所以每个list的第一个元素  必须是10位以上的纯数字
            for (int order = 0; order < valList.size(); order++) {
                String replace = valList.get(order).getWord().trim().replace(" ", StringUtils.EMPTY);
                if(num == 0){
                    if(replace.contains("账户名称")){
                        map.put("accountName",order);
                    }else if(replace.contains("账号")){
                        map.put("accountNum",order);
                    }else if(replace.contains("类别")||replace.contains("账户类型")||replace.contains("性质")){
                        map.put("accountType",order);
                    }else if(replace.contains("余额")||replace.contains("金额")){
                        map.put("accountBalance",order);
                    }
                }else if(Objects.isNull(offset)&&num!=0){
                    //发现第一个长度大于10的字符串
                    if(replace.length() >10){
                        offset = order;
                    }
                }
            }
        }

        //在调整好顺序并且  提取好关键字后  就可以进行表格地提取
        //第一行  是标题行  可以直接舍弃
        for (int order = 1; order < list.size(); order++) {
            BankReceiptTable table = new BankReceiptTable();
            List<RectVal> valList = list.get(order);
            if(valList.size() <4){
                continue;
            }
            if(map.containsKey("accountName")){
                table.setAccountName(valList.get(map.get("accountName")+(Objects.isNull(offset)?0:offset)).getWord());
            }
            if(map.containsKey("accountNum")){
                table.setAccountNum(valList.get(map.get("accountNum")+(Objects.isNull(offset)?0:offset)).getWord());
            }
            if(map.containsKey("accountType")){
                table.setAccountType(valList.get(map.get("accountType")+(Objects.isNull(offset)?0:offset)).getWord());
            }
            if(map.containsKey("accountBalance")){
                table.setAccountBalance(valList.get(map.get("accountBalance")+(Objects.isNull(offset)?0:offset)).getWord());
            }
            tableList.add(table);
        }
        return tableList;

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

    //统计错误页面
    private static String getResultJson(List<CompletableFuture<BankReceipt>> list) throws ExecutionException, InterruptedException {
        BankResult bankResult = new BankResult();
        //将list转化为VO然后导出
        List<Integer> result = new ArrayList<>();
        List<BankReceiptVO> voList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            BankReceipt x = list.get(i).get();
            if(Objects.isNull(x.getTableList())){
                result.add(i+1);
            }else{
                x.getTableList().forEach(y->{
                    BankReceiptVO vo = new BankReceiptVO();
                    vo.setBankName(x.getBankName());
                    vo.setDeadline(x.getDeadline());
                    vo.setAccountName(x.getAccountName());
                    vo.setAccountNum(y.getAccountNum());
                    vo.setAccountType(y.getAccountType());
                    vo.setAccountBalance(y.getAccountBalance());
                    voList.add(vo);
                });
            }
        }
        bankResult.setDataList(voList);
        bankResult.setErrorPage(result);
        //将BEAN转为JSON
        return JSON.toJSONString(bankResult);
    }


    //将多个字符  排列组合  返回所有的可能 以prefix做分隔符
    public static List<String> arrangeAll(List array, String prefix,List<String> result){
        result.add(prefix);
        for (int i = 0; i < array.size(); i++) {
            List temp = new LinkedList(array);
            arrangeAll(temp, prefix + temp.remove(i),result);
        }
        return result;
    }


}
