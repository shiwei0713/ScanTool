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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.dialog.DeptConfigDialog;
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

public class CheckStockDetailActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private int intIndex;
    private String strTitle;
    private String strDept;
    private String strArrayDept;
    private String statusCode;
    private String statusDescription;

    private TextView checkStockDetailDept;
    private Button checkBtnDept;

    private TextView inputDetailProductCode;
    private TextView inputDetailProductModels;
    private TextView inputDetailModel;
    private TextView inputDetailStartPlanDate;
    private EditText inputDetailQuantity;
    private TextView inputDetailDocno;
    private TextView inputDetailFeatures;
    private TextView inputDetailFeaturesName;
    private TextView inputDetailFeaturesModels;
    private Button btnDetailSubmit;

    private LoadingDialog loadingDialog;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_stock_detail);

        //???????????????
        initBundle();
        initView();

        //???????????????
        Toolbar toolbar=findViewById(R.id.checkStockDetailToolBar);
        setSupportActionBar(toolbar);

        //??????????????????????????????????????????
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //???????????????
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        intIndex = bundle.getInt("index");
    }

    private void initView(){
        checkStockDetailDept = findViewById(R.id.checkStockDetailDept);
        checkBtnDept = findViewById(R.id.checkBtnDept);

        inputDetailProductCode = findViewById(R.id.inputDetailProductCode);
        inputDetailProductModels = findViewById(R.id.inputDetailProductModels);
        inputDetailModel = findViewById(R.id.inputDetailModel);
        inputDetailStartPlanDate = findViewById(R.id.inputDetailStartPlanDate);
        inputDetailQuantity = findViewById(R.id.inputDetailQuantity);
        inputDetailDocno = findViewById(R.id.inputDetailDocno);
        inputDetailFeatures = findViewById(R.id.inputDetailFeatures);
        inputDetailFeaturesName = findViewById(R.id.inputDetailFeaturesName);
        inputDetailFeaturesModels = findViewById(R.id.inputDetailFeaturesModels);
        btnDetailSubmit = findViewById(R.id.btnDetailSubmit);

        //?????????
        checkStockDetailDept.setText(UserInfo.getDept(CheckStockDetailActivity.this));

        checkBtnDept.setOnClickListener(new submitDataClickListener());
        btnDetailSubmit.setOnClickListener(new submitDataClickListener());
    }

    private class submitDataClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnDetailSubmit:
                    if(inputDetailDocno.getText().toString().isEmpty()){
                        MyToast.myShow(CheckStockDetailActivity.this,"????????????????????????",0,0);
                    }else{
                        getScanQrData(inputDetailDocno.getText().toString(),"U");
                        inputDetailQuantity.setVisibility(View.INVISIBLE);
                        btnDetailSubmit.setVisibility(View.GONE);
                    }
                    break;
                case R.id.checkBtnDept:
                    DeptConfigDialog deptConfigDialog = new DeptConfigDialog(CheckStockDetailActivity.this,handler);
                    deptConfigDialog.show();
                    break;
            }
        }
    }

    //??????????????????
    private Handler handler =new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if(msg.what==1){
                String strDept = msg.getData().getString("dept");
                checkStockDetailDept.setText(strDept);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //???????????????????????????
        switch (item.getItemId()){
            case R.id.action_scan:
                //??????zxing????????????
                IntentIntegrator intentIntegrator = new IntentIntegrator(CheckStockDetailActivity.this);
                intentIntegrator.setTimeout(5000);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //????????????
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

        //?????????????????????
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SCANACTION);
        intentFilter.setPriority(Integer.MAX_VALUE);
        registerReceiver(scanReceiver,intentFilter);
    }

    //PDA??????????????????
    private BroadcastReceiver scanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SCANACTION)){
                String qrContent = intent.getStringExtra("scannerdata");

                if(qrContent!=null && qrContent.length()!=0){
                    scanResult(qrContent,context,intent);
                }else{
                    MyToast.myShow(context,"????????????,???????????????",0,0);
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
            Intent intent = null;

            if(qrContent!=null && qrContent.length()!=0){
                scanResult(qrContent,this,intent);
            }else{
                MyToast.myShow(this,"????????????,???????????????"+qrContent,0,0);
            }
        }
    }

    //??????????????????
    private void scanResult(String qrContent,Context context, Intent intent){
        inputDetailQuantity.setVisibility(View.VISIBLE);
        btnDetailSubmit.setVisibility(View.VISIBLE);

        //????????????
        strArrayDept = checkStockDetailDept.getText().toString();
        if(strArrayDept.isEmpty()){
            MyToast.myShow(context,"????????????????????????",0,1);
            return;
        }else{
            String[] arrayDept = strArrayDept.split("_");
            strDept = arrayDept[0].toString();
        }

        //???????????????
        String[] qrCodeValue = qrContent.split("_");
        int qrIndex = qrContent.indexOf("_");
        if(qrIndex==-1){
            MyToast.myShow(context,"????????????:"+qrContent,0,1);
        }else{
            getScanQrData(qrCodeValue[0].toString(),"A");
        }
    }

    //????????????????????????
    private void getScanQrData(String qrCode,String scmd){
        //???????????????
        loadingDialog = new LoadingDialog(this,"???????????????",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "GetQrCode";
                String qrStatus = "K";  //????????????K??????
                String strStock = strDept;  //????????????
                String sQuantity = inputDetailQuantity.getText().toString();
                float fQuantity = 0;
                if (!sQuantity.isEmpty()){
                    fQuantity = Float.valueOf(sQuantity);
                }

                //?????????????????????
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
                        "&lt;Field name=\"bcaaud001\" value=\""+strStock+"\"/&gt;\n"+
                        "&lt;Field name=\"scmd\" value=\""+scmd+"\"/&gt;\n"+
                        "&lt;Field name=\"qty\" value=\""+fQuantity+"\"/&gt;\n"+
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
                if(scmd.equals("A")){
                    mapResponseList = t100ServiceHelper.getT100JsonQrCodeData(strResponse,"qrcode");
                }
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
                            MyToast.myShow(CheckStockDetailActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(CheckStockDetailActivity.this,"??????????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(CheckStockDetailActivity.this,"????????????",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(mapResponseList.size()>0){
                    for(Map<String,Object> mData: mapResponseList){
                        String sProductCode = mData.get("ProductCode").toString();
                        String sProductName = mData.get("ProductName").toString();
                        String sProductModels = mData.get("ProductModels").toString();
                        String sProductSize = mData.get("ProductSize").toString();
                        String sDocno = mData.get("Docno").toString();
                        String sPlanDate = mData.get("PlanDate").toString();
                        String sQuantity = mData.get("Quantity").toString();
                        String sWeight = mData.get("Weight").toString();
                        String sLots = mData.get("Lots").toString();
                        String sFeatures = mData.get("Features").toString();
                        String sFeaturesName = mData.get("FeaturesName").toString();
                        String sFeaturesModels = mData.get("FeaturesModels").toString();

                        inputDetailProductCode.setText(sProductCode);
                        inputDetailProductModels.setText(sProductName);
                        inputDetailModel.setText(sProductSize+"/"+sProductModels);
                        inputDetailStartPlanDate.setText(sPlanDate);
                        inputDetailQuantity.setText(sQuantity);
                        inputDetailDocno.setText(sDocno);
                        inputDetailFeatures.setText(sFeatures);
                        inputDetailFeaturesName.setText(sFeaturesName);
                        inputDetailFeaturesModels.setText(sFeaturesModels);
                    }
                }
                loadingDialog.dismiss();
            }
        });
    }
}