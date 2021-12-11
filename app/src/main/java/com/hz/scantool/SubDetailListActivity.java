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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.DetailListItemAdapter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubListDetailAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.HashMap;
import java.util.Iterator;
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

public class SubDetailListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private ListView listDetailView;
    private TextView txtSubDetailDocnoTitle;
    private TextView txtSubDetailDocno;
    private TextView txtSubDetailStockTitle;
    private TextView txtSubDetailStockId;
    private TextView txtSubDetailStock;
    private TextView txtSubDetailDeptTitle;
    private TextView txtSubDetailDeptId;
    private TextView txtSubDetailDept;
    private TextView txtSubDetailPlanDate;
    private TextView txtSubDetailPositionTitle;
    private TextView txtSubDetailPosition;
    private TextView txtSubDetailPlanCount;
    private TextView txtSubDetailScanCount;
    private Button btnSubmit;
    private Button btnCancel;
    private ImageView imageViewSubDetailLogo;
    private ProgressBar progressSubDetailBar;

    private String strSlip;
    private String strStockType;
    private String strProg;
    private String strLocationId;
    private String strTitle="";
    private String strWhere;
    private String strDocType;
    private String strIndex;
    private String statusCode;
    private String statusDescription;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private List<Map<String,Object>> mapResponseScanList;
    private SubListDetailAdapter subListDetailAdapter;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_list);

        //初始化
        initView();
        setTitle();
        initQueryCondition();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subDetailListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //显示储位清单
        getSubDetailListData();
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailListActivity.this);
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

    //PDA扫描广播注册
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

    //手机扫描
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

    //初始化控件
    private void initView(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        txtSubDetailDocnoTitle = findViewById(R.id.txtSubDetailDocnoTitle);
        txtSubDetailDocno = findViewById(R.id.txtSubDetailDocno);
        txtSubDetailStockTitle = findViewById(R.id.txtSubDetailStockTitle);
        txtSubDetailStockId = findViewById(R.id.txtSubDetailStockId);
        txtSubDetailStock = findViewById(R.id.txtSubDetailStock);
        txtSubDetailDeptTitle = findViewById(R.id.txtSubDetailDeptTitle);
        txtSubDetailDeptId = findViewById(R.id.txtSubDetailDeptId);
        txtSubDetailDept = findViewById(R.id.txtSubDetailDept);
        txtSubDetailPlanDate = findViewById(R.id.txtSubDetailPlanDate);
        txtSubDetailPositionTitle = findViewById(R.id.txtSubDetailPositionTitle);
        txtSubDetailPosition = findViewById(R.id.txtSubDetailPosition);
        txtSubDetailPlanCount = findViewById(R.id.txtSubDetailPlanCount);
        txtSubDetailScanCount = findViewById(R.id.txtSubDetailScanCount);
        listDetailView = findViewById(R.id.subDetailListView);
        progressSubDetailBar = findViewById(R.id.progressSubDetailBar);
        imageViewSubDetailLogo = findViewById(R.id.imageViewSubDetailLogo);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        txtSubDetailStockId.setText(bundle.getString("StockId"));
        txtSubDetailStock.setText(bundle.getString("Stock"));
        txtSubDetailDeptId.setText(bundle.getString("DeptId"));
        txtSubDetailDept.setText(bundle.getString("Dept"));
        txtSubDetailPlanDate.setText(bundle.getString("PlanDate"));
        txtSubDetailDocno.setText(bundle.getString("Docno"));
        strDocType = bundle.getString("DocType");
        strIndex = bundle.getString("Type");

        if(strDocType.equals("1")){
            imageViewSubDetailLogo.setImageDrawable(getResources().getDrawable(R.drawable.sub_detail_inside));
        }else{
            imageViewSubDetailLogo.setImageDrawable(getResources().getDrawable(R.drawable.sub_detail_outside));
        }

        //绑定事件
        btnSubmit.setOnClickListener(new btnActionListener());
        btnCancel.setOnClickListener(new btnActionListener());
    }

    private void setTitle(){
//        txtSubDetailDocnoTitle.setVisibility(View.GONE);
//        txtSubDetailDocno.setVisibility(View.GONE);
        txtSubDetailPositionTitle.setVisibility(View.GONE);
        txtSubDetailPosition.setVisibility(View.GONE);
//        btnSubmit.setVisibility(View.GONE);
//        btnCancel.setVisibility(View.GONE);
        strTitle = getResources().getString(R.string.master_action5);

        if(strIndex.equals("2")){
            //完工入库
            strTitle = getResources().getString(R.string.master_action3);
            txtSubDetailStockTitle.setText(getString(R.string.sub_detail_list_stock2));
            txtSubDetailDeptTitle.setText(getString(R.string.sub_detail_list_dept2));
            txtSubDetailDocnoTitle.setVisibility(View.VISIBLE);
            txtSubDetailDocno.setVisibility(View.VISIBLE);
        }else if(strIndex.equals("3")){
            //采购入库
            strTitle = getResources().getString(R.string.master_action4);
            txtSubDetailStockTitle.setText(getString(R.string.sub_detail_list_stock3));
            txtSubDetailDeptTitle.setText(getString(R.string.sub_detail_list_dept3));
            txtSubDetailDocnoTitle.setVisibility(View.VISIBLE);
            txtSubDetailDocno.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        }else if(strIndex.equals("41")){
            //生产退货
            strTitle = getResources().getString(R.string.master_action51);
            txtSubDetailStockTitle.setText(getString(R.string.sub_detail_list_stock41));
            txtSubDetailDeptTitle.setText(getString(R.string.sub_detail_list_dept41));
        }
    }

    //初始化查询条件
    private void initQueryCondition(){
        if(strIndex.equals("2")){
            //完工入库

        }else if(strIndex.equals("3")){
            //采购入库
            strWhere = " pmdudocno = '"+txtSubDetailDocno.getText().toString()+"'";
        }else if(strIndex.equals("4")){
            //生产备货
            strWhere = " sfba019 = '"+txtSubDetailDeptId.getText().toString()+"' AND sfaa019 = to_date('"+txtSubDetailPlanDate.getText().toString()+"','YYYY-MM-DD')";
        }else if(strIndex.equals("41")){
            //生产退货
            strWhere = " indd032 = '"+txtSubDetailStockId.getText().toString()+"' AND indcdocdt = to_date('"+txtSubDetailPlanDate.getText().toString()+"','YYYY-MM-DD')";
        }
    }

    //初始化单别参数
    private void initDocno(){
        if(strIndex.equals("2")){
            //完工入库

        }else if(strIndex.equals("3")){
            //采购入库
            //初始化单据参数
            if(strDocType.equals("1")){
                //采购入库
                strSlip = "PM28";
                strStockType = "6";
                strProg = "apmt570";
                strLocationId = "";
            }else{
                //委外入库
                strSlip = "PM35";
                strStockType = "12";
                strProg = "apmt571";
                strLocationId = "";
            }
        }else if(strIndex.equals("4")){
            //生产备货
            //初始化单据参数
            if(strDocType.equals("1")){
                //厂内发料
                strSlip = "IN17";
                strStockType = "1";
                strProg = "cint335";    //cint335
                strLocationId = "";
            }else{
                //委外发料
                strSlip = "IN19";
                strStockType = "1";
                strProg = "cint336";           //cint336
                strLocationId = "";
            }
        }else if(strIndex.equals("41")){
            //生产退货

        }
    }

    public class btnActionListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnSubmit:
//                    confirmInventoryBillRequest(txtSubDetailDocno.getText().toString());
                    finish();
                    break;
                case R.id.btnCancel:
                    finish();
                    break;
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
            initDocno();
            getScanQrData(qrCodeValue[0]);
        }
    }

    //获取发料储位明细
    private void getSubDetailListData(){
        //显示进度条
        progressSubDetailBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppGetStockLot";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strIndex +"\"/&gt;\n"+
                        "&lt;Field name=\"stock\" value=\""+txtSubDetailStockId.getText().toString()+"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseList = t100ServiceHelper.getT100JsonStockData(strResponse,"productlist");
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
                            MyToast.myShow(SubDetailListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = progressSubDetailBar.getProgress();
                            progress = progress + 50;
                            progressSubDetailBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailListActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailListActivity.this,"网络错误",0,0);
                //隐藏滚动条
                progressSubDetailBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                refreshList();

                subListDetailAdapter = new SubListDetailAdapter(mapResponseList,getApplicationContext());
                listDetailView.setAdapter(subListDetailAdapter);

                //隐藏滚动条
                progressSubDetailBar.setVisibility(View.GONE);
            }
        });
    }

    //获取扫描条码信息
    private void getScanQrData(String qrCode){
        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "GetQrCode";
                String qrStatus = "A";   //扫描状态记录，A代表出库扫描过，无其他管控

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
                mapResponseScanList = t100ServiceHelper.getT100JsonQrCodeData(strResponse,"qrcode");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

                e.onNext(mapResponseStatus);
                e.onNext(mapResponseScanList);
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
                            MyToast.myShow(SubDetailListActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailListActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailListActivity.this,"网络错误",0,0);
            }

            @Override
            public void onComplete() {
                if(mapResponseScanList.size()>0){
                    boolean isSuccess = false;
                    int index = 0;
                    for(Map<String,Object> mScanData: mapResponseScanList){
                        String strStatus = "";
                        String sProductCode = mScanData.get("ProductCode").toString();
                        String sStockId = mScanData.get("StockId").toString();
                        String sStockLocationId = mScanData.get("StockLocationId").toString();
                        String sQuantity = mScanData.get("Quantity").toString();
                        String sWeight = mScanData.get("Weight").toString();
                        boolean isFailure = false;

                        for(Map<String,Object> mData: mapResponseList){
                            String mProductCode = mData.get("ProductCode").toString();
                            String mStockLocationId = mData.get("StockLocationId").toString();
                            String mWeight  =  mData.get("Weight").toString();
                            if(sProductCode.equals(mProductCode) && sStockLocationId.equals(mStockLocationId) && sWeight.equals(mWeight)){
                                isSuccess = true;
//                                mData.put("ScanQuantity",sQuantity);
//                                mData.put("ScanQuantityPcs","1");
//                                mData.put("Weight",sWeight);
//                                strStatus = subListDetailAdapter.updateData(index,listDetailView);
//                                if(strStatus.equals("S")){
//                                    isFailure = false;
                                    genT100Doc(mData,qrCode,index);
//                                }else{
//                                    isFailure = true;
//                                }
                            }
//                            index += 1;
                        }

//                        if(isFailure && !strStatus.equals("Z")){
//                            MyToast.myShow(SubDetailListActivity.this,"扫描数量不可大于备货数量",0,0);
//                        }
                    }

                    if(!isSuccess){
                        MyToast.myShow(SubDetailListActivity.this,"标签错误，本次备货清单中无此零件",0,0);
                    }
                }
            }
        });
    }

    //生成T100单据
    private void genT100Doc(Map<String,Object> mInputData,String qrCode,int index){
        //显示进度条
        loadingDialog = new LoadingDialog(SubDetailListActivity.this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "InventoryBillRequestGen";

                //初始化字段
                String strProductCode = "";
                String strProductSize = "";
                String strStockId= "";
                String strStockLocationId = "";
                String strQuantity = "0";
                String strWeight = "0";
                if(!mInputData.isEmpty()){
                    strProductCode = mInputData.get("ProductCode").toString();
                    strProductSize = mInputData.get("ProductSize").toString();
                    strStockId = mInputData.get("StockId").toString();
                    strStockLocationId = mInputData.get("StockLocationId").toString();

                    if(!strProductSize.isEmpty()){
                        strQuantity = mInputData.get("Weight").toString();
                        strWeight = mInputData.get("Quantity").toString();
                    }else{
                        strQuantity = mInputData.get("Quantity").toString();
                    }

                }

                //区分原材料单据，如果是原材料则程式为cint331，厂内单别为:IN14；厂外单别为：IN37
                if(!strProductSize.isEmpty()){
                    if(strIndex.equals("4")) {
                        //生产备货
                        //初始化单据参数
                        if (strDocType.equals("1")) {
                            //原材料厂内发料
                            strSlip = "IN14";
                            strProg = "cint331";
                        } else {
                            //原材料委外发料
                            strSlip = "IN37";
                            strProg = "cint331";
                        }
                    }
                }

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"inaj_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"inajsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"inajent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"inaj001\" value=\""+strSlip+"\"/&gt;\n"+
                        "&lt;Field name=\"inaj004\" value=\""+strStockType+"\"/&gt;\n"+
                        "&lt;Field name=\"inaj015\" value=\""+strProg+"\"/&gt;\n"+
                        "&lt;Field name=\"inaj044\" value=\""+txtSubDetailDocno.getText()+"\"/&gt;\n"+
                        "&lt;Field name=\"inajuser\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+ qrCode +"\"/&gt;\n"+  //二维码
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"inaj002\" value=\"1.0\"/&gt;\n"+
                        "&lt;Field name=\"inaj005\" value=\""+strProductCode+"\"/&gt;\n"+    //料件编码
                        "&lt;Field name=\"inaj006\" value=\""+strProductSize+"\"/&gt;\n"+    //产品特征
                        "&lt;Field name=\"inaj008\" value=\""+strStockId+"\"/&gt;\n"+    //拨出库位编号
                        "&lt;Field name=\"inaj009\" value=\""+strStockLocationId+"\"/&gt;\n"+    //拨出储位编号
                        "&lt;Field name=\"inaj0081\" value=\""+txtSubDetailDeptId.getText()+"\"/&gt;\n"+   //拨入库位编号
                        "&lt;Field name=\"inaj0091\" value=\""+strLocationId+"\"/&gt;\n"+   //拨入储位编号
                        "&lt;Field name=\"inaj011\" value=\""+strQuantity+"\"/&gt;\n"+       //交易数量
                        "&lt;Field name=\"inaj027\" value=\""+strWeight+"\"/&gt;\n"+       //交易重量
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
                            MyToast.myShow(SubDetailListActivity.this, statusDescription, 0, 0);
                        }else{
                            MyToast.myShow(SubDetailListActivity.this, statusDescription, 1, 0);
                            subListDetailAdapter.updateDocno(index,listDetailView,statusDescription);
                            txtSubDetailDocno.setText(statusDescription);
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailListActivity.this,"执行接口错误",2,0);
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailListActivity.this,"网络错误",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                getSubDetailListData();
                loadingDialog.dismiss();
            }
        });
    }

    //隐藏已扫描项
    public void refreshList(){
        int intCurrent = 0;
        int intScan = 0;
        int intCurrentTotal=0;
        int intScanTotal=0;

        Iterator<Map<String,Object>> listItem = mapResponseList.iterator();
        while (listItem.hasNext()){
            Map<String,Object> map = listItem.next();
            if(map.get("Status").equals("Y")){
                intScan ++;
                listItem.remove();
            }

            intCurrent ++;
        }

        txtSubDetailScanCount.setText(String.valueOf(intCurrent - intScan));
        txtSubDetailPlanCount.setText(String.valueOf(intCurrent));

//        //初始化ListView
//        subListDetailAdapter = new SubListDetailAdapter(mapResponseList,getApplicationContext());
//        listDetailView.setAdapter(subListDetailAdapter);
    }

    //过账调拨单
    private void confirmInventoryBillRequest(String strDocno){
        loadingDialog = new LoadingDialog(this,"单据过账中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "InventoryBillRequestConfirm";
                String strProg = "post330";

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
                            MyToast.myShow(SubDetailListActivity.this, statusDescription, 0, 1);
                        }else{
                            MyToast.myShow(SubDetailListActivity.this, statusDescription, 1, 0);
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailListActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailListActivity.this,"网络错误",0,0);
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