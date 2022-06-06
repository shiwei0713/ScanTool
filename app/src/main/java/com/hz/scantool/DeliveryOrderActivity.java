/**
*文件：DeliveryOrderActivity,2022/5/25
*描述: 1、备货完成清单显示
 *2、扫描显示指定备货单数据,并下载至本地sqllite数据库
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.DeliveryOrderListAdapter;
import com.hz.scantool.adapter.DetailListItemAdapter;
import com.hz.scantool.adapter.LabelListAdapter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubListAdapter;
import com.hz.scantool.database.DeliveryOrderEntity;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
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

public class DeliveryOrderActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";  //PDA广播

    private HzDb hzDb;
    private String dataBaseName = "HzDb";
    private String strTitle;
    private int actionId;
    private TextView txtDeliveryOrderDocno,txtDeliveryOrderFlag,txtDeliveryOrderQuantity,txtDeliveryOrderQuantityCurrent,txtDeliveryOrderQuantityPcs,txtDeliveryOrderSaler;
    private Button btnSubmit,btnRefresh;
    private ListView deliveryOrderListView;
    private ImageView imgDeliveryOrderResultIcon;
    private LoadingDialog loadingDialog;
    private String statusCode;
    private String statusDescription;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private List<Map<String,Object>> mapSqlList;
    private DeliveryOrderListAdapter deliveryOrderListAdapter;
    private DeliveryOrderEntity deliveryOrderEntity = null;
    private List<DeliveryOrderEntity> deliveryOrderEntityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_order);

        //初始化
        initBundle();
        initView();
        initDataBase();

    }

    /*
     *后台操作，创建数据库
     */
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this,HzDb.class,dataBaseName).build();
    }

    /**
    *描述: 获取传入参数
    *日期：2022/5/25
    **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        actionId = bundle.getInt("btnId");
        strTitle = bundle.getString("title");
    }

    /**
    *描述: 初始化view控件
    *日期：2022/5/25
    **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.deliveryOrderToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        txtDeliveryOrderDocno = findViewById(R.id.txtDeliveryOrderDocno);
        txtDeliveryOrderFlag = findViewById(R.id.txtDeliveryOrderFlag);
        txtDeliveryOrderQuantity = findViewById(R.id.txtDeliveryOrderQuantity);
        txtDeliveryOrderQuantityCurrent = findViewById(R.id.txtDeliveryOrderQuantityCurrent);
        txtDeliveryOrderQuantityPcs = findViewById(R.id.txtDeliveryOrderQuantityPcs);
        txtDeliveryOrderSaler = findViewById(R.id.txtDeliveryOrderSaler);
        deliveryOrderListView = findViewById(R.id.deliveryOrderListView);
        imgDeliveryOrderResultIcon = findViewById(R.id.imgDeliveryOrderResultIcon);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnRefresh = findViewById(R.id.btnSubmit);

        //定义事件
        btnSubmit.setOnClickListener(new btnClickListener());
        btnRefresh.setOnClickListener(new btnClickListener());

        //初始化
        txtDeliveryOrderFlag.setText("N");
        imgDeliveryOrderResultIcon.setVisibility(View.GONE);
    }

    /**
    *描述: 按钮事件实现
    *日期：2022/5/25
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSubmit: //保存
                    if(isSuccessCommit()){
                        updateQcData();
                    }else{
                        MyToast.myShow(DeliveryOrderActivity.this,"未完成三点照合，不可保存",0,0);
                    }
                    break;
                case R.id.btnRefresh: //刷新
                    getDetailListData(txtDeliveryOrderDocno.getText().toString());
                    break;
            }
        }
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(DeliveryOrderActivity.this);
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
    *描述: 注册PDA扫码广播
    *日期：2022/5/25
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

        getDetailListData(txtDeliveryOrderDocno.getText().toString());
    }

    /**
    *描述: PDA扫描数据接收
    *日期：2022/5/25
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
    *日期：2022/5/25
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
    *日期：2022/5/25
    **/
    private void scanResult(String qrContent,Context context, Intent intent){
        //解析二维码
        String[] qrCodeValue = qrContent.split("_");
        int qrIndex = qrContent.indexOf("_");
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            String sFlag = txtDeliveryOrderFlag.getText().toString();
            if(sFlag.equals("N")){
                //备货单清单
                getDetailListData(qrCodeValue[0].trim());
            }else{
                //零件信息核对
                getScanCheckData(qrContent);
            }
        }

    }
    
    /**
    *描述: 检核是否全部三点照合
    *日期：2022/6/1
    **/
    private boolean isSuccessCommit(){
        boolean isSuccess = false;
        String sQuantityCurrent = txtDeliveryOrderQuantityCurrent.getText().toString();
        String SQuantityPcs = txtDeliveryOrderQuantityPcs.getText().toString();

        try{
            int iQuantityCurrent = Integer.parseInt(sQuantityCurrent);
            int iQuantityPcs = Integer.parseInt(SQuantityPcs);
            if(iQuantityCurrent == iQuantityPcs){
                isSuccess = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isSuccess;
    }

    /**
    *描述: 刷新数量
    *日期：2022/5/25
    **/
    private void refreshData(List<Map<String,Object>> mList){
        int iQuantity = 0;
        int iQuantityPcs = 0;
        int iTotal = 0;
        int iTotalPcs = 0;
        int iSalePcs = 0;

        for(Map<String,Object> mData: mList){
            iQuantity = (int)mData.get("Quantity");
            iQuantityPcs = (int)mData.get("QuantityPcs");
            iTotal = iTotal + iQuantity;
            iTotalPcs = iTotalPcs + iQuantityPcs;

            String sStatus = (String)mData.get("Status");
            if(sStatus.equals("C")){
                iSalePcs = iSalePcs + iQuantityPcs;
            }
        }

        //刷新显示
        txtDeliveryOrderQuantityCurrent.setText(String.valueOf(iSalePcs)); //备货数
        txtDeliveryOrderQuantityPcs.setText(String.valueOf(iTotalPcs));  //总箱数
        txtDeliveryOrderQuantity.setText(String.valueOf(iTotal));  //总出货量
    }

    /**
    *描述: 刷新清单，隐藏合格项
    *日期：2022/5/31
    **/
    private void refreshList(List<Map<String,Object>> mList){
        if(mapSqlList.size()>0){
            Iterator<Map<String,Object>> mapIterator = mapSqlList.iterator();
            while (mapIterator.hasNext()){
                //删除非原始标签
                Map<String,Object> map = mapIterator.next();
                String sStatus = map.get("Status").toString();
                if(sStatus.equals("C")){
                    mapIterator.remove();
                }
            }
        }
    }

    /**
    *描述: 从sqllite数据库获取备货单数据
    *日期：2022/5/25
    **/
    public void getDetailListData(String strDocno) {
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据获取中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //检查本地库
                deliveryOrderEntityList = hzDb.deliveryOrderDao().getOrderDoc(strDocno);
                if(deliveryOrderEntityList.size()==0){
                    //初始化T100服务名
                    String webServiceName = "AppGetStockLot";
                    int iIndex = 140;

                    T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                    String requestBody = "&lt;Parameter&gt;\n"+
                            "&lt;Record&gt;\n"+
                            "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                            "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                            "&lt;Field name=\"type\" value=\""+iIndex+"\"/&gt;\n"+
                            "&lt;Field name=\"qrcode\" value=\""+strDocno+"\"/&gt;\n"+
                            "&lt;/Record&gt;\n"+
                            "&lt;/Parameter&gt;\n"+
                            "&lt;Document/&gt;\n";
                    String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                    mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                    mapResponseList = t100ServiceHelper.getT100JsonOqcDetailData(strResponse,"stocklist");

                    if(mapResponseList.size()>0){
                        int seq = 1;
                        for(Map<String,Object> mData: mapResponseList){
                            String sDocno = mData.get("Docno").toString();
                            String sSalerId = mData.get("SalerId").toString();
                            String sSaler = mData.get("Saler").toString();
                            String sCodeRule = mData.get("CodeRule").toString();
                            String sProductCode = mData.get("ProductCode").toString();
                            String sProductName = mData.get("ProductName").toString();
                            String sProductModels = mData.get("ProductModels").toString();
                            String sQuantity = mData.get("Quantity").toString();
                            int iQuantity = Integer.parseInt(sQuantity);
                            String sQuantityPcs = mData.get("QuantityPcs").toString();
                            int iQuantityPcs = Integer.parseInt(sQuantityPcs);
                            String sTray = mData.get("Tray").toString();
                            String sStatus = mData.get("Status").toString();

                            deliveryOrderEntity = new DeliveryOrderEntity(seq,sDocno,sSalerId,sSaler,sProductCode,sProductName,sProductModels,iQuantity,iQuantityPcs,sCodeRule,sTray,sStatus,"1","","");
                            hzDb.deliveryOrderDao().insert(deliveryOrderEntity);
                            seq++;
                        }
                    }
                }else{
                    //本地数据库状态
                    mapResponseStatus = new ArrayList<Map<String,Object>>();
                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put("statusCode","0");
                    map.put("statusSqlcode","0");
                    map.put("statusDescription","Success");
                    mapResponseStatus.add(map);
                }

                deliveryOrderEntityList = hzDb.deliveryOrderDao().getOrderDoc(strDocno);
                mapSqlList = new ArrayList<Map<String,Object>>();
                for(int i=0;i<deliveryOrderEntityList.size();i++){
                    mapSqlList.add(deliveryOrderEntityList.get(i).getListData());
                }

                e.onNext(mapResponseStatus);
                e.onNext(mapSqlList);
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
                            MyToast.myShow(DeliveryOrderActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(DeliveryOrderActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.i("rxjavaerror",e.getMessage());
                MyToast.myShow(DeliveryOrderActivity.this,e.getMessage(),2,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(mapSqlList.size()>0){
                    //单头数据显示
                    String sDocno = mapSqlList.get(0).get("Docno").toString();
                    String sSaler = mapSqlList.get(0).get("SalerId").toString()+"."+mapSqlList.get(0).get("Saler").toString();
                    txtDeliveryOrderDocno.setText(sDocno);
                    txtDeliveryOrderFlag.setText("Y");
                    txtDeliveryOrderSaler.setText(sSaler);

                    //刷新总数显示
                    refreshData(mapSqlList);
                    refreshList(mapSqlList);

                    //明细数据显示
                    deliveryOrderListAdapter = new DeliveryOrderListAdapter(mapSqlList,getApplicationContext());
                    deliveryOrderListView.setAdapter(deliveryOrderListAdapter);

                    //图片显示
                    if(isSuccessCommit()){
                        imgDeliveryOrderResultIcon.setVisibility(View.VISIBLE);
                        imgDeliveryOrderResultIcon.setImageDrawable(getResources().getDrawable(R.drawable.complete));
                    }
                }

                loadingDialog.dismiss();
            }
        });
    }

    /**
    *描述: 扫描标签信息跳转三点照合界面,扫描结果匹配本地库是否有符合条件数据
    *日期：2022/5/26
    **/
    private void getScanCheckData(String qrCode){
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据获取中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<DeliveryOrderEntity>() {
            @Override
                public void subscribe(ObservableEmitter<DeliveryOrderEntity> e) throws Exception {

                //解析二维码
                String[] qrCodeValue = qrCode.split("_");
                int qrIndex = qrCode.indexOf("_");

                //初始化变量
                String sDocno = txtDeliveryOrderDocno.getText().toString();
                String sProductCode = "";
                String sQuantity = "0";
                String sQrcode = "";
                int iQuantity = 0;
                String sFlag = "N";
                String sMessage="";

                if(qrIndex==-1){
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
                        //查询本地数据库数据
                        sProductCode = mapResponseList.get(0).get("ProductCode").toString();
                        sQuantity = mapResponseList.get(0).toString();
                        sQrcode = qrCode;
                        iQuantity = Integer.parseInt(sQuantity);
                        deliveryOrderEntity = hzDb.deliveryOrderDao().queryOrder(sDocno,sProductCode,iQuantity,"C");
                        if(deliveryOrderEntity!=null){
                            sFlag = "Y";
                        }else{
                            sMessage = "备货单未匹配到此零件和数量";
                        }
                    }else{
                        sMessage = "T100无此标签数据";
                    }
                }else{
                    //检查是否扫码重复
                    sQrcode = qrCodeValue[0];
                    deliveryOrderEntity = hzDb.deliveryOrderDao().queryOrderQrcode(sQrcode,"C");
                    if(deliveryOrderEntity == null){
                        //按照本地库匹配条码信息，取消从T100查询标签信息
                        sDocno = txtDeliveryOrderDocno.getText().toString();
                        sProductCode = qrCodeValue[1];
                        sQuantity = qrCodeValue[5];
                        if(sQuantity.equals("")||sQuantity.isEmpty()){
                            sQuantity = "0";
                        }

                        try{
                            iQuantity = Integer.parseInt(sQuantity.trim());
                            deliveryOrderEntity = hzDb.deliveryOrderDao().queryOrder(sDocno,sProductCode,iQuantity,"C");
                            if(deliveryOrderEntity!=null){
                                sFlag = "Y";
                            }else{
                                sMessage = "备货单未匹配到此零件和数量";
                            }
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }else{
                        sFlag = "N";
                        sMessage = "标签重复";
                    }


                }

                //写入异常数据
                if(sFlag.equals("N")){
                    deliveryOrderEntity = new DeliveryOrderEntity(0,sDocno,"","",sProductCode,"","",iQuantity,1,"",sMessage,"X",qrCode,"","");
                }else if(sFlag.equals("Y")){
                    hzDb.deliveryOrderDao().updateOrderQrcode(sQrcode,"S",sDocno,deliveryOrderEntity.getId());
                    deliveryOrderEntity = hzDb.deliveryOrderDao().queryOrderQrcode(sQrcode,"S");
                }

                e.onNext(deliveryOrderEntity);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<DeliveryOrderEntity>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(DeliveryOrderEntity deliveryOrderEntity) {
                if(deliveryOrderEntity.getId()==0){
                    MyToast.myShow(DeliveryOrderActivity.this,deliveryOrderEntity.getTray(),2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.i("rxjavaerror",e.getMessage());
                MyToast.myShow(DeliveryOrderActivity.this,e.getMessage(),2,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(deliveryOrderEntity.getId()!=0){
                    Intent intent = new Intent(DeliveryOrderActivity.this,DeliveryOrderCheckActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("title",strTitle);
                    bundle.putString("productCode",deliveryOrderEntity.getProductCode());
                    bundle.putString("productName",deliveryOrderEntity.getProductName());
                    bundle.putString("productModels",deliveryOrderEntity.getProductModels());
                    bundle.putString("docno",deliveryOrderEntity.getDocNo());
                    bundle.putString("qrcode",deliveryOrderEntity.getQrCode());
                    bundle.putString("qrCodeRule",deliveryOrderEntity.getQrCodeRule());
                    bundle.putString("saler",deliveryOrderEntity.getSaler());
                    bundle.putInt("quantity",deliveryOrderEntity.getQuantity());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                loadingDialog.dismiss();
            }
        });
    }

    /**
    *描述: 更新出货OQC数据
    *日期：2022/6/1
    **/
    private void updateQcData(){
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String,Object>>> e) throws Exception {

                //获取提交数据
                String sRecordDetail="";
                deliveryOrderEntityList = hzDb.deliveryOrderDao().getAll();
                mapSqlList = new ArrayList<Map<String,Object>>();
                for(int i=0;i<deliveryOrderEntityList.size();i++){
//                    mapSqlList.add(deliveryOrderEntityList.get(i).getListData());
                    sRecordDetail = sRecordDetail + "&lt;Record&gt;\n"+
                            "&lt;Field name=\"bcaa011\" value=\""+deliveryOrderEntityList.get(i).getQrCode()+"\"/&gt;\n"+    //条码编号
                            "&lt;Field name=\"bcaa016\" value=\""+deliveryOrderEntityList.get(i).getStatus()+"\"/&gt;\n"+    //标识码
                            "&lt;Field name=\"bcaaud009\" value=\""+deliveryOrderEntityList.get(i).getDocNo()+"\"/&gt;\n"+    //关联出货单号
                            "&lt;Field name=\"bcaaud010\" value=\""+deliveryOrderEntityList.get(i).getDesc()+"\"/&gt;\n"+    //异常原因
                            "&lt;Field name=\"bcaaua007\" value=\""+deliveryOrderEntityList.get(i).getSaleQrCode()+"\"/&gt;\n"+    //客户标签
                            "&lt;Field name=\"bcaaua008\" value=\""+deliveryOrderEntityList.get(i).getStatus()+"\"/&gt;\n"+    //检验状态
                            "&lt;/Record&gt;\n";
                }

                //初始化T100服务名
                String webServiceName = "QcCheckUpdate";

                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"qcba_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"qcbasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"qcbaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"qcba001\" value=\""+txtDeliveryOrderDocno.getText().toString()+"\"/&gt;\n"+
                        "&lt;Field name=\"qcbacnfid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        sRecordDetail+
                        "&lt;/Detail&gt;\n"+
                        "&lt;Memo/&gt;\n"+
                        "&lt;Attachment count=\"0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Master&gt;\n"+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

                //回写成功删除本地数据
                for(Map<String,Object> mStatus: mapResponseStatus){
                    statusCode = mStatus.get("statusCode").toString();
                    if(statusCode.equals("0")){
                        hzDb.deliveryOrderDao().deleteOrderDoc(txtDeliveryOrderDocno.getText().toString());
                    }
                }

                e.onNext(mapResponseStatus);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<Map<String,Object>>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<Map<String,Object>> maps) {
                if(mapResponseStatus.size()> 0){
                    for(Map<String,Object> mStatus: mapResponseStatus){
                        statusCode = mStatus.get("statusCode").toString();
                        statusDescription = mStatus.get("statusDescription").toString();

                        if(!statusCode.equals("0")){
                            MyToast.myShow(DeliveryOrderActivity.this,statusDescription,0,0);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.i("rxjavaerror",e.getMessage());
                MyToast.myShow(DeliveryOrderActivity.this,e.getMessage(),2,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(DeliveryOrderActivity.this,statusDescription,1,0);
                    finish();
                }
                loadingDialog.dismiss();
            }
        });
    }

}