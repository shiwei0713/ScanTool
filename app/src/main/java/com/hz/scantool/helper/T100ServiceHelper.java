package com.hz.scantool.helper;

import android.content.Context;

import com.hz.scantool.models.Company;
import com.hz.scantool.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    //获取网络类型
    private void setRequestUrl(){
        if(UserInfo.getUserNetwork(mContext).equals(ARG_NETWORK_WLAN)){
            this.requestUrl = SERVICE_WLAN_URL;
        }else{
            this.requestUrl = SERVICE_LAN_URL;
        }
    }

    //设置网络类型
    private void setRequestUrl(String netWork){
        if(netWork.equals(ARG_NETWORK_WLAN)){
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
                .retryOnConnectionFailure(false)
                .connectTimeout(20L,TimeUnit.SECONDS)
                .readTimeout(20L,TimeUnit.SECONDS)
                .writeTimeout(20L,TimeUnit.SECONDS)
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

    //获取返回XML数据
    public String getT100Data(String requestBody,String requestMethod,Context mContext,String netWork) throws IOException {
        String strResponse="";

        //初始化webservice地址
        this.mContext = mContext;
        if(netWork.isEmpty()){
            setRequestUrl();
        }else{
            setRequestUrl(netWork);
        }

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

    //解析xml状态数据
    public List<Map<String,Object>> getT100StatusData(String strResponse){
        List<Map<String,Object>> statusList = new ArrayList<Map<String,Object>>();
        String statusCode="";
        String statusSqlcode="";
        String statusDescription="";

        //检查索引
        int iStatusIndex=strResponse.indexOf("Status",1);

        Map<String,Object> map = new HashMap<String,Object>();
        if (iStatusIndex>-1){
            //当前任务数
            String strStatus=strResponse.substring(strResponse.indexOf("Status",1),strResponse.length()).replace("\"","");
            statusCode=strStatus.substring(strStatus.indexOf("code",1)+5,strStatus.indexOf("sqlcode",1)-1);
            statusSqlcode = strStatus.substring(strStatus.indexOf("sqlcode",1)+8,strStatus.indexOf("description",1)-1);
            statusDescription = strStatus.substring(strStatus.indexOf("description",1)+12,strStatus.indexOf("&gt;",1)-1);

            map.put("statusCode",statusCode.trim());
            map.put("statusSqlcode",statusSqlcode.trim());
            map.put("statusDescription",statusDescription.trim());
            statusList.add(map);
        }

        return statusList;
    }

    //解析xml多结果数据
    public List<Map<String,Object>> getT100JsonListData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson;
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("StockId", jsonObject.getString("erpStockId").trim());
                map.put("StockLocationId", jsonObject.getString("erpStockLocationId").trim());
                map.put("StockLocation", jsonObject.getString("erpStockLocation").trim());
                map.put("StockBatch", jsonObject.getString("erpStockBatch").trim());
                map.put("Inventory", jsonObject.getString("erpInventory").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("QuantityPcs", jsonObject.getString("erpQuantityPcs").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析xml单条结果数据
    public List<Map<String,Object>> getT100JsonData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("ProductCode",jsonObject.getString("erpProductCode").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels",jsonObject.getString("erpProductModels").trim());
                map.put("Process",jsonObject.getString("erpProcess").trim());
                map.put("Device",jsonObject.getString("erpDevice").trim());
                map.put("PlanDate",jsonObject.getString("erpPlanDate").trim());
                map.put("Quantity",jsonObject.getString("erpQuantity").trim());
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("QuantityNg",jsonObject.getString("erpQuantityNg").trim());
                map.put("QuantityNo",jsonObject.getString("erpQuantityNo").trim());
                map.put("QrCodeRule",jsonObject.getString("erpQrCodeRule").trim());
                map.put("Status",jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }

    //解析xml出货三点照合单条结果数据
    public List<Map<String,Object>> getT100DeliveryData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("ProductCode",jsonObject.getString("erpProductCode").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels",jsonObject.getString("erpProductModels").trim());
                map.put("Quantity",jsonObject.getString("erpQuantity").trim());
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("Tray",jsonObject.getString("erpTray").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }

    //解析xml单条结果数据
    public List<Map<String,Object>> getT100UserData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("UserCode",jsonObject.getString("erpUserCode").trim());
                map.put("UserName",jsonObject.getString("erpUserName").trim());
                map.put("MacAddress",jsonObject.getString("erpMacAddress").trim());
                map.put("UserPassword",jsonObject.getString("erpUserPassword").trim());
                map.put("Power",jsonObject.getString("erpPower").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }

    //解析xml单条结果数据
    public List<Map<String,Object>> getT100StockData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("StockId",jsonObject.getString("erpStockId").trim());
                map.put("StockLocationId",jsonObject.getString("erpStockLocationId").trim());
                map.put("StockLocation",jsonObject.getString("erpStockLocation").trim());
                map.put("StockType",jsonObject.getString("erpStockType").trim());
                map.put("PlanDate",jsonObject.getString("erpPlanDate").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }

    //解析工单xml结果数据
    public List<Map<String,Object>> getT100JsonWorkOrderData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("DocType", jsonObject.getString("erpDocType").trim());
                map.put("DeptId", jsonObject.getString("erpDeptId").trim());
                map.put("Dept", jsonObject.getString("erpDept").trim());
                map.put("StockId", jsonObject.getString("erpStockId").trim());
                map.put("Stock", jsonObject.getString("erpStock").trim());
                map.put("Docno", jsonObject.getString("erpDocno").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析料件数据
    public List<Map<String,Object>> getT100JsonItemData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("Url", jsonObject.getString("erpUrl").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析料件数据
    public List<Map<String,Object>> getT100JsonItemStockListData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("Stock", jsonObject.getString("erpStock").trim());
                map.put("Package", jsonObject.getString("erpPackage").trim());
                map.put("StockCount", jsonObject.getString("erpStockCount").trim());
                map.put("StockCountNg", jsonObject.getString("erpStockCountNg").trim());
                map.put("StockCountOk", jsonObject.getString("erpStockCountOk").trim());
                map.put("ProductCount", jsonObject.getString("erpProductCount").trim());
                map.put("ProductCountNg", jsonObject.getString("erpProductCountNg").trim());
                map.put("ProductCountOk", jsonObject.getString("erpProductCountOk").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析料件二维码数据
    public List<Map<String,Object>> getT100JsonItemQrcodeData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("Qrcode", jsonObject.getString("erpQrcode").trim());
                map.put("ProcessId", jsonObject.getString("erpProcessId").trim());
                map.put("Process", jsonObject.getString("erpProcess").trim());
                map.put("Attribute", jsonObject.getString("erpAttribute").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("Docno", jsonObject.getString("erpDocno").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析制造命令xml结果数据
    public List<Map<String,Object>> getT100JsonProductData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("Docno", jsonObject.getString("erpDocno").trim());
                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("ProcessId", jsonObject.getString("erpProcessId").trim());
                map.put("Process", jsonObject.getString("erpProcess").trim());
                map.put("Device", jsonObject.getString("erpDevice").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("Employee", jsonObject.getString("erpEmployee").trim());
                map.put("Lots", jsonObject.getString("erpLots").trim());
                map.put("Flag", jsonObject.getString("erpFlag").trim());
                map.put("ModStatus", jsonObject.getString("erpModStatus").trim());
                map.put("OperateCount", jsonObject.getString("erpOperateCount").trim());
                map.put("PrintCount", jsonObject.getString("erpPrintCount").trim());
                map.put("StartStatus", jsonObject.getString("erpStartStatus").trim());
                map.put("CheckStatus", jsonObject.getString("erpCheckStatus").trim());
                map.put("UpStatus", jsonObject.getString("erpUpStatus").trim());
                map.put("ErrorStartStatus", jsonObject.getString("erpErrorStartStatus").trim());
                map.put("ErrorStopStatus", jsonObject.getString("erpErrorStopStatus").trim());
                map.put("Version", jsonObject.getString("erpVersion").trim());
                map.put("StartTime", jsonObject.getString("erpStartTime").trim());
                map.put("CheckTime", jsonObject.getString("erpCheckTime").trim());
                map.put("UpTime", jsonObject.getString("erpUpTime").trim());
                map.put("ErrorTime", jsonObject.getString("erpErrorTime").trim());
                map.put("ProductTotal", jsonObject.getString("erpProductTotal").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析制造命令xml结果数据
    public List<Map<String,Object>> getT100JsonProductMaterialData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("Docno", jsonObject.getString("erpDocno").trim());
                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("ProcessId", jsonObject.getString("erpProcessId").trim());
                map.put("Process", jsonObject.getString("erpProcess").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("Attribute", jsonObject.getString("erpAttribute").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析pqc xml结果数据
    public List<Map<String,Object>> getT100JsonPqcData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("Docno", jsonObject.getString("erpDocno").trim());
                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("ProcessId", jsonObject.getString("erpProcessId").trim());
                map.put("Process", jsonObject.getString("erpProcess").trim());
                map.put("Device", jsonObject.getString("erpDevice").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("Employee", jsonObject.getString("erpEmployee").trim());
                map.put("Lots", jsonObject.getString("erpLots").trim());
                map.put("Flag", jsonObject.getString("erpFlag").trim());
                map.put("Version", jsonObject.getString("erpVersion").trim());
                map.put("ModStatus", jsonObject.getString("erpModStatus").trim());
                map.put("OperateCount", jsonObject.getString("erpOperateCount").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析共模报工数据xml结果数据
    public List<Map<String,Object>> getT100JsonProductDetailData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("Docno", jsonObject.getString("erpDocno").trim());
                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("ProcessId", jsonObject.getString("erpProcessId").trim());
                map.put("Process", jsonObject.getString("erpProcess").trim());
                map.put("Device", jsonObject.getString("erpDevice").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("Employee", jsonObject.getString("erpEmployee").trim());
                map.put("Lots", jsonObject.getString("erpLots").trim());
                map.put("Flag", jsonObject.getString("erpFlag").trim());
                map.put("ModStatus", jsonObject.getString("erpModStatus").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析PQC报工数据xml结果数据
    public List<Map<String,Object>> getT100JsonPqcDetailData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("Docno", jsonObject.getString("erpDocno").trim());
                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("ProcessId", jsonObject.getString("erpProcessId").trim());
                map.put("Process", jsonObject.getString("erpProcess").trim());
                map.put("Device", jsonObject.getString("erpDevice").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("BadQuantity", jsonObject.getString("erpBadQuantity").trim());
                map.put("NgQuantity", jsonObject.getString("erpNgQuantity").trim());
                map.put("Lots", jsonObject.getString("erpLots").trim());
                map.put("Version", jsonObject.getString("erpVersion").trim());
                map.put("Unit", jsonObject.getString("erpUnit").trim());
                map.put("Seq", jsonObject.getString("erpSeq").trim());
                map.put("Seq1", jsonObject.getString("erpSeq1").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                map.put("Attribute", jsonObject.getString("erpAttribute").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析OQC出货数据xml结果数据
    public List<Map<String,Object>> getT100JsonOqcDetailData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("Docno", jsonObject.getString("erpDocno").trim());
                map.put("SalerId", jsonObject.getString("erpSalerId").trim());
                map.put("Saler", jsonObject.getString("erpSaler").trim());
                map.put("CodeRule", jsonObject.getString("erpCodeRule").trim());
                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("QuantityPcs", jsonObject.getString("erpQuantityPcs").trim());
                map.put("Tray", jsonObject.getString("erpTray").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析扫码盘点xml单条结果数据
    public List<Map<String,Object>> getT100CheckData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("ProductCode",jsonObject.getString("erpProductCode").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels",jsonObject.getString("erpProductModels").trim());
                map.put("ProductSize",jsonObject.getString("erpProductSize").trim());
                map.put("StockId",jsonObject.getString("erpStockId").trim());
                map.put("Stock",jsonObject.getString("erpStock").trim());
                map.put("StockLocationId",jsonObject.getString("erpStockLocationId").trim());
                map.put("StockLocation",jsonObject.getString("erpStockLocation").trim());
                map.put("Quantity",jsonObject.getString("erpQuantity").trim());
                map.put("Features",jsonObject.getString("erpFeatures").trim());
                map.put("FeaturesName",jsonObject.getString("erpFeaturesName").trim());
                map.put("FeaturesModels",jsonObject.getString("erpFeaturesModels").trim());
                map.put("Lots",jsonObject.getString("erpLots").trim());
                map.put("Weight",jsonObject.getString("erpWeight").trim());
                map.put("Tray",jsonObject.getString("erpTray").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }

    //解析制造命令xml单条结果数据
    public List<Map<String,Object>> getT100JsonModelData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("ProductCode",jsonObject.getString("erpProductCode").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels",jsonObject.getString("erpProductModels").trim());
                map.put("PlanDate",jsonObject.getString("erpPlanDate").trim());
                map.put("ProcessId",jsonObject.getString("erpProcessId").trim());
                map.put("Process",jsonObject.getString("erpProcess").trim());
                map.put("Device",jsonObject.getString("erpDevice").trim());
                map.put("Emp",jsonObject.getString("erpEmp").trim());
                map.put("Status",jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }

    //解析制造命令xml结果数据
    public List<Map<String,Object>> getT100JsonProductErrorData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("Docno", jsonObject.getString("erpDocno").trim());
                map.put("ProcessId", jsonObject.getString("erpProcessId").trim());
                map.put("Process", jsonObject.getString("erpProcess").trim());
                map.put("Device", jsonObject.getString("erpDevice").trim());
                map.put("Employee", jsonObject.getString("erpEmp").trim());
                map.put("Version", jsonObject.getString("erpVersion").trim());
                map.put("Seq", jsonObject.getString("erpSeq").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析制造命令xml结果数据
    public List<Map<String,Object>> getT100JsonLabelData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("Program", jsonObject.getString("erpProgram").trim());
                map.put("StockId", jsonObject.getString("erpStockId").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("Docno", jsonObject.getString("erpDocno").trim());
                map.put("Process", jsonObject.getString("erpProcess").trim());
                map.put("Device", jsonObject.getString("erpDevice").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("Employee", jsonObject.getString("erpEmployee").trim());
                map.put("Lots", jsonObject.getString("erpLots").trim());
                map.put("Qrcode", jsonObject.getString("erpQrcode").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析生产领料储位xml结果数据
    public List<Map<String,Object>> getT100JsonStockData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("ProductSize", jsonObject.getString("erpProductSize").trim());
                map.put("StockId", jsonObject.getString("erpStockId").trim());
                map.put("StockLocationId", jsonObject.getString("erpStockLocationId").trim());
                map.put("StockLocation", jsonObject.getString("erpStockLocation").trim());
                map.put("StockBatch", jsonObject.getString("erpStockBatch").trim());
                map.put("Inventory", jsonObject.getString("erpInventory").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("QuantityPcs", jsonObject.getString("erpQuantityPcs").trim());
                map.put("ScanQuantity", jsonObject.getString("erpScanQuantity").trim());
                map.put("ScanQuantityPcs", jsonObject.getString("erpScanQuantityPcs").trim());
                map.put("Weight", jsonObject.getString("erpWeight").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("Product", jsonObject.getString("erpProduct").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析盘点清单xml结果数据
    public List<Map<String,Object>> getT100JsonCheckStockData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode",jsonObject.getString("erpProductCode").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels",jsonObject.getString("erpProductModels").trim());
                map.put("PlanDate",jsonObject.getString("erpPlanDate").trim());
                map.put("Quantity",jsonObject.getString("erpQuantity").trim());
                map.put("QuantityPcs",jsonObject.getString("erpQuantityPcs").trim());
                map.put("StockId",jsonObject.getString("erpStockId").trim());
                map.put("Stock",jsonObject.getString("erpStock").trim());
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("StorageId",jsonObject.getString("erpStorageId").trim());
                map.put("Storage",jsonObject.getString("erpStorage").trim());
                map.put("Lot",jsonObject.getString("erpLot").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析扫描二维码xml结果数据
    public List<Map<String,Object>> getT100JsonQrCodeData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode",jsonObject.getString("erpProductCode").trim());
                map.put("StockId",jsonObject.getString("erpStockId").trim());
                map.put("StockLocationId",jsonObject.getString("erpStockLocationId").trim());
                map.put("Quantity",jsonObject.getString("erpQuantity").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels",jsonObject.getString("erpProductModels").trim());
                map.put("ProductSize",jsonObject.getString("erpProductSize").trim());
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("Lots",jsonObject.getString("erpLots").trim());
                map.put("Features",jsonObject.getString("erpFeatures").trim());
                map.put("FeaturesName",jsonObject.getString("erpFeaturesName").trim());
                map.put("FeaturesModels",jsonObject.getString("erpFeaturesModels").trim());
                map.put("PlanDate",jsonObject.getString("erpPlanDate").trim());
                map.put("Weight",jsonObject.getString("erpWeight").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析工单完工入库xml结果数据
    public List<Map<String,Object>> getT100JsonWorkOrderStockinData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("Docno", jsonObject.getString("erpDocno").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("ProducerId", jsonObject.getString("erpProducerId").trim());
                map.put("Producer", jsonObject.getString("erpProducer").trim());
                map.put("StockId", jsonObject.getString("erpStockId").trim());
                map.put("Stock", jsonObject.getString("erpStock").trim());
                map.put("Planno", jsonObject.getString("erpPlanno").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("QuantityPcs", jsonObject.getString("erpQuantityPcs").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                map.put("DocStatus", jsonObject.getString("erpDocStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析QCxml结果数据
    public List<Map<String,Object>> getT100JsonQcData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson.replaceAll("&amp;quot;","\"");
        int iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode",jsonObject.getString("erpProductCode").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels",jsonObject.getString("erpProductModels").trim());
                map.put("ProducerId",jsonObject.getString("erpProducerId").trim());
                map.put("Producer",jsonObject.getString("erpProducer").trim());
                map.put("PlanDate",jsonObject.getString("erpPlanDate").trim());
                map.put("Quantity",jsonObject.getString("erpQuantity").trim());
                map.put("Inventory",jsonObject.getString("erpInventory").trim());
                map.put("QuantityPcs",jsonObject.getString("erpQuantityPcs").trim());
                map.put("StockId",jsonObject.getString("erpStockId").trim());
                map.put("Stock",jsonObject.getString("erpStock").trim());
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("Status",jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int iCurrentStartId = strSubContent.indexOf("/Record", 1);
            int iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //解析回传结果数据
    public List<Map<String,Object>> getT100ResponseData(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("Producer",jsonObject.getString("erpProducer").trim());
                map.put("PlanDate",jsonObject.getString("erpPlanDate").trim());
                map.put("Stock",jsonObject.getString("erpStock").trim());
                map.put("Storage",jsonObject.getString("erpStorage").trim());
                map.put("Quantity",jsonObject.getString("erpQuantity").trim());
                map.put("QuantityPcs",jsonObject.getString("erpQuantityPcs").trim());
                map.put("PlanQuantity",jsonObject.getString("erpPlanQuantity").trim());
                map.put("PlanQuantityPcs",jsonObject.getString("erpPlanQuantityPcs").trim());
                map.put("Status",jsonObject.getString("erpStatus").trim());
                map.put("DocStatus",jsonObject.getString("erpDocStatus").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("Container",jsonObject.getString("erpContainer").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }

    //解析回传结果数据
    public List<Map<String,Object>> getT100ResponseDocno(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("ErrorCount",jsonObject.getString("erpErrorCount").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }

    //解析回传结果数据
    public List<Map<String,Object>> getT100ResponseDocno3(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("ProductCode",jsonObject.getString("erpProductCode").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels",jsonObject.getString("erpProductModels").trim());
                map.put("ProcessId",jsonObject.getString("erpProcessId").trim());
                map.put("Process",jsonObject.getString("erpProcess").trim());
                map.put("Device",jsonObject.getString("erpDevice").trim());
                map.put("Quantity",jsonObject.getString("erpQuantity").trim());
                map.put("Emp",jsonObject.getString("erpEmp").trim());
                map.put("Status",jsonObject.getString("erpStatus").trim());
                map.put("ErrorCount",jsonObject.getString("erpErrorCount").trim());
                map.put("Attribute",jsonObject.getString("erpAttribute").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }

    //解析上料检核回传结果数据
    public List<Map<String,Object>> getT100ResponseDocno4(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("ProductCode",jsonObject.getString("erpProductCode").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels",jsonObject.getString("erpProductModels").trim());
                map.put("ProcessId",jsonObject.getString("erpProcessId").trim());
                map.put("Process",jsonObject.getString("erpProcess").trim());
                map.put("Quantity",jsonObject.getString("erpQuantity").trim());
                map.put("Status",jsonObject.getString("erpStatus").trim());
                map.put("Attribute",jsonObject.getString("erpAttribute").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }

    //解析回传结果数据2
    public List<Map<String,Object>> getT100ResponseDocno2(String listJson, String xmlIndexStr){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        Map<String,Object> map = new HashMap<String,Object>();

        //检查索引
        int iTaskIndex=listJson.indexOf(xmlIndexStr,1);
        if (iTaskIndex>-1){
            //扫描明细
            String strContent =listJson.replaceAll("&amp;quot;","\"");
            String strQr=strContent.substring(strContent.indexOf(xmlIndexStr,1),strContent.length());
            String strQrJson=strQr.substring(strQr.indexOf("value",1)+7,strQr.indexOf("&gt;",1)-2);
            try{
                JSONArray jsonArray = new JSONArray(strQrJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("StartCount",jsonObject.getString("erpStartCount").trim());
                map.put("QcCount",jsonObject.getString("erpQcCount").trim());
                map.put("ErrorCount",jsonObject.getString("erpErrorCount").trim());
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }
}
