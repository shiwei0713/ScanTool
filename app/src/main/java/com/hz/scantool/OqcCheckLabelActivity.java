/**
*文件：OqcCheckLabelActivity,2022/5/28
*描述: 出货标签尾数拆箱合箱后，FQC扫描确认拆分标签和原始标签是否一直，三张标签匹配
*作者：shiwei
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.DeliveryOrderListAdapter;
import com.hz.scantool.adapter.LabelListAdapter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.database.DeliveryOrderEntity;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.database.LabelEntity;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.ArrayList;
import java.util.Iterator;
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

public class OqcCheckLabelActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";  //PDA广播

    private HzDb hzDb;
    private String dataBaseName = "HzDb";
    private String strTitle;
    private TextView txtOqcTotal1,txtOqcTotal2,txtOqcTotal3;
    private Button btnOqcSave,btnOqcCancel;
    private ListView oqcCheckListView1,oqcCheckListView2,oqcCheckListView3;
    private LoadingDialog loadingDialog;
    private String statusCode;
    private String statusDescription;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private List<Map<String,Object>> mapSqlList;
    private LabelListAdapter labelListAdapter1;
    private LabelListAdapter labelListAdapter2;
    private LabelListAdapter labelListAdapter3;
    private LabelEntity labelEntity;
    private List<LabelEntity> labelEntityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oqc_check_label);

        //初始化
        initBundle();
        initDataBase();
        initView();
    }

    /*
     *后台操作，创建数据库
     */
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this,HzDb.class,dataBaseName).build();
    }

    /**
     *描述: 获取传入参数
     *日期：2022/5/28
     **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
    }

    /**
     *描述: 初始化view控件
     *日期：2022/5/28
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.oqcCheckToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        oqcCheckListView1 = findViewById(R.id.oqcCheckListView1);
        oqcCheckListView2 = findViewById(R.id.oqcCheckListView2);
        oqcCheckListView3 = findViewById(R.id.oqcCheckListView3);
        txtOqcTotal1 = findViewById(R.id.txtOqcTotal1);
        txtOqcTotal2 = findViewById(R.id.txtOqcTotal2);
        txtOqcTotal3 = findViewById(R.id.txtOqcTotal3);
        btnOqcSave = findViewById(R.id.btnOqcSave);
        btnOqcCancel = findViewById(R.id.btnOqcCancel);

        //按钮事件
        btnOqcSave.setOnClickListener(new btnClickListener());
        btnOqcCancel.setOnClickListener(new btnClickListener());

    }

    /**
     *描述: 工具栏菜单样式
     *日期：2022/5/28
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu,menu);

        return true;
    }

    /**
     *描述: 工具栏菜单事件
     *日期：2022/5/28
     **/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(OqcCheckLabelActivity.this);
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

    /**
    *描述: 按钮单击事件
    *日期：2022/5/30
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnOqcSave:
                    if(isCheckQty()){
                        updScanCheckData();
                    }else{
                        MyToast.myShow(OqcCheckLabelActivity.this,"数量不匹配",0,0);
                    }
                    break;
                case R.id.btnOqcCancel:
                    clearDoomData();
                    break;
            }
        }
    }

    /**
     *描述: 注册PDA扫码广播
     *日期：2022/5/28
     **/
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


    }

    /**
     *描述: PDA扫描数据接收
     *日期：2022/5/28
     **/
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

    /**
     *描述: 手机调用摄像头扫描数据接收
     *日期：2022/5/28
     **/
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
     *日期：2022/5/28
     **/
    private void scanResult(String qrContent,Context context, Intent intent){
        //解析二维码
        String[] qrCodeValue = qrContent.split("_");
        int qrIndex = qrContent.indexOf("_");
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            getScanCheckData(qrContent);
        }

    }

    /**
     *描述: 扫描标签信息写入本地sqllite并显示
     *日期：2022/5/26
     **/
    private void getScanCheckData(String qrCode){
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据获取中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String,Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "LabelInfoGet";

                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+qrCode+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaa000\" value=\"1.0\"/&gt;\n"+
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
                mapResponseList = t100ServiceHelper.getT100DeliveryData(strResponse,"responsedata");

                if(mapResponseList.size()>0){
                    for(Map<String,Object> mData: mapResponseList){
                        String sDocno = mData.get("Docno").toString();
                        String sProductCode = mData.get("ProductCode").toString();
                        String sProductName = mData.get("ProductName").toString();
                        String sProductModels = mData.get("ProductModels").toString();
                        String sQuantity = mData.get("Quantity").toString();
                        int iQuantity = Integer.parseInt(sQuantity);
//                        String sQuantityPcs = mData.get("QuantityPcs").toString();
//                        int iQuantityPcs = Integer.parseInt(sQuantityPcs);
                        String sTray = mData.get("Tray").toString();
                        String sStatus = "";

                        labelEntity = new LabelEntity(sDocno,sProductCode,sProductName,sProductModels,iQuantity,1,sTray,sStatus);
                        hzDb.labelDao().insert(labelEntity);
                    }
                }

                labelEntityList = hzDb.labelDao().getAll();
                mapSqlList = new ArrayList<Map<String,Object>>();
                for(int i=0;i<labelEntityList.size();i++){
                    mapSqlList.add(labelEntityList.get(i).getListData());
                }

                e.onNext(mapSqlList);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<Map<String,Object>>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<Map<String, Object>> maps) {

            }

            @Override
            public void onError(Throwable e) {
                Log.i("rxjavaerror",e.getMessage());
                MyToast.myShow(OqcCheckLabelActivity.this,e.getMessage(),2,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                refreshLabelData();
                loadingDialog.dismiss();
            }
        });
    }

    /**
    *描述: 刷新显示标签清单
    *日期：2022/5/28
    **/
    private void refreshLabelData(){

        List<Map<String,Object>> mapSqlListA=new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> mapSqlListB=new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> mapSqlListC=new ArrayList<Map<String,Object>>();

        if(mapSqlList.size()>0){
            Iterator<Map<String,Object>> mapIterator = mapSqlList.iterator();
            while (mapIterator.hasNext()){
                //删除非原始标签
                Map<String,Object> map = mapIterator.next();
                String sTray = map.get("Tray").toString();
                if(sTray.equals("A")){
                    mapSqlListA.add(map);
                }else if(sTray.equals("B")){
                    mapSqlListB.add(map);
                }else if(sTray.equals("C")){
                    mapSqlListC.add(map);
                }
            }

            //原始标签清单A
            if(mapSqlListA.size()>0){
                labelListAdapter1 = new LabelListAdapter(mapSqlListA,getApplicationContext());
                oqcCheckListView1.setAdapter(labelListAdapter1);
            }

            //出货标签清单C
            if(mapSqlListC.size()>0){
                labelListAdapter2 = new LabelListAdapter(mapSqlListC,getApplicationContext());
                oqcCheckListView2.setAdapter(labelListAdapter2);
            }

            //回仓标签清单B
            if(mapSqlListB.size()>0){
                labelListAdapter3 = new LabelListAdapter(mapSqlListB,getApplicationContext());
                oqcCheckListView3.setAdapter(labelListAdapter3);
            }

            //刷新总数显示
            refreshTotal();
        }else{
            MyToast.myShow(OqcCheckLabelActivity.this,"无此标签数据",2,0);
        }
    }

    /**
    *描述: 刷新标签总数
    *日期：2022/5/30
    **/
    private void refreshTotal(){
        int iTotal1 = 0;
        int iTotal2 = 0;
        int iTotal3 = 0;

        //原始标签汇总
        if(labelListAdapter1!=null){
            iTotal1 = labelListAdapter1.getTotal("A");
        }

        //出货标签汇总
        if(labelListAdapter2!=null){
            iTotal2 = labelListAdapter2.getTotal("C");
        }

        //回仓标签汇总
        if(labelListAdapter3!=null){
            iTotal3 = labelListAdapter3.getTotal("B");
        }

        txtOqcTotal1.setText(String.valueOf(iTotal1));
        txtOqcTotal2.setText(String.valueOf(iTotal2));
        txtOqcTotal3.setText(String.valueOf(iTotal3));
    }

    /**
    *描述: 检查数量，原始标签=出货标签+回仓标签
    *日期：2022/5/30
    **/
    private boolean isCheckQty(){
        boolean isSuccess;

        String sTotal1 = txtOqcTotal1.getText().toString();
        String sTotal2 = txtOqcTotal2.getText().toString();
        String sTotal3 = txtOqcTotal3.getText().toString();

        int iTotal1 = Integer.parseInt(sTotal1);
        int iTotal2 = Integer.parseInt(sTotal2);
        int iTotal3 = Integer.parseInt(sTotal3);
        int iTotal = iTotal2 + iTotal3;

        if(iTotal1 == iTotal){
            isSuccess = true;
        }else{
            isSuccess = true;
        }

        return isSuccess;

    }

    /**
    *描述: 更新条码数据,写入条码状态为C，用于出货扫码
    *日期：2022/5/30
    **/
    private void updScanCheckData(){
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String,Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "LabelInfoGet";
                String sActioncode = "update";

                //获取本地数据
                String requestRecord = "";
                labelEntityList = hzDb.labelDao().getAll();
                for(int i=0;i<labelEntityList.size();i++){
                    requestRecord = requestRecord + "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                            "&lt;Record&gt;\n"+
                            "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                            "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                            "&lt;Field name=\"qrcode\" value=\""+labelEntityList.get(i).getDocNo()+"\"/&gt;\n"+
                            "&lt;Field name=\"bcaamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                            "&lt;Field name=\"actioncode\" value=\""+ sActioncode +"\"/&gt;\n"+
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

                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        requestRecord+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n";
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

                //删除本地数据
                for(Map<String,Object> mStatus: mapResponseStatus){
                    statusCode = mStatus.get("statusCode").toString();
                    if(statusCode.equals("0")){
                        hzDb.labelDao().deleteLabel();
                    }
                }

                //刷新数据
                labelEntityList = hzDb.labelDao().getAll();
                mapSqlList.clear();
                for(int i=0;i<labelEntityList.size();i++){
                    mapSqlList.add(labelEntityList.get(i).getListData());
                }

                e.onNext(mapResponseStatus);
                e.onNext(mapSqlList);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<Map<String,Object>>>() {
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
                    MyToast.myShow(OqcCheckLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.i("rxjavaerror",e.getMessage());
                MyToast.myShow(OqcCheckLabelActivity.this,e.getMessage(),2,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    refreshLabelData();
                    MyToast.myShow(OqcCheckLabelActivity.this,statusDescription,1,0);
                }

                loadingDialog.dismiss();
            }
        });
    }

    /**
    *描述: 取消标签确认，清除本地sqllite数据
    *日期：2022/5/30
    **/
    private void clearDoomData(){
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据清除中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                //删除本地数据
                hzDb.labelDao().deleteLabel();

                //刷新数据
                labelEntityList = hzDb.labelDao().getAll();
                String sFlag = "N";
                if(labelEntityList.size()>0){
                    sFlag = "Y";
                }

                e.onNext(sFlag);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                if(s.equals("N")){
                    finish();
                }else{
                    MyToast.myShow(OqcCheckLabelActivity.this,"删除数据失败",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.i("rxjavaerror",e.getMessage());
                MyToast.myShow(OqcCheckLabelActivity.this,e.getMessage(),2,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                loadingDialog.dismiss();
            }
        });
    }
}