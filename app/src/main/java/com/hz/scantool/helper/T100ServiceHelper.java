package com.hz.scantool.helper;

import android.content.Context;

import com.hz.scantool.models.Company;
import com.hz.scantool.models.UserInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class T100ServiceHelper {

    private static final String SERVICE_IP="192.168.210.3";
    private static final String SERVICE_LISTENER="topprd";   //toptst
    private static final String SERVICE_ENTERPRISE="10";
    private static final String ARG_NETWORK_WLAN="外部网络";
    private static final String SERVICE_LAN_URL="http://192.168.210.3/wtopprd/ws/r/awsp900?WSDL";  //wtoptst
    private static final String SERVICE_WLAN_URL="http://119.97.210.146:8089/wtopprd/ws/r/awsp900?WSDL";  //wtoptst

    private String requestUrl;
    private String requestBody;
    private StringBuilder webXmlContent;
    private String requestMethod;
    private String userId;
    private String userSiteId;
    private Context mContext;

    //设置网络类型
    private void setRequestUrl(){
        if(UserInfo.getUserNetwork(mContext).equals(ARG_NETWORK_WLAN)){
            this.requestUrl = SERVICE_WLAN_URL;
        }else{
            this.requestUrl = SERVICE_LAN_URL;
        }
    }

    //设置据点
    private void setUserSiteId(){
        userSiteId = UserInfo.getUserSiteId(mContext);
    }

    //设置请求xml
    private void setRequestBody(){
        String webKey = "16baae6c40b922d8ddb12a0320d8ea1d";
        String webTimestamp = "20201114083106031";

        webXmlContent=new StringBuilder();
        webXmlContent.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tip=\"http://www.digiwin.com.cn/tiptop/TIPTOPServiceGateWay\">\n"+
                "<soapenv:Header/>\n"+
                "<soapenv:Body>\n"+
                "<tip:invokeSrv>\n"+
                "<request>\n"+
                "&lt;request type=\"sync\" key=\""+webKey+"\"&gt;\n"+
                "&lt;host prod=\"\" ver=\"\" ip=\"\" id=\"\" lang=\"zh_CN\" timezone=\"+8\" timestamp=\""+webTimestamp+"\" acct=\"tiptop\"/&gt;\n"+
                "&lt;service prod=\"T100\" name=\""+requestMethod+"\" srvver=\"1.0\" ip=\""+SERVICE_IP+"\" id=\""+SERVICE_LISTENER+"\"/&gt;\n"+
                "&lt;datakey&gt;\n"+
                "&lt;key name=\"EntId\"&gt;"+SERVICE_ENTERPRISE+"&lt;/key&gt;\n"+
                "&lt;key name=\"CompanyId\"&gt;"+userSiteId+"&lt;/key&gt;\n"+
                "&lt;/datakey&gt;\n"+
                "&lt;payload&gt;\n"+
                "&lt;param key=\"data\" type=\"XML\"&gt;\n"+
                "&lt;![CDATA[\n"+
                "&lt;Request&gt;\n"+
                "&lt;RequestContent&gt;\n"
        );
        webXmlContent.append(requestBody);
        webXmlContent.append("&lt;/RequestContent&gt;\n"+
                        "&lt;/Request&gt;\n"+
                        "]]&gt;\n"+
                        "&lt;/param&gt;\n"+
                        "&lt;/payload&gt;\n"+
                        "&lt;/request&gt;\n"+
                        "</request>\n"+
                        "</tip:invokeSrv>\n"+
                        " </soapenv:Body>\n"+
                        "</soapenv:Envelope>");
    }

    //执行post
    private Response executeT100Service() throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/xml");
        RequestBody body = RequestBody.create(mediaType, webXmlContent.toString());
        Request request = new Request.Builder()
                .url(requestUrl)
                .post(body)
                .addHeader("Content-Type", "text/xml")
                .addHeader("SOAPAction", "")
                .build();
        Response response = client.newCall(request).execute();

        return response;
    }

    //获取返回数据
    public String getT100Data(String requestBody,String requestMethod,Context mContext) throws IOException {
        String strResponse="";

        //初始化webservice地址
        this.mContext = mContext;
        setRequestUrl();

        setUserSiteId();  //初始化营运据点

        //初始化传入xml数据
        this.requestBody = requestBody;
        this.requestMethod = requestMethod;
        setRequestBody();

        Response erpResponse = executeT100Service();
        if(erpResponse.isSuccessful()){
            strResponse = erpResponse.body().string();
        }else{
            strResponse = "网络错误";
        }


        return strResponse;
    }

}
