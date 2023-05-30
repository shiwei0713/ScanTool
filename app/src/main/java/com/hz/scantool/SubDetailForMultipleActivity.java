package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.hz.scantool.adapter.MyAlertDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.database.MergeLabelEntity;
import com.hz.scantool.database.ProductEntity;
import com.hz.scantool.dialog.InputDialog;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

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
    private static final int MATERIALQTY = 1003; //上料检核量
    private static final int PERIOD = 5000;  //5秒
    private static final String TYPE_MEGERE = "MEGERE";
    private static final String TYPE_DIFF = "DIFF";
    private static final String TYPE_SPILT = "+";

    private HzDb hzDb;
    private String dataBaseName = "HzDb";

    private String strTitle,strUser;
    private String strPlanNo,strVersion,strProcessId,strProcess,strConnectDocno,strProcessEnd,strStationDocno,strGroupId,strGroup,strDevice;
    private String strOperateCount,strPrintCount,strStartStatus,strCheckStatus,strUpStatus,strErrorStartStatus,strErrorStopStatus;
    private String strStartTime,strCheckTime,strUpTime,strErrorTime,strProductTotal,strErrorLots,strWorkTime,strDocType;
    private String strProcessInitId,strProcessInit;
    private String strStation,strUpStation;
    private String statusCode,statusDescription,isCommit;
    private String mRecordSet="";
    private boolean isMore;
    private long lastClick,firstClick;
    private int w,h;
    private int id = 0;
    private int iTotal = 0;
    private int iDiffTotal = 0;
    private int iMaterialTotal = 0;
    boolean isHide = false;

    private TextView txtMultipleProcessEnd,txtMultipleStation,txtMultipleGroupStation,txtMultipleConnectDocno,txtMultipleGroupId,txtMultipleGroup;
    private EditText editPackages;
    private LinearLayout viewInputBasic;
    private TextView txtMultipleInputCount,txtMultiplePrintCount,txtMultipleErrorCount,txtMultipleQcCount,txtMultipleMaterialQty;
    private TextView txtMultipleSum,txtMultipleStartTime,txtMultipleCheckTime,txtMultipleProductTime,txtMultipleErrorTime;
    private ImageView imgMultipleQrcode,imgMultipleStartStatus,imgMultipleQcStatus,imgMultipleProductStatus,imgMultipleErrorBeginStatus,imgMultipleErrorEndStatus;
    private Button btnMultipleStart,btnMultipleEnd,btnMultipleQc,btnMultipleProduct,btnMultipleError;
    private Button btnMultipleSave,btnMultiplePrint,btnMultiplePrintMaterial,btnMultipleTransate,btnMore;
    private Button btnLess,btnLabel;
    private ListView subMultipleView;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private LoadingDialog loadingDialog;
    private MultipleDetailAdapter multipleDetailAdapter;
    private ProductEntity productEntity;
    private InputDialog inputDialog;
    private List<ProductEntity> productEntityList;
    private List<MergeLabelEntity> mergeLabelEntityList,mergeLabelEntityDiffQtyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_for_multiple);

        //初始化
        initBundle();
        initView();

        //初始化班次
        setWorktime();

        //初始化本地缓存数据库
        initDataBase();
        deleteDbData();

        //生成二维码
        createQrcode(strPlanNo +"_"+strVersion+"_"+ strUser+"_"+strProcessId+"_"+strProcess+"_"+txtMultipleInputCount.getText().toString().trim()+"_"+strProcessEnd);

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
                checkLabel();
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
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = this.getResources().getString(R.string.master_detail1);
        strUser = UserInfo.getUserId(getApplicationContext());
        strPlanNo = bundle.getString("PlanNo");
        strProcessId = bundle.getString("ProcessId");
        strProcess = bundle.getString("Process");
        strVersion  = bundle.getString("Version");
        strProcessEnd = bundle.getString("ProcessEnd");
        strConnectDocno = bundle.getString("ConnectDocno");
        strStationDocno = bundle.getString("StationDocno");
        strDevice = bundle.getString("Device");
        strStation = bundle.getString("GroupStation");
        strGroupId = bundle.getString("GroupId");
        strGroup = bundle.getString("Group");
        strOperateCount = bundle.getString("OperateCount");
        strPrintCount = bundle.getString("PrintCount");
        strDocType = bundle.getString("DocType");
        isCommit="N";
    }

    //初始化控件
    private void initView(){
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

        txtMultipleInputCount = findViewById(R.id.txtMultipleInputCount);
        txtMultiplePrintCount = findViewById(R.id.txtMultiplePrintCount);
        imgMultipleQrcode = findViewById(R.id.imgMultipleQrcode);
        imgMultipleStartStatus = findViewById(R.id.imgMultipleStartStatus);
        imgMultipleQcStatus = findViewById(R.id.imgMultipleQcStatus);
        imgMultipleProductStatus = findViewById(R.id.imgMultipleProductStatus);
        imgMultipleErrorBeginStatus = findViewById(R.id.imgMultipleErrorBeginStatus);
        imgMultipleErrorEndStatus = findViewById(R.id.imgMultipleErrorEndStatus);
        subMultipleView = findViewById(R.id.subMultipleView);
        txtMultipleErrorCount = findViewById(R.id.txtMultipleErrorCount);
        txtMultipleQcCount = findViewById(R.id.txtMultipleQcCount);
        txtMultipleMaterialQty = findViewById(R.id.txtMultipleMaterialQty);
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
        txtMultipleGroup = findViewById(R.id.txtMultipleGroup);
        txtMultipleProcessEnd = findViewById(R.id.txtMultipleProcessEnd);
        txtMultipleStation = findViewById(R.id.txtMultipleStation);
        txtMultipleGroupStation = findViewById(R.id.txtMultipleGroupStation);
        txtMultipleConnectDocno = findViewById(R.id.txtMultipleConnectDocno);

        btnMultipleTransate = findViewById(R.id.btnMultipleTransate);
        btnMore = findViewById(R.id.btnMore);
        btnLess = findViewById(R.id.btnLess);
        btnLabel = findViewById(R.id.btnLabel);

        //初始化值
        txtMultipleProcessEnd.setText(strProcessEnd);
        txtMultipleGroupId.setText(strGroupId);
        txtMultipleGroup.setText(strGroup);
        txtMultipleGroupStation.setText(strStation);
        txtMultipleStation.setText(strUpStation);
        txtMultipleConnectDocno.setText(strConnectDocno);
        txtMultipleInputCount.setText(strOperateCount);
        txtMultiplePrintCount.setText(strPrintCount);

        //绑定事件
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
        btnLabel.setOnClickListener(new commandClickListener());
        txtMultipleStation.setOnClickListener(new commandClickListener());
    }

    /**
    *描述: 班次
    *日期：2023-05-22
    **/
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

    /**
    *描述: listview废品录入实现
    *日期：2023/4/14
    **/
    private MultipleDetailAdapter.BadQuantityClickListener badQuantityClickListener = new MultipleDetailAdapter.BadQuantityClickListener() {
        @Override
        public void BadQuantityClick(int position, View view) {
            inputDialog = new InputDialog(SubDetailForMultipleActivity.this);
            inputDialog.setTitle("请输入废品数");
            inputDialog.setOnOkOnClickListener("确定", new InputDialog.onOkOnClickListener() {
                @Override
                public void onOkClick() {
                    String sQuantity = inputDialog.getEtQuantity().getText().toString();
                    multipleDetailAdapter.setBadQuantity(position,subMultipleView,sQuantity);

                    inputDialog.dismiss();
                }
            });

            inputDialog.setCancelOnClickListener("取消", new InputDialog.onCancelOnClickListener() {
                @Override
                public void onCancelClick() {
                    inputDialog.dismiss();
                }
            });

            inputDialog.show();

            inputDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });
        }
    };

    /**
    *描述: 按钮事件实现
    *日期：2023/4/14
    **/
    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            String sScanStation = txtMultipleStation.getText().toString();

            switch (view.getId()){
                case R.id.txtMultipleStation:  //工位
                    if(!strStation.equals("")&&!strStation.isEmpty()){
                        scanStation();
                    }
                    break;
                case R.id.btnMultipleStart: //开始生产
                    mRecordSet = "";

                    if(strStation.equals("")||strStation.isEmpty()){
                        saveAssistToT100("insert","10","B","",sScanStation);
                    }else{
                        if(sScanStation.isEmpty()||sScanStation.equals("")){
                            MyToast.myShow(SubDetailForMultipleActivity.this,"请先扫描工位",0,0);
                        }else{
                            saveAssistToT100("insert","10","B","",sScanStation);
                        }
                    }
                    break;
                case R.id.btnMultipleEnd:   //异常结束  //S修改为X
                    mRecordSet = "";
                    strErrorLots = txtMultipleErrorCount.getText().toString();
                    saveAssistToT100("insert","14","X","",sScanStation);
                    break;
                case R.id.btnMultipleSave:  //保存数据
                    //注释保存直接写入服务器，修改为保存至本地，点击打印再写入服务器
                    mRecordSet = "";
                    strErrorLots = txtMultipleErrorCount.getText().toString();
                    btnMultiplePrint.setEnabled(true);
                    isCommit = "N";
                    saveData("save","19","V","");
                    break;
                case R.id.btnMultiplePrint: //打印数据
                    mRecordSet = "";
                    strErrorLots = txtMultipleErrorCount.getText().toString();
                    firstClick = System.currentTimeMillis();
                    btnMultiplePrint.setEnabled(false);
                    if(firstClick-lastClick<=PERIOD){
                        return;
                    }
                    lastClick = System.currentTimeMillis();
                    savePrintToT100("print","19","P",sScanStation);
                    break;
                case R.id.btnMultipleQc:    //报首检已报首检:F,首检合格：K
                    mRecordSet = "";
                    strErrorLots = txtMultipleQcCount.getText().toString();
                    saveAssistToT100("insert","11","F","",sScanStation);
                    break;
                case R.id.btnMultipleProduct:   //上料检核
                    mRecordSet = "";
                    checkMaterial();
                    break;
                case R.id.btnMultipleError:     //异常开始
                    mRecordSet = "";
                    strErrorLots = txtMultipleErrorCount.getText().toString();
                    saveAssistToT100("insert","13","S","",sScanStation);
                    break;
                case R.id.btnMultiplePrintMaterial:    //打印余料标签
                    mRecordSet = "";
                    strErrorLots = txtMultipleErrorCount.getText().toString();
                    saveAssistToT100("printmaterial","19","I","",sScanStation);
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
                case R.id.btnLabel: //标签信息,显示已报工标签
                    Intent intent = new Intent(SubDetailForMultipleActivity.this,ProductLabelActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("planNo",strPlanNo);
                    bundle.putString("version",strVersion);
                    bundle.putString("title",btnLabel.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
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
        }else if(requestCode==MATERIALQTY&&resultCode==MATERIALQTY){
            iMaterialTotal = data.getIntExtra("total",0);
            txtMultipleMaterialQty.setText(String.valueOf(iMaterialTotal));
        }
    }

    /**
    *描述: 组合工位，开始生产需扫描工位条码
    *日期：2023-05-05
    **/
    private void scanStation(){
        //调用zxing扫码界面
        IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailForMultipleActivity.this);
        intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
        //开始扫描
        intentIntegrator.initiateScan();
    }

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){

        //检查工位匹配
        String sCurrentStation = "";
        String sCurrentDevice= "";
        int iStation = qrContent.indexOf(TYPE_SPILT);
        if(iStation>-1){
            String[] arrayGroupStation = qrContent.split(Pattern.quote(TYPE_SPILT));
            sCurrentDevice = arrayGroupStation[0];

            if(sCurrentDevice.equals(strDevice)){
                sCurrentStation = arrayGroupStation[1];
                int iGroupStation = strStation.indexOf(sCurrentStation);
                if(iGroupStation>-1){
                    txtMultipleStation.setText(sCurrentStation);
                }else{
                    MyAlertDialog.myShowAlertDialog(SubDetailForMultipleActivity.this,"错误信息","工位不匹配,任务单组合:"+strStation+",扫描工位:"+sCurrentStation);
                }
            }else{
                MyAlertDialog.myShowAlertDialog(SubDetailForMultipleActivity.this,"错误信息","设备编号不匹配,任务单设备:"+strDevice+",扫描设备:"+sCurrentDevice);
            }
        }else{
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }

    }

    /**
    *描述: 格式化条件
    *日期：2023-05-23
    **/
    private String formatConditions(String condition,String spilt){
        String strCondition = "";

        for(int i= 0;i<multipleDetailAdapter.getCount();i++){
            String sCurrentValue = multipleDetailAdapter.getItemValue(i,condition);

            if(strCondition.equals("")||strCondition.isEmpty()){
                strCondition = sCurrentValue;
            }else{
                int iSameIndex = strCondition.indexOf(sCurrentValue); //去除重复
                if(iSameIndex<=-1){
                    strCondition = strCondition + spilt +sCurrentValue;
                }
            }
        }

        return strCondition;
    }

    /**
    *描述: 格式化连线条件
    *日期：2023-05-23
    **/
    private String formatConnectConditions(String condition,String spilt){
        String strCondition = "";

        for(int i= 0;i<multipleDetailAdapter.getCount();i++){
            String sCurrentValue = multipleDetailAdapter.getItemValue(i,condition);
            String sConnectProcess = multipleDetailAdapter.getItemValue(i,"ConnectProcess"); //获取工序标识
            if(!sConnectProcess.equals("INIT")){
                continue;
            }

            if(strCondition.equals("")||strCondition.isEmpty()){
                strCondition = sCurrentValue;
            }else{
                int iSameIndex = strCondition.indexOf(sCurrentValue); //去除重复
                if(iSameIndex<=-1){
                    strCondition = strCondition + spilt +sCurrentValue;
                }
            }
        }

        return strCondition;
    }

    /**
    *描述: 标签确认
    *日期：2022/12/31
    **/
    private void checkLabel(){
        //初始化传入参数
        String sProductDocno = formatConditions("Docno",","); //工单号
        String sProcessId = formatConditions("ProcessId","/"); //工序项次
        String sProcess = formatConditions("Process","/"); //工序号

        //打开标签确认界面
        Intent intent = new Intent(SubDetailForMultipleActivity.this,CheckLabelActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("ProcessId",sProcessId);
        bundle.putString("Process",sProcess);
        bundle.putString("ProductDocno",sProductDocno);
        bundle.putString("ConnectProduct",strProcessEnd);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
    *描述: 上料检核
    *日期：2022/6/12
    **/
    private void checkMaterial(){
        //检查是否生产开始
        if(strStartStatus.equals("")||strStartStatus.isEmpty()){
            strStartStatus = "0";
        }
        int iStartStatus = Integer.parseInt(strStartStatus);
        if(iStartStatus>0){
            //初始化传入参数
            String sPlanDate = multipleDetailAdapter.getItemValue(0,"PlanDate"); //工单日期
            String sProductCode = multipleDetailAdapter.getItemValue(0,"ProductCode"); //料号
            String sProductDocno = formatConditions("Docno",","); //工单号
            String sProcessId = formatConditions("ProcessId","/"); //工序项次
            if(strProcessEnd.equals("Y")){
                sProcessId = formatConnectConditions("ProcessId","/");
            }
            String sProcess = formatConditions("Process","/"); //工序号

            //打开上料检核界面
            Intent intent = new Intent(SubDetailForMultipleActivity.this,CheckMaterialActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("PlanNo", strPlanNo);
            bundle.putString("Version",strVersion);
            bundle.putString("ProcessId",sProcessId);
            bundle.putString("Process",sProcess);
            bundle.putString("Device",strDevice);
            bundle.putString("ProductDocno",sProductDocno);
            bundle.putString("PlanSeq",txtMultipleInputCount.getText().toString());
            bundle.putString("WorkTime",strWorkTime);
            bundle.putString("ProcessEnd",strProcessEnd);
            bundle.putString("ConnectDocno",strConnectDocno);
            bundle.putString("PlanDate",sPlanDate);
            bundle.putString("GroupId",strGroupId);
            bundle.putString("DocType",strDocType);
            bundle.putString("ProductCode",sProductCode);
            intent.putExtras(bundle);
            startActivityForResult(intent,MATERIALQTY);
        }else{
            MyToast.myShow(SubDetailForMultipleActivity.this,"开始生产才可进行上料",2,0);
        }
    }

    /**
    *描述: 来料差异
    *日期：2022/10/12
    **/
    private void setDiff(int type,String btnTitle){
        //初始化传入参数
        String strProductDocno = formatConditions("Docno",","); //工单号;
        String strProductCode = formatConditions("ProductCode","/"); //料号

        //打开来料差界面
        Intent intent = new Intent(SubDetailForMultipleActivity.this,SubDetailForDiffQtyActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Docno", strPlanNo);
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
        //初始化传入参数
        String strProductDocno = formatConditions("Docno",","); //工单号;
        String strProductCode = formatConditions("ProductCode","/"); //工单号;

        //打开尾数合箱界面
        Intent intent = new Intent(SubDetailForMultipleActivity.this,SubDetailForPackageActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString("Docno", strPlanNo);
        bundle.putString("Version",strVersion);
        bundle.putString("ProcessId",strProcessId);
        bundle.putString("Process",strProcess);
        bundle.putString("ProductDocno",strProductDocno);
        bundle.putString("ProductCode",strProductCode);
        bundle.putString("PlanSeq",txtMultipleInputCount.getText().toString());
        bundle.putString("TypeDesc",TYPE_MEGERE);
        intent.putExtras(bundle);
        startActivityForResult(intent,MERGEPACKAGE);
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
                String strType = "9";
                String strwhere = " sfaauc014='"+ strPlanNo +"' AND sfaauc007='"+strProcessId+"' AND sfaauc001="+strVersion;
                String strhaving = "";

                //连线任务,按照连线单号显示当前用户任务
                if(strProcessEnd.equals("Y")){
                    strwhere = " sfaauc028='"+ strConnectDocno +"'";
                }

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
                        "&lt;Field name=\"user\" value=\""+ strUser +"\"/&gt;\n"+  //异动人员
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonUserTaskDetailData(strResponse,"workorder");

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
                    MyToast.myShow(SubDetailForMultipleActivity.this,"无备料数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                if(loadingDialog != null){
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }

            @Override
            public void onComplete() {
                if(!statusCode.equals("0")){
                    MyToast.myShow(SubDetailForMultipleActivity.this,statusDescription,0,0);
                }else{
                    //显示list item数据
                    multipleDetailAdapter = new MultipleDetailAdapter(mapResponseList,getApplicationContext(),badQuantityClickListener);
                    subMultipleView.setAdapter(multipleDetailAdapter);

                    //显示辅助数据
                    showAssistDetail();
                }

                if(loadingDialog != null){
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }
        });
    }

    /**
    *描述: 显示辅助事项数据
    *日期：2023-05-22
    **/
    private void showAssistDetail(){
        if(mapResponseList.size()>0){
            //辅助次数
            strStartStatus= (String)mapResponseList.get(0).get("StartStatus");
            strCheckStatus= (String)mapResponseList.get(0).get("CheckStatus");
            strUpStatus= (String)mapResponseList.get(0).get("UpStatus");
            strErrorStartStatus= (String)mapResponseList.get(0).get("ErrorStartStatus");
            strErrorStopStatus= (String)mapResponseList.get(0).get("ErrorStopStatus");

            //辅助时间
            strStartTime = (String)mapResponseList.get(0).get("StartTime");
            strCheckTime = (String)mapResponseList.get(0).get("CheckTime");
            strUpTime = (String)mapResponseList.get(0).get("UpTime");
            strErrorTime = (String)mapResponseList.get(0).get("ErrorTime");

            //状态图片切换
            setStatusImage(imgMultipleStartStatus,strStartStatus); //开始生产状态
            setStatusImage(imgMultipleQcStatus,strCheckStatus); //报首检状态
            setStatusImage(imgMultipleProductStatus,strUpStatus); //上料检核状态
            setStatusImage(imgMultipleErrorBeginStatus,strErrorStartStatus); //异常开始状态
            setStatusImage(imgMultipleErrorEndStatus,strErrorStopStatus); //异常结束状态

            //辅助事项次数显示
            txtMultipleErrorCount.setText(strErrorStartStatus);  //异常次数
            txtMultipleQcCount.setText(strCheckStatus); //首检次数

            //辅助事项时间显示
            txtMultipleStartTime.setText(formatAssistTime(strStartTime)); //开始生产时间
            txtMultipleCheckTime.setText(formatAssistTime(strCheckTime)); //报首检时间
            txtMultipleErrorTime.setText(formatAssistTime(strErrorTime)); //异常时间
            txtMultipleProductTime.setText(formatAssistTime(strUpTime));
            txtMultipleSum.setText(strProductTotal);    //报工数
        }
    }

    /**
    *描述: 状态图片显示
    *日期：2023-05-22
    **/
    private void setStatusImage(ImageView imageView,String status){
        if(status.equals("0")||status.isEmpty()){
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }
    }

    /**
    *描述: 格式化辅助时间
    *日期：2023-05-22
    **/
    private String formatAssistTime(String assist){
        String sFormatAssistTime = "";

        if(!assist.equals("")&&!assist.isEmpty()){
            sFormatAssistTime = assist.replace("/","\n");
        }

        return sFormatAssistTime;
    }

    /**
    *描述: 核对输入数量
    *日期：2023/4/14
    **/
    private boolean checkListItemQuantity(String strAction){
        if(strAction.equals("insert") || strAction.equals("printmaterial")){
            return true;
        }

        for(int i= 0;i<multipleDetailAdapter.getCount();i++){
            LinearLayout linearLayout = (LinearLayout)subMultipleView.getAdapter().getView(i,null,null);
            TextView txtMultipleDetailProductName = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProductName);
            String strQuantity = multipleDetailAdapter.getQuantity(i);
            String strBadQuantity = multipleDetailAdapter.getBadQuantity(i);
            String strModStatus = multipleDetailAdapter.getItemValue(i,"ModStatus");

            if(strQuantity.equals("")||strQuantity.isEmpty()||strQuantity.equals("0")){
                if(strBadQuantity.equals("")||strBadQuantity.isEmpty()||strBadQuantity.equals("0")){
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
        }

        return true;
    }

    /**
    *描述: 实现点击确定方法
    *日期：2023/4/14
    **/
    private MyAlertDialog.ConfirmResult confirmResult = new MyAlertDialog.ConfirmResult() {
        @Override
        public void OnOkConfirm(String strAction, String strActionId, String qcstatus, String qrcode) {
            if(checkListItemQuantity(strAction)){
                saveDbData(strAction,strActionId,qcstatus,qrcode);
            }
        }

        @Override
        public void OnCancelConfirm() {
            deleteDbData();
        }
    };

    /**
     *描述: 确认框，是否输入正确废品数
     *日期：2023/4/14
     **/
    private void isConfirm(String strAction,String strActionId,String qcstatus,String qrcode){
        MyAlertDialog myAlertDialog = new MyAlertDialog(SubDetailForMultipleActivity.this);
        myAlertDialog.setConfirmResult(confirmResult);

        //获取提示信息
        String msg="";
        for(int i= 0;i<multipleDetailAdapter.getCount();i++){
            LinearLayout linearLayout = (LinearLayout)subMultipleView.getAdapter().getView(i,null,null);
            TextView txtMultipleDetailProductName = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProductName);
            String strQuantity = multipleDetailAdapter.getQuantity(i);
            String strBadQuantity = multipleDetailAdapter.getBadQuantity(i);

            if(msg.equals("")||msg.isEmpty()){
                msg = "零件:"+txtMultipleDetailProductName.getText().toString()+",良品数:"+strQuantity+",废品数:"+strBadQuantity;
            }else{
                msg = msg+"\n"+"零件:"+txtMultipleDetailProductName.getText().toString()+",良品数:"+strQuantity+",废品数:"+strBadQuantity;
            }

        }

        MyAlertDialog.myShowConfirmDialog(SubDetailForMultipleActivity.this,"确认信息",msg,strAction,strActionId,qcstatus,qrcode);
    }

    /**
    *描述: 检查保存数据合理性，合理则写入本地数据库
    *日期：2022/6/10
    **/
    private void saveData(String strAction,String strActionId,String qcstatus,String qrcode){
        //增加确认按钮
        isConfirm(strAction,strActionId,qcstatus,qrcode);
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

                statusCode = "-1";
                statusDescription = "保存失败";

                //检查是否存在数据
                int iCount = hzDb.productDao().getCount();
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
                    TextView txtMultipleDetailStationDocno = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailStationDocno);
                    TextView txtMultipleConnectProcess = (TextView)linearLayout.findViewById(R.id.txtMultipleConnectProcess);
                    TextView txtMultiplePlanDocno = (TextView)linearLayout.findViewById(R.id.txtMultiplePlanDocno);
                    TextView txtMultipleVersion = (TextView)linearLayout.findViewById(R.id.txtMultipleVersion);

                    String strProductCode = txtMultipleDetailProductCode.getText().toString();
                    String strDocno = txtMultipleDetailDocno.getText().toString();
                    String strProcessId = txtMultipleDetailProcessId.getText().toString();
                    String strProcess = txtMultipleDetailProcess.getText().toString();
                    String strDevice = txtMultipleDetailDevice.getText().toString();
                    String strLots = txtMultipleDetailLots.getText().toString();
                    String strEmployee = txtMultipleDetailEmployee.getText().toString();
                    String strPlanDate = txtMultipleDetailPlanDate.getText().toString();
                    String strStationDocno = txtMultipleDetailStationDocno.getText().toString();
                    String strConnectProcess = txtMultipleConnectProcess.getText().toString();
                    String strPlanDocno = txtMultiplePlanDocno.getText().toString();
                    String strVersion = txtMultipleVersion.getText().toString();
                    String strModStatus = multipleDetailAdapter.getItemValue(i,"ModStatus");

                    //连线生产，只末序数据传入，报工数据由组合单号产生
                    if(strProcessEnd.equals("Y")){
                        strProcessInitId = formatConnectConditions("ProcessId","/"); //连线工序项次
                        strProcessInit = formatConditions("Process","/"); //连线工序号
                        if(!strConnectProcess.equals("END")){
                            continue;
                        }
                    }

                    //同模生产，实物相同零件只取第一项数量
                    String strQuantity,strBadQuantity;
                    if(strModStatus.equals("2")||strModStatus.equals("3")){
                        strQuantity = multipleDetailAdapter.getQuantity(0);
                        strBadQuantity = multipleDetailAdapter.getBadQuantity(0);
                    }else{
                        strQuantity = multipleDetailAdapter.getQuantity(i);
                        strBadQuantity = multipleDetailAdapter.getBadQuantity(i);
                    }
                    float fQuantity = 0;
                    if(!strQuantity.equals("")&&!strQuantity.isEmpty()){
                        fQuantity = Float.valueOf(strQuantity);
                    }
                    float fBadQuantity = 0;
                    if(!strBadQuantity.equals("")&&!strBadQuantity.isEmpty()){
                        fBadQuantity = Float.valueOf(strBadQuantity);
                    }

                    //生成XML文件
                    genRecordSetStr(strAction,strActionId,qcstatus,strProductCode,strDocno,"",strProcessId,strProcess,strDevice,strLots,strQuantity,"",i,strEmployee,qrcode,strGroupId,diffQrcode,strBadQuantity,"",strStationDocno,strConnectProcess,strPlanDocno,strVersion);

                    //写入sqllite
                    productEntity = new ProductEntity(strDocno, strPlanDocno,0,strVersion,strProductCode,strProcessId,strProcess,strDevice,strEmployee,strLots,fQuantity,strPlanDate,fBadQuantity,strStationDocno,strConnectProcess,strConnectDocno);
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
                    MyAlertDialog.myShowAlertDialog(SubDetailForMultipleActivity.this,"错误信息",statusDescription);
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
    private void savePrintToT100(String strAction,String strActionId,String qcstatus,String station){
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
                isCommit = "Y";

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
                        float fBadQuantity = productEntityList.get(i).getBadquantity();
                        String strQuantity = String.valueOf(fQuantity);
                        String strBadQuantity = String.valueOf(fBadQuantity);
                        String strPlanDate = productEntityList.get(i).getPlanDate();
                        String strStationDocno = productEntityList.get(i).getStationDocno();
                        String strConnectProcess = productEntityList.get(i).getConnectProcess();
                        String strPlanDocno = productEntityList.get(i).getPlanDocno();
                        String strVersion = productEntityList.get(i).getVersion();

                        //生成xml文件
                        genRecordSetStr(strAction,strActionId,qcstatus,strProductCode,strDocno,strPlanDate,strProcessId,strProcess,strDevice,strLots,strQuantity,"",i,strEmployee,qrcode,strGroupId,diffQrcode,strBadQuantity,station,strStationDocno,strConnectProcess,strPlanDocno,strVersion);
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

                    //执行完成删除本地数据
                    hzDb.productDao().deleteAll();

                    //获取返回信息
                    for(Map<String,Object> mStatus: mapResponseStatus){
                        statusCode = mStatus.get("statusCode").toString();
                        statusDescription = mStatus.get("statusDescription").toString();
                    }
                }

                e.onNext(statusCode);
                e.onNext(statusDescription);
                e.onNext(isCommit);
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
                lastClick = System.currentTimeMillis();
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage()+",总耗时:"+String.valueOf((lastClick-firstClick)/1000)+"秒",0,0);

                if(loadingDialog != null){
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }

            @Override
            public void onComplete() {
                lastClick = System.currentTimeMillis();

                if(statusCode.equals("0")){
                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription+",总耗时:"+String.valueOf((lastClick-firstClick)/1000)+"秒", 1, 1);
                    finish();
                }else{
                    MyToast.myShow(SubDetailForMultipleActivity.this,statusDescription,0,0);
                }

                if(loadingDialog != null){
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }
        });
    }

    /**
    *描述: 提交request请求至ERP
    *日期：2022/6/10
    **/
    private void saveAssistToT100(String strAction, String strActionId, String qcstatus, String qrcode, String station){
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
                TextView txtMultipleDetailStationDocno = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailStationDocno); //组合单号
                TextView txtMultipleConnectProcess = (TextView)linearLayout.findViewById(R.id.txtMultipleConnectProcess);
                TextView txtMultiplePlanDocno = (TextView)linearLayout.findViewById(R.id.txtMultiplePlanDocno);
                TextView txtMultipleVersion = (TextView)linearLayout.findViewById(R.id.txtMultipleVersion);

                //获取数量,同模生产实物相同只取第一项数量
                String strQuantity;
                String strBadQuantity;
                String strModStatus = multipleDetailAdapter.getItemValue(i,"ModStatus");
                if(strModStatus.equals("2")||strModStatus.equals("3")){
                    strQuantity = multipleDetailAdapter.getQuantity(0);
                    strBadQuantity = multipleDetailAdapter.getBadQuantity(0);
                }else{
                    strQuantity = multipleDetailAdapter.getQuantity(i);
                    strBadQuantity = multipleDetailAdapter.getBadQuantity(i);
                }

                String strProductCode = txtMultipleDetailProductCode.getText().toString();
                String strDocno = txtMultipleDetailDocno.getText().toString();
                String strPlanDate = txtMultipleDetailPlanDate.getText().toString();
                String strProcessId = txtMultipleDetailProcessId.getText().toString();
                String strProcess = txtMultipleDetailProcess.getText().toString();
                String strDevice = txtMultipleDetailDevice.getText().toString();
                String strLots = txtMultipleDetailLots.getText().toString();
                String strEmployee = txtMultipleDetailEmployee.getText().toString();
                String strStationDocno = txtMultipleDetailStationDocno.getText().toString(); //组合单号
                String strConnectProcess = txtMultipleConnectProcess.getText().toString();
                String strPlanDocno = txtMultiplePlanDocno.getText().toString();
                String strVersion = txtMultipleVersion.getText().toString();
                String strProductDocno = multipleDetailAdapter.getProductDocno(i);   //报工单号

                //连线生产，生产准备10和报首检11只首工序产生记录,其他按照人员任务每序产生
                if(strProcessEnd.equals("Y")){
                    if(strActionId.equals("10")||strActionId.equals("11")){
                        if(!strConnectProcess.equals("INIT")){
                            continue;
                        }
                    }
                }

                //生成XML文件
                genRecordSetStr(strAction,strActionId,qcstatus,strProductCode,strDocno,strPlanDate,strProcessId,strProcess,strDevice,strLots,strQuantity,strProductDocno,i,strEmployee,qrcode,strGroupId,"",strBadQuantity,station,strStationDocno,strConnectProcess,strPlanDocno,strVersion);
            }

            //发送请求至T100 ERP
            saveData2ToT100(strAction,strActionId);
        }
    }

    /**
    *描述: XML文件生成
    *日期：2022/6/10
    **/
    private void genRecordSetStr(String action,String actionid,String qcstatus,String strProductCode,String strDocno,String strPlanDate,String strProcessId,String strProcess,String strDevice,String strLots,String strQuantity,String strProductDocno,int i,String strEmployee,String qrcode,String sGroupId,String qrcode2,String strBadQuantity,String station,String strStationDocno,String strConnectProcess,String strPlanDocno,String strVersion){
        long timeCurrentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        String currentTime = simpleTimeFormat.format(timeCurrentTimeMillis);
        String currentDate = simpleDateFormat.format(new Date());

        //是否升级，用于代码切换使用
        String sIsUpdate = "Y";

        //生成数据集合
        mRecordSet = mRecordSet + "&lt;Master name=\"sffb_t\" node_id=\""+i+"\"&gt;\n"+
                "&lt;Record&gt;\n"+
                "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                "&lt;Field name=\"sffbdocdt\" value=\""+strPlanDate+"\"/&gt;\n"+
                "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                "&lt;Field name=\"sffb004\" value=\""+ strGroupId +"\"/&gt;\n"+  //班次
                "&lt;Field name=\"sffb005\" value=\""+ strDocno +"\"/&gt;\n"+  //工单单号
                "&lt;Field name=\"sffbseq\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                "&lt;Field name=\"sffb010\" value=\""+ strDevice +"\"/&gt;\n"+  //机器编号
                "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //批量生产止日期
                "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //批量生产止时间
                "&lt;Field name=\"sffb029\" value=\""+ strProductCode +"\"/&gt;\n"+  //报工料号
                "&lt;Field name=\"sffb017\" value=\""+ strQuantity +"\"/&gt;\n"+  //良品数量
                "&lt;Field name=\"sffb018\" value=\""+ strBadQuantity +"\"/&gt;\n"+  //废品数量
                "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //工序
                "&lt;Field name=\"lots\" value=\""+ strLots +"\"/&gt;\n"+  //批次
                "&lt;Field name=\"sffbdocno\" value=\""+ strProductDocno +"\"/&gt;\n"+  //报工单号
                "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //状态
                "&lt;Field name=\"planno\" value=\""+ strPlanDocno +"\"/&gt;\n"+  //计划单号
                "&lt;Field name=\"planseq\" value=\""+ txtMultipleInputCount.getText().toString() +"\"/&gt;\n"+  //报工次数
                "&lt;Field name=\"planuser\" value=\""+ strEmployee +"\"/&gt;\n"+  //生产人员
                "&lt;Field name=\"errorlots\" value=\""+ strErrorLots +"\"/&gt;\n"+  //异常批次
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
                "&lt;Field name=\"sffbuc033\" value=\""+ station +"\"/&gt;\n"+  //机器人组合
                "&lt;Field name=\"sffbuc040\" value=\""+ strStationDocno +"\"/&gt;\n"+  //机器人组合单号
                "&lt;Field name=\"sffbuc041\" value=\""+ strConnectDocno +"\"/&gt;\n"+  //连线单号
                "&lt;Field name=\"mergeqty\" value=\""+ iTotal +"\"/&gt;\n"+  //尾数合箱量
                "&lt;Field name=\"isupdate\" value=\""+ sIsUpdate +"\"/&gt;\n"+  //代码升级
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
                if(loadingDialog != null){
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(action.equals("print")){
                        finish();
                    }else{
                        getMultipleDetailData();
                    }

                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription, 1, 1);
                }else{
                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription, 0, 1);
                }

                if(loadingDialog != null){
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }
        });
    }
}