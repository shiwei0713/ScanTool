package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.QueryProductAdapter;
import com.hz.scantool.dialog.DeviceListDialog;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class QueryProductActivity extends AppCompatActivity {

    private String strTitle;
    private String statusCode,statusDescription;
    private EditText inputQueryProduct,inputQueryProcess,inputQuerySdate,inputQueryEdate;
    private TextView inputQueryDevice,inputQueryDept,inputQueryEmployee;
    private Button btnQuery,btnCancel;
    private ListView queryDataList;

    private LoadingDialog loadingDialog;
    private QueryProductAdapter queryProductAdapter;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private List<String> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_product);

        //初始化
        initBundle();
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *描述: 初始化传入参数
     *日期：2023/1/2
     **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
    }

    /**
     *描述: 初始化控件
     *日期：2022/7/13
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.queryProductToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        inputQueryProduct = findViewById(R.id.inputQueryProduct);
        inputQueryProcess = findViewById(R.id.inputQueryProcess);
        inputQueryDevice = findViewById(R.id.inputQueryDevice);
        inputQuerySdate = findViewById(R.id.inputQuerySdate);
        inputQueryEdate = findViewById(R.id.inputQueryEdate);
        queryDataList = findViewById(R.id.queryDataList);
        inputQueryDept = findViewById(R.id.inputQueryDept);
        inputQueryEmployee = findViewById(R.id.inputQueryEmployee);
        btnQuery = findViewById(R.id.btnQuery);
        btnCancel = findViewById(R.id.btnCancel);

        //初始化日期
        inputQuerySdate.setText(setQueryDate(-1));
        inputQueryEdate.setText(setQueryDate(1));

        //定义事件
        btnQuery.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
        inputQueryDevice.setOnClickListener(new btnClickListener());
        inputQueryDept.setOnClickListener(new btnClickListener());
        inputQueryEmployee.setOnClickListener(new btnClickListener());
    }

    /**
     *描述: 按钮事件
     *日期：2022/6/12
     **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnQuery:
                    getDataListData();
                    break;
                case R.id.btnCancel:
                    inputQueryProduct.setText("");
                    inputQueryProcess.setText("");
                    inputQueryDevice.setText("");
                    inputQueryDept.setText("");
                    inputQueryEmployee.setText("");
                    break;
                case R.id.inputQueryDevice:
                    openSearchSelectDialog("3","请选择设备",inputQueryDevice);
                    break;
                case R.id.inputQueryDept:
                    openSearchSelectDialog("9","请选择部门",inputQueryDept);
                    break;
                case R.id.inputQueryEmployee:
                    openSearchSelectDialog("4","请选择人员",inputQueryEmployee);
                    break;
            }
        }
    }

    /**
     *描述: 查询日期
     *日期：2022/7/13
     **/
    private String setQueryDate(int interval){
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,interval);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date date =  calendar.getTime();
        String strDate = simpleDateFormat.format(date);

        return strDate;
    }

    /**
     *描述: 选择设备清单
     *日期：2022/7/17
     **/
//    public void openSearchSelectDialog() {
//        DeviceListDialog.Builder alert = new DeviceListDialog.Builder(QueryProductActivity.this);
//        alert.setListData(mDatas);
//        alert.setTitle("请选择设备");
//        alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
//            @Override
//            public void onSelected(String info) {
//                inputQueryDevice.setText(info);
//            }
//        });
//        DeviceListDialog mDialog = alert.show();
//        //设置Dialog 尺寸
//        mDialog.setDialogWindowAttr(0.8, 0.8, QueryProductActivity.this);
//    }

    /**
     *描述: 初始化选择数据
     *日期：2022/7/17
     **/
    private void openSearchSelectDialog(String strType,String strTitle,TextView inputQuery) {
        mDatas = new ArrayList<>();

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "StockGet";
                String strwhere = " 1=1";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"user\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
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
                    MyToast.myShow(QueryProductActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(QueryProductActivity.this,e.getMessage(),0,0);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()> 0) {
                        //显示单头数据
                        for(int i=0;i<mapResponseList.size();i++){
                            String sData;
                            if(strType.equals("3")){
                                sData = mapResponseList.get(i).get("DeviceId").toString();
                            }else{
                                //部门信息
                                sData = mapResponseList.get(i).get("DeviceId").toString()+":"+mapResponseList.get(i).get("Device").toString();
                            }

                            mDatas.add(sData);
                        }

                        //打开数据选择框
                        DeviceListDialog.Builder alert = new DeviceListDialog.Builder(QueryProductActivity.this);
                        alert.setListData(mDatas);
                        alert.setTitle(strTitle);
                        alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
                            @Override
                            public void onSelected(String info) {
                                inputQuery.setText(info);
                            }
                        });
                        DeviceListDialog mDialog = alert.show();
                        //设置Dialog 尺寸
                        mDialog.setDialogWindowAttr(0.8, 0.8, QueryProductActivity.this);
                    }
                }else{
                    MyToast.myShow(QueryProductActivity.this,statusDescription,0,0);
                }
            }
        });
    }

    /**
     *描述: 获取查询报表数据
     *日期：2023/1/2
     **/
    private void getDataListData(){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(QueryProductActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名:cwssp024
                String webServiceName = "QueryReport";
                String strType = "5";

                //查询条件
                //查询条件
                String strwhere=" sffbuc012 BETWEEN to_date('"+inputQuerySdate.getText().toString().trim()+"','YY-MM-DD') AND to_date('"+inputQueryEdate.getText().toString().trim()+"','YY-MM-DD')";
                String sProcess = inputQueryProcess.getText().toString().trim().toUpperCase();
                String sProductName = inputQueryProduct.getText().toString().trim().toUpperCase();
                String sDevice = inputQueryDevice.getText().toString().trim().toUpperCase();
                String sEmployee = inputQueryEmployee.getText().toString().trim().toUpperCase();
                String sDept = inputQueryDept.getText().toString().trim().toUpperCase();
                String strWhere1 = " 1=1";
                String strWhere2 = " 1=1";
                String strWhere3 = " 1=1";
                String strWhere4 = " 1=1";
                String strWhere5 = " 1=1";
                if(!sProductName.equals("")&&!sProductName.isEmpty()){
                    strWhere1=" imaal003 LIKE '%"+sProductName+"%'";
                }
                if(!sProcess.equals("")&&!sProcess.isEmpty()){
                    strWhere2=" sffbuc021 LIKE '%"+sProcess+"%'";
                }
                if(!sDevice.equals("")&&!sDevice.isEmpty()){
                    strWhere3=" sffbuc010 LIKE '%"+sDevice+"%'";
                }
                if(!sEmployee.equals("")&&!sEmployee.isEmpty()){
                    strWhere4=" sffe001 LIKE '%"+sEmployee+"%'";
                }
                if(!sDept.equals("")&&!sDept.isEmpty()){
                    strWhere5=" sfaa017='"+sDept+"'";
                }
                strwhere = strwhere+" AND "+strWhere1+" AND "+strWhere2+" AND "+strWhere3+" AND "+strWhere4;

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"user\" value=\""+UserInfo.getUserId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"gwhere\" value=\""+ strWhere5 +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonQueryProductReportData(strResponse,"iteminfo");

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
                    MyToast.myShow(QueryProductActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(QueryProductActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    //显示成功清单
                    queryProductAdapter = new QueryProductAdapter(mapResponseList,getApplicationContext());
                    queryDataList.setAdapter(queryProductAdapter);
                }else{
                    MyToast.myShow(QueryProductActivity.this,statusDescription,0,0);
                }

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}