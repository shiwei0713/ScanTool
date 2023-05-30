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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.hz.scantool.adapter.SubAdapter;
import com.hz.scantool.dialog.DeptConfigDialog;
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
    private String strTitle,strToolTitle;
    private String strDept;
    private String strArrayDept;
    private String statusCode;
    private String statusDescription;
    private boolean isClearMode;

    private TextView checkStockDetailDept;
    private Button checkBtnDept,checkBtnSaveArea,checkBtnClear;
    private ActionBar actionBar;

    private TextView inputDetailProductCode;
    private TextView inputDetailProductModels,inputDetailProductModelsTitle;
    private TextView inputDetailModel,inputDetailModelTitle;
    private TextView inputDetailStartPlanDate;
    private EditText inputDetailQuantity;
    private TextView inputDetailScanQuantity;
    private TextView inputDetailDocno;
    private TextView inputDetailFeatures;
    private TextView inputDetailFeaturesName;
    private TextView inputDetailFeaturesModels;
    private TextView inputDetailProcess;
    private TextView checkStockDetailAreaId,checkStockDetailArea;
    private Button btnDetailSubmit;
    private LinearLayout viewFeatures,viewFeaturesName,viewFeaturesModels;

    private LoadingDialog loadingDialog;

    private List<Map<String,Object>> mapResponseList,mapResponseAreaList;
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
        actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strToolTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //初始化参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        strToolTitle = strTitle+"-"+getResources().getString(R.string.check_product_tool_title1);
        intIndex = bundle.getInt("index");
        isClearMode = false;
    }

    private void initView(){
        checkStockDetailDept = findViewById(R.id.checkStockDetailDept);
        checkBtnDept = findViewById(R.id.checkBtnDept);
        checkBtnSaveArea = findViewById(R.id.checkBtnSaveArea);
        checkBtnClear = findViewById(R.id.checkBtnClear);

        inputDetailProductCode = findViewById(R.id.inputDetailProductCode);
        inputDetailProductModels = findViewById(R.id.inputDetailProductModels);
        inputDetailModel = findViewById(R.id.inputDetailModel);
        inputDetailStartPlanDate = findViewById(R.id.inputDetailStartPlanDate);
        inputDetailQuantity = findViewById(R.id.inputDetailQuantity);
        inputDetailScanQuantity = findViewById(R.id.inputDetailScanQuantity);
        inputDetailDocno = findViewById(R.id.inputDetailDocno);
        inputDetailFeatures = findViewById(R.id.inputDetailFeatures);
        inputDetailFeaturesName = findViewById(R.id.inputDetailFeaturesName);
        inputDetailFeaturesModels = findViewById(R.id.inputDetailFeaturesModels);
        btnDetailSubmit = findViewById(R.id.btnDetailSubmit);
        inputDetailProductModelsTitle = findViewById(R.id.inputDetailProductModelsTitle);
        inputDetailModelTitle = findViewById(R.id.inputDetailModelTitle);
        inputDetailProcess = findViewById(R.id.inputDetailProcess);
        checkStockDetailAreaId = findViewById(R.id.checkStockDetailAreaId);
        checkStockDetailArea = findViewById(R.id.checkStockDetailArea);
        viewFeatures = findViewById(R.id.viewFeatures);
        viewFeaturesName = findViewById(R.id.viewFeaturesName);
        viewFeaturesModels = findViewById(R.id.viewFeaturesModels);

        //初始化
        checkStockDetailDept.setText(UserInfo.getDept(CheckStockDetailActivity.this));
        if(!isClearMode){
            checkBtnClear.setVisibility(View.GONE);
        }

        //事件定义
        checkBtnDept.setOnClickListener(new submitDataClickListener());
        checkBtnSaveArea.setOnClickListener(new submitDataClickListener());
        btnDetailSubmit.setOnClickListener(new submitDataClickListener());
        checkStockDetailAreaId.setOnClickListener(new submitDataClickListener());
        checkStockDetailArea.setOnClickListener(new submitDataClickListener());
        checkBtnClear.setOnClickListener(new submitDataClickListener());
    }

    /**
    *描述: 初始化数据
    *日期：2022/12/30
    **/
    private void initData(){
        inputDetailProductCode.setText("");
        inputDetailProductModels.setText("");
        inputDetailStartPlanDate.setText("");
        inputDetailScanQuantity.setText("");
        inputDetailQuantity.setText("");
        inputDetailDocno.setText("");
        inputDetailProcess.setText("");
        inputDetailModel.setText("");
        inputDetailFeatures.setText("");
        inputDetailFeaturesName.setText("");
        inputDetailFeaturesModels.setText("");
    }

    private class submitDataClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnDetailSubmit:
                    if(inputDetailDocno.getText().toString().isEmpty()){
                        MyToast.myShow(CheckStockDetailActivity.this,"扫描成功才可提交",0,0);
                    }else{
                        String sQuantity = inputDetailQuantity.getText().toString();
                        if(sQuantity.isEmpty()||sQuantity.equals("")){
                            MyToast.myShow(CheckStockDetailActivity.this,"实盘数不可为0",0,0);
                        }else{
                            getScanQrData(inputDetailDocno.getText().toString(),"U","K");
                        }
                        inputDetailQuantity.clearFocus();
                    }
                    break;
                case R.id.checkBtnDept:
                    DeptConfigDialog deptConfigDialog = new DeptConfigDialog(CheckStockDetailActivity.this,handler);
                    deptConfigDialog.show();
                    break;
                case R.id.checkBtnSaveArea:
                    checkStockDetailAreaId.setText("");
                    checkStockDetailArea.setText("");
                    MyToast.myShow(CheckStockDetailActivity.this,"保存成功",1,0);
                    break;
                case R.id.checkStockDetailArea:
                    checkStockDetailAreaId.setText("");
                    checkStockDetailArea.setText("");
                    break;
                case R.id.checkBtnClear:
                    delScanQrData();
                    break;
            }
        }
    }

    //刷新显示部门
    private Handler handler =new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if(msg.what==1){
                String strDept = msg.getData().getString("dept");
                checkStockDetailDept.setText(strDept);
            }
        }
    };

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
                IntentIntegrator intentIntegrator = new IntentIntegrator(CheckStockDetailActivity.this);
//                intentIntegrator.setTimeout(5000);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //开始扫描
                intentIntegrator.initiateScan();
                break;
            case R.id.action_clear:
                if(!isClearMode){
                    strToolTitle = strTitle+"-"+getResources().getString(R.string.check_product_tool_title2);
                    actionBar.setTitle(strToolTitle);
                    isClearMode = true;
                    checkBtnClear.setVisibility(View.VISIBLE);
                }else{
                    strToolTitle = strTitle+"-"+getResources().getString(R.string.check_product_tool_title1);
                    actionBar.setTitle(strToolTitle);
                    isClearMode = false;
                    checkBtnClear.setVisibility(View.GONE);
                }
                initData();
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
        //解析部门
        strArrayDept = checkStockDetailDept.getText().toString();
        if(strArrayDept.isEmpty()){
            MyToast.myShow(context,"盘点部门不可为空",0,1);
            return;
        }else{
            String[] arrayDept = strArrayDept.split("_");
            strDept = arrayDept[0];
        }

        //解析二维码
        String sQrcode = qrContent;
        int qrIndex = qrContent.indexOf("_");
        if(qrIndex>-1){
            String[] qrCodeValue = qrContent.split("_");
            sQrcode = qrCodeValue[0];
        }

        //解析扫码数据
        if(!isClearMode){
            String sAreaId = checkStockDetailAreaId.getText().toString();
            if(sAreaId.equals("")||sAreaId.isEmpty()){
                getAreaData(qrContent);
            }else{
                getScanQrData(sQrcode,"A","K");
            }
        }else{
            getScanQrData(sQrcode,"B","Y");
        }
    }

    //获取清单
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
                MyToast.myShow(CheckStockDetailActivity.this,e.getMessage(),0,0);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseAreaList.size()>0){
                        for(Map<String,Object> mData: mapResponseAreaList){
                            String sAreaId = mData.get("AreaId").toString();
                            String sArea = mData.get("Area").toString();

                            checkStockDetailAreaId.setText(sAreaId);
                            checkStockDetailArea.setText(sArea);
                        }
                    }
                }else{
                    MyToast.myShow(CheckStockDetailActivity.this,statusDescription,0,0);
                }
            }
        });
    }

    //获取扫描条码信息
    private void getScanQrData(String qrCode,String scmd,String qrStatus){
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "GetQrCode";
//                String qrStatus = "K";  //扫描状态K盘点
                String strStock = strDept;  //盘点库位
                String sQuantity = inputDetailQuantity.getText().toString();
                float fQuantity = 0;
                if (!sQuantity.isEmpty()){
                    fQuantity = Float.valueOf(sQuantity);
                }

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
                        "&lt;Field name=\"bcaaud001\" value=\""+strStock+"\"/&gt;\n"+
                        "&lt;Field name=\"scmd\" value=\""+scmd+"\"/&gt;\n"+
                        "&lt;Field name=\"qty\" value=\""+fQuantity+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaua021\" value=\""+checkStockDetailAreaId.getText().toString()+"\"/&gt;\n"+
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
                if(scmd.equals("A")){
                    mapResponseList = t100ServiceHelper.getT100JsonQrCodeData(strResponse,"qrcode");
                }else{
                    mapResponseList = t100ServiceHelper.getT100JsonQrCodeClearData(strResponse,"qrcode");
                }
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
                    }
                }else{
                    MyToast.myShow(CheckStockDetailActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckStockDetailActivity.this,e.getLocalizedMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(!statusCode.equals("0")){
                    MyToast.myShow(CheckStockDetailActivity.this,statusDescription,0,0);
                }else{
                    if(scmd.equals("U")){
                        MyToast.myShow(CheckStockDetailActivity.this,statusDescription,1,0);
                    }else{
                        if(mapResponseList.size()>0){
                            for(Map<String,Object> mData: mapResponseList){
                                String sProductCode = "";
                                String sProductName = "";
                                String sProductModels = "";
                                String sProductSize = "";
                                String sDocno = "";
                                String sPlanDate = "";
                                String sQuantity = "";
                                String sWeight = "";
                                String sLots = "";
                                String sFeatures = "";
                                String sFeaturesName = "";
                                String sFeaturesModels = "";
                                String sProcess = "";

                                if(scmd.equals("B")){
                                    sProductCode = mData.get("ProductCode").toString();
                                    sProductName = mData.get("ProductName").toString();
                                    sProductModels = mData.get("ProductModels").toString();
                                    sProductSize = mData.get("ProductSize").toString();
                                    sDocno = mData.get("Qrcode").toString();
                                    sPlanDate = mData.get("PlanDate").toString();
                                    sQuantity = mData.get("Quantity").toString();
                                    sWeight = mData.get("Weight").toString();
                                    sLots = mData.get("Lots").toString();
                                    sProcess = mData.get("Process").toString();
                                }else{
                                    sProductCode = mData.get("ProductCode").toString();
                                    sProductName = mData.get("ProductName").toString();
                                    sProductModels = mData.get("ProductModels").toString();
                                    sProductSize = mData.get("ProductSize").toString();
                                    sDocno = mData.get("Docno").toString();
                                    sPlanDate = mData.get("PlanDate").toString();
                                    sQuantity = mData.get("Quantity").toString();
                                    sWeight = mData.get("Weight").toString();
                                    sLots = mData.get("Lots").toString();
                                    sFeatures = mData.get("Features").toString();
                                    sFeaturesName = mData.get("FeaturesName").toString();
                                    sFeaturesModels = mData.get("FeaturesModels").toString();
                                    sProcess = mData.get("Process").toString();
                                }

                                //显示扫描结果
                                inputDetailProductCode.setText(sProductCode);
                                inputDetailProductModels.setText(sProductName);
                                inputDetailStartPlanDate.setText(sPlanDate);
                                inputDetailScanQuantity.setText(sQuantity);
                                inputDetailQuantity.setText("");
                                inputDetailDocno.setText(sDocno);
                                inputDetailProcess.setText(sProcess);
                                if(sProductSize.equals("")||sProductSize.isEmpty()){
                                    //如果为零件
                                    inputDetailModel.setText(sProductModels);
                                    viewFeatures.setVisibility(View.GONE);
                                    viewFeaturesName.setVisibility(View.GONE);
                                    viewFeaturesModels.setVisibility(View.GONE);
                                }else{
                                    //如果原材料
                                    viewFeatures.setVisibility(View.VISIBLE);
                                    viewFeaturesName.setVisibility(View.VISIBLE);
                                    viewFeaturesModels.setVisibility(View.VISIBLE);
                                    inputDetailModel.setText(sProductSize+"/"+sProductModels);
                                    inputDetailFeatures.setText(sFeatures);
                                    inputDetailFeaturesName.setText(sFeaturesName);
                                    inputDetailFeaturesModels.setText(sFeaturesModels);
                                    inputDetailProductModelsTitle.setText(getResources().getString(R.string.check_product_label3));
                                    inputDetailModelTitle.setText(getResources().getString(R.string.check_product_label4));
                                }

                            }
                        }
                    }
                }

                loadingDialog.dismiss();
            }
        });
    }

    /**
    *描述: 删除盘点数据
    *日期：2022/12/30
    **/
    private void delScanQrData(){
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据删除中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "StorageCheckRequestDelete";
                String strAct = "delete";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcah_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaa001\" value=\""+inputDetailDocno.getText().toString().trim()+"\"/&gt;\n"+
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
                    MyToast.myShow(CheckStockDetailActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckStockDetailActivity.this,e.getLocalizedMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(!statusCode.equals("0")){
                    MyAlertDialog.myShowAlertDialog(CheckStockDetailActivity.this,"错误信息",statusDescription);
                }else{
                    MyToast.myShow(CheckStockDetailActivity.this,statusDescription,1,0);
                    initData();
                }

                loadingDialog.dismiss();
            }
        });
    }
}