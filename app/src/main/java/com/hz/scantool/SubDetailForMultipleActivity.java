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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MultipleDetailAdapter;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.ProductMaterialAdapter;
import com.hz.scantool.adapter.SubAdapter;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.database.ProductEntity;
import com.hz.scantool.dialog.ShowAlertDialog;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;
import com.tencent.bugly.proguard.B;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
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

public class SubDetailForMultipleActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private HzDb hzDb;
    private String dataBaseName = "HzDb";

    private String strTitle;
    private String strFlag;
    private String strQrcode;
    private String strProcessId,strProcess;
    private String strModStatus;
    private String strOperateCount;
    private String strPrintCount;
    private String strStartStatus;
    private String strCheckStatus;
    private String strUpStatus;
    private String strErrorStartStatus;
    private String strErrorStopStatus;
    private String strVersion;
    private String strStartTime;
    private String strCheckTime;
    private String strUpTime;
    private String strErrorTime;
    private String strProductTotal;
    private String strErrorLots;
    private String mRecordSet="";
    private int id = 0;
    private int w,h;

    private TextView txtMultipleInputCount;
    private TextView txtMultiplePrintCount;
    private TextView txtMultiplePlanNo;
    private TextView txtMultipleModle;
    private TextView txtMultipleVersion;
    private TextView txtMultipleErrorCount;
    private TextView txtMultipleQcCount;

    private TextView txtMultipleSum;
    private TextView txtMultipleStartTime;
    private TextView txtMultipleCheckTime;
    private TextView txtMultipleProductTime;
    private TextView txtMultipleErrorTime;

    private ImageView imgMultipleQrcode;
    private ListView subMultipleView;
    private ImageView imgMultipleStartStatus;
    private ImageView imgMultipleQcStatus;
    private ImageView imgMultipleProductStatus;
    private ImageView imgMultipleErrorBeginStatus;
    private ImageView imgMultipleErrorEndStatus;
    private Button btnMultipleStart;
    private Button btnMultipleEnd;
    private Button btnMultipleQc;
    private Button btnMultipleProduct;
    private Button btnMultipleError;
    private Button btnMultipleSave;
    private Button btnMultiplePrint;
    private Button btnMultipleQuery;

    private String statusCode;
    private String statusDescription;
    private String strWorkTime;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapList;
    private LoadingDialog loadingDialog;
    private MultipleDetailAdapter multipleDetailAdapter;
    private ProductEntity productEntity;
    private List<ProductEntity> productEntityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_for_multiple);

        //?????????
        initView();
        initBundle();
        initDataBase();

        //???????????????
        Toolbar toolbar=findViewById(R.id.subDetailMultipleToolBar);
        setSupportActionBar(toolbar);

        //??????????????????????????????????????????
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        createQrcode(strFlag+"_"+txtMultipleVersion.getText().toString()+"_"+ UserInfo.getUserId(getApplicationContext())+"_"+strProcessId+"_"+strProcess+"_"+txtMultipleInputCount.getText().toString());

        //??????????????????
        getMultipleDetailData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //???????????????????????????
        switch (item.getItemId()){
            case R.id.action_scan:
                //??????zxing????????????
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailForMultipleActivity.this);
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

    /*
     *??????????????????????????????
     */
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this,HzDb.class,dataBaseName).build();
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

    //?????????????????????
    private void initBundle(){
        strTitle = this.getResources().getString(R.string.master_detail1);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strFlag = bundle.getString("Flag");
        strProcessId = bundle.getString("ProcessId");
        strProcess = bundle.getString("Process");
        strModStatus = bundle.getString("ModStatus");
        strOperateCount = bundle.getString("OperateCount");
        strPrintCount = bundle.getString("PrintCount");
        strStartStatus = bundle.getString("StartStatus");
        strCheckStatus = bundle.getString("CheckStatus");
        strUpStatus = bundle.getString("UpStatus");
        strErrorStartStatus = bundle.getString("ErrorStartStatus");
        strErrorStopStatus = bundle.getString("ErrorStopStatus");
        strVersion  = bundle.getString("Version");
        strStartTime = bundle.getString("StartTime");
        strCheckTime = bundle.getString("CheckTime");
        strUpTime = bundle.getString("UpTime");
        strErrorTime = bundle.getString("ErrorTime");
        strProductTotal = bundle.getString("ProductTotal");

        strStartTime = strStartTime.replace("/","\n");
        strCheckTime = strCheckTime.replace("/","\n");
        strUpTime = strUpTime.replace("/","\n");
        strErrorTime = strErrorTime.replace("/","\n");
        strProductTotal = strProductTotal.replace("/","\n");

        if(strOperateCount.equals("")||strOperateCount.isEmpty()){
            strOperateCount = "0";
        }
        if(strPrintCount.equals("")||strPrintCount.isEmpty()){
            strPrintCount = "0";
        }

        //??????????????????
        if(strStartStatus.equals("0")||strStartStatus.isEmpty()){
            imgMultipleStartStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imgMultipleStartStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }

        //???????????????
        if(strCheckStatus.equals("0")||strCheckStatus.isEmpty()){
            imgMultipleQcStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imgMultipleQcStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }

        //??????????????????
        if(strUpStatus.equals("0")||strUpStatus.isEmpty()){
            imgMultipleProductStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imgMultipleProductStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }

        //??????????????????
        if(strErrorStartStatus.equals("0")||strErrorStartStatus.isEmpty()){
            imgMultipleErrorBeginStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imgMultipleErrorBeginStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }

        //??????????????????
        if(strErrorStopStatus.equals("0")||strErrorStopStatus.isEmpty()){
            imgMultipleErrorEndStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
        }else{
            imgMultipleErrorEndStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
        }

        int iOperateCount = Integer.valueOf(strOperateCount) + 1;
        int iPrintCount = Integer.valueOf(strPrintCount) + 1;

        txtMultiplePlanNo.setText(strFlag);
        txtMultipleModle.setText(strModStatus);
        txtMultipleInputCount.setText(String.valueOf(iOperateCount));
        txtMultiplePrintCount.setText(String.valueOf(iPrintCount));
        txtMultipleVersion.setText(strVersion);
        txtMultipleErrorCount.setText(strErrorStartStatus);
        txtMultipleQcCount.setText(strCheckStatus);

        txtMultipleSum.setText(strProductTotal);
        txtMultipleStartTime.setText(strStartTime);
        txtMultipleCheckTime.setText(strCheckTime);
        txtMultipleProductTime.setText(strUpTime);
        txtMultipleErrorTime.setText(strErrorTime);
    }

    //???????????????
    private void initView(){
        txtMultipleInputCount = findViewById(R.id.txtMultipleInputCount);
        txtMultiplePrintCount = findViewById(R.id.txtMultiplePrintCount);
        txtMultiplePlanNo = findViewById(R.id.txtMultiplePlanNo);
        txtMultipleModle = findViewById(R.id.txtMultipleModle);
        imgMultipleQrcode = findViewById(R.id.imgMultipleQrcode);
        imgMultipleStartStatus = findViewById(R.id.imgMultipleStartStatus);
        imgMultipleQcStatus = findViewById(R.id.imgMultipleQcStatus);
        imgMultipleProductStatus = findViewById(R.id.imgMultipleProductStatus);
        imgMultipleErrorBeginStatus = findViewById(R.id.imgMultipleErrorBeginStatus);
        imgMultipleErrorEndStatus = findViewById(R.id.imgMultipleErrorEndStatus);
        subMultipleView = findViewById(R.id.subMultipleView);
        txtMultipleVersion = findViewById(R.id.txtMultipleVersion);
        txtMultipleErrorCount = findViewById(R.id.txtMultipleErrorCount);
        txtMultipleQcCount = findViewById(R.id.txtMultipleQcCount);

        btnMultipleStart = findViewById(R.id.btnMultipleStart);
        btnMultipleEnd = findViewById(R.id.btnMultipleEnd);
        btnMultipleQc = findViewById(R.id.btnMultipleQc);
        btnMultipleProduct = findViewById(R.id.btnMultipleProduct);
        btnMultipleError = findViewById(R.id.btnMultipleError);
        btnMultipleSave = findViewById(R.id.btnMultipleSave);
        btnMultiplePrint = findViewById(R.id.btnMultiplePrint);
        btnMultipleQuery = findViewById(R.id.btnMultipleQuery);

        txtMultipleSum = findViewById(R.id.txtMultipleSum);
        txtMultipleStartTime = findViewById(R.id.txtMultipleStartTime);
        txtMultipleCheckTime = findViewById(R.id.txtMultipleCheckTime);
        txtMultipleProductTime = findViewById(R.id.txtMultipleProductTime);
        txtMultipleErrorTime = findViewById(R.id.txtMultipleErrorTime);

        btnMultipleStart.setOnClickListener(new commandClickListener());
        btnMultipleEnd.setOnClickListener(new commandClickListener());
        btnMultipleQc.setOnClickListener(new commandClickListener());
        btnMultipleProduct.setOnClickListener(new commandClickListener());
        btnMultipleError.setOnClickListener(new commandClickListener());
        btnMultipleSave.setOnClickListener(new commandClickListener());
        btnMultiplePrint.setOnClickListener(new commandClickListener());
        btnMultipleQuery.setOnClickListener(new commandClickListener());

        //???????????????
        setWorktime();
    }

    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnMultipleStart: //????????????
                    saveMultipleToT100("insert","10","B","");
                    break;
                case R.id.btnMultipleEnd:   //????????????
                    strErrorLots = txtMultipleErrorCount.getText().toString();
                    saveMultipleToT100("insert","14","E","");
                    break;
                case R.id.btnMultipleSave:  //????????????
                    //?????????????????????????????????????????????????????????????????????????????????????????????
                    strErrorLots = txtMultipleErrorCount.getText().toString();
//                    saveMultipleToT100("save","","V","");
                    saveData("save","","V","");
                    break;
                case R.id.btnMultiplePrint: //????????????
                    strErrorLots = txtMultipleErrorCount.getText().toString();
//                    saveMultipleToT100("print","","P","");
                    savePrintToT100("print","","P","");
                    break;
                case R.id.btnMultipleQc:    //?????????????????????:F,???????????????K
                    strErrorLots = txtMultipleQcCount.getText().toString();
                    saveMultipleToT100("insert","11","F","");
                    break;
                case R.id.btnMultipleProduct:   //????????????
                    //??????zxing????????????
//                    IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailForMultipleActivity.this);
//                    intentIntegrator.setTimeout(10000);
//                    intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
//                    //????????????
//                    intentIntegrator.initiateScan();
                    checkMaterial();
                    break;
                case R.id.btnMultipleError:     //????????????
                    strErrorLots = txtMultipleErrorCount.getText().toString();
                    saveMultipleToT100("insert","13","S","");
                    imgMultipleErrorEndStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
                    break;
                case R.id.btnMultipleQuery:    //????????????

                    break;
            }
        }
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
    private void scanResult(String qrContent, Context context, Intent intent){
        //???????????????
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"????????????:"+qrContent,0,1);
        }else{
//            saveMultipleToT100("insert","12","M",qrContent);   //????????????????????????
            //????????????
            updQrcodeData("GX","12","C",qrContent);
        }
    }

    /**
    *??????: ????????????
    *?????????2022/6/12
    **/
    private void checkMaterial(){
        //??????????????????
        int iCount = multipleDetailAdapter.getCount();
        String strProductDocno = "";

        for(int i= 0;i<iCount;i++){
            if(strProductDocno.equals("")||strProductDocno.isEmpty()){
                strProductDocno = multipleDetailAdapter.getItemValue(i,"Docno");
            }else{
                strProductDocno = strProductDocno + ","+multipleDetailAdapter.getItemValue(i,"Docno");
            }

        }

        //????????????????????????
        Intent intent = new Intent(SubDetailForMultipleActivity.this,CheckMaterialActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("Docno",strFlag);
        bundle.putString("Version",strVersion);
        bundle.putString("ProcessId",strProcessId);
        bundle.putString("Process",strProcess);
        bundle.putString("ProductDocno",strProductDocno);
        bundle.putString("PlanSeq",txtMultipleInputCount.getText().toString());
        bundle.putString("WorkTime",strWorkTime);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
    *??????: ???????????????????????????????????????PQC???????????????????????????????????????
    *?????????2022/6/10
    **/
    private void createQrcode(String qrcode){
        w=300;
        h=300;
        try{
            if(qrcode == null || "".equals(qrcode) || qrcode.length()<1){
                return;
            }

            Hashtable<EncodeHintType,String> hints = new Hashtable<EncodeHintType,String>();
            hints.put(EncodeHintType.CHARACTER_SET,"utf-8");

            BitMatrix bitMatrix = new QRCodeWriter().encode(qrcode, BarcodeFormat.QR_CODE,w,h,hints);
            int[] pixels = new int[w*h];

            for(int y=0;y<h;y++){
                for(int x=0;x<w;x++){
                    if(bitMatrix.get(x,y)){
                        pixels[y*w+x] = 0xff000000;
                    }else{
                        pixels[y*w+x] = 0xffffffff;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels,0,w,0,0,w,h);
            imgMultipleQrcode.setImageBitmap(bitmap);

        }catch (WriterException e){
            e.printStackTrace();
        }
    }

    //????????????
    private void getMultipleDetailData(){
        //???????????????
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForMultipleActivity.this,"????????????",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "ProductListGet";
                String strwhere = " sfaauc014='"+strFlag+"' AND sfaauc007='"+strProcessId+"' AND sfaauc001="+strVersion;
                String strType = "21";

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
                mapResponseList = t100ServiceHelper.getT100JsonProductDetailData(strResponse,"workorder");

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
                            MyToast.myShow(SubDetailForMultipleActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailForMultipleActivity.this,"???????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                multipleDetailAdapter = new MultipleDetailAdapter(mapResponseList,getApplicationContext(),strModStatus);
                subMultipleView.setAdapter(multipleDetailAdapter);

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    private void setWorktime(){
        long timeCurrentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = simpleDateFormat.format(timeCurrentTimeMillis);
        strWorkTime = "??????";

        try{
            Date date1 = simpleDateFormat.parse(currentTime);
            Date date2 = simpleDateFormat.parse("07:00:00");
            Date date3 = simpleDateFormat.parse("20:00:00");
            if(date1.getTime()>=date2.getTime() && date1.getTime()<=date3.getTime()){
                strWorkTime = "??????";
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    private boolean checkListItemQuantity(String strAction){
        if(strAction.equals("insert")){
            return true;
        }

        for(int i= 0;i<multipleDetailAdapter.getCount();i++){
            LinearLayout linearLayout = (LinearLayout)subMultipleView.getAdapter().getView(i,null,null);
            TextView txtMultipleDetailProductName = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProductName);
            String strQuantity = multipleDetailAdapter.getQuantity(i);

            if(strQuantity.equals("")||strQuantity.isEmpty()||strQuantity.equals("0")){
                if(strModStatus.equals("4")){
                    MyToast.myShow(SubDetailForMultipleActivity.this,"??????:"+txtMultipleDetailProductName.getText().toString()+",???????????????0",2,0);
                    return false;
                }else{
                    if(i==0){
                        MyToast.myShow(SubDetailForMultipleActivity.this,"??????:"+txtMultipleDetailProductName.getText().toString()+",???????????????0",2,0);
                        return false;
                    }
                }

            }
        }

        return true;
    }

    /**
    *??????: ????????????????????????????????????????????????????????????
    *?????????2022/6/10
    **/
    private void saveData(String strAction,String strActionId,String qcstatus,String qrcode){
        if(checkListItemQuantity(strAction)){
            saveDbData(strAction,strActionId,qcstatus,qrcode);
        }
    }

    /**
    *??????: ?????????????????????????????????????????????????????????
    *?????????2022/6/10
    **/
    private void saveDbData(String strAction,String strActionId,String qcstatus,String qrcode){
        //???????????????
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForMultipleActivity.this,"???????????????",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                int iCount = 0;
                statusCode = "-1";
                statusDescription = "????????????";

                //????????????????????????
                iCount = hzDb.productDao().getCount();
                if(iCount>0){
                    //????????????
                    hzDb.productDao().deleteAll();
                }

                //??????????????????
                int iCnt = multipleDetailAdapter.getCount();
                for(int i= 0;i<iCnt;i++){
                    LinearLayout linearLayout = (LinearLayout)subMultipleView.getAdapter().getView(i,null,null);
                    TextView txtMultipleDetailProductCode = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProductCode);
                    TextView txtMultipleDetailDocno = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailDocno);
                    TextView txtMultipleDetailProcessId = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProcessId);
                    TextView txtMultipleDetailProcess = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProcess);
                    TextView txtMultipleDetailDevice = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailDevice);
                    TextView txtMultipleDetailLots = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailLots);
                    TextView txtMultipleDetailEmployee = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailEmployee);

                    String strQuantity;
                    if(strModStatus.equals("2")||strModStatus.equals("3")){
                        strQuantity = multipleDetailAdapter.getQuantity(0);
                    }else{
                        strQuantity = multipleDetailAdapter.getQuantity(i);
                    }

                    String strProductCode = txtMultipleDetailProductCode.getText().toString();
                    String strDocno = txtMultipleDetailDocno.getText().toString();
                    String strProcessId = txtMultipleDetailProcessId.getText().toString();
                    String strProcess = txtMultipleDetailProcess.getText().toString();
                    String strDevice = txtMultipleDetailDevice.getText().toString();
                    String strLots = txtMultipleDetailLots.getText().toString();
                    String strEmployee = txtMultipleDetailEmployee.getText().toString();
                    float fQuantity = Float.valueOf(strQuantity);

                    genRecordSetStr(strAction,strActionId,qcstatus,strProductCode,strDocno,"",strProcessId,strProcess,strDevice,strLots,strQuantity,"",i,strEmployee,qrcode);

                    //??????sqllite
                    productEntity = new ProductEntity(strDocno,strFlag,0,0,strProductCode,strProcessId,strProcess,strDevice,strEmployee,strLots,fQuantity);
                    hzDb.productDao().insert(productEntity);
                }

                //?????????T100?????????
                String webServiceName = "WorkReportRequestGen";

                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        mRecordSet+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

                if(mapResponseStatus.size()> 0){
                    for(Map<String,Object> mStatus: mapResponseStatus){
                        statusCode = mStatus.get("statusCode").toString();
                        statusDescription = mStatus.get("statusDescription").toString();
                    }

                    if(!statusCode.equals("0")){
                        //?????????????????????????????????
                        hzDb.productDao().deleteAll();
                    }

                }else{
                    //?????????????????????????????????
                    hzDb.productDao().deleteAll();
                }

                //????????????????????????
                iCount = hzDb.productDao().getCount();
                if(iCount>0){
                    statusCode = "0";
                    statusDescription = "????????????";
                }

                e.onNext(statusCode);
                e.onNext(statusDescription);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {

            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription, 1, 1);
                }else{
                    MyToast.myShow(SubDetailForMultipleActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *??????: ?????????????????????ERP
    *?????????2022/6/10
    **/
    private void savePrintToT100(String strAction,String strActionId,String qcstatus,String qrcode){
        //???????????????
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForMultipleActivity.this,"???????????????",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                //??????
                statusCode = "-1";
                statusDescription = "???????????????,????????????";

                //????????????????????????
                productEntityList = hzDb.productDao().getAll();
                if(productEntityList.size()>0){
                    for(int i=0;i<productEntityList.size();i++){
                        String strProductCode = productEntityList.get(i).getProductCode();
                        String strDocno = productEntityList.get(i).getProductDocno();
                        String strProcessId = productEntityList.get(i).getProcessId();
                        String strProcess = productEntityList.get(i).getProcess();
                        String strDevice = productEntityList.get(i).getDevices();
                        String strLots = productEntityList.get(i).getLots();
                        String strEmployee = productEntityList.get(i).getProductUser();
                        float fQuantity = productEntityList.get(i).getQuantity();
                        String strQuantity = String.valueOf(fQuantity);

                        genRecordSetStr(strAction,strActionId,qcstatus,strProductCode,strDocno,"",strProcessId,strProcess,strDevice,strLots,strQuantity,"",i,strEmployee,qrcode);
                    }

                    //?????????T100?????????
                    String webServiceName = "WorkReportRequestGen";

                    //?????????????????????
                    T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                    String requestBody = "&lt;Document&gt;\n"+
                            "&lt;RecordSet id=\"1\"&gt;\n"+
                            mRecordSet+
                            "&lt;/RecordSet&gt;\n"+
                            "&lt;/Document&gt;\n";
                    String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                    mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

                    if(mapResponseStatus.size()> 0){
                        for(Map<String,Object> mStatus: mapResponseStatus){
                            statusCode = mStatus.get("statusCode").toString();
                            statusDescription = mStatus.get("statusDescription").toString();
                        }

                        //?????????????????????????????????
                        hzDb.productDao().deleteAll();
                    }
                }

                e.onNext(statusCode);
                e.onNext(statusDescription);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {

            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    finish();
                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription, 1, 1);
                }else{
                    MyToast.myShow(SubDetailForMultipleActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *??????: ??????request?????????ERP
    *?????????2022/6/10
    **/
    private void saveMultipleToT100(String strAction,String strActionId,String qcstatus,String qrcode){
        int iCount = multipleDetailAdapter.getCount();
        mRecordSet="";

        if(checkListItemQuantity(strAction)){
            for(int i= 0;i<iCount;i++){
                LinearLayout linearLayout = (LinearLayout)subMultipleView.getAdapter().getView(i,null,null);
                TextView txtMultipleDetailProductCode = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProductCode);
                TextView txtMultipleDetailDocno = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailDocno);
                TextView txtMultipleDetailPlanDate = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailPlanDate);
                TextView txtMultipleDetailProcessId = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProcessId);
                TextView txtMultipleDetailProcess = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailProcess);
                TextView txtMultipleDetailDevice = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailDevice);
                TextView txtMultipleDetailLots = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailLots);
                TextView txtMultipleDetailEmployee = (TextView)linearLayout.findViewById(R.id.txtMultipleDetailEmployee);

                String strQuantity;
                if(strModStatus.equals("2")||strModStatus.equals("3")){
                    strQuantity = multipleDetailAdapter.getQuantity(0);
                }else{
                    strQuantity = multipleDetailAdapter.getQuantity(i);
                }

                String strProductCode = txtMultipleDetailProductCode.getText().toString();
                String strDocno = txtMultipleDetailDocno.getText().toString();
                String strPlanDate = txtMultipleDetailPlanDate.getText().toString();
                String strProcessId = txtMultipleDetailProcessId.getText().toString();
                String strProcess = txtMultipleDetailProcess.getText().toString();
                String strDevice = txtMultipleDetailDevice.getText().toString();
                String strLots = txtMultipleDetailLots.getText().toString();
                String strEmployee = txtMultipleDetailEmployee.getText().toString();
                String strProductDocno = multipleDetailAdapter.getProductDocno(i);

                genRecordSetStr(strAction,strActionId,qcstatus,strProductCode,strDocno,strPlanDate,strProcessId,strProcess,strDevice,strLots,strQuantity,strProductDocno,i,strEmployee,qrcode);
            }
            saveData2ToT100(strAction,strActionId);
        }
    }

    /**
    *??????: XML????????????
    *?????????2022/6/10
    **/
    private void genRecordSetStr(String action,String actionid,String qcstatus,String strProductCode,String strDocno,String strPlanDate,String strProcessId,String strProcess,String strDevice,String strLots,String strQuantity,String strProductDocno,int i,String strEmployee,String qrcode){
        long timeCurrentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        String currentTime = simpleTimeFormat.format(timeCurrentTimeMillis);
        String currentDate = simpleDateFormat.format(new Date());

        //??????????????????
        if(mRecordSet==""||mRecordSet.isEmpty()){
            mRecordSet = "&lt;Master name=\"sffb_t\" node_id=\""+i+"\"&gt;\n"+
                    "&lt;Record&gt;\n"+
                    "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                    "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                    "&lt;Field name=\"sffbdocdt\" value=\""+strPlanDate+"\"/&gt;\n"+
                    "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"sffb004\" value=\""+ strWorkTime +"\"/&gt;\n"+  //??????
                    "&lt;Field name=\"sffb005\" value=\""+ strDocno +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"sffbseq\" value=\""+ strProcessId +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"sffb010\" value=\""+ strDevice +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //?????????????????????
                    "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //?????????????????????
                    "&lt;Field name=\"sffb029\" value=\""+ strProductCode +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"sffb017\" value=\""+ strQuantity +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //??????
                    "&lt;Field name=\"lots\" value=\""+ strLots +"\"/&gt;\n"+  //??????
                    "&lt;Field name=\"sffbdocno\" value=\""+ strProductDocno +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //??????
                    "&lt;Field name=\"planno\" value=\""+ strFlag +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"planseq\" value=\""+ txtMultipleInputCount.getText().toString() +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"planuser\" value=\""+ strEmployee +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"errorlots\" value=\""+ strErrorLots +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"models\" value=\""+ strModStatus +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"version\" value=\""+ strVersion +"\"/&gt;\n"+  //??????
                    "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+  //?????????
                    "&lt;Field name=\"actcode\" value=\""+ actionid +"\"/&gt;\n"+  //????????????ID
                    "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                    "&lt;Record&gt;\n"+
                    "&lt;Field name=\"sffyucseq\" value=\"1.0\"/&gt;\n"+
                    "&lt;/Record&gt;\n"+
                    "&lt;/Detail&gt;\n"+
                    "&lt;Memo/&gt;\n"+
                    "&lt;Attachment count=\"0\"/&gt;\n"+
                    "&lt;/Record&gt;\n"+
                    "&lt;/Master&gt;\n";
        }else{
            mRecordSet = mRecordSet + "&lt;Master name=\"sffb_t\" node_id=\""+i+"\"&gt;\n"+
                    "&lt;Record&gt;\n"+
                    "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                    "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                    "&lt;Field name=\"sffbdocdt\" value=\""+strPlanDate+"\"/&gt;\n"+
                    "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"sffb004\" value=\""+ strWorkTime +"\"/&gt;\n"+  //??????
                    "&lt;Field name=\"sffb005\" value=\""+ strDocno +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"sffbseq\" value=\""+ strProcessId +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"sffb010\" value=\""+ strDevice +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //?????????????????????
                    "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //?????????????????????
                    "&lt;Field name=\"sffb029\" value=\""+ strProductCode +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"sffb017\" value=\""+ strQuantity +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //??????
                    "&lt;Field name=\"lots\" value=\""+ strLots +"\"/&gt;\n"+  //??????
                    "&lt;Field name=\"sffbdocno\" value=\""+ strProductDocno +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //??????
                    "&lt;Field name=\"planno\" value=\""+ strFlag +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"planseq\" value=\""+ txtMultipleInputCount.getText().toString() +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"planuser\" value=\""+ strEmployee +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"errorlots\" value=\""+ strErrorLots +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"models\" value=\""+ strModStatus +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"version\" value=\""+ strVersion +"\"/&gt;\n"+  //??????
                    "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //????????????
                    "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+  //?????????
                    "&lt;Field name=\"actcode\" value=\""+ actionid +"\"/&gt;\n"+  //????????????ID
                    "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                    "&lt;Record&gt;\n"+
                    "&lt;Field name=\"sffyucseq\" value=\"1.0\"/&gt;\n"+
                    "&lt;/Record&gt;\n"+
                    "&lt;/Detail&gt;\n"+
                    "&lt;Memo/&gt;\n"+
                    "&lt;Attachment count=\"0\"/&gt;\n"+
                    "&lt;/Record&gt;\n"+
                    "&lt;/Master&gt;\n";
        }

    }

    /**
    *??????: ??????webservice????????????
    *?????????2022/6/10
    **/
    private void saveData2ToT100(String action,String actionid){
        //???????????????
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForMultipleActivity.this,"???????????????",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "WorkReportRequestGen";

                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        mRecordSet+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100ResponseDocno3(strResponse,"docno");

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
                    MyToast.myShow(SubDetailForMultipleActivity.this,"??????????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    String strDocno="";

                    int i=0;

                    if(action.equals("save")){
                        if(mapResponseList.size()> 0) {
                            for (Map<String, Object> mResponse : mapResponseList) {
                                strDocno = mResponse.get("Docno").toString();
                                multipleDetailAdapter.updateData(i,subMultipleView,strDocno);
                                i++;
                            }
                        }
                    }
                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription, 1, 1);

                    if(action.equals("print")){
                        finish();
                    }else{
                        if(actionid.equals("10")){
                            imgMultipleStartStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                        }else if(actionid.equals("11")){
                            if(mapResponseList.size()> 0) {
                                String strErrorCount="";
                                for (Map<String, Object> mResponse : mapResponseList) {
                                    strErrorCount = mResponse.get("ErrorCount").toString();
                                }
                                txtMultipleQcCount.setText(strErrorCount);
                            }

                            imgMultipleQcStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                        }else if(actionid.equals("12")){
                            imgMultipleProductStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));

                            //????????????????????????
                            if(mapResponseList.size()> 0) {
                                String sProductCode="";
                                String sProductName="";
                                String sProductModels="";
                                String sProcessId="";
                                String sProcess="";
                                String sDevice="";
                                String sQuantity="";
                                String sEmp="";
                                String sStatus="";
                                String sAttribute="";

                                for (Map<String, Object> mResponse : mapResponseList) {
                                    sProductCode = mResponse.get("ProductCode").toString();
                                    sProductName = mResponse.get("ProductName").toString();
                                    sProductModels = mResponse.get("ProductModels").toString();
                                    sProcessId = mResponse.get("ProcessId").toString();
                                    sProcess = mResponse.get("Process").toString();
                                    sDevice = mResponse.get("Device").toString();
                                    sQuantity = mResponse.get("Quantity").toString();
                                    sEmp = mResponse.get("Emp").toString();
                                    sStatus = mResponse.get("Status").toString();
                                    sAttribute = mResponse.get("Attribute").toString();
                                }

                                Intent intent = new Intent(SubDetailForMultipleActivity.this,CheckMaterialActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("Docno",strFlag);
                                bundle.putString("ProductCode",sProductCode);
                                bundle.putString("ProductName",sProductName);
                                bundle.putString("ProductModels",sProductModels);
                                bundle.putString("ProcessId",sProcessId);
                                bundle.putString("Process",sProcess);
                                bundle.putString("Device",sDevice);
                                bundle.putString("Quantity",sQuantity);
                                bundle.putString("Emp",sEmp);
                                bundle.putString("Status",sStatus);
                                bundle.putString("Attribute",sAttribute);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }

                        }else if(actionid.equals("13")){
                            if(mapResponseList.size()> 0) {
                                String strErrorCount="";
                                for (Map<String, Object> mResponse : mapResponseList) {
                                    strErrorCount = mResponse.get("ErrorCount").toString();
                                }
                                txtMultipleErrorCount.setText(strErrorCount);
                            }

                            imgMultipleErrorBeginStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                            imgMultipleQcStatus.setImageDrawable(getResources().getDrawable(R.drawable.fail));
                        }else if(actionid.equals("14")){
                            imgMultipleErrorEndStatus.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                        }
                    }

                }else{
                    MyToast.myShow(SubDetailForMultipleActivity.this, statusDescription, 0, 1);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *??????: ????????????????????????,?????????N
    *?????????2022/6/12
    **/
    private void updQrcodeData(String strAction,String strActionId,String qcstatus,String qrcode){
        //???????????????
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubDetailForMultipleActivity.this,"???????????????",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //?????????T100?????????:cwssp022
                String webServiceName = "MaterialCheckInsert";

                //?????????????????????
                long timeCurrentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
                String currentTime = simpleTimeFormat.format(timeCurrentTimeMillis);
                String currentDate = simpleDateFormat.format(new Date());

                //??????????????????
                int iCount = multipleDetailAdapter.getCount();
                String strProductDocno = "";

                for(int i= 0;i<iCount;i++){
                    if(strProductDocno.equals("")||strProductDocno.isEmpty()){
                        strProductDocno = multipleDetailAdapter.getItemValue(i,"Docno");
                    }else{
                        strProductDocno = strProductDocno + ","+multipleDetailAdapter.getItemValue(i,"Docno");
                    }

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
                        "&lt;Field name=\"sffb005\" value=\""+ strProductDocno +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"sffb012\" value=\""+ currentDate +"\"/&gt;\n"+  //?????????????????????
                        "&lt;Field name=\"sffb013\" value=\""+ currentTime +"\"/&gt;\n"+  //?????????????????????
                        "&lt;Field name=\"planuser\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"processid\" value=\""+ strProcessId +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //??????
                        "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //??????
                        "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+  //?????????
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
                mapList = t100ServiceHelper.getT100ResponseDocno4(strResponse,"docno");

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
                    MyToast.myShow(SubDetailForMultipleActivity.this,"??????????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForMultipleActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
//                    String sDocno = "";
//                    String sProductCode = "";
                    String sProductName = "";
//                    String sProcessId = "";

                    if(mapList.size()> 0) {
                        for (Map<String, Object> mResponse : mapList) {
//                            sDocno = mResponse.get("Docno").toString();
//                            sProductCode = mResponse.get("ProductCode").toString();
                            sProductName = mResponse.get("ProductName").toString();
//                            sProcessId = mResponse.get("ProcessId").toString();
                        }
                    }
                    MyToast.myShow(SubDetailForMultipleActivity.this, "??????:"+sProductName+",??????????????????", 1, 1);
                }else{
//                    MyToast.myShow(SubDetailForMultipleActivity.this,statusDescription,0,0);
                    ShowAlertDialog.myShow(SubDetailForMultipleActivity.this,statusDescription);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}