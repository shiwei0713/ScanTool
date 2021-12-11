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
    private static final String SERVICE_ENTERPRISE="12";
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
                detailList.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return detailList;
    }
}
