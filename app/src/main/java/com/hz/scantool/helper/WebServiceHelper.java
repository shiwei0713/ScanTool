package com.hz.scantool.helper;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebServiceHelper {
    private String webKey;
    private String webTimestamp;
    private String webName;
    private String webUrl;
    private static final String webIp="192.168.210.3";
    private static final String webId="topprd";   //toptst
    private static final String webEntId="10";
    private static final String ARG_NETWORK_WLAN="外部网络";
    private static final String serviceUrl="http://192.168.210.3/wtopprd/ws/r/awsp900?WSDL";  //wtoptst
    private static final String serviceWlanUrl="http://119.97.210.146:8089/wtopprd/ws/r/awsp900?WSDL";  //wtoptst
    private String webSite;
    private String webSiteId;
    private StringBuilder webRequestContent;
    private String string="T100";
    private Integer webResponseCode;

    public void setWebKey(String webKey) {
        this.webKey = webKey;
    }

    public void setWebTimestamp(String webTimestamp) {
        this.webTimestamp = webTimestamp;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public void setWebRequestContent(StringBuilder webRequestContent) {
        this.webRequestContent = webRequestContent;
    }

    public void setWebUrl(String webUrlType) {
        if(webUrlType.equals(ARG_NETWORK_WLAN)){
            this.webUrl = serviceWlanUrl;
        }else{
            this.webUrl = serviceUrl;
        }
    }

    public Integer getWebResponseCode() {
        return webResponseCode;
    }

    public String sendWebRequest() throws IOException {
        //处理XML请求
        StringBuilder webXmlContent=new StringBuilder();
        webXmlContent.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tip=\"http://www.digiwin.com.cn/tiptop/TIPTOPServiceGateWay\">\n"+
                "<soapenv:Header/>\n"+
                "<soapenv:Body>\n"+
                "<tip:invokeSrv>\n"+
                "<request>\n"+
                "&lt;request type=\"sync\" key=\""+webKey+"\"&gt;\n"+
                "&lt;host prod=\"\" ver=\"\" ip=\"\" id=\"\" lang=\"zh_CN\" timezone=\"+8\" timestamp=\""+webTimestamp+"\" acct=\"tiptop\"/&gt;\n"+
                "&lt;service prod=\"T100\" name=\""+webName+"\" srvver=\"1.0\" ip=\""+webIp+"\" id=\""+webId+"\"/&gt;\n"+
                "&lt;datakey&gt;\n"+
                "&lt;key name=\"EntId\"&gt;"+webEntId+"&lt;/key&gt;\n"+
                "&lt;key name=\"CompanyId\"&gt;"+webSite+"&lt;/key&gt;\n"+
                "&lt;/datakey&gt;\n"+
                "&lt;payload&gt;\n"+
                "&lt;param key=\"data\" type=\"XML\"&gt;\n"+
                "&lt;![CDATA[\n"+
                "&lt;Request&gt;\n"+
                "&lt;RequestContent&gt;\n"/*+
                "&lt;Parameter&gt;\n"+
                "&lt;Record&gt;\n"*/
                );
        webXmlContent.append(webRequestContent);
        webXmlContent.append(/*"&lt;Field name=\"site\" value=\""+webSite+"\"/&gt;\n"+
                "&lt;/Record&gt;\n"+
                "&lt;/Parameter&gt;\n"+
                "&lt;Document/&gt;\n"+*/
                "&lt;/RequestContent&gt;\n"+
                "&lt;/Request&gt;\n"+
                "]]&gt;\n"+
                "&lt;/param&gt;\n"+
                "&lt;/payload&gt;\n"+
                "&lt;/request&gt;\n"+
                "</request>\n"+
                "</tip:invokeSrv>\n"+
                " </soapenv:Body>\n"+
                "</soapenv:Envelope>");

        //发送WebService请求
        byte[] xmlBytes = webXmlContent.toString().getBytes("UTF-8");
        URL url = new URL(webUrl);

        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setDefaultUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("SOAPAction","");
        conn.setRequestProperty("Content-Type","text/xml; charset=UTF-8");
        conn.getOutputStream().write(xmlBytes);
        conn.getOutputStream().flush();
        conn.getOutputStream().close();
        //服务器响应代码
        webResponseCode=conn.getResponseCode();

        if(webResponseCode==200) {
            //获取输出流
            InputStream is = conn.getInputStream();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                out.write(buf, 0, len);
            }

            string = out.toString("UTF-8");
        }

        return string;
    }
}
