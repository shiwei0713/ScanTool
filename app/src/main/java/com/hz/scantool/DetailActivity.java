

package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.helper.SharedHelper;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.helper.WebServiceHelper;
import com.hz.scantool.models.Company;
import com.hz.scantool.models.UserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
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

public class DetailActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private Integer intIndex;
    private String qrCode;
    private String qrContent;
    private String docno;
    private String doctype;
    private String qrSid;
    private String qrType;
    private String statusCode;
    private String statusDescription;
    private String strResult;
    private String strFlag;
    private String codeRule;
    private String strSubmitTitle;
    private String strCancelTitle;
    private String strInspeciton;

    private String strProductCode="";
    private String strProductName="";
    private String strQuantityNg="";
    private String strQuantityNo="";
    private String strProductModels="";
    private String strProcess="";
    private String strDevice="";
    private String strPlanDate="";
    private String strQuantity="";
    private String strDocno="";
    private String strQrCodeRule="";
    private String strStatus="";

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    private Intent intent;
    private Bundle bundle;

    TextView detailProductModelsTitle;
    TextView detailQuantityNgTitle;
    TextView detailQuantityNoTitle;
    LinearLayout detailLinearDevice;
    LinearLayout detailLinearProcess;
    TextView detailQuantityTitle;
    ImageView imageViewInput;

    TextView detailProductName;
    EditText detailQuantityNg;
    EditText detailQuantityNo;
    TextView detailProductCode;
    TextView detailProductModels;
    TextView detailProcess;
    TextView detailDevice;
    TextView detailStartPlanDate;
    TextView detailEndPlanDate;
    TextView detailQuantity;
    TextView detailDocno;
    ImageView imageViewResult;
    Button btnSubmit;
    Button btnCancel;
    Button btnScanSubmit;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //获取传入参数
        initBundle();
        if(intIndex==53){
            initErrorBundle();
        }else{
            initNormalBundle();
        }

        //设置标题
        String strTitle = "";
        strSubmitTitle= getResources().getString(R.string.detail_submit);
        strCancelTitle = getResources().getString(R.string.detail_cancel);
        switch (intIndex){
            case 0:
                strTitle = getResources().getString(R.string.master_detail1);
                qrType = "asft335";
                break;
            case 11:
                strTitle = getResources().getString(R.string.master_detail2);
                qrType = "aqct300";
                break;
            case 13:
                strTitle = getResources().getString(R.string.master_detail2);
                strSubmitTitle = getResources().getString(R.string.detail_qc_submit);
                strCancelTitle = getResources().getString(R.string.detail_qc_cancel);
                qrType = "aqct300";
                break;
            case 14:
                strTitle = getResources().getString(R.string.master_detail2);
                strSubmitTitle = getResources().getString(R.string.detail_qc_submit);
                qrType = "aqct300";
                break;
            case 2:
                strTitle = getResources().getString(R.string.master_detail3);
                break;
            case 3:
                strTitle = getResources().getString(R.string.master_detail3);
                break;
            case 4:
                strTitle = getResources().getString(R.string.master_detail4);
                break;
            case 5:
                strTitle = getResources().getString(R.string.master_detail5);
                break;
            case 53:
                strTitle = getResources().getString(R.string.master_detail5);
                break;
            case 6:
                strTitle = getResources().getString(R.string.master_detail6);
                break;
        }

        //显示信息
        initView();
        if(intIndex==13 || intIndex==11){
            getDetailItemData(qrCode);
        }else if(intIndex==53){
            showDetail();
        }else{
            getDetailItemData(qrContent);
        }

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.detailToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
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

    private void initBundle(){
        intent=getIntent();
        bundle=intent.getExtras();
        intIndex=bundle.getInt("index");
    }

    //传入正常数据
    private void initNormalBundle(){
        qrCode=bundle.getString("qrCode");
        docno=bundle.getString("docno");

        //单据类别
        if(!docno.isEmpty()){
            doctype = docno.substring(1,3);
        }else{
            doctype = "";
        }
    }

    //传入异常出货数据
    private void initErrorBundle(){
        strProductCode = bundle.getString("ProductCode");
        strProductName = bundle.getString("ProductName");
        strQuantityNg = bundle.getString("QuantityNg");
        strQuantityNo = bundle.getString("QuantityNo");
        strProductModels = bundle.getString("ProductModels");
        strProcess = bundle.getString("Process");
        strDevice = bundle.getString("Device");
        strPlanDate = bundle.getString("PlanDate");
        strQuantity = bundle.getString("Quantity");
        strDocno = bundle.getString("Docno");
        strQrCodeRule = bundle.getString("QrCodeRule");
        strStatus = bundle.getString("Status");
        qrCode=bundle.getString("qrCode");
    }

    private void initView(){
        //初始化控件
        imageViewInput = findViewById(R.id.imageViewInput);
        detailQuantityNgTitle = findViewById(R.id.detailQuantityNgTitle);
        detailQuantityNoTitle = findViewById(R.id.detailQuantityNoTitle);
        detailProductModelsTitle = findViewById(R.id.detailProductModelsTitle);
        detailLinearProcess = findViewById(R.id.detailLinearProcess);
        detailLinearDevice = findViewById(R.id.detailLinearDevice);
        detailQuantityTitle = findViewById(R.id.detailQuantityTitle);

        detailProductName = findViewById(R.id.detailProductName);
        detailQuantityNg = findViewById(R.id.detailQuantityNg);
        detailQuantityNo = findViewById(R.id.detailQuantityNo);
        detailProductCode = findViewById(R.id.detailProductCode);
        detailProductModels = findViewById(R.id.detailProductModels);
        detailProcess = findViewById(R.id.detailProcess);
        detailDevice = findViewById(R.id.detailDevice);
        detailStartPlanDate = findViewById(R.id.detailStartPlanDate);
        detailEndPlanDate = findViewById(R.id.detailEndPlanDate);
        detailQuantity = findViewById(R.id.detailQuantity);
        detailDocno = findViewById(R.id.detailDocno);
        imageViewResult = findViewById(R.id.imageViewResult);

        //按钮事件绑定
        btnSubmit=findViewById(R.id.btnSubmit);
        btnCancel=findViewById(R.id.btnCancel);
        btnScanSubmit=findViewById(R.id.btnScanSubmit);
        btnSubmit.setText(strSubmitTitle);
        btnCancel.setText(strCancelTitle);
        btnScanSubmit.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        btnSubmit.setOnClickListener(new detailClickListener());
        btnScanSubmit.setOnClickListener(new detailClickListener());
        btnCancel.setOnClickListener(new detailClickListener());

        //初始化传入值
        if(intIndex!=53){
            int iIndex = qrCode.indexOf("_");
            String sProductCode="0";
            String sProductName="0";
            if(iIndex==-1){
                qrSid = qrCode.trim();
            }else{
                String[] strQrCode = qrCode.split("_");
                qrSid = strQrCode[0].trim();
                sProductCode = strQrCode[1].trim();
                sProductName = strQrCode[2].trim();
            }

            //隐藏控件
            if(doctype.equals("XM")){
                try{
                    qrContent = docno+"_"+sProductCode+"_"+0+"_"+qrSid;
                }catch (Exception e){
                    e.printStackTrace();
                }

                imageViewInput.setVisibility(View.GONE);
                detailQuantityNgTitle.setVisibility(View.GONE);
                detailQuantityNoTitle.setVisibility(View.GONE);
                detailLinearProcess.setVisibility(View.GONE);
                detailLinearDevice.setVisibility(View.GONE);
                detailQuantityNg.setVisibility(View.GONE);
                detailQuantityNo.setVisibility(View.GONE);
                detailProcess.setVisibility(View.GONE);
                detailDevice.setVisibility(View.GONE);
            }else{
                try{
                    if(qrSid.isEmpty() || qrSid.length() == 0){
                        qrContent = "";
                    }else{
                        qrContent = qrType+"_"+sProductCode+"_"+sProductName+"_"+qrSid;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                imageViewInput.setVisibility(View.GONE);
                detailQuantityNgTitle.setVisibility(View.GONE);
                detailQuantityNoTitle.setVisibility(View.GONE);
                detailLinearProcess.setVisibility(View.GONE);
                detailLinearDevice.setVisibility(View.GONE);
                detailQuantityNg.setVisibility(View.GONE);
                detailQuantityNo.setVisibility(View.GONE);
                detailProcess.setVisibility(View.GONE);
                detailDevice.setVisibility(View.GONE);
            }
        }else {
            imageViewInput.setVisibility(View.GONE);
            detailQuantityNgTitle.setVisibility(View.GONE);
            detailQuantityNoTitle.setVisibility(View.GONE);
            detailLinearProcess.setVisibility(View.GONE);
            detailLinearDevice.setVisibility(View.GONE);
            detailQuantityNg.setVisibility(View.GONE);
            detailQuantityNo.setVisibility(View.GONE);
            detailProcess.setVisibility(View.GONE);
            detailDevice.setVisibility(View.GONE);
        }

        //显示结果
        imageViewResult = findViewById(R.id.imageViewResult);
    }

    private void showDetail(){
        if(!strProductCode.isEmpty()){
            detailProductName.setText(strProductName);
            detailQuantityNg.setText(strQuantityNg);
            detailQuantityNo.setText(strQuantityNo);
            detailProductCode.setText(strProductCode);
            String strProductType = strProductCode.substring(0,3);
            if(strProductType.equals("111")){
                detailProductModelsTitle.setText(getResources().getString(R.string.item_title_models));
                detailQuantityTitle.setText(getResources().getString(R.string.detail_content_title9));
            }

            detailProductModels.setText(strProductModels);
            detailProcess.setText(strProcess);
            detailDevice.setText(strDevice);
            detailStartPlanDate.setText(strPlanDate);
            detailEndPlanDate.setText(strPlanDate);
            detailQuantity.setText(strQuantity);
            detailDocno.setText(strDocno);

            codeRule = strQrCodeRule;
            if(codeRule.isEmpty()){
                btnSubmit.setVisibility(View.VISIBLE);
                btnScanSubmit.setVisibility(View.GONE);
            }else{
                btnSubmit.setVisibility(View.GONE);
                btnScanSubmit.setVisibility(View.VISIBLE);
            }

            strResult = strStatus;
            if(strResult.equals("Y")){
                imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ok));
                detailQuantityNg.setFocusable(false);
                detailQuantityNo.setFocusable(false);
                strFlag = "Y";
            }else{
                imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_deal));
                strFlag = "N";
            }
        }else{
            finish();
        }
    }

    private class detailClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnSubmit:
                    strInspeciton = "OK";
                    if(strFlag.equals("N")){
                        if(checkQty()){
                            if(intIndex == 53){
                                getScanQrData(detailDocno.getText().toString().trim());
                            }else{
                                updateDetailItemData();
                            }
                        }else{
                            MyToast.myShow(DetailActivity.this,"不良数量不可大于申请数据",2,0);
                        }
                    }else{
                        MyToast.myShow(DetailActivity.this,"单据已处理,不可重复提交",2,0);
                    }

                    break;
                case R.id.btnScanSubmit:
                    strInspeciton = "OK";
                    //调用zxing扫码界面
                    IntentIntegrator intentIntegrator = new IntentIntegrator(DetailActivity.this);
                    intentIntegrator.setTimeout(5000);
                    intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                    //开始扫描
                    intentIntegrator.initiateScan();
                    break;
                case R.id.btnCancel:
                    strInspeciton = "NG";
//                    if(intIndex==13){
//                        updateDetailItemData();
//                    }
                    finish();
                    break;
            }
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

    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SCANACTION)){
                String qrContent = intent.getStringExtra("scannerdata");

                if(qrContent!=null && qrContent.length()!=0){
                    scanResult(qrContent);
                }else{
                    MyToast.myShow(DetailActivity.this,"扫描失败,请重新扫描",0,0);
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(scanReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE){
            IntentResult intentResult = IntentIntegrator.parseActivityResult(resultCode,data);
            String qrContent = intentResult.getContents();
            if(qrContent!=null && qrContent.length()!=0){
                scanResult(qrContent);
            }else{
                MyToast.myShow(DetailActivity.this,"扫描失败,请重新扫描",0,0);
            }
        }
    }

    private void scanResult(String qrScanContent){
        String erpDocno = detailDocno.getText().toString().trim();
        String erpCode = detailProductName.getText().toString().trim();
        String erpQty = detailQuantity.getText().toString().trim();
        if(codeRule.isEmpty() || codeRule.length() == 0){
            MyToast.myShow(DetailActivity.this,"无扫描功能",2,0);
        }else{
            if(deCodeQrCode(codeRule,qrScanContent,erpCode,erpQty)){
                if(checkQty()){
                    if(intIndex == 53){
                        getScanQrData(erpDocno);
                    }else{
                        updateDetailItemData();
                    }
                    imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ok));
                    strFlag = "Y";
                }else{
                    MyToast.myShow(DetailActivity.this,"不良数量不可大于申请数据",2,0);
                }
            }else{
                imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ng));
                strFlag = "N";
            }
        }
    }

    //检查输入数量
    private boolean checkQty(){
        String strDetailQuantity = detailQuantity.getText().toString();
        String strDetailQuantityNg = detailQuantityNg.getText().toString();
        String strDetailQuantityNo = detailQuantityNo.getText().toString();

        if(strDetailQuantity.length()==0 || strDetailQuantity.equals(" ")){
            strDetailQuantity = "0";
        }

        if(strDetailQuantityNg.length()==0 || strDetailQuantityNg.equals(" ")){
            strDetailQuantityNg = "0";
        }

        if(strDetailQuantityNo.length()==0 || strDetailQuantityNo.equals(" ")){
            strDetailQuantityNo = "0";
        }

        float qtySum = Float.parseFloat(strDetailQuantity);
        float qtyNg1 = Float.parseFloat(strDetailQuantityNg);
        float qtyNg2 = Float.parseFloat(strDetailQuantityNo);
        float qtyNgSum = qtyNg1 + qtyNg2;
        if(qtyNgSum > qtySum){
            return false;
        }else{
            return true;
        }
    }

    //解析客户产品条码和内部条码
    private boolean deCodeQrCode(String codeRule,String codeContent,String erpCdoe,String erpQty) {
        boolean isMatch = false;
        String poNo="";
        String productCode="";
        String productCodeNew = "";
        String erpCodeNew = "";
        String saleQty="";
        String msg="";
        String code1="";

        if(qrCode.equals(codeContent) || qrCode.isEmpty() || codeContent.isEmpty()){
            MyToast.myShow(DetailActivity.this,"请扫描客户标签",2,0);
            return false;
        }

        if(codeRule.isEmpty() || codeRule.length() == 0){
            return false;
        }

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
                productCodeNew = productCode.substring(0,productCode.indexOf(' ',1))+productCode.substring(productCode.indexOf(' ',1)+1,productCode.length()-1);
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
            MyToast.myShow(DetailActivity.this,"扫描错误，请重新扫描客户标签",0,0);
            return isMatch;
        }

        if(productCode.trim().equals(erpCdoe.trim()) || productCodeNew.trim().equals(erpCdoe.trim()) || productCode.trim().equals(erpCodeNew.trim())){
            if(iSaleQty == iErpQty || code1.equals("2")){
                isMatch = true;
                MyToast.myShow(DetailActivity.this,"检核成功",1,0);
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

    private void showAlertDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
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

    private void getDetailItemData(String qrCode){
        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {

            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppListGet";
                String strIndexStr = "erpqr";
                if(intIndex==13){
                    strIndexStr = "fqc";
                }else if(intIndex==14){
                    strIndexStr = "oqc";
                }else if(intIndex==11){
                    strIndexStr = "iqc";
                }

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+qrCode+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+intIndex+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseList = t100ServiceHelper.getT100JsonData(strResponse,strIndexStr);
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

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
                            finish();
                            MyToast.myShow(DetailActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    finish();
                    MyToast.myShow(DetailActivity.this,"扫描失败，无数据显示",0,0);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                if(mapResponseList.size()> 0){
                    for(Map<String,Object> m: mapResponseList){
                        String strProductCode = m.get("ProductCode").toString();
                        if(!strProductCode.isEmpty()){
                            detailProductName.setText(m.get("ProductName").toString());
                            detailQuantityNg.setText(m.get("QuantityNg").toString());
                            detailQuantityNo.setText(m.get("QuantityNo").toString());
                            detailProductCode.setText(m.get("ProductCode").toString());
//                            String strProductType = strProductCode.substring(0,3);
//                            if(strProductType.equals("111")){
//                                detailProductModelsTitle.setText(getResources().getString(R.string.item_title_models));
//                                detailQuantityTitle.setText(getResources().getString(R.string.detail_content_title9));
//                            }

                            detailProductModels.setText(m.get("ProductModels").toString());
                            detailProcess.setText(m.get("Process").toString());
                            detailDevice.setText(m.get("Device").toString());
                            detailStartPlanDate.setText(m.get("PlanDate").toString());
                            detailEndPlanDate.setText(m.get("PlanDate").toString());
                            detailQuantity.setText(m.get("Quantity").toString());
                            detailDocno.setText(m.get("Docno").toString());

                            codeRule = m.get("QrCodeRule").toString();
                            if(codeRule.isEmpty()){
                                btnSubmit.setVisibility(View.VISIBLE);
                                btnScanSubmit.setVisibility(View.GONE);
                            }else{
                                btnSubmit.setVisibility(View.GONE);
                                btnScanSubmit.setVisibility(View.VISIBLE);
                            }

                            strResult = m.get("Status").toString();
                            if(strResult.equals("Y")){
                                imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ok));
                                detailQuantityNg.setFocusable(false);
                                detailQuantityNo.setFocusable(false);
                                strFlag = "Y";
                            }else{
                                imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_deal));
                                strFlag = "N";
                            }
                        }else{
                            finish();
                            MyToast.myShow(DetailActivity.this,statusDescription,0,0);
                        }
                    }
                }
            }
        });
    }

    //更新ERP数据
    private void updateDetailItemData(){
        btnSubmit.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
        loadingDialog = new LoadingDialog(this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "QcRequestUpdate";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"qcba_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"qcbasite\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"qcbaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"qcbadocno\" value=\""+detailDocno.getText().toString().trim()+"\"/&gt;\n"+
                        "&lt;Field name=\"qcba000\" value=\""+intIndex+"\"/&gt;\n"+
                        "&lt;Field name=\"qcba010\" value=\""+detailProductCode.getText().toString().trim()+"\"/&gt;\n"+
                        "&lt;Field name=\"qcba017\" value=\""+detailQuantity.getText().toString().trim()+"\"/&gt;\n"+
                        "&lt;Field name=\"qrsid\" value=\""+qrSid+"\"/&gt;\n"+
                        "&lt;Field name=\"qcba022\" value=\""+strInspeciton+"\"/&gt;\n"+
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"qcbdseq\" value=\"1.0\"/&gt;\n"+
                        "&lt;Field name=\"qcbd010\" value=\""+detailQuantityNo.getText().toString().trim()+"\"/&gt;\n"+
                        "&lt;Field name=\"qcbd021\" value=\""+detailQuantityNg.getText().toString().trim()+"\"/&gt;\n"+
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
                            strFlag = "N";
                            MyToast.myShow(DetailActivity.this,"更新失败,"+statusDescription,0,0);
                        }else{
                            imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ok));
                            detailQuantityNg.setFocusable(false);
                            detailQuantityNo.setFocusable(false);
                            strFlag = "Y";
                            finish();
                            MyToast.myShow(DetailActivity.this,"更新成功",1,0);
                        }
                    }
                }else{
                    MyToast.myShow(DetailActivity.this,"接口执行异常",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(DetailActivity.this,"执行异常,请联系管理员",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
//                if(mapResponseList.size()>0){
//                    showListDetail();
//                }
                loadingDialog.dismiss();
            }
        });
    }

    //获取扫描条码信息
    private void getScanQrData(String qrCode){
        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "GetQrCode";
                String qrStatus = "E";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaa011\" value=\""+qrCode+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;Field name=\"bcaa016\" value=\""+qrStatus+"\"/&gt;\n"+
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaa000\" value=\"1.0\"/&gt;\n"+
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
                            MyToast.myShow(DetailActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(DetailActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(DetailActivity.this,"网络错误",0,0);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ok));
                    detailQuantityNg.setFocusable(false);
                    detailQuantityNo.setFocusable(false);
                    strFlag = "Y";
                    finish();
                    MyToast.myShow(DetailActivity.this,"更新成功",1,0);
                }
            }
        });
    }

    private void showListDetail(){
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
            String strDocStatus = "";

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
                    strDocStatus = mResponse.get("DocStatus").toString();
                    strProductName = mResponse.get("ProductName").toString();
                    strContainer = mResponse.get("Container").toString();
                }
            }
            if(strStatus.equals("Y")){
                Intent intent = new Intent(DetailActivity.this,SubMasterListDetailActivity.class);
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
                bundle.putString("DocStatus", strDocStatus);
                bundle.putString("Container", strContainer);
                bundle.putString("Type", "2");
                intent.putExtras(bundle);
                startActivity(intent);
            }else{
                MyToast.myShow(DetailActivity.this,"过账失败",1,1);
            }
        }
    }
}