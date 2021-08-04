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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.DetailListItemAdapter;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.helper.SharedHelper;
import com.hz.scantool.helper.WebServiceHelper;
import com.hz.scantool.models.Company;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class DetailListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private Integer intIndex;
    private String strDocno;
    private String strProductCode;
    private String strProductName;
    private String strProductModels;
    private String strProducerId;
    private String strProducer;
    private String strStockId;
    private String strStock;
    private String strQuantity;
    private String strQuantityPcs;

    private LinearLayout linearDetailDocno;
    private LinearLayout linearDetailProductName;
    private LinearLayout linearDetailProductModels;
    private LinearLayout linearDetailStock;

    private TextView txtDetailDocnoTitle;
    private TextView txtDetailProductNameTitle;
    private TextView txtDetailProductModelsTitle;
    private TextView txtDetailQuantityTitle;
    private TextView txtDetailQuantityPcsTitle;
    private TextView txtDetailProducerTitle;

    private TextView txtDetailDocno;
    private TextView txtDetailProductCode;
    private TextView txtDetailProductName;
    private TextView txtDetailProductModels;
    private TextView txtDetailQuantity;
    private TextView txtDetailQuantityCurrent;
    private TextView txtDetailQuantityPcs;
    private TextView txtDetailProducerId;
    private TextView txtDetailProducer;
    private TextView txtDetailStockId;
    private TextView txtDetailStock;

    private Button btnCancel;
    private Button btnSubmit;

    private ListView listView;
    private List<Map<String,Object>> list;
    private DetailListItemAdapter detailItemAdapter;
    public static String listJson = "";

    private Company company;
    Context mContext;
    private String nerworkType;
    private String userCode;
    private String strStatus = "Y";
    SharedHelper sharedHelper;

    //创建Handler
    private final Handler detailListHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            listJson = (String)msg.obj;
            list = decodeJsonData(listJson);
            //初始化ListView
            detailItemAdapter = new DetailListItemAdapter(list,getApplicationContext(),sharedHelper,txtDetailDocno.getText().toString(),intIndex);
            listView.setAdapter(detailItemAdapter);

            //刷新汇总数
            refreshCurrentPcs();
        }
    };

    //创建Handler
    private final Handler uHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if(msg.what == 1){
//                Toast.makeText(DetailListActivity.this,"提交成功!",Toast.LENGTH_SHORT).show();
                MyToast.myShow(DetailListActivity.this,"提交成功",1);
            }else{
//                Toast.makeText(DetailListActivity.this,"提交失败!",Toast.LENGTH_SHORT).show();
                MyToast.myShow(DetailListActivity.this,"提交失败",0);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list);

        //初始化存储信息
        mContext=getApplicationContext();
        sharedHelper=new SharedHelper(mContext);

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.detailListToolBar);
        setSupportActionBar(toolbar);

        //获取传入参数
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        intIndex=bundle.getInt("index");
        strDocno=bundle.getString("txtListDocno");
        strProductCode=bundle.getString("txtListProductCode");
        strProductName=bundle.getString("txtListProductName");
        strProductModels=bundle.getString("txtListProductModels");
        strProducerId=bundle.getString("txtListProducerId");
        strProducer=bundle.getString("txtListProducer");
        strStockId=bundle.getString("txtListStockId");
        strStock=bundle.getString("txtListStock");
        strQuantity=bundle.getString("txtListQuantity");
        strQuantityPcs=bundle.getString("txtListQuantityPcs");

        //工具栏增加返回按钮和标题显示
        //设置标题
        String strTitle = "备货信息";
        switch (intIndex){
            case 1:
                strTitle = "检验信息";
                break;
            case 5:
                break;
        }
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        initView();

        //初始化控件事件
        btnSubmit.setOnClickListener(new btnActionListener());
        btnCancel.setOnClickListener(new btnActionListener());
//        listView.setOnItemClickListener(new listViewClick());  //列表项点击事件

        //初始化控件值
        txtDetailDocno.setText(strDocno);
        txtDetailProductCode.setText(strProductCode);
        txtDetailProductName.setText(strProductName);
        txtDetailQuantity.setText(strQuantity);
        txtDetailQuantityPcs.setText(strQuantityPcs);
        txtDetailProductModels.setText(strProductModels);
        txtDetailProducerId.setText(strProducerId);
        txtDetailProducer.setText(strProducer);
        txtDetailStock.setText(strStock);
        txtDetailStockId.setText(strStockId);

        //获取网络接口数据并解析
        getData();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //刷新显示数
        getData();
    }

    private void initView(){
        //初始化标题
        String strProducerTitle = mContext.getResources().getString(R.string.detail_title5);

        //初始化控件
        linearDetailDocno = findViewById(R.id.linearDetailDocno);
        linearDetailProductName = findViewById(R.id.linearDetailProductName);
        linearDetailProductModels = findViewById(R.id.linearDetailProductModels);
        linearDetailStock = findViewById(R.id.linearDetailStock);

        txtDetailDocnoTitle = findViewById(R.id.txtDetailDocnoTitle);
        txtDetailProductNameTitle = findViewById(R.id.txtDetailProductNameTitle);
        txtDetailProductModelsTitle = findViewById(R.id.txtDetailProductModelsTitle);
        txtDetailQuantityTitle = findViewById(R.id.txtDetailQuantityTitle);
        txtDetailQuantityPcsTitle = findViewById(R.id.txtDetailQuantityPcsTitle);
        txtDetailProducerTitle = findViewById(R.id.txtDetailProducerTitle);

        txtDetailDocno = findViewById(R.id.txtDetailDocno);
        txtDetailProductCode =findViewById(R.id.txtDetailProductCode);
        txtDetailProductName =findViewById(R.id.txtDetailProductName);
        txtDetailProductModels =findViewById(R.id.txtDetailProductModels);
        txtDetailQuantity =findViewById(R.id.txtDetailQuantity);
        txtDetailQuantityPcs =findViewById(R.id.txtDetailQuantityPcs);
        txtDetailQuantityCurrent = findViewById(R.id.txtDetailQuantityCurrent);
        txtDetailProducerId =findViewById(R.id.txtDetailProducerId);
        txtDetailProducer =findViewById(R.id.txtDetailProducer);
        txtDetailStock =findViewById(R.id.txtDetailStock);
        txtDetailStockId =findViewById(R.id.txtDetailStockId);

        btnSubmit = findViewById(R.id.btnSubmit);
        btnCancel = findViewById(R.id.btnCancel);
        listView = (ListView) findViewById(R.id.detailListView);

        switch (intIndex){
            case 1:
                linearDetailProductName.setVisibility(View.GONE);
                linearDetailProductModels.setVisibility(View.GONE);
                linearDetailStock.setVisibility(View.GONE);
                btnSubmit.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                strProducerTitle = mContext.getResources().getString(R.string.item_title_saler);
                break;
            case 5:
                linearDetailProductName.setVisibility(View.GONE);
                linearDetailProductModels.setVisibility(View.GONE);
                strProducerTitle = mContext.getResources().getString(R.string.item_title_saler);
                break;
        }

        txtDetailProducerTitle.setText(strProducerTitle);
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
                String qrDetailContent = intent.getStringExtra("scannerdata");

                try{
                    if(!qrDetailContent.isEmpty()){
                        String qrFirst = qrDetailContent.substring(1,3);

                        if(qrFirst.equals("XM")){
//                            Toast.makeText(context,"请扫描零件条码！",Toast.LENGTH_SHORT).show();
                            MyToast.myShow(context,"请扫描零件条码",2);
                        }else{
                            Bundle bundle;
                            switch (intIndex){
                                //质量检验
                                case 1:
                                    intent = new Intent(context,DetailActivity.class);
                                    //设置传入参数
                                    bundle=new Bundle();
                                    bundle.putString("qrCode",qrDetailContent);
                                    bundle.putString("docno",txtDetailDocno.getText().toString());
                                    bundle.putInt("index",intIndex);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    getData();
                                    refreshCurrentPcs();
                                    break;
                                case 5:
                                    //刷新显示数据
                                    try{
                                        List<Map<String,Object>> refreshList = refreshData(qrDetailContent);
                                        //初始化ListView
                                        detailItemAdapter = new DetailListItemAdapter(refreshList,getApplicationContext(),sharedHelper,txtDetailDocno.getText().toString(),intIndex);
                                        listView.setAdapter(detailItemAdapter);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                        }
                    }else{
//                        Toast.makeText(context,"扫描结果:"+qrDetailContent,Toast.LENGTH_SHORT).show();
                        MyToast.myShow(context,"扫描结果:"+qrDetailContent,2);
                    }
                }catch (Exception e){
                    e.printStackTrace();
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
            final String qrDetailContent = intentResult.getContents();
            Log.i("QRCODE","CODE:"+qrDetailContent);

            try{
                if(!qrDetailContent.isEmpty()){
                    Bundle bundle;
                    switch (intIndex){
                        //质量检验
                        case 1:
                            Intent intent = new Intent(this,DetailActivity.class);
                            //设置传入参数
                            bundle=new Bundle();
                            bundle.putString("qrCode",qrDetailContent);
                            bundle.putString("docno",txtDetailDocno.getText().toString());
                            bundle.putInt("index",intIndex);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            getData();
                            refreshCurrentPcs();
                            break;
                        case 5:
                            //刷新显示数据
                            try{
                                List<Map<String,Object>> refreshList = refreshData(qrDetailContent);
                                //初始化ListView
                                detailItemAdapter = new DetailListItemAdapter(refreshList,getApplicationContext(),sharedHelper,txtDetailDocno.getText().toString(),intIndex);
                                listView.setAdapter(detailItemAdapter);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            break;
                    }
                }else{
//                    Toast.makeText(this,"扫描结果:"+qrDetailContent,Toast.LENGTH_SHORT).show();
                    MyToast.myShow(this,"扫描结果:"+qrDetailContent,2);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public class btnActionListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //提交备货
                case R.id.btnSubmit:
                    if(checkStatus()){
                        sendData();
                    }else{
                        strStatus = "N";
//                        Toast.makeText(DetailListActivity.this,"所有零件备货完成才可提交",Toast.LENGTH_SHORT).show();
                        MyToast.myShow(DetailListActivity.this,"所有零件备货完成才可提交",2);
                    }
                    break;
                case R.id.btnCancel:
                    refreshCurrentPcs();
                    break;
            }
        }
    }

    public class listViewClick implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //调用zxing扫码界面
            IntentIntegrator intentIntegrator = new IntentIntegrator(DetailListActivity.this);
            intentIntegrator.setTimeout(5000);
            intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE

            //开始扫描
            intentIntegrator.initiateScan();
        }
    }

    private Boolean checkStatus(){
        Boolean isSuccess = true;

//        for(int i=0;i<list.size();i++){
//            String erpStatus = (String)list.get(i).get("Status");
//            if(erpStatus.equals("N")){
//                isSuccess = false;
//                return isSuccess;
//            }
//        }

        return true;
    }

    //获取显示数据
    public void getData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String strDetailContent = "";
//                    //初始化网络类型和营运据点
//                    company=new Company();
//                    Map<String,String> data=sharedHelper.readShared();
//                    nerworkType = data.get("network");
//                    company.setSite(data.get("userSite"));
                    //设置传入请求参数
                    StringBuilder strWebRequestConten= new StringBuilder();
                    strWebRequestConten.append("&lt;Parameter&gt;\n"+
                            "&lt;Record&gt;\n"+
                            "&lt;Field name=\"enterprise\" value=\"10\"/&gt;\n"+
                            "&lt;Field name=\"site\" value=\""+company.getCode()+"\"/&gt;\n"+
                            "&lt;Field name=\"code\" value=\""+strProductCode+"\"/&gt;\n"+
                            "&lt;Field name=\"stock\" value=\""+strStockId+"\"/&gt;\n"+
                            "&lt;Field name=\"type\" value=\""+intIndex+"\"/&gt;\n"+
                            "&lt;Field name=\"qrcode\" value=\""+strDocno+"\"/&gt;\n"+
                            "&lt;/Record&gt;\n"+
                            "&lt;/Parameter&gt;\n"+
                            "&lt;Document/&gt;\n");

                    //设置WebService参数
                    WebServiceHelper webServiceHelper=new WebServiceHelper();
                    webServiceHelper.setWebKey("16baae6c40b922d8ddb12a0320d8ea1d");
                    webServiceHelper.setWebTimestamp("20201114083106031");
                    webServiceHelper.setWebName("AppGetStockLot");
                    webServiceHelper.setWebUrl(nerworkType);
                    webServiceHelper.setWebSite(company.getCode());
                    webServiceHelper.setWebRequestContent(strWebRequestConten);

                    //发送WebService请求,并返回结果
                    String strResponse= null;
                    try {
                        strResponse = webServiceHelper.sendWebRequest();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //获取WebService相应代码
                    Integer iResponseCode=webServiceHelper.getWebResponseCode();

                    if(iResponseCode==200){
                        String strContent = strResponse.replaceAll("&amp;quot;","\"");
                        strDetailContent = strContent.substring(strContent.indexOf("Detail",1),strContent.indexOf("/Detail",1));

                    }

                    Message message = new Message();
                    message.obj = strDetailContent;
                    detailListHandler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //更新备货完成数据
    public void sendData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String strDetailContent = "";
                    //初始化网络类型和营运据点
//                    company=new Company();
//                    Map<String,String> data=sharedHelper.readShared();
//                    nerworkType = data.get("network");
//                    userCode = data.get("userId");
//                    company.setSite(data.get("userSite"));

                    //初始化T100服务名
                    String webServiceName = "SaleRequestUpdate";

                    //设置传入请求参数
                    StringBuilder strWebRequestConten= new StringBuilder();
                    strWebRequestConten.append("&lt;Document&gt;\n"+
                            "&lt;RecordSet id=\"1\"&gt;\n"+
                            "&lt;Master name=\"xmdk_t\" node_id=\"1\"&gt;\n"+
                            "&lt;Record&gt;\n"+
                            "&lt;Field name=\"xmdksite\" value=\""+company.getCode()+"\"/&gt;\n"+
                            "&lt;Field name=\"xmdkent\" value=\"10\"/&gt;\n"+
                            "&lt;Field name=\"xmdkdocno\" value=\""+txtDetailDocno.getText()+"\"/&gt;\n"+
                            "&lt;Field name=\"xmdkud002\" value=\""+userCode+"\"/&gt;\n"+
                            "&lt;Field name=\"xmdkud003\" value=\""+strStatus+"\"/&gt;\n"+
                            "&lt;Memo/&gt;\n"+
                            "&lt;Attachment count=\"0\"/&gt;\n"+
                            "&lt;/Record&gt;\n"+
                            "&lt;/Master&gt;\n"+
                            "&lt;/RecordSet&gt;\n"+
                            "&lt;/Document&gt;\n");

                    //设置WebService参数
                    WebServiceHelper webServiceHelper=new WebServiceHelper();
                    webServiceHelper.setWebKey("16baae6c40b922d8ddb12a0320d8ea1d");
                    webServiceHelper.setWebTimestamp("20201114083106031");
                    webServiceHelper.setWebName(webServiceName);
                    webServiceHelper.setWebUrl(nerworkType);
                    webServiceHelper.setWebSite(company.getCode());
                    webServiceHelper.setWebRequestContent(strWebRequestConten);

                    //发送WebService请求,并返回结果
                    String strResponse = "";
                    try{
                        strResponse=webServiceHelper.sendWebRequest();

                        //获取WebService相应代码
                        Integer iResponseCode=webServiceHelper.getWebResponseCode();
                        Message message = new Message();
                        if(iResponseCode==200){
                            message.what = 1;
                        }else{
                            message.what = 0;
                        }
                        uHandler.sendMessage(message);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public List<Map<String,Object>> decodeJsonData(String listJson){
        List<Map<String,Object>> detailList = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson;
        String xmlIndexStr = "stocklist";
        Integer iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1) {
            String strSubContent = strDetailContent.substring(iStartId, strDetailContent.length());
            String strJson = strSubContent.substring(strSubContent.indexOf("value", 1) + 7, strSubContent.indexOf("&gt;", 1) - 2);
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode", jsonObject.getString("erpProductCode").trim());
                map.put("ProductName", jsonObject.getString("erpProductName").trim());
                map.put("ProductModels", jsonObject.getString("erpProductModels").trim());
                map.put("StockId", jsonObject.getString("erpStockId").trim());
                map.put("StockLocationId", jsonObject.getString("erpStockLocationId").trim());
                map.put("StockLocation", jsonObject.getString("erpStockLocation").trim());
                map.put("StockBatch", jsonObject.getString("erpStockBatch").trim());
                map.put("Inventory", jsonObject.getString("erpInventory").trim());
                map.put("Quantity", jsonObject.getString("erpQuantity").trim());
                map.put("QuantityPcs", jsonObject.getString("erpQuantityPcs").trim());
                map.put("PlanDate", jsonObject.getString("erpPlanDate").trim());
                map.put("Status", jsonObject.getString("erpStatus").trim());
                detailList.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Integer iCurrentStartId = strSubContent.indexOf("/Record", 1);
            Integer iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId, iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr, 1);
        }

        return detailList;
    }

    //刷新listview项
    public List<Map<String,Object>> refreshData(String qrCode){
        String[] qrCodeValue = qrCode.split("_");
        for(int i=0;i<list.size();i++){
            String erpStockLocation = (String)list.get(i).get("StockLocation");
            String erpProductCode = (String)list.get(i).get("ProductCode");
            if(erpStockLocation.equals(qrCodeValue[1]) && erpProductCode.equals(qrCodeValue[0])){
                list.get(i).put("Status","Y");
            }
        }

        return list;
    }

    //刷新当前备货箱数
    private void refreshCurrentPcs(){
        String strCurrent = "0";
        String strQuantity = "0";
        String strQuantityPcs = "0";

        int intCurrent = 0;
        int intQuantity = 0;
        int intQuantityPcs = 0;

        for(int i=0;i<list.size();i++){
            String erpStatus = (String)list.get(i).get("Status");
            String erpQuantityPcs = (String)list.get(i).get("QuantityPcs");
            if(erpQuantityPcs.equals(" ") || erpQuantityPcs.length()==0){
                erpQuantityPcs = "0";
            }

            String erpQuantity = (String)list.get(i).get("Quantity");
            if(erpQuantity.equals(" ") || erpQuantity.length()==0){
                erpQuantity = "0";
            }

            try{
                intQuantity = intQuantity + Integer.parseInt(erpQuantity);
                intQuantityPcs = intQuantityPcs + Integer.parseInt(erpQuantityPcs);
            }catch (Exception e){
                e.printStackTrace();
                intQuantity = 0;
                intQuantityPcs = 0;
            }


            if(intIndex == 1){
                if(erpStatus.equals("S")){
                    intCurrent = intCurrent + Integer.parseInt(erpQuantityPcs);
                }
            }else{
                if(erpStatus.equals("Y")){
                    intCurrent = intCurrent + Integer.parseInt(erpQuantityPcs);
                }
            }
        }

        if(intQuantityPcs == intCurrent){
//            Toast.makeText(DetailListActivity.this,"此备货单已全部检核完成",Toast.LENGTH_SHORT).show();
            MyToast.myShow(DetailListActivity.this,"此备货单已全部检核完成",1);
        }

        strCurrent = String.valueOf(intCurrent);
        strQuantity = String.valueOf(intQuantity);
        strQuantityPcs = String.valueOf(intQuantityPcs);
        txtDetailQuantityCurrent.setText(strCurrent);
        txtDetailQuantity.setText(strQuantity);
        txtDetailQuantityPcs.setText(strQuantityPcs);
    }
}