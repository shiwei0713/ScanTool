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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubListAdapter;
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

public class SubListActivity extends AppCompatActivity {
    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private int intIndex;
    private String strType;
    private String strTitle;
    private String strWhere;
    private String statusCode;
    private String statusDescription;
    private String strProg;

    private ListView listView;
    private SubListAdapter subDetailAdapter;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    private Button btnFlag1;
    private Button btnFlag2;
    private Button btnSubQuery;
    private TextView txtSubQueryDeptNameTitle;
    private TextView txtSubLabel1;
    private TextView txtSubLabel2;
    private EditText txtSubQuerybDate;
    private EditText txtSubQueryeDate;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_list);

        //初始化参数
        initBundle();

        //初始化控件
        initView();
        initQueryCondition(0);

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

        //初始化数据
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubListActivity.this);
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

        initQueryCondition(9);
        getSubListData();
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
            if(intIndex == 2){
                genT100StockLot(qrContent);
            }else{
                MyToast.myShow(context,"只有入库才可扫描产品条码",2,1);
            }
        }
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

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        intIndex = bundle.getInt("index");
        strTitle = bundle.getString("title");
    }

    private void initView(){
        btnFlag1 = findViewById(R.id.btnFlag1);
        btnFlag2 = findViewById(R.id.btnFlag2);
        btnSubQuery = findViewById(R.id.btnSubQuery);
        txtSubQueryDeptNameTitle = findViewById(R.id.txtSubQueryDeptNameTitle);
        txtSubQuerybDate=findViewById(R.id.txtSubQuerybDate);
        txtSubQueryeDate=findViewById(R.id.txtSubQueryeDate);
        txtSubLabel1 = findViewById(R.id.txtSubLabel1);
        txtSubLabel2 = findViewById(R.id.txtSubLabel2);
        listView = findViewById(R.id.subListView);
        progressBar = findViewById(R.id.progressBar);

        //初始化flag按钮状态
        setStrType(true);
        btnFlag1.setSelected(true);
        btnFlag2.setSelected(false);
        initFlagTitle();

        //初始化日期
        if(intIndex == 4){
            txtSubQuerybDate.setText(setQueryDate(1));
            txtSubQueryeDate.setText(setQueryDate(1));
        }else{
            txtSubQuerybDate.setText(setQueryDate(0));
            txtSubQueryeDate.setText(setQueryDate(0));
        }

        //绑定事件
        btnFlag1.setOnClickListener(new queryClickListener());
        btnFlag2.setOnClickListener(new queryClickListener());
        btnSubQuery.setOnClickListener(new queryClickListener());
        txtSubLabel1.setOnClickListener(new queryClickListener());
        txtSubLabel2.setOnClickListener(new queryClickListener());
        listView.setOnItemClickListener(new listItemClickListener());
    }

    //初始化标题
    private void initFlagTitle(){
        switch (intIndex){
            //完工入库
            case 2:
                btnFlag1.setText(getString(R.string.sub_list_flag21));
                btnFlag2.setVisibility(View.GONE);
                txtSubQueryDeptNameTitle.setText(getString(R.string.query_title_dept_in));
                break;
            //采购入库
            case 3:
                btnFlag1.setText(getString(R.string.sub_list_flag31));
                btnFlag2.setText(getString(R.string.sub_list_flag32));
                btnFlag2.setVisibility(View.GONE);
                txtSubQueryDeptNameTitle.setText(getString(R.string.query_title_dept_in));
                break;
            //生产备货
            case 4:
                if(strType.equals("4")){
                    txtSubQueryDeptNameTitle.setText(getString(R.string.query_title_dept));
                }else{
                    txtSubQueryDeptNameTitle.setText(getString(R.string.query_title_dept_out));
                }
                break;
        }
    }

    //初始化查询条件
    private void initQueryCondition(int iCondition){
        if(iCondition==0){
            strWhere = "1=1 AND sfeastus = 'Y'";
        }else{
            if(iCondition!=9){
                strWhere = "sfaa019 BETWEEN to_date('"+txtSubQuerybDate.getText().toString()+"','YYYY-MM-DD') AND to_date('"+txtSubQueryeDate.getText().toString()+"','YYYY-MM-DD') AND sfeastus = 'Y'";
            }
        }
    }

    //设置接口类型
    private void setStrType(Boolean bool){
        switch (intIndex){
            //完工入库
            case 2:
                if(bool){
                    strType = "2";
                    strProg="asft340";
                }else{
                    strType = "21";
                    strProg="asft340";
                }
                break;
            //采购入库
            case 3:
                if(bool){
                    strType = "3";
                    strProg="apmt570";
                }else{
                    strType = "31";
                    strProg="apmt571";
                }
                break;
            //生产备货
            case 4:
                if(bool){
                    strType = "4";
                }else{
                    strType = "41";
                }
                break;
        }
    }

    //按钮单击事件
    private class queryClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnFlag1:
                    btnFlag1.setSelected(true);
                    btnFlag2.setSelected(false);
                    setStrType(true);
                    if(strType.equals("4")){
                        txtSubQuerybDate.setText(setQueryDate(1));
                        txtSubQueryeDate.setText(setQueryDate(1));
                    }
                    break;
                case R.id.btnFlag2:
                    btnFlag1.setSelected(false);
                    btnFlag2.setSelected(true);
                    setStrType(false);
                    txtSubQuerybDate.setText(setQueryDate(0));
                    txtSubQueryeDate.setText(setQueryDate(0));
                    break;
                case R.id.btnSubQuery:
                    break;
                case R.id.txtSubLabel1:
                    txtSubQuerybDate.setText(setQueryDate(0));
                    txtSubQueryeDate.setText(setQueryDate(2));
                    break;
                case R.id.txtSubLabel2:
                    txtSubQuerybDate.setText(setQueryDate(0));
                    txtSubQueryeDate.setText(setQueryDate(6));
                    break;
            }
            initFlagTitle();
            initQueryCondition(1);
            getSubListData();
        }
    }

    //行单击事件
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(intIndex == 4){
                Intent intent = new Intent(SubListActivity.this,SubDetailListActivity.class);
                Bundle bundle = new Bundle();

                TextView txtViewDept = view.findViewById(R.id.txtViewDept);
                TextView txtViewDeptId = view.findViewById(R.id.txtViewDeptId);
                TextView txtViewStock = view.findViewById(R.id.txtViewStock);
                TextView txtViewStockId = view.findViewById(R.id.txtViewStockId);
                TextView txtViewDate = view.findViewById(R.id.txtViewDate);
                TextView textViewDocno = view.findViewById(R.id.txtViewDocno);
                String strDocType = subDetailAdapter.getItem(i,"DocType");

                bundle.putString("Docno",textViewDocno.getText().toString());
                bundle.putString("Dept",txtViewDept.getText().toString());
                bundle.putString("DeptId",txtViewDeptId.getText().toString());
                bundle.putString("Stock",txtViewStock.getText().toString());
                bundle.putString("StockId",txtViewStockId.getText().toString());
                bundle.putString("PlanDate",txtViewDate.getText().toString());
                bundle.putString("DocType",strDocType);
                bundle.putString("Type", strType);
                intent.putExtras(bundle);
                startActivity(intent);
            }
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
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
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
                            MyToast.myShow(SubListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = progressBar.getProgress();
                            progress = progress + 50;
                            progressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubListActivity.this,"无备料数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubListActivity.this,"网络错误",0,0);
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

    //产生仓储批
    private void genT100StockLot(String qrContent){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppGenStockLot";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"inaj_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"inajsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"inajent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+qrContent+"\"/&gt;\n"+
                        "&lt;Field name=\"inaj015\" value=\""+strProg+"\"/&gt;\n"+
                        "&lt;Field name=\"inajuser\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"inaj002\" value=\"1.0\"/&gt;\n"+
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
                mapResponseList = t100ServiceHelper.getT100ResponseData(strResponse,"docno");

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
                            MyToast.myShow(SubListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = progressBar.getProgress();
                            progress = progress + 50;
                            progressBar.setProgress(progress);
//                            MyToast.myShow(SubListActivity.this,statusDescription,1,0);
                        }
                    }
                }else{
                    MyToast.myShow(SubListActivity.this,"接口执行异常",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
//                MyToast.myShow(SubListActivity.this,"网络错误",0,0);
                showDetail();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                showDetail();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showDetail(){
        if(statusCode.equals("0")){
            String strDocno="";
            String strProducer="";
            String strPlanDate="";
            String strStock="";
            String strStorage="";
            String strQuantity="";
            String strQuantityPcs="";
            String strProductName = "";
            String strPlanQuantity = "";
            String strPlanQuantityPcs = "";
            String strStatus = "";
            String strContainer = "";

            if(mapResponseList.size()> 0){
                for(Map<String,Object> mResponse: mapResponseList){
                    strDocno = mResponse.get("Docno").toString();
                    strProducer = mResponse.get("Producer").toString();
                    strPlanDate = mResponse.get("PlanDate").toString();
                    strStock = mResponse.get("Stock").toString();
                    strStorage = mResponse.get("Storage").toString();
                    strQuantity = mResponse.get("Quantity").toString();
                    strQuantityPcs = mResponse.get("QuantityPcs").toString();
                    strPlanQuantity = mResponse.get("PlanQuantity").toString();
                    strPlanQuantityPcs = mResponse.get("PlanQuantityPcs").toString();
                    strStatus = mResponse.get("Status").toString();
                    strProductName = mResponse.get("ProductName").toString();
                    strContainer = mResponse.get("Container").toString();
                }
            }
            if(strStatus.equals("Y")){
                Intent intent = new Intent(SubListActivity.this,SubMasterListDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Docno", strDocno);
                bundle.putString("Producer", strProducer);
                bundle.putString("PlanDate", strPlanDate);
                bundle.putString("Stock", strStock);
                bundle.putString("Storage", strStorage);
                bundle.putString("Quantity", strQuantity);
                bundle.putString("QuantityPcs", strQuantityPcs);
                bundle.putString("ProductName", strProductName);
                bundle.putString("PlanQuantity", strPlanQuantity);
                bundle.putString("PlanQuantityPcs", strPlanQuantityPcs);
                bundle.putString("Status", strStatus);
                bundle.putString("Container", strContainer);
                bundle.putString("Type", strType);
                intent.putExtras(bundle);
                strWhere = "sfeadocno='"+strDocno+"'";
                startActivity(intent);
            }else{
                MyToast.myShow(SubListActivity.this,"托盘量:"+strPlanQuantity+",箱数:"+strPlanQuantityPcs+",已扫描量:"+strQuantity+",已扫描箱数:"+strQuantityPcs,1,1);
            }
        }
    }
}