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

        //初始化
        initBundle();
        initView();

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

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(scanReceiver);
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
                IntentIntegrator intentIntegrator = new IntentIntegrator(PrintLabelActivity.this);
//                intentIntegrator.setTimeout(5000);
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

    //PDA扫描数据接收
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

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){
        //解析二维码
        if(qrContent.equals("")||qrContent.isEmpty()){
            MyToast.myShow(context,"条码错误:"+qrContent,0,1);
        }else{
            getQrcodeData(qrContent);
        }
    }

    /**
     *描述: 获取传入参数值
     *日期：2022/6/6
     **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
    }

    /**
     *描述: 初始化控件
     *日期：2022/6/6
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.printLabelToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
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

        //定义事件
        btnPrintLabelQrcode.setOnClickListener(new btnClickListener());
        btnPrint.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
        printLabelList.setOnItemClickListener(new listItemClickListener());
    }

    /**
    *描述: 按钮事件实现
    *日期：2022/6/15
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
    *描述: 标签列表行单击
    *日期：2022/6/15
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
    *描述: 获取二维码信息
    *日期：2022/6/15
    **/
    private void getQrcodeData(String qrcode){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(PrintLabelActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "ItemInfoGet";
                String strType = "4";
                String strwhere = "";

                //发送服务器请求
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
                    MyToast.myShow(PrintLabelActivity.this,"执行接口错误",2,0);
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
                        //显示单头数据
                        printLabelProductCode.setText(mapResponseList.get(0).get("ProductCode").toString());
                        printLabelProductName.setText(mapResponseList.get(0).get("ProductName").toString());
                        printLabelProductModels.setText(mapResponseList.get(0).get("ProductModels").toString());
                        printLabelQuantity.setText(mapResponseList.get(0).get("Quantity").toString());
                        printLabelCurrentProcessId.setText(mapResponseList.get(0).get("ProcessId").toString());
                        printLabelCurrentProcess.setText(mapResponseList.get(0).get("Process").toString());
                        printLabelAttribute.setText(mapResponseList.get(0).get("Attribute").toString());
                        printLabelQrcode.setText(mapResponseList.get(0).get("Qrcode").toString());
                        printLabelProductDocno.setText(mapResponseList.get(0).get("Docno").toString());

                        //显示清单
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
    *描述: 打印标签
    *日期：2022/6/15
    **/
    private void updateQrcodeData(){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(PrintLabelActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "RepeatPrintLabel";

                //标签数量
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

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"qrcode\" value=\""+ printLabelQrcode.getText().toString() +"\"/&gt;\n"+  //条码编号
                        "&lt;Field name=\"bcaa009\" value=\""+ fQty +"\"/&gt;\n"+  //条码数量
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
                    MyToast.myShow(PrintLabelActivity.this,"执行接口错误",2,0);
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