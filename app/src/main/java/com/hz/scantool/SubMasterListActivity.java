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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubMasterListItemAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class SubMasterListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private String strTitle="";
    private String strType;
    private String strWhere;
    private String strJsonType;
    private int actionId;
    private String statusCode;
    private String statusDescription;
    private Bundle bundle;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    private LinearLayout toolFlag;
    private LinearLayout toolQuery;
    private LinearLayout toolCount;

    private TextView btnSubMasterListTitle;
    private Button btnSubMasterQcFlag1;
    private Button btnSubMasterQcFlag2;
    private Button btnSubMasterQcFlag3;
    private Button btnSubMasterQcFlag4;
    private Button btnSubMasterQcQuery;
    private EditText txtQueryQcName;
    private EditText txtQueryQcbDate;
    private EditText txtQueryQceDate;
    private TextView txtLabel1;
    private TextView txtLabel2;
    private TextView txtSubListTask1;
    private TextView txtSubListTask2;
    private ProgressBar subMasterQcProgressBar;
    private ListView subMasterQcView;
    private SubMasterListItemAdapter subMasterListItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_master_list);

        //初始化传入参数
        //初始化控件
        initBundle();
        initView();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subMasterQcToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //显示QC清单
        initQueryCondition(0);
        getSubQcListData();
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubMasterListActivity.this);
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

    //获取传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        actionId = bundle.getInt("btnId");
        strTitle = bundle.getString("title");
    }

    //设置查询日期
    private String setQueryDate(int interval){
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,interval);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date date =  calendar.getTime();
        String strDate = simpleDateFormat.format(date);

        return strDate;
    }

    private void initView(){
        toolFlag = findViewById(R.id.toolFlag);
        toolQuery = findViewById(R.id.toolQuery);
        toolCount = findViewById(R.id.toolCount);

        btnSubMasterListTitle =  findViewById(R.id.btnSubMasterListTitle);
        btnSubMasterQcFlag1 = findViewById(R.id.btnSubMasterQcFlag1);
        btnSubMasterQcFlag2 = findViewById(R.id.btnSubMasterQcFlag2);
        btnSubMasterQcFlag3 = findViewById(R.id.btnSubMasterQcFlag3);
        btnSubMasterQcFlag4 = findViewById(R.id.btnSubMasterQcFlag4);
        btnSubMasterQcQuery = findViewById(R.id.btnSubMasterQcQuery);
        txtQueryQcName = findViewById(R.id.txtQueryQcName);
        txtQueryQcbDate = findViewById(R.id.txtQueryQcbDate);
        txtQueryQceDate = findViewById(R.id.txtQueryQceDate);
        txtLabel1 = findViewById(R.id.txtLabel1);
        txtLabel2 = findViewById(R.id.txtLabel2);
        txtSubListTask1 = findViewById(R.id.txtSubListTask1);
        txtSubListTask2 = findViewById(R.id.txtSubListTask2);
        subMasterQcProgressBar = findViewById(R.id.subMasterQcProgressBar);
        subMasterQcView = findViewById(R.id.subMasterQcView);

        //声明按钮ID和图片ID
        int[] btnId = new int[]{R.id.txtSubListTask1, R.id.txtSubListTask2};
        int[] imgId = new int[]{R.drawable.task1, R.drawable.task2};
        int[] titleId = new int[]{R.string.sub_master_content_quantity,R.string.sub_master_content_quantitypcs};

        //初始化按钮和图片
        TextView textAction;
        Drawable drawable;

        //设置按钮样式
        for(int i=0;i<btnId.length;i++){
            textAction=findViewById(btnId[i]);
            drawable=getResources().getDrawable(imgId[i]);
            drawable.setBounds(0,0,50,50);
            textAction.setCompoundDrawables(drawable,null,null,null);
            textAction.setCompoundDrawablePadding(10);
            textAction.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            textAction.setText(getResources().getString(titleId[i]));
        }

        toolCount.setVisibility(View.GONE);
        switch (actionId){
            //IQC检验
            case 11:
                strJsonType = "iqc";
                strType = "1";
                break;
            //PQC检验
            case 12:
                strJsonType = "pqc";
                strType = "1";
                break;
            //FQC检验
            case 15:
                strJsonType = "fqc";
                strType = "1";
                break;
            //OQC检验
            case 16:
                strJsonType = "oqc";
                strType = "1";
                break;
            //库存检验
            case 17:
                strJsonType = "Inventoryqc";
                strType = "1";
                break;
            //任务分配
            case 51:
                strType = "5";
                strJsonType = "salelist";
                break;
            //销售备货
            case 52:
                strType = "5";
                strJsonType = "stocklist";
                toolFlag.setVisibility(View.GONE);
                break;
            //异常备货
            case 53:
                strType = "53";
                strJsonType = "erpqr";
                toolFlag.setVisibility(View.GONE);
                toolQuery.setVisibility(View.GONE);
                toolCount.setVisibility(View.VISIBLE);
                break;
            //销售退回
            case 55:
                strType = "5";
                break;
        }

        btnSubMasterQcFlag1.setOnClickListener(new queryClickListener());
        btnSubMasterQcFlag2.setOnClickListener(new queryClickListener());
        btnSubMasterQcFlag3.setOnClickListener(new queryClickListener());
        btnSubMasterQcFlag4.setOnClickListener(new queryClickListener());
        btnSubMasterQcQuery.setOnClickListener(new queryClickListener());
        txtLabel1.setOnClickListener(new queryClickListener());
        txtLabel2.setOnClickListener(new queryClickListener());
    }

    //初始化查询条件
    private void initQueryCondition(int isAll){
        if(isAll == 0){
            strWhere = "1=1";
        }else{
            //异常备货
            if(actionId == 53){
                strWhere = "to_char(bcaamoddt,'YYYY-MM-DD') = '"+setQueryDate(0)+"'";
            }else{
                strWhere = "qcbadocdt BETWEEN to_date('"+txtQueryQcbDate.getText().toString()+"','YYYY-MM-DD') AND to_date('"+txtQueryQceDate.getText().toString()+"','YYYY-MM-DD')";
            }
        }

        switch (actionId){
            //IQC检验
            case 11:
                strWhere = strWhere+" AND qcba000='1'";
                break;
            //PQC检验
            case 12:
                strWhere = strWhere+" AND qcba000='3'";
                break;
            //FQC检验
            case 15:
                strWhere = strWhere+" AND qcba000='2'";
                break;
            //OQC检验
            case 16:
                strWhere = strWhere+" AND qcba000='4'";
                break;
            //库存检验
            case 17:
                strWhere = strWhere+" AND qcba000='5'";
                break;
        }
    }

    //刷新统计数
    private void refreshCount(){
        int iQuantity = 0;
        int iQuantityPcs=0;
        int iQuantityTotal = 0;
        int iQuantityPcsTotal=0;

        for(Map<String,Object> mData: mapResponseList) {
            String sQuantity = mData.get("Quantity").toString();
            String sQuantityPcs = mData.get("QuantityPcs").toString();

            if(!sQuantity.isEmpty()){
                iQuantity =Integer.parseInt(sQuantity);
            }else{
                iQuantity = 0;
            }

            if(!sQuantityPcs.isEmpty()){
                iQuantityPcs = Integer.parseInt(sQuantityPcs);
            }else{
                iQuantityPcs = 0;
            }

            iQuantityTotal = iQuantityTotal + iQuantity;
            iQuantityPcsTotal = iQuantityPcsTotal + iQuantityPcs;
        }

        txtSubListTask1.setText(getResources().getString(R.string.sub_master_content_quantity)+String.valueOf(iQuantityTotal));
        txtSubListTask2.setText(getResources().getString(R.string.sub_master_content_quantitypcs)+String.valueOf(iQuantityPcsTotal));
    }

    private class queryClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSubMasterQcFlag1:
                    btnSubMasterQcFlag1.setSelected(true);
                    btnSubMasterQcFlag2.setSelected(false);
                    btnSubMasterQcFlag3.setSelected(false);
                    btnSubMasterQcFlag4.setSelected(false);
                    strWhere = " imaa009='101'";
                    break;
                case R.id.btnSubMasterQcFlag2:
                    btnSubMasterQcFlag1.setSelected(false);
                    btnSubMasterQcFlag2.setSelected(true);
                    btnSubMasterQcFlag3.setSelected(false);
                    btnSubMasterQcFlag4.setSelected(false);
                    strWhere = " imaa009='102'";
                    break;
                case R.id.btnSubMasterQcFlag3:
                    btnSubMasterQcFlag1.setSelected(false);
                    btnSubMasterQcFlag2.setSelected(false);
                    btnSubMasterQcFlag3.setSelected(true);
                    btnSubMasterQcFlag4.setSelected(false);
                    strWhere = " imaa009='103'";
                    break;
                case R.id.btnSubMasterQcFlag4:
                    btnSubMasterQcFlag1.setSelected(false);
                    btnSubMasterQcFlag2.setSelected(false);
                    btnSubMasterQcFlag3.setSelected(false);
                    btnSubMasterQcFlag4.setSelected(true);
                    strWhere = " imaa009 IN ('104','105')";
                    break;
                case R.id.btnSubMasterQcQuery:
                    initQueryCondition(1);
                    break;
                case R.id.txtLabel1:
                    txtQueryQcbDate.setText(setQueryDate(0));
                    txtQueryQceDate.setText(setQueryDate(2));
                    initQueryCondition(1);
                    break;
                case R.id.txtLabel2:
                    txtQueryQcbDate.setText(setQueryDate(0));
                    txtQueryQceDate.setText(setQueryDate(6));
                    break;
            }

            //执行接口显示数据
            getSubQcListData();
        }
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

    @Override
    protected void onRestart() {
        super.onRestart();

        //执行接口显示数据
        initQueryCondition(53);
        getSubQcListData();
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

    //手机调用摄像头扫描
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
    private void scanResult(String qrContent,Context context, Intent intent){
        //解析二维码
        String[] qrCodeValue = qrContent.split("_");
        int qrIndex = qrContent.indexOf("_");
        if(qrIndex==-1){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            Boolean isOqc = false;
            String doctype = "";
            String docno = qrCodeValue[0].trim();
            if(!docno.isEmpty()) {
                String[] docList = docno.split("-");
                String docSlip = docList[0].trim();
                doctype = docSlip.substring(1,3);

                if(doctype.equals("XM") && docList.length == 2){
                    isOqc = true;
                }
            }

            int index = 0;
            //16:OQC检验;11:IQC检验;15:FQC检验
            if(actionId == 16 || actionId == 11 || actionId == 15){
                index=1;
            }else if(actionId ==52){
                index=5;
            }else{
                index=actionId;
            }

            if(actionId == 16 || actionId ==52){
                if(isOqc){
                    intent = new Intent(context,DetailListActivity.class);
                    //设置传入参数
                    bundle=new Bundle();
                    bundle.putString("txtListDocno",qrCodeValue[0].trim());
                    bundle.putString("txtListProductCode","");
                    bundle.putString("txtListProductName","");
                    bundle.putString("txtListProductModels","");
                    bundle.putString("txtListProducerId",qrCodeValue[1].trim());
                    bundle.putString("txtListProducer",qrCodeValue[2].trim());
                    bundle.putString("txtListStockId",qrCodeValue[3].trim());
                    bundle.putString("txtListStock",qrCodeValue[4].trim());
                    bundle.putString("txtListQuantity","");
                    bundle.putInt("index",index);

                    intent.putExtras(bundle);
                    startActivity(intent);
                }else{
                    MyToast.myShow(SubMasterListActivity.this,"OQC需先扫描备货单,请重新扫描",0,0);
                }
            }else{
                if(isOqc){
                    MyToast.myShow(SubMasterListActivity.this,"只有OQC才可扫描备货单,请重新扫描",0,0);
                }else{
                    intent = new Intent(context,DetailActivity.class);
                    //设置传入参数
                    bundle=new Bundle();
                    bundle.putString("qrCode",qrContent);
                    bundle.putString("docno",docno);
                    bundle.putInt("index",index);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        }
    }

    //显示QC清单
    private void getSubQcListData(){
        //显示进度条
        subMasterQcProgressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppListGet";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonQcData(strResponse,strJsonType);

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
                            MyToast.myShow(SubMasterListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = subMasterQcProgressBar.getProgress();
                            progress = progress + 50;
                            subMasterQcProgressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubMasterListActivity.this,"无入库数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubMasterListActivity.this,"网络错误",0,0);
                subMasterQcProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                subMasterListItemAdapter = new SubMasterListItemAdapter(mapResponseList,getApplicationContext(),strType);
                subMasterQcView.setAdapter(subMasterListItemAdapter);

                subMasterQcProgressBar.setVisibility(View.GONE);
                refreshCount();
            }
        });
    }
}