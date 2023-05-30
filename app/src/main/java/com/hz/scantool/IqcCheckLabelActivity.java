package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.IqcCheckResultAdapter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyAlertDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.database.QrcodeEntity;
import com.hz.scantool.dialog.ShowAlertDialog;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.ArrayList;
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

public class IqcCheckLabelActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private static final int CHECKDETAIL = 1002;

    private HzDb hzDb;
    private String dataBaseName = "HzDb";
    private String strTitle,strSupply;
    private String statusCode,statusDescription;
    private int actionId,iTotal;
    private float fTotal;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mLocalList;

    private ScrollView viewBasic;
    private TextView iqcCheckSupply,iqcCheckQty,iqcCheckQtyPcs;
    private Button btnHidden,btnShow,btnSave,btnDelete;
    private ListView iqcCheckLoadView;
    private LoadingDialog loadingDialog;
    private IqcCheckResultAdapter iqcCheckResultAdapter;
    private List<QrcodeEntity> qrcodeEntityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iqc_check_label);

        //初始化
        initBundle();
        initView();
        initListener();
        initDataBase();

        //初始化数据
        getDbData();

    }

    /**
    *描述: 初始化控件
    *日期：2022/11/3
    **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.iqcCheckToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        viewBasic = findViewById(R.id.viewBasic);
        iqcCheckSupply = findViewById(R.id.iqcCheckSupply);
        iqcCheckQty = findViewById(R.id.iqcCheckQty);
        iqcCheckQtyPcs = findViewById(R.id.iqcCheckQtyPcs);
        btnHidden = findViewById(R.id.btnHidden);
        btnShow = findViewById(R.id.btnShow);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        iqcCheckLoadView = findViewById(R.id.iqcCheckLoadView);

        //ListView增加表头
        View header = getLayoutInflater().inflate(R.layout.list_iqc_check_result_head,null);
        iqcCheckLoadView.addHeaderView(header);

        //初始化值
        iqcCheckSupply.setText(strSupply);

    }

    /**
    *描述: 初始化事件
    *日期：2022/11/3
    **/
    private void initListener(){
        btnHidden.setOnClickListener(new btnClickListener());
        btnShow.setOnClickListener(new btnClickListener());
        btnSave.setOnClickListener(new btnClickListener());
        btnDelete.setOnClickListener(new btnClickListener());
    }

    /**
    *描述: 按钮单击事件
    *日期：2022/11/3
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnHidden:
                    viewBasic.setVisibility(View.GONE);
                    break;
                case R.id.btnShow:
                    viewBasic.setVisibility(View.VISIBLE);
                    break;
                case R.id.btnSave:
                    saveDataToErp("Y");
                    break;
                case R.id.btnDelete:
                    deleteDbData("DELETE");
                    break;
            }
        }
    }

    /**
    *描述: 初始化传入参数
    *日期：2022/11/3
    **/
    private void initBundle(){

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strSupply = bundle.getString("supply");
        strTitle = bundle.getString("title");

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
                IntentIntegrator intentIntegrator = new IntentIntegrator(IqcCheckLabelActivity.this);
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

    /**
    *描述: 初始化本地数据库
    *日期：2022/11/3
    **/
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this,HzDb.class,dataBaseName).build();
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
        }else if(requestCode==CHECKDETAIL && resultCode == CHECKDETAIL){
            String sResult = data.getStringExtra("result");
            if(sResult.equals("0")){
                getDbData();
            }
        }
    }

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){
        //解析二维码
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            getProductListData(qrContent);
        }
    }

    /**
    *描述: 获取条码数据
    *日期：2022/11/3
    **/
    private void getProductListData(String qrCode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(IqcCheckLabelActivity.this,"数据查询中",R.drawable.dialog_loading);
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
                    MyToast.myShow(IqcCheckLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(IqcCheckLabelActivity.this,e.getMessage(),0,0);
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
                        String planDate = "";
                        String sQuantity = "";
                        String status = "N";

                        for(int i = 0;i<mapResponseList.size();i++){
                            qrcode = qrCode;
                            docNo = (String)mapResponseList.get(i).get("Docno");
                            productCode = (String)mapResponseList.get(i).get("ProductCode");
                            productName = (String)mapResponseList.get(i).get("ProductName");
                            productModel = (String)mapResponseList.get(i).get("ProductModels");
                            planDate = (String)mapResponseList.get(i).get("Date");
                            sQuantity = (String)mapResponseList.get(i).get("Quantity");
                            status = (String)mapResponseList.get(i).get("Status");
                        }

                        Intent intent = new Intent(IqcCheckLabelActivity.this,IqcCheckLabelDetailActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("qrcode",qrcode);
                        bundle.putString("docNo",docNo);
                        bundle.putString("productCode",productCode);
                        bundle.putString("productName",productName);
                        bundle.putString("productModel",productModel);
                        bundle.putString("planDate",planDate);
                        bundle.putString("quantity",sQuantity);
                        bundle.putString("status",status);
                        intent.putExtras(bundle);
                        startActivityForResult(intent,CHECKDETAIL);
                    }
                }else{
                    MyToast.myShow(IqcCheckLabelActivity.this,statusDescription,0,0);
                }

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
     *描述: 获取本地数据
     *日期：2022/6/10
     **/
    private void getDbData(){

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //查询本地库数据
                if(mLocalList==null){
                    mLocalList = new ArrayList<Map<String,Object>>();
                }else{
                    mLocalList.clear();
                }

                qrcodeEntityList = hzDb.qrcodeDao().getAll();
                for(int j=0;j<qrcodeEntityList.size();j++){
                    mLocalList.add(qrcodeEntityList.get(j).getQrcodeListData());
                }

                e.onNext(mLocalList);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<Map<String, Object>>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<Map<String, Object>> maps) {

            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(IqcCheckLabelActivity.this,e.getMessage(),0,0);
            }

            @Override
            public void onComplete() {
                if(mLocalList!=null){
                    //显示清单
                    iqcCheckResultAdapter = new IqcCheckResultAdapter(mLocalList,getApplicationContext());
                    iqcCheckLoadView.setAdapter(iqcCheckResultAdapter);

                    //显示汇总数
                    fTotal=0;
                    for(int i=0;i<mLocalList.size();i++){
                        float fQty = (float)mLocalList.get(i).get("Quantity");
                        fTotal = fTotal + fQty;
                    }
                    iqcCheckQty.setText(String.valueOf(fTotal));

                    int iTotalPcs = 0;
                    if(mLocalList.size()>0){
                        iTotalPcs = mLocalList.size();
                    }
                    iqcCheckQtyPcs.setText(String.valueOf(iTotalPcs));
                }
            }
        });
    }

    /**
     *描述: 保存数据
     *日期：2022/11/3
     **/
    private void saveDataToErp(String status){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(IqcCheckLabelActivity.this,"数据保存中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<String>(){
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

//                //初始化T100服务名:cwssp028
//                String webServiceName = "CheckUpdateQc";
//                String sCheckType = "IQCUPD";
//
//                //获取扫描批次
//                String strDocNo = hzDb.qrcodeDao().getDcono();
//
//                //发送服务器请求
//                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
//                String requestBody = "&lt;Document&gt;\n"+
//                        "&lt;RecordSet id=\"1\"&gt;\n"+
//                        "&lt;Master name=\"bcacuc_t\" node_id=\"1\"&gt;\n"+
//                        "&lt;Record&gt;\n"+
//                        "&lt;Field name=\"bcacucsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
//                        "&lt;Field name=\"bcacucent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
//                        "&lt;Field name=\"bcacucmodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
//                        "&lt;Field name=\"bcacuc004\" value=\""+ strDocNo +"\"/&gt;\n"+  //扫描批次
//                        "&lt;Field name=\"bcacuc005\" value=\""+ sCheckType +"\"/&gt;\n"+  //扫描类别
//                        "&lt;Field name=\"bcacuc006\" value=\""+ status +"\"/&gt;\n"+  //状态
//                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
//                        "&lt;Record&gt;\n"+
//                        "&lt;Field name=\"bcacucseq\" value=\"1.0\"/&gt;\n"+
//                        "&lt;/Record&gt;\n"+
//                        "&lt;/Detail&gt;\n"+
//                        "&lt;Memo/&gt;\n"+
//                        "&lt;Attachment count=\"0\"/&gt;\n"+
//                        "&lt;/Record&gt;\n"+
//                        "&lt;/Master&gt;\n"+
//                        "&lt;/RecordSet&gt;\n"+
//                        "&lt;/Document&gt;\n";
//                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
//                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
//                for(Map<String,Object> mStatus: mapResponseStatus){
//                    statusCode = mStatus.get("statusCode").toString();
//                    statusDescription = mStatus.get("statusDescription").toString();
//                }

                //清空本地数据
                statusCode="0";
                statusDescription="提交成功";
                if(statusCode.equals("0")){
                    hzDb.qrcodeDao().deleteAll();
                }

                e.onNext(statusCode);
                e.onNext(statusDescription);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {

            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(IqcCheckLabelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    iqcCheckLoadView.setAdapter(null);
                    MyToast.myShow(IqcCheckLabelActivity.this,statusDescription,1,0);
                }else{
//                    MyToast.myShow(IqcCheckLabelActivity.this,statusDescription,0,0);
//                    ShowAlertDialog.myShow(IqcCheckLabelActivity.this,statusDescription);
                    MyAlertDialog.myShowAlertDialog(IqcCheckLabelActivity.this,"错误信息",statusDescription);
                }

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
     *描述: 清除本地数据
     *日期：2022/6/10
     **/
    private void deleteDbData(String type){

        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(IqcCheckLabelActivity.this,"数据保存中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                int iCount = 0;
                statusCode = "0";

                //检查是否存在数据
                iCount = hzDb.qrcodeDao().getAllCount();
                if(iCount>0){
                    //清空数据
                    hzDb.qrcodeDao().deleteAll();
                }
                statusDescription = "删除成功:"+String.valueOf(iCount)+"条数据";

                e.onNext(statusCode);
                e.onNext(statusDescription);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                getDbData();
                if(type.equals("DELETE")){
                    MyToast.myShow(IqcCheckLabelActivity.this,statusDescription,1,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}