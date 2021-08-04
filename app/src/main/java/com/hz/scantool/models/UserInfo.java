package com.hz.scantool.models;

import android.content.Context;

import com.hz.scantool.helper.SharedHelper;

import java.util.Map;

public class UserInfo {
    private static String userId;
    private static String userName;
    private static String userPassword;
    private static String userSite;
    private static String userSiteId;
    private static Integer userSiteCode;
    private static String userNetwork;

    private static SharedHelper sharedHelper;
    private static Map<String,String> userData;

    private static Map<String,String> getUserData(Context mContext) {
        sharedHelper = new SharedHelper(mContext);
        userData = sharedHelper.readShared();

        return userData;
    }

    public static String getUserId(Context mContext) {
        userId = getUserData(mContext).get("userId");

        return userId;
    }

    public static String getUserName(Context mContext) {
        userName = getUserData(mContext).get("userName");

        return userName;
    }

    public static String getUserPassword(Context mContext) {
        userPassword = getUserData(mContext).get("userPassword");

        return userPassword;
    }

    //营运据点中文名称
    public static String getUserSite(Context mContext) {
        userSite = getUserData(mContext).get("userSite");

        return userSite;
    }

    //营运据点编码:HZ10/HZ20/HZ50
    public static String getUserSiteId(Context mContext) {
        Company company = new Company();
        company.setSite(UserInfo.getUserSite(mContext));
        userSiteId = company.getCode();

        return userSiteId;
    }

    //spinner选择框ID
    public static Integer getUserSiteCode(Context mContext){
        Company company = new Company();
        company.setSite(UserInfo.getUserSite(mContext));

        userSiteCode = company.getId();

        return userSiteCode;
    }

    public static String getUserNetwork(Context mContext){
        userNetwork = getUserData(mContext).get("network");

        return userNetwork;
    }
}
