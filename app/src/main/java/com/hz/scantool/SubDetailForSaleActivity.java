package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gprinter.bean.PrinterDevices;
import com.gprinter.utils.CallbackListener;
import com.gprinter.utils.Command;
import com.gprinter.utils.ConnMethod;
import com.gprinter.utils.LogUtils;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubSaleDetailAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;
import com.hz.scantool.printer.PermissionUtils;
import com.hz.scantool.printer.PrintContent;
import com.hz.scantool.printer.Printer;
import com.hz.scantool.printer.ThreadPoolManager;
import com.hz.scantool.printer.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SubDetailForSaleActivity extends AppCompatActivity implements CallbackListener {

    private static final String TAG = SubDetailForSaleActivity.class.getSimpleName();

    private String strTitle;
    private String sDocno,sSaler,sFlag;
    private Button btnHide,btnFlag1,btnFlag2;
    private TextView saleDetailDocno,saleDetailSale,saleDetailCount,saleDetailDealCount,saleDetailPrinter;
    private ProgressBar progressBar;
    private ListView saleDetailTaskView;

    private String statusCode;
    private String statusDescription;
    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapResponseFilterList;
    private SubSaleDetailAdapter subSaleDetailAdapter;

    private Context context;
    private Printer printer=null;
    private PermissionUtils permissionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_for_sale);

        //初始化
        initBundle();
        initView();

        //初始化权限
        initPermission();

        //连接打印机
        getPairBLEAndConnectBLE();

        //显示详情数据
        sFlag= "N";
        getSaleListData(sFlag);
    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        sDocno = bundle.getString("docno");
        sSaler = bundle.getString("saler");
    }

    //初始化控件
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.saleDetailToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        context=SubDetailForSaleActivity.this;
        permissionUtils=new PermissionUtils(context);
        printer=Printer.getInstance();//获取管理对象

        //初始化控件
        saleDetailDocno = findViewById(R.id.saleDetailDocno);
        saleDetailSale = findViewById(R.id.saleDetailSale);
        saleDetailPrinter = findViewById(R.id.saleDetailPrinter);
        saleDetailCount = findViewById(R.id.saleDetailCount);
        saleDetailDealCount = findViewById(R.id.saleDetailDealCount);
        btnHide = findViewById(R.id.btnHide);
        btnFlag1 = findViewById(R.id.btnFlag1);
        btnFlag2 = findViewById(R.id.btnFlag2);
        progressBar = findViewById(R.id.progressBar);
        saleDetailTaskView = findViewById(R.id.saleDetailTaskView);

        //初始化值
        saleDetailDocno.setText(sDocno);
        saleDetailSale.setText(sSaler);
        btnFlag1.setSelected(true);
        btnFlag2.setSelected(false);

        //绑定事件
        btnHide.setOnClickListener(new btnClickListener());
        btnFlag1.setOnClickListener(new btnClickListener());
        btnFlag2.setOnClickListener(new btnClickListener());
        saleDetailTaskView.setOnItemClickListener(new itemClickListener());
    }

    /**
     *描述: 初始化权限
     *日期：2022/9/1
     **/
    private void initPermission() {
        permissionUtils.requestPermissions(getString(R.string.permission),
                new PermissionUtils.PermissionListener(){
                    @Override
                    public void doAfterGrand(String... permission) {

                    }
                    @Override
                    public void doAfterDenied(String... permission) {
                        for (String p:permission) {
                            switch (p){
                                case Manifest.permission.READ_EXTERNAL_STORAGE:
                                    Utils.shortToast(context,getString(R.string.no_read));
                                    break;
                                case Manifest.permission.ACCESS_FINE_LOCATION:
                                    Utils.shortToast(context,getString(R.string.no_permission));
                                    break;
                            }
                        }
                    }
                },  Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnecting() {
        saleDetailPrinter.setText(getString(R.string.conning));
    }

    @Override
    public void onCheckCommand() {
        saleDetailPrinter.setText(getString(R.string.checking));
    }

    @Override
    public void onSuccess(PrinterDevices printerDevices) {
        MyToast.myShow(context,getString(R.string.conn_success),1,0);
        saleDetailPrinter.setText(getString(R.string.conned));  //+"\n"+printerDevices.toString()
    }

    @Override
    public void onReceive(byte[] bytes) {

    }

    @Override
    public void onFailure() {
        MyToast.myShow(context,getString(R.string.conn_fail),2,0);
        mHandler.obtainMessage(0x02).sendToTarget();
    }

    @Override
    public void onDisconnect() {
        MyToast.myShow(context,getString(R.string.disconnect),2,0);
        mHandler.obtainMessage(0x02).sendToTarget();
    }

    /**
     *描述: 按钮点击事件
     *日期：2022/9/8
     **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnHide:
                    updateDetailListData();
                    break;
                case R.id.btnFlag1:
                    sFlag = "N";
                    btnFlag1.setSelected(true);
                    btnFlag2.setSelected(false);
                    getSaleListData(sFlag);
                    break;
                case R.id.btnFlag2:
                    sFlag = "Y";
                    btnFlag1.setSelected(false);
                    btnFlag2.setSelected(true);
                    getSaleListData(sFlag);
                    break;
            }
        }
    }

    //更新备货完成数据
    public void updateDetailListData() {
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "SaleRequestUpdate";
                String strStatus = "Y";

                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"xmdk_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"xmdksite\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"xmdkent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"xmdkdocno\" value=\""+saleDetailDocno.getText().toString()+"\"/&gt;\n"+
                        "&lt;Field name=\"xmdkud002\" value=\""+UserInfo.getUserId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"xmdkud003\" value=\""+strStatus+"\"/&gt;\n"+
                        "&lt;Memo/&gt;\n"+
                        "&lt;Attachment count=\"0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Master&gt;\n"+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                List<Map<String,Object>> strResponseList = t100ServiceHelper.getT100StatusData(strResponse);
                for(Map<String,Object> m: strResponseList){
                    statusCode = m.get("statusCode").toString();
                    statusDescription = m.get("statusDescription").toString();
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
                MyToast.myShow(SubDetailForSaleActivity.this,"更新失败",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(!statusCode.equals("0")){
                    MyToast.myShow(SubDetailForSaleActivity.this,statusDescription,0,0);
                }else{
                    MyToast.myShow(SubDetailForSaleActivity.this,statusDescription,1,0);
                    finish();
                }
                loadingDialog.dismiss();
            }
        });
    }

    /**
     *描述: 明细行点击事件
     *日期：2022/9/9
     **/
    private class itemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        }
    }

    /**
    *描述: 显示汇总清单
    *日期：2022/10/14
    **/
    private void setTotal(String status){
        int iCount = 0;
        int iDealCount = 0 ;
        for(int i=0;i<mapResponseList.size();i++){
            String sStatus = (String)mapResponseList.get(i).get("Status");
            if(sStatus.equals("Y")){
                iDealCount = iDealCount + 1;
            }else{
                iCount = iCount + 1;
            }
        }

        saleDetailCount.setText(String.valueOf(iCount));
        saleDetailDealCount.setText(String.valueOf(iDealCount));

        if(mapResponseFilterList!=null){
            mapResponseFilterList.clear();
        }else{
            mapResponseFilterList = new ArrayList<Map<String,Object>>();
        }

        for(int i=0;i<mapResponseList.size();i++){
            String sStatus = (String)mapResponseList.get(i).get("Status");
            if(sStatus.equals(status)){
                Map<String, Object> map = new HashMap<String, Object>();

                String sDocno = (String)mapResponseList.get(i).get("Docno");
                String sProductCode = (String)mapResponseList.get(i).get("ProductCode");
                String sProductName = (String)mapResponseList.get(i).get("ProductName");
                String sProductModels = (String)mapResponseList.get(i).get("ProductModels");
                String sStockId = (String)mapResponseList.get(i).get("StockId");
                String sStock = (String)mapResponseList.get(i).get("Stock");
                String sPositionId = (String)mapResponseList.get(i).get("PositionId");
                String sPosition = (String)mapResponseList.get(i).get("Position");
                String sQuantity = (String)mapResponseList.get(i).get("Quantity");
                String sInventory = (String)mapResponseList.get(i).get("Inventory");
                String sPackages = (String)mapResponseList.get(i).get("Packages");
                String sQuantityStock = (String)mapResponseList.get(i).get("QuantityStock");
                String sFlag = (String)mapResponseList.get(i).get("Flag");
                String sPrintQuantity = (String)mapResponseList.get(i).get("PrintQuantity");
                String sPrintQuantityStock = (String)mapResponseList.get(i).get("PrintQuantityStock");

                map.put("Docno", sDocno);
                map.put("ProductCode", sProductCode);
                map.put("ProductName", sProductName);
                map.put("ProductModels", sProductModels);
                map.put("StockId", sStockId);
                map.put("Stock", sStock);
                map.put("Position", sPosition);
                map.put("PositionId", sPositionId);
                map.put("Quantity", sQuantity);
                map.put("Inventory", sInventory);
                map.put("Packages", sPackages);
                map.put("QuantityStock", sQuantityStock);
                map.put("Status", sStatus);
                map.put("Flag", sFlag);
                map.put("PrintQuantity", sPrintQuantity);
                map.put("PrintQuantityStock", sPrintQuantityStock);

                mapResponseFilterList.add(map);
            }
        }
    }

    /**
     *描述: 显示备货清单
     *日期：2022/9/8
     **/
    private void getSaleListData(String status){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppListGet";
                String strType = "6";
                String sWhere = " xmdkdocno = '"+sDocno+"'";

                //清空数据
                if(mapResponseList!=null){
                    mapResponseList.clear();
                }

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+sWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonSaleDetailData(strResponse,"salelist");

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
                            MyToast.myShow(SubDetailForSaleActivity.this,statusDescription,0,0);
                        }else{
                            int progress = progressBar.getProgress();
                            progress = progress + 50;
                            progressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailForSaleActivity.this,"无备货数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForSaleActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete() {
                if(mapResponseList.size()>0){
                    //更新集合
//                    mergeLabelQty();

                    //刷新总数
                    setTotal(status);

                    subSaleDetailAdapter = new SubSaleDetailAdapter(mapResponseFilterList,getApplicationContext(),printClickListener);
                    saleDetailTaskView.setAdapter(subSaleDetailAdapter);
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
    *描述: 合并尾数标签
    *日期：2022/10/17
    **/
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void mergeLabelQty() throws NumberFormatException{
        for(int i=0;i<mapResponseList.size();i++){
            String sProductCode = (String)mapResponseList.get(i).get("ProductCode");
            String sQuantityStock = (String)mapResponseList.get(i).get("QuantityStock");
            String sPackage = (String)mapResponseList.get(i).get("Packages");
            String sQuantity = (String)mapResponseList.get(i).get("Quantity");
            String sFlag = (String)mapResponseList.get(i).get("Flag");
            String sInventory = (String)mapResponseList.get(i).get("Inventory");

            int iPackage = Integer.parseInt(sPackage);     //包装量
            int iQuantity = Integer.parseInt(sQuantity);    //出货量
            int iQuantityStock = Integer.parseInt(sQuantityStock); //回仓量
            int iInventory = Integer.parseInt(sInventory);  //库存量
            int iMod = Math.floorMod(iQuantity,iPackage);  //出货尾数量
            int iModStock = Math.floorMod(iQuantityStock,iPackage);  //回仓尾数量

            //如果库存量>包装量，则出货类别为托盘，当出货量=出货尾数量，则无尾数
            if(iInventory>iPackage && iQuantity==iMod){
                iMod = 0;
                iModStock = 0;
            }

            for(int j=i+1;j<mapResponseList.size();j++){
                String sProductCode2 = (String)mapResponseList.get(j).get("ProductCode");
                String sQuantityStock2 = (String)mapResponseList.get(j).get("QuantityStock");
                String sQuantity2 = (String)mapResponseList.get(j).get("Quantity");
                String sInventory2 = (String)mapResponseList.get(j).get("Inventory");
                String sPackage2 = (String)mapResponseList.get(j).get("Packages");

                int iQuantity2 = Integer.parseInt(sQuantity2);
                int iQuantityStock2 = Integer.parseInt(sQuantityStock2);
                int iMod2 = Math.floorMod(iQuantity2,iPackage);
                int iModStock2 = Math.floorMod(iQuantityStock2,iPackage);
                int iInventory2 = Integer.parseInt(sInventory2);
                int iPackage2 = Integer.parseInt(sPackage2);

                //匹配相同零件
                if(sProductCode.equals(sProductCode2)){
                    int iQuantity3 = iQuantity2 + iMod;
                    int iQuantityStock3 = iQuantityStock2 - iMod;
                    int iModStock3 = iModStock2 - iModStock;
                    if(iModStock3<0){
                        iModStock3 = 0;
                    }

                    mapResponseList.get(j).put("Quantity", String.valueOf(iQuantity3));
                    mapResponseList.get(j).put("QuantityStock", String.valueOf(iQuantityStock3));
                    mapResponseList.get(j).put("PrintQuantity", String.valueOf(iMod+iMod2));
                    mapResponseList.get(j).put("PrintQuantityStock", String.valueOf(iModStock3));

                    //更新旧值
                    int iQuantity4 = iQuantity - iMod;
                    int iQuantityStock4 = iQuantityStock + iMod;
                    mapResponseList.get(i).put("Quantity", String.valueOf(iQuantity4));
                    mapResponseList.get(i).put("QuantityStock", String.valueOf(iQuantityStock4));
                    mapResponseList.get(i).put("PrintQuantity", String.valueOf(0));
                    mapResponseList.get(i).put("PrintQuantityStock", String.valueOf(0));

                    //更新标识
                    sFlag = "Y";
                    mapResponseList.get(j).put("Flag", sFlag);
                    break;
                }
            }

            if(sFlag.equals("N")){
                mapResponseList.get(i).put("PrintQuantity", String.valueOf(iMod));
                mapResponseList.get(i).put("PrintQuantityStock", String.valueOf(iModStock));
            }
        }
    }

    /**
     *描述: 打印机handler
     *日期：2022/9/1
     **/
    Handler mHandler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 0x00:
                    String tip=(String)msg.obj;
                    MyToast.myShow(context,tip,1,0);
                    break;
                case 0x01:
                    int status=msg.arg1;
                    if (status==-1){//获取状态失败
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle(getString(R.string.tip))
                                .setMessage(getString(R.string.status_fail))
                                .setIcon(R.mipmap.ic_launcher)
                                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .create();
                        alertDialog.show();
                        return;
                    }else if (status==0){//状态正常
                        MyToast.myShow(context,getString(R.string.status_normal),1,0);
                        return;
                    }else if (status==-2){//状态缺纸
                        MyToast.myShow(context,getString(R.string.status_out_of_paper),2,0);
                        return;
                    }else if (status==-3){//状态开盖
                        MyToast.myShow(context,getString(R.string.status_open),2,0);
                        return;
                    }else if (status==-4){
                        MyToast.myShow(context,getString(R.string.status_overheated),2,0);
                        return;
                    }
                    break;
                case 0x02://关闭连接
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (printer.getPortManager()!=null){
                                printer.close();
                            }
                        }
                    }).start();
                    saleDetailPrinter.setText(getString(R.string.not_connected));
                    break;
                case 0x03:
                    String message=(String)msg.obj;
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle(getString(R.string.tip))
                            .setMessage(message)
                            .setIcon(R.mipmap.ic_launcher)
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .create();
                    alertDialog.show();
                    break;
            }
        }
    };

    /**
    *描述: 获取已连接蓝牙设备
    *日期：2022/9/9
    **/
    private void getPairBLEAndConnectBLE(){
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        String sMac = "";
        String sPrinter = "";

        if(defaultAdapter!=null){
            Set<BluetoothDevice> devices = defaultAdapter.getBondedDevices();

            for(BluetoothDevice bluetoothDevice : devices){
                sMac = bluetoothDevice.getAddress();
                sPrinter = bluetoothDevice.getName();
                Log.i(TAG,bluetoothDevice.getAddress());
            }

            //连接打印机
            PrinterDevices blueTooth=new PrinterDevices.Build()
                    .setContext(context)
                    .setConnMethod(ConnMethod.BLUETOOTH)
                    .setMacAddress(sMac)
                    .setCommand(Command.ESC)
                    .setCallbackListener(this)
                    .build();
            printer.connect(blueTooth);

            //显示连接
            if(printer.getPortManager()!=null){
                saleDetailPrinter.setText(sPrinter);
            }
        }
    }

    /**
     *描述: 成功消息
     *日期：2022/9/1
     **/
    private void tipsToast(String message){
        Message msg =new Message();
        msg.what=0x00;
        msg.obj=message;
        mHandler.sendMessage(msg);
    }

    /**
     *描述: 提示弹框
     *日期：2022/9/1
     **/
    private void tipsDialog(String message){
        Message msg =new Message();
        msg.what=0x03;
        msg.obj=message;
        mHandler.sendMessage(msg);
    }

    private SubSaleDetailAdapter.PrintClickListener printClickListener = new SubSaleDetailAdapter.PrintClickListener() {
        @Override
        public void PrintOnClick(int position, View v) {

            String docno = saleDetailDocno.getText().toString();
            String productCode = subSaleDetailAdapter.getItemValue(position,"ProductCode");
            String productName = subSaleDetailAdapter.getItemValue(position,"ProductName");
            String productModel = subSaleDetailAdapter.getItemValue(position,"ProductModels");
            String stockId = subSaleDetailAdapter.getItemValue(position,"StockId");
            String positionId = subSaleDetailAdapter.getItemValue(position,"PositionId");
            String quantity = subSaleDetailAdapter.getItemValue(position,"PrintQuantity");
            String labelQuantity = subSaleDetailAdapter.getItemValue(position,"PrintQuantityStock");
            String packages = subSaleDetailAdapter.getItemValue(position,"Packages");
            String isSuccess = "Y";

            if(quantity.equals("")||quantity.isEmpty()||quantity.equals("0")){
                if(labelQuantity.equals("")||labelQuantity.isEmpty()||labelQuantity.equals("0")){
                    isSuccess = "N";
                    MyToast.myShow(SubDetailForSaleActivity.this,"数量为0,不需打印",2,0);
                }
            }

            if(isSuccess.equals("Y")){
                mergeQrcodeData(v,productCode,productName,productModel,docno,stockId,positionId,quantity,labelQuantity,packages);
            }
        }
    };

    /**
    *描述: 生成条码数据
    *日期：2022/10/13
    **/
    private void mergeQrcodeData(View v,String sProductCode,String sProductName,String sProductModel,String sDocno,String sStockId,String sPositionId,String sQuantity,String sLabelQuantity,String sPackages){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForSaleActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "RepeatPrintLabel";
                String sAction = "merge";
                String sDevices = "BlueToolth";

                //标签数量
                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"bcaa002\" value=\""+ sProductCode +"\"/&gt;\n"+  //料件编号
                        "&lt;Field name=\"bcaa009\" value=\""+ sQuantity +"\"/&gt;\n"+  //条码数量
                        "&lt;Field name=\"bcaaud001\" value=\""+ sStockId +"\"/&gt;\n"+  //库位
                        "&lt;Field name=\"bcaaud002\" value=\""+ sPositionId +"\"/&gt;\n"+  //储位
                        "&lt;Field name=\"bcaaud004\" value=\""+ sDevices +"\"/&gt;\n"+  //设备编号
                        "&lt;Field name=\"bcaa004\" value=\""+ sDocno +"\"/&gt;\n"+  //来源单号
                        "&lt;Field name=\"bcaa0092\" value=\""+ sLabelQuantity +"\"/&gt;\n"+  //回仓数量
                        "&lt;Field name=\"package\" value=\""+ sPackages +"\"/&gt;\n"+  //包装数量
                        "&lt;Field name=\"act\" value=\""+ sAction +"\"/&gt;\n"+  //操作
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
                mapResponseList = t100ServiceHelper.getT100RepeatMulPrintData(strResponse,"responsedata");

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
                    MyToast.myShow(SubDetailForSaleActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForSaleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()>0){
                        for(int i=0;i<mapResponseList.size();i++){
                            String sQrcode = (String)mapResponseList.get(i).get("Qrcode");
                            String sLots = (String)mapResponseList.get(i).get("Lots");
                            String sEmp = "";
                            String sPrograme = (String)mapResponseList.get(i).get("Programe");
                            String sTray = (String)mapResponseList.get(i).get("Tray");
                            String sSaler = (String)mapResponseList.get(i).get("Saler");
                            String sKinds = (String)mapResponseList.get(i).get("Kind");
                            String sPosition = (String)mapResponseList.get(i).get("Position");

                            if(sPosition.equals("")||sPosition.isEmpty()){
                                sPosition = sLots;
                            }else{
                                sPosition = sLots+"("+sPosition+")";
                            }

                            String sQty = (String)mapResponseList.get(i).get("Quantity");

                            printLabel(v,sQrcode,sProductName,sProductModel,sLots,sEmp,sPrograme,sTray,sSaler,sKinds,sPosition,sQty);
                        }
                    }else{
                        MyToast.myShow(SubDetailForSaleActivity.this,"无打印数据",1,0);
                    }
                }else{
                    MyToast.myShow(SubDetailForSaleActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;

                //刷新显示
                getSaleListData(sFlag);
            }
        });
    }

    /**
     *描述: 打印标签
     *日期：2022/9/1
     **/
    public void printLabel(View view,String qrcode,String productName,String productModel,String lots,String emp,String programe,String tray,String saler,String kinds,String position,String qty) {
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    if (printer.getPortManager()==null){
                        tipsToast(getString(R.string.conn_first));
                        return;
                    }
                    //打印前查询打印机状态，部分老款打印机不支持查询请去除下面查询代码
                    //******************     查询状态     ***************************
                    Command command = printer.getPortManager().getCommand();
                    int status = printer.getPrinterState(command);
                    if (status != 0) {//打印机处于不正常状态、则不发送打印
                        Message msg = new Message();
                        msg.what = 0x01;
                        msg.arg1 = status;
                        mHandler.sendMessage(msg);
                        return;
                    }
                    //***************************************************************
                    boolean isRight;
                    if(kinds.equals("右件")){
                        isRight = true;
                    }else{
                        isRight = false;
                    }

                    boolean result=  printer.getPortManager().writeDataImmediately(isRight? PrintContent.getProductLabel(context,qrcode,productName,productModel,lots,emp,programe,tray,saler,kinds,position,qty):PrintContent.getLeftProductLabel(context,qrcode,productName,productModel,lots,emp,programe,tray,saler,kinds,position,qty));
                    if (result) {
                        tipsToast(getString(R.string.send_success));
                    }else {
                        tipsDialog(getString(R.string.send_fail));
                    }
                    LogUtils.e("send result",result);
                } catch (IOException e) {
                    tipsDialog(getString(R.string.disconnect)+"\n"+getString(R.string.print_fail)+e.getMessage());
                }catch (Exception e){
                    tipsDialog(getString(R.string.print_fail)+e.getMessage());
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtils.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (printer.getPortManager()!=null){
            printer.close();
        }
    }

}