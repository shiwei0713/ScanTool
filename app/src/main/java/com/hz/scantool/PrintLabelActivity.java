package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.gprinter.bean.PrinterDevices;
import com.gprinter.utils.CallbackListener;
import com.gprinter.utils.Command;
import com.gprinter.utils.ConnMethod;
import com.gprinter.utils.LogUtils;
import com.gprinter.utils.SDKUtils;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.PrintLabelListAdapter;
import com.hz.scantool.dialog.DeviceListDialog;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;
import com.hz.scantool.printer.BlueToothDeviceActivity;
import com.hz.scantool.printer.PermissionUtils;
import com.hz.scantool.printer.PrintContent;
import com.hz.scantool.printer.Printer;
import com.hz.scantool.printer.ThreadPoolManager;
import com.hz.scantool.printer.Utils;

import org.w3c.dom.Text;

import java.io.IOException;
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

public class PrintLabelActivity extends AppCompatActivity implements CallbackListener {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private String TAG=MainActivity.class.getSimpleName();

    private String strTitle;
    private int intIndex;
    private List<String> mDatas;

    private EditText inputPrintLabelQrcode,printLabelModQuantity;
    private Button btnPrintLabelQrcode,btnPrint,btnCancel,btnSetDevice,btnHide;
    private TextView printLabelProductCode,printLabelProductName,printLabelProductModels,printLabelQuantity,printLabelCurrentProcessId,printLabelCurrentProcess;
    private TextView printLabelAttribute,printLabelProductDocno,printLabelQrcode,inputPrintLabelDevice,txtPrinter;
    private ListView printLabelList;
    private LinearLayout viewBasic;

    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private String statusCode,statusDescription;
    private PrintLabelListAdapter printLabelListAdapter;
    private Context context;
    private Printer printer=null;
    private PermissionUtils permissionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_label);

        //初始化
        initBundle();
        initView();

        //初始化权限
        initPermission();
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
                    MyToast.myShow(PrintLabelActivity.this,tip,1,0);
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
                        MyToast.myShow(PrintLabelActivity.this,getString(R.string.status_normal),1,0);
                        return;
                    }else if (status==-2){//状态缺纸
                        MyToast.myShow(PrintLabelActivity.this,getString(R.string.status_out_of_paper),2,0);
                        return;
                    }else if (status==-3){//状态开盖
                        MyToast.myShow(PrintLabelActivity.this,getString(R.string.status_open),2,0);
                        return;
                    }else if (status==-4){
                        MyToast.myShow(PrintLabelActivity.this,getString(R.string.status_overheated),2,0);
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
                    inputPrintLabelDevice.setText(getString(R.string.not_connected));
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(PrintLabelActivity.this);
//                intentIntegrator.setTimeout(5000);
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

        //PDA扫码
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

        //蓝牙打印机
        if (resultCode== Activity.RESULT_OK){
            switch (requestCode){
                case 0x00://蓝牙返回mac地址
                    String mac =data.getStringExtra(BlueToothDeviceActivity.EXTRA_DEVICE_ADDRESS);
                    Log.e(TAG, SDKUtils.bytesToHexString(mac.getBytes()));
                    PrinterDevices blueTooth=new PrinterDevices.Build()
                            .setContext(context)
                            .setConnMethod(ConnMethod.BLUETOOTH)
                            .setMacAddress(mac)
                            .setCommand(Command.ESC)
                            .setCallbackListener(this)
                            .build();
                    printer.connect(blueTooth);
                    break;
            }
        }
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
        intIndex = bundle.getInt("btnId");
    }

    /**
     *描述: 初始化控件
     *日期：2022/6/6
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.printLabelToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        viewBasic = findViewById(R.id.viewBasic);
        inputPrintLabelQrcode = findViewById(R.id.inputPrintLabelQrcode);
        printLabelProductCode = findViewById(R.id.printLabelProductCode);
        printLabelProductName = findViewById(R.id.printLabelProductName);
        printLabelProductModels = findViewById(R.id.printLabelProductModels);
        printLabelQuantity = findViewById(R.id.printLabelQuantity);
        printLabelModQuantity = findViewById(R.id.printLabelModQuantity);
        printLabelCurrentProcessId = findViewById(R.id.printLabelCurrentProcessId);
        printLabelCurrentProcess = findViewById(R.id.printLabelCurrentProcess);
        printLabelAttribute = findViewById(R.id.printLabelAttribute);
        printLabelProductDocno = findViewById(R.id.printLabelProductDocno);
        printLabelQrcode = findViewById(R.id.printLabelQrcode);
        inputPrintLabelDevice = findViewById(R.id.inputPrintLabelDevice);
        btnPrintLabelQrcode = findViewById(R.id.btnPrintLabelQrcode);
        btnPrint = findViewById(R.id.btnPrint);
        btnCancel = findViewById(R.id.btnCancel);
        btnSetDevice = findViewById(R.id.btnSetDevice);
        printLabelList = findViewById(R.id.printLabelList);
        txtPrinter = findViewById(R.id.txtPrinter);
        btnHide = findViewById(R.id.btnHide);

        context=PrintLabelActivity.this;
        permissionUtils=new PermissionUtils(context);
        printer=Printer.getInstance();//获取管理对象

        //定义事件
        btnPrintLabelQrcode.setOnClickListener(new btnClickListener());
        btnPrint.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
        btnSetDevice.setOnClickListener(new btnClickListener());
        btnHide.setOnClickListener(new btnClickListener());
        printLabelList.setOnItemClickListener(new listItemClickListener());

        //初始化设备清单
        initData();

        //初始化显示标题
        if(intIndex == 530){
            txtPrinter.setText(getResources().getText(R.string.content_title371));
        }else{
            txtPrinter.setText(getResources().getText(R.string.content_title37));
        }
    }

    @Override
    public void onConnecting() {
        inputPrintLabelDevice.setText(getString(R.string.conning));
    }

    @Override
    public void onCheckCommand() {
        inputPrintLabelDevice.setText(getString(R.string.checking));
    }

    @Override
    public void onSuccess(PrinterDevices printerDevices) {
        MyToast.myShow(PrintLabelActivity.this,getString(R.string.conn_success),1,0);
        inputPrintLabelDevice.setText(getString(R.string.conned));  //+"\n"+printerDevices.toString()
    }

    @Override
    public void onReceive(byte[] bytes) {

    }

    @Override
    public void onFailure() {
        MyToast.myShow(PrintLabelActivity.this,getString(R.string.conn_fail),2,0);
        mHandler.obtainMessage(0x02).sendToTarget();
    }

    @Override
    public void onDisconnect() {
        MyToast.myShow(PrintLabelActivity.this,getString(R.string.disconnect),2,0);
        mHandler.obtainMessage(0x02).sendToTarget();
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

    /**
    *描述: 按钮事件实现
    *日期：2022/6/15
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnPrintLabelQrcode:
                    String sCode = inputPrintLabelQrcode.getText().toString();
                    getQrcodeData(sCode.toUpperCase());
                    break;
                case R.id.btnSetDevice:
                    if(intIndex==530){
                        startActivityForResult(new Intent(context, BlueToothDeviceActivity.class),0x00);
                    }else{
                        openSearchSelectDialog();
                    }
                    break;
                case R.id.btnPrint:
                    if(isDevice()){
                        updateQrcodeData();
                    }else{
                        MyToast.myShow(PrintLabelActivity.this,"设备必须输入",2,0);
                    }
                    break;
                case R.id.btnHide:
                    viewBasic.setVisibility(View.GONE);
                    break;
                case R.id.btnCancel:
                    finish();
                    break;
            }
        }
    }

    /**
     *描述: 选择设备清单
     *日期：2022/7/17
     **/
    public void openSearchSelectDialog() {
        DeviceListDialog.Builder alert = new DeviceListDialog.Builder(PrintLabelActivity.this);
        alert.setListData(mDatas);
        alert.setTitle("请选择设备");
        alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
            @Override
            public void onSelected(String info) {
                inputPrintLabelDevice.setText(info);
            }
        });
        DeviceListDialog mDialog = alert.show();
        //设置Dialog 尺寸
        mDialog.setDialogWindowAttr(0.8, 0.8, PrintLabelActivity.this);
    }

    /**
     *描述: 初始化选择数据
     *日期：2022/7/17
     **/
    private void initData() {
        mDatas = new ArrayList<>();

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "StockGet";
                String strType = "3";
                String strwhere = " 1=1";

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
                mapResponseList = t100ServiceHelper.getT100JsonDeviceData(strResponse,"stockinfo");

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
                    MyToast.myShow(PrintLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(PrintLabelActivity.this,e.getMessage(),0,0);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()> 0) {
                        //显示单头数据
                        for(int i=0;i<mapResponseList.size();i++){
                            mDatas.add(mapResponseList.get(i).get("DeviceId").toString());
                        }
                    }
                }else{
                    MyToast.myShow(PrintLabelActivity.this,statusDescription,0,0);
                }
            }
        });
    }

    /**
     *描述: 检查设备是否为空
     *日期：2022/7/16
     **/
    private boolean isDevice(){
        //检查设备是否为空
        String sDevicce = inputPrintLabelDevice.getText().toString();
        if(sDevicce.isEmpty()||sDevicce.equals("")){
            return false;
        }

        return true;
    }

    /**
    *描述: 标签列表行单击
    *日期：2022/6/15
    **/
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            TextView txtLabelQrcode = view.findViewById(R.id.txtLabelQrcode);
            TextView txtLabelProductCode = view.findViewById(R.id.txtLabelProductCode);
            TextView txtLabelProductName = view.findViewById(R.id.txtLabelProductName);
            TextView txtLabelProductModels = view.findViewById(R.id.txtLabelProductModels);
            TextView txtLabelProcessId = view.findViewById(R.id.txtLabelProcessId);
            TextView txtLabelProcess = view.findViewById(R.id.txtLabelProcess);
            TextView txtLabelDocno = view.findViewById(R.id.txtLabelDocno);
            TextView txtLabelQuantity = view.findViewById(R.id.txtLabelQuantity);

            printLabelProductCode.setText(txtLabelProductCode.getText().toString());
            printLabelProductName.setText(txtLabelProductName.getText().toString());
            printLabelProductModels.setText(txtLabelProductModels.getText().toString());
            printLabelQuantity.setText(txtLabelQuantity.getText().toString());
            printLabelCurrentProcessId.setText(txtLabelProcessId.getText().toString());
            printLabelCurrentProcess.setText(txtLabelProcess.getText().toString());
            printLabelQrcode.setText(txtLabelQrcode.getText().toString());
            printLabelProductDocno.setText(txtLabelDocno.getText().toString());
            viewBasic.setVisibility(View.VISIBLE);
        }
    }

    /**
    *描述: 获取二维码信息
    *日期：2022/6/15
    **/
    private void getQrcodeData(String qrcode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(PrintLabelActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "ItemInfoGet";
                String strType = "4";
                String strwhere = "";

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
                mapResponseList = t100ServiceHelper.getT100JsonItemQrcodeData(strResponse,"iteminfo");

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
                    MyToast.myShow(PrintLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(PrintLabelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()> 0) {
                        //显示单头数据
                        printLabelProductCode.setText(mapResponseList.get(0).get("ProductCode").toString());
                        printLabelProductName.setText(mapResponseList.get(0).get("ProductName").toString());
                        printLabelProductModels.setText(mapResponseList.get(0).get("ProductModels").toString());
                        printLabelQuantity.setText(mapResponseList.get(0).get("Quantity").toString());
                        printLabelCurrentProcessId.setText(mapResponseList.get(0).get("ProcessId").toString());
                        printLabelCurrentProcess.setText(mapResponseList.get(0).get("Process").toString());
                        printLabelAttribute.setText(mapResponseList.get(0).get("Attribute").toString());
                        printLabelQrcode.setText(mapResponseList.get(0).get("Qrcode").toString());
                        printLabelProductDocno.setText(mapResponseList.get(0).get("Docno").toString());

                        //显示清单
                        printLabelListAdapter = new PrintLabelListAdapter(mapResponseList,getApplicationContext());
                        printLabelList.setAdapter(printLabelListAdapter);
                    }
                }else{
                    MyToast.myShow(PrintLabelActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *描述: 打印标签
    *日期：2022/6/15
    **/
    private void updateQrcodeData(){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(PrintLabelActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "RepeatPrintLabel";

                //设备编号
                String sDevices = inputPrintLabelDevice.getText().toString().toUpperCase();
                if(intIndex==530){
                    sDevices = "BlueToolth";
                }

                //标签数量
                String sQuantity = printLabelModQuantity.getText().toString();
                String sLabelQuantity = printLabelQuantity.getText().toString();
                float fQuantity = 0;
                float fLabelQuantity = 0;
                float fQty = 0;
                if(sQuantity.equals("")||sQuantity.isEmpty()){
                    sQuantity = "0";
                }

                try{
                    fQuantity = Float.valueOf(sQuantity);
                    fLabelQuantity = Float.valueOf(sLabelQuantity);
                }catch (Exception ex){
                    ex.printStackTrace();
                    fQuantity = 0;
                    fLabelQuantity = 0;
                }
                if(fQuantity>0){
                    fQty = fQuantity;
                }else{
                    fQty = fLabelQuantity;
                }

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"qrcode\" value=\""+ printLabelQrcode.getText().toString() +"\"/&gt;\n"+  //条码编号
                        "&lt;Field name=\"bcaa009\" value=\""+ fQty +"\"/&gt;\n"+  //条码数量
                        "&lt;Field name=\"bcaaud004\" value=\""+ sDevices +"\"/&gt;\n"+  //设备编号
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
                mapResponseList = t100ServiceHelper.getT100RepeatPrintData(strResponse,"responsedata");

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
                    MyToast.myShow(PrintLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(PrintLabelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()>0){
                        if(intIndex==530){
                            String sQrcode = (String)mapResponseList.get(0).get("Qrcode");
                            String sProductName = printLabelProductName.getText().toString();
                            String sProductModel = printLabelProductModels.getText().toString();
                            String sLots = "";
                            String sEmp = "";
                            String sPrograme = (String)mapResponseList.get(0).get("Programe");
                            String sTray = (String)mapResponseList.get(0).get("Tray");
                            String sSaler = (String)mapResponseList.get(0).get("Saler");
                            String sKinds = (String)mapResponseList.get(0).get("Kind");
                            String sPosition = "";
                            String sQty = (String)mapResponseList.get(0).get("Quantity");
                            printLabel(null,sQrcode,sProductName,sProductModel,sLots,sEmp,sPrograme,sTray,sSaler,sKinds,sPosition,sQty);
                        }else{
                            MyToast.myShow(PrintLabelActivity.this,statusDescription,1,0);
                        }
                    }else{
                        MyToast.myShow(PrintLabelActivity.this,"无打印数据",1,0);
                    }
                }else{
                    MyToast.myShow(PrintLabelActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}