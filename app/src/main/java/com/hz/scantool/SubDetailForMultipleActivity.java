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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MultipleDetailAdapter;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.database.MergeLabelEntity;
import com.hz.scantool.database.ProductEntity;
import com.hz.scantool.dialog.ShowAlertDialog;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class SubDetailForMultipleActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private static final int MERGEPACKAGE = 1001;
    private static final int DIFFQTY = 1002;
    private static final String TYPE_MEGERE = "MEGERE";
    private static final String TYPE_DIFF = "DIFF";

    private HzDb hzDb;
    private String dataBaseName = "HzDb";

    private String strTitle;
    private String strFlag;
    private String strQrcode;
    private String strProcessId,strProcess,strDevice;
    private String strModStatus;
    private String strOperateCount;
    private String strPrintCount;
    private String strStartStatus;
    private String strCheckStatus;
    private String strUpStatus;
    private String strErrorStartStatus;
    private String strErrorStopStatus;
    private String strVersion;
    private String strStartTime;
    private String strCheckTime;
    private String strUpTime;
    private String strErrorTime;
    private String strProductTotal;
    private String strErrorLots;
    private String strProcessEnd;
    private String mRecordSet="";
    private String strGroupId;
    private String strProcessInitId;
    private String strProcessInit;
    private String strCheckMaterial;
    private String strInputStatus;
    private String sConnectProcessId,sConnectProcess;
    private int id = 0;
    private int w,h;
    private int iTotal = 0;
    private int iDiffTotal = 0;
    boolean isHide = false;

    private EditText editPackages;
    private LinearLayout viewInputBasic;

    private TextView txtMultipleInputCount;
    private TextView txtMultiplePrintCount;
    private TextView txtMultiplePlanNo;
    private TextView txtMultipleModle;
    private TextView txtMultipleVersion;
    private TextView txtMultipleErrorCount;
    private TextView txtMultipleQcCount;
    private TextView txtMultipleGroupId;

    private TextView txtMultipleSum;
    private TextView txtMultipleStartTime;
    private TextView txtMultipleCheckTime;
    private TextView txtMultipleProductTime;
    private TextView txtMultipleErrorTime;
    private TextView txtMultipleProcessEnd;

    private ImageView imgMultipleQrcode;
    private ListView subMultipleView;
    private ImageView imgMultipleStartStatus;
    private ImageView imgMultipleQcStatus;
    private ImageView imgMultipleProductStatus;
    private ImageView imgMultipleErrorBeginStatus;
    private ImageView imgMultipleErrorEndStatus;
    private Button btnMultipleStart;
    private Button btnMultipleEnd;
    private Button btnMultipleQc;
    private Button btnMultipleProduct;
    private Button btnMultipleError;
    private Button btnMultipleSave;
    private Button btnMultiplePrint;
    private Button btnMultiplePrintMaterial;
    private Button btnMultipleTransate,btnMore,btnLess;

    private String statusCode;
    private String statusDescription,sMessage;
    private String strWorkTime;
    private String sSocketDocno,sServerIp;
    private boolean isMore;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapList;
    private LoadingDialog loadingDialog;
    private MultipleDetailAdapter multipleDetailAdapter;
    private ProductEntity productEntity;
    private List<ProductEntity> productEntityList;
    private List<MergeLabelEntity> mergeLabelEntityList,mergeLabelEntityDiffQtyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_for_multiple);

        //初始化
        initView();
        initBundle();
        initDataBase();
        deleteDbData();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subDetailMultipleToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        createQrcode(strFlag+"_"+txtMultipleVersion.getText().toString()+"_"+ UserInfo.getUserId(getApplicationContext())+"_"+strProcessId+"_"+strProcess+"_"+txtMultipleInputCount.getText().toString());

        //获取显示数据
        getMultipleDetailData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu_info,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailForMultipleActivity.this);
//                intentIntegrator.setTimeout(5000);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //开始扫描
                intentIntegrator.initiateScan();
                break;
            case android.R.id.home:
                finish();
                break;
            case R.id.action_info:
                hideView();
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

    /*
     *后台操作，创建数据库
     */
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this,HzDb.class,dataBaseName).build();
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

    //初始化传入参数
    private void initBundle(){
        strTitle = this.getResources().getString(R.string.master_detail1);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strFlag = bundle.getString("Flag");
        strProcessId = bundle.getString("ProcessId");
        strProcess = bundle.getString("Process");
        strDevice = bundle.getString("Device");
        strModStatus = bundle.getString("ModStatus");
        strOperateCount = bundle.getString("OperateCount");
        strPrintCount = bundle.getString("PrintCount");
        strStartStatus = bundle.getString("StartStatus");
        strCheckStatus = bundle.getString("CheckStatus");
        strUpStatus = bundle.getString("UpStatus");
        strErrorStartStatus = bundle.getString("ErrorStartStatus");
        strErrorStopStatus = bundle.getString("ErrorStopStatus");
        strVersion  = bundle.getString("Version");
        strStartTime = bundle.getString("StartTime");
        strCheckTime = bundle.getString("CheckTime");
        strUpTime = bundle.getString("UpTime");
        strErrorTime = bundle.getString("ErrorTime");
        strProductTotal = bundle.getString("ProductTotal");
        strGroupId = bundle.getString("GroupId");
        strProcessEnd = bundle.getString("ProcessEnd");
        strProcessInitId = bundle.getString("ProcessInitId");
        strProcessInit = bundle.getString("ProcessInit");
        strCheckMaterial = bundle.getString("CheckMaterial");
        strInputStatus = bundle.getString("InputStatus");

        strStartTime = strStartTime.replace("/","\n");
        strCheckTime = strCheckTime.replace("/","\n");
        strUpTime = strUpTime.replace("/","\n");
        strErrorTime = strErrorTime.replace("/","\n");
        strProductTotal = strProductTotal.replace("/","\n");

        if(strOperateCount.equals("")||strOperateCount.isEmpty()){
            strOperateCount = "0";
        }
        if(strPrintCount.equals("")||strPrintCount.isEmpty()){
            strPrintCount = "0";
        }

        //开始生产状态
        if(strStartStatus.equals("0")||strStartStatus.isEmpty()){
            imgMultipleStartStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imgMultipleStartStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }

        //报首检状态
        if(strCheckStatus.equals("0")||strCheckStatus.isEmpty()){
            imgMultipleQcStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imgMultipleQcStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }

        //上料检核状态
        if(strUpStatus.equals("0")||strUpStatus.isEmpty()){
            imgMultipleProductStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imgMultipleProductStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }

        //异常开始状态
        if(strErrorStartStatus.equals("0")||strErrorStartStatus.isEmpty()){
            imgMultipleErrorBeginStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imgMultipleErrorBeginStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }

        //异常结束状态
        if(strErrorStopStatus.equals("0")||strErrorStopStatus.isEmpty()){
            imgMultipleErrorEndStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imgMultipleErrorEndStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }

        int iOperateCount = Integer.valueOf(strOperateCount) + 1;
        int iPrintCount = Integer.valueOf(strPrintCount) + 1;

        txtMultiplePlanNo.setText(strFlag);
        txtMultipleModle.setText(strModStatus);
        txtMultipleInputCount.setText(String.valueOf(iOperateCount));
        txtMultiplePrintCount.setText(String.valueOf(iPrintCount));
        txtMultipleVersion.setText(strVersion);
        txtMultipleErrorCount.setText(strErrorStartStatus);
        txtMultipleQcCount.setText(strCheckStatus);
        txtMultipleProcessEnd.setText(strProcessEnd);

        txtMultipleSum.setText(strProductTotal);
        txtMultipleStartTime.setText(strStartTime);
        txtMultipleCheckTime.setText(strCheckTime);
        txtMultipleProductTime.setText(strUpTime);
        txtMultipleErrorTime.setText(strErrorTime);
        txtMultipleGroupId.setText(strGroupId);
    }

    //初始化控件
    private void initView(){
        txtMultipleInputCount = findViewById(R.id.txtMultipleInputCount);
        txtMultiplePrintCount = findViewById(R.id.txtMultiplePrintCount);
        txtMultiplePlanNo = findViewById(R.id.txtMultiplePlanNo);
        txtMultipleModle = findViewById(R.id.txtMultipleModle);
        imgMultipleQrcode = findViewById(R.id.imgMultipleQrcode);
        imgMultipleStartStatus = findViewById(R.id.imgMultipleStartStatus);
        imgMultipleQcStatus = findViewById(R.id.imgMultipleQcStatus);
        imgMultipleProductStatus = findViewById(R.id.imgMultipleProductStatus);
        imgMultipleErrorBeginStatus = findViewById(R.id.imgMultipleErrorBeginStatus);
        imgMultipleErrorEndStatus = findViewById(R.id.imgMultipleErrorEndStatus);
        subMultipleView = findViewById(R.id.subMultipleView);
        txtMultipleVersion = findViewById(R.id.txtMultipleVersion);
        txtMultipleErrorCount = findViewById(R.id.txtMultipleErrorCount);
        txtMultipleQcCount = findViewById(R.id.txtMultipleQcCount);
        editPackages = findViewById(R.id.editPackages);
        viewInputBasic = findViewById(R.id.viewInputBasic);

        btnMultipleStart = findViewById(R.id.btnMultipleStart);
        btnMultipleEnd = findViewById(R.id.btnMultipleEnd);
        btnMultipleQc = findViewById(R.id.btnMultipleQc);
        btnMultipleProduct = findViewById(R.id.btnMultipleProduct);
        btnMultipleError = findViewById(R.id.btnMultipleError);
        btnMultipleSave = findViewById(R.id.btnMultipleSave);
        btnMultiplePrint = findViewById(R.id.btnMultiplePrint);
        btnMultiplePrintMaterial = findViewById(R.id.btnMultiplePrintMaterial);

        txtMultipleSum = findViewById(R.id.txtMultipleSum);
        txtMultipleStartTime = findViewById(R.id.txtMultipleStartTime);
        txtMultipleCheckTime = findViewById(R.id.txtMultipleCheckTime);
        txtMultipleProductTime = findViewById(R.id.txtMultipleProductTime);
        txtMultipleErrorTime = findViewById(R.id.txtMultipleErrorTime);
        txtMultipleGroupId = findViewById(R.id.txtMultipleGroupId);
        txtMultipleProcessEnd = findViewById(R.id.txtMultipleProcessEnd);

        btnMultipleTransate = findViewById(R.id.btnMultipleTransate);
        btnMore = findViewById(R.id.btnMore);
        btnLess = findViewById(R.id.btnLess);

        btnMultipleStart.setOnClickListener(new commandClickListener());
        btnMultipleEnd.setOnClickListener(new commandClickListener());
        btnMultipleQc.setOnClickListener(new commandClickListener());
        btnMultipleProduct.setOnClickListener(new commandClickListener());
        btnMultipleError.setOnClickListener(new commandClickListener());
        btnMultipleSave.setOnClickListener(new commandClickListener());
        btnMultiplePrint.setOnClickListener(new commandClickListener());
        btnMultiplePrintMaterial.setOnClickListener(new commandClickListener());
        btnMultipleTransate.setOnClickListener(new commandClickListener());
        btnMore.setOnClickListener(new commandClickListener());
        btnLess.setOnClickListener(new commandClickListener());

        //初始化班次
        setWorktime();
    }

    /**
    *描述: 显示辅助信息
    *日期：2022/10/10
    **/
    private void hideView(){

        if(!isHide){
            viewInputBasic.setVisibility(View.GONE);
            subMultipleView.setVisibility(View.GONE);
            isHide = true;
        }else{
            viewInputBasic.setVisibility(View.VISIBLE);
            subMultipleView.setVisibility(View.VISIBLE);
            isHide = false;
        }
    }

    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            //连线生产工序
            if(multipleDetailAdapter!=null){
                sConnectProcessId = multipleDetailAdapter.getItemValue(0,"ProcessId");
                sConnectProcess = multipleDetailAdapter.getItemValue(0,"Process");
            }

            switch (view.getId()){
                case R.id.btnMultipleStart: //开始生产
                    mRecordSet = "";
                    saveMultipleToT100("insert","10","B","");
                    break;
                case R.id.btnMultipleEnd:   //异常结束  //S修改为X
                    mRecordSet = "";
                    strErrorLots = txtMultipleErrorCount.getText().toString();
                    saveMultipleToT100("insert","14","X","");
                    break;
                case R.id.btnMultipleSave:  //保存数据
                    //注释保存直接写入服务器，修改为保存至本地，点击打印再写入服务器
                    mRecordSet = "";
                    strErrorLots = txtMultipleErrorCount.getText().toString();
//                    saveMultipleToT100("save","","V","");
                    saveData("save","19","V","");
                    break;
                case R.id.btnMultiplePrint: //打印数据
                    mRecordSet = "";
                    strErrorLots = txtMultipleErrorCount.getText().toString();
//                    saveMultipleToT100("print","","P","");
                    savePrintToT100("print","19","P");
                    break;
                case R.id.btnMultipleQc:    //报首检已报首检:F,首检合格：K
                    mRecordSet = "";
                    strErrorLots = txtMultipleQcCount.getText().toString();
                    saveMultipleToT100("insert","11","F","");
                    break;
                case R.id.btnMultipleProduct:   //上料检核
                    mRecordSet = "";
                    checkMaterial();
                    break;
                case R.id.btnMultipleError:     //异常开始
                    mRecordSet = "";
                    strErrorLots = txtMultipleErrorCount.getText().toString();
                    saveMultipleToT100("insert","13","S","");
                    break;
                case R.id.btnMultiplePrintMaterial:    //打印余料标签
                    mRecordSet = "";
                    strErrorLots = txtMultipleErrorCount.getText().toString();
                    saveMultipleToT100("printmaterial","19","I","");
                    break;
                case R.id.btnMultipleTransate:  //尾数合箱
                    setLabelMerge();
                    break;
                case R.id.btnMore:   //来料差-多
                    isMore = true;
                    setDiff(1,btnMore.getText().toString());
                    break;
                case R.id.btnLess:  //来料差-少
                    isMore = false;
                    setDiff(-1,btnLess.getText().toString());
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
        }else if(requestCode==MERGEPACKAGE && resultCode == MERGEPACKAGE){
            iTotal = data.getIntExtra("total",0);
            String sBtnTitle = getResources().getString(R.string.list_detail_button22);
            String sTotle = String.valueOf(iTotal);
            btnMultipleTransate.setText(sBtnTitle+"("+sTotle+")");
        }else if(requestCode==DIFFQTY && resultCode == DIFFQTY){
            iDiffTotal = data.getIntExtra("total",0);
            String sTotle = String.valueOf(iDiffTotal);
            String sBtnTitle = "";
            if(!isMore){
                sBtnTitle = getResources().getString(R.string.query_less);
                btnLess.setText(sBtnTitle+"("+sTotle+")");
            }else{
                sBtnTitle = getResources().getString(R.string.query_more);
                btnMore.setText(sBtnTitle+"("+sTotle+")");
            }
        }
    }

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){
        //解析二维码
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
//            saveMultipleToT100("insert","12","M",qrContent);   //取消上料检核扫描
            //标签确认
            updQrcodeData("GX","12","C",qrContent);
        }
    }

    /**
    *描述: 上料检核
    *日期：2022/6/12
    **/
    private void checkMaterial(){

        //获取工单信息
        int iCount = multipleDetailAdapter.getCount();
        String strProductDocno = "";

        for(int i= 0;i<iCount;i++){
            if(strProductDocno.equals("")||strProductDocno.isEmpty()){
                strProductDocno = multipleDetailAdapter.getItemValue(i,"Docno");
            }else{
                strProductDocno = strProductDocno + ","+multipleDetailAdapter.getItemValue(i,"Docno");
            }
        }

        //打开上料检核界面
        Intent intent = new Intent(SubDetailForMultipleActivity.this,CheckMaterialActivity.class);
        Bundle bundle = new Bundle();

        //连线生产工序
        String sProcessEnd = txtMultipleProcessEnd.getText().toString();
        if(sProcessEnd.equals("Y")){
            strProcessId = strProcessInitId;
            strProcess = strProcessInit;
        }

        bundle.putString("Docno",strFlag);
        bundle.putString("Version",strVersion);
        bundle.putString("ProcessId",strProcessId);
        bundle.putString("Process",strProcess);
        bundle.putString("Device",strDevice);
        bundle.putString("ProductDocno",strProductDocno);
        bundle.putString("PlanSeq",txtMultipleInputCount.getText().toString());
        bundle.putString("WorkTime",strWorkTime);
        bundle.putString("ProcessEnd",txtMultipleProcessEnd.getText().toString());
        bundle.putString("ConnectProcessId",sConnectProcessId);
        bundle.putString("ConnectProcess",sConnectProcess);
        bundle.putString("CheckMaterial",strCheckMaterial);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
    *描述: 来料差异
    *日期：2022/10/12
    **/
    private void setDiff(int type,String btnTitle){
        //获取工单信息
        int iCount = multipleDetailAdapter.getCount();
        String strProductDocno = "";
        String strProductCode = "";

        for(int i= 0;i<iCount;i++){
            //工单单号
            if(strProductDocno.equals("")||strProductDocno.isEmpty()){
                strProductDocno = multipleDetailAdapter.getItemValue(i,"Docno");
            }else{
                strProductDocno = strProductDocno + ","+multipleDetailAdapter.getItemValue(i,"Docno");
            }

            //料件编码
            if(strProductCode.equals("")||strProductCode.isEmpty()){
                strProductCode = multipleDetailAdapter.getItemValue(i,"ProductCode");
            }else{
                strProductCode = strProductCode + ","+multipleDetailAdapter.getItemValue(i,"ProductCode");
            }
        }

        //打开尾数合箱界面
        Intent intent = new Intent(SubDetailForMultipleActivity.this,SubDetailForDiffQtyActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString("Docno",strFlag);
        bundle.putString("Version",strVersion);
        bundle.putString("ProcessId",strProcessId);
        bundle.putString("Process",strProcess);
        bundle.putString("ProductDocno",strProductDocno);
        bundle.putString("ProductCode",strProductCode);
        bundle.putString("PlanSeq",txtMultipleInputCount.getText().toString());
        bundle.putInt("Type",type);
        bundle.putString("TypeDesc",TYPE_DIFF);
        bundle.putString("BtnTitle",btnTitle);
        intent.putExtras(bundle);
        startActivityForResult(intent,DIFFQTY);
    }

    /**
    *描述: 尾数合箱
    *日期：2022/10/10
    **/
    private void setLabelMerge(){
        //获取工单信息
        int iCount = multipleDetailAdapter.getCount();
        String strProductDocno = "";
        String strProductCode = "";

        for(int i= 0;i<iCount;i++){
            //工单单号
            if(strProductDocno.equals("")||strProductDocno.isEmpty()){
                strProductDocno = multipleDetailAdapter.getItemValue(i,"Docno");
            }else{
                strProductDocno = strProductDocno + ","+multipleDetailAdapter.getItemValue(i,"Docno");
            }

            //料件编码
            if(strProductCode.equals("")||strProductCode.isEmpty()){
                strProductCode = multipleDetailAdapter.getItemValue(i,"ProductCode");
            }else{
                strProductCode = strProductCode + ","+multipleDetailAdapter.getItemValue(i,"ProductCode");
            }
        }

        //打开尾数合箱界面
        Intent intent = new Intent(SubDetailForMultipleActivity.this,SubDetailForPackageActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString("Docno",strFlag);
        bundle.putString("Version",strVersion);
        bundle.putString("ProcessId",strProcessId);
        bundle.putString("Process",strProcess);
        bundle.putString("ProductDocno",strProductDocno);
        bundle.putString("ProductCode",strProductCode);
        bundle.putString("PlanSeq",txtMultipleInputCount.getText().toString());
        bundle.putString("TypeDesc",TYPE_MEGERE);
        intent.putExtras(bundle);
//        startActivity(intent);
        startActivityForResult(intent,MERGEPACKAGE);
    }

    /**
    *描述: 创建二维码，用于模具安装和PQC检验扫码确认生产任务单信息
    *日期：2022/6/10
    **/
    private void createQrcode(String qrcode){
        w=250;
        h=250;
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
            imgMultipleQrcode.setImageBitmap(bitmap);

        }catch (WriterException e){
            e.printStackTrace();
        }
    }

    //获取清单
    private void getMultipleDetailData(){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForMultipleActivity.this,"正在刷新",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductListGet";
                String strwhere = " sfaauc014='"+strFlag+"' AND sfaauc007='"+strProcessId+"' AND sfaauc001="+strVersion;
                String strhaving = "";
                if(strProcessEnd.equals("Y")){
                    strwhere = " sfaauc014='"+strFlag+"' AND sfaauc001="+strVersion;
                    strhaving = " HAVING listagg(sfaauc007,'/') WITHIN GROUP(ORDER BY sfaauc007)='"+strProcessId+"'";
                }

                String strType = "21";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"gconnect\" value=\""+ strProcessEnd +"\"/&gt;\n"+
                        "&lt;Field name=\"having\" value=\""+ strhaving +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonProductDetailData(strResponse,"workorder");

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
                            MyToast.myShow(SubDetailForMultipleActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailForMultipleActivity.this,"无备料数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                multipleDetailAdapter = new MultipleDetailAdapter(mapResponseList,getApplicationContext(),strModStatus);
                subMultipleView.setAdapter(multipleDetailAdapter);

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    private void setWorktime(){
        long timeCurrentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = simpleDateFormat.format(timeCurrentTimeMillis);
        strWorkTime = "晚班";

        try{
            Date date1 = simpleDateFormat.parse(currentTime);
            Date date2 = simpleDateFormat.parse("07:00:00");
            Date date3 = simpleDateFormat.parse("20:00:00");
            if(date1.getTime()>=date2.getTime() && date1.getTime()<=date3.getTime()){
                strWorkTime = "白班";
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    private boolean checkListItemQuantity(String strAction){
        if(strAction.equals("insert") || strAction.equals("printmaterial")){
            return true;
        }

        if(strInputStatus.equals("N")&&strProcessEnd.equals("Y")){
            MyToast.myShow(SubDetailForMultipleActivity.this,"连线生产只末序打印标签",2,0);
            return false;
        }

        for(int i= 0;i<multipleDetailAdapter.getCount();i++){
            LinearLayout linearLayout = (LinearLayout)subMultipleView.getAdapter().getView(i,null,null);
            TextView txtMultipleDetailProductName = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProductName);
            String strQuantity = multipleDetailAdapter.getQuantity(i);

            if(strQuantity.equals("")||strQuantity.isEmpty()||strQuantity.equals("0")){
                if(strModStatus.equals("4")){
                    MyToast.myShow(SubDetailForMultipleActivity.this,"零件:"+txtMultipleDetailProductName.getText().toString()+",数量不可为0",2,0);
                    return false;
                }else{
                    if(i==0){
                        MyToast.myShow(SubDetailForMultipleActivity.this,"零件:"+txtMultipleDetailProductName.getText().toString()+",数量不可为0",2,0);
                        return false;
                    }
                }

            }
        }

        return true;
    }

    /**
    *描述: 检查保存数据合理性，合理则写入本地数据库
    *日期：2022/6/10
    **/
    private void saveData(String strAction,String strActionId,String qcstatus,String qrcode){
        if(checkListItemQuantity(strAction)){
            saveDbData(strAction,strActionId,qcstatus,qrcode);
        }
    }

    /**
     *描述: 清除本地数据
     *日期：2022/6/10
     **/
    private void deleteDbData(){

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                int iCount = 0;
                int iCount2 = 0;
                statusCode = "0";
                statusDescription = "删除成功";

                //检查是否存在数据
                iCount = hzDb.productDao().getCount();
                if(iCount>0){
                    //清空数据
                    hzDb.productDao().deleteAll();
                }

                //删除尾数合箱和差异数
                iCount2 = hzDb.mergeLabelDao().getCount();
                if(iCount2>0){
                    hzDb.mergeLabelDao().deleteAll();
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

            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
    *描述: 暂存数据至本地，点击打印才提交至服务器
    *日期：2022/6/10
    **/
    private void saveDbData(String strAction,String strActionId,String qcstatus,String qrcode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForMultipleActivity.this,"数据提交中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                int iCount = 0;
                statusCode = "-1";
                statusDescription = "保存失败";

                //检查是否存在数据
                iCount = hzDb.productDao().getCount();
                if(iCount>0){
                    //清空数据
                    hzDb.productDao().deleteAll();
                }

                //获取来料差异条码信息
                String diffQrcode = "";
                mergeLabelEntityDiffQtyList = hzDb.mergeLabelDao().getAll(TYPE_DIFF);
                if(mergeLabelEntityDiffQtyList.size()>0){
                    for(int m=0;m<mergeLabelEntityDiffQtyList.size();m++){
                        String sQrcode = mergeLabelEntityDiffQtyList.get(m).getDiffQrcode();
                        if(diffQrcode.equals("")||diffQrcode.isEmpty()){
                            diffQrcode = sQrcode;
                        }else{
                            diffQrcode = diffQrcode+","+sQrcode;
                        }
                    }
                }

                //写入暂存数据
                int iCnt = multipleDetailAdapter.getCount();
                for(int i= 0;i<iCnt;i++){
                    LinearLayout linearLayout = (LinearLayout)subMultipleView.getAdapter().getView(i,null,null);
                    TextView txtMultipleDetailProductCode = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProductCode);
                    TextView txtMultipleDetailDocno = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailDocno);
                    TextView txtMultipleDetailProcessId = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProcessId);
                    TextView txtMultipleDetailProcess = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProcess);
                    TextView txtMultipleDetailDevice = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailDevice);
                    TextView txtMultipleDetailLots = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailLots);
                    TextView txtMultipleDetailEmployee = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailEmployee);
                    TextView txtMultipleDetailPlanDate = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailPlanDate);

                    String strQuantity;
                    if(strModStatus.equals("2")||strModStatus.equals("3")){
                        strQuantity = multipleDetailAdapter.getQuantity(0);
                    }else{
                        strQuantity = multipleDetailAdapter.getQuantity(i);
                    }

                    String strProductCode = txtMultipleDetailProductCode.getText().toString();
                    String strDocno = txtMultipleDetailDocno.getText().toString();
                    String strProcessId = txtMultipleDetailProcessId.getText().toString();
                    String strProcess = txtMultipleDetailProcess.getText().toString();
                    String strDevice = txtMultipleDetailDevice.getText().toString();
                    String strLots = txtMultipleDetailLots.getText().toString();
                    String strEmployee = txtMultipleDetailEmployee.getText().toString();
                    String strPlanDate = txtMultipleDetailPlanDate.getText().toString();
                    float fQuantity = Float.valueOf(strQuantity);

                    genRecordSetStr(strAction,strActionId,qcstatus,strProductCode,strDocno,"",strProcessId,strProcess,strDevice,strLots,strQuantity,"",i,strEmployee,qrcode,strGroupId,diffQrcode);

                    //写入sqllite
                    productEntity = new ProductEntity(strDocno,strFlag,0,0,strProductCode,strProcessId,strProcess,strDevice,strEmployee,strLots,fQuantity,strPlanDate);
                    hzDb.productDao().insert(productEntity);
                }

                //初始化T100服务名
                String webServiceName = "WorkReportRequestGen";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        mRecordSet+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

                if(mapResponseStatus.size()> 0){
                    for(Map<String,Object> mStatus: mapResponseStatus){
                        statusCode = mStatus.get("statusCode").toString();
                        statusDescription = mStatus.get("statusDescription").toString();
                    }

                    if(!statusCode.equals("0")){
                        //执行失败，删除本地数据
                        hzDb.productDao().deleteAll();
                    }

                }else{
                    //执行失败，删除本地数据
                    hzDb.productDao().deleteAll();
                }

                //检查是否写入成功
                iCount = hzDb.productDao().getCount();
                if(iCount>0){
                    statusCode = "0";
                    statusDescription = "保存成功";
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
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription, 1, 1);
                }else{
//                    MyToast.myShow(SubDetailForMultipleActivity.this,statusDescription,0,0);
                    ShowAlertDialog.myShow(SubDetailForMultipleActivity.this,statusDescription);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *描述: 提交打印数据至ERP
    *日期：2022/6/10
    **/
    private void savePrintToT100(String strAction,String strActionId,String qcstatus){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForMultipleActivity.this,"数据打印中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                //变量
                statusCode = "-1";
                statusDescription = "无报工数据,打印失败";
                int iPrintCount = 0;
                String qrcode = "";
                String diffQrcode = "";

                //获取合箱条码信息
                mergeLabelEntityList = hzDb.mergeLabelDao().getAll(TYPE_MEGERE);
                if(mergeLabelEntityList.size()>0){
                    for(int m=0;m<mergeLabelEntityList.size();m++){
                        String sQrcode = mergeLabelEntityList.get(m).getQrcode();
                        if(qrcode.equals("")||qrcode.isEmpty()){
                            qrcode = sQrcode;
                        }else{
                            qrcode = qrcode+","+sQrcode;
                        }
                    }
                }

                //获取来料差异条码信息
                mergeLabelEntityDiffQtyList = hzDb.mergeLabelDao().getAll(TYPE_DIFF);
                if(mergeLabelEntityDiffQtyList.size()>0){
                    for(int m=0;m<mergeLabelEntityDiffQtyList.size();m++){
                        String sQrcode = mergeLabelEntityDiffQtyList.get(m).getDiffQrcode();
                        if(diffQrcode.equals("")||diffQrcode.isEmpty()){
                            diffQrcode = sQrcode;
                        }else{
                            diffQrcode = diffQrcode+","+sQrcode;
                        }
                    }
                }

                //检查是否存在数据
                productEntityList = hzDb.productDao().getAll();
                if(productEntityList.size()>0){
                    for(int i=0;i<productEntityList.size();i++){
                        String strProductCode = productEntityList.get(i).getProductCode();
                        String strDocno = productEntityList.get(i).getProductDocno();
                        String strProcessId = productEntityList.get(i).getProcessId();
                        String strProcess = productEntityList.get(i).getProcess();
                        String strDevice = productEntityList.get(i).getDevices();
                        String strLots = productEntityList.get(i).getLots();
                        String strEmployee = productEntityList.get(i).getProductUser();
                        float fQuantity = productEntityList.get(i).getQuantity();
                        String strQuantity = String.valueOf(fQuantity);
                        String strPlanDate = productEntityList.get(i).getPlanDate();

                        //连线生产拆分工序
                        int iIndex = strProcessId.indexOf("/");
                        if(iIndex>-1){
                            String[] arrayProcessId = strProcessId.split("/");
                            String[] arrayProcess = strProcess.split("/");
                            for(int m=0;m<arrayProcessId.length;m++){
                                genRecordSetStr(strAction,strActionId,qcstatus,strProductCode,strDocno,strPlanDate,arrayProcessId[m],arrayProcess[m],strDevice,strLots,strQuantity,"",i,strEmployee,qrcode,strGroupId,diffQrcode);
                            }
                        }else{
                            genRecordSetStr(strAction,strActionId,qcstatus,strProductCode,strDocno,strPlanDate,strProcessId,strProcess,strDevice,strLots,strQuantity,"",i,strEmployee,qrcode,strGroupId,diffQrcode);
                        }
                    }

                    //初始化T100服务名
                    String webServiceName = "WorkReportRequestGen";

                    //发送服务器请求
                    T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                    String requestBody = "&lt;Document&gt;\n"+
                            "&lt;RecordSet id=\"1\"&gt;\n"+
                            mRecordSet+
                            "&lt;/RecordSet&gt;\n"+
                            "&lt;/Document&gt;\n";
                    String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                    mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
//                    mapResponseList = t100ServiceHelper.getT100JsonDocListData(strResponse,"docno");

                    //执行完成删除本地数据
                    hzDb.productDao().deleteAll();

//                    //开启打印任务
//                    String sCurrentDocno = "";
//                    sSocketDocno = "";
//                    for (Map<String, Object> mResponse : mapResponseList) {
//                        sCurrentDocno = mResponse.get("Docno").toString();
//                        sServerIp = mResponse.get("ServerIp").toString();
//                        if(!sCurrentDocno.isEmpty() && !sCurrentDocno.equals("") && !sServerIp.isEmpty() && !sServerIp.equals("")){
//                            if(sSocketDocno.isEmpty() ||sSocketDocno.equals("")){
//                                sSocketDocno = sCurrentDocno;
//                            }else{
//                                sSocketDocno = sSocketDocno+","+sCurrentDocno;
//                            }
//
//                            iPrintCount++;
//                        }
//                    }

                    //获取返回信息
                    for(Map<String,Object> mStatus: mapResponseStatus){
                        statusCode = mStatus.get("statusCode").toString();
                        statusDescription = mStatus.get("statusDescription").toString();
                    }
                }

//                statusDescription = statusDescription+",打印标签:"+String.valueOf(iPrintCount)+"张(IP:"+sServerIp+")";
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
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription, 1, 1);
                    finish();
                }else{
                    MyToast.myShow(SubDetailForMultipleActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *描述: 发送数据至打印机
    *日期：2022/8/8
    **/
    private void sendDataToPrinter(String docno,String serverIp){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Socket socket = new Socket(serverIp,1986);
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bufferedWriter.write(docno+"\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    String msg_get = "Response";
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while((msg_get=bufferedReader.readLine())!=null){
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        Log.d("PING",msg_get);
                        bundle.putString("msg",msg_get);
                        message.setData(bundle);
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
    *描述: 使用rxjava发送打印数据至打印机
    *日期：2022/8/12
    **/
    private void sendDataToPrint2(String docno,String serverIp){

        DatagramSocket socket;

        try{
            socket = new DatagramSocket(1986);
            InetAddress serverAddress = InetAddress.getByName(serverIp);
            byte data[] = docno.getBytes();
            DatagramPacket packet = new DatagramPacket(data,data.length,serverAddress,1986);
            socket.send(packet);
            socket.close();
            Log.i("print",docno);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
    *描述: 获取服务端消息至主线程UI显示
    *日期：2022/8/10
    **/
    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    Bundle bundle = msg.getData();
                    String msg_get = bundle.getString("msg");
                    MyToast.myShow(SubDetailForMultipleActivity.this, msg_get, 2, 1);
                    break;
            }
        }
    };

    /**
    *描述: 提交request请求至ERP
    *日期：2022/6/10
    **/
    private void saveMultipleToT100(String strAction,String strActionId,String qcstatus,String qrcode){
        int iCount = multipleDetailAdapter.getCount();
        mRecordSet="";

        if(checkListItemQuantity(strAction)){
            for(int i= 0;i<iCount;i++){
                LinearLayout linearLayout = (LinearLayout)subMultipleView.getAdapter().getView(i,null,null);
                TextView txtMultipleDetailProductCode = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProductCode);
                TextView txtMultipleDetailDocno = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailDocno);
                TextView txtMultipleDetailPlanDate = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailPlanDate);
                TextView txtMultipleDetailProcessId = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProcessId);
                TextView txtMultipleDetailProcess = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProcess);
                TextView txtMultipleDetailDevice = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailDevice);
                TextView txtMultipleDetailLots = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailLots);
                TextView txtMultipleDetailEmployee = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailEmployee);

                String strQuantity;
                if(strModStatus.equals("2")||strModStatus.equals("3")){
                    strQuantity = multipleDetailAdapter.getQuantity(0);
                }else{
                    strQuantity = multipleDetailAdapter.getQuantity(i);
                }

                String strProductCode = txtMultipleDetailProductCode.getText().toString();
                String strDocno = txtMultipleDetailDocno.getText().toString();
                String strPlanDate = txtMultipleDetailPlanDate.getText().toString();
                String strProcessId = txtMultipleDetailProcessId.getText().toString();
                String strProcess = txtMultipleDetailProcess.getText().toString();
                String strDevice = txtMultipleDetailDevice.getText().toString();
                String strLots = txtMultipleDetailLots.getText().toString();
                String strEmployee = txtMultipleDetailEmployee.getText().toString();
                String strProductDocno = multipleDetailAdapter.getProductDocno(i);

                genRecordSetStr(strAction,strActionId,qcstatus,strProductCode,strDocno,strPlanDate,strProcessId,strProcess,strDevice,strLots,strQuantity,strProductDocno,i,strEmployee,qrcode,strGroupId,"");
            }
            saveData2ToT100(strAction,strActionId);
        }
    }

    /**
    *描述: XML文件生成
    *日期：2022/6/10
    **/
    private void genRecordSetStr(String action,String actionid,String qcstatus,String strProductCode,String strDocno,String strPlanDate,String strProcessId,String strProcess,String strDevice,String strLots,String strQuantity,String strProductDocno,int i,String strEmployee,String qrcode,String sGroupId,String qrcode2){
        long timeCurrentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        String currentTime = simpleTimeFormat.format(timeCurrentTimeMillis);
        String currentDate = simpleDateFormat.format(new Date());

        //生成数据集合
        if(mRecordSet==""||mRecordSet.isEmpty()){
            mRecordSet = "&lt;Master name=\"sffb_t\" node_id=\""+i+"\"&gt;\n"+
                    "&lt;Record&gt;\n"+
                    "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                    "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                    "&lt;Field name=\"sffbdocdt\" value=\""+strPlanDate+"\"/&gt;\n"+
                    "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                    "&lt;Field name=\"sffb004\" value=\""+ sGroupId +"\"/&gt;\n"+  //班次
                    "&lt;Field name=\"sffb005\" value=\""+ strDocno +"\"/&gt;\n"+  //工单单号
                    "&lt;Field name=\"sffbseq\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                    "&lt;Field name=\"sffb010\" value=\""+ strDevice +"\"/&gt;\n"+  //机器编号
                    "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //批量生产止日期
                    "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //批量生产止时间
                    "&lt;Field name=\"sffb029\" value=\""+ strProductCode +"\"/&gt;\n"+  //报工料号
                    "&lt;Field name=\"sffb017\" value=\""+ strQuantity +"\"/&gt;\n"+  //良品数量
                    "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                    "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //工序
                    "&lt;Field name=\"lots\" value=\""+ strLots +"\"/&gt;\n"+  //批次
                    "&lt;Field name=\"sffbdocno\" value=\""+ strProductDocno +"\"/&gt;\n"+  //报工单号
                    "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //状态
                    "&lt;Field name=\"planno\" value=\""+ strFlag +"\"/&gt;\n"+  //计划单号
                    "&lt;Field name=\"planseq\" value=\""+ txtMultipleInputCount.getText().toString() +"\"/&gt;\n"+  //报工次数
                    "&lt;Field name=\"planuser\" value=\""+ strEmployee +"\"/&gt;\n"+  //生产人员
                    "&lt;Field name=\"errorlots\" value=\""+ strErrorLots +"\"/&gt;\n"+  //异常批次
                    "&lt;Field name=\"models\" value=\""+ strModStatus +"\"/&gt;\n"+  //同模类型
                    "&lt;Field name=\"version\" value=\""+ strVersion +"\"/&gt;\n"+  //版本
                    "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //执行动作
                    "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+  //二维码
                    "&lt;Field name=\"qrcode2\" value=\""+ qrcode2 +"\"/&gt;\n"+  //来料差二维码
                    "&lt;Field name=\"actcode\" value=\""+ actionid +"\"/&gt;\n"+  //执行命令ID
                    "&lt;Field name=\"package\" value=\""+ editPackages.getText().toString().trim() +"\"/&gt;\n"+  //单个包装量
                    "&lt;Field name=\"processend\" value=\""+ strProcessEnd +"\"/&gt;\n"+  //是否连线生产
                    "&lt;Field name=\"connectProcessId\" value=\""+ strProcessInitId +"\"/&gt;\n"+  //连线生产工序ID
                    "&lt;Field name=\"connectProcess\" value=\""+ strProcessInit +"\"/&gt;\n"+  //连线生产工序
                    "&lt;Field name=\"sffbuc038\" value=\""+ iDiffTotal +"\"/&gt;\n"+  //差异量
                    "&lt;Field name=\"mergeqty\" value=\""+ iTotal +"\"/&gt;\n"+  //尾数合箱量
                    "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                    "&lt;Record&gt;\n"+
                    "&lt;Field name=\"sffyucseq\" value=\"1.0\"/&gt;\n"+
                    "&lt;/Record&gt;\n"+
                    "&lt;/Detail&gt;\n"+
                    "&lt;Memo/&gt;\n"+
                    "&lt;Attachment count=\"0\"/&gt;\n"+
                    "&lt;/Record&gt;\n"+
                    "&lt;/Master&gt;\n";
        }else{
            mRecordSet = mRecordSet + "&lt;Master name=\"sffb_t\" node_id=\""+i+"\"&gt;\n"+
                    "&lt;Record&gt;\n"+
                    "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                    "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                    "&lt;Field name=\"sffbdocdt\" value=\""+strPlanDate+"\"/&gt;\n"+
                    "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                    "&lt;Field name=\"sffb004\" value=\""+ sGroupId +"\"/&gt;\n"+  //班次
                    "&lt;Field name=\"sffb005\" value=\""+ strDocno +"\"/&gt;\n"+  //工单单号
                    "&lt;Field name=\"sffbseq\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                    "&lt;Field name=\"sffb010\" value=\""+ strDevice +"\"/&gt;\n"+  //机器编号
                    "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //批量生产止日期
                    "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //批量生产止时间
                    "&lt;Field name=\"sffb029\" value=\""+ strProductCode +"\"/&gt;\n"+  //报工料号
                    "&lt;Field name=\"sffb017\" value=\""+ strQuantity +"\"/&gt;\n"+  //良品数量
                    "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                    "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //工序
                    "&lt;Field name=\"lots\" value=\""+ strLots +"\"/&gt;\n"+  //批次
                    "&lt;Field name=\"sffbdocno\" value=\""+ strProductDocno +"\"/&gt;\n"+  //报工单号
                    "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //状态
                    "&lt;Field name=\"planno\" value=\""+ strFlag +"\"/&gt;\n"+  //计划单号
                    "&lt;Field name=\"planseq\" value=\""+ txtMultipleInputCount.getText().toString() +"\"/&gt;\n"+  //报工次数
                    "&lt;Field name=\"planuser\" value=\""+ strEmployee +"\"/&gt;\n"+  //生产人员
                    "&lt;Field name=\"errorlots\" value=\""+ strErrorLots +"\"/&gt;\n"+  //异常批次
                    "&lt;Field name=\"models\" value=\""+ strModStatus +"\"/&gt;\n"+  //同模类型
                    "&lt;Field name=\"version\" value=\""+ strVersion +"\"/&gt;\n"+  //版本
                    "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //执行动作
                    "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+  //二维码
                    "&lt;Field name=\"qrcode2\" value=\""+ qrcode2 +"\"/&gt;\n"+  //来料差二维码
                    "&lt;Field name=\"actcode\" value=\""+ actionid +"\"/&gt;\n"+  //执行命令ID
                    "&lt;Field name=\"package\" value=\""+ editPackages.getText().toString().trim() +"\"/&gt;\n"+  //单个包装量
                    "&lt;Field name=\"processend\" value=\""+ strProcessEnd +"\"/&gt;\n"+  //是否连线生产
                    "&lt;Field name=\"connectProcessId\" value=\""+ strProcessInitId +"\"/&gt;\n"+  //连线生产工序ID
                    "&lt;Field name=\"connectProcess\" value=\""+ strProcessInit +"\"/&gt;\n"+  //连线生产工序
                    "&lt;Field name=\"sffbuc038\" value=\""+ iDiffTotal +"\"/&gt;\n"+  //差异量
                    "&lt;Field name=\"mergeqty\" value=\""+ iTotal +"\"/&gt;\n"+  //尾数合箱量
                    "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                    "&lt;Record&gt;\n"+
                    "&lt;Field name=\"sffyucseq\" value=\"1.0\"/&gt;\n"+
                    "&lt;/Record&gt;\n"+
                    "&lt;/Detail&gt;\n"+
                    "&lt;Memo/&gt;\n"+
                    "&lt;Attachment count=\"0\"/&gt;\n"+
                    "&lt;/Record&gt;\n"+
                    "&lt;/Master&gt;\n";
        }

    }

    /**
    *描述: 执行webservice写入操作
    *日期：2022/6/10
    **/
    private void saveData2ToT100(String action,String actionid){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForMultipleActivity.this,"数据提交中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "WorkReportRequestGen";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        mRecordSet+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100ResponseDocno3(strResponse,"docno");

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
                    MyToast.myShow(SubDetailForMultipleActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    String strDocno="";

                    int i=0;

                    if(action.equals("save")){
                        if(mapResponseList.size()> 0) {
                            for (Map<String, Object> mResponse : mapResponseList) {
                                strDocno = mResponse.get("Docno").toString();
                                multipleDetailAdapter.updateData(i,subMultipleView,strDocno);
                                i++;
                            }
                        }
                    }
                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription, 1, 1);

                    if(action.equals("print")){
                        finish();
                    }else{
                        if(actionid.equals("10")){
                            imgMultipleStartStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                        }else if(actionid.equals("11")){
                            if(mapResponseList.size()> 0) {
                                String strErrorCount="";
                                for (Map<String, Object> mResponse : mapResponseList) {
                                    strErrorCount = mResponse.get("ErrorCount").toString();
                                }
                                txtMultipleQcCount.setText(strErrorCount);
                            }

                            imgMultipleQcStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                        }else if(actionid.equals("12")){
                            imgMultipleProductStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));

                            //打开上料检核数据
                            if(mapResponseList.size()> 0) {
                                String sProductCode="";
                                String sProductName="";
                                String sProductModels="";
                                String sProcessId="";
                                String sProcess="";
                                String sDevice="";
                                String sQuantity="";
                                String sEmp="";
                                String sStatus="";
                                String sAttribute="";

                                for (Map<String, Object> mResponse : mapResponseList) {
                                    sProductCode = mResponse.get("ProductCode").toString();
                                    sProductName = mResponse.get("ProductName").toString();
                                    sProductModels = mResponse.get("ProductModels").toString();
                                    sProcessId = mResponse.get("ProcessId").toString();
                                    sProcess = mResponse.get("Process").toString();
                                    sDevice = mResponse.get("Device").toString();
                                    sQuantity = mResponse.get("Quantity").toString();
                                    sEmp = mResponse.get("Emp").toString();
                                    sStatus = mResponse.get("Status").toString();
                                    sAttribute = mResponse.get("Attribute").toString();
                                }

                                Intent intent = new Intent(SubDetailForMultipleActivity.this,CheckMaterialActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("Docno",strFlag);
                                bundle.putString("ProductCode",sProductCode);
                                bundle.putString("ProductName",sProductName);
                                bundle.putString("ProductModels",sProductModels);
                                bundle.putString("ProcessId",sProcessId);
                                bundle.putString("Process",sProcess);
                                bundle.putString("Device",sDevice);
                                bundle.putString("Quantity",sQuantity);
                                bundle.putString("Emp",sEmp);
                                bundle.putString("Status",sStatus);
                                bundle.putString("Attribute",sAttribute);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }

                        }else if(actionid.equals("13")){
                            if(mapResponseList.size()> 0) {
                                String strErrorCount="";
                                for (Map<String, Object> mResponse : mapResponseList) {
                                    strErrorCount = mResponse.get("ErrorCount").toString();
                                }
                                txtMultipleErrorCount.setText(strErrorCount);
                            }

                            imgMultipleErrorBeginStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                            imgMultipleQcStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
                            imgMultipleErrorEndStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
                        }else if(actionid.equals("14")){
                            imgMultipleErrorEndStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                        }
                    }

                }else{
                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription, 0, 1);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *描述: 更新当前标签状态,更新为N
    *日期：2022/6/12
    **/
    private void updQrcodeData(String strAction,String strActionId,String qcstatus,String qrcode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForMultipleActivity.this,"数据更新中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名:cwssp022
                String webServiceName = "MaterialCheckInsert";

                //初始化日期时间
                long timeCurrentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
                String currentTime = simpleTimeFormat.format(timeCurrentTimeMillis);
                String currentDate = simpleDateFormat.format(new Date());

                //获取工单信息
                int iCount = multipleDetailAdapter.getCount();
                String strProductDocno = "";

                for(int i= 0;i<iCount;i++){
                    if(strProductDocno.equals("")||strProductDocno.isEmpty()){
                        strProductDocno = multipleDetailAdapter.getItemValue(i,"Docno");
                    }else{
                        strProductDocno = strProductDocno + ","+multipleDetailAdapter.getItemValue(i,"Docno");
                    }

                }

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"sffb_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"sffb005\" value=\""+ strProductDocno +"\"/&gt;\n"+  //工单单号
                        "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //批量生产止日期
                        "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //批量生产止时间
                        "&lt;Field name=\"planuser\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //生产人员
                        "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                        "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //工序
                        "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //状态
                        "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+  //二维码
                        "&lt;Field name=\"act\" value=\""+ strAction +"\"/&gt;\n"+  //操作类别
                        "&lt;Field name=\"actcode\" value=\""+ strActionId +"\"/&gt;\n"+  //执行命令ID
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
                mapList = t100ServiceHelper.getT100ResponseDocno4(strResponse,"docno");

                e.onNext(mapResponseStatus);
                e.onNext(mapList);
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
                    MyToast.myShow(SubDetailForMultipleActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
//                    String sDocno = "";
//                    String sProductCode = "";
                    String sProductName = "";
//                    String sProcessId = "";

                    if(mapList.size()> 0) {
                        for (Map<String, Object> mResponse : mapList) {
//                            sDocno = mResponse.get("Docno").toString();
//                            sProductCode = mResponse.get("ProductCode").toString();
                            sProductName = mResponse.get("ProductName").toString();
//                            sProcessId = mResponse.get("ProcessId").toString();
                        }
                    }
                    MyToast.myShow(SubDetailForMultipleActivity.this, "零件:"+sProductName+",标签确认成功", 1, 1);
                }else{
//                    MyToast.myShow(SubDetailForMultipleActivity.this,statusDescription,0,0);
                    ShowAlertDialog.myShow(SubDetailForMultipleActivity.this,statusDescription);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}