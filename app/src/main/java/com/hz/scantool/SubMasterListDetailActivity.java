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
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.StockPositionAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
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

public class SubMasterListDetailActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private String strTitle="";
    private String strType;
    private String statusCode;
    private String statusDescription;
    private String strStatus;
    private String strDocStatus;

    private TextView txtSubListDetailDocno;
    private TextView txtSubListDetailStock;
    private TextView txtSubListDetailDept;
    private TextView txtSubListDetailPlanDate;
    private TextView txtSubListDetailPosition;
    private TextView txtSubListDetailQuantityTitle;
    private TextView txtSubListDetailQuantity;
    private TextView txtSubListDetailProductName;
    private TextView txtSubListDetailPlanQuantity;
    private TextView txtSubListDetailPlanQuantityPcs;
    private TextView txtSubListDetailContainer;
    private Button btnSubmit;
    private Button btnCancel;
    private Bundle bundle;
    private LoadingDialog loadingDialog;
    private ListView stockPositionList;
    private StockPositionAdapter stockPositionAdapter;

    private List<Map<String,Object>> mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_master_list_detail);

        //初始化传入参数
        initBundle();
        initView();

        //填充清单
        initListView();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subMasterListDetailToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubMasterListDetailActivity.this);
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

    //获取传入参数
    private void initBundle(){
        Intent intent = getIntent();
        bundle = intent.getExtras();
        strType = bundle.getString("Type");
    }

    private void initView(){
        txtSubListDetailDocno = findViewById(R.id.txtSubListDetailDocno);
        txtSubListDetailStock = findViewById(R.id.txtSubListDetailStock);
        txtSubListDetailDept = findViewById(R.id.txtSubListDetailDept);
        txtSubListDetailPlanDate = findViewById(R.id.txtSubListDetailPlanDate);
        txtSubListDetailPosition = findViewById(R.id.txtSubListDetailPosition);
        txtSubListDetailQuantityTitle =findViewById(R.id.txtSubListDetailQuantityTitle);
        txtSubListDetailQuantity = findViewById(R.id.txtSubListDetailQuantity);
        txtSubListDetailProductName = findViewById(R.id.txtSubListDetailProductName);
        txtSubListDetailPlanQuantity = findViewById(R.id.txtSubListDetailPlanQuantity);
        txtSubListDetailPlanQuantityPcs = findViewById(R.id.txtSubListDetailPlanQuantityPcs);
        txtSubListDetailContainer = findViewById(R.id.txtSubListDetailContainer);
        stockPositionList = findViewById(R.id.stockPositionList);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        txtSubListDetailDocno.setText(bundle.getString("Docno"));
        txtSubListDetailStock.setText(bundle.getString("Stock"));
        txtSubListDetailDept.setText(bundle.getString("Producer"));
        txtSubListDetailPlanDate.setText(bundle.getString("PlanDate"));
        txtSubListDetailPosition.setText(bundle.getString("Storage"));
        txtSubListDetailQuantity.setText(bundle.getString("Quantity"));
        txtSubListDetailPlanQuantity.setText(bundle.getString("PlanQuantity"));
        txtSubListDetailPlanQuantityPcs.setText(bundle.getString("PlanQuantityPcs"));
        txtSubListDetailProductName.setText(bundle.getString("ProductName"));
        txtSubListDetailContainer.setText(bundle.getString("Container"));
        strStatus = bundle.getString("Status");
        strDocStatus = bundle.getString("DocStatus");

        if(strDocStatus.equals("Y")){
            btnSubmit.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.GONE);
        }else{
            btnSubmit.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
            if(strDocStatus.equals("X")){
                txtSubListDetailQuantityTitle.setVisibility(View.GONE);
                txtSubListDetailQuantity.setVisibility(View.GONE);
                txtSubListDetailPosition.setTextColor(Color.RED);
            }else{
                txtSubListDetailPosition.setTextColor(Color.RED);
            }
        }

        if(strType.equals("2")){
            strTitle = getResources().getString(R.string.master_action3);
        }else{
            strTitle = getResources().getString(R.string.master_action4);
        }

        btnSubmit.setOnClickListener(new listDetailClickListener());
        btnCancel.setOnClickListener(new listDetailClickListener());
    }

    private void initListView(){
        String sStorage = bundle.getString("Storage");
        if(sStorage.isEmpty()||sStorage.equals("")){
            return;
        }else{
            int iArrayIndex = sStorage.indexOf("/");

            List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
            if(iArrayIndex>-1){
                String[] sArray = sStorage.split("/");

                for(int i=0;i<sArray.length;i++){
                    String sDesc = sArray[i];
                    int iIndex = sDesc.indexOf("_");
                    String sMapDesc = sDesc.substring(iIndex,sDesc.length());
                    if(sMapDesc.equals("_")){
                        sMapDesc = "不合箱";
                    }
                    String sMapPosition = sDesc.substring(0,iIndex);

                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("Desc", sMapDesc);
                    map.put("Position", sMapPosition);
                    mapList.add(map);
                }
            }else{
                String sDesc = sStorage;
                int iIndex = sDesc.indexOf("_");
                String sMapDesc = sDesc.substring(iIndex,sDesc.length());
                if(sMapDesc.equals("_")){
                    sMapDesc = "不合箱";
                }
                String sMapPosition = sDesc.substring(0,iIndex);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("Desc", sMapDesc);
                map.put("Position", sMapPosition);
                mapList.add(map);
            }

            stockPositionAdapter = new StockPositionAdapter(mapList,getApplicationContext());
            stockPositionList.setAdapter(stockPositionAdapter);
        }
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

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(scanReceiver);
    }

    private class listDetailClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSubmit:
                    checkConfirm();
                    finish();
                    break;
                case R.id.btnCancel:
                    finish();
                    break;
            }
        }
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

    //手机调用摄像头扫描
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
//            MyToast.myShow(context,"扫描功能未启用",2,1);
            finish();
        }
    }

    //过账检核
    private void checkConfirm(){
        String strQuantity = txtSubListDetailQuantity.getText().toString().trim();
        String strPlanQuantity = txtSubListDetailPlanQuantity.getText().toString().trim();
        if(strQuantity.isEmpty()){
            strQuantity = "0";
        }
        if(strPlanQuantity.isEmpty()){
            strPlanQuantity = "0";
        }
        int iQuantity = Integer.parseInt(strQuantity);
        int iPlanQuantity = Integer.parseInt(strPlanQuantity);
        if(iQuantity==iPlanQuantity && iQuantity>0 && iPlanQuantity>0){
//            confirmInventoryBillRequest();
        }else{
            MyToast.myShow(SubMasterListDetailActivity.this,"申请量和扫描量不一致，无法提交",0,0);
        }
    }

    //过账入库单并产生倒扣
    private void confirmInventoryBillRequest(){
        loadingDialog = new LoadingDialog(this,"单据过账中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "InventoryBillRequestConfirm";
                String strProg = "post340";
                String strDocno = txtSubListDetailDocno.getText().toString();

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"inaj_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"inajsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"inajent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"inaj001\" value=\""+strDocno+"\"/&gt;\n"+
                        "&lt;Field name=\"inaj015\" value=\""+strProg+"\"/&gt;\n"+
                        "&lt;Field name=\"inajuser\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"inaj002\" value=\"1.0\"/&gt;\n"+
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

                        if(!statusCode.equals("0")) {
                            MyToast.myShow(SubMasterListDetailActivity.this, statusDescription, 0, 1);
                        }else{
                            MyToast.myShow(SubMasterListDetailActivity.this, statusDescription, 1, 0);
                        }
                    }
                }else{
                    MyToast.myShow(SubMasterListDetailActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubMasterListDetailActivity.this,"网络错误",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    finish();
                }
                loadingDialog.dismiss();
            }
        });
    }
}