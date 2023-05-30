package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubAdapter;
import com.hz.scantool.adapter.UserTaskListAdapter;
import com.hz.scantool.dialog.SearchView;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;
import com.hz.scantool.myui.MyDecoration;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

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
    private UserTaskListAdapter userTaskListAdapter;
    private RecyclerView userTaskProcessRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private TextView txtLoginout;
    private TextView txtWorktime;
    private Button subAction1,subAction2,subAction3,subAction4;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        //初始化参数
        initBundle();

        //初始化控件
        initView();
        setWorktime();
        setBtnStyle();

        //初始化清单数据
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
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubActivity.this);
                intentIntegrator.setTimeout(5000);
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

    @Override
    protected void onResume() {
        super.onResume();

        //初始化清单数据
        getSubListData();
    }

    //扫描结果解析
    private void scanResult(String qrContent, Context context, Intent intent){

    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        intIndex = bundle.getInt("index");
        strTitle = bundle.getString("title");
        strType = "8";
    }

    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        txtLoginout = findViewById(R.id.txtLoginout);
        txtWorktime = findViewById(R.id.txtWorktime);
        subAction1 = findViewById(R.id.subAction1);
        subAction2 = findViewById(R.id.subAction2);
        subAction3 = findViewById(R.id.subAction3);
        subAction4 = findViewById(R.id.subAction4);
        progressBar = findViewById(R.id.progressBar);

        //定义list控件
        linearLayoutManager = new LinearLayoutManager(this);
        userTaskProcessRecyclerView = findViewById(R.id.userTaskProcessRecyclerView);
        userTaskProcessRecyclerView.setLayoutManager(linearLayoutManager);
        userTaskProcessRecyclerView.setItemAnimator(new DefaultItemAnimator());
        userTaskProcessRecyclerView.addItemDecoration(new MyDecoration());

        txtLoginout.setText("工号:"+UserInfo.getUserId(getApplicationContext()));
        subAction1.setOnClickListener(new queryClickListener());
        subAction2.setOnClickListener(new queryClickListener());
        subAction3.setOnClickListener(new queryClickListener());
        subAction4.setOnClickListener(new queryClickListener());

        //初始化查询
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setSearchViewListener(new SearchView.onSearchViewListener() {
            @Override
            public boolean onQueryTextChange(String text) {
                searchItem("ProductName",text);
                return false;
            }
        });
    }

    /**
    *描述: 查询结果解析
    *日期：2022/7/18
    **/
    public void searchItem(String name,String query) {
        List<Map<String,Object>> mSearchList = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < mapResponseList.size(); i++) {
            String sProductName = (String)mapResponseList.get(i).get(name);
            int index = sProductName.indexOf(query);
            //存在匹配的数据
            if (index != -1) {
                mSearchList.add(mapResponseList.get(i));
            }
        }

        //填充清单
        if(mSearchList.size()>0){
//            subAdapter = new SubAdapter(mSearchList,getApplicationContext(),"CJ");
//            listView.setAdapter(subAdapter);
            showItemData();
        }

    }

    /**
    *描述: 设置导航按钮样式
    *日期：2022/6/21
    **/
    private void setBtnStyle(){
        //声明按钮ID和图片ID
        int[] btnId;
        int[] imgId;
        int[] titleId;

        //按照不同导航显示对应按钮
        btnId = new int[]{R.id.subAction1, R.id.subAction2, R.id.subAction3,R.id.subAction4};
        imgId = new int[]{R.drawable.sub_action1, R.drawable.sub_action2, R.drawable.sub_action3, R.drawable.sub_action4};
        titleId = new int[]{R.string.query_price,R.string.query_stock,R.string.query_material,R.string.query_employee};

        //初始化按钮和图片
        Button btnAction;
        Drawable drawable;

        //设置按钮样式
        for(int i=0;i<btnId.length;i++){
            btnAction=findViewById(btnId[i]);
            drawable=getResources().getDrawable(imgId[i]);
            drawable.setBounds(0,0,64,64);
            btnAction.setCompoundDrawables(drawable,null,null,null);
            btnAction.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btnAction.setBackground(getResources().getDrawable(R.drawable.button_selector_white));
            btnAction.setText(getResources().getString(titleId[i]));
        }
    }

    /**
    *描述: 获取班次
    *日期：2023-05-19
    **/
    private void setWorktime(){
        long timeCurrentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
        String currentTime = simpleDateFormat.format(timeCurrentTimeMillis);
        String strWorkTime = "晚班";

        try{
            Date date1 = simpleDateFormat.parse(currentTime);
            Date date2 = simpleDateFormat.parse("07:00:00");
            Date date3 = simpleDateFormat.parse("20:00:00");
            if(date1.getTime()>=date2.getTime() && date1.getTime()<=date3.getTime()){
                strWorkTime = "白班";
            }
        }catch (ParseException e){
            e.printStackTrace();
        }

        txtWorktime.setText(strWorkTime);
    }

    //查询报表
    private class queryClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            Intent intent;
            Bundle bundle = new Bundle();;

            switch (view.getId()){
                case R.id.subAction1:   //产量查询
                    intent = new Intent(SubActivity.this,EmployeeReportActivity.class);
                    startActivity(intent);
                    break;
                case R.id.subAction2:   //库容查询
                    intent = new Intent(SubActivity.this,QueryStockActivity.class);
                    startActivity(intent);
                    break;
                case R.id.subAction3:   //零件查询
                    intent = new Intent(SubActivity.this,SubMaterialListActivity.class);
                    bundle.putInt("btnId",120);
                    bundle.putString("title",subAction3.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                case R.id.subAction4:   //考勤查询
                    intent = new Intent(SubActivity.this,QueryClockActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }

    //获取清单
    private void getSubListData(){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductListGet";
                String strwhere = " sfaauc002='"+UserInfo.getUserId(getApplicationContext())+"'";

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
                mapResponseList = t100ServiceHelper.getT100JsonUserTaskData(strResponse,"workorder");

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
                    showItemData();
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
    *描述: 绑定RecyclerView Item数据
    *日期：2023-05-19
    **/
    private void showItemData(){
        userTaskListAdapter = new UserTaskListAdapter(mapResponseList,getApplicationContext());
        userTaskProcessRecyclerView.setAdapter(userTaskListAdapter);

        //item点击事件
        userTaskListAdapter.setOnItemClickListener(new UserTaskListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                TextView txtProcessId = itemView.findViewById(R.id.txtProcessId);    //工序项次
                TextView txtProcess = itemView.findViewById(R.id.txtProcess);   //工序号
                TextView txtPlanNo = itemView.findViewById(R.id.txtSubFlag);    //计划单号
                TextView txtConnectDocno = itemView.findViewById(R.id.txtConnectDocno);  //连线单号
                TextView txtProcessEnd = itemView.findViewById(R.id.txtProcessEnd); //是否连线
                TextView txtStationDocno = itemView.findViewById(R.id.txtStationDocno);  //组合单号
                TextView txtVersion = itemView.findViewById(R.id.txtVersion);
                TextView txtDevice = itemView.findViewById(R.id.txtDevice); //设备
                TextView txtGroupStation = itemView.findViewById(R.id.txtGroupStation); //组合
                TextView txtGroupId = itemView.findViewById(R.id.txtGroupId); //班次
                TextView txtGroup = itemView.findViewById(R.id.txtGroup); //班次

                String strPlanNo = txtPlanNo.getText().toString();
                String strVersion = txtVersion.getText().toString();
                String strProcessId = txtProcessId.getText().toString();
                String strProcess = txtProcess.getText().toString();
                String strConnectDocno = txtConnectDocno.getText().toString();
                String strStationDocno = txtStationDocno.getText().toString();
                String strProcessEnd = txtProcessEnd.getText().toString();
                String strDevice = txtDevice.getText().toString();
                String strGroupStation = txtGroupStation.getText().toString();
                String strGroupId = txtGroupId.getText().toString();
                String strGroup = txtGroup.getText().toString();
                String strOperateCount = userTaskListAdapter.getItemValue(position,"OperateCount");
                String strPrintCount = userTaskListAdapter.getItemValue(position,"PrintCount");
                String strDocType = userTaskListAdapter.getItemValue(position,"DocType");

                showTaskDetail(strPlanNo,strVersion,strProcessId,strProcess,strConnectDocno,strStationDocno,strProcessEnd,strDevice,strOperateCount,strPrintCount,strGroupStation,strGroupId,strGroup,strDocType);
            }
        });
    }

    /**
    *描述: 显示任务细节数据
    *日期：2023-05-19
    **/
    private void showTaskDetail(String strPlanNo,String strVersion,String strProcessId,String strProcess,String strConnectDocno,String strStationDocno,String strProcessEnd,String strDevice,String strOperateCount,String strPrintCount,String strGroupStation,String strGroupId,String strGroup,String strDocType){
        Intent intent = new Intent(SubActivity.this,SubDetailForMultipleActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("PlanNo",strPlanNo);
        bundle.putString("Version",strVersion);
        bundle.putString("ProcessId",strProcessId);
        bundle.putString("Process",strProcess);
        bundle.putString("ConnectDocno",strConnectDocno);
        bundle.putString("StationDocno",strStationDocno);
        bundle.putString("ProcessEnd",strProcessEnd);
        bundle.putString("Device",strDevice);
        bundle.putString("GroupStation",strGroupStation);
        bundle.putString("GroupId",strGroupId);
        bundle.putString("Group",strGroup);
        bundle.putString("OperateCount",strOperateCount);
        bundle.putString("PrintCount",strPrintCount);
        bundle.putString("DocType",strDocType);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}