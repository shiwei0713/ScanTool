package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SendTaskListAdapter;
import com.hz.scantool.adapter.SubAdapter;
import com.hz.scantool.dialog.DeviceListDialog;
import com.hz.scantool.dialog.LoadListView;
import com.hz.scantool.dialog.SearchView;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import org.w3c.dom.Text;

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

public class SubDetailForTaskActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";
    private static final int ROWS = 30;

    private String strTitle,strMessage;
    private String statusCode;
    private String statusDescription;
    private String strWhere,strWhereProduct,strFlag,strRestart;
    private boolean isLoadMore;
    private int iRows,iEveryRow,iCount;
    private int iStartCount,iEndCount;

    private Button btnFlag1,btnFlag2,btnFlag3,btnQuery,btnClear;
    private ProgressBar progressBar;
    private LoadListView subTaskLoadView;
    private SendTaskListAdapter sendTaskListAdapter;
    private EditText inputDevice,inputProductName;
    private CheckBox checkBoxThree,checkBoxSeven;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_for_task);

        //初始化
        initBundle();
        initRows();
        initView();

        //初始化数据
        strFlag = "N";
        setWhereCondition("N");
        getSubListData("11",strWhere,strFlag);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSubListData("11",strWhere,strFlag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu_for_task,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_print:
                Intent intent = new Intent(SubDetailForTaskActivity.this,SurplusMaterialListActivity.class);
                startActivity(intent);
                break;
            case R.id.action_closetask:
                AlertDialog.Builder builder = new AlertDialog.Builder(SubDetailForTaskActivity.this);
                builder.setMessage("是否确认中止所有任务");
                builder.setTitle("提示");
                builder.setIcon(R.drawable.dialog_error);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateTaskData(0,"closetask","C");
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();

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

    /**
    *描述: 初始化显示笔数
    *日期：2022/10/20
    **/
    private void initRows(){
        iRows = ROWS;  //每次加载30行数据
        iEveryRow = ROWS; //每次加载30行数据
        iCount = 0 ; //当前笔数
        isLoadMore = true; // 是否加载更多
    }

    //初始化控件
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.taskToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        inputDevice = findViewById(R.id.inputDevice);
        inputProductName = findViewById(R.id.inputProductName);
        btnFlag1 = findViewById(R.id.btnFlag1);
        btnFlag2 = findViewById(R.id.btnFlag2);
        btnFlag3 = findViewById(R.id.btnFlag3);
        progressBar = findViewById(R.id.progressBar);
        subTaskLoadView = findViewById(R.id.subTaskLoadView);
        btnQuery = findViewById(R.id.btnQuery);
        btnClear = findViewById(R.id.btnClear);
        checkBoxThree = findViewById(R.id.checkBoxThree);
        checkBoxSeven = findViewById(R.id.checkBoxSeven);

        //定义事件
        btnFlag1.setOnClickListener(new commandClickListener());
        btnFlag2.setOnClickListener(new commandClickListener());
        btnFlag3.setOnClickListener(new commandClickListener());
        btnQuery.setOnClickListener(new commandClickListener());
        btnClear.setOnClickListener(new commandClickListener());
        checkBoxThree.setOnCheckedChangeListener(new checkClickListener());
        checkBoxSeven.setOnCheckedChangeListener(new checkClickListener());

        //加载更多
        subTaskLoadView.setOnItemClickListener(new listItemClickListener());
        subTaskLoadView.setLoadMoreListener(new loadMoreItemListener());

        //初始化按钮选中状态
        btnFlag1.setSelected(true);
        btnFlag2.setSelected(false);
        btnFlag3.setSelected(false);

        //初始化显示天数
        iStartCount = -1;
        iEndCount = 1;
    }

    /**
    *描述: 日期天数选择事件
    *日期：2022/10/27
    **/
    private class checkClickListener implements CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            CheckBox checkBox = (CheckBox)compoundButton;
            if(b){
                if(checkBox==checkBoxThree){
                    iStartCount = -1;
                    iEndCount = 1;
                    checkBoxSeven.setChecked(false);
                }else{
                    iStartCount = -5;
                    iEndCount = 1;
                    checkBoxThree.setChecked(false);
                }
            }
        }
    }

    /**
    *描述: 加载更多
    *日期：2022/9/21
    **/
    private class loadMoreItemListener implements LoadListView.OnLoadMoreListener{

        @Override
        public void LoadMore() {
            sendTaskListAdapter.notifyDataSetChanged();

            if(isLoadMore){
                getSubListData("11",strWhere,strFlag);
            }else{
                MyToast.myShow(SubDetailForTaskActivity.this,"无更多数据加载",2,0);
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
            //获取list item控件
            TextView txtProductCode = view.findViewById(R.id.txtProductCode);
            TextView txtProductName = view.findViewById(R.id.txtProductName);
            TextView txtProductModels = view.findViewById(R.id.txtProductModels);
            TextView txtProcessId = view.findViewById(R.id.txtProcessId);
            TextView txtProcess = view.findViewById(R.id.txtProcess);
            TextView txtDevice = view.findViewById(R.id.txtDevice);
            TextView txtDocno = view.findViewById(R.id.txtDocno);
            TextView txtEmployee = view.findViewById(R.id.txtEmployee);
            TextView txtVersion = view.findViewById(R.id.txtVersion);
            TextView txtSubStatus = view.findViewById(R.id.txtSubStatus);
            TextView txtPlanDate = view.findViewById(R.id.txtPlanDate);
            TextView txtGroupId = view.findViewById(R.id.txtGroupId);
            TextView txtGroup = view.findViewById(R.id.txtGroup);
            TextView txtProcessEnd = view.findViewById(R.id.txtProcessEnd);
            TextView txtSubFlag = view.findViewById(R.id.txtSubFlag);
            TextView txtGroupStation = view.findViewById(R.id.txtGroupStation);
            TextView txtQuantity = view.findViewById(R.id.txtQuantity);
            TextView txtConnectDocno = view.findViewById(R.id.txtConnectDocno);

            //获取值
            String strProductCode = txtProductCode.getText().toString();
            String sStatus = txtSubStatus.getText().toString();
            String sConnect = txtProcessEnd.getText().toString();
            String sStationDocno = sendTaskListAdapter.getItemValue(i,"StationDocno");
            String strProductName = txtProductName.getText().toString();
            String strProductModels = txtProductModels.getText().toString();
            String strProcessId = txtProcessId.getText().toString();
            String strProcess = txtProcess.getText().toString();
            String strPlanDocno = txtSubFlag.getText().toString();
            String strVersion = txtVersion.getText().toString();
            String strPlanDate = txtPlanDate.getText().toString();
            String strGroupId = txtGroupId.getText().toString();
            String strGroup = txtGroup.getText().toString();
            String strQuantity = txtQuantity.getText().toString();
            String strEmployee = txtEmployee.getText().toString();
            String strDevice = txtDevice.getText().toString();
            String strDocno = txtDocno.getText().toString();
            String strConnectDocno = txtConnectDocno.getText().toString();
            String strErrorMsg = sendTaskListAdapter.getItemValue(i,"ErrorMsg");
            String strLabel = sendTaskListAdapter.getItemValue(i,"Label");

            if(sConnect.equals("Y")){
                showConnectTask(strProductName,strProductCode,strProductModels,strProcessId,strProcess,strDevice,strDocno,strVersion,strFlag,strPlanDate,strGroupId,strGroup,sConnect,strConnectDocno,strErrorMsg,strLabel);
            }else{
                //连线生产禁止直接启用
                if(sStationDocno.equals("")||sStationDocno.isEmpty()){
                    showSetTask(strProductName,strProductModels,strProcessId,strProcess,strPlanDocno,strVersion,strPlanDate,strGroupId,strGroup,strQuantity,strEmployee,strDevice,strDocno,strErrorMsg,strLabel);
                }else{
                    showSetStation(sStationDocno,txtGroupStation.getText().toString(),txtDevice.getText().toString(),txtVersion.getText().toString(),strErrorMsg,strLabel);
                }
            }
        }
    }

    /**
    *描述: 筛选条件
    *日期：2022/8/24
    **/
    private void setWhereCondition(String status){
        //筛选类别
        strFlag = status;

        //筛选设备和零件号
        String sDevice = inputDevice.getText().toString();
        String sProductName = inputProductName.getText().toString();
        String sWhere1 = " AND 1=1";
        String sWhere2 = " 1=1";

        //设备
        if(!sDevice.equals("")&&!sDevice.isEmpty()){
            sWhere1 = " AND sfaauc009 LIKE '%"+sDevice+"%'";
        }

        //零件
        if(!sProductName.equals("")&&!sProductName.isEmpty()){
            sWhere2 = " imaal003 LIKE '%"+sProductName+"%'";
        }

        strWhere = "sfaaucstus='"+status+"'"+sWhere1;
        strWhereProduct = sWhere2;
    }

    /**
    *描述: 按钮事件实现
    *日期：2022/7/19
    **/
    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnFlag1:  //未启用
                    initRows();
                    initList();
                    btnFlag1.setSelected(true);
                    btnFlag2.setSelected(false);
                    btnFlag3.setSelected(false);
                    setWhereCondition("N");
                    getSubListData("11",strWhere,"N");
                    break;
                case R.id.btnFlag2: //已启用
                    initRows();
                    initList();
                    btnFlag1.setSelected(false);
                    btnFlag2.setSelected(true);
                    btnFlag3.setSelected(false);
                    setWhereCondition("Y");
                    getSubListData("11",strWhere,"Y");
                    break;
                case R.id.btnFlag3: //已中止
                    initRows();
                    initList();
                    btnFlag1.setSelected(false);
                    btnFlag2.setSelected(false);
                    btnFlag3.setSelected(true);
                    setWhereCondition("C");
                    getSubListData("11",strWhere,"C");
                    break;
                case R.id.btnQuery:  //查询
                    initRows();
                    setWhereCondition(strFlag);
                    getSubListData("11",strWhere,strFlag);
                    break;
                case R.id.btnClear:  //清除
                    initRows();
                    inputDevice.setText("");
                    inputProductName.setText("");
                    setWhereCondition(strFlag);
                    getSubListData("11",strWhere,strFlag);
                    break;
            }
        }
    }

    private void initList(){
        if(mapResponseList!=null){
            mapResponseList.clear();
        }
    }

    /**
    *描述: 显示连线任务设置
    *日期：2023-05-18
    **/
    private void showConnectTask(String strProductName,String strProductCode,String strProductModels,String strProcessId,String strProcess,String strDevice,String strDocno,String strVersion,String strFlag,String strPlanDate,String strGroupId,String strGroup,String strProcessEnd,String strConnectDocno,String strErrorMsg,String strLabel){

        Intent intent = new Intent(SubDetailForTaskActivity.this,SetProcessActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("productName",strProductName);
        bundle.putString("productCode",strProductCode);
        bundle.putString("productModels",strProductModels);
        bundle.putString("processId",strProcessId);
        bundle.putString("process",strProcess);
        bundle.putString("device",strDevice);
        bundle.putString("docno",strDocno);
        bundle.putString("version",strVersion);
        bundle.putString("menuitem",strFlag);
        bundle.putString("plandate",strPlanDate);
        bundle.putString("groupid",strGroupId);
        bundle.putString("group",strGroup);
        bundle.putString("connect",strProcessEnd);
        bundle.putString("connectDocno",strConnectDocno);
        bundle.putString("errorMsg",strErrorMsg);
        bundle.putString("label",strLabel);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
    *描述: 显示组合任务设置
    *日期：2023-05-11
    **/
    private void showSetStation(String stationDocno,String station,String device,String version,String strErrorMsg,String strLabel){
        Intent intent = new Intent(SubDetailForTaskActivity.this,SetStationActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("station",station);
        bundle.putString("stationDocno",stationDocno);
        bundle.putString("device",device);
        bundle.putString("version",version);
        bundle.putString("menuitem",strFlag);
        bundle.putString("errorMsg",strErrorMsg);
        bundle.putString("label",strLabel);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
    *描述: 显示一般任务设置
    *日期：2023-05-17
    **/
    private void showSetTask(String strProductName,String strProductModels,String strProcessId,String strProcess,String strPlanDocno,String strVersion,String strPlanDate,String strGroupId,String strGroup,String strQuantity,String strEmployee,String strDevice,String strDocno,String strErrorMsg,String strLabel){
        Intent intent = new Intent(SubDetailForTaskActivity.this,SetTaskActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("menuitem",strFlag);
        bundle.putString("ProductName",strProductName);
        bundle.putString("ProductModels",strProductModels);
        bundle.putString("ProcessId",strProcessId);
        bundle.putString("Process",strProcess);
        bundle.putString("PlanDocno",strPlanDocno);
        bundle.putString("Version",strVersion);
        bundle.putString("PlanDate",strPlanDate);
        bundle.putString("GroupId",strGroupId);
        bundle.putString("Group",strGroup);
        bundle.putString("Quantity",strQuantity);
        bundle.putString("Employee",strEmployee);
        bundle.putString("Device",strDevice);
        bundle.putString("Docno",strDocno);
        bundle.putString("ErrorMsg",strErrorMsg);
        bundle.putString("Label",strLabel);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
    *描述: 获取所有任务清单
    *日期：2022/7/19
    **/
    private void getSubListData(String strType,String strwhere,String status){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductListGet";
                String strType = "7";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ strwhere +"\"/&gt;\n"+
                        "&lt;Field name=\"rows\" value=\""+ iRows +"\"/&gt;\n"+
                        "&lt;Field name=\"startCount\" value=\""+ iStartCount +"\"/&gt;\n"+
                        "&lt;Field name=\"endCount\" value=\""+ iEndCount +"\"/&gt;\n"+
                        "&lt;Field name=\"gwhere\" value=\""+ strWhereProduct +"\"/&gt;\n"+
                        "&lt;Field name=\"user\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonSendTaskData(strResponse,"workorder");

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
                MyToast.myShow(SubDetailForTaskActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList!=null){
                        sendTaskListAdapter = new SendTaskListAdapter(mapResponseList,getApplicationContext(),mSetTaskClickListener,strFlag,mStartTaskListener,mStopTaskListener);
                        subTaskLoadView.setAdapter(sendTaskListAdapter);

                        //显示当前笔数
                        String sCount = (String)mapResponseList.get(0).get("Count");
                        if(sCount.equals("")||sCount.isEmpty()){
                            sCount = "0";
                        }
                        iCount = Integer.parseInt(sCount);
                        if(iCount<=mapResponseList.size()){
                            isLoadMore = false;
                        }else{
                            isLoadMore = true;
                        }

                        //显示加载结果
                        String msg = "总数:"+sCount+",当前:"+String.valueOf(mapResponseList.size());
                        subTaskLoadView.setLoadMoreTitle(msg);

                        //记录条目数
                        iRows = iRows + iEveryRow;
                    }else{
                        //显示加载结果
                        String msg = "总数:0,当前:0";
                        subTaskLoadView.setLoadMoreTitle(msg);
                    }
                }else{
                    //显示加载结果
                    String msg = "总数:0,当前:0";
                    subTaskLoadView.setLoadMoreTitle(msg);
                    MyToast.myShow(SubDetailForTaskActivity.this,statusDescription,0,0);
                }

                progressBar.setVisibility(View.GONE);

                //初始化重启任务状态
                strRestart = "N";
            }
        });
    }

    /**
    *描述: 开启任务
    *日期：2023-05-25
    **/
    private SendTaskListAdapter.StartTaskListener mStartTaskListener = new SendTaskListAdapter.StartTaskListener() {
        @Override
        public void StartTaskClick(int position, View view) {

        }
    };

    /**
    *描述: 中止任务
    *日期：2023-05-25
    **/
    private SendTaskListAdapter.StopTaskListener mStopTaskListener = new SendTaskListAdapter.StopTaskListener() {
        @Override
        public void StopTaskClick(int position, View view) {

        }
    };

    /**
    *描述: 连线任务
    *日期：2022/9/14
    **/
    private SendTaskListAdapter.SetTaskClickListener mSetTaskClickListener = new SendTaskListAdapter.SetTaskClickListener() {
        @Override
        public void SetTaskClick(int position, View view) {
            //初始化值
            String strProductName = sendTaskListAdapter.getItemValue(position,"ProductName");
            String strProductCode = sendTaskListAdapter.getItemValue(position,"ProductCode");
            String strProductModels = sendTaskListAdapter.getItemValue(position,"ProductModels");
            String strProcessId = sendTaskListAdapter.getItemValue(position,"ProcessId");
            String strProcess = sendTaskListAdapter.getItemValue(position,"Process");
            String strDevice = sendTaskListAdapter.getItemValue(position,"Device");
            String strDocno = sendTaskListAdapter.getItemValue(position,"Docno");
            String strVersion = sendTaskListAdapter.getItemValue(position,"Version");
            String strPlanNo = sendTaskListAdapter.getItemValue(position,"Flag");
            String strPlanDate = sendTaskListAdapter.getItemValue(position,"PlanDate");
            String strGroupId = sendTaskListAdapter.getItemValue(position,"GroupId");
            String strGroup = sendTaskListAdapter.getItemValue(position,"Group");
            String strConnectDocno = sendTaskListAdapter.getItemValue(position,"ConnectDocno");

            //打开连线任务设置
            showConnectTask(strProductName,strProductCode,strProductModels,strProcessId,strProcess,strDevice,strDocno,strVersion,strFlag,strPlanDate,strGroupId,strGroup,"Y",strConnectDocno,"","");
        }
    };

    /**
    *描述: 更新派工单数据
    *日期：2022/7/19
    **/
    private void updateTaskData(int position,String action,String status){
        //显示进度条
        loadingDialog = new LoadingDialog(SubDetailForTaskActivity.this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductTaskUpdate";

                String strProcessId = sendTaskListAdapter.getItemValue(position,"ProcessId");
                String strProcess = sendTaskListAdapter.getItemValue(position,"Process");
                String strDevice = sendTaskListAdapter.getItemValue(position,"Device");
                String strDocno = sendTaskListAdapter.getItemValue(position,"Docno");
                String strVersion = sendTaskListAdapter.getItemValue(position,"Version");
                String strPlanNo = sendTaskListAdapter.getItemValue(position,"Flag");
                String strPlanDate = sendTaskListAdapter.getItemValue(position,"PlanDate");
                String strGroupId = sendTaskListAdapter.getItemValue(position,"GroupId");
                String strStationDocno = sendTaskListAdapter.getItemValue(position,"StationDocno");
                String strProcessEnd = sendTaskListAdapter.getItemValue(position,"ProcessEnd");
                String strConnectDocno = sendTaskListAdapter.getItemValue(position,"ConnectDocno");

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"sfaauc_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sfaaucsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sfaaucent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sfaaucmodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"sfaaucdocno\" value=\""+ strDocno +"\"/&gt;\n"+  //工单单号
                        "&lt;Field name=\"sfaaucdocdt\" value=\""+ strPlanDate +"\"/&gt;\n"+  //单据日期
                        "&lt;Field name=\"sfaauc007\" value=\""+ strProcessId +"\"/&gt;\n"+  //工序项次
                        "&lt;Field name=\"sfaauc008\" value=\""+ strProcess +"\"/&gt;\n"+  //工序号
                        "&lt;Field name=\"sfaauc009\" value=\""+ strDevice +"\"/&gt;\n"+  //机器编号
                        "&lt;Field name=\"sfaauc001\" value=\""+ strVersion +"\"/&gt;\n"+  //版本
                        "&lt;Field name=\"sfaauc011\" value=\""+ strGroupId +"\"/&gt;\n"+  //班次
                        "&lt;Field name=\"sfaauc014\" value=\""+ strPlanNo +"\"/&gt;\n"+  //计划单号
                        "&lt;Field name=\"sfaauc023\" value=\""+ strProcessEnd +"\"/&gt;\n"+  //是否连线
                        "&lt;Field name=\"sfaauc028\" value=\""+ strConnectDocno +"\"/&gt;\n"+  //连线单号
                        "&lt;Field name=\"sfaauc031\" value=\""+ strStationDocno +"\"/&gt;\n"+  //组合单号
                        "&lt;Field name=\"sfaaucstus\" value=\""+ status +"\"/&gt;\n"+  //状态码
                        "&lt;Field name=\"restart\" value=\""+ strRestart +"\"/&gt;\n"+  //是否重新启动任务
                        "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //执行动作
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sfaaucseq\" value=\"1.0\"/&gt;\n"+
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
                    MyToast.myShow(SubDetailForTaskActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForTaskActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SubDetailForTaskActivity.this, statusDescription, 1, 1);
                    mapResponseList.clear();
                    getSubListData("11",strWhere,status);
                }else{
                    MyToast.myShow(SubDetailForTaskActivity.this, statusDescription, 0, 1);
                }

                loadingDialog.dismiss();
            }
        });
    }
}