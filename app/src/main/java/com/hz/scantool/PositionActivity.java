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

public class PositionActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle;
    private String strStockId;
    private String strPositionId;
    private String strPosition;
    private String strStockType;
    private String statusCode;
    private String statusDescription;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    private TextView checkPosition;
    private TextView checkDetailProductCode;
    private TextView checkDetailProductModels;
    private TextView checkDetailModel;
    private TextView checkDetailStartPlanDate;
    private TextView checkDetailQuantity;
    private TextView checkDetailStockId;
    private TextView checkDetailStock;
    private TextView checkDetailPositionId;
    private TextView checkDetailPosition;
    private EditText checkDetailQuantityPcs;
    private TextView checkDetailDocno;
    private Button btnCheckError,btnCancelCheckError;
    private Button btnCheckModify;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);

        //初始化控件
        initBundle();
        initView();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.positionToolBar);
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(PositionActivity.this);
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
        String[] qrCodeValue = qrContent.split("_");
        if(qrContent.isEmpty()||qrContent.equals("")){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            if(strStockId.isEmpty()|| strStockId.equals("")){
                MyToast.myShow(context,"储位不可为空,请先扫描储位码",0,1);
            }else{
                getScanQrData(qrCodeValue[0].toString(),"insert");
            }
        }
    }

    private void initView(){
        checkPosition = findViewById(R.id.checkPosition);
        checkDetailProductCode = findViewById(R.id.checkDetailProductCode);
        checkDetailProductModels = findViewById(R.id.checkDetailProductModels);
        checkDetailModel = findViewById(R.id.checkDetailModel);
        checkDetailStartPlanDate = findViewById(R.id.checkDetailStartPlanDate);
        checkDetailQuantity = findViewById(R.id.checkDetailQuantity);
        checkDetailStockId = findViewById(R.id.checkDetailStockId);
        checkDetailStock = findViewById(R.id.checkDetailStock);
        checkDetailQuantityPcs = findViewById(R.id.checkDetailQuantityPcs);
        checkDetailPositionId = findViewById(R.id.checkDetailPositionId);
        checkDetailPosition = findViewById(R.id.checkDetailPosition);
        checkDetailDocno = findViewById(R.id.checkDetailDocno);

        btnCheckError= findViewById(R.id.btnCheckError);
        btnCancelCheckError = findViewById(R.id.btnCancelCheckError);
        btnCheckModify = findViewById(R.id.btnCheckModify);
        checkPosition.setText(strPosition);

        btnCheckError.setOnClickListener(new btnClickListener());
        btnCancelCheckError.setOnClickListener(new btnClickListener());
        btnCheckModify.setOnClickListener(new btnClickListener());
    }

    //初始化参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        strStockId = bundle.getString("stockid");
        strPosition = bundle.getString("position");
        strPositionId = bundle.getString("positionid");
        strStockType = bundle.getString("stocktype");
    }

    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnCheckError:
                    getScanQrData("","check");
                    break;
                case R.id.btnCancelCheckError:
                    getScanQrData("","clear");
                    break;
                case R.id.btnCheckModify:
                    getScanQrData("","update");
                    break;
            }
        }
    }

    //获取扫描条码信息
    private void getScanQrData(String qrCode,String actcode){
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
                        "&lt;Field name=\"bcah006\" value=\""+strPositionId+"\"/&gt;\n"+   //储位编号
                        "&lt;Field name=\"stocktype\" value=\""+strStockType+"\"/&gt;\n"+   //仓库类别,区分材料(Y)和非材料(N)
                        "&lt;Field name=\"bcah016\" value=\""+checkDetailQuantity.getText().toString()+"\"/&gt;\n"+     //托盘数量
                        "&lt;Field name=\"bcah017\" value=\""+checkDetailQuantityPcs.getText().toString()+"\"/&gt;\n"+     //托盘箱数
                        "&lt;Field name=\"bcah001\" value=\""+checkDetailDocno.getText().toString()+"\"/&gt;\n"+     //单据编号
                        "&lt;Field name=\"actcode\" value=\""+actcode+"\"/&gt;\n"+     //执行动作
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
                            MyToast.myShow(PositionActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(PositionActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(PositionActivity.this,"网络错误",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(actcode.equals("insert")){
                    if(mapResponseList.size()>0){
                        String sProductCode = "";
                        String sProductName = "";
                        String sProductModels = "";
                        String sProductSize = "";
                        String sQuantity = "";
                        String sStockId = "";
                        String sStock = "";
                        String sStockLocationId = "";
                        String sStockLocation = "";
                        String sWeight = "";
                        String sLots = "";
                        String sFeatures = "";
                        String sFeaturesName = "";
                        String sFeaturesModels = "";
                        String sTray = "";

                        for(Map<String,Object> mData: mapResponseList){
                            sProductCode = mData.get("ProductCode").toString();
                            sProductName = mData.get("ProductName").toString();
                            sProductModels = mData.get("ProductModels").toString();
                            sProductSize = mData.get("ProductSize").toString();
                            sQuantity = mData.get("Quantity").toString();
                            sStockId = mData.get("StockId").toString();
                            sStock = mData.get("Stock").toString();
                            sStockLocationId = mData.get("StockLocationId").toString();
                            sStockLocation = mData.get("StockLocation").toString();
                            sWeight = mData.get("Weight").toString();
                            sLots = mData.get("Lots").toString();
                            sFeatures = mData.get("Features").toString();
                            sFeaturesName = mData.get("FeaturesName").toString();
                            sFeaturesModels = mData.get("FeaturesModels").toString();
                            sTray = mData.get("Tray").toString();
                        }

                        checkDetailProductCode.setText(sProductCode);
                        checkDetailProductModels.setText(sProductName);
                        checkDetailModel.setText(sProductModels);
//                        checkDetailStartPlanDate.setText(sPlanDate);
                        checkDetailQuantity.setText(sQuantity);
                        checkDetailStockId.setText(sStockId);
                        checkDetailStock.setText(sStock);
                        checkDetailQuantityPcs.setText(sWeight);
                        checkDetailPositionId.setText(sStockLocationId);
                        checkDetailPosition.setText(sStockLocation);
                        checkDetailDocno.setText(qrCode);

                        btnCheckError.setVisibility(View.GONE);

//                        if(sTray.equals("N")){
//                            finish();
//                        }
                    }
                }else{
                    if(statusCode.equals("0")){
                        MyToast.myShow(PositionActivity.this,statusDescription,1,0);
                        finish();
                    }
                }
                loadingDialog.dismiss();
            }
        });
    }
}