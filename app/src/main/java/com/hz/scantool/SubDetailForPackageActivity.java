/**
*描述: 工序报工-尾数合箱
*日期：2022/10/12
**/
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
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LabelMergeListAdapter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyAlertDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.PrintLabelListAdapter;
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

public class SubDetailForPackageActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private static final int MERGEPACKAGE = 1001;

    private HzDb hzDb;
    private String dataBaseName = "HzDb";
    private String strTitle;
    private String sProductCode,sDocno,sProcessId,sProcess,sProductDocno,sVersion,sPlanSeq,sTypeDesc;
    private int iPlanSeq,iVersion;

    private EditText inputSubPackageQrcode;
    private TextView subPackageProductCode,subPackageDocno,subPackageVersion,subPackageSeq,subPackageProductDocno,subPackageProcessId,subPackageProcess,subPackageTotal;
    private ListView subPackageList;
    private Button btnSubPackageQrcode,btnClear,btnCancel;

    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mLocalList;
    private String statusCode,statusDescription;
    private LabelMergeListAdapter labelMergeListAdapter;
    private MergeLabelEntity mergeLabelEntity;
    private List<MergeLabelEntity> mergeLabelEntityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_for_package);

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
        sTypeDesc = bundle.getString("TypeDesc");
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
        Toolbar toolbar=findViewById(R.id.subDetailPackageToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        strTitle = getResources().getString(R.string.list_detail_button22);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        subPackageProductCode = findViewById(R.id.subPackageProductCode);
        inputSubPackageQrcode = findViewById(R.id.inputSubPackageQrcode);
        subPackageDocno = findViewById(R.id.subPackageDocno);
        subPackageVersion = findViewById(R.id.subPackageVersion);
        subPackageSeq = findViewById(R.id.subPackageSeq);
        subPackageProductDocno = findViewById(R.id.subPackageProductDocno);
        subPackageProcessId = findViewById(R.id.subPackageProcessId);
        subPackageProcess = findViewById(R.id.subPackageProcess);
        subPackageList = findViewById(R.id.subPackageList);
        btnSubPackageQrcode = findViewById(R.id.btnSubPackageQrcode);
        subPackageTotal = findViewById(R.id.subPackageTotal);
        btnClear = findViewById(R.id.btnClear);
        btnCancel = findViewById(R.id.btnCancel);

        //初始化显示值
        subPackageProductCode.setText(sProductCode);
        subPackageProcessId.setText(sProcessId);
        subPackageProcess.setText(sProcess);
        subPackageDocno.setText(sDocno);
        subPackageVersion.setText(sVersion);
        subPackageProductDocno.setText(sProductDocno);
        subPackageSeq.setText(sPlanSeq);

        //定义事件
        btnSubPackageQrcode.setOnClickListener(new btnClickListener());
        btnClear.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
    }

    /**
    *描述: 返回尾数合箱总量至SubDetailForMultipleActivity
    *日期：2022/10/11
    **/
    private void returnData(){
        Intent intent = new Intent();
        String sTotal = subPackageTotal.getText().toString();
        if(sTotal.equals("")||sTotal.isEmpty()){
            sTotal = "0";
        }
        int iTotal = Integer.parseInt(sTotal);
        intent.putExtra("total",iTotal);
        setResult(MERGEPACKAGE,intent);
        SubDetailForPackageActivity.this.finish();
    }

    /**
     *描述: 按钮事件
     *日期：2022/6/12
     **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSubPackageQrcode:
                    String sCode = inputSubPackageQrcode.getText().toString();
                    getQrcodeData(sCode.toUpperCase());
                    break;
                case R.id.btnClear:
                    deleteDbData();
                    break;
                case R.id.btnCancel:
                    returnData();
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailForPackageActivity.this);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //开始扫描
                intentIntegrator.initiateScan();
                break;
            case android.R.id.home:
                returnData();
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
            loadingDialog = new LoadingDialog(SubDetailForPackageActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "ItemInfoGet";
                String strType = "9";
                String strwhere = " bcaa002 IN (";
                String strwhere1 = "";

                int iIndex = sProductCode.indexOf(",");
                if(iIndex>-1){
                    String[] arrayProductCode = sProductCode.split(",");
                    for(int m=0;m<arrayProductCode.length;m++){
                        if(strwhere1.equals("")||strwhere1.isEmpty()){
                            strwhere1 = "'"+arrayProductCode[m]+"'";
                        }else{
                            strwhere1 = strwhere1+",'"+arrayProductCode[m]+"'";
                        }
                    }
                    strwhere = strwhere+strwhere1+")";
                }else {
                    strwhere = " bcaa002 = '"+sProductCode+"'";
                }
                strwhere = strwhere+" AND bcaaud007='"+sProcessId+"'";

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

                        mergeLabelEntity = new MergeLabelEntity(qrcode,productDocno,planDocno,planSeq,version,productCode,productName,processId,process,devices,productUser,quantity,planDate,sTypeDesc,0);
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
                    MyToast.myShow(SubDetailForPackageActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForPackageActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    getDbData();
                }else{
//                    MyToast.myShow(SubDetailForPackageActivity.this,statusDescription,0,0);
//                    ShowAlertDialog.myShow(SubDetailForPackageActivity.this,statusDescription);
                    MyAlertDialog.myShowAlertDialog(SubDetailForPackageActivity.this,"错误信息",statusDescription);
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

                mergeLabelEntityList = hzDb.mergeLabelDao().getAll(sDocno,iVersion,iPlanSeq,sTypeDesc,0);
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
                labelMergeListAdapter = new LabelMergeListAdapter(mLocalList,getApplicationContext());
                subPackageList.setAdapter(labelMergeListAdapter);

                //显示汇总数
                int iTotal=0;
                for(int i=0;i<mLocalList.size();i++){
                    String sQty = (String)mLocalList.get(i).get("Quantity");
                    int iQty = Integer.parseInt(sQty);
                    iTotal = iTotal + iQty;
                }
                subPackageTotal.setText(String.valueOf(iTotal));
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
                iCount = hzDb.mergeLabelDao().getCount(sDocno,iVersion,iPlanSeq,sTypeDesc,0);
                if(iCount>0){
                    //清空数据
                    hzDb.mergeLabelDao().deleteAllMergeLabel(sDocno,iVersion,iPlanSeq,sTypeDesc,0);
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
                MyToast.myShow(SubDetailForPackageActivity.this,statusDescription,0,0);
            }
        });
    }

}