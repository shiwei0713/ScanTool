package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
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

public class SubQualityCheckDetailActivity extends AppCompatActivity {

    private String strTitle;
    private String strProductName="";
    private String strPlanDate="";
    private String strProductCode="";
    private String strProductModels="";
    private String strProcessId="";
    private String strProcess="";
    private String strDevice="";
    private String strDocno="";
    private String strQuantity="";
    private String strBadQuantity="";
    private String strNgQuantity="";
    private String strLots="";
    private String strVersion="";
    private String strSeq="";
    private String strSeq1="";
    private String strStatus="";

    private TextView subQualityCheckDetailProductName;
    private EditText subQualityCheckDetailNgQuantity;
    private EditText subQualityCheckDetailBadQuantity;
    private TextView subDetailProductCode;
    private TextView subDetailProductModels;
    private TextView subDetailProcessId;
    private TextView subDetailProcess;
    private TextView subDetailDevice;
    private TextView subDetailLots;
    private TextView subDetailStartPlanDate;
    private TextView subDetailQuantity;
    private TextView subDetailDocno;
    private TextView subDetailSeq;
    private TextView subDetailSeq1;
    private TextView subDetailVersion;
    private ImageView imageViewResult;
    private Button btnSave;
    private Button btnCancel;

    private String statusCode;
    private String statusDescription;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_quality_check_detail);

        //初始化
        initBundle();
        initView();

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
        strSeq = bundle.getString("Seq");
        strSeq1 = bundle.getString("Seq1");
        strStatus = bundle.getString("Status");
    }

    //初始化控件
    private void initView(){
        subQualityCheckDetailProductName = findViewById(R.id.subQualityCheckDetailProductName);
        subQualityCheckDetailNgQuantity = findViewById(R.id.subQualityCheckDetailNgQuantity);
        subQualityCheckDetailBadQuantity = findViewById(R.id.subQualityCheckDetailBadQuantity);
        subDetailProductCode = findViewById(R.id.subDetailProductCode);
        subDetailProductModels = findViewById(R.id.subDetailProductModels);
        subDetailProcessId = findViewById(R.id.subDetailProcessId);
        subDetailProcess = findViewById(R.id.subDetailProcess);
        subDetailDevice = findViewById(R.id.subDetailDevice);
        subDetailLots = findViewById(R.id.subDetailLots);
        subDetailStartPlanDate = findViewById(R.id.subDetailStartPlanDate);
        subDetailQuantity = findViewById(R.id.subDetailQuantity);
        subDetailDocno = findViewById(R.id.subDetailDocno);
        subDetailSeq = findViewById(R.id.subDetailSeq);
        subDetailSeq1 = findViewById(R.id.subDetailSeq1);
        subDetailVersion = findViewById(R.id.subDetailVersion);
        imageViewResult = findViewById(R.id.imageViewResult);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        subQualityCheckDetailProductName.setText(strProductName);
        subDetailProductCode.setText(strProductCode);
        subDetailProductModels.setText(strProductModels);
        subDetailProcessId.setText(strProcessId);
        subDetailProcess.setText(strProcess);
        subDetailDevice.setText(strDevice);
        subDetailLots.setText(strLots);
        subDetailStartPlanDate.setText(strPlanDate);
        subDetailQuantity.setText(strQuantity);
        subDetailDocno.setText(strDocno);
        subDetailSeq.setText(strSeq);
        subDetailSeq1.setText(strSeq1);
        subDetailVersion.setText(strVersion);

        if(strStatus.equals("K")){
            subQualityCheckDetailNgQuantity.setEnabled(false);
            subQualityCheckDetailBadQuantity.setEnabled(false);
            btnSave.setEnabled(false);
            if(strNgQuantity.equals("")||strNgQuantity.isEmpty()){
                strNgQuantity = "0";
            }
            if(strBadQuantity.equals("")||strBadQuantity.isEmpty()){
                strBadQuantity = "0";
            }
            subQualityCheckDetailNgQuantity.setText(strNgQuantity);
            subQualityCheckDetailBadQuantity.setText(strBadQuantity);
            imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ok));
        }else{
            subQualityCheckDetailNgQuantity.setEnabled(true);
            subQualityCheckDetailBadQuantity.setEnabled(true);
            btnSave.setEnabled(true);
            imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_deal));
        }

        btnSave.setOnClickListener(new commandClickListener());
        btnCancel.setOnClickListener(new commandClickListener());
    }

    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSave: //保存
                    if(checkQty()){
                        updatePqcData();
                    }else{
                        MyToast.myShow(SubQualityCheckDetailActivity.this,"数量不可为0或不可大于标签量",2,0);
                    }
                    break;
                case R.id.btnCancel://取消
                    finish();
                    break;
            }
        }
    }

    private boolean checkQty(){
        boolean isCheck = true;
        String strBadQty=subQualityCheckDetailBadQuantity.getText().toString();
        String strNgQty=subQualityCheckDetailNgQuantity.getText().toString();

        if(strBadQty.equals("")||strBadQty.isEmpty()){
            strBadQty = "0";
        }

        if(strNgQty.equals("")||strNgQty.isEmpty()){
            strNgQty = "0";
        }

        int iBadQty = Integer.valueOf(strBadQty);
        int iNgQty = Integer.valueOf(strNgQty);
        int iQuantity = Integer.valueOf(strQuantity);

        if(iBadQty==0 && iNgQty==0){
            isCheck = true;  //不检核数量为0
        }else{
            int iQty = iQuantity - iBadQty - iNgQty;
            if(iQty<0){
                isCheck = false;
            }
        }

        return isCheck;
    }

    private void updatePqcData(){
        //显示进度条
        loadingDialog = new LoadingDialog(SubQualityCheckDetailActivity.this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "WorkReportRequestGen";
                String qcstatus = "K";
                String action = "pqccheck";

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
                        "&lt;Field name=\"sffbdocdt\" value=\""+strPlanDate+"\"/&gt;\n"+
                        "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"sffbdocno\" value=\""+ strDocno +"\"/&gt;\n"+  //报工单号
                        "&lt;Field name=\"sffbseq\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                        "&lt;Field name=\"sffb010\" value=\""+ strDevice +"\"/&gt;\n"+  //机器编号
                        "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //批量生产止日期
                        "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //批量生产止时间
                        "&lt;Field name=\"sffb029\" value=\""+ strProductCode +"\"/&gt;\n"+  //报工料号
                        "&lt;Field name=\"sffb018\" value=\""+ subQualityCheckDetailBadQuantity.getText().toString() +"\"/&gt;\n"+  //报废数量
                        "&lt;Field name=\"sffb019\" value=\""+ subQualityCheckDetailNgQuantity.getText().toString() +"\"/&gt;\n"+  //当站下线数量
                        "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //工序
                        "&lt;Field name=\"lots\" value=\""+ strLots +"\"/&gt;\n"+  //批次
                        "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //首检状态
                        "&lt;Field name=\"planseq\" value=\""+ strSeq +"\"/&gt;\n"+  //报工次数
                        "&lt;Field name=\"version\" value=\""+ strVersion +"\"/&gt;\n"+  //版本
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
                }else{
                    MyToast.myShow(SubQualityCheckDetailActivity.this, statusDescription, 0, 1);
                }
                loadingDialog.dismiss();
            }
        });
    }
}