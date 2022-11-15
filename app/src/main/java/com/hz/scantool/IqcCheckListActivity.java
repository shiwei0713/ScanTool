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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.IqcCheckListAdapter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubMasterListItemAdapter;
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

public class IqcCheckListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private static final int CHECKMATERIAL = 1003;

    private String strTitle,strType;
    private String statusCode;
    private String statusDescription;

    private EditText inputSupply;
    private CheckBox checkBox1,checkBox2;
    private Button btnQuery,btnClear;
    private ProgressBar progressBar;
    private LoadingDialog loadingDialog;
    private ListView iqcCheckListView;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private IqcCheckListAdapter iqcCheckListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iqc_check_list);

        //初始化
        initBundle();
        initView();

        //初始化数据
        getSubQcListData();

    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        strType = "8";  //原材料
    }

    //初始化控件
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.iqcCheckListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        inputSupply = findViewById(R.id.inputSupply);
        checkBox1 = findViewById(R.id.checkBox1);
        checkBox2 = findViewById(R.id.checkBox2);
        btnQuery = findViewById(R.id.btnQuery);
        btnClear = findViewById(R.id.btnClear);
        progressBar = findViewById(R.id.progressBar);
        iqcCheckListView = findViewById(R.id.iqcCheckListView);

        //绑定事件
        checkBox1.setOnCheckedChangeListener(new checkClickListener());
        checkBox2.setOnCheckedChangeListener(new checkClickListener());
        btnQuery.setOnClickListener(new btnClickListener());
        btnClear.setOnClickListener(new btnClickListener());
        iqcCheckListView.setOnItemClickListener(new listItemClickListener());

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
                IntentIntegrator intentIntegrator = new IntentIntegrator(IqcCheckListActivity.this);
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
        }else if(requestCode==CHECKMATERIAL && resultCode == CHECKMATERIAL){
            String sResult = data.getStringExtra("result");
            if(sResult.equals("0")){
                getSubQcListData();
            }
        }
    }

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){
        //解析二维码
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            if(strType.equals("8")){
                getMaterialDetailData(qrContent);
            }else{
                MyToast.myShow(IqcCheckListActivity.this,"原材料才可直接扫描材料标签",2,0);
            }
        }
    }

    /**
     *描述: 检验类别选择事件
     *日期：2022/10/27
     **/
    private class checkClickListener implements CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            CheckBox checkBox = (CheckBox)compoundButton;
            if(b){
                if(checkBox==checkBox1){
                    strType = "8";
                    checkBox2.setChecked(false);
                }else{
                    strType = "9";
                    checkBox1.setChecked(false);
                }
            }
        }
    }

    /**
    *描述: 按钮单击事件
    *日期：2022/11/9
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnQuery:
                    getSubQcListData();
                    break;
                case R.id.btnClear:
                    break;
            }
        }
    }

    /**
     *描述: 清单行点击事件
     *日期：2022/7/19
     **/
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(strType.equals("9")){
                TextView txtListIqcSupply = view.findViewById(R.id.listIqcSupply);

                Intent intent = new Intent(IqcCheckListActivity.this, IqcCheckLabelActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("supply",txtListIqcSupply.getText().toString());
                bundle.putString("title",strTitle);
                intent.putExtras(bundle);
                startActivity(intent);
            }else {
                MyToast.myShow(IqcCheckListActivity.this,"原材料检验,请直接扫描材料标签",2,0);
            }
        }
    }

    /**
    *描述: 显示QC清单
    *日期：2022/11/9
    **/
    private void getSubQcListData(){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppListGet";
                String strWhere = "";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonIqcListData(strResponse,"iqc");

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
                            MyToast.myShow(IqcCheckListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = progressBar.getProgress();
                            progress = progress + 50;
                            progressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(IqcCheckListActivity.this,"无入库数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(IqcCheckListActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                iqcCheckListAdapter = new IqcCheckListAdapter(mapResponseList,getApplicationContext(),strType);
                iqcCheckListView.setAdapter(iqcCheckListAdapter);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     *描述: 显示原材料明细数据
     *日期：2022/11/9
     **/
    private void getMaterialDetailData(String qrCode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(IqcCheckListActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名:cwssp017
                String webServiceName = "ItemInfoGet";
                String strType = "11";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"user\" value=\""+UserInfo.getUserId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+ qrCode +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonIqcData(strResponse,"iteminfo");

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
                    MyToast.myShow(IqcCheckListActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(IqcCheckListActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()>0){
                        String qrcode = "";
                        String docNo = "";
                        String productCode = "";
                        String productName = "";
                        String productModel = "";
                        String productSize = "";
                        String stockId = "";
                        String stock = "";
                        String positionId = "";
                        String position = "";
                        String lots = "";
                        String planDate = "";
                        String sQuantity = "";
                        String status = "N";

                        for(int i = 0;i<mapResponseList.size();i++){
                            qrcode = qrCode;
                            docNo = (String)mapResponseList.get(i).get("Docno");
                            productCode = (String)mapResponseList.get(i).get("ProductCode");
                            productName = (String)mapResponseList.get(i).get("ProductName");
                            productModel = (String)mapResponseList.get(i).get("ProductModels");
                            productSize = (String)mapResponseList.get(i).get("ProductSize");
                            stockId = (String)mapResponseList.get(i).get("StockId");
                            stock = (String)mapResponseList.get(i).get("Stock");
                            positionId = (String)mapResponseList.get(i).get("PositionId");
                            position = (String)mapResponseList.get(i).get("Position");
                            lots = (String)mapResponseList.get(i).get("Lots");
                            planDate = (String)mapResponseList.get(i).get("Date");
                            sQuantity = (String)mapResponseList.get(i).get("Quantity");
                            status = (String)mapResponseList.get(i).get("Status");
                        }

                        Intent intent = new Intent(IqcCheckListActivity.this,IqcCheckMaterialDetailActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("qrcode",qrcode);
                        bundle.putString("docNo",docNo);
                        bundle.putString("productCode",productCode);
                        bundle.putString("productName",productName);
                        bundle.putString("productModel",productModel);
                        bundle.putString("planDate",planDate);
                        bundle.putString("productSize",productSize);
                        bundle.putString("stockId",stockId);
                        bundle.putString("stock",stock);
                        bundle.putString("positionId",positionId);
                        bundle.putString("position",position);
                        bundle.putString("lots",lots);
                        bundle.putString("quantity",sQuantity);
                        bundle.putString("status",status);
                        intent.putExtras(bundle);
                        startActivityForResult(intent,CHECKMATERIAL);
                    }
                }else{
                    MyToast.myShow(IqcCheckListActivity.this,statusDescription,0,0);
                }

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}