package com.hz.scantool;

import android.content.Context;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateManager {

    private static UpdateManager updateManager = null;

    private static final String ARG_NETWORK_WLAN="外部网络";
    private static final String WLAN_UPDATE_URL="http://119.97.210.146:8090/app/version.aspx";
    private static final String LAN_UPDATE_URL="http://192.168.210.1/app/version.aspx";
    private static final String WLAN_DOWNLOAD_URL="http://119.97.210.146:8090/app/ScanTool.apk";
    private static final String LAN_DOWNLOAD_URL="http://192.168.210.1/app/ScanTool.apk";

    private String serverUpdateUrl = "";
    private String serverApkUrl = "";   //APK下载地址
    private int sVersion;
    private String sVersionName;
    private int version;
    private String versionName;
    private String netWorkType;

    private UpdateManager(String netWorkType){
        this.netWorkType = netWorkType;
    }

    public String getServerApkUrl() {
        if(netWorkType.equals(ARG_NETWORK_WLAN)){
            serverApkUrl = WLAN_DOWNLOAD_URL;
        }else{
            serverApkUrl = LAN_DOWNLOAD_URL;
        }
        return serverApkUrl;
    }

    //实例化对象
    public static UpdateManager getInstance(String nerworkType){
        updateManager = new UpdateManager(nerworkType);
        return updateManager;
    }

    //获取当前程序版本号
    public int getVersion(Context context){
        version = 0;

        try{
            version = context.getPackageManager().getPackageInfo("com.hz.scantool",0).versionCode;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        return version;
    }

    //获取当前程序版本名
    public String getVersionName(Context context){
        versionName = "";

        try{
            versionName = context.getPackageManager().getPackageInfo("com.hz.scantool",0).versionName;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        return versionName;
    }

    //获取服务器程序版本号和版本名
    public int getServerVersion(){
        sVersion = 0;
        sVersionName="";

        if(netWorkType.equals(ARG_NETWORK_WLAN)){
            serverUpdateUrl=WLAN_UPDATE_URL;
        }else{
            serverUpdateUrl=LAN_UPDATE_URL;
        }

        try{
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(serverUpdateUrl)
                    .method("POST", body)
                    .build();
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                String strResponse = response.body().string();
                JSONArray jsonArray = new JSONArray(strResponse);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                sVersion = jsonObject.getInt("VersionCode");
                sVersionName = jsonObject.getString("VersionName");
            }
        }catch (JSONException | IOException e){
            e.printStackTrace();
        }

        return sVersion;
    }
}
