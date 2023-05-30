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
import com.hz.scantool.adapter.QueryDataAdapter;
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

public class QueryLabelActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle;
    private String statusCode,statusDescription;

    private TextView queryLabelQrcode,queryLabelProcessId,queryLabelProcess,queryLabelDevices,queryLabelQty,queryLabelStatus;
    private TextView queryLabelProduct,queryLabelProductName,queryLabelProductModel;
    private EditText inputQueryQrcode;
    private Button btnCancel,btnQuery;
    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_label);

        //初始化
        initBundle();
        initView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(QueryLabelActivity.this);
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
            //标签查询
            getDataListData("1",qrContent);
        }

    }

    /**
     *描述: 初始化控件
     *日期：2022/11/3
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.queryLabelToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        queryLabelQrcode = findViewById(R.id.queryLabelQrcode);
        queryLabelProcessId = findViewById(R.id.queryLabelProcessId);
        queryLabelProcess = findViewById(R.id.queryLabelProcess);
        queryLabelDevices = findViewById(R.id.queryLabelDevices);
        queryLabelQty = findViewById(R.id.queryLabelQty);
        queryLabelStatus = findViewById(R.id.queryLabelStatus);
        queryLabelProduct = findViewById(R.id.queryLabelProduct);
        queryLabelProductName = findViewById(R.id.queryLabelProductName);
        queryLabelProductModel = findViewById(R.id.queryLabelProductModel);
        inputQueryQrcode = findViewById(R.id.inputQueryQrcode);
        btnCancel = findViewById(R.id.btnCancel);
        btnQuery = findViewById(R.id.btnQuery);

        //定义事件
        //定义事件
        btnQuery.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
    }

    /**
     *描述: 初始化传入参数
     *日期：2023/1/2
     **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
    }

    /**
     *描述: 按钮事件
     *日期：2022/6/12
     **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnQuery:
                    String sQrcode=inputQueryQrcode.getText().toString().trim().toUpperCase();
                    if(sQrcode.equals("")||sQrcode.isEmpty()){
                        MyToast.myShow(QueryLabelActivity.this,"请输入条码编号再查询",2,0);
                    }else{
                        getDataListData("0",sQrcode);
                    }
                    break;
                case R.id.btnCancel:
                    inputQueryQrcode.setText("");
                    break;
            }
        }
    }

    /**
     *描述: 获取查询报表数据
     *日期：2023/1/2
     **/
    private void getDataListData(String type,String qrcode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(QueryLabelActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名:cwssp024
                String webServiceName = "QueryReport";
                String strType = "4";

                //查询条件
                String sWhere="";
                if(type.equals("1")){
                    //扫描查询
                    sWhere = " (bcaa001='"+qrcode.trim()+"' OR bcaa011='"+qrcode.trim()+"')";
                }else{
                    //模糊查询
                    sWhere = " bcaa001 LIKE '%"+qrcode.trim()+"%'";
                }

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"user\" value=\""+UserInfo.getUserId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ sWhere +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonQueryProductLabelData(strResponse,"iteminfo");

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
                    MyToast.myShow(QueryLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(QueryLabelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    //显示成功清单
                    if(mapResponseList.size()>0){
                        queryLabelQrcode.setText((String)mapResponseList.get(0).get("Qrcode"));
                        queryLabelProcessId.setText((String)mapResponseList.get(0).get("ProcessId"));
                        queryLabelProcess.setText((String)mapResponseList.get(0).get("Process"));
                        queryLabelDevices.setText((String)mapResponseList.get(0).get("Device"));
                        queryLabelQty.setText((String)mapResponseList.get(0).get("Quantity"));
                        queryLabelStatus.setText((String)mapResponseList.get(0).get("Status"));
                        queryLabelProduct.setText((String)mapResponseList.get(0).get("ProductCode"));
                        queryLabelProductName.setText((String)mapResponseList.get(0).get("ProductName"));
                        queryLabelProductModel.setText((String)mapResponseList.get(0).get("ProductModel"));

                        String sFlag = (String)mapResponseList.get(0).get("Flag");
                        if(sFlag.equals("N")){
                            queryLabelStatus.setTextColor(getResources().getColor(R.color.master_loginout));
                        }else{
                            queryLabelStatus.setTextColor(getResources().getColor(R.color.teal_700));
                        }
                    }
                }else{
                    MyToast.myShow(QueryLabelActivity.this,statusDescription,0,0);
                }

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}