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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubAdapter;
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

public class SubDetailForModelActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private String strTitle;
    private int btnId;

    private String statusCode;
    private String statusDescription;

    private TextView subForModelProductName;
    private TextView subForModelUserName;
    private EditText subForModelQuantity;
    private TextView subDetailProductCode;
    private TextView subDetailProductModels;
    private TextView subDetailProcessId,subDetailProcess;
    private TextView subDetailDevice;
    private TextView subDetailStartPlanDate;
    private TextView subDetailVersion,subDetailSeq;
    private TextView subDetailDocno;
    private Button btnSave,btnSaveForUser;
    private LoadingDialog loadingDialog;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_for_model);

        //?????????????????????
        //???????????????
        initBundle();
        intiView();

        //???????????????
        Toolbar toolbar=findViewById(R.id.subForModelToolBar);
        setSupportActionBar(toolbar);

        //??????????????????????????????????????????
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailForModelActivity.this);
//                intentIntegrator.setTimeout(5000);
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

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(scanReceiver);
    }

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
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        btnId = bundle.getInt("btnId");
        strTitle = bundle.getString("title");
    }

    private void intiView(){
        subForModelProductName = findViewById(R.id.subForModelProductName);
        subForModelUserName = findViewById(R.id.subForModelUserName);
        subForModelQuantity= findViewById(R.id.subForModelQuantity);
        subDetailProductCode = findViewById(R.id.subDetailProductCode);
        subDetailProductModels= findViewById(R.id.subDetailProductModels);
        subDetailProcessId = findViewById(R.id.subDetailProcessId);
        subDetailProcess = findViewById(R.id.subDetailProcess);
        subDetailDevice = findViewById(R.id.subDetailDevice);
        subDetailStartPlanDate= findViewById(R.id.subDetailStartPlanDate);
        subDetailVersion= findViewById(R.id.subDetailVersion);
        subDetailDocno= findViewById(R.id.subDetailDocno);
        subDetailSeq = findViewById(R.id.subDetailSeq);
        btnSave= findViewById(R.id.btnSave);
        btnSaveForUser = findViewById(R.id.btnSaveForUser);

        btnSave.setOnClickListener(new btnClickListener());
        btnSaveForUser.setOnClickListener(new btnClickListener());
    }

    //??????????????????
    private void scanResult(String qrContent,Context context, Intent intent){
        //???????????????
        String[] qrCodeValue = qrContent.split("_");
        int qrIndex = qrContent.indexOf("_");
        if(qrIndex==-1){
            MyToast.myShow(context,"????????????:"+qrContent,0,1);
        }else{
            getModelsData(qrCodeValue[0],qrCodeValue[1],qrCodeValue[2],qrCodeValue[5]);
        }
    }

    //????????????
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSave:
                    //???????????????????????????model??????20??????????????????21???????????????
                    saveModelToT100("model","20");
                    break;
                case R.id.btnSaveForUser:
                    //???????????????????????????model??????20??????????????????21???????????????
                    saveModelToT100("model","21");
                    break;
            }
        }
    }

    //????????????
    private void getModelsData(String docno,String sVersion,String empcode,String seq){
        //???????????????
        loadingDialog = new LoadingDialog(this,"???????????????",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "ProductListGet";
                String strwhere = " sfaauc014='"+docno.trim()+"' AND sfaauc001='"+sVersion+"'";// AND sfaauc002='"+ empcode.trim()+"'
                String strType = "2";

                //?????????????????????
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
                mapResponseList = t100ServiceHelper.getT100JsonModelData(strResponse,"workorder");

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
                            MyToast.myShow(SubDetailForModelActivity.this,statusDescription,0,0);
                            loadingDialog.dismiss();
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailForModelActivity.this,"???????????????",2,0);
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForModelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(mapResponseList.size()>0) {
                    for (Map<String, Object> mData : mapResponseList) {
                        subForModelProductName.setText(mData.get("ProductName").toString());
                        subForModelUserName.setText(mData.get("Emp").toString());
                        subDetailProductCode.setText(mData.get("ProductCode").toString());
                        subDetailProductModels.setText(mData.get("ProductModels").toString());
                        subDetailProcessId.setText(mData.get("ProcessId").toString());
                        subDetailProcess.setText(mData.get("Process").toString());
                        subDetailDevice.setText(mData.get("Device").toString());
                        subDetailStartPlanDate.setText(mData.get("PlanDate").toString());
                        subDetailDocno.setText(docno);
                        subDetailVersion.setText(sVersion);
                        subDetailSeq.setText(seq);
                    }
                }
                loadingDialog.dismiss();
            }
        });
    }

    /**
    *??????: ????????????????????????
    *?????????2022/6/10
    **/
    private void saveModelToT100(String strAction,String strActionId){
        //???????????????
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForModelActivity.this,"???????????????",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //?????????T100?????????
                String webServiceName = "WorkReportRequestGen";

                //??????????????????
                long timeCurrentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
                String currentTime = simpleTimeFormat.format(timeCurrentTimeMillis);
                String currentDate = simpleDateFormat.format(new Date());

                //???????????????????????????model??????20??????????????????21???????????????
                String sUser="";
                if(strActionId.equals("20")){
                    sUser = UserInfo.getUserId(getApplicationContext());
                }else{
                    sUser = subForModelUserName.getText().toString();
                }

                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"sffb_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffbseq\" value=\""+ subDetailProcessId.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //?????????????????????
                        "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //?????????????????????
                        "&lt;Field name=\"sffb029\" value=\""+ subDetailProductCode.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffb017\" value=\""+ subForModelQuantity.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"processid\" value=\""+ subDetailProcessId.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"process\" value=\""+ subDetailProcess.getText().toString() +"\"/&gt;\n"+  //??????
                        "&lt;Field name=\"planseq\" value=\""+ subDetailSeq.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"planno\" value=\""+ subDetailDocno.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"planuser\" value=\""+ sUser +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"version\" value=\""+ subDetailVersion.getText().toString() +"\"/&gt;\n"+  //??????
                        "&lt;Field name=\"act\" value=\""+ strAction +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"actcode\" value=\""+ strActionId +"\"/&gt;\n"+  //????????????ID
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
                    }
                }else{
                    MyToast.myShow(SubDetailForModelActivity.this,"??????????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForModelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SubDetailForModelActivity.this, statusDescription, 1, 1);
                    finish();
                }else{
                    MyToast.myShow(SubDetailForModelActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}