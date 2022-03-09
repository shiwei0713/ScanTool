package com.hz.scantool.models;

import android.content.Context;

import com.hz.scantool.helper.SharedHelper;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

public class UserInfo {
    private static String userId;
    private static String userName;
    private static String userPassword;
    private static String userSite;
    private static String userSiteId;
    private static Integer userSiteCode;
    private static String userNetwork;
    private static String userEnterprise;
    private static String macAddress;
    private static String userDept;

    private static SharedHelper sharedHelper;
    private static Map<String,String> userData;
    private static Map<String,String> userDeptData;

    private static Map<String,String> getUserData(Context mContext) {
        sharedHelper = new SharedHelper(mContext);
        userData = sharedHelper.readShared();

        return userData;
    }

    private static Map<String,String> getUserDept(Context mContext){
        sharedHelper = new SharedHelper(mContext);
        userDeptData = sharedHelper.readDept();

        return userDeptData;
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

    //获取用户盘点部门
    public static String getDept(Context mContext){
        userDept = getUserDept(mContext).get("dept");

        return userDept;
    }

    //网络类型
    public static String getUserNetwork(Context mContext){
        userNetwork = getUserData(mContext).get("network");

        return userNetwork;
    }

    //企业编号
    public static String getUserEnterprise(Context mContext){
        userEnterprise = "12";

        return userEnterprise;
    }

    //设备网卡地址
    public static String getMacAddress(){
        macAddress = "";
        try{
            InetAddress inetAddress = getLocalInetAddress();
            byte[] bytes = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0;i<bytes.length;i++){
                if(i!=0){
                    stringBuilder.append(":");
                }

                String str = Integer.toHexString(bytes[i]&0xFF);
                stringBuilder.append(str.length()==1?0+str:str);
            }
            macAddress = stringBuilder.toString().toUpperCase();
        }catch (SocketException e){
            e.printStackTrace();
        }

        return macAddress;
    }

    private static InetAddress getLocalInetAddress() {
        InetAddress inetAddress = null;
        try{
            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
            while(enumeration.hasMoreElements()){
                NetworkInterface networkInterface = (NetworkInterface) enumeration.nextElement();
                Enumeration enumerationIp = networkInterface.getInetAddresses();
                while (enumerationIp.hasMoreElements()){
                    inetAddress = (InetAddress) enumerationIp.nextElement();
                    if(!inetAddress.isLoopbackAddress()&&inetAddress.getHostAddress().indexOf(":")==-1){
                        break;
                    }else{
                        inetAddress = null;
                    }
                }

                if(inetAddress !=null){
                    break;
                }
            }
        }catch (SocketException e){
            e.printStackTrace();
        }

        return inetAddress;
    }

}
