package com.hz.scantool;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gprinter.command.CpclCommand;
import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.helper.T100ServiceHelper;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.hz.scantool.printer.Constant.ACTION_USB_PERMISSION;
import static com.hz.scantool.printer.Constant.MESSAGE_UPDATE_PARAMETER;
import static com.hz.scantool.printer.Constant.tip;
import static com.hz.scantool.printer.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.hz.scantool.printer.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class PrinterActivity extends AppCompatActivity {

    private static final String TAG = SubActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 0x004;
    /**
     * ??????????????????
     */
    private static final int CONN_STATE_DISCONN = 0x007;
    /**
     * ???????????????????????????
     */
    private static final int PRINTER_COMMAND_ERROR = 0x008;
    private static final int CONN_PRINTER = 0x12;
    private static final int PERIOD = 3000;
    private static final int DELAY = 100;
    private Disposable mDisposable;

    private String mIp;
    private String mPort;
    private int id = 0;
    private int w,h;

    private ThreadPool threadPool;
    private CheckWifiConnThread checkWifiConnThread;//wifi??????????????????
    private Button btnConnect;
    private Button btnStartJob;
    private Button btnStopJob;
    private Button btnPrint;
    private TextView txtStatus;
    private TextView txtMessage;
    private EditText txtIp;
    private ImageView imgQrcode;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);

        //???????????????
        initView();

        //???????????????
        initBroadcast();

        //???????????????????????????
        createQrcode("TEST");
    }

    private void initView(){
        btnConnect = findViewById(R.id.btnConnect);
        btnPrint = findViewById(R.id.btnPrint);
        btnStartJob = findViewById(R.id.btnStartJob);
        btnStopJob = findViewById(R.id.btnStopJob);
        txtStatus = findViewById(R.id.txtStatus);
        imgQrcode = findViewById(R.id.imgQrcode);
        txtMessage = findViewById(R.id.txtMessage);
        txtIp = findViewById(R.id.txtIp);

        btnConnect.setOnClickListener(new commandClickListener());
        btnPrint.setOnClickListener(new commandClickListener());
        btnStartJob.setOnClickListener(new commandClickListener());
        btnStopJob.setOnClickListener(new commandClickListener());
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
                case R.id.btnStartJob:
                    startJob();
                    break;
                case R.id.btnStopJob:
                    stopJob();
                    break;
            }
        }
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

    private void startJob(){
        mDisposable = Observable.interval(DELAY,PERIOD, TimeUnit.MILLISECONDS)
                .map((aLong -> aLong+1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> btnLabelPrint());
        txtMessage.setText("?????????????????????");
    }

    private void stopJob(){
        if(mDisposable!=null){
            mDisposable.dispose();
        }
        txtMessage.setText("?????????????????????");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //??????????????????????????????
        DeviceConnFactoryManager[] deviceConnFactoryManagers;
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
     ????????????
    */
    private void initBroadcast() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);//USB??????????????????
        filter.addAction(ACTION_USB_DEVICE_DETACHED);//USB?????????
        filter.addAction(ACTION_QUERY_PRINTER_STATE);//?????????????????????????????????????????????????????????
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE);//????????????????????????
        filter.addAction(ACTION_USB_DEVICE_ATTACHED);//USB?????????
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                //????????????
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
                            MyToast.myShow(PrinterActivity.this,"????????????",2,0);
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

    //????????????????????????IP?????????
    private void initPrinter(){
//        mIp = "192.168.30.100";  //??????
//        mIp = "192.168.2.50";  //??????
        mIp = txtIp.getText().toString();
        mPort = "9100";

        WifiParameterConfig wifiParameterConfig = new WifiParameterConfig(this,mHandler,mIp,mPort);
        wifiParameterConfig.WifiConnect();
    }

    //????????????????????????
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
                checkWifiConnThread=new CheckWifiConnThread(deviceConnFactoryManager.getIp(),mHandler);//????????????WiFi??????
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

    //??????????????????
    private void getLableData(){
        //?????????T100?????????
        String webServiceName = "PrintLabelGet";

        //?????????????????????
        T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
        String requestBody = "&lt;Document&gt;\n"+
                "&lt;RecordSet id=\"1\"&gt;\n"+
                "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                "&lt;Record&gt;\n"+
                "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                "&lt;Field name=\"ip\" value=\""+mIp+"\"/&gt;\n"+
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

        try{
            String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
            mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
            mapResponseList = t100ServiceHelper.getT100JsonLabelData(strResponse,"workorder");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    //????????????
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
                    getLableData();
                    if(mapResponseList.size()>0){
                        Bitmap b = PrintContent.getBitmap(PrinterActivity.this,mapResponseList);
                        String qrcode = (String)mapResponseList.get(0).get("Qrcode");

                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(PrintContent.getLabel(b,qrcode));
                    }
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    //????????????
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
        MyToast.myShow(PrinterActivity.this,"????????????",2,0);
    }

//    //??????xml??????
//    private void printLabel(){
//
//        //?????????????????????
////        setPrinterMode();
//
//        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
//                !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
//            mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
//            return;
//        }
//
//        threadPool = ThreadPool.getInstantiation();
//        threadPool.addSerialTask(new Runnable() {
//            @Override
//            public void run() {
//
//                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.CPCL) {
//                    CpclCommand cpcl=new CpclCommand();
//                    cpcl.addInitializePrinter(1500,1);
//                    // ????????????  ????????????  384????????????????????????  0??????????????????
//                    // 58mm????????? ?????????????????????????????? 384 ???80mm ????????? ?????????????????????????????? 576 ?????????80mmd?????????
//                    cpcl.addCGraphics(0,0,576, PrintContent.getBitmap(PrinterActivity.this));
//                    cpcl.addPrint();
//                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(cpcl.getCommand());
//                } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC) {
//                    LabelCommand labelCommand=new LabelCommand();
//                    labelCommand.addSize(100,70);
//                    labelCommand.addGap(2);
//                    labelCommand.addCls();
//                    // ????????????  ????????????  384????????????????????????  0??????????????????
//                    // 58mm????????? ?????????????????????????????? 384 ???80mm ????????? ?????????????????????????????? 576 ?????????80mmd?????????
//                    labelCommand.addBitmap(0,0, 576,PrintContent.getBitmap(PrinterActivity.this));
//                    labelCommand.addPrint(1);
//                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(labelCommand.getCommand());
//                }else  if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC) {
//                    EscCommand esc = new EscCommand();
//                    esc.addInitializePrinter();
//                    // ????????????  ????????????  384????????????????????????  0??????????????????
//                    // 58mm????????? ?????????????????????????????? 384 ???80mm ????????? ?????????????????????????????? 576 ?????????80mmd?????????
//                    esc.addRastBitImage(PrintContent.getBitmap(PrinterActivity.this), 576, 0);
//                    esc.addPrintAndLineFeed();
//                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(esc.getCommand());
//                }
//
//            }
//        });
//    }

    private void tip(String msg){
        Message message=new Message();
        message.obj=msg;
        message.what= Constant.tip;
        mHandler.sendMessage(message);
    }

    private Handler dHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONN_STATE_DISCONN://????????????
                    DeviceConnFactoryManager deviceConnFactoryManager=DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id];
                    if (deviceConnFactoryManager!= null&&deviceConnFactoryManager.getConnState()) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
                        MyToast.myShow(PrinterActivity.this,"??????????????????",2,0);
                    }
                    break;
                case PRINTER_COMMAND_ERROR://?????????????????????
                    MyToast.myShow(PrinterActivity.this,"?????????????????????????????????",2,0);
                    break;
                case CONN_PRINTER://??????????????????
                    MyToast.myShow(PrinterActivity.this,"?????????????????????",2,0);
                    break;
                case MESSAGE_UPDATE_PARAMETER:
                    String strIp = msg.getData().getString("Ip");
                    String strPort = msg.getData().getString("Port");
                    //?????????????????????
                    new DeviceConnFactoryManager.Build()
                            //????????????????????????
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                            //????????????IP??????
                            .setIp(strIp)
                            //????????????ID?????????????????????????????????
                            .setId(id)
                            //??????????????????????????????
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
                case CheckWifiConnThread.PING_SUCCESS://WIfi????????????\
                    Log.e(TAG,"wifi connect success!");
                    break;
                case CheckWifiConnThread.PING_FAIL://WIfI????????????
                    Log.e(TAG,"wifi connect fail!");
                    MyToast.myShow(PrinterActivity.this,"????????????",2,0);
                    checkWifiConnThread.cancel();
                    checkWifiConnThread=null;
                    mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                    break;
                case tip:
                    String str=(String) msg.obj;
                    MyToast.myShow(PrinterActivity.this,str,2,0);
                    break;
                default:
                    new DeviceConnFactoryManager.Build()
                            //????????????????????????
                            .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI)
                            //????????????IP??????
                            .setIp("192.168.2.227")
                            //????????????ID?????????????????????????????????
                            .setId(id)
                            //??????????????????????????????
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