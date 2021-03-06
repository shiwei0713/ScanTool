/**
*文件：CheckMaterialActivity,2022/6/6
*描述: 上料检核详细数据
*作者：shiwei
**/
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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.ProductMaterialAdapter;
import com.hz.scantool.adapter.SubAdapter;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.database.ProductEntity;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
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

public class CheckMaterialActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private HzDb hzDb;
    private String dataBaseName = "HzDb";
    private String strTitle;
    private String sDocno,sProcessId,sProcess,sProductDocno,sVersion,sWorkTime,sPlanSeq;
    private TextView checkMaterialProductCode,checkMaterialProductName,checkMaterialProductModels,checkMaterialQuantity,checkMaterialProcess,checkMaterialDocno;
    private TextView checkMaterialProcessId,checkMaterialAttribute,checkMaterialVersion,checkMaterialProductDocno,checkMaterialSeq,checkMaterialCurrentProcessId,checkMaterialCurrentProcess;
    private EditText inputMaterialQrcode;
    private Button btnCheckMaterialQrcode;
    private ListView materialList;
    private LoadingDialog loadingDialog;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapList;
    private String statusCode,statusDescription;
    private ProductMaterialAdapter productMaterialAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_material);

        //初始化
        initBundle();
        initView();
//        initDataBase();

        //获取备料清单
        getMaterialListData("3");
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
        }
    }

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){
        //解析二维码
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            getQrcodeData("BL","12","M",qrContent);
        }
    }

    /**
    *描述: 获取传入参数值
    *日期：2022/6/6
    **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        sDocno = bundle.getString("Docno");
        sVersion = bundle.getString("Version");
        sProcessId = bundle.getString("ProcessId");
        sProcess = bundle.getString("Process");
        sProductDocno = bundle.getString("ProductDocno");
        sPlanSeq = bundle.getString("PlanSeq");
        sWorkTime = bundle.getString("WorkTime");
    }

    /**
    *描述: 初始化控件
    *日期：2022/6/6
    **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.checkMaterialToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        strTitle = getResources().getString(R.string.list_detail_button12);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        checkMaterialProductCode = findViewById(R.id.checkMaterialProductCode);
        checkMaterialProductName = findViewById(R.id.checkMaterialProductName);
        checkMaterialProductModels = findViewById(R.id.checkMaterialProductModels);
        checkMaterialQuantity = findViewById(R.id.checkMaterialQuantity);
        checkMaterialProcess = findViewById(R.id.checkMaterialProcess);
        checkMaterialDocno = findViewById(R.id.checkMaterialDocno);
        checkMaterialProcessId = findViewById(R.id.checkMaterialProcessId);
        checkMaterialAttribute = findViewById(R.id.checkMaterialAttribute);
        checkMaterialVersion = findViewById(R.id.checkMaterialVersion);
        checkMaterialProductDocno = findViewById(R.id.checkMaterialProductDocno);
        checkMaterialSeq = findViewById(R.id.checkMaterialSeq);
        checkMaterialCurrentProcessId = findViewById(R.id.checkMaterialCurrentProcessId);
        checkMaterialCurrentProcess = findViewById(R.id.checkMaterialCurrentProcess);
        materialList = findViewById(R.id.materialList);
        inputMaterialQrcode = findViewById(R.id.inputMaterialQrcode);
        btnCheckMaterialQrcode = findViewById(R.id.btnCheckMaterialQrcode);

        //ListView增加表头
        View header = getLayoutInflater().inflate(R.layout.list_product_material_head,null);
        materialList.addHeaderView(header);

        //初始化
        checkMaterialProcessId.setText(sProcessId);
        checkMaterialProcess.setText(sProcess);
        checkMaterialDocno.setText(sDocno);
        checkMaterialVersion.setText(sVersion);
        checkMaterialProductDocno.setText(sProductDocno);
        checkMaterialSeq.setText(sPlanSeq);

        //定义事件
        btnCheckMaterialQrcode.setOnClickListener(new btnClickListener());
    }

    /**
    *描述: 按钮事件
    *日期：2022/6/12
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnCheckMaterialQrcode:
                    String sInputQrcode = inputMaterialQrcode.getText().toString();
                    if(sInputQrcode.equals("")||sInputQrcode.isEmpty()){
                        MyToast.myShow(CheckMaterialActivity.this,"条码不可为空",2,0);
                    }else{
                        getQrcodeData("BL","12","M",sInputQrcode.toUpperCase());
                    }
                    break;
            }
        }
    }

    /**
    *描述: 初始化数据库
    *日期：2022/6/10
    **/
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this,HzDb.class,dataBaseName).build();
    }

    /**
     *描述: 工具栏菜单样式
     *日期：2022/5/25
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu,menu);

        return true;
    }

    /**
     *描述: 工具栏菜单事件
     *日期：2022/5/25
     **/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(CheckMaterialActivity.this);
//                intentIntegrator.setTimeout(5000);
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

    /**
    *描述: 按钮点击事件
    *日期：2022/6/6
    **/
    private class buttonClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){

            }
        }
    }

    /**
    *描述: 获取备料清单
    *日期：2022/6/12
    **/
    private void getMaterialListData(String strType){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(CheckMaterialActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名:cwssp015
                String webServiceName = "ProductListGet";

                //查询条件
                String strwhere1=" sfbadocno IN (";
                String strwhere2="";
                String strwhere3=" )";
                String sDocno = checkMaterialProductDocno.getText().toString();
                int iIndex = sDocno.indexOf(",");
                if(iIndex!=-1){
                    String[] sDocArray = sDocno.split(",");
                    for(int i=0;i<sDocArray.length;i++){
                        if(i==0){
                            strwhere2 = "'"+ sDocArray[0]+"'";
                        }else{
                            strwhere2 = strwhere2 +",'"+ sDocArray[0]+"'";
                        }
                    }
                }else{
                    strwhere2 = "'"+sDocno+"'";
                }
                String strwhere = strwhere1.trim()+strwhere2.trim()+strwhere3.trim();//+" AND sfba004='"+checkMaterialProcess.getText().toString()+"'";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapList = t100ServiceHelper.getT100JsonProductMaterialData(strResponse,"workorder");

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
                    MyToast.myShow(CheckMaterialActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckMaterialActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    //显示成功清单
                    productMaterialAdapter = new ProductMaterialAdapter(mapList,getApplicationContext());
                    materialList.setAdapter(productMaterialAdapter);
                }else{
                    MyToast.myShow(CheckMaterialActivity.this,statusDescription,0,0);
                }

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *描述: 扫描扫码,生成上料信息
    *日期：2022/6/10
    **/
    private void getQrcodeData(String strAction,String strActionId,String qcstatus,String qrcode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(CheckMaterialActivity.this,"数据查询中",R.drawable.dialog_loading);
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

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"sffb_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"sffb004\" value=\""+ sWorkTime +"\"/&gt;\n"+  //班次
                        "&lt;Field name=\"sffb005\" value=\""+ checkMaterialProductDocno.getText().toString() +"\"/&gt;\n"+  //工单单号
//                        "&lt;Field name=\"sffb010\" value=\""+  +"\"/&gt;\n"+  //机器编号
                        "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //批量生产止日期
                        "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //批量生产止时间
                        "&lt;Field name=\"processid\" value=\""+ checkMaterialProcessId.getText().toString() +"\"/&gt;\n"+  //工艺项次
                        "&lt;Field name=\"process\" value=\""+ checkMaterialProcess.getText().toString() +"\"/&gt;\n"+  //工序
                        "&lt;Field name=\"planseq\" value=\""+ checkMaterialSeq.getText().toString() +"\"/&gt;\n"+  //报工次数
                        "&lt;Field name=\"planno\" value=\""+ checkMaterialDocno.getText().toString() +"\"/&gt;\n"+  //计划单号
                        "&lt;Field name=\"planuser\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //生产人员
                        "&lt;Field name=\"version\" value=\""+ checkMaterialVersion.getText().toString() +"\"/&gt;\n"+  //版本
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
                mapResponseList = t100ServiceHelper.getT100ResponseDocno4(strResponse,"docno");

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
                    MyToast.myShow(CheckMaterialActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckMaterialActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()> 0) {
                        String sProductCode = "";
                        String sProductName = "";
                        String sProductModels = "";
                        String sProcessId = "";
                        String sProcess = "";
                        String sQuantity = "";
                        String sAttribute = "";

                        for (Map<String, Object> mResponse : mapResponseList) {
                            sProductCode = mResponse.get("ProductCode").toString();
                            sProductName = mResponse.get("ProductName").toString();
                            sProductModels = mResponse.get("ProductModels").toString();
                            sProcessId = mResponse.get("ProcessId").toString();
                            sProcess = mResponse.get("Process").toString();
                            sQuantity = mResponse.get("Quantity").toString();
                            sAttribute = mResponse.get("Attribute").toString();
                        }

                        checkMaterialProductCode.setText(sProductCode);
                        checkMaterialProductName.setText(sProductName);
                        checkMaterialProductModels.setText(sProductModels);
                        checkMaterialQuantity.setText(sQuantity);
                        checkMaterialCurrentProcessId.setText(sProcessId);
                        checkMaterialCurrentProcess.setText(sProcess);
                        checkMaterialAttribute.setText(sAttribute);

                        //刷新备料清单
                        checkMaterialList(sProductCode,sProcess);
                    }


                    MyToast.myShow(CheckMaterialActivity.this, statusDescription, 1, 1);
                }else{
                    MyToast.myShow(CheckMaterialActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *描述: 核对清单零件结果
    *日期：2022/6/12
    **/
    private void checkMaterialList(String sProductCode,String sProcess){
        for(int i=0;i<mapList.size();i++){
            String sMapProductCode = (String)mapList.get(i).get("ProductCode");
            String sMapProcess = (String)mapList.get(i).get("Process");
            if(sMapProductCode.equals(sProductCode) && sMapProcess.equals(sProcess)){
                mapList.get(i).put("Status","Y");
            }
        }

        //显示成功清单
        productMaterialAdapter = new ProductMaterialAdapter(mapList,getApplicationContext());
        materialList.setAdapter(productMaterialAdapter);
    }
}