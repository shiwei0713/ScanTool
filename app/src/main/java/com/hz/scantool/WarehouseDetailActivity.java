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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.IqcCheckResultAdapter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyAlertDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.WarehouseDetailAdapter;
import com.hz.scantool.adapter.WarehouseListAdapter;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.database.QrcodeEntity;
import com.hz.scantool.dialog.LoadListView;
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

public class WarehouseDetailActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private static final int ROWS = 30;

    private String strTitle,strWhere,strDocno,strDeptId,strDept,strStockId,strStock,strType,strFlag;
    private String statusCode,statusDescription;
    private int iRows,iEveryRow,iCount;
    private boolean isLoadMore;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapResponseAreaList;

    private ScrollView viewBasic;
    private TextView warehouseDetailDocno,warehouseDetailDeptTitle,warehouseDetailDeptId,warehouseDetailDept,warehouseDetailStockTitle,warehouseDetailStockId,warehouseDetailStock;
    private TextView warehouseDetailQty,warehouseDetailQtyPcs,warehouseDetailAreaId,warehouseDetailArea;
    private Button btnSave,btnDelete;
    private LoadListView warehouseDetailView;
    private ProgressBar progressBar;
    private LoadingDialog loadingDialog;
    private WarehouseDetailAdapter warehouseDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse_detail);

        //初始化
        initBundle();
        initView();
        initText();
        initListener();

        //初始化数据
        initRows();
        getWarehouseDetailData();
    }

    /**
     *描述: 初始化控件
     *日期：2022/11/3
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.warehouseDetailToolBar);
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
        warehouseDetailDocno = findViewById(R.id.warehouseDetailDocno);
        warehouseDetailDeptTitle = findViewById(R.id.warehouseDetailDeptTitle);
        warehouseDetailDeptId = findViewById(R.id.warehouseDetailDeptId);
        warehouseDetailDept = findViewById(R.id.warehouseDetailDept);
        warehouseDetailStockTitle = findViewById(R.id.warehouseDetailStockTitle);
        warehouseDetailStockId = findViewById(R.id.warehouseDetailStockId);
        warehouseDetailStock = findViewById(R.id.warehouseDetailStock);
        warehouseDetailQty = findViewById(R.id.warehouseDetailQty);
        warehouseDetailQtyPcs = findViewById(R.id.warehouseDetailQtyPcs);
        warehouseDetailAreaId = findViewById(R.id.warehouseDetailAreaId);
        warehouseDetailArea = findViewById(R.id.warehouseDetailArea);
        progressBar = findViewById(R.id.progressBar);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        warehouseDetailView = findViewById(R.id.warehouseDetailView);

    }

    /**
     *描述: 初始化事件
     *日期：2022/11/3
     **/
    private void initListener(){
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
                case R.id.btnSave:
                    warehouseDetailAreaId.setText("");
                    warehouseDetailArea.setText("");
                    break;
                case R.id.btnDelete:

                    break;
            }
        }
    }

    /**
    *描述: 初始化控件值
    *日期：2022/11/14
    **/
    private void initText(){
        warehouseDetailDocno.setText(strDocno);
        warehouseDetailDeptId.setText(strDeptId);
        warehouseDetailDept.setText(strDept);
        warehouseDetailStockId.setText(strStockId);
        warehouseDetailStock.setText(strStock);

        if(strType.equals("8")){
            //领料
            warehouseDetailDeptTitle.setText(R.string.warehous_detail_label1);
            warehouseDetailStockTitle.setText(R.string.warehous_detail_label3);
        }else if(strType.equals("9")){
            //退料
            warehouseDetailDeptTitle.setText(R.string.warehous_detail_label2);
            warehouseDetailStockTitle.setText(R.string.warehous_detail_label4);
        }
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

    /**
     *描述: 初始化传入参数
     *日期：2022/11/3
     **/
    private void initBundle(){

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strDocno = bundle.getString("Docno");
        strDept = bundle.getString("Dept");
        strDeptId = bundle.getString("DeptId");
        strStock = bundle.getString("Stock");
        strStockId = bundle.getString("StockId");
        strType = bundle.getString("Type");
        strFlag = bundle.getString("Flag");
        strTitle = bundle.getString("Title");

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
                IntentIntegrator intentIntegrator = new IntentIntegrator(WarehouseDetailActivity.this);
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
        }
    }

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){
        //解析二维码
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            String sAreaId = warehouseDetailAreaId.getText().toString();
            if(sAreaId.equals("")||sAreaId.isEmpty()){
                getAreaData(qrContent);
            }else{
                updateT100Data(qrContent);
            }
        }
    }

    /**
     *描述: 初始化查询条件
     *日期：2022/11/14
     **/
    private void initCondition(){
        strWhere = " indddocno= '"+strDocno+"'";
    }

    /**
     *描述: 加载更多
     *日期：2022/9/21
     **/
    private class loadMoreItemListener implements LoadListView.OnLoadMoreListener{

        @Override
        public void LoadMore() {
//            subAdapter.notifyDataSetChanged();
//
//            if(isLoadMore){
//                getSubListData("11",strWhere,strFlag);
//            }else{
//                MyToast.myShow(WarehouseDetailActivity.this,"无更多数据加载",2,0);
//            }
        }
    }

    /**
     *描述: 显示领退料清单
     *日期：2022/11/9
     **/
    private void getWarehouseDetailData(){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppWorkOrderListGet";
                String strArg = "6";

                //初始化查询条件
                initCondition();

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strArg +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonWarehouseDetailData(strResponse,"workorder");

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
                }else{
                    MyToast.myShow(WarehouseDetailActivity.this,"无备料数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(WarehouseDetailActivity.this,e.getLocalizedMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                warehouseDetailAdapter = new WarehouseDetailAdapter(mapResponseList,getApplicationContext());
                warehouseDetailView.setAdapter(warehouseDetailAdapter);

                //刷新显示
                showTotal();

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     *描述: 刷新总数显示
     *日期：2022/11/14
     **/
    private void showTotal(){
        //显示汇总数
        float fTotal=0;
        int iTotalPcs = 0 ;
        for(int i=0;i<mapResponseList.size();i++){
            float fQty = Float.parseFloat((String) mapResponseList.get(i).get("Quantity"));
            int iQtyPcs = Integer.parseInt((String)mapResponseList.get(i).get("QuantityPcs"));
            fTotal = fTotal + fQty;
            iTotalPcs = iTotalPcs + iQtyPcs;
        }
        warehouseDetailQty.setText(String.valueOf(fTotal));
        warehouseDetailQtyPcs.setText(String.valueOf(iTotalPcs));
    }

    /**
     *描述: 获取条码数据
     *日期：2022/11/3
     **/
    private void updateT100Data(String qrCode){
        //显示进度条
        loadingDialog = new LoadingDialog(WarehouseDetailActivity.this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "InventoryBillRequestConfirm";
                String strDocno = warehouseDetailDocno.getText().toString();
                String strProg = "cint331";

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
                        "&lt;Field name=\"qrcode\" value=\""+ qrCode +"\"/&gt;\n"+  //条码编号
                        "&lt;Field name=\"areacode\" value=\""+ warehouseDetailAreaId.getText().toString().trim() +"\"/&gt;\n"+  //区域码
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
                    }
                }else{
                    MyToast.myShow(WarehouseDetailActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(WarehouseDetailActivity.this,e.getLocalizedMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(!statusCode.equals("0")) {
                    MyToast.myShow(WarehouseDetailActivity.this, statusDescription, 0, 0);
                }else{
                    getWarehouseDetailData();
                    MyToast.myShow(WarehouseDetailActivity.this, statusDescription, 1, 0);
                }

                loadingDialog.dismiss();
            }
        });
    }

    /**
     *描述: 获取区域信息
     *日期：2022/12/30
     **/
    private void getAreaData(String qrcontent){
        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "StockGet";
                String strwhere = " oocql002='"+qrcontent.substring(0,3)+"'";
                String strType = "8";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseAreaList = t100ServiceHelper.getT100JsonAreaData(strResponse,"areainfo");

                e.onNext(mapResponseStatus);
                e.onNext(mapResponseAreaList);
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
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(WarehouseDetailActivity.this,e.getMessage(),0,0);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseAreaList.size()>0){
                        for(Map<String,Object> mData: mapResponseAreaList){
                            String sAreaId = mData.get("AreaId").toString();
                            String sArea = mData.get("Area").toString();

                            warehouseDetailAreaId.setText(sAreaId);
                            warehouseDetailArea.setText(sArea);
                        }
                    }else{
                        MyAlertDialog.myShowAlertDialog(WarehouseDetailActivity.this,"错误信息",statusDescription);
                    }
                }else{
                    MyAlertDialog.myShowAlertDialog(WarehouseDetailActivity.this,"错误信息",statusDescription);
                }
            }
        });
    }
}