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
    public void saveShared(String userId,String userName,String userPassword,String userSite,String network,String userPower){
        SharedPreferences sharedPreferences=mContext.getSharedPreferences("userinfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("userId",userId);
        editor.putString("userName",userName);
        editor.putString("userPassword",userPassword);
        editor.putString("userSite",userSite);
        editor.putString("network",network);
        editor.putString("userPower",userPower);
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
        data.put("userPower",sharedPreferences.getString("userPower",""));
        return data;
    }

    //保存部门
    public void saveDept(String dept){
        SharedPreferences sharedPreferences=mContext.getSharedPreferences("deptinfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("dept",dept);
        editor.commit();
    }

    //读取部门
    public Map<String,String> readDept(){
        Map<String,String> data=new HashMap<String,String>();
        SharedPreferences sharedPreferences=mContext.getSharedPreferences("deptinfo",Context.MODE_PRIVATE);
        data.put("dept",sharedPreferences.getString("dept",""));

        return data;
    }
}
