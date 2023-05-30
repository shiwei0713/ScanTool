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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
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

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class ProductAreaActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle,strToolTitle;
    private String statusCode;
    private String statusDescription;
    private boolean isClearMode;
    private int iTotal,iTotalPcs;
    private TextView txtViewAreaId,txtViewArea;
    private TextView areaDetailProductCode,areaDetailProductModels,areaDetailModel,areaDetailQuantityTotal,areaDetailPcsTotal,areaDetailProcessId,areaDetailProcess;
    private TextView areaDetailDevice,areaDetailEmployee,areaDetailQuantity,areaDetailQrcode;
    private EditText inputAreaQrcode;
    private LinearLayout layoutArea;
    private ActionBar actionBar;
    private Button btnSave,btnClose,areaBtnClear,btnConfirm;

    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapResponseAreaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_area);

        //初始化控件
        initBundle();
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu_clear,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(ProductAreaActivity.this);
//                intentIntegrator.setTimeout(5000);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //开始扫描
                intentIntegrator.initiateScan();
                break;
            case R.id.action_clear:
                if(!isClearMode){
                    strToolTitle = strTitle+"-"+getResources().getString(R.string.produc_area_tool_title2);
                    actionBar.setTitle(strToolTitle);
                    isClearMode = true;
                    areaBtnClear.setVisibility(View.VISIBLE);
                    layoutArea.setVisibility(View.GONE);
                }else{
                    strToolTitle = strTitle+"-"+getResources().getString(R.string.produc_area_tool_title1);
                    actionBar.setTitle(strToolTitle);
                    isClearMode = false;
                    areaBtnClear.setVisibility(View.GONE);
                    layoutArea.setVisibility(View.VISIBLE);
                }
                initValue();
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

    //取消PDA广播注册
    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(scanReceiver);
    }

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
        String sArea = txtViewArea.getText().toString();
        if(qrContent.isEmpty()||qrContent.equals("")){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            if(!isClearMode){
                //扫描模式
                if(sArea.isEmpty()|| sArea.equals("")){
                    getAreaData(qrContent);
                }else{
                    genAreaData(qrContent,"insert");
                }
            }else{
                //清除模式
                genAreaData(qrContent,"select");
            }
        }
    }

    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.areaToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle+"-"+getResources().getString(R.string.produc_area_tool_title1));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        txtViewAreaId = findViewById(R.id.txtViewAreaId);
        txtViewArea = findViewById(R.id.txtViewArea);
        inputAreaQrcode = findViewById(R.id.inputAreaQrcode);
        areaDetailProductCode = findViewById(R.id.areaDetailProductCode);
        areaDetailProductModels = findViewById(R.id.areaDetailProductModels);
        areaDetailModel = findViewById(R.id.areaDetailModel);
        areaDetailQuantityTotal = findViewById(R.id.areaDetailQuantityTotal);
        areaDetailPcsTotal = findViewById(R.id.areaDetailPcsTotal);
        areaDetailProcessId = findViewById(R.id.areaDetailProcessId);
        areaDetailProcess = findViewById(R.id.areaDetailProcess);
        areaDetailDevice = findViewById(R.id.areaDetailDevice);
        areaDetailEmployee = findViewById(R.id.areaDetailEmployee);
        areaDetailQuantity = findViewById(R.id.areaDetailQuantity);
        areaDetailQrcode = findViewById(R.id.areaDetailQrcode);
        btnSave = findViewById(R.id.btnSave);
        btnClose = findViewById(R.id.btnClose);
        btnConfirm = findViewById(R.id.btnConfirm);
        areaBtnClear = findViewById(R.id.areaBtnClear);
        layoutArea = findViewById(R.id.layoutArea);

        //定义事件
        btnSave.setOnClickListener(new btnClickListener());
        btnClose.setOnClickListener(new btnClickListener());
        btnConfirm.setOnClickListener(new btnClickListener());
        areaBtnClear.setOnClickListener(new btnClickListener());

        //初始化参数
        isClearMode=false;
        areaBtnClear.setVisibility(View.GONE);
        layoutArea.setVisibility(View.VISIBLE);
    }

    //初始化参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        iTotal = 0;
        iTotalPcs = 0;
    }

    /**
    *描述: 恢复化默认值
    *日期：2023/2/27
    **/
    private void initValue(){
        areaDetailProductCode.setText("");
        areaDetailProductModels.setText("");
        areaDetailModel.setText("");
        areaDetailProcessId.setText("");
        areaDetailProcess.setText("");
        areaDetailDevice.setText("");
        areaDetailEmployee.setText("");
        areaDetailQuantity.setText("");
        areaDetailQrcode.setText("");
        areaDetailQuantityTotal.setText("");
        areaDetailPcsTotal.setText("");
    }

    /**
    *描述: 点击事件
    *日期：2022/12/30
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSave:         //保存
                    txtViewAreaId.setText("");
                    txtViewArea.setText("");
                    areaDetailQuantityTotal.setText("0");
                    areaDetailPcsTotal.setText("0");
                    MyToast.myShow(ProductAreaActivity.this,"保存区域成功",1,0);
                    break;
                case R.id.btnClose:        //关闭
                    finish();
                    break;
                case R.id.areaBtnClear:    //清除
                    String sDelQrcode = areaDetailQrcode.getText().toString().trim();
                    if(!sDelQrcode.equals("")&&!sDelQrcode.isEmpty()){
                        genAreaData(sDelQrcode,"delete");
                    }else{
                        MyAlertDialog.myShowAlertDialog(ProductAreaActivity.this,"错误信息","条码编号不可为空");
                    }
                    break;
                case R.id.btnConfirm:
                    String sQrcode = inputAreaQrcode.getText().toString().trim().toUpperCase();
                    if(!sQrcode.equals("")&&!sQrcode.equals("")){
                        genAreaData(sQrcode,"insert");
                    }else{
                        MyAlertDialog.myShowAlertDialog(ProductAreaActivity.this,"错误信息","条码编号不可为空");
                    }
                    break;
            }
        }
    }

    /**
    *描述: 获取区域信息
    *日期：2022/12/30
    **/
    private void getAreaData(String qrcontent){
        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "StockGet";
                String strwhere = " oocql002='"+qrcontent.substring(0,3)+"'";
                String strType = "8";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseAreaList = t100ServiceHelper.getT100JsonAreaData(strResponse,"areainfo");

                e.onNext(mapResponseStatus);
                e.onNext(mapResponseAreaList);
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
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(ProductAreaActivity.this,e.getMessage(),0,0);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseAreaList.size()>0){
                        for(Map<String,Object> mData: mapResponseAreaList){
                            String sAreaId = mData.get("AreaId").toString();
                            String sArea = mData.get("Area").toString();

                            txtViewAreaId.setText(sAreaId);
                            txtViewArea.setText(sArea);
                        }
                    }
                }else{
                    MyAlertDialog.myShowAlertDialog(ProductAreaActivity.this,"错误信息",statusDescription);
                }
            }
        });
    }

    /**
     *描述: 生成区域数据
     *日期：2022/12/30
     **/
    private void genAreaData(String qrcode,String strAct){
        //显示进度条
        if(loadingDialog == null) {
            loadingDialog = new LoadingDialog(this, "数据提交中", R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductAreaRequestGen";
                String sType = "1";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaa001\" value=\""+qrcode.trim()+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaua021\" value=\""+txtViewAreaId.getText().toString().trim()+"\"/&gt;\n"+
                        "&lt;Field name=\"typecode\" value=\""+sType+"\"/&gt;\n"+
                        "&lt;Field name=\"act\" value=\""+strAct+"\"/&gt;\n"+
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
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonAreaListData(strResponse,"docno");

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
                    MyToast.myShow(ProductAreaActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(ProductAreaActivity.this,e.getLocalizedMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(!statusCode.equals("0")){
                    MyAlertDialog.myShowAlertDialog(ProductAreaActivity.this,"错误信息",statusDescription);
                }else{
                    if(mapResponseList.size()>0){
                        for(Map<String,Object> mData: mapResponseList){
                            String sProductCode = mData.get("ProductCode").toString();
                            String sProductName = mData.get("ProductName").toString();
                            String sProductModels = mData.get("ProductModels").toString();
                            String sProcessId = mData.get("ProcessId").toString();
                            String sProcess = mData.get("Process").toString();
                            String sDevice = mData.get("Device").toString();
                            String sEmployee = mData.get("Employee").toString();
                            String sQuantity = mData.get("Quantity").toString();
                            String sQrcode = mData.get("Qrcode").toString();
                            int iQuantity = 0;
                            int iQuantityPcs = 0 ;

                            //计算总量和总箱数
                            if(!sQuantity.equals("")&&!sQuantity.isEmpty()){
                                iQuantity = Integer.parseInt(sQuantity);
                            }

                            if(iQuantity>0){
                                iQuantityPcs = 1;
                            }
                            iTotal = iTotal + iQuantity;
                            iTotalPcs = iTotalPcs + iQuantityPcs;

                            areaDetailProductCode.setText(sProductCode);
                            areaDetailProductModels.setText(sProductName);
                            areaDetailModel.setText(sProductModels);
                            areaDetailProcessId.setText(sProcessId);
                            areaDetailProcess.setText(sProcess);
                            areaDetailDevice.setText(sDevice);
                            areaDetailEmployee.setText(sEmployee);
                            areaDetailQuantity.setText(sQuantity);
                            areaDetailQrcode.setText(sQrcode);
                            areaDetailQuantityTotal.setText(String.valueOf(iTotal));
                            areaDetailPcsTotal.setText(String.valueOf(iTotalPcs));
                        }
                    }
                    MyToast.myShow(ProductAreaActivity.this,statusDescription,1,0);
                }

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}