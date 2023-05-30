package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyAlertDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ResetUserActivity extends AppCompatActivity {

    private EditText newPassword,confirmPassword,oldPassword;
    private ImageButton showNewPassword,showConfirmPassword,showOldPassword;
    private Button btnSave,btnCancel;
    private boolean isShow;
    private String sUserCode;
    private String statusCode,statusDescription;
    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_user);

        //初始化
        initBundle();
        initView();
        initData();
    }

    /**
    *描述: 获取传入参数
    *日期：2022/12/27
    **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        sUserCode = bundle.getString("UserCode");
    }

    /**
    *描述: 初始化控件
    *日期：2022/12/27
    **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.resetUserToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(getResources().getString(R.string.reset_user_title));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        oldPassword = findViewById(R.id.oldPassword);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        showNewPassword = findViewById(R.id.showNewPassword);
        showConfirmPassword = findViewById(R.id.showConfirmPassword);
        showOldPassword = findViewById(R.id.showOldPassword);

        //初始化事件
        btnSave.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
        showNewPassword.setOnClickListener(new btnClickListener());
        showConfirmPassword.setOnClickListener(new btnClickListener());
        showOldPassword.setOnClickListener(new btnClickListener());
    }

    /**
    *描述: 初始化值
    *日期：2022/12/27
    **/
    private void initData(){
        isShow = false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
    *描述: 事件实现
    *日期：2022/12/27
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSave:
                    if(isCheck()){
                        updateUserData();
                    }
                    break;
                case R.id.btnCancel:
                    finish();
                    break;
                case R.id.showNewPassword:
                    showPassword(newPassword);
                    break;
                case R.id.showConfirmPassword:
                    showPassword(confirmPassword);
                    break;
                case R.id.showOldPassword:
                    showPassword(oldPassword);
                    break;
            }
        }
    }

    /**
    *描述: 显示密码
    *日期：2022/12/27
    **/
    private void showPassword(EditText editText){
        if(!isShow){
            //显示密码
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isShow = true;
        }else{
            //隐藏密码
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isShow = false;
        }
    }

    /**
    *描述: 检核密码
    *日期：2022/12/27
    **/
    private boolean isCheck(){
        String sNewPassword = newPassword.getText().toString();
        if(sNewPassword.equals("")||sNewPassword.isEmpty()){
            MyAlertDialog.myShowAlertDialog(ResetUserActivity.this,"错误信息","密码不可为空");
            return false;
        }else{
            String sConfirmPassword = confirmPassword.getText().toString();
            if(!sConfirmPassword.equals(sNewPassword)){
                MyAlertDialog.myShowAlertDialog(ResetUserActivity.this,"错误信息","两次密码不一致");
                return false;
            }
        }

        return true;
    }

    /**
     *描述: 修改个人密码
     *日期：2022/6/12
     **/
    private void updateUserData(){
        //显示进度条
        loadingDialog = new LoadingDialog(ResetUserActivity.this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "UserUpdate";
                String action = "update";
                String sOldPassword = oldPassword.getText().toString();
                String sNewPassword = newPassword.getText().toString();

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"rpxa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"rpxaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"rpxamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"rpxa001\" value=\""+ sUserCode +"\"/&gt;\n"+  //用户编号
                        "&lt;Field name=\"rpxaold\" value=\""+ sOldPassword +"\"/&gt;\n"+  //用户旧密码
                        "&lt;Field name=\"rpxaua002\" value=\""+ sNewPassword +"\"/&gt;\n"+  //用户新密码
                        "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //执行动作
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sfaaucseq\" value=\"1.0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Detail&gt;\n"+
                        "&lt;Memo/&gt;\n"+
                        "&lt;Attachment count=\"0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Master&gt;\n"+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

                e.onNext(mapResponseStatus);
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
                    MyToast.myShow(ResetUserActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(ResetUserActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(ResetUserActivity.this, statusDescription, 1, 1);
                    finish();
                }else{
                    MyAlertDialog.myShowAlertDialog(ResetUserActivity.this,"错误信息",statusDescription);
                }

                loadingDialog.dismiss();
            }
        });
    }
}