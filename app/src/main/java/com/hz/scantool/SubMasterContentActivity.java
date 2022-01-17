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
import android.graphics.drawable.Drawable;
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
import com.hz.scantool.adapter.SubMasterListItemAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

public class SubMasterContentActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle="";
    private String strType;
    private String strWhere;
    private String statusCode;
    private String statusDescription;
    private String strScanContent;
    private int actionId;
    private Bundle bundle;
    private TextView txtSubTask1;
    private TextView txtSubTask2;
    private Button btnMasterFlag1;
    private Button btnMasterFlag2;
    private Button btnMasterFlag3;
    private Button btnMasterFlag4;
    private ListView subMasterContentView;
    private ProgressBar subMasterContentProgressBar;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private SubMasterListItemAdapter subMasterListItemAdapter;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_master_content);

        //初始化传入参数
        //初始化控件
        initBundle();
        intiView();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subMasterContentToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //显示数据
        if(actionId ==62){
            //完工入庫
            initQueryCondition(4);
            getSubContentWorkOrderListData();
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubMasterContentActivity.this);
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

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(scanReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        strScanContent="";
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
            if(actionId ==62){
                //完工入库
                //初始化查询条件为当天，同时生成asft340单据为审核状态
                if(qrContent.equals(strScanContent)){
                    MyToast.myShow(context,"重复扫描",2,1);
                }else{
                    initQueryCondition(3);
                    genT100Doc(qrContent);
                }
                strScanContent=qrContent;
            }
        }
    }

    //获取传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        actionId = bundle.getInt("btnId");
        strTitle = bundle.getString("title");
        strScanContent="";
    }

    //初始化控件
    private void intiView(){
        txtSubTask1 = findViewById(R.id.txtSubTask1);
        txtSubTask2 = findViewById(R.id.txtSubTask2);
        btnMasterFlag1 = findViewById(R.id.btnMasterFlag1);
        btnMasterFlag2 = findViewById(R.id.btnMasterFlag2);
        btnMasterFlag3 = findViewById(R.id.btnMasterFlag3);
        btnMasterFlag4 = findViewById(R.id.btnMasterFlag4);
        subMasterContentView = findViewById(R.id.subMasterContentView);
        subMasterContentProgressBar = findViewById(R.id.subMasterContentProgressBar);

        //声明按钮ID和图片ID
        int[] btnId = new int[]{R.id.txtSubTask1, R.id.txtSubTask2};
        int[] imgId = new int[]{R.drawable.task1, R.drawable.task2};
        int[] titleId = new int[]{R.string.sub_master_content_quantity,R.string.sub_master_content_quantitypcs};

        //初始化按钮和图片
        TextView textAction;
        Drawable drawable;

        //设置按钮样式
        for(int i=0;i<btnId.length;i++){
            textAction=findViewById(btnId[i]);
            drawable=getResources().getDrawable(imgId[i]);
            drawable.setBounds(0,0,50,50);
            textAction.setCompoundDrawables(drawable,null,null,null);
            textAction.setCompoundDrawablePadding(10);
            textAction.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            textAction.setText(getResources().getString(titleId[i]));
        }

        //初始化flag按钮状态
        btnMasterFlag1.setSelected(false);
        btnMasterFlag2.setSelected(false);
        btnMasterFlag3.setSelected(false);
        btnMasterFlag4.setSelected(true);

        //绑定按钮事件
        btnMasterFlag1.setOnClickListener(new queryClickListener());
        btnMasterFlag2.setOnClickListener(new queryClickListener());
        btnMasterFlag3.setOnClickListener(new queryClickListener());
        btnMasterFlag4.setOnClickListener(new queryClickListener());

        if(actionId==62){
            //完工入库
            strType = "21";
        }

    }

    private class queryClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnMasterFlag1:
                    initQueryCondition(1);
                    break;
                case R.id.btnMasterFlag2:
                    initQueryCondition(2);
                    break;
                case R.id.btnMasterFlag3:
                    initQueryCondition(3);
                    break;
                case R.id.btnMasterFlag4:
                    initQueryCondition(4);
                    break;
            }

            getSubContentWorkOrderListData();
        }
    }

    //初始化查询条件
    private void initQueryCondition(int i){
        switch (i){
            //未检验
            case 1:
                btnMasterFlag1.setSelected(true);
                btnMasterFlag2.setSelected(false);
                btnMasterFlag3.setSelected(false);
                btnMasterFlag4.setSelected(false);
                strWhere = "sfeb027=0";
                break;
            //未入库
            case 2:
                btnMasterFlag1.setSelected(false);
                btnMasterFlag2.setSelected(true);
                btnMasterFlag3.setSelected(false);
                btnMasterFlag4.setSelected(false);
                strWhere = "sfeb027=sfeb008";
                break;
            //当日
            case 3:
                btnMasterFlag1.setSelected(false);
                btnMasterFlag2.setSelected(false);
                btnMasterFlag3.setSelected(true);
                btnMasterFlag4.setSelected(false);
                strWhere = "sfeadocdt=to_date('"+setQueryDate(0)+"','YYYY-MM-DD')";
                break;
            //所有
            case 4:
                btnMasterFlag1.setSelected(false);
                btnMasterFlag2.setSelected(false);
                btnMasterFlag3.setSelected(false);
                btnMasterFlag4.setSelected(true);
                strWhere = "1=1";
                break;
        }
    }

    //设置查询日期
    private String setQueryDate(int interval){
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,interval);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date date =  calendar.getTime();
        String strDate = simpleDateFormat.format(date);

        return strDate;
    }

    //刷新统计数
    private void refreshCount(){
        int iQuantity = 0;
        int iQuantityPcs=0;
        int iQuantityTotal = 0;
        int iQuantityPcsTotal=0;

        for(Map<String,Object> mData: mapResponseList) {
            String sQuantity = mData.get("Quantity").toString();
            String sQuantityPcs = mData.get("QuantityPcs").toString();

            if(!sQuantity.isEmpty()){
                iQuantity =Integer.parseInt(sQuantity);
            }else{
                iQuantity = 0;
            }

            if(!sQuantityPcs.isEmpty()){
                iQuantityPcs = Integer.parseInt(sQuantityPcs);
            }else{
                iQuantityPcs = 0;
            }

            iQuantityTotal = iQuantityTotal + iQuantity;
            iQuantityPcsTotal = iQuantityPcsTotal + iQuantityPcs;
        }

        txtSubTask1.setText(getResources().getString(R.string.sub_master_content_quantity)+String.valueOf(iQuantityTotal));
        txtSubTask2.setText(getResources().getString(R.string.sub_master_content_quantitypcs)+String.valueOf(iQuantityPcsTotal));
    }

    private void getSubContentWorkOrderListData(){
        //显示进度条
        subMasterContentProgressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppWorkOrderListGet";

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
                mapResponseList = t100ServiceHelper.getT100JsonWorkOrderStockinData(strResponse,"workorder");

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
                            MyToast.myShow(SubMasterContentActivity.this,statusDescription,0,0);
                        }else{
                            int progress = subMasterContentProgressBar.getProgress();
                            progress = progress + 50;
                            subMasterContentProgressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubMasterContentActivity.this,"无入库数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubMasterContentActivity.this,"网络错误",0,0);
                subMasterContentProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                subMasterListItemAdapter = new SubMasterListItemAdapter(mapResponseList,getApplicationContext(),strType, mConfirmClickListener);
                subMasterContentView.setAdapter(subMasterListItemAdapter);

                subMasterContentProgressBar.setVisibility(View.GONE);
                refreshCount();
            }
        });
    }

    //生成T100单据
    private void genT100Doc(String qrCode){
        //显示进度条
        subMasterContentProgressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "InventoryBillRequestGen";
                String strProg = "asft340";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"inaj_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"inajsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"inajent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"inaj015\" value=\""+strProg+"\"/&gt;\n"+
                        "&lt;Field name=\"inajuser\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"qrcode\" value=\""+ qrCode +"\"/&gt;\n"+  //二维码
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
                mapResponseList = t100ServiceHelper.getT100ResponseDocno(strResponse,"docno");

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

                        if(!statusCode.equals("0")) {
                            MyToast.myShow(SubMasterContentActivity.this, statusDescription, 0, 1);
                        }else{
                            int progress = subMasterContentProgressBar.getProgress();
                            progress = progress + 50;
                            subMasterContentProgressBar.setProgress(progress);
                            MyToast.myShow(SubMasterContentActivity.this, statusDescription, 1, 0);
                        }
                    }
                }else{
                    MyToast.myShow(SubMasterContentActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
//                MyToast.myShow(SubMasterContentActivity.this,"网络错误",0,0);
                //解析二维码
                String[] qrCodeValue = qrCode.split("_");
                Intent intent = new Intent(SubMasterContentActivity.this,ErrorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("qrCode", qrCodeValue[0]);
                intent.putExtras(bundle);
                startActivity(intent);

                subMasterContentProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    String strDocno="";

                    if(mapResponseList.size()> 0) {
                        for (Map<String, Object> mResponse : mapResponseList) {
                            strDocno = mResponse.get("Docno").toString();
                        }
                    }
                    if(!strDocno.isEmpty()){
                        strWhere = "sfeadocno='"+strDocno+"'";
                    }
                    getSubContentWorkOrderListData();
                }
                subMasterContentProgressBar.setVisibility(View.GONE);
            }
        });
    }

    //托盘完成
    private SubMasterListItemAdapter.ConfirmClickListener mConfirmClickListener = new SubMasterListItemAdapter.ConfirmClickListener() {
        @Override
        public void ConfirmOnClick(int position, View v) {
            //显示进度条
            loadingDialog = new LoadingDialog(SubMasterContentActivity.this,"数据提交中",R.drawable.dialog_loading);
            loadingDialog.show();

            Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
                @Override
                public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                    //初始化T100服务名
                    String webServiceName = "InventoryBillRequestConfirm";
                    String strProg = "asft340";
//                    String strDocno = mapResponseList.get(position).get("Docno").toString();
                    String strDocno = subMasterListItemAdapter.getItemValue(position);

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
                                MyToast.myShow(SubMasterContentActivity.this, statusDescription, 0, 1);
                            }else{
                                Button listBtn = v.findViewById(R.id.txtSubContentListBtnDelete);
                                listBtn.setVisibility(View.INVISIBLE);
//                                int progress = subMasterContentProgressBar.getProgress();
//                                progress = progress + 50;
//                                subMasterContentProgressBar.setProgress(progress);
                                MyToast.myShow(SubMasterContentActivity.this, statusDescription, 1, 0);
                            }
                        }
                    }else{
                        MyToast.myShow(SubMasterContentActivity.this,"执行接口错误",2,0);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    MyToast.myShow(SubMasterContentActivity.this,mapResponseStatus.toString(),0,0);
                    getSubContentWorkOrderListData();
//                    subMasterContentProgressBar.setVisibility(View.GONE);
                    loadingDialog.dismiss();
                }

                @Override
                public void onComplete() {
                    getSubContentWorkOrderListData();
//                    subMasterContentProgressBar.setVisibility(View.GONE);
                    loadingDialog.dismiss();
                }
            });
        }
    };
}