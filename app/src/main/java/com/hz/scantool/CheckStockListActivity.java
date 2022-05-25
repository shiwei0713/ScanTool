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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.MainListItemAdapter;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

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

public class CheckStockListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private int intIndex;
    private String strTitle;
    private String strWhere;
    private String statusCode;
    private String statusDescription;
    private String qrSid;
    private String productCode;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private MainListItemAdapter mainListItemAdapter;

    private ProgressBar checkStockProgressBar;
    private ListView checkStockListView;
    private TextView txtTask1;
    private TextView txtTask2;
    private TextView txtTask3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_stock_list);

        //初始化参数
        initBundle();

        //初始化控件
        initView();
        initQueryCondition();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.checkStockListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //显示盘点清单
        getStorageForCheck();
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(CheckStockListActivity.this);
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
            qrSid = qrCodeValue[0].trim();
            productCode = qrCodeValue[1].trim();
            updateStorageForCheck();
        }
    }

    private void initView(){
        checkStockProgressBar = findViewById(R.id.progressBar);
        checkStockListView = findViewById(R.id.checkStockListView);

        txtTask1 = findViewById(R.id.txtTask1);
        txtTask2 = findViewById(R.id.txtTask2);
        txtTask3 = findViewById(R.id.txtTask3);

        //声明按钮ID和图片ID
        int[] btnId = new int[]{R.id.txtTask1, R.id.txtTask2, R.id.txtTask3};
        int[] imgId = new int[]{R.drawable.task1, R.drawable.task2, R.drawable.task3};
        int[] titleId = new int[]{R.string.list_task1,R.string.list_task2,R.string.list_task3};

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

        //事件绑定
        txtTask1.setOnClickListener(new queryClickListener());
        txtTask2.setOnClickListener(new queryClickListener());
        txtTask3.setOnClickListener(new queryClickListener());
    }


    //初始化参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        intIndex = bundle.getInt("index");
    }

    //初始化查询条件
    private void initQueryCondition(){
        strWhere = "";
    }

    //刷新总数
    private void refreshCount(){
        txtTask1.setText(getResources().getString(R.string.list_task1)+String.valueOf(mainListItemAdapter.getCount()));
        txtTask2.setText(getResources().getString(R.string.list_task2)+String.valueOf(mainListItemAdapter.getCount("Y")));
        txtTask3.setText(getResources().getString(R.string.list_task3)+String.valueOf(mainListItemAdapter.getCount("N")));
    }

    private class queryClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.txtTask1:
                    strWhere = " 1=1";
                    break;
                case R.id.txtTask2:
                    strWhere = " round(bcah011,3)=round(bcah016,3)";
                    break;
                case R.id.txtTask3:
                    strWhere = " round(bcah011,3)<>round(bcah016,3)";
                    break;
            }

            //显示盘点清单
            getStorageForCheck();
        }
    }

    //显示盘点清单
    private void getStorageForCheck(){
        //显示进度条
        checkStockProgressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
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
                        "&lt;Field name=\"type\" value=\""+intIndex+"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonCheckStockData(strResponse,"checklist");

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
                            MyToast.myShow(CheckStockListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = checkStockProgressBar.getProgress();
                            progress = progress + 50;
                            checkStockProgressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(CheckStockListActivity.this,"接口执行失败",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckStockListActivity.this,"网络错误",0,0);
            }

            @Override
            public void onComplete() {
                if(mapResponseList.size()>0){
                    mainListItemAdapter = new MainListItemAdapter(mapResponseList,getApplicationContext());
                    checkStockListView.setAdapter(mainListItemAdapter);

                    //刷新总数
                    refreshCount();
                }else{
                    MyToast.myShow(CheckStockListActivity.this,"无盘点数据",2,0);
                }

                //隐藏滚动条
                checkStockProgressBar.setVisibility(View.GONE);
            }
        });

    }

    //扫描更新盘点数据
    private void updateStorageForCheck(){
        //显示进度条
        checkStockProgressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String,Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "StorageCheckRequestUpdate";

                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcah_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcahsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
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
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

                e.onNext(mapResponseStatus);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<Map<String,Object>>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<Map<String,Object>> maps) {
                if(mapResponseStatus.size()> 0){
                    for(Map<String,Object> mStatus: mapResponseStatus){
                        statusCode = mStatus.get("statusCode").toString();
                        statusDescription = mStatus.get("statusDescription").toString();

                        if(statusCode.equals("0")){
                            int progress = checkStockProgressBar.getProgress();
                            progress = progress + 50;
                            checkStockProgressBar.setProgress(progress);
                            MyToast.myShow(CheckStockListActivity.this,statusDescription,1,0);
                        }else{
                            MyToast.myShow(CheckStockListActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(CheckStockListActivity.this,"接口执行失败",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckStockListActivity.this,"网络错误",0,0);
            }

            @Override
            public void onComplete() {
                //隐藏滚动条
                checkStockProgressBar.setVisibility(View.GONE);
            }
        });
    }
}