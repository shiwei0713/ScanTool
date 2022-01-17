package com.hz.scantool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class ErrorActivity extends AppCompatActivity {

    private String qrCode;

    private TextView errorCode;
    private TextView errorResult;
    private Button btnSubmit;
    private Button btnCancel;
    private ProgressBar progressBar;

    private String statusCode;
    private String statusDescription;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        initBundle();
        intiView();
        if(!qrCode.isEmpty()){
            getScanQrData(qrCode);
        }
    }

    //获取传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        qrCode = bundle.getString("qrCode");
    }

    //初始化控件
    private void intiView(){
        errorCode = findViewById(R.id.errorCode);
        errorResult = findViewById(R.id.errorResult);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);

        errorCode.setText(qrCode);
        btnCancel.setVisibility(View.INVISIBLE);
        //绑定按钮事件
        btnSubmit.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
    }

    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnSubmit:
                    getScanQrData(qrCode);
                    break;
                case R.id.btnCancel:
                    finish();
                    break;
            }
        }
    }

    //获取扫描条码信息
    private void getScanQrData(String qrCode){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "GetQrCode";
                String qrStatus = "Y";   //扫描状态记录，Y代表车间已扫描

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaa011\" value=\""+qrCode+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;Field name=\"bcaa016\" value=\""+qrStatus+"\"/&gt;\n"+
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaa000\" value=\"1.0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Detail&gt;\n"+
                        "&lt;Memo/&gt;\n"+
                        "&lt;Attachment count=\"0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Master&gt;\n"+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseList = t100ServiceHelper.getT100JsonQrCodeData(strResponse,"qrcode");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

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

                        if(!statusCode.equals("0")){
                            MyToast.myShow(ErrorActivity.this,statusDescription,0,0);
                        }else{
                            int progress = progressBar.getProgress();
                            progress = progress + 50;
                            progressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(ErrorActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(ErrorActivity.this,"网络错误",0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                if(mapResponseList.size()>0){
                    String sErpDocno = "";

                    for(Map<String,Object> mScanData: mapResponseList){
                        sErpDocno = mScanData.get("erpDocno").toString();
                    }

                    errorResult.setText(sErpDocno);

                    if(!sErpDocno.isEmpty()){
                        btnCancel.setVisibility(View.VISIBLE);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}