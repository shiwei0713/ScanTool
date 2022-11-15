/**
*文件：SubQualityCheckDetailActivity,2022/6/7
*描述: PQC扫描检验详情
*作者：shiwei
**/
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
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class SubQualityCheckDetailActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle;
    private String strProductName,strPlanDate,strProductCode,strProductModels,strProcessId,strProcess,strDevice,strDocno,strProductDocno,strPlanno;
    private String strQuantity,strBadQuantity,strNgQuantity;
    private String strLots,strVersion,strUnit,strSeq,strSeq1,strStatus,strAttribute,strQrcode;

    private TextView checkDetailProductCode,checkDetailProductName,checkDetailProductModels,checkDetailProcessId,checkDetailProcess,checkDetailLots;
    private TextView checkDetailDocno,checkDetailQuantity,checkDetailUnit,checkDetailAttribute,checkDetailPlanno,checkDetailVersion,checkDetailQrcode;
    private EditText checkDetailNgQuantity,checkDetailBadQuantity;
    private ImageView imageViewResult;
    private Button btncheckDetailCancel,btncheckDetailFinish;

    private String statusCode;
    private String statusDescription;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_quality_check_detail);

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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubQualityCheckDetailActivity.this);
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

    //初始化传入参数
    private void initBundle(){
        strTitle = this.getResources().getString(R.string.master_detail2);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strDocno = bundle.getString("Docno");
        strProductName = bundle.getString("ProductName");
        strPlanDate = bundle.getString("PlanDate");
        strProductCode = bundle.getString("ProductCode");
        strProductModels = bundle.getString("ProductModels");
        strProcessId = bundle.getString("ProcessId");
        strProcess = bundle.getString("Process");
        strDevice = bundle.getString("Device");
        strQuantity = bundle.getString("Quantity");
        strBadQuantity = bundle.getString("BadQuantity");
        strNgQuantity = bundle.getString("NgQuantity");
        strLots = bundle.getString("Lots");
        strVersion = bundle.getString("Version");
        strUnit = bundle.getString("Unit");
        strSeq = bundle.getString("Seq");
        strSeq1 = bundle.getString("Seq1");
        strStatus = bundle.getString("Status");
        strAttribute = bundle.getString("Attribute");
        strPlanno = bundle.getString("Planno");
        strProductDocno = bundle.getString("ProductDocno");
        strQrcode = bundle.getString("qrCode");
        strQrcode = strQrcode.trim();
    }

    //初始化控件
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subQualityCheckDetailToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        checkDetailProductCode = findViewById(R.id.checkDetailProductCode);
        checkDetailProductName = findViewById(R.id.checkDetailProductName);
        checkDetailProductModels = findViewById(R.id.checkDetailProductModels);
        checkDetailProcessId = findViewById(R.id.checkDetailProcessId);
        checkDetailProcess = findViewById(R.id.checkDetailProcess);
        checkDetailLots = findViewById(R.id.checkDetailLots);
        checkDetailDocno = findViewById(R.id.checkDetailDocno);
        checkDetailQuantity = findViewById(R.id.checkDetailQuantity);
        checkDetailUnit = findViewById(R.id.checkDetailUnit);
        checkDetailNgQuantity = findViewById(R.id.checkDetailNgQuantity);
        checkDetailBadQuantity = findViewById(R.id.checkDetailBadQuantity);
        checkDetailAttribute = findViewById(R.id.checkDetailAttribute);
        checkDetailPlanno = findViewById(R.id.checkDetailPlanno);
        checkDetailVersion = findViewById(R.id.checkDetailVersion);
        checkDetailQrcode = findViewById(R.id.checkDetailQrcode);
        btncheckDetailCancel = findViewById(R.id.btncheckDetailCancel);
        btncheckDetailFinish = findViewById(R.id.btncheckDetailFinish);
        imageViewResult = findViewById(R.id.imageViewResult);

        //初始值
        checkDetailProductName.setText(strProductName);
        checkDetailProductCode.setText(strProductCode);
        checkDetailProductModels.setText(strProductModels);
        checkDetailProcessId.setText(strProcessId);
        checkDetailProcess.setText(strProcess);
        checkDetailLots.setText(strLots);
        checkDetailDocno.setText(strDocno);
        checkDetailQuantity.setText(strQuantity);
        checkDetailUnit.setText(strUnit);
        checkDetailAttribute.setText(strAttribute);
        checkDetailQrcode.setText(strQrcode);
        checkDetailPlanno.setText(strPlanno);
        checkDetailVersion.setText(strVersion);

        if(strStatus.equals("K")){
//            checkDetailNgQuantity.setEnabled(false);
//            checkDetailBadQuantity.setEnabled(false);
//            btncheckDetailFinish.setEnabled(false);
//            btncheckDetailCancel.setEnabled(false);
            if(strNgQuantity.equals("")||strNgQuantity.isEmpty()){
                strNgQuantity = "0";
            }
            if(strBadQuantity.equals("")||strBadQuantity.isEmpty()){
                strBadQuantity = "0";
            }
            checkDetailNgQuantity.setText(strNgQuantity);
            checkDetailBadQuantity.setText(strBadQuantity);
            imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ok));
        }else{
//            checkDetailNgQuantity.setEnabled(true);
//            checkDetailBadQuantity.setEnabled(true);
//            btncheckDetailFinish.setEnabled(true);
//            btncheckDetailCancel.setEnabled(true);
            imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_deal));
        }

        btncheckDetailFinish.setOnClickListener(new commandClickListener());
        btncheckDetailCancel.setOnClickListener(new commandClickListener());
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
    private void scanResult(String qrContent,Context context, Intent intent){
        //解析二维码
        String[] qrCodeValue = qrContent.split("_");
        int qrIndex = qrContent.indexOf("_");

        if(qrIndex==-1){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            checkDetailProcessId.setText(qrCodeValue[3].trim());
            checkDetailProcess.setText(qrCodeValue[4].trim());
            checkDetailPlanno.setText(qrCodeValue[0].trim());
            checkDetailVersion.setText(qrCodeValue[1].trim());
        }
    }

    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btncheckDetailFinish: //合格
                    if(checkDocno()){
                        if(checkQty("OK")){
                            updatePqcData("K");
                        }else{
                            MyToast.myShow(SubQualityCheckDetailActivity.this,"合格产品,不良品和废品量应全为0",2,0);
                        }
                    }else{
                        MyToast.myShow(SubQualityCheckDetailActivity.this,"计划信息和工序不可为空",2,0);
                    }
                    break;
                case R.id.btncheckDetailCancel://异常
//                    strErrorLots = txtMultipleErrorCount.getText().toString();
//                    saveMultipleToT100("insert","13","S","");
                    if(checkDocno()){
                        if(checkQty("NG")){
                            updatePqcData("KO");
                        }else{
                            MyToast.myShow(SubQualityCheckDetailActivity.this,"异常产品，不良品和废品量不可为0或不可大于报工量",2,0);
                        }
                    }else{
                        MyToast.myShow(SubQualityCheckDetailActivity.this,"计划信息和工序不可为空",2,0);
                    }
                    break;
            }
        }
    }

    /**
    *描述: 检核单号是否完整
    *日期：2022/6/7
    **/
    private boolean checkDocno(){
        boolean isCheck = true;
        String strProcessId = checkDetailProcessId.getText().toString();
        String strProcess = checkDetailProcess.getText().toString();
        String strPlanno = checkDetailPlanno.getText().toString();
        String strVersion = checkDetailVersion.getText().toString();
        String strAttribute = checkDetailAttribute.getText().toString();

//        if(strAttribute.equals("BL")){
//            if(strProcessId.equals("")||strProcessId.isEmpty()||strProcess.equals("")||strProcess.isEmpty()||strPlanno.equals("")||strPlanno.isEmpty()||strVersion.equals("")||strVersion.isEmpty()){
//                isCheck = false;
//            }
//        }

        return isCheck;
    }

    /**
    *描述: 核对数量是否录入
    *日期：2022/6/7
    **/
    private boolean checkQty(String sFlag){
        boolean isCheck = true;
        String strBadQty = checkDetailBadQuantity.getText().toString();
        String strNgQty = checkDetailNgQuantity.getText().toString();

        if(strBadQty.equals("")||strBadQty.isEmpty()){
            strBadQty = "0";
        }

        if(strNgQty.equals("")||strNgQty.isEmpty()){
            strNgQty = "0";
        }

        int iBadQty = Integer.valueOf(strBadQty);
        int iNgQty = Integer.valueOf(strNgQty);
        int iQuantity = Integer.valueOf(strQuantity);

        if(sFlag.equals("OK")){
            //合格
            if(iNgQty>0 ){
                isCheck = false;
            }
        }else{
            //异常
            if(iNgQty==0 ){
                isCheck = false;
            }else{
                int iQty = iQuantity + iBadQty - iNgQty;
                if(iQty<0){
                    isCheck = false;
                }
            }
        }

        return isCheck;
    }

    private void updatePqcData(String qcstatus){
        //显示进度条
        loadingDialog = new LoadingDialog(SubQualityCheckDetailActivity.this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "WorkReportRequestGen";
                String action = "pqccheck";

                long timeCurrentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
                String currentTime = simpleTimeFormat.format(timeCurrentTimeMillis);
                String currentDate = simpleDateFormat.format(new Date());

                String strBadQty = checkDetailBadQuantity.getText().toString();
                String strNgQty = checkDetailNgQuantity.getText().toString();

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"sffb_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffbdocdt\" value=\""+strPlanDate+"\"/&gt;\n"+
                        "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"sffbdocno\" value=\""+ strDocno +"\"/&gt;\n"+  //报工单号
                        "&lt;Field name=\"sffbseq\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                        "&lt;Field name=\"sffb005\" value=\""+ strProductDocno +"\"/&gt;\n"+  //工单单号
                        "&lt;Field name=\"sffb010\" value=\""+ strDevice +"\"/&gt;\n"+  //机器编号
                        "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //批量生产止日期
                        "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //批量生产止时间
                        "&lt;Field name=\"sffb029\" value=\""+ strProductCode +"\"/&gt;\n"+  //报工料号
                        "&lt;Field name=\"sffb018\" value=\""+ strBadQty +"\"/&gt;\n"+  //报废数量
                        "&lt;Field name=\"sffb019\" value=\""+ strNgQty +"\"/&gt;\n"+  //当站下线数量
                        "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                        "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //工序
                        "&lt;Field name=\"lots\" value=\""+ strLots +"\"/&gt;\n"+  //批次
                        "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //首检状态
                        "&lt;Field name=\"planseq\" value=\""+ strSeq +"\"/&gt;\n"+  //报工次数
                        "&lt;Field name=\"planno\" value=\""+ strPlanno +"\"/&gt;\n"+  //计划单号
                        "&lt;Field name=\"version\" value=\""+ strVersion +"\"/&gt;\n"+  //版本
                        "&lt;Field name=\"qrcode\" value=\""+ strQrcode +"\"/&gt;\n"+  //二维码
                        "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //执行动作
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
                    }
                }else{
                    MyToast.myShow(SubQualityCheckDetailActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubQualityCheckDetailActivity.this,"网络错误",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SubQualityCheckDetailActivity.this, statusDescription, 1, 1);
                    finish();
                }else{
                    MyToast.myShow(SubQualityCheckDetailActivity.this, statusDescription, 0, 1);
                }
                loadingDialog.dismiss();
            }
        });
    }

}