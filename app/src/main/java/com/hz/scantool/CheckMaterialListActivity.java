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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubAdapter;
import com.hz.scantool.dialog.DeviceListDialog;
import com.hz.scantool.dialog.SearchView;
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

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class CheckMaterialListActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle;
    private String statusCode;
    private String statusDescription;

    private SearchView searchView;
    private ProgressBar progressBar;
    private ListView materialListView;
    private SubAdapter subAdapter;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private List<Map<String,Object>> mSearchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_material_list);

        //初始化
        initBundle();
        initView();

        //初始化数据
        getSubListData("31");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(CheckMaterialListActivity.this);
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

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
    }

    //初始化控件
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.materialListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        progressBar = findViewById(R.id.progressBar);
        materialListView = findViewById(R.id.materialListView);

        materialListView.setOnItemClickListener(new listItemClickListener());

//        //初始化查询--零件筛选
//        searchView = (SearchView) findViewById(R.id.searchView);
//        searchView.setSearchViewListener(new SearchView.onSearchViewListener() {
//            @Override
//            public boolean onQueryTextChange(String text) {
//                searchItem("ProductName",text);
//                return false;
//            }
//        });
    }

    /**
     *描述: 查询结果解析
     *日期：2022/7/18
     **/
    public void searchItem(String name,String query) {
        List<Map<String,Object>> mSearchList2 = new ArrayList<Map<String,Object>>();
        if(mSearchList==null){
            mSearchList = mapResponseList;
        }
        for (int i = 0; i < mSearchList.size(); i++) {
            String sProductName = (String)mSearchList.get(i).get(name);
            int index = sProductName.indexOf(query);
            //存在匹配的数据
            if (index != -1) {
                mSearchList2.add(mSearchList.get(i));
            }
        }

    }

    /**
     *描述: 清单行点击事件
     *日期：2022/7/19
     **/
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            TextView txtProcess = view.findViewById(R.id.txtProcess);
            TextView txtProcessId = view.findViewById(R.id.txtProcessId);
            TextView txtDocno = view.findViewById(R.id.txtDocno);
            TextView txtSubFlag = view.findViewById(R.id.txtSubFlag);
            TextView txtSubOperateCount = view.findViewById(R.id.txtSubOperateCount);
            TextView txtVersion = view.findViewById(R.id.txtVersion);
            String sPlanSeq = txtSubOperateCount.getText().toString();
            if(sPlanSeq.equals("")||sPlanSeq.isEmpty()){
                sPlanSeq = "0";
            }
            int iPlanSeq = Integer.valueOf(sPlanSeq);
            iPlanSeq = iPlanSeq + 1;
            String sPlanSeqCurrent = String.valueOf(iPlanSeq);

            //工单号
            String sDocno = txtDocno.getText().toString();
            String sNewDocno = sDocno.replace("/",",");

            //打开上料检核界面
            Intent intent = new Intent(CheckMaterialListActivity.this,CheckMaterialActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("Docno",txtSubFlag.getText().toString());
            bundle.putString("Version",txtVersion.getText().toString());
            bundle.putString("ProcessId",txtProcessId.getText().toString());
            bundle.putString("Process",txtProcess.getText().toString());
            bundle.putString("ProductDocno",sNewDocno);
            bundle.putString("PlanSeq",sPlanSeqCurrent);
            intent.putExtras(bundle);
            startActivity(intent);

        }
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

    /**
     *描述: 扫描结果解析
     *日期：2022/7/19
     **/
    private void scanResult(String qrContent,Context context, Intent intent){

    }

    /**
     *描述: 获取需上料检核任务清单
     *日期：2022/8/18
     **/
    private void getSubListData(String strType){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductListGet";
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
                MyToast.myShow(CheckMaterialListActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    subAdapter = new SubAdapter(mapResponseList,getApplicationContext(),"CJ");
                    materialListView.setAdapter(subAdapter);
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }
}