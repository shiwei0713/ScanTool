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
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class QueryStockActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle;

    private EditText inputQueryStockQrcode;
    private Button btnQueryStockQrcode;
    private TextView queryStockProductCode,queryStockProductName,queryStockProductModels,queryStockQuantity,queryStockName,queryStockCount;
    private TextView queryStockQty,queryStockCountNg,queryProductCount,queryProductCountNg,queryProductQty,queryTrayName;
    private ListView stockList;

    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private String statusCode,statusDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_stock);

        //初始化
        initView();
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

    /**
     *描述: 工具栏菜单样式
     *日期：2022/5/25
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu,menu);

        return true;
    }

    /**
     *描述: 工具栏菜单事件
     *日期：2022/5/25
     **/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(QueryStockActivity.this);
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
    private void scanResult(String qrContent, Context context, Intent intent){
        //解析二维码
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            getQrcodeData(qrContent);
        }
    }

    /**
     *描述: 初始化控件
     *日期：2022/6/6
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.queryStockToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        strTitle = getResources().getString(R.string.query_stock);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        inputQueryStockQrcode = findViewById(R.id.inputQueryStockQrcode);
        queryStockProductCode = findViewById(R.id.queryStockProductCode);
        queryStockProductName = findViewById(R.id.queryStockProductName);
        queryStockProductModels = findViewById(R.id.queryStockProductModels);
        queryStockQuantity = findViewById(R.id.queryStockQuantity);
        queryStockName = findViewById(R.id.queryStockName);
        queryStockCount = findViewById(R.id.queryStockCount);
        queryStockQty = findViewById(R.id.queryStockQty);
        queryStockCountNg = findViewById(R.id.queryStockCountNg);
        queryProductCount = findViewById(R.id.queryProductCount);
        queryProductCountNg = findViewById(R.id.queryProductCountNg);
        queryProductQty = findViewById(R.id.queryProductQty);
        queryTrayName = findViewById(R.id.queryTrayName);
        stockList = findViewById(R.id.stockList);
        btnQueryStockQrcode = findViewById(R.id.btnQueryStockQrcode);

        //ListView增加表头
        View header = getLayoutInflater().inflate(R.layout.list_product_material_head,null);
        stockList.addHeaderView(header);

        //定义事件
        btnQueryStockQrcode.setOnClickListener(new btnClickListener());
    }

    /**
     *描述: 按钮事件
     *日期：2022/6/12
     **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnQueryStockQrcode:
                    String sQrcode = inputQueryStockQrcode.getText().toString();
                    getQrcodeData(sQrcode.toUpperCase());
                    break;
            }
        }
    }

    /**
     *描述: 扫描扫码,获取库容分布信息
     *日期：2022/6/10
     **/
    private void getQrcodeData(String qrcode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(QueryStockActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "ItemInfoGet";
                String strType = "3";
                String strwhere = "";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonItemStockListData(strResponse,"iteminfo");

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
                    MyToast.myShow(QueryStockActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(QueryStockActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()> 0) {
                        String sProductCode = "";
                        String sProductName = "";
                        String sProductModels = "";
                        String sQuantity = "";
                        String sStock = "";
                        String sPackage = "0";
                        String sStockCount = "0";
                        String sStockCountNg = "0";
                        String sStockCountOk = "0";
                        String sProductCount = "0";
                        String sProductCountNg = "0";
                        String sProductCountOk = "0";

                        for (Map<String, Object> mResponse : mapResponseList) {
                            sProductCode = mResponse.get("ProductCode").toString();
                            sProductName = mResponse.get("ProductName").toString();
                            sProductModels = mResponse.get("ProductModels").toString();
                            sStock = mResponse.get("Stock").toString();
                            sPackage = mResponse.get("Package").toString();
                            sStockCount = mResponse.get("StockCount").toString();
                            sStockCountNg = mResponse.get("StockCountNg").toString();
                            sStockCountOk = mResponse.get("StockCountOk").toString();
                            sQuantity = mResponse.get("Quantity").toString();
                            sProductCount = mResponse.get("ProductCount").toString();
                            sProductCountNg = mResponse.get("ProductCountNg").toString();
                            sProductCountOk = mResponse.get("ProductCountOk").toString();
                        }

                        queryStockProductCode.setText(sProductCode);
                        queryStockProductName.setText(sProductName);
                        queryStockProductModels.setText(sProductModels);
                        queryStockQuantity.setText(sQuantity);
                        queryStockName.setText(sStock);
                        queryTrayName.setText(sPackage);
                        queryStockCount.setText(sStockCount);
                        queryStockCountNg.setText(sStockCountNg);
                        queryStockQty.setText(sStockCountOk);
                        queryProductCount.setText(sProductCount);
                        queryProductCountNg.setText(sProductCountNg);
                        queryProductQty.setText(sProductCountOk);
                    }
                }else{
                    MyToast.myShow(QueryStockActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}