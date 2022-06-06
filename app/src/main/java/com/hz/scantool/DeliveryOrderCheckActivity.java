/**
*文件：DeliveryOrderCheckActivity,2022/5/25
*描述: 1、出货扫码三点照合，扫描内部标签显示零件信息，扫描客户标签二次核对，并记录核对记录
*作者：
**/
package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.database.DeliveryOrderEntity;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.io.UnsupportedEncodingException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class DeliveryOrderCheckActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";  //PDA广播

    private String dataBaseName = "HzDb";
    private String strTitle;
    private String strProductCode,strProductName,strProductModels,strDocno,strSaler,strQrcode,strQrCodeRule;
    private int iQuantity;

    private TextView dOrderCheckProductName,cOrderCheckProductCode,cOrderCheckProductModels,cOrderCheckSaler,cOrderCheckQuantity,cOrderCheckDocno,cOrderCheckQrcode;
    private Button btnSubmit,btnScanSubmit,btnCancel;
    private ImageView dOrderCheckResultIcon;
    private LoadingDialog loadingDialog;
    private DeliveryOrderEntity deliveryOrderEntity;
    private HzDb hzDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_order_check);

        //初始化
        initBundle();
        initView();
        initDataBase();
    }

    /*
     *后台操作，创建数据库
     */
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this,HzDb.class,dataBaseName).build();
    }

    /**
     *描述: 获取传入参数
     *日期：2022/5/25
     **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        strProductCode = bundle.getString("productCode");
        strProductName = bundle.getString("productName");
        strProductModels = bundle.getString("productModels");
        strDocno = bundle.getString("docno");
        strSaler = bundle.getString("saler");
        strQrcode = bundle.getString("qrcode");
        strQrCodeRule = bundle.getString("qrCodeRule");
        iQuantity = bundle.getInt("quantity");
    }

    /**
     *描述: 初始化view控件
     *日期：2022/5/25
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.dOrderCheckToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        dOrderCheckProductName = findViewById(R.id.dOrderCheckProductName);
        cOrderCheckProductCode = findViewById(R.id.cOrderCheckProductCode);
        cOrderCheckProductModels = findViewById(R.id.cOrderCheckProductModels);
        cOrderCheckSaler = findViewById(R.id.cOrderCheckSaler);
        cOrderCheckQuantity = findViewById(R.id.cOrderCheckQuantity);
        cOrderCheckDocno = findViewById(R.id.cOrderCheckDocno);
        cOrderCheckQrcode = findViewById(R.id.cOrderCheckQrcode);
        dOrderCheckResultIcon= findViewById(R.id.dOrderCheckResultIcon);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnScanSubmit = findViewById(R.id.btnScanSubmit);
        btnCancel = findViewById(R.id.btnCancel);

        //定义事件
        btnSubmit.setOnClickListener(new btnClickListener());
        btnScanSubmit.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());

        //初始化值
        cOrderCheckProductCode.setText(strProductCode);
        dOrderCheckProductName.setText(strProductName);
        cOrderCheckProductModels.setText(strProductModels);
        cOrderCheckDocno.setText(strDocno);
        cOrderCheckSaler.setText(strSaler);
        cOrderCheckQrcode.setText(strQrcode);
        cOrderCheckQuantity.setText(String.valueOf(iQuantity));

        //按钮动作定义
        if(strQrCodeRule.equals("")||strQrCodeRule.isEmpty()){
            btnScanSubmit.setVisibility(View.GONE);
        }else{
            btnSubmit.setVisibility(View.GONE);
        }
    }

    /**
     *描述: 按钮事件实现
     *日期：2022/5/25
     **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSubmit: //保存
                    updateData("");
                    break;
                case R.id.btnScanSubmit: //扫描提交
                    //调用zxing扫码界面
                    IntentIntegrator intentIntegrator = new IntentIntegrator(DeliveryOrderCheckActivity.this);
                    intentIntegrator.setTimeout(5000);
                    intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                    //开始扫描
                    intentIntegrator.initiateScan();
                    break;
                case R.id.btnCancel: //取消
                    finish();
                    break;
            }
        }
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(DeliveryOrderCheckActivity.this);
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

    /**
     *描述: 注册PDA扫码广播
     *日期：2022/5/25
     **/
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


    }

    /**
     *描述: PDA扫描数据接收
     *日期：2022/5/25
     **/
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

    /**
     *描述: 手机调用摄像头扫描数据接收
     *日期：2022/5/25
     **/
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

    /**
     *描述: 扫描结果解析
     *日期：2022/5/25
     **/
    private void scanResult(String qrContent,Context context, Intent intent){
        //解析二维码
        String[] qrCodeValue = qrContent.split("_");
        int qrIndex = qrContent.indexOf("_");
        if(qrContent.isEmpty()||qrContent.equals("")){
            MyToast.myShow(DeliveryOrderCheckActivity.this,"条码错误:"+qrContent,0,1);
        }else{
            if(strQrCodeRule.equals("")||strQrCodeRule.isEmpty()){
                MyToast.myShow(DeliveryOrderCheckActivity.this,"无扫描功能",2,0);
            }else{
                String erpQty = cOrderCheckQuantity.getText().toString();

                if(deCodeQrCode(strQrCodeRule,qrContent,strProductName,erpQty)){
                    updateData(qrContent);
                }else{
                    dOrderCheckResultIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ng));
                }
            }

        }

    }

    /**
    *描述: 更新扫描结果
    *日期：2022/5/31
    **/
    private void updateData(String salerCode){
        //显示进度条
        loadingDialog = new LoadingDialog(this,"数据获取中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<DeliveryOrderEntity>() {
            @Override
            public void subscribe(ObservableEmitter<DeliveryOrderEntity> e) throws Exception {

                String sQrCode = cOrderCheckQrcode.getText().toString();
                hzDb.deliveryOrderDao().updateOrderSalerCode(salerCode,"C",sQrCode);
                deliveryOrderEntity = hzDb.deliveryOrderDao().queryOrderResult(sQrCode);

                e.onNext(deliveryOrderEntity);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<DeliveryOrderEntity>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(DeliveryOrderEntity deliveryOrderEntity) {

            }

            @Override
            public void onError(Throwable e) {
                Log.i("rxjavaerror",e.getMessage());
                MyToast.myShow(DeliveryOrderCheckActivity.this,e.getMessage(),2,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                String sStatus = deliveryOrderEntity.getStatus();
                if(sStatus.equals("C")){
                    dOrderCheckResultIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ok));
                    finish();
                }else{
                    dOrderCheckResultIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ng));
                }
                loadingDialog.dismiss();
            }
        });
    }

    /**
    *描述: 解析客户产品条码和内部条码
    *日期：2022/5/30
    **/
    private boolean deCodeQrCode(String codeRule,String codeContent,String erpCdoe,String erpQty) {
        boolean isMatch = false;
        String poNo="";
        String productCode="";
        String productCodeNew = "";
        String erpCodeNew = "";
        String saleQty="";
        String msg="";
        String code1="";

        try{
            //获取分隔索引
            int index1 = codeRule.indexOf(',',0);
            int index2 = codeRule.indexOf(',',index1+1);
            int index3 = codeRule.indexOf(',',index2+1);
            int indexLen = codeRule.length();

            //获取索引值
            String sIndex1 = codeRule.substring(index1 + 1,index2);
            String sIndex2 = codeRule.substring(index2 + 1,index3);
            String sIndex3 = codeRule.substring(index3 + 1,indexLen);

            //获取第一码值
            code1 = codeRule.substring(0,index1);

            //第一码为0,代表无分隔符,则分割索引按照规则中数字
            //第一码不为0，代表有分隔符，则分隔索引按照规则中分割符号所在索引位置
            //0,0-23,23-51,51-56:分隔符,PO单号,零件号,数量;|,1,7,3:分隔符,PO单号,零件号,数量
            if(code1.equals("0")){
                //解析开始和结束索引
                int codeIndexStart1 = Integer.parseInt(sIndex1.substring(0,sIndex1.indexOf('-',1)));
                int codeIndexEnd1 = Integer.parseInt(sIndex1.substring(sIndex1.indexOf('-',1)+1,sIndex1.length()));
                int codeIndexStart2 = Integer.parseInt(sIndex2.substring(0,sIndex2.indexOf('-',1)));
                int codeIndexEnd2 = Integer.parseInt(sIndex2.substring(sIndex2.indexOf('-',1)+1,sIndex2.length()));
                int codeIndexStart3 = Integer.parseInt(sIndex3.substring(0,sIndex3.indexOf('-',1)));
                int codeIndexEnd3 = Integer.parseInt(sIndex3.substring(sIndex3.indexOf('-',1)+1,sIndex3.length()));

                poNo = codeContent.substring(codeIndexStart1,codeIndexEnd1).trim();
                productCode = codeContent.substring(codeIndexStart2,codeIndexEnd2).trim();
                saleQty = codeContent.substring(codeIndexStart3,codeIndexEnd3).trim();

                //本田备件处理
                productCodeNew = productCode.substring(0,productCode.indexOf(" ",1))+productCode.substring(productCode.indexOf(' ',1)+1,productCode.length()-1);
            }else {
                String strCodeContentUft = "";
                if(code1.equals("1")){
                    strCodeContentUft = codeContent.trim();
                }else{
                    if(code1.equals("2")){
                        strCodeContentUft = "P"+codeContent.trim();
                    }else{
                        String strCodeContent = codeContent.trim().replace(code1,",");

                        try{
                            strCodeContentUft = new String(strCodeContent.getBytes("gb2312"),"gb2312");
                        }catch (UnsupportedEncodingException e){
                            e.printStackTrace();
                            return false;
                        }
                    }

                }

                int poNoIndex = Integer.parseInt(sIndex1);
                int productCodeIndex = Integer.parseInt(sIndex2);
                int saleQtyIndex = Integer.parseInt(sIndex3);
                String[] arrayCodeContent = strCodeContentUft.split(",");
                for(int i=0;i<arrayCodeContent.length;i++){
                    poNo = arrayCodeContent[poNoIndex].trim();
                    productCode = arrayCodeContent[productCodeIndex].trim();
                    saleQty = arrayCodeContent[saleQtyIndex].trim();

                    //模冲零件开头为C0230
                    productCodeNew = productCode.substring(4,productCode.length());
                }

                //乘用车DFPV零件处理
                int iDfpv = erpCdoe.indexOf("-DFPV",1);
                if(iDfpv>0){
                    erpCodeNew = erpCdoe.substring(0,iDfpv);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            MyToast.myShow(DeliveryOrderCheckActivity.this,"扫描错误，请重新扫描客户标签",0,0);
            return false;
        }

        if(saleQty.equals(" ") || saleQty.length()==0){
            saleQty = "0";
        }
        if(erpQty.equals(" ") || erpQty.length()==0){
            erpQty = "0";
        }
        float iErpQty = Float.parseFloat(erpQty);
        float iSaleQty = 0;

        try{
            iSaleQty = Float.parseFloat(saleQty);
        }catch (Exception e){
            e.printStackTrace();
            iSaleQty = 0;
            MyToast.myShow(DeliveryOrderCheckActivity.this,"扫描错误，请重新扫描客户标签",0,0);
            return isMatch;
        }

        if(productCode.trim().equals(erpCdoe.trim()) || productCodeNew.trim().equals(erpCdoe.trim()) || productCode.trim().equals(erpCodeNew.trim())){
            if(iSaleQty == iErpQty || code1.equals("2")){
                isMatch = true;
                MyToast.myShow(DeliveryOrderCheckActivity.this,"检核成功",1,0);
            }else{
                isMatch = false;
                msg = "数量不一致,客户数量:"+saleQty+",系统数量:"+iErpQty;
                showAlertDialog(msg);
            }
        }else{
            isMatch = false;
            if(iSaleQty == iErpQty || code1.equals("2")){
                msg = "零件号不一致,客户零件号:"+productCode+",系统零件号:"+erpCdoe;
            }else{
                msg = "零件号和数量不一致,客户零件号:"+productCode+",系统零件号:"+erpCdoe+",客户数量:"+saleQty+",系统数量:"+iErpQty;
            }

            showAlertDialog(msg);
        }

        return isMatch;
    }

    /**
    *描述: 错误提示
    *日期：2022/5/30
    **/
    private void showAlertDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryOrderCheckActivity.this);
        builder.setMessage(msg);
        builder.setTitle("提示");
        builder.setIcon(R.drawable.dialog_error);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }
}