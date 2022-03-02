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
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.gprinter.command.CpclCommand;
import com.gprinter.command.EscCommand;
import com.gprinter.command.FactoryCommand;
import com.gprinter.command.LabelCommand;
import com.gprinter.io.EthernetPort;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubListAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;
import com.hz.scantool.printer.CheckWifiConnThread;
import com.hz.scantool.printer.Constant;
import com.hz.scantool.printer.DeviceConnFactoryManager;
import com.hz.scantool.printer.PrintContent;
import com.hz.scantool.printer.PrinterCommand;
import com.hz.scantool.printer.ThreadPool;
import com.hz.scantool.printer.WifiParameterConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;
import static com.hz.scantool.printer.Constant.MESSAGE_UPDATE_PARAMETER;
import static com.hz.scantool.printer.Constant.tip;
import static com.hz.scantool.printer.Constant.ACTION_USB_PERMISSION;
import static com.hz.scantool.printer.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.hz.scantool.printer.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class SubActivity extends AppCompatActivity {

    private static final String TAG = SubActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 0x004;
    /**
     * 连接状态断开
     */
    private static final int CONN_STATE_DISCONN = 0x007;
    /**
     * 使用打印机指令错误
     */
    private static final int PRINTER_COMMAND_ERROR = 0x008;
    private static final int CONN_PRINTER = 0x12;
    private String strTitle;
    private int intIndex;
    private String mIp;
    private String mPort;
    private int id = 0;

    private Button btnPrinter;
    private Button btnConnect;
    private Button btnPrinterTest;
    private TextView txtStatus;
    private ThreadPool threadPool;
    private CheckWifiConnThread checkWifiConnThread;//wifi连接线程监听

    private String strType;
    private String statusCode;
    private String statusDescription;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private ProgressBar progressBar;
    private LoadingDialog loadingDialog;
    private ListView listView;
    private SubListAdapter subDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        //初始化参数
        initBundle();

        //初始化控件
        initView();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化广播
        initBroadcast();

        //初始化清单数据
        strType = "4";
        getSubListData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(intIndex != 4){
            getMenuInflater().inflate(R.menu.sub_menu,menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubActivity.this);
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

    @Override
    protected void onStart() {
        super.onStart();

        //获取连接对象是否连接
        DeviceConnFactoryManager [] deviceConnFactoryManagers;
        deviceConnFactoryManagers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
        for (int i = 0; i < 4; i++) {
            if (deviceConnFactoryManagers[i] != null && deviceConnFactoryManagers[i].getConnState()) {
                txtStatus.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                break;
            } else {
                txtStatus.setText(getString(R.string.str_conn_state_disconnect));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
        DeviceConnFactoryManager.closeAllPort();
        if (threadPool != null) {
            threadPool.stopThreadPool();
        }
    }

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){

    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        intIndex = bundle.getInt("index");
        strTitle = bundle.getString("title");
    }

    //初始化连接打印机IP和端口
    private void initPrinter(){
        mIp = "192.168.30.100";
        mPort = "9100";

        WifiParameterConfig wifiParameterConfig = new WifiParameterConfig(this,mHandler,mIp,mPort);
        wifiParameterConfig.WifiConnect();
    }

    //设置打印机模式
    private void setPrinterMode(){
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
            MyToast.myShow(SubActivity.this,"请先连接打印机",2,0);
            return;
        }

        byte [] bytes=null;

        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC){
            tip(String.format(getString(R.string.str_mode_tip),getString(R.string.str_cpclmode)));
            return;
        }else {
            bytes = FactoryCommand.changPrinterMode(FactoryCommand.printerMode.TSC);
        }

        threadPool=ThreadPool.getInstantiation();
        final byte[] finalBytes = bytes;
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {//发送切换打印机模式后会断开连接，如果切换模式成功，打印机蜂鸣器会响一声，打印机关机，需手动开启
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendByteDataImmediately(finalBytes);
                DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
            }
        });
    }

    private void initView(){
        txtStatus = findViewById(R.id.txtStatus);
        btnPrinter = findViewById(R.id.btnPrinter);
        btnConnect = findViewById(R.id.btnConnect);
        btnPrinterTest = findViewById(R.id.btnPrinterTest);
        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.subView);

        btnPrinter.setOnClickListener(new commandClickListener());
        btnConnect.setOnClickListener(new commandClickListener());
        btnPrinterTest.setOnClickListener(new commandClickListener());
        listView.setOnItemClickListener(new SubActivity.listItemClickListener());
    }

    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnPrinter:
                    savePicture(PrintContent.getBitmap(SubActivity.this),"testlabel");
                    break;
                case R.id.btnPrinterTest:
                    btnLabelPrint();
                    break;
                case R.id.btnConnect:
                    initPrinter();
                    break;
            }
        }
    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);//USB访问权限广播
        filter.addAction(ACTION_USB_DEVICE_DETACHED);//USB线拔出
        filter.addAction(ACTION_QUERY_PRINTER_STATE);//查询打印机缓冲区状态广播，用于一票一控
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);//与打印机连接状态
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);//USB线插入
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                //连接状态
                case DeviceConnFactoryManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                    int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                    switch (state) {
                        case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                            if (id == deviceId) {
                                Log.e(TAG,"connection is lost");
                                txtStatus.setText(getString(R.string.str_conn_state_disconnect));
                            }
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            txtStatus.setText(getString(R.string.str_conn_state_connecting));
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                            txtStatus.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                            break;
                        case CONN_STATE_FAILED:
                            MyToast.myShow(SubActivity.this,"连接失败",2,0);
                            //wificonn=false;
                            txtStatus.setText(getString(R.string.str_conn_state_disconnect));
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //获取设备连接信息
    private String getConnDeviceInfo() {
        String str = "";
        DeviceConnFactoryManager deviceConnFactoryManager = DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id];
        if (deviceConnFactoryManager != null
                && deviceConnFactoryManager.getConnState()) {
            if ("USB".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "USB\n";
                str += "USB Name: " + deviceConnFactoryManager.usbDevice().getDeviceName();
            } else if ("WIFI".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "WIFI\n";
                str += "IP: " + deviceConnFactoryManager.getIp() + "\t";
                str += "Port: " + deviceConnFactoryManager.getPort();
                checkWifiConnThread=new CheckWifiConnThread(deviceConnFactoryManager.getIp(),mHandler);//开启监听WiFi线程
                checkWifiConnThread.start();
            } else if ("BLUETOOTH".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "BLUETOOTH\n";
                str += "MacAddress: " + deviceConnFactoryManager.getMacAddress();
            } else if ("SERIAL_PORT".equals(deviceConnFactoryManager.getConnMethod().toString())) {
                str += "SERIAL_PORT\n";
                str += "Path: " + deviceConnFactoryManager.getSerialPortPath() + "\t";
                str += "Baudrate: " + deviceConnFactoryManager.getBaudrate();
            }
        }
        return str;
    }

    //打印标签
    public void btnLabelPrint() {
        threadPool = ThreadPool.getInstantiation();
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                        !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                    mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
                    return;
                }
                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    Bitmap b = PrintContent.getBitmap(SubActivity.this);

                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(PrintContent.getLabel(b));
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    private void savePicture(Bitmap bitmap,String fileName){
        if(bitmap==null){
            return;
        }

        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/hzimages");
        if(!folder.exists()){
            folder.mkdirs();
        }

        File lableFile = new File(folder,fileName);
        try{
            if(!lableFile.exists()){
                lableFile.createNewFile();
            }

            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(lableFile));
            bitmap.compress(Bitmap.CompressFormat.PNG,80,bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();

        }catch (IOException e){
            e.printStackTrace();
        }
        MyToast.myShow(SubActivity.this,"保存成功",2,0);
    }

    //打印xml标签
    private void printLabel(){

        //设置打印机模式
//        setPrinterMode();

        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
            mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
            return;
        }

        threadPool = ThreadPool.getInstantiation();
        threadPool.addSerialTask(new Runnable() {
            @Override
            public void run() {

                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.CPCL) {
                    CpclCommand cpcl=new CpclCommand();
                    cpcl.addInitializePrinter(1500,1);
                    // 打印图片  光栅位图  384代表打印图片像素  0代表打印模式
                    // 58mm打印机 可打印区域最大点数为 384 ，80mm 打印机 可打印区域最大点数为 576 例子为80mmd打印机
                    cpcl.addCGraphics(0,0,576, PrintContent.getBitmap(SubActivity.this));
                    cpcl.addPrint();
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(cpcl.getCommand());
                } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    LabelCommand labelCommand=new LabelCommand();
                    labelCommand.addSize(100,70);
                    labelCommand.addGap(2);
                    labelCommand.addCls();
                    // 打印图片  光栅位图  384代表打印图片像素  0代表打印模式
                    // 58mm打印机 可打印区域最大点数为 384 ，80mm 打印机 可打印区域最大点数为 576 例子为80mmd打印机
                    labelCommand.addBitmap(0,0, 576,PrintContent.getBitmap(SubActivity.this));
                    labelCommand.addPrint(1);
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(labelCommand.getCommand());
                }else  if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC) {
                    EscCommand esc = new EscCommand();
                    esc.addInitializePrinter();
                    // 打印图片  光栅位图  384代表打印图片像素  0代表打印模式
                    // 58mm打印机 可打印区域最大点数为 384 ，80mm 打印机 可打印区域最大点数为 576 例子为80mmd打印机
                    esc.addRastBitImage(PrintContent.getBitmap(SubActivity.this), 576, 0);
                    esc.addPrintAndLineFeed();
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(esc.getCommand());
                }

            }
        });
    }

    private void tip(String msg){
        Message message=new Message();
        message.obj=msg;
        message.what=Constant.tip;
        mHandler.sendMessage(message);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONN_STATE_DISCONN://断开连接
                    DeviceConnFactoryManager deviceConnFactoryManager=DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id];
                    if (deviceConnFactoryManager!= null&&deviceConnFactoryManager.getConnState()) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
                        MyToast.myShow(SubActivity.this,"成功断开连接",2,0);
                    }
                    break;
                case PRINTER_COMMAND_ERROR://打印机指令错误
                    MyToast.myShow(SubActivity.this,"请选择正确的打印机指令",2,0);
                    break;
                case CONN_PRINTER://未连接打印机
                    MyToast.myShow(SubActivity.this,"请先连接打印机",2,0);
                    break;
                case MESSAGE_UPDATE_PARAMETER:
                    String strIp = msg.getData().getString("Ip");
                    String strPort = msg.getData().getString("Port");
                    //初始化端口信息
                    new DeviceConnFactoryManager.Build()
                            //设置端口连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                            //设置端口IP地址
                            .setIp(strIp)
                            //设置端口ID（主要用于连接多设备）
                            .setId(id)
                            //设置连接的热点端口号
                            .setPort(Integer.parseInt(strPort))
                            .build();
                    threadPool = ThreadPool.getInstantiation();
                    threadPool.addSerialTask(new Runnable() {
                        @Override
                        public void run() {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                        }
                    });
                    break;
                case CheckWifiConnThread.PING_SUCCESS://WIfi连接成功\
                    Log.e(TAG,"wifi connect success!");
                    break;
                case CheckWifiConnThread.PING_FAIL://WIfI断开连接
                    Log.e(TAG,"wifi connect fail!");
                    MyToast.myShow(SubActivity.this,"断开连接",2,0);
                    checkWifiConnThread.cancel();
                    checkWifiConnThread=null;
                    mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                    break;
                case tip:
                    String str=(String) msg.obj;
                    MyToast.myShow(SubActivity.this,str,2,0);
                    break;
                default:
                    new DeviceConnFactoryManager.Build()
                            //设置端口连接方式
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                            //设置端口IP地址
                            .setIp("192.168.2.227")
                            //设置端口ID（主要用于连接多设备）
                            .setId(id)
                            //设置连接的热点端口号
                            .setPort(9100)
                            .build();
                    threadPool.addSerialTask(new Runnable() {
                        @Override
                        public void run() {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                        }
                    });
                    break;
            }
        }
    };

    //行单击事件
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(SubActivity.this,SubDetailActivity.class);
            startActivity(intent);
        }
    }

    //获取清单
    private void getSubListData(){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppWorkOrderListGet";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonWorkOrderData(strResponse,"workorder");

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
                            MyToast.myShow(SubActivity.this,statusDescription,0,0);
                        }else{
                            int progress = progressBar.getProgress();
                            progress = progress + 50;
                            progressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubActivity.this,"无备料数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubActivity.this,"网络错误",0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                subDetailAdapter = new SubListAdapter(mapResponseList,getApplicationContext(), strType);
                listView.setAdapter(subDetailAdapter);

                progressBar.setVisibility(View.GONE);
            }
        });
    }
}