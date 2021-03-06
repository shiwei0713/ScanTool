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
import com.hz.scantool.adapter.LoadingDialog;
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

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
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

public class SubDetailActivity extends AppCompatActivity {

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

    private String mIp;
    private String mPort;
    private int id = 0;
    private int w,h;

    private ThreadPool threadPool;
    private CheckWifiConnThread checkWifiConnThread;//wifi??????????????????

    private String strTitle;
    private String strQuantity;
    private String statusCode;
    private String statusDescription;
    private String strWorktime;
    private TextView subDetailProductName;
    private TextView subDetailQuantity;
    private TextView subDetailProductCode;
    private TextView subDetailProductModels;
    private TextView subDetailProcessId;
    private TextView subDetailProcess;
    private TextView subDetailDevice;
    private TextView subDetailStartPlanDate;
    private TextView subDetailEndPlanDate;
    private TextView subDetailDocno;
    private TextView subDetailProductDocno;
    private TextView subDetailLots;
    private ImageView imgQrcode;

    private Button btnSave;
    private Button btnPrint;
    private Button btnQc;
    private Button btnProduct;
    private Button btnError;
    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail);

        //???????????????
        initView();
        initBundle();

        //???????????????
        Toolbar toolbar=findViewById(R.id.subDetailToolBar);
        setSupportActionBar(toolbar);

        //??????????????????????????????????????????
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //???????????????
        initBroadcast();

        //?????????????????????
        createQrcode(subDetailDocno.getText().toString()+"_"+subDetailProcess.getText().toString()+"_"+ UserInfo.getUserId(getApplicationContext()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //???????????????????????????
        switch (item.getItemId()){
            case R.id.action_scan:
                //??????zxing????????????
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailActivity.this);
                intentIntegrator.setTimeout(5000);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //????????????
                intentIntegrator.initiateScan();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //?????????????????????
    private void initBundle(){
        strTitle = this.getResources().getString(R.string.master_detail1);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        subDetailProductName.setText(bundle.getString("ProductName"));
        strQuantity = bundle.getString("Quantity");
        strWorktime = bundle.getString("Worktime");
        subDetailQuantity.setText("");
        subDetailProductCode.setText(bundle.getString("ProductCode"));
        subDetailProductModels.setText(bundle.getString("ProductModels"));
        subDetailProcessId.setText(bundle.getString("ProcessId"));
        subDetailProcess.setText(bundle.getString("Process"));
        subDetailDevice.setText(bundle.getString("Device"));
        subDetailStartPlanDate.setText(bundle.getString("PlanDate"));
        subDetailEndPlanDate.setText(bundle.getString("PlanDate"));
        subDetailDocno.setText(bundle.getString("Docno"));
        subDetailLots.setText(bundle.getString("Lots"));
    }

    //???????????????
    private void initView(){
        subDetailProductName = findViewById(R.id.subDetailProductName);
        subDetailQuantity = findViewById(R.id.subDetailQuantity);
        subDetailProductCode = findViewById(R.id.subDetailProductCode);
        subDetailProductModels = findViewById(R.id.subDetailProductModels);
        subDetailProcessId = findViewById(R.id.subDetailProcessId);
        subDetailProcess = findViewById(R.id.subDetailProcess);
        subDetailDevice = findViewById(R.id.subDetailDevice);
        subDetailStartPlanDate = findViewById(R.id.subDetailStartPlanDate);
        subDetailEndPlanDate = findViewById(R.id.subDetailEndPlanDate);
        subDetailDocno = findViewById(R.id.subDetailDocno);
        subDetailProductDocno = findViewById(R.id.subDetailProductDocno);
        subDetailLots = findViewById(R.id.subDetailLots);
        imgQrcode = findViewById(R.id.imgQrcode);

        btnSave = findViewById(R.id.btnSave);
        btnPrint = findViewById(R.id.btnPrint);
        btnQc = findViewById(R.id.btnQc);
        btnProduct = findViewById(R.id.btnProduct);
        btnError = findViewById(R.id.btnError);
        btnSave.setOnClickListener(new commandClickListener());
        btnPrint.setOnClickListener(new commandClickListener());
        btnQc.setOnClickListener(new commandClickListener());
        btnProduct.setOnClickListener(new commandClickListener());
        btnError.setOnClickListener(new commandClickListener());
    }

    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSave:
                    saveDataToT100("insert");
                    break;
                case R.id.btnPrint:
                    saveDataToT100("print");
                    break;
                case R.id.btnQc:
                    saveDataToT100("check");
                    break;
                case R.id.btnProduct:
                    break;
                case R.id.btnError:
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
                MyToast.myShow(this,"????????????,???????????????"+qrContent,0,0);
            }
        }
    }

    //??????????????????
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

    private void saveDataToT100(String action){
        //???????????????
        loadingDialog = new LoadingDialog(SubDetailActivity.this,"???????????????",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "WorkReportRequestGen";
                String qcstatus = "PY";

                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"sffb_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffbdocdt\" value=\""+subDetailStartPlanDate.getText().toString()+"\"/&gt;\n"+
                        "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffb004\" value=\""+ strWorktime +"\"/&gt;\n"+  //??????
                        "&lt;Field name=\"sffb005\" value=\""+ subDetailDocno.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffbseq\" value=\""+ subDetailProcessId.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffb010\" value=\""+ subDetailDevice.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffb029\" value=\""+ subDetailProductCode.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffb017\" value=\""+ subDetailQuantity.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"process\" value=\""+ subDetailProcess.getText().toString() +"\"/&gt;\n"+  //??????
                        "&lt;Field name=\"lots\" value=\""+ subDetailLots.getText().toString() +"\"/&gt;\n"+  //??????
                        "&lt;Field name=\"sffbdocno\" value=\""+ subDetailProductDocno.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //????????????
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sffyucseq\" value=\"1.0\"/&gt;\n"+
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
                mapResponseList = t100ServiceHelper.getT100ResponseDocno(strResponse,"docno");

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
                    MyToast.myShow(SubDetailActivity.this,"??????????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailActivity.this,"????????????",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    String strDocno="";

                    if(mapResponseList.size()> 0) {
                        for (Map<String, Object> mResponse : mapResponseList) {
                            strDocno = mResponse.get("Docno").toString();
                            subDetailProductDocno.setText(strDocno);
                        }
                    }
                    MyToast.myShow(SubDetailActivity.this, statusDescription, 1, 1);
                }else{
                    MyToast.myShow(SubDetailActivity.this, statusDescription, 0, 1);
                }
                loadingDialog.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

//        //??????????????????????????????
//        DeviceConnFactoryManager [] deviceConnFactoryManagers;
//        deviceConnFactoryManagers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
//        for (int i = 0; i < 4; i++) {
//            if (deviceConnFactoryManagers[i] != null && deviceConnFactoryManagers[i].getConnState()) {
//                txtStatus.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
//                break;
//            } else {
//                txtStatus.setText(getString(R.string.str_conn_state_disconnect));
//            }
//        }
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
//                                txtStatus.setText(getString(R.string.str_conn_state_disconnect));
                            }
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
//                            txtStatus.setText(getString(R.string.str_conn_state_connecting));
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
//                            txtStatus.setText(getString(R.string.str_conn_state_connected) + "\n" + getConnDeviceInfo());
                            break;
                        case CONN_STATE_FAILED:
                            MyToast.myShow(SubDetailActivity.this,"????????????",2,0);
//                            txtStatus.setText(getString(R.string.str_conn_state_disconnect));
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
        mIp = "192.168.2.50";  //??????
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

//    //????????????
//    public void btnLabelPrint() {
//        threadPool = ThreadPool.getInstantiation();
//        threadPool.addSerialTask(new Runnable() {
//            @Override
//            public void run() {
//                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
//                        !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
//                    mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
//                    return;
//                }
//                if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC) {
//                    Bitmap b = PrintContent.getBitmap(SubDetailActivity.this);
//
//                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(PrintContent.getLabel(b));
//                    mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
//                } else {
//                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
//                }
//            }
//        });
//    }

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
        MyToast.myShow(SubDetailActivity.this,"????????????",2,0);
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
//                    cpcl.addCGraphics(0,0,576, PrintContent.getBitmap(SubDetailActivity.this));
//                    cpcl.addPrint();
//                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(cpcl.getCommand());
//                } else if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC) {
//                    LabelCommand labelCommand=new LabelCommand();
//                    labelCommand.addSize(100,70);
//                    labelCommand.addGap(2);
//                    labelCommand.addCls();
//                    // ????????????  ????????????  384????????????????????????  0??????????????????
//                    // 58mm????????? ?????????????????????????????? 384 ???80mm ????????? ?????????????????????????????? 576 ?????????80mmd?????????
//                    labelCommand.addBitmap(0,0, 576,PrintContent.getBitmap(SubDetailActivity.this));
//                    labelCommand.addPrint(1);
//                    DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(labelCommand.getCommand());
//                }else  if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC) {
//                    EscCommand esc = new EscCommand();
//                    esc.addInitializePrinter();
//                    // ????????????  ????????????  384????????????????????????  0??????????????????
//                    // 58mm????????? ?????????????????????????????? 384 ???80mm ????????? ?????????????????????????????? 576 ?????????80mmd?????????
//                    esc.addRastBitImage(PrintContent.getBitmap(SubDetailActivity.this), 576, 0);
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

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONN_STATE_DISCONN://????????????
                    DeviceConnFactoryManager deviceConnFactoryManager=DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id];
                    if (deviceConnFactoryManager!= null&&deviceConnFactoryManager.getConnState()) {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort(id);
                        MyToast.myShow(SubDetailActivity.this,"??????????????????",2,0);
                    }
                    break;
                case PRINTER_COMMAND_ERROR://?????????????????????
                    MyToast.myShow(SubDetailActivity.this,"?????????????????????????????????",2,0);
                    break;
                case CONN_PRINTER://??????????????????
                    MyToast.myShow(SubDetailActivity.this,"?????????????????????",2,0);
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
                    MyToast.myShow(SubDetailActivity.this,"????????????",2,0);
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