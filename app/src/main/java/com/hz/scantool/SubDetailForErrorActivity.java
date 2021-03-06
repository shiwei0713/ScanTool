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
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.PrintLabelListAdapter;
import com.hz.scantool.adapter.ProductErrorListAdapter;
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

public class SubDetailForErrorActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle,statusCode,statusDescription;
    private TextView subDetailErrorProductCode,subDetailErrorProductName,subDetailErrorProductModels,subDetailErrorProcessId,subDetailErrorProcess,subDetailErrorDevice,subDetailErrorDate;
    private TextView subDetailErrorDocno,subDetailErrorVersion,subDetailErrorSeq,subDetailErrorSeq1,subDetailErrorEmp;
    private Button btnSave,btnCancel,btnDetailError;
    private EditText inputErrorWhere;
    private ListView errorList;

    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private ProductErrorListAdapter productErrorListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_for_error);

        //?????????
        initBundle();
        intiView();

    }


    /**
    *??????: ??????????????????
    *?????????2022/6/12
    **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
    }

    /**
    *??????: ???????????????
    *?????????2022/6/12
    **/
    private void intiView(){
        //???????????????
        Toolbar toolbar=findViewById(R.id.subForErrorToolBar);
        setSupportActionBar(toolbar);

        //??????????????????????????????????????????
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //?????????????????????
        subDetailErrorProductCode = findViewById(R.id.subDetailErrorProductCode);
        subDetailErrorProductName = findViewById(R.id.subDetailErrorProductName);
        subDetailErrorProductModels = findViewById(R.id.subDetailErrorProductModels);
        subDetailErrorProcessId = findViewById(R.id.subDetailErrorProcessId);
        subDetailErrorProcess = findViewById(R.id.subDetailErrorProcess);
        subDetailErrorDevice = findViewById(R.id.subDetailErrorDevice);
        subDetailErrorDate = findViewById(R.id.subDetailErrorDate);
        subDetailErrorDocno = findViewById(R.id.subDetailErrorDocno);
        subDetailErrorVersion = findViewById(R.id.subDetailErrorVersion);
        subDetailErrorSeq = findViewById(R.id.subDetailErrorSeq);
        subDetailErrorSeq1 = findViewById(R.id.subDetailErrorSeq1);
        subDetailErrorEmp = findViewById(R.id.subDetailErrorEmp);
        inputErrorWhere = findViewById(R.id.inputErrorWhere);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnDetailError = findViewById(R.id.btnDetailError);
        errorList = findViewById(R.id.errorList);

        //??????????????????
        btnSave.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
        btnDetailError.setOnClickListener(new btnClickListener());
        errorList.setOnItemClickListener(new listItemClickListener());
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailForErrorActivity.this);
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
    private void scanResult(String qrContent,Context context, Intent intent){
        //???????????????
        String[] qrCodeValue = qrContent.split("_");
        int qrIndex = qrContent.indexOf("_");
        if(qrIndex==-1){
            MyToast.myShow(context,"????????????:"+qrContent,0,1);
        }else{
            getErrorData(qrCodeValue[0],qrCodeValue[1],qrCodeValue[2],qrCodeValue[5],false);
        }
    }

    /**
     *??????: ?????????????????????
     *?????????2022/6/15
     **/
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            TextView txtErrorProductCode = view.findViewById(R.id.txtErrorProductCode);
            TextView txtErrorProductName = view.findViewById(R.id.txtErrorProductName);
            TextView txtErrorProductModels = view.findViewById(R.id.txtErrorProductModels);
            TextView txtProductErrorProcessId = view.findViewById(R.id.txtProductErrorProcessId);
            TextView txtProductErrorProcess = view.findViewById(R.id.txtProductErrorProcess);
            TextView txtProductErrorDocno = view.findViewById(R.id.txtProductErrorDocno);
            TextView txtProductErrorEmployee = view.findViewById(R.id.txtProductErrorEmployee);
            TextView txtProductErrorPlanDate = view.findViewById(R.id.txtProductErrorPlanDate);
            TextView txtProductErrorDevice = view.findViewById(R.id.txtProductErrorDevice);
            TextView txtProductErrorVersion = view.findViewById(R.id.txtProductErrorVersion);
            TextView txtProductErrorSeq = view.findViewById(R.id.txtProductErrorSeq);

            subDetailErrorProductCode.setText(txtErrorProductCode.getText().toString());
            subDetailErrorProductName.setText(txtErrorProductName.getText().toString());
            subDetailErrorProductModels.setText(txtErrorProductModels.getText().toString());
            subDetailErrorProcessId.setText(txtProductErrorProcessId.getText().toString());
            subDetailErrorProcess.setText(txtProductErrorProcess.getText().toString());
            subDetailErrorDevice.setText(txtProductErrorDevice.getText().toString());
            subDetailErrorDate.setText(txtProductErrorPlanDate.getText().toString());
            subDetailErrorDocno.setText(txtProductErrorDocno.getText().toString());
            subDetailErrorVersion.setText(txtProductErrorVersion.getText().toString());
            subDetailErrorSeq.setText(txtProductErrorSeq.getText().toString());
            subDetailErrorEmp.setText(txtProductErrorEmployee.getText().toString());
        }
    }

    //????????????
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnDetailError:
                    String sEmployee = inputErrorWhere.getText().toString();
                    getErrorData("","",sEmployee.trim(),"",true);
                    break;
                case R.id.btnSave:
                    saveErrorToT100("error","");
                    break;
                case R.id.btnCancel:
                    finish();
                    break;
            }
        }
    }

    /**
    *??????: ??????????????????
    *?????????2022/6/16
    **/
    private void getErrorData(String docno, String sVersion, String empcode, String seq,boolean isQuery){
        //???????????????
        loadingDialog = new LoadingDialog(this,"???????????????",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "ProductListGet";
                String strType = "0";
                String strwhere = "";
                if(isQuery){
                    strwhere = " sffyuc001='"+ empcode.trim()+"'";
                }else{
                    strwhere = " sffyucdocno='"+docno.trim()+"' AND sffyuc015='"+sVersion+"' AND sffyuc001='"+ empcode.trim()+"' AND sffyucseq="+seq;
                }

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
                mapResponseList = t100ServiceHelper.getT100JsonProductErrorData(strResponse,"workorder");

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
                    MyToast.myShow(SubDetailForErrorActivity.this,"???????????????",2,0);
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForErrorActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(mapResponseList.size()>0) {
                    //??????????????????
                    subDetailErrorProductCode.setText(mapResponseList.get(0).get("ProductCode").toString());
                    subDetailErrorProductName.setText(mapResponseList.get(0).get("ProductName").toString());
                    subDetailErrorProductModels.setText(mapResponseList.get(0).get("ProductModels").toString());
                    subDetailErrorProcessId.setText(mapResponseList.get(0).get("ProcessId").toString());
                    subDetailErrorProcess.setText(mapResponseList.get(0).get("Process").toString());
                    subDetailErrorDevice.setText(mapResponseList.get(0).get("Device").toString());
                    subDetailErrorDate.setText(mapResponseList.get(0).get("PlanDate").toString());
                    subDetailErrorDocno.setText(mapResponseList.get(0).get("Docno").toString());
                    subDetailErrorVersion.setText(mapResponseList.get(0).get("Version").toString());
                    subDetailErrorSeq.setText(mapResponseList.get(0).get("Seq").toString());
                    subDetailErrorEmp.setText(mapResponseList.get(0).get("Employee").toString());

                    //????????????
                    productErrorListAdapter = new ProductErrorListAdapter(mapResponseList,getApplicationContext());
                    errorList.setAdapter(productErrorListAdapter);
                }else{
                    MyToast.myShow(SubDetailForErrorActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
            }
        });
    }

    /**
     *??????: ????????????????????????
     *?????????2022/6/10
     **/
    private void saveErrorToT100(String strAction,String strActionId){
        //???????????????
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForErrorActivity.this,"???????????????",R.drawable.dialog_loading);
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


                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"sffb_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffbseq\" value=\""+ subDetailErrorProcessId.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffb005\" value=\""+ subDetailErrorDocno.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //?????????????????????
                        "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //?????????????????????
                        "&lt;Field name=\"sffb029\" value=\""+ subDetailErrorProductCode.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"processid\" value=\""+ subDetailErrorProcessId.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"process\" value=\""+ subDetailErrorProcess.getText().toString() +"\"/&gt;\n"+  //??????
                        "&lt;Field name=\"planseq\" value=\""+ subDetailErrorSeq.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"planno\" value=\""+ subDetailErrorDocno.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"version\" value=\""+ subDetailErrorVersion.getText().toString() +"\"/&gt;\n"+  //??????
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
                    MyToast.myShow(SubDetailForErrorActivity.this,"??????????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForErrorActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SubDetailForErrorActivity.this, statusDescription, 1, 1);
                }else{
                    MyToast.myShow(SubDetailForErrorActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}