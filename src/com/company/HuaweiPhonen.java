package com.company;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by free2 on 16-7-13.
 *
 */
public class HuaweiPhonen {

    public static final String UTF8 = "UTF-8";


    //荣耀8预约
    public void yuyue() throws Exception {
        List<String> datas;
        datas =  readFileByLines("C:\\test2\\phone.txt");

        int singleCount = datas.size()/10;

        for(int index =1;index<=10;index++){
            int start = getStopPoint(index);
            if(start==0){//文件不存在
                start = (index-1)*singleCount;
            }
            int end = index*singleCount;
            System.out.println("----------线程"+index+"启动"+start+"-----------");

            int threadIndex = index;
            int threadStart = start;
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    for(int i = threadStart;i<=end;i++){
                        String single = datas.get(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("site_id","6873693832");
                        map.put("ad_id","0");
                        map.put("csrfmiddlewaretoken","TiUiMcYMaAtwzhWE69zqOoCbP2oGpGQ0");
                        map.put("6882443947",single);
                        map.put("form_id","6882443946");
                        try {
                            String res =  myPost("http://ad.toutiao.com/tetris/form/submit/",map);
                            //System.out.println(res);
                            if(res .contains("success")){
                                System.out.println("预约"+single+"成功");
                                writeLog(i+"\t"+single+ "\tok");
                                writeProcess(threadIndex,i,end);
                            }else{
                                writeLog(i+"\t"+single+ "\tfail");
                                System.out.println("预约"+single+"失败");
                            }

                            Thread.sleep(200);
                            //writeLog
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                writeLog(i+"\t"+single+ "\tfail");
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }.start();
        }





        //{"status": "success"}
        //{"status": "fail", "data": {"6882443947": "18706798710\u5df2\u4f7f\u7528\uff0c\u8bf7\u91cd\u65b0\u586b\u5199!"}}
    }

    /**
     * post 函数
     * @param urlStr
     * @param map
     * @return
     * @throws Exception
     */
    public String myPost(String urlStr, Map<String, String> map) throws Exception {

        byte[] content = encodeParameters(map);

        StringBuilder sb = new StringBuilder();

        String method = "POST";

        URL url = new URL(urlStr);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

        urlConn.setConnectTimeout(5000);
        // POST请求
        urlConn.setRequestMethod(method);
        // 设置要发送消息
        urlConn.setDoOutput(true);
        // 设置要读取响应消息
        urlConn.setDoInput(true);
        // POST不能使用cache
        urlConn.setUseCaches(false);
        urlConn.setInstanceFollowRedirects(true);

        urlConn.setRequestProperty("Cookie", "uuid=w:398fb67d4344433e8dc1f81eb31faf74; csrftoken=TiUiMcYMaAtwzhWE69zqOoCbP2oGpGQ0");
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConn.setRequestProperty("Content-Length", Long.toString(content.length));
        urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 5.1; m1 note Build/LMY47D) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/37.0.0.0 Mobile MQQBrowser/6.2 TBS/036524 Safari/537.36 MicroMessenger/6.3.22.821 NetType/WIFI Language/zh_CN");

        urlConn.setRequestProperty("Origin", "http://ad.toutiao.com");
        urlConn.setRequestProperty("Referer", "http://ad.toutiao.com/tetris/page/6874814164/");
        urlConn.setRequestProperty("Content-Length", Long.toString(content.length));


        urlConn.connect();

        // 向输出流写出数据，这些数据将存到内存缓冲区中
        OutputStream os = urlConn.getOutputStream();
        os.write(content);
        os.flush();
        os.close();


        // 接收返回消息
        // 解析返回值编码格式
        String charset = "UTF-8";
        String ct = urlConn.getContentType();
        Pattern p = Pattern.compile("charset=.*;?");
        Matcher m = p.matcher(ct);
        if (m.find()) {
            charset = m.group();
            // 去除charset=和;,如果有的话
            if (charset.endsWith(";")) {
                charset = charset.substring(charset.indexOf("=") + 1, charset.indexOf(";"));
            } else {
                charset = charset.substring(charset.indexOf("=") + 1);
            }
            // charset = "\"UTF-8\"";
            // 去除引号 ,如果有的话
            if (charset.contains("\"")) {
                charset = charset.substring(1, charset.length() - 1);
            }
            charset = charset.trim();
        }

        int responseCode = urlConn.getResponseCode();
        //System.out.println(responseCode);

        // 将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端。
        // <===注意，实际发送请求的代码段就在这里
        InputStream inStream = urlConn.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(inStream, charset));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        urlConn.disconnect();
        return sb.toString();
    }

    private byte[] encodeParameters(Map<String, String> map) {

        if (map == null) {
            map = new TreeMap<>();
        }
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (encodedParams.length() > 0) {
                    encodedParams.append("&");
                }
                encodedParams.append(URLEncoder.encode(entry.getKey(), UTF8));
                encodedParams.append('=');
                String v = entry.getValue() == null ? "" : entry.getValue();
                encodedParams.append(URLEncoder.encode(v, UTF8));
            }
            return encodedParams.toString().getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding not supported: " + UTF8, e);
        }
    }


    /**
     * 读取电话号码 传回数组
     */
    public static List<String> readFileByLines(String fileName) {
        List<String> datas = new ArrayList<>();
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                System.out.println("line " + line + ": " + tempString);
                line++;
                datas.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        return datas;
    }

    /**
     * 输出日志 增量添加，每次一行
     * @param txt
     */
    public static void writeLog(String txt) throws IOException {
        File f = new File("log.txt") ;
        if(!f.exists()){
            System.out.println("创建日志文件 log.txt" +f.createNewFile());
        }
        try {
            OutputStream out = null ;
            //设为true为增量写入
            String writeStr = txt+"\r\n";
            out = new FileOutputStream(f,true) ;
            byte b[] = writeStr.getBytes() ;
            out.write(b);
            out.close() ;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写出进度
     */
    public static void writeProcess(int id,int start,int end) throws IOException {
        File f = new File("process_"+id+".txt") ;
        if(!f.exists()){
            System.out.println("创建进度文件 process_"+id+".txt"+f.createNewFile());
        }
        try {
            OutputStream out = null ;
            //设为true为增量写入
            String writeStr = start+"\r\n"+end+"\r\n";
            out = new FileOutputStream(f,false) ;
            byte b[] = writeStr.getBytes() ;
            out.write(b);
            out.close() ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取上次停止的位置
     * 0为没有文件 从头开始
     * -1 结束
     * @param id
     * @return
     */
    public static int getStopPoint(int id) throws Exception {
        File f = new File("process_"+id+".txt") ;
        int start = -1;
        int end = -1;
        if(!f.exists()){
            return 0;
        }
        BufferedReader reader = null;
        reader = new BufferedReader(new FileReader(f));
        String tempString = null;

        int line = 1;
        // 一次读入一行，直到读入null为文件结束
        while ((tempString = reader.readLine()) != null) {
            // 显示行号
            if(line==1){
                start = Integer.parseInt(tempString);
            }else {
                end = Integer.parseInt(tempString);
            }
            line++;
        }

        System.out.println("线程"+id+" " + start+"-"+end );
        reader.close();
        if(start<end){
            return -1;
        }

        return start;
    }
}
