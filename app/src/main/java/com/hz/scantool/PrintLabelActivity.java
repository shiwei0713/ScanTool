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
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import org.w3c.dom.Text;

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

public class PrintLabelActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle;

    private EditText inputPrintLabelQrcode,printLabelModQuantity;
    private Button btnPrintLabelQrcode,btnPrint,btnCancel;
    private TextView printLabelProductCode,printLabelProductName,printLabelProductModels,printLabelQuantity,printLabelCurrentProcessId,printLabelCurrentProcess;
    private TextView printLabelAttribute,printLabelProductDocno,printLabelQrcode;
    private ListView printLabelList;

    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private String statusCode,statusDescription;
    private PrintLabelListAdapter printLabelListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_label);

        //?????????
        initBundle();
        initView();

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

    /**
     *??????: ?????????????????????
     *?????????2022/5/25
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu,menu);

        return true;
    }

    /**
     *??????: ?????????????????????
     *?????????2022/5/25
     **/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //???????????????????????????
        switch (item.getItemId()){
            case R.id.action_scan:
                //??????zxing????????????
                IntentIntegrator intentIntegrator = new IntentIntegrator(PrintLabelActivity.this);
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
    private void scanResult(String qrContent, Context context, Intent intent){
        //???????????????
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"????????????:"+qrContent,0,1);
        }else{
            getQrcodeData(qrContent);
        }
    }

    /**
     *??????: ?????????????????????
     *?????????2022/6/6
     **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
    }

    /**
     *??????: ???????????????
     *?????????2022/6/6
     **/
    private void initView(){
        //???????????????
        Toolbar toolbar=findViewById(R.id.printLabelToolBar);
        setSupportActionBar(toolbar);

        //??????????????????????????????????????????
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //?????????????????????
        inputPrintLabelQrcode = findViewById(R.id.inputPrintLabelQrcode);
        printLabelProductCode = findViewById(R.id.printLabelProductCode);
        printLabelProductName = findViewById(R.id.printLabelProductName);
        printLabelProductModels = findViewById(R.id.printLabelProductModels);
        printLabelQuantity = findViewById(R.id.printLabelQuantity);
        printLabelModQuantity = findViewById(R.id.printLabelModQuantity);
        printLabelCurrentProcessId = findViewById(R.id.printLabelCurrentProcessId);
        printLabelCurrentProcess = findViewById(R.id.printLabelCurrentProcess);
        printLabelAttribute = findViewById(R.id.printLabelAttribute);
        printLabelProductDocno = findViewById(R.id.printLabelProductDocno);
        printLabelQrcode = findViewById(R.id.printLabelQrcode);
        btnPrintLabelQrcode = findViewById(R.id.btnPrintLabelQrcode);
        btnPrint = findViewById(R.id.btnPrint);
        btnCancel = findViewById(R.id.btnCancel);
        printLabelList = findViewById(R.id.printLabelList);

        //????????????
        btnPrintLabelQrcode.setOnClickListener(new btnClickListener());
        btnPrint.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
        printLabelList.setOnItemClickListener(new listItemClickListener());
    }

    /**
    *??????: ??????????????????
    *?????????2022/6/15
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnPrintLabelQrcode:
                    String sCode = inputPrintLabelQrcode.getText().toString();
                    getQrcodeData(sCode.toUpperCase());
                    break;
                case R.id.btnPrint:
                    updateQrcodeData();
                    break;
                case R.id.btnCancel:
                    finish();
                    break;
            }
        }
    }

    /**
    *??????: ?????????????????????
    *?????????2022/6/15
    **/
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            TextView txtLabelQrcode = view.findViewById(R.id.txtLabelQrcode);
            TextView txtLabelProductCode = view.findViewById(R.id.txtLabelProductCode);
            TextView txtLabelProductName = view.findViewById(R.id.txtLabelProductName);
            TextView txtLabelProductModels = view.findViewById(R.id.txtLabelProductModels);
            TextView txtLabelProcessId = view.findViewById(R.id.txtLabelProcessId);
            TextView txtLabelProcess = view.findViewById(R.id.txtLabelProcess);
            TextView txtLabelDocno = view.findViewById(R.id.txtLabelDocno);
            TextView txtLabelQuantity = view.findViewById(R.id.txtLabelQuantity);

            printLabelProductCode.setText(txtLabelProductCode.getText().toString());
            printLabelProductName.setText(txtLabelProductName.getText().toString());
            printLabelProductModels.setText(txtLabelProductModels.getText().toString());
            printLabelQuantity.setText(txtLabelQuantity.getText().toString());
            printLabelCurrentProcessId.setText(txtLabelProcessId.getText().toString());
            printLabelCurrentProcess.setText(txtLabelProcess.getText().toString());
            printLabelQrcode.setText(txtLabelQrcode.getText().toString());
            printLabelProductDocno.setText(txtLabelDocno.getText().toString());
        }
    }

    /**
    *??????: ?????????????????????
    *?????????2022/6/15
    **/
    private void getQrcodeData(String qrcode){
        //???????????????
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(PrintLabelActivity.this,"???????????????",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //?????????T100?????????
                String webServiceName = "ItemInfoGet";
                String strType = "4";
                String strwhere = "";

                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonItemQrcodeData(strResponse,"iteminfo");

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
                    MyToast.myShow(PrintLabelActivity.this,"??????????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(PrintLabelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()> 0) {
                        //??????????????????
                        printLabelProductCode.setText(mapResponseList.get(0).get("ProductCode").toString());
                        printLabelProductName.setText(mapResponseList.get(0).get("ProductName").toString());
                        printLabelProductModels.setText(mapResponseList.get(0).get("ProductModels").toString());
                        printLabelQuantity.setText(mapResponseList.get(0).get("Quantity").toString());
                        printLabelCurrentProcessId.setText(mapResponseList.get(0).get("ProcessId").toString());
                        printLabelCurrentProcess.setText(mapResponseList.get(0).get("Process").toString());
                        printLabelAttribute.setText(mapResponseList.get(0).get("Attribute").toString());
                        printLabelQrcode.setText(mapResponseList.get(0).get("Qrcode").toString());
                        printLabelProductDocno.setText(mapResponseList.get(0).get("Docno").toString());

                        //????????????
                        printLabelListAdapter = new PrintLabelListAdapter(mapResponseList,getApplicationContext());
                        printLabelList.setAdapter(printLabelListAdapter);
                    }
                }else{
                    MyToast.myShow(PrintLabelActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *??????: ????????????
    *?????????2022/6/15
    **/
    private void updateQrcodeData(){
        //???????????????
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(PrintLabelActivity.this,"???????????????",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //?????????T100?????????
                String webServiceName = "RepeatPrintLabel";

                //????????????
                String sQuantity = printLabelModQuantity.getText().toString();
                String sLabelQuantity = printLabelQuantity.getText().toString();
                float fQuantity = 0;
                float fLabelQuantity = 0;
                float fQty = 0;
                if(sQuantity.equals("")||sQuantity.isEmpty()){
                    sQuantity = "0";
                }

                try{
                    fQuantity = Float.valueOf(sQuantity);
                    fLabelQuantity = Float.valueOf(sLabelQuantity);
                }catch (Exception ex){
                    ex.printStackTrace();
                    fQuantity = 0;
                    fLabelQuantity = 0;
                }
                if(fQuantity>0){
                    fQty = fQuantity;
                }else{
                    fQty = fLabelQuantity;
                }

                //?????????????????????
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"qrcode\" value=\""+ printLabelQrcode.getText().toString() +"\"/&gt;\n"+  //????????????
                        "&lt;Field name=\"bcaa009\" value=\""+ fQty +"\"/&gt;\n"+  //????????????
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
                    }
                }else{
                    MyToast.myShow(PrintLabelActivity.this,"??????????????????",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(PrintLabelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(PrintLabelActivity.this,statusDescription,1,0);
                }else{
                    MyToast.myShow(PrintLabelActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}