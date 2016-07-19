package com.company;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 该类可以将一个soap消息发送至服务端，并获取响应的soap消息
 * 解析成json格式并存储到文件
 */
public class Main {

    private static final String gradexml_h = "<v:Envelope xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\"><v:Header /><v:Body><GetMyGrades xmlns=\"http://murpcn.com/murpwebservice/\" id=\"o0\" c:root=\"1\"><umcid i:type=\"d:int\">";
    private static final String gradexml_t = "</umcid></GetMyGrades></v:Body></v:Envelope>";
    private static final String url = "http://202.117.124.128/KEY/MURPNewsService.asmx";

    private static String invokeSrv(String endpoint, String soapXml) throws Exception{
        StringBuilder sb = new StringBuilder();

        String method = "POST";
        String contentType = "text/xml;charset=utf-8";

        URL url = new URL(endpoint);
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

        urlConn.setRequestProperty("SOAPAction", "http://murpcn.com/murpwebservice/GetMyGrades");
        urlConn.setRequestProperty("Content-Type", contentType);
        urlConn.connect();

        // 向输出流写出数据，这些数据将存到内存缓冲区中
        PrintWriter pw = new PrintWriter(urlConn.getOutputStream());
        pw.write(soapXml);

        // 刷新对象输出流，将任何字节都写入潜在的流中
        pw.flush();
        pw.close();

        // 接收返回消息
        // 解析返回值编码格式
        String charset = "UTF-8";
        String ct = urlConn.getContentType();
        Pattern p = Pattern.compile("charset=.*;?");
        Matcher m = p.matcher(ct);
        if(m.find()){
            charset = m.group();
            // 去除charset=和;,如果有的话
            if(charset.endsWith(";")){
                charset = charset.substring(charset.indexOf("=") + 1, charset.indexOf(";"));
            }else{
                charset = charset.substring(charset.indexOf("=") + 1);
            }
            // charset = "\"UTF-8\"";
            // 去除引号 ,如果有的话
            if(charset.contains("\"")){
                charset = charset.substring(1, charset.length() - 1);
            }
            charset = charset.trim();
        }

        // 将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端。
        // <===注意，实际发送请求的代码段就在这里
        InputStream inStream = urlConn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream,charset));

        String line;
        while((line = br.readLine()) != null){
            sb.append(line);
        }
        br.close();
        urlConn.disconnect();
        return sb.toString();
    }

    public static void main(String[] args) {

        System.out.println("--------------------start----------------------");
        //返回请求
        //MAX 26977

        for(int i=1;i<=6;i++){
            String fileName = "C:\\test\\log"+i+".json";
            int start = readFileByLines(fileName);
            int end = i*5000;
            if(end>26977){
                end = 26977;
            }
            System.out.println("----------线程"+i+"启动"+start+"-----------");
            int finalEnd = end;
            int index =i;
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    getData("log"+index,start, finalEnd);
                }
            }.start();
        }

        System.out.println("--------------------stop----------------------");

    }





    private static void getData(String Tag,int start, int end){
        int i =1;
        try {
            for (i = start; i <= end; i += 1) {
                String xmlin = gradexml_h + i + gradexml_t;
                String xmlStr = "";
                xmlStr = invokeSrv(url, xmlin);
                SingleUser user = parserXml(i, xmlStr);
                if (user == null) {
                    continue;
                }
                Gson gson = new Gson();
                String json = gson.toJson(user);
                writeFile(user.sid, json);
                writeFile(Tag,i+"\n");
                System.out.println(Tag+"==写入数据" + i + "==");
                int ramd = (int)(10+Math.random()*(50-1+1));
                Thread.sleep(ramd);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(Tag+"=====error=====");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            String datestr = df.format(new Date());
            writeFile(Tag+ " error", datestr + "   " + i + "\n");
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String datestr = df.format(new Date());
        writeFile(Tag+ "ok", datestr + "   " + i + "\n");


        System.out.println( " =====结束=====");
    }


    /**
     * 解析XML文件
     */
    private static SingleUser parserXml(int id,String  xmlSrt) {
        SingleUser user = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlSrt)));

            NodeList gradelist = document.getElementsByTagName("GetMyGradesResult");
            for(int i =0;i<gradelist.getLength();i++){
                //
                Node secondNode = gradelist.item(i);
                //System.out.println(secondNode.getNodeName()+secondNode.getTextContent());
                NodeList seconds = secondNode.getChildNodes();

                String sid = "";
                String sname = "";
                List<Grades> grades = new ArrayList<>();

                for(int j=0;j<seconds.getLength();j++){
                    Node third = seconds.item(j);
                    //System.out.println(third.getNodeName()+third.getTextContent());
                    NodeList thirds = third.getChildNodes();
                    sid = thirds.item(0).getTextContent();
                    sname = "null";
                    grades.add(new Grades(thirds.item(6).getTextContent(),thirds.item(1).getTextContent(),thirds.item(3).getTextContent(),
                            thirds.item(8).getTextContent(),thirds.item(2).getTextContent(),thirds.item(9).getTextContent(),thirds.item(11).getTextContent()));

                }
                user =new SingleUser(id,sid,sname,grades);
            }
            return user;

        }  catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class SingleUser {
        int id;
        String sid;
        String sname;
        List<Grades> grades;

        SingleUser(int id,String sid, String sname, List<Grades> grades) {
            this.sid = sid;
            this.sname = sname;
            this.grades = grades;
            this.id = id;
        }
    }

    private static class  Grades {
        String cid;
        String cname;
        String ctime;
        String ctype;
        String cgrade;
        String cxf;
        String cstatus;

        Grades(String cid, String cname, String ctime, String ctype, String cgrade, String cxf, String cstatus) {
            this.cid = cid;
            this.cname = cname;
            this.ctime = ctime;
            this.ctype = ctype;
            this.cgrade = cgrade;
            this.cxf = cxf;
            this.cstatus = cstatus;
        }
    }

    /**
     * 字符串写入文件
     * @param name
     * @param txt
     */
    private static void writeFile(String name,String txt) {
        File f = new File("c:\\test\\"+name+".json") ;

        try {
            if(!f.exists()) {
                f.createNewFile();
            }
            System.out.println("创建文件成功"+name);
            OutputStream out = null ;
            //设为true为增量写入
            out = new FileOutputStream(f) ;
            byte b[] = txt.getBytes() ;
            out.write(b);
            out.close() ;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static int readFileByLines(String fileName) {
        File file = new File(fileName);
        int i = 26998;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;


            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                //System.out.println("line " + line + ": " + tempString);
                i = Integer.parseInt(tempString);
                //line++;
                break;
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

        return i;
    }
}

