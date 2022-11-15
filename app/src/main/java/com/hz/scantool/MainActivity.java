package com.hz.scantool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.helper.SharedHelper;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Rxjava";
    private static final String ARG_NETWORK_WLAN="外部网络";

    private String userId="";
    private String userName="";
    private String userStatus="";
    private int iServerVersion;
    private TextView txtUserName;
    private TextView txtUserPassword;
    private TextView txtAppVersion;
    private TextView txtLoginContent;
    private RadioButton rBtnLan;
    private RadioButton rBtnWlan;
    private Spinner spinnerSite;
    private SharedHelper sharedHelper;
    private Context mContext;
    private String nerworkType;
    private LoadingDialog loadingDialog;
    private String statusCode;
    private String statusDescription;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取版本信息
        txtAppVersion = findViewById(R.id.txtAppVersion);
        txtAppVersion.setText(UpdateManager.getInstance(nerworkType).getVersionName(this));

        //初始化存储信息
        mContext=getApplicationContext();
        sharedHelper=new SharedHelper(mContext);

        //设置下拉控件
        setSpinner();

        //网络类型事件绑定
        rBtnLan=findViewById(R.id.rBtnLan);
        rBtnWlan=findViewById(R.id.rBtnWlan);
        rBtnLan.setOnClickListener(new netWorkChangeListener());
        rBtnWlan.setOnClickListener(new netWorkChangeListener());

        //按钮事件处理
        txtUserName = findViewById(R.id.txtUserName);
        txtUserPassword = findViewById(R.id.txtPassword);
        Button btnLogin=findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new loginClickListener());
        Button btnRest=findViewById(R.id.btnReset);
        btnRest.setOnClickListener(new resetClickListener());

        txtLoginContent = findViewById(R.id.txtLoginContent);
        txtLoginContent.setOnClickListener(new txtClickListener());

        //测试网络
        testNetwork();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //初始化用户、密码、网络
        txtUserName.setText(UserInfo.getUserId(getApplicationContext()));
//        txtUserPassword.setText(UserInfo.getUserPassword(getApplicationContext()));
        txtUserPassword.setText("");
        nerworkType = UserInfo.getUserNetwork(getApplicationContext());

        if(nerworkType.equals(ARG_NETWORK_WLAN)){
            rBtnLan.setChecked(false);
            rBtnWlan.setChecked(true);
        }else{
            rBtnLan.setChecked(true);
            rBtnWlan.setChecked(false);
        }

        //初始化公司
        spinnerSite.setSelection(UserInfo.getUserSiteCode(getApplicationContext()));
    }

    //下拉控件样式
    private void setSpinner(){
        spinnerSite = findViewById(R.id.cmbSite);
        ArrayAdapter arrayAdapter=ArrayAdapter.createFromResource(this,R.array.site,R.layout.support_simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerSite.setAdapter(arrayAdapter);

        //添加事件监听
        spinnerSite.setOnItemSelectedListener(new spinnerXMLSelectedListener());

        //设置默认值
        spinnerSite.setVisibility(View.VISIBLE);

    }

    //下拉控件事件
    public class spinnerXMLSelectedListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class netWorkChangeListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.rBtnLan:
                    nerworkType=rBtnLan.getText().toString();
                    break;
                case R.id.rBtnWlan:
                    nerworkType=rBtnWlan.getText().toString();
                    break;
            }
        }
    }

    public String Ping(String str) {
        String result = "";
        Process p;
        try {
            p = Runtime.getRuntime().exec("ping -c 1 -w 3 " + str);
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null){
                buffer.append(line);
            }
            input.close();
            in.close();
            if(buffer.toString().indexOf("100%")!=-1||buffer.toString().equals("")){
                result = "fail";
            }  else{
                result = "success";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void testNetwork(){
        Observable.create(new ObservableOnSubscribe<String>(){

            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                String ip = "192.168.210.3";
                String str=Ping(ip);

                e.onNext(str);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>(){

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                if(s.equals("success")){
                    nerworkType=rBtnLan.getText().toString();
                    rBtnLan.setChecked(true);
                    rBtnWlan.setChecked(false);
                }else{
                    nerworkType=rBtnWlan.getText().toString();
                    rBtnLan.setChecked(false);
                    rBtnWlan.setChecked(true);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    //提交按钮事件
    public class loginClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //检查用户是否为空，非空执行获取用户信息线程
            String strUserName = txtUserName.getText().toString();
            if(strUserName.equals("")){
                MyToast.myShow(MainActivity.this,"请输入用户工号",2,0);
            }else {
//                getUserLogin();
                getUserData();

                Log.i("MACADDRESS",UserInfo.getMacAddress());
            }
        }
    }

    //重置按钮事件
    public class resetClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent=new Intent(MainActivity.this,ResetUserActivity.class);
            startActivity(intent);
        }
    }

    private class txtClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.txtLoginContent:
                    Uri uri = Uri.parse("http://192.168.210.1/app");
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    startActivity(intent);
                    break;
            }
        }
    }

    //获取用户信息
    private void getUserLogin(){
        loadingDialog = new LoadingDialog(this,"正在登录,请稍后",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<String>(){

            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                //检查版本
                int currentVersion = UpdateManager.getInstance(nerworkType).getVersion(MainActivity.this);
                int serverVersion = UpdateManager.getInstance(nerworkType).getServerVersion();

                if(currentVersion == serverVersion){
                    T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                    String requestBody = "&lt;Parameter&gt;\n"+
                            "&lt;Record&gt;\n"+
                            "&lt;Field name=\"enterprise\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                            "&lt;Field name=\"account\" value=\""+txtUserName.getText().toString()+"\"/&gt;\n"+
                            "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                            "&lt;/Record&gt;\n"+
                            "&lt;/Parameter&gt;\n"+
                            "&lt;Document/&gt;\n";
                    String strResponse = t100ServiceHelper.getT100Data(requestBody,"UserInfoGet",getApplicationContext(),nerworkType);
                    if (strResponse.indexOf("rpxa002",1)>-1){
                        //查找工号
                        String sUserId=strResponse.substring(strResponse.indexOf("rpxa002",1),strResponse.length());
                        userId=sUserId.substring(sUserId.indexOf("value",1)+7,sUserId.indexOf("&gt;",1)-2);

                        //查找姓名
                        String sUserName=strResponse.substring(strResponse.indexOf("rpxa003",1),strResponse.length());
                        userName=sUserName.substring(sUserName.indexOf("value",1)+7,sUserName.indexOf("&gt;",1)-2);

                    }else{
                        userStatus = "用户不存在,请联系系统管理员";
                    }
                }else{
                    if(serverVersion == 0){
                        userStatus = "网络异常";
                    }else{
                        userStatus = "当前版本低,请升级后使用";
                    }
                }

                e.onNext(userId);
                e.onNext(userName);
                e.onNext(userStatus);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>(){

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                if(!userId.isEmpty()){
                    txtUserName.setText(userId);
                    sharedHelper.saveShared(userId,userName,txtUserPassword.getText().toString(),spinnerSite.getSelectedItem().toString(),nerworkType,"");
                    Intent intent=new Intent(MainActivity.this,MasterActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    if(userStatus.isEmpty()){
                        MyToast.myShow(MainActivity.this,"用户不存在,请联系系统管理员",0,0);
                    }{
                        MyToast.myShow(MainActivity.this,userStatus,0,0);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(MainActivity.this,"网络异常,请联系系统管理员",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                loadingDialog.dismiss();
            }
        });
    }

    /**
    *描述: 获取系统版本
    *日期：2022/7/28
    **/
    private void getServerVersion(){

        Observable.create(new ObservableOnSubscribe<Integer>(){

            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                //检查版本
                int serverVersion = UpdateManager.getInstance(nerworkType).getServerVersion();

                e.onNext(serverVersion);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>(){


            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                iServerVersion = integer;
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(MainActivity.this,e.getMessage(),0,0);
            }

            @Override
            public void onComplete() {

            }
        });

    }

    //获取用户数据-账号/密码/权限/PDA
    private void getUserData(){
        //显示进度条
        if(loadingDialog==null){
            loadingDialog = new LoadingDialog(MainActivity.this,"正在登录,请稍后",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "UserGet";
                int iVersion = getPackageManager().getPackageInfo("com.hz.scantool",0).versionCode;
                String sVersion = getPackageManager().getPackageInfo("com.hz.scantool",0).versionName;

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"account\" value=\""+ txtUserName.getText().toString() +"\"/&gt;\n"+
                        "&lt;Field name=\"appversion\" value=\""+ iVersion +"\"/&gt;\n"+
                        "&lt;Field name=\"appversionname\" value=\""+ sVersion +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),nerworkType);
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100UserData(strResponse,"userinfo");

                e.onNext(mapResponseStatus);
                e.onNext(mapResponseList);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<Map<String, Object>>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<Map<String, Object>> maps) {
                if(mapResponseStatus.size()> 0){
                    for(Map<String,Object> mStatus: mapResponseStatus){
                        statusCode = mStatus.get("statusCode").toString();
                        statusDescription = mStatus.get("statusDescription").toString();
                    }
                }else{
                    MyToast.myShow(MainActivity.this,"用户不存在",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(MainActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(!statusCode.equals("0")){
                    MyToast.myShow(MainActivity.this,statusDescription,0,0);
                }else{
                    if(mapResponseList.size()> 0) {
                        String strUserCode = "";
                        String strUserName = "";
                        String strMacAddress = "";
                        String strUserPassword = "";
                        String strPower = "";
                        boolean isLogin = true;

                        for (Map<String, Object> mResponse : mapResponseList) {
                            strUserCode = mResponse.get("UserCode").toString();
                            strUserName = mResponse.get("UserName").toString();
                            strMacAddress = mResponse.get("MacAddress").toString();
                            strUserPassword = mResponse.get("UserPassword").toString();
                            strPower = mResponse.get("Power").toString();
                        }

                        if(!strUserCode.isEmpty()){
                            String txtMacAddress = UserInfo.getMacAddress();
                            if(!strMacAddress.isEmpty()){
                                if(!txtMacAddress.equals(strMacAddress)){
                                    isLogin = false;
                                    MyToast.myShow(MainActivity.this,"此用户不可使用PDA",0,0);
                                }
                            }

                            if(isLogin){
                                String txtPassword = txtUserPassword.getText().toString();
                                if(strUserPassword.equals(txtPassword) ){
                                    txtUserName.setText(strUserCode);
                                    sharedHelper.saveShared(strUserCode,strUserName,txtPassword,spinnerSite.getSelectedItem().toString(),nerworkType,strPower);
                                    finish();
                                    Intent intent=new Intent(MainActivity.this,MasterActivity.class);
                                    startActivity(intent);
                                }else{
                                    MyToast.myShow(MainActivity.this,"密码错误",0,0);
                                }
                            }
                        }else{
                            MyToast.myShow(MainActivity.this,"用户不存在,请联系系统管理员",0,0);
                        }
                    }
                }

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}