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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubListDetailAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

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

public class SubDetailListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private ListView listDetailView;
    private TextView txtSubDetailDocnoTitle;
    private TextView txtSubDetailDocno;
    private TextView txtSubDetailStockTitle;
    private TextView txtSubDetailStockId;
    private TextView txtSubDetailStock;
    private TextView txtSubDetailDeptTitle;
    private TextView txtSubDetailDeptId;
    private TextView txtSubDetailDept;
    private TextView txtSubDetailPlanDate;
    private TextView txtSubDetailPosition;
    private ImageView imageViewSubDetailLogo;
    private ProgressBar progressSubDetailBar;

    private String strWhere;
    private String strDocType;
    private String strIndex;
    private String statusCode;
    private String statusDescription;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private List<Map<String,Object>> mapResponseScanList;
    private SubListDetailAdapter subListDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_list);

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subDetailListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(getResources().getString(R.string.master_action5));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化
        initView();
        setTitle();
        initQueryCondition();

        //显示储位清单
        getSubDetailListData();
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailListActivity.this);
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

    //PDA扫描广播注册
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

    //手机扫描
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

    //初始化控件
    private void initView(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        txtSubDetailDocnoTitle = findViewById(R.id.txtSubDetailDocnoTitle);
        txtSubDetailDocno = findViewById(R.id.txtSubDetailDocno);
        txtSubDetailStockTitle = findViewById(R.id.txtSubDetailStockTitle);
        txtSubDetailStockId = findViewById(R.id.txtSubDetailStockId);
        txtSubDetailStock = findViewById(R.id.txtSubDetailStock);
        txtSubDetailDeptTitle = findViewById(R.id.txtSubDetailDeptTitle);
        txtSubDetailDeptId = findViewById(R.id.txtSubDetailDeptId);
        txtSubDetailDept = findViewById(R.id.txtSubDetailDept);
        txtSubDetailPlanDate = findViewById(R.id.txtSubDetailPlanDate);
        txtSubDetailPosition = findViewById(R.id.txtSubDetailPosition);
        listDetailView = findViewById(R.id.subDetailListView);
        progressSubDetailBar = findViewById(R.id.progressSubDetailBar);
        imageViewSubDetailLogo = findViewById(R.id.imageViewSubDetailLogo);

        txtSubDetailStockId.setText(bundle.getString("StockId"));
        txtSubDetailStock.setText(bundle.getString("Stock"));
        txtSubDetailDeptId.setText(bundle.getString("DeptId"));
        txtSubDetailDept.setText(bundle.getString("Dept"));
        txtSubDetailPlanDate.setText(bundle.getString("PlanDate"));
        txtSubDetailDocno.setText(bundle.getString("Docno"));
        strDocType = bundle.getString("DocType");
        strIndex = bundle.getString("Type");

        if(strDocType.equals("1")){
            imageViewSubDetailLogo.setImageDrawable(getResources().getDrawable(R.drawable.sub_detail_inside));
        }else{
            imageViewSubDetailLogo.setImageDrawable(getResources().getDrawable(R.drawable.sub_detail_outside));
        }
    }

    private void setTitle(){
        txtSubDetailDocnoTitle.setVisibility(View.GONE);
        txtSubDetailDocno.setVisibility(View.GONE);

        if(strIndex.equals("2")){
            //完工入库
            txtSubDetailStockTitle.setText(getString(R.string.sub_detail_list_stock2));
            txtSubDetailDeptTitle.setText(getString(R.string.sub_detail_list_dept2));
            txtSubDetailDocnoTitle.setVisibility(View.VISIBLE);
            txtSubDetailDocno.setVisibility(View.VISIBLE);
        }else if(strIndex.equals("3")){
            //采购入库
            txtSubDetailStockTitle.setText(getString(R.string.sub_detail_list_stock3));
            txtSubDetailDeptTitle.setText(getString(R.string.sub_detail_list_dept3));
            txtSubDetailDocnoTitle.setVisibility(View.VISIBLE);
            txtSubDetailDocno.setVisibility(View.VISIBLE);
        }else if(strIndex.equals("41")){
            //生产退货
            txtSubDetailStockTitle.setText(getString(R.string.sub_detail_list_stock41));
            txtSubDetailDeptTitle.setText(getString(R.string.sub_detail_list_dept41));
        }
    }

    //初始化查询条件
    private void initQueryCondition(){
        if(strIndex.equals("2")){
            //完工入库
        }else if(strIndex.equals("3")){
            //采购入库
            strWhere = " pmdudocno = '"+txtSubDetailDocno.getText().toString()+"'";
        }else if(strIndex.equals("4")){
            //生产备货
            strWhere = " sfaa017 = '"+txtSubDetailDeptId.getText().toString()+"' AND sfaa019 = to_date('"+txtSubDetailPlanDate.getText().toString()+"','YYYY-MM-DD')";
        }else if(strIndex.equals("41")){
            //生产退货
            strWhere = " indd032 = '"+txtSubDetailStockId.getText().toString()+"' AND indcdocdt = to_date('"+txtSubDetailPlanDate.getText().toString()+"','YYYY-MM-DD')";
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
            getScanQrData(qrCodeValue[0]);
        }
    }

    //获取发料储位明细
    private void getSubDetailListData(){
        //显示进度条
        progressSubDetailBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppGetStockLot";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strIndex +"\"/&gt;\n"+
                        "&lt;Field name=\"stock\" value=\""+txtSubDetailStockId.getText().toString()+"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseList = t100ServiceHelper.getT100JsonStockData(strResponse,"productlist");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

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
                            MyToast.myShow(SubDetailListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = progressSubDetailBar.getProgress();
                            progress = progress + 50;
                            progressSubDetailBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailListActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailListActivity.this,"网络错误",0,0);
            }

            @Override
            public void onComplete() {
                subListDetailAdapter = new SubListDetailAdapter(mapResponseList,getApplicationContext());
                listDetailView.setAdapter(subListDetailAdapter);

                //隐藏滚动条
                progressSubDetailBar.setVisibility(View.GONE);
            }
        });
    }

    //获取扫描条码信息
    private void getScanQrData(String qrCode){
        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "GetQrCode";
                String qrStatus = "Y";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaa011\" value=\""+qrCode+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;Field name=\"bcaa016\" value=\""+qrStatus+"\"/&gt;\n"+
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
                mapResponseScanList = t100ServiceHelper.getT100JsonQrCodeData(strResponse,"qrcode");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

                e.onNext(mapResponseStatus);
                e.onNext(mapResponseScanList);
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
                            MyToast.myShow(SubDetailListActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailListActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailListActivity.this,"网络错误",0,0);
            }

            @Override
            public void onComplete() {
                if(mapResponseScanList.size()>0){
                    boolean isSuccess = false;
                    for(Map<String,Object> mScanData: mapResponseScanList){
                        String sProductCode = mScanData.get("ProductCode").toString();
                        String sStockId = mScanData.get("StockId").toString();
                        String sStockLocationId = mScanData.get("StockLocationId").toString();
                        String sQuantity = mScanData.get("Quantity").toString();

                        int index = 0;
                        for(Map<String,Object> mData: mapResponseList){
                            String mProductCode = mData.get("ProductCode").toString();
                            String mStockLocationId = mData.get("StockLocationId").toString();
                            if(sProductCode.equals(mProductCode) && sStockLocationId.equals(mStockLocationId)){
                                isSuccess = true;
                                mData.put("ScanQuantity",sQuantity);
                                mData.put("ScanQuantityPcs","1");
                                String strStatus = subListDetailAdapter.updateData(index,listDetailView);
                                if(strStatus.equals("S")){
                                    MyToast.myShow(SubDetailListActivity.this,"扫描成功"+mData.get("ProductCode"),0,0);
                                }else{
                                    if(strStatus.equals("X")){
                                        MyToast.myShow(SubDetailListActivity.this,"扫描数量不可大于备货数量",0,0);
                                    }
                                }

                                break;
                            }

                            index += 1;
                        }
                    }

                    if(!isSuccess){
                        MyToast.myShow(SubDetailListActivity.this,"标签错误，本次备货清单中无此零件",0,0);
                    }
                }
            }
        });
    }
}