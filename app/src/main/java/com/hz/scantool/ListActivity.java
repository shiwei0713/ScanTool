package com.hz.scantool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.helper.SharedHelper;
import com.hz.scantool.helper.WebServiceHelper;
import com.hz.scantool.models.Company;
import com.hz.scantool.models.UserInfo;
import com.hz.scantool.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class ListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabs;
    private String nerworkType;
    Company company;
    Context mContext;
    SharedHelper sharedHelper;
    private int intCount;
    private int intIndex;
    private TextView txtTask1;
    private TextView txtTask2;
    private TextView txtTask3;

    private LinearLayout linearStaus;
    private LinearLayout linearQuery;
    private LinearLayout linearFlag;

    private Button btnFlag1;
    private Button btnFlag2;
    private Button btnFlag3;
    private Button btnFlag4;
    Bundle bundle;

    private String qrSid;
    private String productCode;

    //创建Handler
    private final Handler listHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            List<Map<String,Object>> strResponseList = (List<Map<String,Object>>)msg.obj;
            for(Map<String,Object> m: strResponseList){
                if(!m.get("statusCode").toString().equals("0")){
                    MyToast.myShow(ListActivity.this,m.get("statusDescription").toString(),0);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //初始化存储信息
        mContext=getApplicationContext();
        sharedHelper=new SharedHelper(mContext);

        //初始化textview
        txtTask1 = findViewById(R.id.txtTask1);
        txtTask2 = findViewById(R.id.txtTask2);
        txtTask3 = findViewById(R.id.txtTask3);
        linearStaus = findViewById(R.id.linearStaus);
        linearQuery = findViewById(R.id.linearQuery);
        linearFlag = findViewById(R.id.linearFlag);
        btnFlag1 = findViewById(R.id.btnFlag1);
        btnFlag2 = findViewById(R.id.btnFlag2);
        btnFlag3 = findViewById(R.id.btnFlag3);
        btnFlag4 = findViewById(R.id.btnFlag4);

        //获取传入参数
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        String strTitle=bundle.getString("title");
        intCount=bundle.getInt("count");
        intIndex=bundle.getInt("index");

        //动态显示工具栏
        linearFlag.setVisibility(View.GONE);
        linearQuery.setVisibility(View.GONE);
        linearStaus.setVisibility(View.GONE);
        if(intIndex == 7){
            linearStaus.setVisibility(View.VISIBLE);
        }else{
            if(intIndex == 1){
                linearFlag.setVisibility(View.VISIBLE);
            }else{
                linearQuery.setVisibility(View.VISIBLE);
            }
        }

        //筛选按钮事件
        btnFlag1.setOnClickListener(new refreshClickListener());
        btnFlag2.setOnClickListener(new refreshClickListener());
        btnFlag3.setOnClickListener(new refreshClickListener());
        btnFlag4.setOnClickListener(new refreshClickListener());

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.listToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //横向Tab导航栏定义,通过setItem传入参数
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        sectionsPagerAdapter.setItem(intCount,intIndex);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //初始化查询条件，并绑定事件
//        txtQueryQcName = findViewById(R.id.txtQueryQcName);
//        txtQueryQcName.addTextChangedListener(textWatcher);

        //浮动按钮扫描事件初始化
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(ListActivity.this);
                intentIntegrator.setTimeout(5000);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //开始扫描
                intentIntegrator.initiateScan();
            }
        });

        //浮动按钮刷新事件初始化
        FloatingActionButton fabRefresh = findViewById(R.id.fabRefresh);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshView();
            }
        });

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

    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SCANACTION)){
                String qrContent = intent.getStringExtra("scannerdata");

                if(qrContent!=null && qrContent.length()!=0){

                    //解析二维码
                    String[] qrCodeValue = qrContent.split("_");
                    int qrIndex = qrContent.indexOf("_");
                    if(qrIndex==-1){
//                        Toast.makeText(context,"条码错误:"+qrContent,Toast.LENGTH_SHORT).show();
                        MyToast.myShow(context,"条码错误:"+qrContent,0);
                    }else{
                        //单据类别
                        Boolean isSale = false;
                        String doctype = "";
                        String docno = qrCodeValue[0].trim();
                        if(!docno.isEmpty()) {
                            String[] docList = docno.split("-");
                            String docSlip = docList[0].trim();
                            doctype = docSlip.substring(1,3);

                            if(doctype.equals("XM") && docList.length == 2 && (intIndex == 1 || intIndex == 5)){
                                isSale = true;
                            }
                        }

                        if(isSale){
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
                            bundle.putInt("index",intIndex);

                            intent.putExtras(bundle);
                            startActivity(intent);
                        }else{
                            if(intIndex == 7){
                                qrSid = qrCodeValue[0].trim();
                                productCode = qrCodeValue[1].trim();
                                updateStorageCheck();
                            }else{
                                intent = new Intent(context,DetailActivity.class);
                                //设置传入参数
                                bundle=new Bundle();
                                bundle.putString("qrCode",qrContent);
                                bundle.putString("docno",docno);
                                bundle.putInt("index",intIndex);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }
                    }
                }else{
//                    Toast.makeText(context,"扫描失败,请重新扫描!"+qrContent,Toast.LENGTH_SHORT).show();
                    MyToast.myShow(context,"扫描失败,请重新扫描",0);
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(scanReceiver);
    }

    //筛选条件查询
    private class refreshClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                //原材料
                case R.id.btnFlag1:

                    break;
                //标准件
                case R.id.btnFlag2:

                    break;
                //外购件
                case R.id.btnFlag3:

                    break;
                //自制件
                case R.id.btnFlag4:

                    break;
                default:

                    break;
            }

            refreshView();
        }
    }

    private void refreshView(){
        //获取任务数
//        getTaskCount();

        //创建listview
        sectionsPagerAdapter = new SectionsPagerAdapter(ListActivity.this, getSupportFragmentManager());
        sectionsPagerAdapter.setItem(intCount,intIndex);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private void updateStorageCheck(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //初始化网络类型和营运据点
//                company=new Company();
//                Map<String,String> data=sharedHelper.readShared();
//                nerworkType = data.get("network");
//                company.setSite(data.get("userSite"));

                //初始化T100服务名
                String webServiceName = "StorageCheckRequestUpdate";

                //设置传入请求参数
                StringBuilder strWebRequestConten= new StringBuilder();
                strWebRequestConten.append("&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcah_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcahsite\" value=\""+company.getCode()+"\"/&gt;\n"+
                        "&lt;Field name=\"bcahent\" value=\"10\"/&gt;\n"+
                        "&lt;Field name=\"bcah002\" value=\""+productCode+"\"/&gt;\n"+
                        "&lt;Field name=\"bcah018\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;Field name=\"qrsid\" value=\""+qrSid+"\"/&gt;\n"+
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcahseq\" value=\"1.0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Detail&gt;\n"+
                        "&lt;Memo/&gt;\n"+
                        "&lt;Attachment count=\"0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Master&gt;\n"+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n");

                //设置WebService参数
                WebServiceHelper webServiceHelper=new WebServiceHelper();
                webServiceHelper.setWebKey("16baae6c40b922d8ddb12a0320d8ea1d");
                webServiceHelper.setWebTimestamp("20201114083106031");
                webServiceHelper.setWebName(webServiceName);
                webServiceHelper.setWebUrl(nerworkType);
                webServiceHelper.setWebSite(company.getCode());
                webServiceHelper.setWebRequestContent(strWebRequestConten);

                //发送WebService请求,并返回结果
                String strResponse = "";
                try{
                    strResponse=webServiceHelper.sendWebRequest();
                }catch (Exception e){
                    e.printStackTrace();
                }

                //获取WebService相应代码
                Integer iResponseCode=webServiceHelper.getWebResponseCode();

                //存储列表
                List<Map<String,Object>> taskList = new ArrayList<Map<String,Object>>();
                String statusCode;
                String statusSqlcode;
                String statusDescription;

                if(iResponseCode==200){
                    Map<String,Object> map = new HashMap<String,Object>();

                    //检查索引
                    Integer iTaskIndex=strResponse.indexOf("Status",1);
                    if (iTaskIndex>-1){
                        //当前任务数
                        String strStatus=strResponse.substring(strResponse.indexOf("Status",1),strResponse.length()).replace("\"","");
                        statusCode=strStatus.substring(strStatus.indexOf("code",1)+5,strStatus.indexOf("sqlcode",1)-1);
                        statusSqlcode = strStatus.substring(strStatus.indexOf("sqlcode",1)+8,strStatus.indexOf("description",1)-1);
                        statusDescription = strStatus.substring(strStatus.indexOf("description",1)+12,strStatus.indexOf("&gt;",1)-1);

                        map.put("statusCode",statusCode.trim());
                        map.put("statusSqlcode",statusSqlcode.trim());
                        map.put("statusDescription",statusDescription.trim());
                        taskList.add(map);
                    }
                }

                Message message = new Message();
                message.obj = taskList;
                listHandler.sendMessage(message);
            }
        }).start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE){
            IntentResult intentResult = IntentIntegrator.parseActivityResult(resultCode,data);
            final String qrContent = intentResult.getContents();
            if(qrContent!=null && qrContent.length()!=0){
                Intent intent = null;
                //解析二维码
                String[] qrCodeValue = qrContent.split("_");
                int qrIndex = qrContent.indexOf("_");
                if(qrIndex==-1){
//                    Toast.makeText(this,"条码错误:"+qrContent,Toast.LENGTH_SHORT).show();
                    MyToast.myShow(this,"条码错误"+qrContent,0);
                }else{
                    intent = new Intent(this,DetailListActivity.class);
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
                    bundle.putInt("index",intIndex);

                    intent.putExtras(bundle);
                    startActivity(intent);
//                    Toast.makeText(this,"扫描:"+qrContent,Toast.LENGTH_SHORT).show();
                    MyToast.myShow(this,"扫描:"+qrContent,2);
                }
            }else{
//                Toast.makeText(this,"扫描失败,请重新扫描!"+qrContent,Toast.LENGTH_SHORT).show();
                MyToast.myShow(this,"扫描失败,请重新扫描"+qrContent,0);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏返回按钮事件定义
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}