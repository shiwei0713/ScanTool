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

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;
import com.hz.scantool.ui.main.SectionsPagerAdapter;

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

public class ListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabs;

    private String statusCode;
    private String statusDescription;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

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

        //浮动按钮扫描事件初始化
        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setVisibility(View.GONE);
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

    //PDA扫描注册
    @Override
    protected void onResume() {
        super.onResume();

        //注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SCANACTION);
        intentFilter.setPriority(Integer.MAX_VALUE);
        registerReceiver(scanReceiver,intentFilter);
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
                    MyToast.myShow(context,"扫描失败,请重新扫描",0);
                }
            }
        }
    };

    //取消PDA广播注册
    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(scanReceiver);
    }

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

    //扫描结果解析
    private void scanResult(String qrContent,Context context, Intent intent){
        //解析二维码
        String[] qrCodeValue = qrContent.split("_");
        int qrIndex = qrContent.indexOf("_");
        if(qrIndex==-1){
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
                    updateStorageForCheck();
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

        //创建listview
        sectionsPagerAdapter = new SectionsPagerAdapter(ListActivity.this, getSupportFragmentManager());
        sectionsPagerAdapter.setItem(intCount,intIndex);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    //扫描更新盘点数据
    private void updateStorageForCheck(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "StorageCheckRequestUpdate";

                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcah_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcahsite\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcahent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
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
                        "&lt;/Document&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),null);

                List<Map<String,Object>> strResponseList = t100ServiceHelper.getT100StatusData(strResponse);
                for(Map<String,Object> m: strResponseList){
                    statusCode = m.get("statusCode").toString();
                    statusDescription = m.get("statusDescription").toString();
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
                int intType = Integer.parseInt(statusCode);
                MyToast.myShow(ListActivity.this,statusDescription,intType);
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(ListActivity.this,"更新失败",0);
            }

            @Override
            public void onComplete() {

            }
        });
    }

}