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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MultipleDetailAdapter;
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

public class SubQualityCheckActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle;
    private int intIndex;
    private String statusCode;
    private String statusDescription;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private LoadingDialog loadingDialog;
    private ListView listView;
    private SubAdapter subAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_quality_check);

        //???????????????
        initBundle();
        initView();

        //???????????????
        Toolbar toolbar=findViewById(R.id.subQualityCheckToolBar);
        setSupportActionBar(toolbar);

        //??????????????????????????????????????????
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //?????????????????????
        getSubListData("12");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu_refresh,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //???????????????????????????
        switch (item.getItemId()){
            case R.id.action_scan:
                //??????zxing????????????
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubQualityCheckActivity.this);
                intentIntegrator.setTimeout(5000);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //????????????
                intentIntegrator.initiateScan();
                break;
            case R.id.action_refresh:
                //????????????
                getSubListData("12");
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
        String qrCode="";
        if(qrIndex==-1){
            qrCode = qrContent;
        }else{
            qrCode = qrCodeValue[0];
        }

        if(qrCode.equals("")||qrCode.isEmpty()){
            MyToast.myShow(context,"????????????:"+qrContent,0,1);
        }else{
            showCheckDetailData(qrCode);
        }
    }

    //?????????????????????
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        intIndex = bundle.getInt("index");
        strTitle = bundle.getString("title");
    }

    private void initView(){
        listView = findViewById(R.id.subQualityCheckView);

        listView.setOnItemClickListener(new listItemClickListener());
    }

    //???????????????
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            TextView txtSubStatus = view.findViewById(R.id.txtSubStatus);
            String strSubStatus = txtSubStatus.getText().toString();

            if(strSubStatus.equals("PC")){
                TextView txtProductName = view.findViewById(R.id.txtProductName);
                TextView txtPlanDate = view.findViewById(R.id.txtPlanDate);
                TextView txtProductCode = view.findViewById(R.id.txtProductCode);
                TextView txtProductModels = view.findViewById(R.id.txtProductModels);
                TextView txtProcess = view.findViewById(R.id.txtProcess);
                TextView txtProcessId = view.findViewById(R.id.txtProcessId);
                TextView txtDevice = view.findViewById(R.id.txtDevice);
                TextView txtDocno = view.findViewById(R.id.txtDocno);
                TextView txtQuantity = view.findViewById(R.id.txtQuantity);
                TextView txtEmployee = view.findViewById(R.id.txtEmployee);
                TextView txtLots = view.findViewById(R.id.txtLots);

                Intent intent = new Intent(SubQualityCheckActivity.this,SubQualityCheckDetailActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("ProductName",txtProductName.getText().toString());
                bundle.putString("PlanDate",txtPlanDate.getText().toString());
                bundle.putString("ProductCode",txtProductCode.getText().toString());
                bundle.putString("ProductModels",txtProductModels.getText().toString());
                bundle.putString("ProcessId",txtProcessId.getText().toString());
                bundle.putString("Process",txtProcess.getText().toString());
                bundle.putString("Device",txtDevice.getText().toString());
                bundle.putString("Docno",txtDocno.getText().toString());
                bundle.putString("Quantity",txtQuantity.getText().toString());
                bundle.putString("Employee",txtEmployee.getText().toString());
                bundle.putString("Lots",txtLots.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    //????????????
    private void getSubListData(String strType){
        //???????????????
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubQualityCheckActivity.this,"????????????",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "ProductListGet";
                String strwhere = " sffyuc004 ='F'";

                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"user\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonPqcData(strResponse,"workorder");

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
                            MyToast.myShow(SubQualityCheckActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(SubQualityCheckActivity.this,"???????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubQualityCheckActivity.this,"????????????",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                subAdapter = new SubAdapter(mapResponseList,getApplicationContext(),mUpdateClickListener,"QC");
                listView.setAdapter(subAdapter);

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    //????????????
    private SubAdapter.UpdateClickListener mUpdateClickListener = new SubAdapter.UpdateClickListener() {
        @Override
        public void UpdateClick(int position, View view) {
            //???????????????
            loadingDialog = new LoadingDialog(SubQualityCheckActivity.this,"???????????????",R.drawable.dialog_loading);
            loadingDialog.show();

            Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
                @Override
                public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                    //?????????T100?????????
                    String webServiceName = "WorkReportRequestGen";
                    String qcstatus = "K";
                    String action = "check";

                    String strPlanDate = subAdapter.getItemValue(position,"PlanDate");
                    String strDocno = subAdapter.getItemValue(position,"Docno");
                    String strProcessId = subAdapter.getItemValue(position,"ProcessId");
                    String strDevice = subAdapter.getItemValue(position,"Device");
                    String strProductCode = subAdapter.getItemValue(position,"ProductCode");
                    String strProcess = subAdapter.getItemValue(position,"Process");
                    String strLots = subAdapter.getItemValue(position,"Lots");
                    String strFlag = subAdapter.getItemValue(position,"Flag");
                    String strSeq = subAdapter.getItemValue(position,"OperateCount");
                    String strVersion = subAdapter.getItemValue(position,"Version");

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
                            "&lt;Field name=\"sffbdocdt\" value=\""+strPlanDate+"\"/&gt;\n"+
                            "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //????????????
                            "&lt;Field name=\"sffb005\" value=\""+ strDocno +"\"/&gt;\n"+  //????????????
                            "&lt;Field name=\"sffbseq\" value=\""+ strProcessId +"\"/&gt;\n"+  //????????????
                            "&lt;Field name=\"sffb010\" value=\""+ strDevice +"\"/&gt;\n"+  //????????????
                            "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //?????????????????????
                            "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //?????????????????????
                            "&lt;Field name=\"sffb029\" value=\""+ strProductCode +"\"/&gt;\n"+  //????????????
                            "&lt;Field name=\"lots\" value=\""+ strLots +"\"/&gt;\n"+  //??????
                            "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //????????????
                            "&lt;Field name=\"planno\" value=\""+ strFlag +"\"/&gt;\n"+  //????????????
                            "&lt;Field name=\"planseq\" value=\""+ strSeq +"\"/&gt;\n"+  //????????????
                            "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //????????????
                            "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //??????
                            "&lt;Field name=\"version\" value=\""+ strVersion +"\"/&gt;\n"+  //??????
                            "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //????????????
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
                    mapResponseList = t100ServiceHelper.getT100ResponseDocno(strResponse,"docno");

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
                        MyToast.myShow(SubQualityCheckActivity.this,"??????????????????",2,0);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    MyToast.myShow(SubQualityCheckActivity.this,"????????????",0,0);
                    loadingDialog.dismiss();
                }

                @Override
                public void onComplete() {
                    if(statusCode.equals("0")){
                        MyToast.myShow(SubQualityCheckActivity.this, statusDescription, 1, 1);
                    }else{
                        MyToast.myShow(SubQualityCheckActivity.this, statusDescription, 0, 1);
                    }
                    getSubListData("12");
                }
            });
        }
    };

    //??????????????????
    private void showCheckDetailData(String qrCode){
        //???????????????
        loadingDialog = new LoadingDialog(SubQualityCheckActivity.this,"????????????",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "ProductListGet";
                String strType = "22";

                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ qrCode +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonPqcDetailData(strResponse,"workorder");

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
                            MyToast.myShow(SubQualityCheckActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(SubQualityCheckActivity.this,"???????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubQualityCheckActivity.this,"????????????",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(mapResponseList.size()>0){
                    String strProductName="";
                    String strPlanDate="";
                    String strProductCode="";
                    String strProductModels="";
                    String strProcessId="";
                    String strProcess="";
                    String strDevice="";
                    String strDocno="";
                    String strQuantity="";
                    String strBadQuantity="";
                    String strNgQuantity="";
                    String strLots="";
                    String strVersion="";
                    String strUnit="";
                    String strSeq="";
                    String strSeq1="";
                    String strStatus="";
                    String strAttribute="";

                    for (Map<String, Object> mResponse : mapResponseList) {
                        strDocno = mResponse.get("Docno").toString();
                        strProductName = mResponse.get("ProductName").toString();
                        strPlanDate = mResponse.get("PlanDate").toString();
                        strProductCode = mResponse.get("ProductCode").toString();
                        strProductModels = mResponse.get("ProductModels").toString();
                        strProcessId = mResponse.get("ProcessId").toString();
                        strProcess = mResponse.get("Process").toString();
                        strDevice = mResponse.get("Device").toString();
                        strQuantity = mResponse.get("Quantity").toString();
                        strBadQuantity = mResponse.get("BadQuantity").toString();
                        strNgQuantity = mResponse.get("NgQuantity").toString();
                        strVersion = mResponse.get("Version").toString();
                        strLots = mResponse.get("Lots").toString();
                        strUnit = mResponse.get("Unit").toString();
                        strSeq = mResponse.get("Seq").toString();
                        strSeq1 = mResponse.get("Seq1").toString();
                        strStatus = mResponse.get("Status").toString();
                        strAttribute= mResponse.get("Attribute").toString();
                    }

                    Intent intent = new Intent(SubQualityCheckActivity.this,SubQualityCheckDetailActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("ProductName",strProductName);
                    bundle.putString("PlanDate",strPlanDate);
                    bundle.putString("ProductCode",strProductCode);
                    bundle.putString("ProductModels",strProductModels);
                    bundle.putString("ProcessId",strProcessId);
                    bundle.putString("Process",strProcess);
                    bundle.putString("Device",strDevice);
                    bundle.putString("Docno",strDocno);
                    bundle.putString("Quantity",strQuantity);
                    bundle.putString("BadQuantity",strBadQuantity);
                    bundle.putString("NgQuantity",strNgQuantity);
                    bundle.putString("Lots",strLots);
                    bundle.putString("Version",strVersion);
                    bundle.putString("Unit",strUnit);
                    bundle.putString("Seq",strSeq);
                    bundle.putString("Seq1",strSeq1);
                    bundle.putString("Status",strStatus);
                    bundle.putString("Attribute",strAttribute);
                    bundle.putString("qrCode",qrCode);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else{
                    MyToast.myShow(SubQualityCheckActivity.this, statusDescription, 0, 1);
                }

                loadingDialog.dismiss();
            }
        });
    }
}