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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyAlertDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.database.CheckLabelEntity;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.database.ProductEntity;
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

public class CheckLabelActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle,strProductDocno,strProcessId,strProcess;
    private String statusCode;
    private String statusDescription;
    private String mRecordSet;
    private String strConnectProduct;
    private boolean isArea;
    private int iTotal,iTotalPcs;

    private HzDb hzDb;
    private String dataBaseName = "HzDb";

    private TextView txtViewAreaId,txtViewArea;
    private EditText inputCheckLabelQrcode;
    private Button btnClose,btnSave,btnSetArea;
    private TextView checkLabelProductCode,checkLabelProductName,checkLabelProductModel,checkLabelProcessId,checkLabelProcess,checkLabelQuantity,checkLabelQrcode,txtAreaCheck;
    private TextView checkLabelQuantityTotal,checkLabelPcsTotal;

    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private CheckLabelEntity checkLabelEntity;
    private List<CheckLabelEntity> checkLabelEntityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_label);

        //初始化
        initBundle();
        initView();
        initDataBase();
        deleteDbData();
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(CheckLabelActivity.this);
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

        //解析二维码
        String sQrcode = checkLabelQrcode.getText().toString().trim();
        String sAreaId = txtViewAreaId.getText().toString().trim();
        if(qrContent.isEmpty()||qrContent.equals("")){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            if(!isArea){
                updQrcodeData("GX","12","C",qrContent);
            }else{
                if(!sAreaId.equals("")&&!sAreaId.isEmpty()){
                    updQrcodeData("GX","12","C",qrContent);
                }else{
                    //更新区域数据
                    genAreaData(qrContent,"byemp");
                }
            }
        }
    }

    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.checkLabelToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        txtViewAreaId = findViewById(R.id.txtViewAreaId);
        txtViewArea = findViewById(R.id.txtViewArea);
        inputCheckLabelQrcode = findViewById(R.id.inputCheckLabelQrcode);
        checkLabelProductCode = findViewById(R.id.checkLabelProductCode);
        checkLabelProductName = findViewById(R.id.checkLabelProductName);
        checkLabelProductModel = findViewById(R.id.checkLabelProductModel);
        checkLabelProcessId = findViewById(R.id.checkLabelProcessId);
        checkLabelProcess = findViewById(R.id.checkLabelProcess);
        checkLabelQuantity = findViewById(R.id.checkLabelQuantity);
        checkLabelQrcode = findViewById(R.id.checkLabelQrcode);
        checkLabelQuantityTotal = findViewById(R.id.checkLabelQuantityTotal);
        checkLabelPcsTotal = findViewById(R.id.checkLabelPcsTotal);
        txtAreaCheck = findViewById(R.id.txtAreaCheck);
        btnSave = findViewById(R.id.btnSave);
        btnClose = findViewById(R.id.btnClose);
        btnSetArea = findViewById(R.id.btnSetArea);

        //初始化值
        btnSetArea.setSelected(false);
        txtAreaCheck.setText("关闭");

        //定义事件
        btnSave.setOnClickListener(new btnClickListener());
        btnClose.setOnClickListener(new btnClickListener());
        btnSetArea.setOnClickListener(new btnClickListener());
    }

    /*
     *后台操作，创建数据库
     */
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this,HzDb.class,dataBaseName).build();
    }

    //初始化参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = getResources().getString(R.string.check_label_title1);
        strProductDocno = bundle.getString("ProductDocno");
        strProcessId = bundle.getString("ProcessId");
        strProcess = bundle.getString("Process");
        strConnectProduct = bundle.getString("ConnectProduct");
        isArea = false;
        iTotal = 0;
        iTotalPcs = 0;
        mRecordSet = "";
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
                    String sQrcode = inputCheckLabelQrcode.getText().toString().trim().toUpperCase();
                    if(!sQrcode.equals("")&&!sQrcode.equals("")){
                        updQrcodeData("GX","12","C",sQrcode);
                    }else{
                        MyAlertDialog.myShowAlertDialog(CheckLabelActivity.this,"错误信息","条码编号不可为空");
                    }
                    break;
                case R.id.btnClose:        //关闭
                    finish();
                    break;
                case R.id.btnSetArea:
                    setAreaStatus();
                    break;
            }
        }
    }

    /**
    *描述: 区域设置开关
    *日期：2023/1/3
    **/
    private void setAreaStatus(){
        if(!isArea){
            isArea = true;
            txtAreaCheck.setText("开启");
        }else{
            isArea = false;
            txtAreaCheck.setText("关闭");
        }
        btnSetArea.setSelected(isArea);

        //初始化总数
        iTotal = 0;
        iTotalPcs = 0;
        checkLabelQuantityTotal.setText(String.valueOf(iTotal));
        checkLabelPcsTotal.setText(String.valueOf(iTotalPcs));
    }

    /**
     *描述: XML文件生成
     *日期：2022/6/10
     **/
    private void genRecordSetStr(String areaID,String qrCode,String strAct,int i){
        String sType = "1";
        mRecordSet = mRecordSet + "&lt;Master name=\"bcaa_t\" node_id=\""+i+"\"&gt;\n"+
                "&lt;Record&gt;\n"+
                "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                "&lt;Field name=\"bcaa001\" value=\""+qrCode.trim()+"\"/&gt;\n"+
                "&lt;Field name=\"bcaaua021\" value=\""+areaID.trim()+"\"/&gt;\n"+
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
                "&lt;/Master&gt;\n";
    }

    /**
     *描述: 更新当前标签状态,更新为N
     *日期：2022/6/12
     **/
    private void updQrcodeData(String strAction,String strActionId,String qcstatus,String qrcode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(CheckLabelActivity.this,"数据更新中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名:cwssp022
                String webServiceName = "MaterialCheckInsert";

                //初始化日期时间
                long timeCurrentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
                String currentTime = simpleTimeFormat.format(timeCurrentTimeMillis);
                String currentDate = simpleDateFormat.format(new Date());

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"sffb_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"sffb005\" value=\""+ strProductDocno +"\"/&gt;\n"+  //工单单号
                        "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //批量生产止日期
                        "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //批量生产止时间
                        "&lt;Field name=\"planuser\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //生产人员
                        "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                        "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //工序
                        "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //状态
                        "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+  //二维码
                        "&lt;Field name=\"act\" value=\""+ strAction +"\"/&gt;\n"+  //操作类别
                        "&lt;Field name=\"actcode\" value=\""+ strActionId +"\"/&gt;\n"+  //执行命令ID
                        "&lt;Field name=\"areaid\" value=\""+ txtViewAreaId.getText().toString().trim() +"\"/&gt;\n"+  //区域编号
                        "&lt;Field name=\"connectProduct\" value=\""+ strConnectProduct +"\"/&gt;\n"+  //是否连线生产
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sffyucseq\" value=\"1.0\"/&gt;\n"+
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
                mapResponseList = t100ServiceHelper.getT100ResponseDocno5(strResponse,"docno");

                //写入本地暂存数据库
                if(mapResponseList.size()>0){
                    for (Map<String, Object> mResponse : mapResponseList) {
                        String sQrcode = mResponse.get("Qrcode").toString();
                        String sProductCode = mResponse.get("ProductCode").toString();
                        String sProductName = mResponse.get("ProductName").toString();
                        String sProductModels = mResponse.get("ProductModels").toString();
                        String sProcessId = mResponse.get("ProcessId").toString();
                        String sProcess = mResponse.get("Process").toString();
                        String sQuantity = mResponse.get("CheckQuantity").toString();

                        checkLabelEntity = new CheckLabelEntity(sQrcode,sProductCode,sProductName,sProductModels,sQuantity,sProcessId,sProcess);
                        hzDb.checkLabelDao().insert(checkLabelEntity);
                    }

                }

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
                    MyToast.myShow(CheckLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckLabelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    String sQrcode = "";
                    String sProductCode = "";
                    String sProductName = "";
                    String sProductModels = "";
                    String sProcessId = "";
                    String sProcess = "";
                    String sQuantity = "";

                    if(mapResponseList.size()> 0) {
                        for (Map<String, Object> mResponse : mapResponseList) {
                            sQrcode = mResponse.get("Qrcode").toString();
                            sProductCode = mResponse.get("ProductCode").toString();
                            sProductName = mResponse.get("ProductName").toString();
                            sProductModels = mResponse.get("ProductModels").toString();
                            sProcessId = mResponse.get("ProcessId").toString();
                            sProcess = mResponse.get("Process").toString();
                            sQuantity = mResponse.get("CheckQuantity").toString();

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
                        }

                        //更新值
                        checkLabelProductCode.setText(sProductCode);
                        checkLabelProductName.setText(sProductName);
                        checkLabelProductModel.setText(sProductModels);
                        checkLabelProcessId.setText(sProcessId);
                        checkLabelProcess.setText(sProcess);
                        checkLabelQuantity.setText(sQuantity);
                        checkLabelQrcode.setText(sQrcode);
                        checkLabelQuantityTotal.setText(String.valueOf(iTotal));
                        checkLabelPcsTotal.setText(String.valueOf(iTotalPcs));
                    }
                    MyToast.myShow(CheckLabelActivity.this, "零件:"+sProductName+",标签确认成功", 1, 1);
                }else{
                    MyAlertDialog.myShowAlertDialog(CheckLabelActivity.this,"错误信息",statusDescription);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
     *描述: 生成区域数据
     *日期：2022/12/30
     **/
    private void genAreaData(String qrcode,String strAct){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(CheckLabelActivity.this,"数据更新中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //产生xml数据
                checkLabelEntityList = hzDb.checkLabelDao().getAll();
                if(checkLabelEntityList.size()>0){
                    for(int i=0;i<checkLabelEntityList.size();i++){
                        String strQrCode = checkLabelEntityList.get(i).getQrcode();

                        //依据暂存数据产生xml数据
                        genRecordSetStr(qrcode,strQrCode,strAct,i);
                    }
                }

                //初始化T100服务名
                String webServiceName = "ProductAreaRequestGen";
                String sType = "1";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        mRecordSet+
//                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
//                        "&lt;Record&gt;\n"+
//                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
//                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
//                        "&lt;Field name=\"bcaa001\" value=\""+checkLabelQrcode.getText().toString().trim()+"\"/&gt;\n"+
//                        "&lt;Field name=\"bcaaua021\" value=\""+qrcode.trim()+"\"/&gt;\n"+
//                        "&lt;Field name=\"typecode\" value=\""+sType+"\"/&gt;\n"+
//                        "&lt;Field name=\"act\" value=\""+strAct+"\"/&gt;\n"+
//                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
//                        "&lt;Record&gt;\n"+
//                        "&lt;Field name=\"bcaa000\" value=\"1.0\"/&gt;\n"+
//                        "&lt;/Record&gt;\n"+
//                        "&lt;/Detail&gt;\n"+
//                        "&lt;Memo/&gt;\n"+
//                        "&lt;Attachment count=\"0\"/&gt;\n"+
//                        "&lt;/Record&gt;\n"+
//                        "&lt;/Master&gt;\n"+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonAreaData(strResponse,"areainfo");

                //清除本地库
                if(mapResponseList.size()>0){
                    hzDb.checkLabelDao().deleteLabel();
                }

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
                    MyToast.myShow(CheckLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckLabelActivity.this,e.getLocalizedMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(!statusCode.equals("0")){
                    MyAlertDialog.myShowAlertDialog(CheckLabelActivity.this,"错误信息",statusDescription);
                }else{
                    if(mapResponseList.size()>0){
                        for(Map<String,Object> mData: mapResponseList){
                            String sAreaId = mData.get("AreaId").toString();
                            String sArea = mData.get("Area").toString();

                            txtViewAreaId.setText(sAreaId);
                            txtViewArea.setText(sArea);
                            isArea = false;
                            mRecordSet = "";
                        }
                    }
                    MyToast.myShow(CheckLabelActivity.this,statusDescription,1,0);
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
    private void deleteDbData(){

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                statusCode = "0";

                //清空数据
                hzDb.checkLabelDao().deleteLabel();

                e.onNext(statusCode);
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

            }
        });
    }
}