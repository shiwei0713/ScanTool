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

import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.QueryDataAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class QueryDataActivity extends AppCompatActivity {

    private String strTitle;
    private String statusCode,statusDescription;
    private EditText inputQueryProduct,inputQueryProcess;
    private Button btnQuery,btnCancel;
    private Button btnFlag1,btnFlag2,btnFlag3;
    private ListView queryDataList;

    private LoadingDialog loadingDialog;
    private QueryDataAdapter queryDataAdapter;
    private List<Map<String,Object>> mapResponseList,mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_data);

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
        Toolbar toolbar=findViewById(R.id.queryDataToolBar);
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
        queryDataList = findViewById(R.id.queryDataList);
        btnQuery = findViewById(R.id.btnQuery);
        btnCancel = findViewById(R.id.btnCancel);
        btnFlag1 = findViewById(R.id.btnFlag1);
        btnFlag2 = findViewById(R.id.btnFlag2);
        btnFlag3 = findViewById(R.id.btnFlag3);

        //设置状态
        setFlag("1");

        //定义事件
        btnQuery.setOnClickListener(new btnClickListener());
        btnCancel.setOnClickListener(new btnClickListener());
        btnFlag1.setOnClickListener(new btnClickListener());
        btnFlag2.setOnClickListener(new btnClickListener());
        btnFlag3.setOnClickListener(new btnClickListener());
    }

    /**
    *描述: 设置状态
    *日期：2023/4/4
    **/
    private void setFlag(String strFlag){
        if(strFlag.equals("1")){
            btnFlag1.setSelected(true);
            btnFlag2.setSelected(false);
            btnFlag3.setSelected(false);
        }else if(strFlag.equals("2")){
            btnFlag1.setSelected(false);
            btnFlag2.setSelected(true);
            btnFlag3.setSelected(false);
        }else if(strFlag.equals("3")){
            btnFlag1.setSelected(false);
            btnFlag2.setSelected(false);
            btnFlag3.setSelected(true);
        }
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
                    setFlag("1");
                    getDataListData("1");
                    break;
                case R.id.btnCancel:
                    inputQueryProduct.setText("");
                    inputQueryProcess.setText("");
                    break;
                case R.id.btnFlag1:
                    setFlag("1");
                    getDataListData("1");
                    break;
                case R.id.btnFlag2:
                    setFlag("2");
                    getDataListData("2");
                    break;
                case R.id.btnFlag3:
                    setFlag("3");
                    getDataListData("3");
                    break;
            }
        }
    }

    /**
     *描述: 获取查询报表数据
     *日期：2023/1/2
     **/
    private void getDataListData(String strFlag){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(QueryDataActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名:cwssp024
                String webServiceName = "QueryReport";
                String strType = "3";

                //查询条件
                String strwhere = " 1=1";
                String sProcess = inputQueryProcess.getText().toString().trim().toUpperCase();
                String sProductName = inputQueryProduct.getText().toString().trim().toUpperCase();
                if(!sProcess.equals("")&&!sProcess.isEmpty()&&!sProductName.equals("")&&!sProductName.isEmpty()){
                    strwhere=" sfecuc018 LIKE '%"+sProcess+"%' AND imaal003 LIKE '%"+sProductName+"%'";
                }else{
                    if(!sProcess.equals("")&&!sProcess.isEmpty()){
                        strwhere=" sfecuc018 LIKE '%"+sProcess+"%'";
                    }else if(!sProductName.equals("")&&!sProductName.isEmpty()){
                        strwhere=" imaal003 LIKE '%"+sProductName+"%'";
                    }
                }

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"user\" value=\""+UserInfo.getUserId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"gwhere\" value=\""+ strFlag +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonQueryProductAreaData(strResponse,"iteminfo");

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
                    MyToast.myShow(QueryDataActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(QueryDataActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
                loadingDialog = null;
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    //显示成功清单
                    queryDataAdapter = new QueryDataAdapter(mapResponseList,getApplicationContext());
                    queryDataList.setAdapter(queryDataAdapter);
                }else{
                    MyToast.myShow(QueryDataActivity.this,statusDescription,0,0);
                }

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}