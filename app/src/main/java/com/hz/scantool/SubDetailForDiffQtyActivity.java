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
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.DiffQtyListAdapter;
import com.hz.scantool.adapter.LabelMergeListAdapter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.database.MergeLabelEntity;
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

public class SubDetailForDiffQtyActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private static final int DIFFQTY = 1002;

    private HzDb hzDb;
    private String dataBaseName = "HzDb";
    private String strTitle,strBtnTitle;
    private String sProductCode,sDocno,sProcessId,sProcess,sProductDocno,sVersion,sPlanSeq,sTypeDesc;
    private int iPlanSeq,iVersion,iType,iTotal;

    private EditText inputSubDiffQtyQrcode;
    private TextView subDiffQtyProductCode,subDiffQtyDocno,subDiffQtyVersion,subDiffQtySeq,subDiffQtyProductDocno,subDiffQtyProcessId,subDiffQtyProcess,subDiffQtyTotal;
    private ListView subDiffQtyList;
    private Button btnSubDiffQtyQrcode,btnClear,btnSave;

    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mLocalList;
    private String statusCode,statusDescription;
    private DiffQtyListAdapter diffQtyListAdapter;
    private MergeLabelEntity mergeLabelEntity;
    private List<MergeLabelEntity> mergeLabelEntityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_for_diff_qty);

        //初始化
        initBundle();
        initView();
        initDataBase();

        //显示数据
        getDbData();
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
            getQrcodeData(qrContent);
        }
    }

    /**
     *描述: 获取传入参数值
     *日期：2022/6/6
     **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        sDocno = bundle.getString("Docno");
        sVersion = bundle.getString("Version");
        sProcessId = bundle.getString("ProcessId");
        sProcess = bundle.getString("Process");
        sProductCode = bundle.getString("ProductCode");
        sProductDocno = bundle.getString("ProductDocno");
        sPlanSeq = bundle.getString("PlanSeq");
        iType = bundle.getInt("Type");
        sTypeDesc = bundle.getString("TypeDesc");
        strBtnTitle = bundle.getString("BtnTitle");
        if(sPlanSeq.equals("")||sPlanSeq.isEmpty()){
            sPlanSeq = "0";
        }
        if(sVersion.equals("")||sVersion.isEmpty()){
            sVersion = "0";
        }

        iPlanSeq = Integer.parseInt(sPlanSeq);
        iVersion = Integer.parseInt(sVersion);
    }

    /**
     *描述: 初始化控件
     *日期：2022/6/6
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subDiffQtyToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        strTitle = getResources().getString(R.string.sub_diff_qty_title);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle+"-"+strBtnTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        subDiffQtyProductCode = findViewById(R.id.subDiffQtyProductCode);
        inputSubDiffQtyQrcode = findViewById(R.id.inputSubDiffQtyQrcode);
        subDiffQtyDocno = findViewById(R.id.subDiffQtyDocno);
        subDiffQtyVersion = findViewById(R.id.subDiffQtyVersion);
        subDiffQtySeq = findViewById(R.id.subDiffQtySeq);
        subDiffQtyProductDocno = findViewById(R.id.subDiffQtyProductDocno);
        subDiffQtyProcessId = findViewById(R.id.subDiffQtyProcessId);
        subDiffQtyProcess = findViewById(R.id.subDiffQtyProcess);
        subDiffQtyList = findViewById(R.id.subDiffQtyList);
        btnSubDiffQtyQrcode = findViewById(R.id.btnSubDiffQtyQrcode);
        subDiffQtyTotal = findViewById(R.id.subDiffQtyTotal);
        btnClear = findViewById(R.id.btnClear);
        btnSave = findViewById(R.id.btnSave);

        //初始化显示值
        subDiffQtyProductCode.setText(sProductCode);
        subDiffQtyProcessId.setText(sProcessId);
        subDiffQtyProcess.setText(sProcess);
        subDiffQtyDocno.setText(sDocno);
        subDiffQtyVersion.setText(sVersion);
        subDiffQtyProductDocno.setText(sProductDocno);
        subDiffQtySeq.setText(sPlanSeq);

        //定义事件
        btnSubDiffQtyQrcode.setOnClickListener(new btnClickListener());
        btnClear.setOnClickListener(new btnClickListener());
        btnSave.setOnClickListener(new btnClickListener());
    }

    /**
     *描述: 返回尾数合箱总量至SubDetailForMultipleActivity
     *日期：2022/10/11
     **/
    private void returnData(){
        Intent intent = new Intent();
        intent.putExtra("total",iTotal);
        setResult(DIFFQTY,intent);
        SubDetailForDiffQtyActivity.this.finish();
    }

    /**
     *描述: 按钮事件
     *日期：2022/6/12
     **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSubDiffQtyQrcode:
                    String sCode = inputSubDiffQtyQrcode.getText().toString();
                    getQrcodeData(sCode.toUpperCase());
                    break;
                case R.id.btnClear:
                    deleteDbData();
                    break;
                case R.id.btnSave:
                    updateDbData();
                    break;
            }
        }
    }

    /**
     *描述: 初始化数据库
     *日期：2022/6/10
     **/
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this,HzDb.class,dataBaseName).build();
    }

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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailForDiffQtyActivity.this);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //开始扫描
                intentIntegrator.initiateScan();
                break;
            case android.R.id.home:
                updateDbData();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *描述: 获取二维码信息
     *日期：2022/6/15
     **/
    private void getQrcodeData(String qrcode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForDiffQtyActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "ItemInfoGet";
                String strType = "10";
                String strwhere = " sfbadocno IN (";
                String strwhere1 = "";

                String strBomWhere = " bmba001 IN (";
                String strBomWhere1 = "";

                //筛选条件-工单
                int iIndex = sProductDocno.indexOf(",");
                if(iIndex>-1){
                    String[] arrayProductDocno = sProductDocno.split(",");
                    for(int m=0;m<arrayProductDocno.length;m++){
                        if(strwhere1.equals("")||strwhere1.isEmpty()){
                            strwhere1 = "'"+arrayProductDocno[m]+"'";
                        }else{
                            strwhere1 = strwhere1+",'"+arrayProductDocno[m]+"'";
                        }
                    }
                    strwhere = strwhere+strwhere1+")";
                }else {
                    strwhere = " sfbadocno = '"+sProductDocno+"'";
                }

                //筛选条件-零件
                int iIndex2 = sProductCode.indexOf(",");
                if(iIndex2>-1){
                    String[] arrayProductCode = sProductCode.split(",");
                    for(int m=0;m<arrayProductCode.length;m++){
                        if(strBomWhere1.equals("")||strBomWhere1.isEmpty()){
                            strBomWhere1 = "'"+arrayProductCode[m]+"'";
                        }else{
                            strBomWhere1 = strBomWhere1+",'"+arrayProductCode[m]+"'";
                        }
                    }
                    strBomWhere = strwhere+strwhere1+")";
                }else {
                    strBomWhere = " bmba001 = '"+sProductCode+"'";
                }

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"gwhere\" value=\""+ strBomWhere1 +"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonItemQrcodeData2(strResponse,"iteminfo");

                //写入本地数据库
                if(mapResponseList.size()>0){
                    for(int i=0;i<mapResponseList.size();i++){
                        String qrcode = (String)mapResponseList.get(i).get("Qrcode");
                        String productDocno = sProductDocno;
                        String planDocno = sDocno;
                        int planSeq = iPlanSeq;
                        int version = iVersion;
                        String productCode = (String)mapResponseList.get(i).get("ProductCode");
                        String productName = (String)mapResponseList.get(i).get("ProductName");
                        String processId = (String)mapResponseList.get(i).get("ProcessId");
                        String process = (String)mapResponseList.get(i).get("Process");
                        String devices = (String)mapResponseList.get(i).get("Device");
                        String productUser = (String)mapResponseList.get(i).get("Employee");
                        String quantity = (String)mapResponseList.get(i).get("Quantity");
                        String planDate = (String)mapResponseList.get(i).get("PlanDate");

                        mergeLabelEntity = new MergeLabelEntity(qrcode,productDocno,planDocno,planSeq,version,productCode,productName,processId,process,devices,productUser,quantity,planDate,sTypeDesc,iType);
                        hzDb.mergeLabelDao().insert(mergeLabelEntity);
                    }
                }

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
                    }
                }else{
                    MyToast.myShow(SubDetailForDiffQtyActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForDiffQtyActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    getDbData();
                }else{
//                    MyToast.myShow(SubDetailForPackageActivity.this,statusDescription,0,0);
                    ShowAlertDialog.myShow(SubDetailForDiffQtyActivity.this,statusDescription);
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

                mergeLabelEntityList = hzDb.mergeLabelDao().getAll(sDocno,iVersion,iPlanSeq,sTypeDesc,iType);
                for(int j=0;j<mergeLabelEntityList.size();j++){
                    mLocalList.add(mergeLabelEntityList.get(j).getMergeLabelListData());
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

            }

            @Override
            public void onComplete() {
                //显示清单
                diffQtyListAdapter = new DiffQtyListAdapter(mLocalList,getApplicationContext());
                subDiffQtyList.setAdapter(diffQtyListAdapter);

                //显示汇总数
                iTotal=0;
                for(int i=0;i<mLocalList.size();i++){
                    String sQty = (String)mLocalList.get(i).get("Quantity");
                    int iQty = Integer.parseInt(sQty);
                    iTotal = iTotal + iQty;
                }
                subDiffQtyTotal.setText(String.valueOf(iTotal));
            }
        });
    }

    /**
     *描述: 更新本地数据
     *日期：2022/6/10
     **/
    private void updateDbData(){

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                statusCode = "0";
                statusDescription = "无来料差数据";
                iTotal = 0;

                int iCnt = diffQtyListAdapter.getCount();
                for(int i= 0;i<iCnt;i++) {
                    LinearLayout linearLayout = (LinearLayout) subDiffQtyList.getAdapter().getView(i, null, null);
                    TextView listDiffQtyQrcode = (TextView) linearLayout.findViewById(R.id.listDiffQtyQrcode);

                    String sQrcode = listDiffQtyQrcode.getText().toString();
                    String sQuantity = diffQtyListAdapter.getQuantity(i);
                    if(sQuantity.equals("")||sQuantity.isEmpty()){
                        sQuantity = "0";
                    }
                    int iQuantity = Integer.parseInt(sQuantity);
                    iTotal = iTotal + iQuantity;

                    if(iQuantity==0){
                        int j = i+1;
                        statusCode = "-1";
                        statusDescription = "第"+j+"条数据未输入差异量";
                    }

                    //更新差异数
                    if(statusCode.equals("0")){
                        hzDb.mergeLabelDao().update(sQuantity,sQrcode);
                        statusDescription = "保存成功:"+String.valueOf(iCnt)+"条数据";
                    }
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
                MyToast.myShow(SubDetailForDiffQtyActivity.this,e.getMessage(),0,0);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SubDetailForDiffQtyActivity.this,statusDescription,1,0);
                    returnData();
                }else{
                    MyToast.myShow(SubDetailForDiffQtyActivity.this,statusDescription,0,0);
                }
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

                int iCount = 0;
                statusCode = "0";

                //检查是否存在数据
                iCount = hzDb.mergeLabelDao().getCount(sDocno,iVersion,iPlanSeq,sTypeDesc,iType);
                if(iCount>0){
                    //清空数据
                    hzDb.mergeLabelDao().deleteAllMergeLabel(sDocno,iVersion,iPlanSeq,sTypeDesc,iType);
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
                MyToast.myShow(SubDetailForDiffQtyActivity.this,statusDescription,0,0);
            }
        });
    }
}