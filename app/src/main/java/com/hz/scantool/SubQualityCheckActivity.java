package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubAdapter;
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

public class SubQualityCheckActivity extends AppCompatActivity {

    private String strTitle;
    private int intIndex;
    private String statusCode;
    private String statusDescription;

    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private LoadingDialog loadingDialog;
    private ListView listView;
    private SubAdapter subAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_quality_check);

        //初始化参数
        initBundle();
        initView();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subQualityCheckToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化清单数据
        getSubListData("12");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu_refresh,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubQualityCheckActivity.this);
                intentIntegrator.setTimeout(5000);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //开始扫描
                intentIntegrator.initiateScan();
                break;
            case R.id.action_refresh:
                //刷新数据
                getSubListData("12");
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
        intIndex = bundle.getInt("index");
        strTitle = bundle.getString("title");
    }

    private void initView(){
        listView = findViewById(R.id.subQualityCheckView);

        listView.setOnItemClickListener(new listItemClickListener());
    }

    //行单击事件
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            TextView txtSubStatus = view.findViewById(R.id.txtSubStatus);
            String strSubStatus = txtSubStatus.getText().toString();

            if(strSubStatus.equals("PC")){
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

                Intent intent = new Intent(SubQualityCheckActivity.this,SubQualityCheckDetailActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("ProductName",txtProductName.getText().toString());
                bundle.putString("PlanDate",txtPlanDate.getText().toString());
                bundle.putString("ProductCode",txtProductCode.getText().toString());
                bundle.putString("ProductModels",txtProductModels.getText().toString());
                bundle.putString("ProcessId",txtProcessId.getText().toString());
                bundle.putString("Process",txtProcess.getText().toString());
                bundle.putString("Device",txtDevice.getText().toString());
                bundle.putString("Docno",txtDocno.getText().toString());
                bundle.putString("Quantity",txtQuantity.getText().toString());
                bundle.putString("Employee",txtEmployee.getText().toString());
                bundle.putString("Lots",txtLots.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    //获取清单
    private void getSubListData(String strType){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(SubQualityCheckActivity.this,"正在刷新",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductListGet";
                String strwhere = " sffyuc004 ='F'";

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
                mapResponseList = t100ServiceHelper.getT100JsonPqcData(strResponse,"workorder");

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
                            MyToast.myShow(SubQualityCheckActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(SubQualityCheckActivity.this,"无检验数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubQualityCheckActivity.this,"网络错误",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                subAdapter = new SubAdapter(mapResponseList,getApplicationContext(),mUpdateClickListener,"QC");
                listView.setAdapter(subAdapter);

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }

    //首检合格
    private SubAdapter.UpdateClickListener mUpdateClickListener = new SubAdapter.UpdateClickListener() {
        @Override
        public void UpdateClick(int position, View view) {
            //显示进度条
            loadingDialog = new LoadingDialog(SubQualityCheckActivity.this,"数据提交中",R.drawable.dialog_loading);
            loadingDialog.show();

            Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
                @Override
                public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                    //初始化T100服务名
                    String webServiceName = "WorkReportRequestGen";
                    String qcstatus = "K";
                    String action = "check";

                    String strPlanDate = subAdapter.getItemValue(position,"PlanDate");
                    String strDocno = subAdapter.getItemValue(position,"Docno");
                    String strProcessId = subAdapter.getItemValue(position,"ProcessId");
                    String strDevice = subAdapter.getItemValue(position,"Device");
                    String strProductCode = subAdapter.getItemValue(position,"ProductCode");
                    String strProcess = subAdapter.getItemValue(position,"Process");
                    String strLots = subAdapter.getItemValue(position,"Lots");
                    String strFlag = subAdapter.getItemValue(position,"Flag");
                    String strSeq = subAdapter.getItemValue(position,"OperateCount");

                    //发送服务器请求
                    T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                    String requestBody = "&lt;Document&gt;\n"+
                            "&lt;RecordSet id=\"1\"&gt;\n"+
                            "&lt;Master name=\"sffb_t\" node_id=\"1\"&gt;\n"+
                            "&lt;Record&gt;\n"+
                            "&lt;Field name=\"sffbsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                            "&lt;Field name=\"sffbent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                            "&lt;Field name=\"sffbdocdt\" value=\""+strPlanDate+"\"/&gt;\n"+
                            "&lt;Field name=\"sffb002\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                            "&lt;Field name=\"sffb005\" value=\""+ strDocno +"\"/&gt;\n"+  //工单单号
                            "&lt;Field name=\"sffbseq\" value=\""+ strProcessId +"\"/&gt;\n"+  //工艺项次
                            "&lt;Field name=\"sffb010\" value=\""+ strDevice +"\"/&gt;\n"+  //机器编号
                            "&lt;Field name=\"sffb029\" value=\""+ strProductCode +"\"/&gt;\n"+  //报工料号
                            "&lt;Field name=\"process\" value=\""+ strProcess +"\"/&gt;\n"+  //工序
                            "&lt;Field name=\"lots\" value=\""+ strLots +"\"/&gt;\n"+  //批次
                            "&lt;Field name=\"qcstatus\" value=\""+ qcstatus +"\"/&gt;\n"+  //首检状态
                            "&lt;Field name=\"planno\" value=\""+ strFlag +"\"/&gt;\n"+  //计划单号
                            "&lt;Field name=\"planseq\" value=\""+ strSeq +"\"/&gt;\n"+  //报工次数
                            "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //执行动作
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
                    mapResponseList = t100ServiceHelper.getT100ResponseDocno(strResponse,"docno");

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
                        MyToast.myShow(SubQualityCheckActivity.this,"执行接口错误",2,0);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    MyToast.myShow(SubQualityCheckActivity.this,"网络错误",0,0);
                    loadingDialog.dismiss();
                }

                @Override
                public void onComplete() {
                    if(statusCode.equals("0")){
                        MyToast.myShow(SubQualityCheckActivity.this, statusDescription, 1, 1);
                    }else{
                        MyToast.myShow(SubQualityCheckActivity.this, statusDescription, 0, 1);
                    }
                    getSubListData("12");
                }
            });
        }
    };
}