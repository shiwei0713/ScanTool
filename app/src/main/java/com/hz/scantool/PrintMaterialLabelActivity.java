package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.PrintMaterialLabelListAdapter;
import com.hz.scantool.dialog.DeviceListDialog;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PrintMaterialLabelActivity extends AppCompatActivity {

    private String strTitle;
    private int intIndex;

    private EditText queryProduct,printMaterialLabelModQuantity;
    private TextView inputPrintDevice,printMaterialLabelMaterialCode,printMaterialLabelMaterialName,printMaterialLabelMaterialModel,printMaterialLabelProductCode;
    private TextView printMaterialLabelProductName,printMaterialLabelProductModels,printMaterialLabelWeight,printMaterialLabelQPA,printMaterialLabelMaterialSize;
    private Button btnQuery,btnClear,btnHide,btnPrint,btnCancel;
    private ListView printMaterialLabelList;
    private LinearLayout viewBasic;

    private LoadingDialog loadingDialog;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private List<String> mDatas;
    private String statusCode,statusDescription;
    private PrintMaterialLabelListAdapter printMaterialLabelListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_material_label);

        //初始化
        initBundle();
        initView();
    }

    /**
     *描述: 工具栏菜单事件
     *日期：2022/5/25
     **/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏返回按钮事件定义
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *描述: 获取传入参数值
     *日期：2022/6/6
     **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        intIndex = bundle.getInt("btnId");
    }

    /**
     *描述: 初始化控件
     *日期：2022/6/6
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.printMaterialLabelToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        queryProduct = findViewById(R.id.queryProduct);
        printMaterialLabelModQuantity = findViewById(R.id.printMaterialLabelModQuantity);
        inputPrintDevice = findViewById(R.id.inputPrintDevice);
        printMaterialLabelMaterialCode = findViewById(R.id.printMaterialLabelMaterialCode);
        printMaterialLabelMaterialName = findViewById(R.id.printMaterialLabelMaterialName);
        printMaterialLabelMaterialModel = findViewById(R.id.printMaterialLabelMaterialModel);
        printMaterialLabelProductCode = findViewById(R.id.printMaterialLabelProductCode);
        printMaterialLabelProductName = findViewById(R.id.printMaterialLabelProductName);
        printMaterialLabelProductModels = findViewById(R.id.printMaterialLabelProductModels);
        printMaterialLabelWeight = findViewById(R.id.printMaterialLabelWeight);
        printMaterialLabelQPA = findViewById(R.id.printMaterialLabelQPA);
        printMaterialLabelMaterialSize = findViewById(R.id.printMaterialLabelMaterialSize);
        btnQuery = findViewById(R.id.btnQuery);
        btnClear = findViewById(R.id.btnClear);
        btnHide = findViewById(R.id.btnHide);
        btnPrint = findViewById(R.id.btnPrint);
        btnCancel = findViewById(R.id.btnCancel);
        printMaterialLabelList = findViewById(R.id.printMaterialLabelList);
        viewBasic = findViewById(R.id.viewBasic);

        //绑定事件
        btnQuery.setOnClickListener(new btnClickListener());
        btnClear.setOnClickListener(new btnClickListener());
        btnHide.setOnClickListener(new btnClickListener());
        btnPrint.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
        inputPrintDevice.setOnClickListener(new btnClickListener());
        printMaterialLabelList.setOnItemClickListener(new listItemClickListener());

        //初始化设备清单
        initData();

    }

    /**
     *描述: 标签列表行单击
     *日期：2022/6/15
     **/
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            TextView txtLabelMaterialCode = view.findViewById(R.id.listMaterialLabelMaterialCode);
            TextView txtLabelMaterialName = view.findViewById(R.id.listMaterialLabelMaterialName);
            TextView txtLabelMaterialModel = view.findViewById(R.id.listMaterialLabelMaterialModel);
            TextView txtLabelMaterialSize = view.findViewById(R.id.listMaterialLabelMaterialSize);
            TextView txtLabelProductCode = view.findViewById(R.id.listMaterialLabelProductCode);
            TextView txtLabelProductName = view.findViewById(R.id.listMaterialLabelProductName);
            TextView txtLabelProductModels = view.findViewById(R.id.listMaterialLabelProductModel);
            TextView txtLabelQPA = view.findViewById(R.id.listMaterialLabelQpa);

            printMaterialLabelMaterialCode.setText(txtLabelMaterialCode.getText().toString());
            printMaterialLabelMaterialName.setText(txtLabelMaterialName.getText().toString());
            printMaterialLabelMaterialModel.setText(txtLabelMaterialModel.getText().toString());
            printMaterialLabelProductCode.setText(txtLabelProductCode.getText().toString());
            printMaterialLabelProductName.setText(txtLabelProductName.getText().toString());
            printMaterialLabelProductModels.setText(txtLabelProductModels.getText().toString());
            printMaterialLabelQPA.setText(txtLabelQPA.getText().toString());
            printMaterialLabelMaterialSize.setText(txtLabelMaterialSize.getText().toString());

            viewBasic.setVisibility(View.VISIBLE);
        }
    }

    /**
     *描述: 按钮事件实现
     *日期：2022/6/15
     **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnQuery:
                    String sProduct = queryProduct.getText().toString().trim().toUpperCase();
                    if(!sProduct.equals("")&&!sProduct.isEmpty()){
                        getMaterialBomData(sProduct);
                    }else{
                        MyToast.myShow(PrintMaterialLabelActivity.this,"请输入零件号再查询",0,0);
                    }
                    break;
                case R.id.inputPrintDevice:
                    openSearchSelectDialog();
                    break;
                case R.id.btnHide:
                    viewBasic.setVisibility(View.GONE);
                    break;
                case R.id.btnPrint:
                    String sDevice = inputPrintDevice.getText().toString();
                    if(!sDevice.equals("")&&!sDevice.isEmpty()){
                        checkQty();
                    }else{
                        MyToast.myShow(PrintMaterialLabelActivity.this,"设备编号不可为空",0,0);
                    }
                    break;
                case R.id.btnClear:
                    queryProduct.setText("");
                    break;
                case R.id.btnCancel:
                    finish();
                    break;
            }
        }
    }

    /**
     *描述: 选择设备清单
     *日期：2022/7/17
     **/
    public void openSearchSelectDialog() {
        DeviceListDialog.Builder alert = new DeviceListDialog.Builder(PrintMaterialLabelActivity.this);
        alert.setListData(mDatas);
        alert.setTitle("请选择设备");
        alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
            @Override
            public void onSelected(String info) {
                inputPrintDevice.setText(info);
            }
        });
        DeviceListDialog mDialog = alert.show();
        //设置Dialog 尺寸
        mDialog.setDialogWindowAttr(0.8, 0.8, PrintMaterialLabelActivity.this);
    }

    /**
     *描述: 初始化选择数据
     *日期：2022/7/17
     **/
    private void initData() {
        mDatas = new ArrayList<>();

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "StockGet";
                String strType = "3";
                String strwhere = " 1=1";

                //发送服务器请求
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
                mapResponseList = t100ServiceHelper.getT100JsonDeviceData(strResponse,"stockinfo");

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
                    MyToast.myShow(PrintMaterialLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(PrintMaterialLabelActivity.this,e.getMessage(),0,0);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()> 0) {
                        //显示单头数据
                        for(int i=0;i<mapResponseList.size();i++){
                            mDatas.add(mapResponseList.get(i).get("DeviceId").toString());
                        }
                    }
                }else{
                    MyToast.myShow(PrintMaterialLabelActivity.this,statusDescription,0,0);
                }
            }
        });
    }

    /**
     *描述: 获取材料BOM信息
     *日期：2022/6/15
     **/
    private void getMaterialBomData(String sProduct){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(PrintMaterialLabelActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "ItemInfoGet";
                String strType = "12";
                String strwhere = " t2.imaal003 LIKE '%"+sProduct+"%'";
                String qrcode = "";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+ qrcode +"\"/&gt;\n"+
                        "&lt;Field name=\"user\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonItemMaterialBomData(strResponse,"iteminfo");

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
                    MyToast.myShow(PrintMaterialLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(PrintMaterialLabelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()> 0) {
                        //显示单头数据
                        printMaterialLabelMaterialCode.setText(mapResponseList.get(0).get("MaterialCode").toString());
                        printMaterialLabelMaterialName.setText(mapResponseList.get(0).get("MaterialName").toString());
                        printMaterialLabelMaterialModel.setText(mapResponseList.get(0).get("MaterialModels").toString());
                        printMaterialLabelMaterialSize.setText(mapResponseList.get(0).get("MaterialSize").toString());
                        printMaterialLabelProductCode.setText(mapResponseList.get(0).get("ProductCode").toString());
                        printMaterialLabelProductName.setText(mapResponseList.get(0).get("ProductName").toString());
                        printMaterialLabelProductModels.setText(mapResponseList.get(0).get("ProductModels").toString());
                        printMaterialLabelQPA.setText(mapResponseList.get(0).get("Qpa").toString());

                        //显示清单
                        printMaterialLabelListAdapter = new PrintMaterialLabelListAdapter(mapResponseList,getApplicationContext());
                        printMaterialLabelList.setAdapter(printMaterialLabelListAdapter);
                    }else{
                        MyToast.myShow(PrintMaterialLabelActivity.this,"无数据",0,0);
                    }
                }else{
                    MyToast.myShow(PrintMaterialLabelActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    /**
    *描述: 核对数量并插入打印数据
    *日期：2023/2/27
    **/
    private void checkQty(){
        //标签数量
        String sQuantity = printMaterialLabelModQuantity.getText().toString();
        String sQpa = printMaterialLabelQPA.getText().toString();
        float fWeight;
        if(!sQuantity.equals("")&&!sQuantity.isEmpty()){
            if(!sQpa.equals("")&&!sQpa.isEmpty()){
                try{
                    float fQuantity = Float.valueOf(sQuantity);  //零件个数
                    float fQpa = Float.valueOf(sQpa);
                    fWeight = fQuantity * fQpa;     //材料重量

                    //插入标签数据
                    if(fQuantity>0 && fWeight>0){
                        updateQrcodeData(fWeight,fQuantity);
                    }else{
                        MyToast.myShow(PrintMaterialLabelActivity.this,"数量必须大于0",2,0);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }else{
                MyToast.myShow(PrintMaterialLabelActivity.this,"消耗定额(QPA)不可为空",2,0);
            }
        }else{
            MyToast.myShow(PrintMaterialLabelActivity.this,"零件个数不可为空",2,0);
        }
    }

    /**
     *描述: 打印标签
     *日期：2022/6/15
     **/
    private void updateQrcodeData(float fWeight,float fQty){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(PrintMaterialLabelActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "RepeatPrintLabel";
                String sAction = "material";
                String sDevices = inputPrintDevice.getText().toString().toUpperCase();

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcaa_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcaasite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaaent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcaamodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"bcaa002\" value=\""+ printMaterialLabelMaterialCode.getText().toString() +"\"/&gt;\n"+  //料件编号
                        "&lt;Field name=\"bcaa009\" value=\""+ fWeight +"\"/&gt;\n"+  //条码数量
                        "&lt;Field name=\"bcaaud004\" value=\""+ sDevices +"\"/&gt;\n"+  //设备编号
                        "&lt;Field name=\"bcaaua011\" value=\""+ printMaterialLabelProductCode.getText().toString()+"\"/&gt;\n"+  //库存管理特征
                        "&lt;Field name=\"bcaaua012\" value=\""+ fQty +"\"/&gt;\n"+  //参考数量
                        "&lt;Field name=\"bcaa008\" value=\""+ printMaterialLabelMaterialSize.getText().toString()+"\"/&gt;\n"+  //参考数量
                        "&lt;Field name=\"act\" value=\""+ sAction +"\"/&gt;\n"+  //操作
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
                mapResponseList = t100ServiceHelper.getT100RepeatPrintData(strResponse,"responsedata");

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
                    MyToast.myShow(PrintMaterialLabelActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(PrintMaterialLabelActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(PrintMaterialLabelActivity.this,statusDescription,1,0);
                }else{
                    MyToast.myShow(PrintMaterialLabelActivity.this,statusDescription,0,0);
                }
                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}