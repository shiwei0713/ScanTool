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
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.gprinter.command.CpclCommand;
import com.gprinter.command.EscCommand;
import com.gprinter.command.FactoryCommand;
import com.gprinter.command.LabelCommand;
import com.gprinter.io.EthernetPort;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubAdapter;
import com.hz.scantool.adapter.SubListAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;
import com.hz.scantool.printer.CheckWifiConnThread;
import com.hz.scantool.printer.Constant;
import com.hz.scantool.printer.DeviceConnFactoryManager;
import com.hz.scantool.printer.PrintContent;
import com.hz.scantool.printer.PrinterCommand;
import com.hz.scantool.printer.ThreadPool;
import com.hz.scantool.printer.WifiParameterConfig;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
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

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;
import static com.hz.scantool.printer.Constant.MESSAGE_UPDATE_PARAMETER;
import static com.hz.scantool.printer.Constant.tip;
import static com.hz.scantool.printer.Constant.ACTION_USB_PERMISSION;
import static com.hz.scantool.printer.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.hz.scantool.printer.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class SubActivity extends AppCompatActivity {

    private String strTitle;
    private String strType;
    private String statusCode;
    private int intIndex;
    private String statusDescription;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private ProgressBar progressBar;
    private LoadingDialog loadingDialog;
    private ListView listView;
    private SubAdapter subAdapter;
    private TextView txtLoginout;
    private TextView txtWorktime;
    private TextView txtQuery,txtQueryStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        //???????????????
        initBundle();

        //???????????????
        initView();
        setWorktime();

        //???????????????
        Toolbar toolbar=findViewById(R.id.subListToolBar);
        setSupportActionBar(toolbar);

        //??????????????????????????????????????????
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //?????????????????????
        strType = "1";
        getSubListData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(intIndex != 4){
            getMenuInflater().inflate(R.menu.sub_menu,menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //???????????????????????????
        switch (item.getItemId()){
            case R.id.action_scan:
                //??????zxing????????????
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubActivity.this);
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

    @Override
    protected void onResume() {
        super.onResume();

        //?????????????????????
        strType = "1";
        getSubListData();
    }

    //??????????????????
    private void scanResult(String qrContent, Context context, Intent intent){

    }

    //?????????????????????
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        intIndex = bundle.getInt("index");
        strTitle = bundle.getString("title");
    }

    private void initView(){
        txtLoginout = findViewById(R.id.txtLoginout);
        txtWorktime = findViewById(R.id.txtWorktime);
        txtQuery = findViewById(R.id.txtQuery);
        txtQueryStock = findViewById(R.id.txtQueryStock);
        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.subView);

        txtLoginout.setText("??????:"+UserInfo.getUserId(getApplicationContext()));
        txtQuery.setOnClickListener(new queryClickListener());
        txtQueryStock.setOnClickListener(new queryClickListener());
        listView.setOnItemClickListener(new listItemClickListener());
    }

    private void setWorktime(){
        long timeCurrentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
        String currentTime = simpleDateFormat.format(timeCurrentTimeMillis);
        String strWorkTime = "??????";

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

        txtWorktime.setText(strWorkTime);
    }

    //????????????
    private class queryClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            Intent intent;

            switch (view.getId()){
                case R.id.txtQuery:
                    intent = new Intent(SubActivity.this,EmployeeReportActivity.class);
                    startActivity(intent);
                    break;
                case R.id.txtQueryStock:
                    intent = new Intent(SubActivity.this,QueryStockActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }

    //???????????????
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
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
            TextView txtSubFlag = view.findViewById(R.id.txtSubFlag);
            TextView txtSubModStatus = view.findViewById(R.id.txtSubModStatus);
            TextView txtSubOperateCount = view.findViewById(R.id.txtSubOperateCount);
            TextView txtSubPrintCount = view.findViewById(R.id.txtSubPrintCount);
            TextView txtVersion = view.findViewById(R.id.txtVersion);
            String modStatus = txtSubModStatus.getText().toString();

            Intent intent = new Intent(SubActivity.this,SubDetailForMultipleActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("Flag",txtSubFlag.getText().toString());
            bundle.putString("ProcessId",txtProcessId.getText().toString());
            bundle.putString("Process",txtProcess.getText().toString());
            bundle.putString("ModStatus",modStatus);
            bundle.putString("OperateCount",txtSubOperateCount.getText().toString());
            bundle.putString("PrintCount",txtSubPrintCount.getText().toString());
            bundle.putString("StartStatus",subAdapter.getItemValue(i,"StartStatus"));
            bundle.putString("CheckStatus",subAdapter.getItemValue(i,"CheckStatus"));
            bundle.putString("UpStatus",subAdapter.getItemValue(i,"UpStatus"));
            bundle.putString("ErrorStartStatus",subAdapter.getItemValue(i,"ErrorStartStatus"));
            bundle.putString("ErrorStopStatus",subAdapter.getItemValue(i,"ErrorStopStatus"));
            bundle.putString("Version",txtVersion.getText().toString());
            bundle.putString("StartTime",subAdapter.getItemValue(i,"StartTime"));
            bundle.putString("CheckTime",subAdapter.getItemValue(i,"CheckTime"));
            bundle.putString("UpTime",subAdapter.getItemValue(i,"UpTime"));
            bundle.putString("ErrorTime",subAdapter.getItemValue(i,"ErrorTime"));
            bundle.putString("ProductTotal",subAdapter.getItemValue(i,"ProductTotal"));
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    //????????????
    private void getSubListData(){
        //???????????????
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //?????????T100?????????
                String webServiceName = "ProductListGet";
                String strwhere = " sfaauc002='"+UserInfo.getUserId(getApplicationContext())+"'";

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
                mapResponseList = t100ServiceHelper.getT100JsonProductData(strResponse,"workorder");

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

                        if(statusCode.equals("0")){
                            int progress = progressBar.getProgress();
                            progress = progress + 50;
                            progressBar.setProgress(progress);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    subAdapter = new SubAdapter(mapResponseList,getApplicationContext(),"CJ");
                    listView.setAdapter(subAdapter);
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }
}