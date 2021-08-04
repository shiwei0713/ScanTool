package com.hz.scantool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.helper.SharedHelper;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.Company;
import com.hz.scantool.models.UserInfo;

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
    private TextView txtUserName;
    private TextView txtUserPassword;
    private TextView txtAppVersion;
    private RadioButton rBtnLan;
    private RadioButton rBtnWlan;
    private Spinner spinnerSite;
    private SharedHelper sharedHelper;
    private Context mContext;
    private String nerworkType;

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        //初始化用户、密码、网络
        txtUserName.setText(UserInfo.getUserId(getApplicationContext()));
        txtUserPassword.setText(UserInfo.getUserPassword(getApplicationContext()));
        nerworkType = UserInfo.getUserNetwork(getApplicationContext());

        if(nerworkType.equals(ARG_NETWORK_WLAN)){
            rBtnLan.setChecked(false);
            rBtnWlan.setChecked(true);
        }else{
            rBtnLan.setChecked(true);
            rBtnWlan.setChecked(false);
        }

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

    //提交按钮事件
    public class loginClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //检查用户是否为空，非空执行获取用户信息线程
            String strUserName = txtUserName.getText().toString();
            if(strUserName.equals("")){
                MyToast.myShow(MainActivity.this,"请输入用户工号",2);
            }else {
                getUserLogin();
            }
        }
    }

    //重置按钮事件
    public class resetClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            txtUserName.setText("");
        }
    }

    //获取用户信息
    private void getUserLogin(){
        Observable.create(new ObservableOnSubscribe<String>(){

            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                //检查版本
                Integer currentVersion = UpdateManager.getInstance(nerworkType).getVersion(MainActivity.this);
                Integer serverVersion = UpdateManager.getInstance(nerworkType).getServerVersion();

                if(currentVersion == serverVersion){
                    T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                    String requestBody = "&lt;Parameter&gt;\n"+
                            "&lt;Record&gt;\n"+
                            "&lt;Field name=\"enterprise\" value=\"10\"/&gt;\n"+
                            "&lt;Field name=\"account\" value=\""+txtUserName.getText().toString()+"\"/&gt;\n"+
                            "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                            "&lt;/Record&gt;\n"+
                            "&lt;/Parameter&gt;\n"+
                            "&lt;Document/&gt;\n";
                    String strResponse = t100ServiceHelper.getT100Data(requestBody,"UserInfoGet",getApplicationContext());
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
                Log.i(TAG,"Observer onSubscribe");
            }

            @Override
            public void onNext(String s) {
                Log.i(TAG,"Observer onNext");
                if(!userId.isEmpty()){
                    txtUserName.setText(userId);
                    sharedHelper.saveShared(userId,userName,txtUserPassword.getText().toString(),spinnerSite.getSelectedItem().toString(),nerworkType);
                    Intent intent=new Intent(MainActivity.this,MasterActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    if(userStatus.isEmpty()){
                        MyToast.myShow(MainActivity.this,"用户不存在,请联系系统管理员",0);
                    }{
                        MyToast.myShow(MainActivity.this,userStatus,0);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG,"Observer onError");
                MyToast.myShow(MainActivity.this,"网络异常,请联系系统管理员",0);
            }

            @Override
            public void onComplete() {
                Log.i(TAG,"Observer onComplete");
            }
        });
    }
}