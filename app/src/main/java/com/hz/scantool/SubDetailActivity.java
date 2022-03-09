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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gprinter.command.CpclCommand;
import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.models.UserInfo;
import com.hz.scantool.printer.CheckWifiConnThread;
import com.hz.scantool.printer.Constant;
import com.hz.scantool.printer.DeviceConnFactoryManager;
import com.hz.scantool.printer.PrintContent;
import com.hz.scantool.printer.PrinterCommand;
import com.hz.scantool.printer.ThreadPool;
import com.hz.scantool.printer.WifiParameterConfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.hz.scantool.printer.Constant.ACTION_USB_PERMISSION;
import static com.hz.scantool.printer.Constant.MESSAGE_UPDATE_PARAMETER;
import static com.hz.scantool.printer.Constant.tip;
import static com.hz.scantool.printer.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.hz.scantool.printer.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class SubDetailActivity extends AppCompatActivity {

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

    private String mIp;
    private String mPort;
    private int id = 0;
    private int w,h;

    private ThreadPool threadPool;
    private CheckWifiConnThread checkWifiConnThread;//wifi连接线程监听

    private String strTitle;
    private String strQuantity;
    private TextView subDetailProductName;
    private TextView subDetailQuantity;
    private TextView subDetailProductCode;
    private TextView subDetailProductModels;
    private TextView subDetailProcess;
    private TextView subDetailDevice;
    private TextView subDetailStartPlanDate;
    private TextView subDetailEndPlanDate;
    private TextView subDetailDocno;
    private TextView txtStatus;
    private ImageView imgQrcode;

    private Button btnConnect;
    private Button btnPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail);

        //初始化参数
        initView();
        initBundle();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subDetailToolBar);
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

        //显示二维码数据
        createQrcode(subDetailDocno.getText().toString()+"_"+subDetailProcess.getText().toString()+"_"+ UserInfo.getUserId(getApplicationContext()));

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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailActivity.this);
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

    //初始化传入参数
    private void initBundle(){
        strTitle = this.getResources().getString(R.string.master_detail1);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        subDetailProductName.setText(bundle.getString("ProductName"));
        strQuantity = bundle.getString("Quantity");
        subDetailQuantity.setText("");
        subDetailProductCode.setText(bundle.getString("ProductCode"));
        subDetailProductModels.setText(bundle.getString("ProductModels"));
        subDetailProcess.setText(bundle.getString("Process"));
        subDetailDevice.setText(bundle.getString("Device"));
        subDetailStartPlanDate.setText(bundle.getString("PlanDate"));
        subDetailEndPlanDate.setText(bundle.getString("PlanDate"));
        subDetailDocno.setText(bundle.getString("Docno"));
    }

    //初始化控件
    private void initView(){
        subDetailProductName = findViewById(R.id.subDetailProductName);
        subDetailQuantity = findViewById(R.id.subDetailQuantity);
        subDetailProductCode = findViewById(R.id.subDetailProductCode);
        subDetailProductModels = findViewById(R.id.subDetailProductModels);
        subDetailProcess = findViewById(R.id.subDetailProcess);
        subDetailDevice = findViewById(R.id.subDetailDevice);
        subDetailStartPlanDate = findViewById(R.id.subDetailStartPlanDate);
        subDetailEndPlanDate = findViewById(R.id.subDetailEndPlanDate);
        subDetailDocno = findViewById(R.id.subDetailDocno);
        txtStatus = findViewById(R.id.txtStatus);
        imgQrcode = findViewById(R.id.imgQrcode);

        btnConnect = findViewById(R.id.btnConnect);
        btnPrint = findViewById(R.id.btnPrint);
        btnConnect.setOnClickListener(new commandClickListener());
        btnPrint.setOnClickListener(new commandClickListener());
    }

    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnConnect:
                    initPrinter();
                    break;
                case R.id.btnPrint:
                    btnLabelPrint();
                    break;
            }
        }
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

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){

    }

    private void createQrcode(String qrcode){
        w=300;
        h=300;
        try{
            if(qrcode == null || "".equals(qrcode) || qrcode.length()<1){
                return;
            }

            Hashtable<EncodeHintType,String> hints = new Hashtable<EncodeHintType,String>();
            hints.put(EncodeHintType.CHARACTER_SET,"utf-8");

            BitMatrix bitMatrix = new QRCodeWriter().encode(qrcode, BarcodeFormat.QR_CODE,w,h,hints);
            int[] pixels = new int[w*h];

            for(int y=0;y<h;y++){
                for(int x=0;x<w;x++){
                    if(bitMatrix.get(x,y)){
                        pixels[y*w+x] = 0xff000000;
                    }else{
                        pixels[y*w+x] = 0xffffffff;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels,0,w,0,0,w,h);
            imgQrcode.setImageBitmap(bitmap);

        }catch (WriterException e){
            e.printStackTrace();
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

    /*
     打印功能
    */
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
                            MyToast.myShow(SubDetailActivity.this,"连接失败",2,0);
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

    //初始化连接打印机IP和端口
    private void initPrinter(){
//        mIp = "192.168.30.100";  //凤一
        mIp = "192.168.2.50";  //薛峰
        mPort = "9100";

        WifiParameterConfig wifiParameterConfig = new WifiParameterConfig(this,mHandler,mIp,mPort);
        wifiParameterConfig.WifiConnect();
    }

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
                    Bitmap b = PrintContent.getBitmap(SubDetailActivity.this);

                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(PrintContent.getLabel(b));
                    mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    //预览标签
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
        MyToast.myShow(SubDetailActivity.this,"保存成功",2,0);
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
                    cpcl.addCGraphics(0,0,576, PrintContent.getBitmap(SubDetailActivity.this));
                    cpcl.addPrint();
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(cpcl.getCommand());
                } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC) {
                    LabelCommand labelCommand=new LabelCommand();
                    labelCommand.addSize(100,70);
                    labelCommand.addGap(2);
                    labelCommand.addCls();
                    // 打印图片  光栅位图  384代表打印图片像素  0代表打印模式
                    // 58mm打印机 可打印区域最大点数为 384 ，80mm 打印机 可打印区域最大点数为 576 例子为80mmd打印机
                    labelCommand.addBitmap(0,0, 576,PrintContent.getBitmap(SubDetailActivity.this));
                    labelCommand.addPrint(1);
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(labelCommand.getCommand());
                }else  if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC) {
                    EscCommand esc = new EscCommand();
                    esc.addInitializePrinter();
                    // 打印图片  光栅位图  384代表打印图片像素  0代表打印模式
                    // 58mm打印机 可打印区域最大点数为 384 ，80mm 打印机 可打印区域最大点数为 576 例子为80mmd打印机
                    esc.addRastBitImage(PrintContent.getBitmap(SubDetailActivity.this), 576, 0);
                    esc.addPrintAndLineFeed();
                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(esc.getCommand());
                }

            }
        });
    }

    private void tip(String msg){
        Message message=new Message();
        message.obj=msg;
        message.what= Constant.tip;
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
                        MyToast.myShow(SubDetailActivity.this,"成功断开连接",2,0);
                    }
                    break;
                case PRINTER_COMMAND_ERROR://打印机指令错误
                    MyToast.myShow(SubDetailActivity.this,"请选择正确的打印机指令",2,0);
                    break;
                case CONN_PRINTER://未连接打印机
                    MyToast.myShow(SubDetailActivity.this,"请先连接打印机",2,0);
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
                    MyToast.myShow(SubDetailActivity.this,"断开连接",2,0);
                    checkWifiConnThread.cancel();
                    checkWifiConnThread=null;
                    mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                    break;
                case tip:
                    String str=(String) msg.obj;
                    MyToast.myShow(SubDetailActivity.this,str,2,0);
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
}