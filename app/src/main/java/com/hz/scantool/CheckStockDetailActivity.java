package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
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

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class CheckStockDetailActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private int intIndex;
    private String strTitle;

    private String statusCode;
    private String statusDescription;

    private TextView inputDetailProductCode;
    private TextView inputDetailProductModels;
    private TextView inputDetailModel;
    private TextView inputDetailStartPlanDate;
    private EditText inputDetailQuantity;
    private TextView inputDetailDocno;
    private Button btnDetailSubmit;

    private LoadingDialog loadingDialog;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_stock_detail);

        //初始化参数
        initBundle();
        initView();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.checkStockDetailToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //初始化参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        intIndex = bundle.getInt("index");
    }

    private void initView(){
        inputDetailProductCode = findViewById(R.id.inputDetailProductCode);
        inputDetailProductModels = findViewById(R.id.inputDetailProductModels);
        inputDetailModel = findViewById(R.id.inputDetailModel);
        inputDetailStartPlanDate = findViewById(R.id.inputDetailStartPlanDate);
        inputDetailQuantity = findViewById(R.id.inputDetailQuantity);
        inputDetailDocno = findViewById(R.id.inputDetailDocno);
        btnDetailSubmit = findViewById(R.id.btnDetailSubmit);

        btnDetailSubmit.setOnClickListener(new submitDataClickListener());
    }

    private class submitDataClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnDetailSubmit:

                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(CheckStockDetailActivity.this);
                intentIntegrator.setTimeout(5000);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //开始扫描
                intentIntegrator.initiateScan();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SCANACTION);
        intentFilter.setPriority(Integer.MAX_VALUE);
        registerReceiver(scanReceiver,intentFilter);
    }

    //PDA扫描数据接收
    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SCANACTION)){
                String qrContent = intent.getStringExtra("scannerdata");

                if(qrContent!=null && qrContent.length()!=0){
                    scanResult(qrContent,context,intent);
                }else{
                    MyToast.myShow(context,"扫描失败,请重新扫描",0,0);
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(scanReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE){
            IntentResult intentResult = IntentIntegrator.parseActivityResult(resultCode,data);
            String qrContent = intentResult.getContents();
            Intent intent = null;

            if(qrContent!=null && qrContent.length()!=0){
                scanResult(qrContent,this,intent);
            }else{
                MyToast.myShow(this,"条码错误,请重新扫描"+qrContent,0,0);
            }
        }
    }

    //扫描结果解析
    private void scanResult(String qrContent,Context context, Intent intent){
        //解析二维码
        String[] qrCodeValue = qrContent.split("_");
        int qrIndex = qrContent.indexOf("_");
        if(qrIndex==-1){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            getScanQrData(qrCodeValue[0].toString());
        }
    }

    //获取扫描条码信息
    private void getScanQrData(String qrCode){
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "GetQrCode";
                String qrStatus = "CK";  //扫描状态CK盘点

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
                            MyToast.myShow(CheckStockDetailActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(CheckStockDetailActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckStockDetailActivity.this,"网络错误",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(mapResponseList.size()>0){
                    for(Map<String,Object> mData: mapResponseList){
                        String sProductCode = mData.get("ProductCode").toString();
                        String sProductName = mData.get("ProductName").toString();
                        String sProductModels = mData.get("ProductModels").toString();
                        String sProductSize = mData.get("ProductSize").toString();
                        String sDocno = mData.get("Docno").toString();
                        String sPlanDate = mData.get("PlanDate").toString();
                        String sQuantity = mData.get("Quantity").toString();
                        String sWeight = mData.get("Weight").toString();

                        inputDetailProductCode.setText(sProductCode);
                        inputDetailProductModels.setText(sProductName);
                        inputDetailModel.setText(sProductSize+"/"+sProductModels);
                        inputDetailStartPlanDate.setText(sPlanDate);
                        inputDetailQuantity.setText(sQuantity);
                        inputDetailDocno.setText(sDocno);
                    }
                }
                loadingDialog.dismiss();
            }
        });
    }
}