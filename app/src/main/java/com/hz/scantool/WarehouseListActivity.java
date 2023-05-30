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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyAlertDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubListAdapter;
import com.hz.scantool.adapter.WarehouseListAdapter;
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

public class WarehouseListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private static final int CHECKMATERIAL = 1003;

    private String strTitle,strType,strFlag,strArg,strWhere;
    private String statusCode;
    private String statusDescription;

    private EditText inputDept;
    private TextView warehouseListTitle;
    private CheckBox checkBox1,checkBox2;
    private Button btnQuery,btnClear,btnFlag1,btnFlag2;
    private ProgressBar progressBar;
    private LoadingDialog loadingDialog;
    private ListView warehouseListView;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private WarehouseListAdapter warehouseListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse_list);

        //初始化
        initBundle();
        initView();

        //初始化数据
        getWarehouseListData();
    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        strType = "8"; //默认领料
        strFlag = "DEPT"; //默认厂内
        strArg = "5";
    }

    //初始化控件
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.warehouseListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        warehouseListTitle = findViewById(R.id.warehouseListTitle);
        inputDept = findViewById(R.id.inputDept);
        checkBox1 = findViewById(R.id.checkBox1);
        checkBox2 = findViewById(R.id.checkBox2);
        btnQuery = findViewById(R.id.btnQuery);
        btnClear = findViewById(R.id.btnClear);
        btnFlag1 = findViewById(R.id.btnFlag1);
        btnFlag2 = findViewById(R.id.btnFlag2);
        progressBar = findViewById(R.id.progressBar);
        warehouseListView = findViewById(R.id.warehouseListView);

        //初始化状态
        btnFlag1.setSelected(true);
        btnFlag2.setSelected(false);

        //绑定事件
        checkBox1.setOnCheckedChangeListener(new checkClickListener());
        checkBox2.setOnCheckedChangeListener(new checkClickListener());
        btnQuery.setOnClickListener(new btnClickListener());
        btnClear.setOnClickListener(new btnClickListener());
        btnFlag1.setOnClickListener(new btnClickListener());
        btnFlag2.setOnClickListener(new btnClickListener());
        warehouseListView.setOnItemClickListener(new listItemClickListener());

    }

    /**
    *描述: 初始化查询条件
    *日期：2022/11/14
    **/
    private void initCondition(){
        strWhere = " 1=1";

        if(strType.equals("8")){
            //领料
            if(strFlag.equals("DEPT")){
                //厂内
                strWhere = " SUBSTR(indddocno,2,4) = 'IN17'";
            }else{
                //委外
                strWhere = " SUBSTR(indddocno,2,4) = 'IN19'";
            }
        }else if(strType.equals("9")){
            //退料
            if(strFlag.equals("DEPT")){
                strWhere = " SUBSTR(indddocno,2,4) = 'IN18'";
            }else{
                //委外
                strWhere = " SUBSTR(indddocno,2,4) = 'IN20'";
            }
        }
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(WarehouseListActivity.this);
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
        }else if(requestCode==CHECKMATERIAL && resultCode == CHECKMATERIAL){
            String sResult = data.getStringExtra("result");
            if(sResult.equals("0")){
                getWarehouseListData();
            }
        }
    }

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){
        //解析二维码
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            getWarehousDetailData(qrContent.trim());
        }
    }

    /**
     *描述: 检验类别选择事件
     *日期：2022/10/27
     **/
    private class checkClickListener implements CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            CheckBox checkBox = (CheckBox)compoundButton;
            if(b){
                if(checkBox==checkBox1){
                    strType = "8";  //领料
                    checkBox2.setChecked(false);
                }else{
                    strType = "9";  //退料
                    checkBox1.setChecked(false);
                }
            }
        }
    }

    /**
     *描述: 按钮单击事件
     *日期：2022/11/9
     **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnQuery:
                    getWarehouseListData();
                    break;
                case R.id.btnClear:
                    break;
                case R.id.btnFlag1:
                    strFlag = "DEPT";
                    btnFlag1.setSelected(true);
                    btnFlag2.setSelected(false);
                    getWarehouseListData();
                    break;
                case R.id.btnFlag2:
                    strFlag = "SUPP";
                    btnFlag1.setSelected(false);
                    btnFlag2.setSelected(true);
                    getWarehouseListData();
                    break;
            }
        }
    }

    /**
     *描述: 清单行点击事件
     *日期：2022/7/19
     **/
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(WarehouseListActivity.this,WarehouseDetailActivity.class);
            Bundle bundle = new Bundle();

            TextView txtViewDept = view.findViewById(R.id.listWarehouseDept);
            TextView txtViewDeptId = view.findViewById(R.id.listWarehouseDeptId);
            TextView txtViewStock = view.findViewById(R.id.listWarehouseStock);
            TextView txtViewStockId = view.findViewById(R.id.listWarehouseStockId);
            TextView txtViewDate = view.findViewById(R.id.listWarehouseDate);
            TextView textViewDocno = view.findViewById(R.id.listWarehouseDocno);

            bundle.putString("Docno",textViewDocno.getText().toString());
            bundle.putString("Dept",txtViewDept.getText().toString());
            bundle.putString("DeptId",txtViewDeptId.getText().toString());
            bundle.putString("Stock",txtViewStock.getText().toString());
            bundle.putString("StockId",txtViewStockId.getText().toString());
            bundle.putString("PlanDate",txtViewDate.getText().toString());
            bundle.putString("Type", strType);
            bundle.putString("Flag", strFlag);
            bundle.putString("Title", strTitle);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    /**
     *描述: 显示领退料清单
     *日期：2022/11/9
     **/
    private void getWarehouseListData(){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppWorkOrderListGet";

                //初始化查询条件
                initCondition();

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strArg +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonWarehouseData(strResponse,"workorder");

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

                        if(statusCode.equals("0")){
                            int progress = progressBar.getProgress();
                            progress = progress + 50;
                            progressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(WarehouseListActivity.this,"无备料数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(WarehouseListActivity.this,e.getLocalizedMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                warehouseListAdapter = new WarehouseListAdapter(mapResponseList,getApplicationContext(), strType);
                warehouseListView.setAdapter(warehouseListAdapter);

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     *描述: 显示发料单明细数据
     *日期：2022/11/9
     **/
    private void getWarehousDetailData(String qrCode){
        boolean isCheck = false;

        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(WarehouseListActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        if(mapResponseList!=null){
            if(mapResponseList.size()>0){
                for(int i=0;i<mapResponseList.size();i++){
                    String sDocno = (String)mapResponseList.get(i).get("Docno");
                    if(sDocno.equals(qrCode)){
                        String sDept = (String)mapResponseList.get(i).get("Dept");
                        String sDeptId = (String)mapResponseList.get(i).get("DeptId");
                        String sStock = (String)mapResponseList.get(i).get("Stock");
                        String sStockId = (String)mapResponseList.get(i).get("StockId");
                        String sPlanDate = (String)mapResponseList.get(i).get("PlanDate");
                        isCheck = true;

                        Intent intent = new Intent(WarehouseListActivity.this,WarehouseDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("Docno",sDocno);
                        bundle.putString("Dept",sDept);
                        bundle.putString("DeptId",sDeptId);
                        bundle.putString("Stock",sStock);
                        bundle.putString("StockId",sStockId);
                        bundle.putString("PlanDate",sPlanDate);
                        bundle.putString("Type", strType);
                        bundle.putString("Flag", strFlag);
                        bundle.putString("Title", strTitle);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
            }
        }else{
            MyToast.myShow(WarehouseListActivity.this,"无备料单数据",0,0);
        }

        if(!isCheck){
            MyAlertDialog.myShowAlertDialog(WarehouseListActivity.this,"错误信息","无备料单数据");
        }

        loadingDialog.dismiss();
        loadingDialog = null;
    }
}