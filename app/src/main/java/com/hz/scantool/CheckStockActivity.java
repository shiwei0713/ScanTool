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
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MainListItemAdapter;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.dialog.DeptConfigDialog;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class CheckStockActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private int intIndex;
    private String strTitle;
    private String strStockId;
    private String strStockType;
    private String strArrayDept;
    private String statusCode;
    private String statusDescription;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private MainListItemAdapter mainListItemAdapter;

    private TextView checkStockDept;
    private TextView checkStockDate;

    private TextView checkDetailProductCode;
    private TextView checkDetailProductModels;
    private TextView checkDetailModel;
    private TextView checkDetailStartPlanDate;
    private TextView checkDetailQuantity;
    private TextView checkDetailQuantityPcs;
    private TextView checkDetailStockId;
    private TextView checkDetailStock;
    private TextView checkDetailPositionId;
    private TextView checkDetailPosition;
    private TextView checkDetailLot;
    private TextView checkDetailFeatures;
    private TextView checkDetailDocno;

    private Button checkBtnDept;
    private Button checkBtnQuery;
//    private Button btnCheckGet;
//    private Button btnCheckPost;

    private LoadingDialog loadingDialog;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_stock);

        //初始化参数
        initBundle();

        //初始化控件
        initView();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.checkStockToolBar);
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(CheckStockActivity.this);
//                intentIntegrator.setTimeout(5000);
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

        //初始化仓库
        initStock();
        //盘点日期检查
//        String sDate = checkStockDate.getText().toString();
//        if(sDate.isEmpty()||sDate.equals("")){
//            MyToast.myShow(context,"无盘点日期,请通知财务开始盘点",0,1);
//            return;
//        }

        //解析二维码
        //解析二维码
        String[] qrCodeValue = qrContent.split("_");
        if(qrContent.isEmpty()||qrContent.equals("")){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            if(strStockType.equals("Y")){
                getScanQrData(qrCodeValue[0].toString());
            }else{
                String where = " inab001='"+ strStockId +"' AND (inab002='"+qrContent.trim()+"' OR inabud001='"+qrContent.trim()+"')";
                getStockData(where,"1");
            }
        }
    }

    private void initView(){
        checkStockDept = findViewById(R.id.checkStockDept);
        checkStockDate = findViewById(R.id.checkStockDate);

        checkDetailProductCode = findViewById(R.id.checkDetailProductCode);
        checkDetailProductModels = findViewById(R.id.checkDetailProductModels);
        checkDetailModel = findViewById(R.id.checkDetailModel);
        checkDetailStartPlanDate = findViewById(R.id.checkDetailStartPlanDate);
        checkDetailQuantity = findViewById(R.id.checkDetailQuantity);
        checkDetailQuantityPcs = findViewById(R.id.checkDetailQuantityPcs);
        checkDetailStockId = findViewById(R.id.checkDetailStockId);
        checkDetailStock = findViewById(R.id.checkDetailStock);
        checkDetailPositionId = findViewById(R.id.checkDetailPositionId);
        checkDetailPosition = findViewById(R.id.checkDetailPosition);
        checkDetailLot = findViewById(R.id.checkDetailLot);
        checkDetailFeatures = findViewById(R.id.checkDetailFeatures);
        checkDetailDocno = findViewById(R.id.checkDetailDocno);

        checkBtnDept = findViewById(R.id.checkBtnDept);
        checkBtnQuery  = findViewById(R.id.checkBtnQuery);
//        btnCheckGet= findViewById(R.id.btnCheckGet);
//        btnCheckPost= findViewById(R.id.btnCheckPost);

        //初始化
        checkStockDept.setText(UserInfo.getDept(CheckStockActivity.this));

        checkBtnDept.setOnClickListener(new btnClickListener());
        checkBtnQuery.setOnClickListener(new btnClickListener());
//        btnCheckGet.setOnClickListener(new btnClickListener());
//        btnCheckPost.setOnClickListener(new btnClickListener());
    }


    //初始化参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        intIndex = bundle.getInt("index");
    }

    private void initStock(){
        //解析部门
        strArrayDept = checkStockDept.getText().toString();
        if(strArrayDept.isEmpty()){
            MyToast.myShow(CheckStockActivity.this,"盘点仓库不可为空",0,1);
            return;
        }else{
            String[] arrayDept = strArrayDept.split("_");
            strStockId = arrayDept[0].toString();
            strStockType = arrayDept[2].toString();
        }

        //初始化盘点日期
//        String where = " bcah005='"+ strStockId +"'";
//        getStockData(where,"2");
        //初始化日期
        calendar= Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String year=String.valueOf(calendar.get(Calendar.YEAR));
        String month=String.valueOf(calendar.get(Calendar.MONTH)+1);
        String day=String.valueOf(calendar.get(Calendar.DATE));
        checkStockDate.setText(year+month+day);
    }

    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.checkBtnDept:
                    DeptConfigDialog deptConfigDialog = new DeptConfigDialog(CheckStockActivity.this,handler);
                    deptConfigDialog.showStock();
                    break;
                case R.id.checkBtnQuery:

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
                checkStockDept.setText(strDept);
            }
        }
    };

    //获取扫描储位信息
    private void getStockData(String where,String stocktype){
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据查询中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "StockGet";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ where +"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ stocktype +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseList = t100ServiceHelper.getT100StockData(strResponse,"stockinfo");
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
                            MyToast.myShow(CheckStockActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(CheckStockActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckStockActivity.this,"网络错误",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(mapResponseList.size()>0){
                    String sStockId="";
                    String sStockLocationId="";
                    String sStockLocation="";
                    String sStockType="";
                    String sPlanDate="";

                    for(Map<String,Object> mData: mapResponseList){
                        sStockId = mData.get("StockId").toString();
                        sStockLocationId = mData.get("StockLocationId").toString();
                        sStockLocation = mData.get("StockLocation").toString();
                        sStockType = mData.get("StockType").toString();
                        sPlanDate = mData.get("PlanDate").toString();

                    }

                    if(stocktype.equals("1")){
                        Intent intent = new Intent(CheckStockActivity.this,PositionActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("title", strTitle);
                        bundle.putString("stockid", sStockId);
                        bundle.putString("positionid", sStockLocationId);
                        bundle.putString("position", sStockLocation);
                        bundle.putString("stocktype", sStockType);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        checkStockDate.setText(sPlanDate);
                    }
                }
                loadingDialog.dismiss();
            }
        });
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
                String webServiceName = "StorageCheckRequestInsert";

                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcah_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcahsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcahent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcah018\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;Field name=\"qrsid\" value=\""+qrCode+"\"/&gt;\n"+
                        "&lt;Field name=\"bcah005\" value=\""+strStockId+"\"/&gt;\n"+   //盘点仓库
                        "&lt;Field name=\"stocktype\" value=\""+strStockType+"\"/&gt;\n"+   //仓库类别,区分材料(Y)和非材料(N)
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcahseq\" value=\"1.0\"/&gt;\n"+
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
                mapResponseList = t100ServiceHelper.getT100CheckData(strResponse,"qrcode");

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
                            MyToast.myShow(CheckStockActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(CheckStockActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckStockActivity.this,"网络错误",0,0);
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
                        String sQuantity = mData.get("Quantity").toString();
                        String sStockId = mData.get("StockId").toString();
                        String sStock = mData.get("Stock").toString();
                        String sStockLocationId = mData.get("StockLocationId").toString();
                        String sStockLocation = mData.get("StockLocation").toString();
                        String sWeight = mData.get("Weight").toString();
                        String sLots = mData.get("Lots").toString();
                        String sFeatures = mData.get("Features").toString();
                        String sFeaturesName = mData.get("FeaturesName").toString();
                        String sFeaturesModels = mData.get("FeaturesModels").toString();
//                        String sPlanDate = mData.get("PlanDate").toString();

                        checkDetailProductCode.setText(sProductCode);
                        checkDetailProductModels.setText(sProductName);
                        checkDetailModel.setText(sProductModels+"_"+sProductSize);
//                        checkDetailStartPlanDate.setText(sPlanDate);
                        checkDetailQuantity.setText(sQuantity);
                        checkDetailStockId.setText(sStockId);
                        checkDetailStock.setText(sStock);
                        checkDetailQuantityPcs.setText(sWeight);
                        checkDetailPositionId.setText(sStockLocationId);
                        checkDetailPosition.setText(sStockLocation);
                        checkDetailLot.setText(sLots);
                        checkDetailFeatures.setText(sFeatures);
                        checkDetailDocno.setText(qrCode);
                    }
                }
                loadingDialog.dismiss();
            }
        });
    }
}