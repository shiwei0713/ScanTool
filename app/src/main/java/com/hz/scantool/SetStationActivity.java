package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hz.scantool.adapter.ConnectProcessListAdapter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyAlertDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.TaskListAdapter;
import com.hz.scantool.dialog.DeviceListDialog;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;
import com.hz.scantool.myui.MyDecoration;

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

public class SetStationActivity extends AppCompatActivity {

    private String strTitle;
    private String statusCode;
    private String statusDescription,strMessage;
    private String strDevice,strStationDocno,strStation,strVersion,strMenuitem,strCount,strErrorMsg,strLabel;
    private String mRecordSet;
    private String strRestart,strStatus,strAction;
    private boolean sSelect;

    private TextView setStationFlag,setStationDocno,setProcessDevice,textProcessDesc;
    private Button btnSave;
    private ProgressBar progressBar;
    private LoadingDialog loadingDialog;
    private RecyclerView setStationRecyclerView;
    private TaskListAdapter taskListAdapter;
    private LinearLayoutManager linearLayoutManager;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapResponseDeviceList;
    private List<String> mDatas,mDeviceDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_station);

        //初始化
        initBundle();
        initView();
        openSearchSelectDialog();

        //显示数据
        getStationList();
    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = getString(R.string.set_station_title);
        strDevice = bundle.getString("device");
        strStationDocno = bundle.getString("stationDocno");
        strStation = bundle.getString("station");
        strVersion = bundle.getString("version");
        strMenuitem = bundle.getString("menuitem");
        strErrorMsg = bundle.getString("errorMsg");
        strLabel = bundle.getString("label");
        sSelect = false;
    }

    //初始化控件
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.setStationToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化布局
        linearLayoutManager = new LinearLayoutManager(this);
        setStationRecyclerView = findViewById(R.id.setStationRecyclerView);
        setStationRecyclerView.setLayoutManager(linearLayoutManager);
        setStationRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //初始化控件
        setProcessDevice = findViewById(R.id.setProcessDevice);
        setStationFlag = findViewById(R.id.setStationFlag);
        setStationDocno = findViewById(R.id.setStationDocno);
        textProcessDesc = findViewById(R.id.textProcessDesc);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        //初始化值
        setProcessDevice.setText(strDevice);
        setStationDocno.setText(strStationDocno);
        setStationFlag.setText(strStation);

        //按钮标题
        showStatus();

        //定义事件
        btnSave.setOnClickListener(new commandClickListener());
    }

    /**
     *描述: 显示任务状态图示
     *日期：2023-05-17
     **/
    private void showStatus(){
        //初始化状态
        switch (strMenuitem){
            case "N":
                btnSave.setText(getResources().getString(R.string.set_process_button_title1));
                strRestart = "N";
                strStatus = "Y";
                strAction = "updconnect";
                break;
            case "Y":
                btnSave.setText(getResources().getString(R.string.set_process_button_title2));
                strRestart = "N";
                strStatus = "C";
                strAction = "updstatus";
                showMessage();
                break;
            case "C":
                btnSave.setText(getResources().getString(R.string.set_process_button_title1));
                strRestart = "Y";
                strStatus = "Y";
                strAction = "updstatus";
                break;
        }
    }

    /**
     *描述: 显示检核信息
     *日期：2023-05-25
     **/
    private void showMessage(){
        String sMessage = strErrorMsg+"\n"+"未确认标签:"+strLabel+"张";

        if(strErrorMsg.equals("")||strErrorMsg.isEmpty()){
            if(strLabel.equals("0")||strLabel.isEmpty()){
                sMessage="";
            }else{
                sMessage="未确认标签:"+strLabel+"张";
            }
        }else{
            if(strLabel.equals("0")||strLabel.isEmpty()){
                sMessage=strErrorMsg;
            }
        }

        textProcessDesc.setText(sMessage);
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
    *描述: 事件实现函数
    *日期：2023-05-11
    **/
    public class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSave:
                    if(checkStation()){
                        updateTaskData();
                    }else{
                        MyAlertDialog.myShowAlertDialog(SetStationActivity.this,"错误信息",strMessage);
                    }
                    break;
            }
        }
    }

    /**
     *描述: 初始化选择数据
     *日期：2022/7/17
     **/
    private void openSearchSelectDialog() {
        if(mDatas==null){
            mDatas = new ArrayList<>();
        }else{
            mDatas.clear();
        }

        if(mDeviceDatas==null){
            mDeviceDatas = new ArrayList<>();
        }else{
            mDeviceDatas.clear();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "StockGet";
                String strType = "10";
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
                mapResponseDeviceList = t100ServiceHelper.getT100JsonEmpDeviceData(strResponse,"stockinfo");

                e.onNext(mapResponseStatus);
                e.onNext(mapResponseDeviceList);
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
                    MyToast.myShow(SetStationActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SetStationActivity.this,e.getMessage(),0,0);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseDeviceList.size()> 0) {
                        //显示单头数据
                        for(int i=0;i<mapResponseDeviceList.size();i++){
                            String sData;
                            String sType =  mapResponseDeviceList.get(i).get("DeviceType").toString();
                            if(sType.equals("4")){
                                //人员信息
                                sData = mapResponseDeviceList.get(i).get("DeviceId").toString()+":"+mapResponseDeviceList.get(i).get("Device").toString();
                                mDatas.add(sData);
                            }else{
                                sData = mapResponseDeviceList.get(i).get("DeviceId").toString();
                                mDeviceDatas.add(sData);
                            }
                        }
                    }
                }else{
                    MyToast.myShow(SetStationActivity.this,statusDescription,0,0);
                }
            }
        });
    }

    /**
     *描述: 显示工艺数据
     *日期：2022/9/14
     **/
    private void getStationList(){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductListGet";
                String strType = "20";
                String strwhere = " sfaauc031='"+strStationDocno+"' AND sfaaucstus='"+strMenuitem+"'";

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
                mapResponseList = t100ServiceHelper.getT100JsonItemProcessData2(strResponse,"iteminfo");

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
                MyToast.myShow(SetStationActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()>0){
                        //显示数据
                        showItemData();
                    }
                }else{
                    MyToast.myShow(SetStationActivity.this,statusDescription,0,0);
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     *描述: 显示数据by RecyclerView
     *日期：2023-05-08
     **/
    private void showItemData(){
        taskListAdapter = new TaskListAdapter(mapResponseList,getApplicationContext());
        setStationRecyclerView.setAdapter(taskListAdapter);
        setStationRecyclerView.addItemDecoration(new MyDecoration());

        //按钮点击事件实现--设置人员
        taskListAdapter.setEmployeeClickListener(new TaskListAdapter.EmployeeClickListener() {
            @Override
            public void employeeClick(View itemView, int position) {
                DeviceListDialog.Builder alert = new DeviceListDialog.Builder(SetStationActivity.this);
                alert.setListData(mDatas);
                alert.setTitle("请选择人员");
                alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
                    @Override
                    public void onSelected(String info) {
                        if(!strMenuitem.equals("Y")){
                            try{
                                taskListAdapter.updateItem("Employee",info,position,false);

                                //已中止任务,修改人员默认为产生新任务
                                if(strMenuitem.equals("C") && !sSelect){
//                                    taskListAdapter.clearEmployeeItem(position);
                                    btnSave.setText(getResources().getString(R.string.set_process_button_title3));
                                    strStatus = "Y";
                                    strAction = "updconnect";
                                    sSelect = true;
                                }
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }else{
                            MyAlertDialog.myShowAlertDialog(SetStationActivity.this,"错误信息","已启用任务不可修改人员");
                        }
                    }
                });
                DeviceListDialog mDialog = alert.show();
                //设置Dialog 尺寸
                mDialog.setDialogWindowAttr(0.8, 0.8, SetStationActivity.this);
            }
        });

        //按钮点击事件实现--单开
        taskListAdapter.setUpdateClickListener(new TaskListAdapter.UpdateClickListener() {
            @Override
            public void updateClick(View itemView, int position) {
                if(!strMenuitem.equals("Y")){
                    if(mapResponseList.size()>0){
                        for(int i=0;i<mapResponseList.size();i++){
                            String sStatus;
                            if(i==position){
                                sStatus = "Y";
                            }else{
                                sStatus = "N";
                            }

                            taskListAdapter.updateItemStatus(i,sStatus);
                        }
                    }
                }else{
                    MyAlertDialog.myShowAlertDialog(SetStationActivity.this,"错误信息","已启用任务不可修改");
                }
            }
        });

        //按钮点击事件实现--启用
        taskListAdapter.setStartClickListener(new TaskListAdapter.StartClickListener() {
            @Override
            public void startClick(View itemView, int position) {
                if(!strMenuitem.equals("Y")){
                    taskListAdapter.updateItemStatus(position,"Y");
                }else{
                    MyAlertDialog.myShowAlertDialog(SetStationActivity.this,"错误信息","已启用任务不可修改");
                }
            }
        });

        //按钮点击事件实现--停用
        taskListAdapter.setStopClickListener(new TaskListAdapter.StopClickListener() {
            @Override
            public void stopClick(View itemView, int position) {
                if(!strMenuitem.equals("Y")){
                    taskListAdapter.updateItemStatus(position,"N");
                }else{
                    MyAlertDialog.myShowAlertDialog(SetStationActivity.this,"错误信息","已启用任务不可修改");
                }
            }
        });

        //按钮点击事件实现--清除
        taskListAdapter.setClearClickListener(new TaskListAdapter.ClearClickListener() {
            @Override
            public void clearClick(View itemView, int position) {
                if(!strMenuitem.equals("Y")){
                    taskListAdapter.clearItem(position);
                }else{
                    MyAlertDialog.myShowAlertDialog(SetStationActivity.this,"错误信息","已启用任务不可修改");
                }
            }
        });

    }

    /**
    *描述: 检查组合任务数据是否完整,至少选择一个任务,不同任务组合工时不同
    *日期：2023-05-13
    **/
    private boolean checkStation(){
        int iCount = 0;
        for(int i= 0;i< mapResponseList.size();i++){
            String sCheck = (String)mapResponseList.get(i).get("Select");
            if(sCheck.equals("Y")) {
                String sEmployee = (String)mapResponseList.get(i).get("Employee");
                if(sEmployee.equals("")||sEmployee.isEmpty()){
                    strMessage = "第"+(i+1)+"条任务未设置人员";
                    return false;
                }

                iCount++;
            }
        }

        if(iCount==0){
            strMessage = "至少选择一个任务";
            return false;
        }

        if(iCount>0){
            strCount = String.valueOf(iCount);
        }

        return true;
    }

    /**
     *描述: 生成XML数据
     *日期：2022/9/16
     **/
    private void genUpdXml(){
        //生成数据集合
        String strDevice = setProcessDevice.getText().toString();
        String strStation = setStationFlag.getText().toString();
        String strStationDocno = setStationDocno.getText().toString();

        int iSeq = 1;
        for(int i= 0;i< mapResponseList.size();i++){
            String sCheck = (String)mapResponseList.get(i).get("Select");
            if(sCheck.equals("Y")) {
                String sDevice = (String)mapResponseList.get(i).get("Device");
                String sFlag = (String)mapResponseList.get(i).get("ConnectProcess");
                String sEmployee = (String)mapResponseList.get(i).get("Employee");
                String sProcessId = (String)mapResponseList.get(i).get("ProcessId");
                String sProcess = (String)mapResponseList.get(i).get("Process");
                String sDocno = (String)mapResponseList.get(i).get("Docno");
                String sPlanNo = (String)mapResponseList.get(i).get("PlanNO");
                String sVersion = (String)mapResponseList.get(i).get("Version");
                String sGroupId = (String)mapResponseList.get(i).get("GroupId");
                String sProductCode = (String)mapResponseList.get(i).get("ProductCode");
                String sPlanDate = (String)mapResponseList.get(i).get("PlanDate");
                String sGroupStation = (String)mapResponseList.get(i).get("GroupStation");
                String sQuantity = (String)mapResponseList.get(i).get("Quantity");
                String sUnit = (String)mapResponseList.get(i).get("Unit");
                String sLots = (String)mapResponseList.get(i).get("Lots");
                String sModLots = (String)mapResponseList.get(i).get("ModLots");
                String sWorkHours = (String)mapResponseList.get(i).get("WorkHours");
                String sDocType =  (String)mapResponseList.get(i).get("DocType");
                String sUserProduct = (String)mapResponseList.get(i).get("UserProduct");
                String sWorkStation = (String)mapResponseList.get(i).get("WorkStation");
                String sOldGroupStation = (String)mapResponseList.get(i).get("OldGroupStation");
                String sStationDocno = (String)mapResponseList.get(i).get("StationDocno");
                String sConnect = "N";

                mRecordSet = mRecordSet + "&lt;Master name=\"sfaauc_t\" node_id=\""+iSeq+"\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sfaaucsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sfaaucent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sfaaucmodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"sfaaucdocno\" value=\""+ sDocno +"\"/&gt;\n"+  //工单单号
                        "&lt;Field name=\"sfaaucdocdt\" value=\""+ sPlanDate +"\"/&gt;\n"+  //单据日期
                        "&lt;Field name=\"sfaauc003\" value=\""+ sProductCode +"\"/&gt;\n"+  //料件编号
                        "&lt;Field name=\"sfaauc004\" value=\""+ sQuantity +"\"/&gt;\n"+  //计划数量
                        "&lt;Field name=\"sfaauc005\" value=\""+ sUnit +"\"/&gt;\n"+  //单位
                        "&lt;Field name=\"sfaauc007\" value=\""+ sProcessId +"\"/&gt;\n"+  //工序项次
                        "&lt;Field name=\"sfaauc008\" value=\""+ sProcess +"\"/&gt;\n"+  //工序号
                        "&lt;Field name=\"sfaauc009\" value=\""+ sDevice +"\"/&gt;\n"+  //机器编号
                        "&lt;Field name=\"sfaauc002\" value=\""+ sEmployee +"\"/&gt;\n"+  //生产人员
                        "&lt;Field name=\"sfaauc001\" value=\""+ sVersion +"\"/&gt;\n"+  //版本
                        "&lt;Field name=\"sfaauc011\" value=\""+ sGroupId +"\"/&gt;\n"+  //班次
                        "&lt;Field name=\"sfaauc012\" value=\""+ sLots +"\"/&gt;\n"+  //批次
                        "&lt;Field name=\"sfaauc013\" value=\""+ sModLots +"\"/&gt;\n"+  //同模类型
                        "&lt;Field name=\"sfaauc014\" value=\""+ sPlanNo +"\"/&gt;\n"+  //计划单号
                        "&lt;Field name=\"sfaauc017\" value=\""+ sWorkHours +"\"/&gt;\n"+  //工时
                        "&lt;Field name=\"sfaauc018\" value=\""+ sGroupStation +"\"/&gt;\n"+  //组合工位标识
                        "&lt;Field name=\"sfaauc020\" value=\""+ sDocType +"\"/&gt;\n"+  //工单类别
                        "&lt;Field name=\"sfaauc021\" value=\""+ sFlag +"\"/&gt;\n"+  //连线标识
                        "&lt;Field name=\"sfaauc023\" value=\""+ sConnect +"\"/&gt;\n"+  //是否连线
                        "&lt;Field name=\"sfaauc024\" value=\""+ sUserProduct +"\"/&gt;\n"+  //用户导入零件号
                        "&lt;Field name=\"sfaauc029\" value=\""+ sWorkStation +"\"/&gt;\n"+  //上机工位
                        "&lt;Field name=\"sfaauc030\" value=\""+ sOldGroupStation +"\"/&gt;\n"+  //旧组合
                        "&lt;Field name=\"sfaauc031\" value=\""+ sStationDocno +"\"/&gt;\n"+  //组合单号
                        "&lt;Field name=\"sfaaucstus\" value=\""+ strStatus +"\"/&gt;\n"+  //状态码
                        "&lt;Field name=\"act\" value=\""+ strAction +"\"/&gt;\n"+  //执行动作
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sfaaucseq\" value=\"1.0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Detail&gt;\n"+
                        "&lt;Memo/&gt;\n"+
                        "&lt;Attachment count=\"0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Master&gt;\n";

                iSeq++;
            }
        }
    }

    /**
     *描述: 更新派工单数据
     *日期：2022/7/19
     **/
    private void updateTaskData(){
        //显示进度条
        loadingDialog = new LoadingDialog(SetStationActivity.this,"玩命提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        //生成需求xml
        genUpdXml();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductTaskUpdate";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        mRecordSet+
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
                    MyToast.myShow(SetStationActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SetStationActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SetStationActivity.this, statusDescription, 1, 1);
                    finish();
                }else{
                    MyAlertDialog.myShowAlertDialog(SetStationActivity.this,"错误信息",statusDescription);
                }

                loadingDialog.dismiss();
            }
        });
    }
}