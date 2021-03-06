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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubMasterListItemAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class SubMasterListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private String strTitle="";
    private String strType;
    private String strWhere;
    private String strJsonType;
    private int actionId;
    private String statusCode;
    private String statusDescription;
    private Bundle bundle;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    private LinearLayout toolFlag;
    private LinearLayout toolQuery;
    private LinearLayout toolCount;

    private TextView btnSubMasterListTitle;
    private Button btnSubMasterQcFlag1;
    private Button btnSubMasterQcFlag2;
    private Button btnSubMasterQcFlag3;
    private Button btnSubMasterQcFlag4;
    private Button btnSubMasterQcQuery;
    private EditText txtQueryQcName;
    private EditText txtQueryQcbDate;
    private EditText txtQueryQceDate;
    private TextView txtLabel1;
    private TextView txtLabel2;
    private TextView txtSubListTask1;
    private TextView txtSubListTask2;
    private ProgressBar subMasterQcProgressBar;
    private ListView subMasterQcView;
    private SubMasterListItemAdapter subMasterListItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_master_list);

        //?????????????????????
        //???????????????
        initBundle();
        initView();

        //???????????????
        Toolbar toolbar=findViewById(R.id.subMasterQcToolBar);
        setSupportActionBar(toolbar);

        //??????????????????????????????????????????
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //??????QC??????
        initQueryCondition(0);
        getSubQcListData();
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubMasterListActivity.this);
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

    //??????????????????
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        actionId = bundle.getInt("btnId");
        strTitle = bundle.getString("title");
    }

    //??????????????????
    private String setQueryDate(int interval){
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,interval);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date date =  calendar.getTime();
        String strDate = simpleDateFormat.format(date);

        return strDate;
    }

    private void initView(){
        toolFlag = findViewById(R.id.toolFlag);
        toolQuery = findViewById(R.id.toolQuery);
        toolCount = findViewById(R.id.toolCount);

        btnSubMasterListTitle =  findViewById(R.id.btnSubMasterListTitle);
        btnSubMasterQcFlag1 = findViewById(R.id.btnSubMasterQcFlag1);
        btnSubMasterQcFlag2 = findViewById(R.id.btnSubMasterQcFlag2);
        btnSubMasterQcFlag3 = findViewById(R.id.btnSubMasterQcFlag3);
        btnSubMasterQcFlag4 = findViewById(R.id.btnSubMasterQcFlag4);
        btnSubMasterQcQuery = findViewById(R.id.btnSubMasterQcQuery);
        txtQueryQcName = findViewById(R.id.txtQueryQcName);
        txtQueryQcbDate = findViewById(R.id.txtQueryQcbDate);
        txtQueryQceDate = findViewById(R.id.txtQueryQceDate);
        txtLabel1 = findViewById(R.id.txtLabel1);
        txtLabel2 = findViewById(R.id.txtLabel2);
        txtSubListTask1 = findViewById(R.id.txtSubListTask1);
        txtSubListTask2 = findViewById(R.id.txtSubListTask2);
        subMasterQcProgressBar = findViewById(R.id.subMasterQcProgressBar);
        subMasterQcView = findViewById(R.id.subMasterQcView);

        //????????????ID?????????ID
        int[] btnId = new int[]{R.id.txtSubListTask1, R.id.txtSubListTask2};
        int[] imgId = new int[]{R.drawable.task1, R.drawable.task2};
        int[] titleId = new int[]{R.string.sub_master_content_quantity,R.string.sub_master_content_quantitypcs};

        //????????????????????????
        TextView textAction;
        Drawable drawable;

        //??????????????????
        for(int i=0;i<btnId.length;i++){
            textAction=findViewById(btnId[i]);
            drawable=getResources().getDrawable(imgId[i]);
            drawable.setBounds(0,0,50,50);
            textAction.setCompoundDrawables(drawable,null,null,null);
            textAction.setCompoundDrawablePadding(10);
            textAction.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            textAction.setText(getResources().getString(titleId[i]));
        }

        toolCount.setVisibility(View.GONE);
        switch (actionId){
            //IQC??????
            case 11:
                strJsonType = "iqc";
                strType = "1";
                break;
            //PQC??????
            case 12:
                strJsonType = "pqc";
                strType = "1";
                break;
            //FQC??????
            case 15:
                strJsonType = "fqc";
                strType = "13";
                break;
            //OQC??????
            case 16:
                strJsonType = "oqc";
                strType = "14";
                break;
            //????????????
            case 17:
                strJsonType = "Inventoryqc";
                strType = "1";
                break;
            //????????????
            case 51:
                strType = "5";
                strJsonType = "salelist";
                break;
            //????????????
            case 52:
                strType = "5";
                strJsonType = "stocklist";
                toolFlag.setVisibility(View.GONE);
                break;
            //????????????
            case 53:
                strType = "53";
                strJsonType = "erpqr";
                toolFlag.setVisibility(View.GONE);
                toolQuery.setVisibility(View.GONE);
                toolCount.setVisibility(View.VISIBLE);
                break;
            //????????????
            case 55:
                strType = "5";
                break;
        }

        btnSubMasterQcFlag1.setOnClickListener(new queryClickListener());
        btnSubMasterQcFlag2.setOnClickListener(new queryClickListener());
        btnSubMasterQcFlag3.setOnClickListener(new queryClickListener());
        btnSubMasterQcFlag4.setOnClickListener(new queryClickListener());
        btnSubMasterQcQuery.setOnClickListener(new queryClickListener());
        txtLabel1.setOnClickListener(new queryClickListener());
        txtLabel2.setOnClickListener(new queryClickListener());
    }

    //?????????????????????
    private void initQueryCondition(int isAll){
        if(isAll == 0){
            strWhere = "1=1";
        }else{
            //????????????
            if(actionId == 53){
                strWhere = "to_char(bcaamoddt,'YYYY-MM-DD') = '"+setQueryDate(0)+"'";
            }else{
                strWhere = "qcbadocdt BETWEEN to_date('"+txtQueryQcbDate.getText().toString()+"','YYYY-MM-DD') AND to_date('"+txtQueryQceDate.getText().toString()+"','YYYY-MM-DD')";
            }

            //???????????????????????????????????????
            if(txtQueryQcbDate.getText().toString().isEmpty() || txtQueryQceDate.getText().toString().isEmpty()){
                strWhere = "1=1";
            }
        }

        switch (actionId){
            //IQC??????
            case 11:
                strWhere = strWhere+" AND qcba000='1'";
                break;
            //PQC??????
            case 12:
                strWhere = strWhere+" AND qcba000='3'";
                break;
            //FQC??????
            case 15:
                strWhere = strWhere+" AND qcba000='2'";
                break;
            //OQC??????
            case 16:
                strWhere = strWhere+" AND qcba000='4'";
                break;
            //????????????
            case 17:
                strWhere = strWhere+" AND qcba000='5'";
                break;
        }
    }

    //???????????????
    private void refreshCount(){
        int iQuantity = 0;
        int iQuantityPcs=0;
        int iQuantityTotal = 0;
        int iQuantityPcsTotal=0;

        try{
            for(Map<String,Object> mData: mapResponseList) {
                String sQuantity = mData.get("Quantity").toString();
                String sQuantityPcs = mData.get("QuantityPcs").toString();

                if(!sQuantity.isEmpty()){
                    iQuantity =Integer.parseInt(sQuantity);
                }else{
                    iQuantity = 0;
                }

                if(!sQuantityPcs.isEmpty()){
                    iQuantityPcs = Integer.parseInt(sQuantityPcs);
                }else{
                    iQuantityPcs = 0;
                }

                iQuantityTotal = iQuantityTotal + iQuantity;
                iQuantityPcsTotal = iQuantityPcsTotal + iQuantityPcs;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        txtSubListTask1.setText(getResources().getString(R.string.sub_master_content_quantity)+String.valueOf(iQuantityTotal));
        txtSubListTask2.setText(getResources().getString(R.string.sub_master_content_quantitypcs)+String.valueOf(iQuantityPcsTotal));
    }

    private class queryClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSubMasterQcFlag1:
                    btnSubMasterQcFlag1.setSelected(true);
                    btnSubMasterQcFlag2.setSelected(false);
                    btnSubMasterQcFlag3.setSelected(false);
                    btnSubMasterQcFlag4.setSelected(false);
                    strWhere = " imaa009='101'";
                    break;
                case R.id.btnSubMasterQcFlag2:
                    btnSubMasterQcFlag1.setSelected(false);
                    btnSubMasterQcFlag2.setSelected(true);
                    btnSubMasterQcFlag3.setSelected(false);
                    btnSubMasterQcFlag4.setSelected(false);
                    strWhere = " imaa009='102'";
                    break;
                case R.id.btnSubMasterQcFlag3:
                    btnSubMasterQcFlag1.setSelected(false);
                    btnSubMasterQcFlag2.setSelected(false);
                    btnSubMasterQcFlag3.setSelected(true);
                    btnSubMasterQcFlag4.setSelected(false);
                    strWhere = " imaa009='103'";
                    break;
                case R.id.btnSubMasterQcFlag4:
                    btnSubMasterQcFlag1.setSelected(false);
                    btnSubMasterQcFlag2.setSelected(false);
                    btnSubMasterQcFlag3.setSelected(false);
                    btnSubMasterQcFlag4.setSelected(true);
                    strWhere = " imaa009 IN ('104','105')";
                    break;
                case R.id.btnSubMasterQcQuery:
                    initQueryCondition(1);
                    break;
                case R.id.txtLabel1:
                    txtQueryQcbDate.setText(setQueryDate(0));
                    txtQueryQceDate.setText(setQueryDate(2));
                    initQueryCondition(1);
                    break;
                case R.id.txtLabel2:
                    txtQueryQcbDate.setText(setQueryDate(0));
                    txtQueryQceDate.setText(setQueryDate(6));
                    break;
            }

            //????????????????????????
            getSubQcListData();
        }
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

    @Override
    protected void onRestart() {
        super.onRestart();

        //????????????????????????
        initQueryCondition(53);
        getSubQcListData();
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

    //???????????????????????????
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
            boolean isOqc = false;  //??????OQC
            String doctype = "";
            String docno = qrCodeValue[0].trim();
            if(!docno.isEmpty()) {
                String[] docList = docno.split("-");
                String docSlip = docList[0].trim();
                doctype = docSlip.substring(1,3);

                if(doctype.equals("XM") && docList.length == 2){
                    isOqc = true;
                }
            }

            int index = 0;
            //16:OQC??????;11:IQC??????;15:FQC??????
            if(actionId == 11){
                index=11;
            } else if(actionId == 15){
                index=13;
            } else if(actionId == 16){
                index=14;
            }
            else if(actionId ==52){
                index=5;
            }else{
                index=actionId;
            }

            if(actionId == 16 || actionId ==52){
                if(isOqc){
                    intent = new Intent(context,DetailListActivity.class);
                    //??????????????????
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
                    bundle.putInt("index",index);

                    intent.putExtras(bundle);
                    startActivity(intent);
                }else{
                    MyToast.myShow(SubMasterListActivity.this,"OQC?????????????????????,???????????????",0,0);
                }
            }else{
                if(isOqc){
                    MyToast.myShow(SubMasterListActivity.this,"??????OQC?????????????????????,???????????????",0,0);
                }else{
                    if(actionId == 53){
                        updErrorDocData(qrCodeValue[0].toString());
                    }else{
                        intent = new Intent(context,DetailActivity.class);
                        //??????????????????
                        bundle=new Bundle();
                        bundle.putString("qrCode",qrContent);
                        bundle.putString("docno",docno);
                        bundle.putInt("index",index);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    //??????QC??????
    private void getSubQcListData(){
        //???????????????
        subMasterQcProgressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "AppListGet";

                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonQcData(strResponse,strJsonType);

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
                            MyToast.myShow(SubMasterListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = subMasterQcProgressBar.getProgress();
                            progress = progress + 50;
                            subMasterQcProgressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubMasterListActivity.this,"???????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubMasterListActivity.this,"????????????",0,0);
                subMasterQcProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                subMasterListItemAdapter = new SubMasterListItemAdapter(mapResponseList,getApplicationContext(),strType);
                subMasterQcView.setAdapter(subMasterListItemAdapter);

                subMasterQcProgressBar.setVisibility(View.GONE);
                refreshCount();
            }
        });
    }

    //??????????????????
    private void updErrorDocData(String qrcode){
        //???????????????
        subMasterQcProgressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "InventoryBillRequestConfirm";
                String strProg = "errpost";
                String strDocno = "";
                String strIndexStr = "docno";

                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"inaj_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"inajsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"inajent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"inaj001\" value=\""+strDocno+"\"/&gt;\n"+
                        "&lt;Field name=\"inaj015\" value=\""+strProg+"\"/&gt;\n"+
                        "&lt;Field name=\"inajuser\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"inaj002\" value=\"1.0\"/&gt;\n"+
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
                mapResponseList = t100ServiceHelper.getT100JsonData(strResponse,strIndexStr);

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
                            MyToast.myShow(SubMasterListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = subMasterQcProgressBar.getProgress();
                            progress = progress + 50;
                            subMasterQcProgressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubMasterListActivity.this,"?????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubMasterListActivity.this,"????????????",0,0);
                subMasterQcProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
//                showDetail(qrcode);
                subMasterQcProgressBar.setVisibility(View.GONE);
                MyToast.myShow(SubMasterListActivity.this,statusDescription,1,0);
            }
        });
    }

    private void showDetail(String qrcode){
        if(!statusCode.equals("0")){
            String strProductCode="";
            String strProductName="";
            String strQuantityNg="";
            String strQuantityNo="";
            String strProductModels="";
            String strProcess="";
            String strDevice="";
            String strPlanDate="";
            String strQuantity="";
            String strDocno="";
            String strQrCodeRule="";
            String strStatus="";

            if(mapResponseList.size()> 0){
                for(Map<String,Object> mResponse: mapResponseList){
                    strProductCode = mResponse.get("ProductCode").toString();
                    strProductName = mResponse.get("ProductName").toString();
                    strQuantityNg = mResponse.get("QuantityNg").toString();
                    strQuantityNo = mResponse.get("QuantityNo").toString();
                    strProductModels = mResponse.get("ProductModels").toString();
                    strProcess = mResponse.get("Process").toString();
                    strDevice = mResponse.get("Device").toString();
                    strPlanDate = mResponse.get("PlanDate").toString();
                    strQuantity = mResponse.get("Quantity").toString();
                    strDocno = mResponse.get("Docno").toString();
//                    strQrCodeRule = mResponse.get("QrCodeRule").toString();
                    strStatus = mResponse.get("Status").toString();
                }
            }

            Intent intent = new Intent(SubMasterListActivity.this,DetailActivity.class);
            //??????????????????
            bundle=new Bundle();
            bundle.putString("ProductCode",strProductCode);
            bundle.putString("ProductName",strProductName);
            bundle.putString("QuantityNg",strQuantityNg);
            bundle.putString("QuantityNo",strQuantityNo);
            bundle.putString("ProductModels",strProductModels);
            bundle.putString("Process",strProcess);
            bundle.putString("Device",strDevice);
            bundle.putString("PlanDate",strPlanDate);
            bundle.putString("Quantity",strQuantity);
            bundle.putString("Docno",strDocno);
            bundle.putString("QrCodeRule",strQrCodeRule);
            bundle.putString("Status",strStatus);
            bundle.putInt("index",actionId);
            bundle.putString("qrCode",qrcode);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}