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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubAdapter;
import com.hz.scantool.adapter.SurplusLabelListAdapter;
import com.hz.scantool.dialog.LoadListView;
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

public class SurplusMaterialListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private static final int ROWS = 30;

    private String statusCode;
    private String statusDescription;
    private String strwhere;
    private boolean isLoadMore;
    private int iRows,iEveryRow,iCount;

    private EditText inputProductName,inputQrcode;
    private Button btnQuery,btnClear;
    private LoadListView surplusListView;
    private ProgressBar progressBar;
    private LoadingDialog loadingDialog;
    private SurplusLabelListAdapter surplusLabelListAdapter;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surplus_material_list);

        //初始化
        initRows();
        initView();

        //显示数据
        getSubListData();
    }

    /**
    *描述: 初始化控件
    *日期：2022/10/24
    **/
    private void initView(){
        //定义控件
        inputProductName = findViewById(R.id.inputProductName);
        inputQrcode = findViewById(R.id.inputQrcode);
        btnQuery = findViewById(R.id.btnQuery);
        btnClear = findViewById(R.id.btnClear);
        progressBar = findViewById(R.id.progressBar);
        surplusListView = findViewById(R.id.surplusListView);
        Toolbar toolbar=findViewById(R.id.surplusListToolBar);
        setSupportActionBar(toolbar);

        //初始化
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(getResources().getString(R.string.surplus_title));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //定义事件
        surplusListView.setLoadMoreListener(new loadMoreListener());
        btnQuery.setOnClickListener(new btnClickListener());
        btnClear.setOnClickListener(new btnClickListener());
    }

    /**
     *描述: 初始化显示笔数
     *日期：2022/10/20
     **/
    private void initRows(){
        iRows = ROWS;  //每次加载30行数据
        iEveryRow = ROWS; //每次加载30行数据
        iCount = 0 ; //当前笔数
        isLoadMore = true; // 是否加载更多
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu_refresh,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(SurplusMaterialListActivity.this);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //开始扫描
                intentIntegrator.initiateScan();
                break;
            case R.id.action_refresh:
                initRows();
                getSubListData();
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

    /**
     *描述: 扫描结果解析
     *日期：2022/7/19
     **/
    private void scanResult(String qrContent,Context context, Intent intent){
        inputQrcode.setText(qrContent);
        getSubListData();
    }

    /**
    *描述: 初始化查询条件
    *日期：2022/10/26
    **/
    private void initWhere(){
        strwhere = " 1=1";
        String sProductName = inputProductName.getText().toString();
        String sQrcode = inputQrcode.getText().toString();
        if(!sProductName.equals("")&&!sProductName.isEmpty()){
            strwhere = strwhere+" AND imaal003 LIKE '%"+sProductName.trim()+"%'";
        }

        if(!sQrcode.equals("")&&!sQrcode.isEmpty()){
            strwhere = strwhere+" AND sffyuc014 LIKE '%"+sQrcode.trim().toUpperCase()+"%'";
        }

        //清空当前数据
        if(mapResponseList!=null){
            mapResponseList.clear();
        }
    }

    /**
    *描述: 加载更多数据
    *日期：2022/10/25
    **/
    private class loadMoreListener implements LoadListView.OnLoadMoreListener{

        @Override
        public void LoadMore() {
            if(isLoadMore){
                getSubListData();
            }else{
                MyToast.myShow(SurplusMaterialListActivity.this,"无更多数据加载",2,0);
            }
        }
    }

    /**
    *描述: 按钮单击事件
    *日期：2022/10/26
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnQuery:
                    getSubListData();
                    break;
                case R.id.btnClear:
                    inputQrcode.setText("");
                    inputProductName.setText("");
                    getSubListData();
                    break;
            }
        }
    }

    /**
     *描述: 获取所有任务清单
     *日期：2022/7/19
     **/
    private void getSubListData(){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        //初始化查询条件
        initWhere();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductListGet";
                String strType = "4";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"rows\" value=\""+ iRows +"\"/&gt;\n"+
                        "&lt;Field name=\"user\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonSurplusLabelData(strResponse,"workorder");

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

                        if(statusCode.equals("0")){
                            int progress = progressBar.getProgress();
                            progress = progress + 50;
                            progressBar.setProgress(progress);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SurplusMaterialListActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                String msg = "总数:0,当前:0";
                if(statusCode.equals("0")){
                    if(mapResponseList!=null){
                        //填充清单
                        surplusLabelListAdapter = new SurplusLabelListAdapter(mapResponseList,getApplicationContext(),printClickListener);
                        surplusListView.setAdapter(surplusLabelListAdapter);

                        //显示当前笔数
                        String sCount = (String)mapResponseList.get(0).get("Count");
                        if(sCount.equals("")||sCount.isEmpty()){
                            sCount = "0";
                        }
                        iCount = Integer.parseInt(sCount);
                        if(iCount<=mapResponseList.size()){
                            isLoadMore = false;
                        }else{
                            isLoadMore = true;
                        }

                        //显示加载结果
                        msg = "总数:"+sCount+",当前:"+String.valueOf(mapResponseList.size());

                        //记录条目数
                        iRows = iRows + iEveryRow;

                    }

                }else{
                    MyToast.myShow(SurplusMaterialListActivity.this,statusDescription,0,0);
                }

                surplusListView.setLoadMoreTitle(msg);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
    *描述: 打印余料标签
    *日期：2022/10/25
    **/
    private SurplusLabelListAdapter.PrintClickListener printClickListener = new SurplusLabelListAdapter.PrintClickListener() {
        @Override
        public void PrintOnClick(int position, View v) {
            String strDocno = surplusLabelListAdapter.getItemValue(position,"ProductDocno");
            String strPlanDate = surplusLabelListAdapter.getItemValue(position,"PlanDate");
            String strGroupId = surplusLabelListAdapter.getItemValue(position,"GroupId");
            String strProcessId = surplusLabelListAdapter.getItemValue(position,"ProcessId");
            String strDevice = surplusLabelListAdapter.getItemValue(position,"Device");
            String strProductCode = surplusLabelListAdapter.getItemValue(position,"ProductCode");
            String strQuantity = surplusLabelListAdapter.getItemValue(position,"Quantity");
            String strProcess = surplusLabelListAdapter.getItemValue(position,"Process");
            String strLots = surplusLabelListAdapter.getItemValue(position,"Lots");
            String strPlanno = surplusLabelListAdapter.getItemValue(position,"Docno");
            String strEmployee = surplusLabelListAdapter.getItemValue(position,"Employee");
            String strVersion = surplusLabelListAdapter.getItemValue(position,"Version");
            String qrcode = surplusLabelListAdapter.getItemValue(position,"Qrcode");

            updatePrintData(strPlanDate,strGroupId,strDocno,strProcessId,strDevice,strProductCode,strQuantity,strProcess,strLots,strPlanno,strEmployee,strVersion,qrcode);
        }
    };

    /**
    *描述: 产生余料标签数据
    *日期：2022/10/25
    **/
    private void updatePrintData(String strPlanDate,String strGroupId,String strDocno,String strProcessId,String strDevice,String strProductCode,String strQuantity,String strProcess,String strLots,String strPlanno,String strEmployee,String strVersion,String qrcode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SurplusMaterialListActivity.this,"数据提交中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "WorkReportRequestGen";
                String action = "printmaterial";
                String actionid = "190";

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
                        "&lt;Field name=\"sffb004\" value=\""+ strGroupId +"\"/&gt;\n"+  //班次
                        "&lt;Field name=\"sffb005\" value=\""+ strDocno +"\"/&gt;\n"+  //工单单号
                        "&lt;Field name=\"sffbseq\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                        "&lt;Field name=\"sffb010\" value=\""+ strDevice +"\"/&gt;\n"+  //机器编号
                        "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //批量生产止日期
                        "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //批量生产止时间
                        "&lt;Field name=\"sffb029\" value=\""+ strProductCode +"\"/&gt;\n"+  //报工料号
                        "&lt;Field name=\"sffb017\" value=\""+ strQuantity +"\"/&gt;\n"+  //良品数量
                        "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                        "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //工序
                        "&lt;Field name=\"lots\" value=\""+ strLots +"\"/&gt;\n"+  //批次
                        "&lt;Field name=\"planno\" value=\""+ strPlanno +"\"/&gt;\n"+  //计划单号
                        "&lt;Field name=\"planuser\" value=\""+ strEmployee +"\"/&gt;\n"+  //生产人员
                        "&lt;Field name=\"version\" value=\""+ strVersion +"\"/&gt;\n"+  //版本
                        "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //执行动作
                        "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+  //二维码
                        "&lt;Field name=\"actcode\" value=\""+ actionid +"\"/&gt;\n"+  //执行命令ID
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
                mapResponseList = t100ServiceHelper.getT100ResponseDocno3(strResponse,"docno");

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
                    MyToast.myShow(SurplusMaterialListActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SurplusMaterialListActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    initRows();
                    getSubListData();
                    MyToast.myShow(SurplusMaterialListActivity.this, statusDescription, 1, 1);
                }else{
                    MyToast.myShow(SurplusMaterialListActivity.this, statusDescription, 0, 1);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}