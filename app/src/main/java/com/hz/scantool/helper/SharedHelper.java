package com.hz.scantool.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class SharedHelper {
    private Context mContext;

    public SharedHelper(Context mContext){
        this.mContext=mContext;
    }

    //保存数据
    public void saveShared(String userId,String userName,String userPassword,String userSite,String network){
        SharedPreferences sharedPreferences=mContext.getSharedPreferences("userinfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("userId",userId);
        editor.putString("userName",userName);
        editor.putString("userPassword",userPassword);
        editor.putString("userSite",userSite);
        editor.putString("network",network);
        editor.commit();
    }

    //读取数据
    public Map<String,String> readShared(){
        Map<String,String> data=new HashMap<String,String>();
        SharedPreferences sharedPreferences=mContext.getSharedPreferences("userinfo",Context.MODE_PRIVATE);
        data.put("userId",sharedPreferences.getString("userId",""));
        data.put("userName",sharedPreferences.getString("userName",""));
        data.put("userPassword",sharedPreferences.getString("userPassword",""));
        data.put("userSite",sharedPreferences.getString("userSite",""));
        data.put("network",sharedPreferences.getString("network",""));

        return data;
    }
}
