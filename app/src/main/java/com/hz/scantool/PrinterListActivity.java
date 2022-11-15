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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.PrinterListAdapter;
import com.hz.scantool.dialog.SearchView;
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

public class PrinterListActivity extends AppCompatActivity {

    private String strTitle;
    private String sMessage;

    private SearchView searchView;
    private ListView printerListView;
    private ProgressBar progressBar;
    private String statusCode,statusDescription;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private PrinterListAdapter printerListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_list);

        //初始化
        initBundle();
        initView();

        //显示打印机清单
        getPrinterListData();
    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
    }

    //初始化控件
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.printerListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        searchView = findViewById(R.id.searchView);
        printerListView = findViewById(R.id.printerListView);
        progressBar = findViewById(R.id.progressBar);

        //事件绑定
        printerListView.setOnItemClickListener(new listItemClickListener());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            TextView txtPrinterIp = view.findViewById(R.id.txtPrinterIp);
            TextView txtPrinerServer = view.findViewById(R.id.txtPrinerServer);
            String sPrinterIp = txtPrinterIp.getText().toString();
            String sPrinerServer = txtPrinerServer.getText().toString();

            if(sPrinterIp.equals("")||sPrinterIp.isEmpty()||sPrinerServer.equals("")||sPrinerServer.isEmpty()){
                MyToast.myShow(PrinterListActivity.this,"打印机和服务器IP不可为空",0,0);
            }else{
                getPrinterStatus(view,i,sPrinerServer,sPrinterIp,"N");
            }
        }
    }

    /**
    *描述: 获取打印机清单
    *日期：2022/9/6
    **/
    private void getPrinterListData(){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "StockGet";
                String strType = "6";
                String strwhere = " 1=1";

                //发送服务器请求
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
                mapResponseList = t100ServiceHelper.getT100JsonPrinterData(strResponse,"printerinfo");

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
                MyToast.myShow(PrinterListActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    printerListAdapter = new PrinterListAdapter(mapResponseList,getApplicationContext(),mUpdateClickListener);
                    printerListView.setAdapter(printerListAdapter);
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
    *描述: 查询打印机状态
    *日期：2022/9/6
    **/
    private void getPrinterStatus(View view, int i,String strServer, String strPrinter,String sRestart){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "StockGet";
                String strType = "7";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"printer\" value=\""+ strPrinter +"\"/&gt;\n"+
                        "&lt;Field name=\"server\" value=\""+ strServer +"\"/&gt;\n"+
                        "&lt;Field name=\"restart\" value=\""+ sRestart +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
//                mapResponseList = t100ServiceHelper.getT100JsonPrinterStatusData(strResponse,"printerinfo");

                e.onNext(mapResponseStatus);
//                e.onNext(mapResponseList);
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
                MyToast.myShow(PrinterListActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                progressBar.setVisibility(View.GONE);

                if(sRestart.equals("Y")){
                    MyToast.myShow(PrinterListActivity.this,"服务器正在重启，请稍后",1,0);
                }else{
                    printerListAdapter.updateData(i,view,statusDescription);
                }
            }
        });
    }

    /**
    *描述: 重启打印机
    *日期：2022/9/7
    **/
    private PrinterListAdapter.UpdateClickListener mUpdateClickListener = new PrinterListAdapter.UpdateClickListener() {
        @Override
        public void UpdateClick(int position, View view) {

            String sPrinterIp = printerListAdapter.getItemValue(position,"PrinterIp");
            String sPrinerServer = printerListAdapter.getItemValue(position,"PrinterServer");

            getPrinterStatus(view,position,sPrinerServer,sPrinterIp,"Y");
        }
    };
}