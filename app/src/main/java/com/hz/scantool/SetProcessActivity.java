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

public class SetProcessActivity extends AppCompatActivity{

    private String strTitle;
    private String strProductName,strProductCode,strProductModels,strProcessId,strProcess,strDevice,strDocno,strVersion, strMenuitem,strPlanDate,strGroupId,strGroup,strConnect,strConnectDocno,strErrorMsg,strLabel;
    private String strRestart,strStatus,strAction;
    private String statusCode;
    private String statusDescription;
    private String mRecordSet;
    private String strMessage;
    private boolean sSelect;

    private TextView setProcessProductCode,setProcessProductName,setProcessProductModels;
    private Button btnSave;
    private TextView setProcessPlanDate,setProcessGroupId,setProcessGroup,textProcessDesc,txtSetEmployee,txtSetDevices;
    private ProgressBar progressBar;
    private LoadingDialog loadingDialog;
    private RecyclerView setProcessRecyclerView;
    private ConnectProcessListAdapter connectProcessListAdapter;
    private LinearLayoutManager linearLayoutManager;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapResponseDeviceList;
    private List<String> mDatas,mDeviceDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_process);

        //初始化
        initBundle();
        initView();
        openSearchSelectDialog();

        //初始化工艺显示
        getProcessList();

    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = getString(R.string.set_process_title);
        strProductName = bundle.getString("productName");
        strProductCode = bundle.getString("productCode");
        strProductModels = bundle.getString("productModels");
        strProcessId = bundle.getString("processId");
        strProcess = bundle.getString("process");
        strDevice = bundle.getString("device");
        strDocno = bundle.getString("docno");
        strVersion = bundle.getString("version");
        strMenuitem = bundle.getString("menuitem");
        strPlanDate = bundle.getString("plandate");
        strGroupId = bundle.getString("groupid");
        strGroup = bundle.getString("group");
        strConnect = bundle.getString("connect");  //是否连线
        strConnectDocno = bundle.getString("connectDocno");
        strErrorMsg = bundle.getString("errorMsg");
        strLabel = bundle.getString("label");
        sSelect = false;
    }

    //初始化控件
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.setProcessToolBar);
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
        setProcessRecyclerView = findViewById(R.id.setProcessRecyclerView);
        setProcessRecyclerView.setLayoutManager(linearLayoutManager);
        setProcessRecyclerView.setItemAnimator(new DefaultItemAnimator());
        setProcessRecyclerView.addItemDecoration(new MyDecoration());

        //初始化控件
        setProcessProductCode = findViewById(R.id.setProcessProductCode);
        setProcessProductName = findViewById(R.id.setProcessProductName);
        setProcessProductModels = findViewById(R.id.setProcessProductModels);
        txtSetEmployee = findViewById(R.id.txtSetEmployee);
        txtSetDevices = findViewById(R.id.txtSetDevices);

        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        setProcessPlanDate = findViewById(R.id.setProcessPlanDate);
        setProcessGroupId = findViewById(R.id.setProcessGroupId);
        setProcessGroup = findViewById(R.id.setProcessGroup);
        textProcessDesc = findViewById(R.id.textProcessDesc);

        //初始化值
        setProcessProductCode.setText(strProductCode);
        setProcessProductName.setText(strProductName);
        setProcessProductModels.setText(strProductModels);
        setProcessPlanDate.setText(strPlanDate);
        setProcessGroupId.setText(strGroupId);
        setProcessGroup.setText(strGroup);

        //按钮标题
        showStatus();

        //定义事件
        btnSave.setOnClickListener(new commandClickListener());
        txtSetEmployee.setOnClickListener(new commandClickListener());
        txtSetDevices.setOnClickListener(new commandClickListener());
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
    *描述: 按钮单击事件
    *日期：2022/9/15
    **/
    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSave:
                    if(checkText()){
                        if(!checkProcess()){
                            MyToast.myShow(SetProcessActivity.this,"连线生产,工艺需连续",2,0);
                        }else{
                            updateTaskData();
                        }
                    }else{
                        MyToast.myShow(SetProcessActivity.this,strMessage,2,0);
                    }
                    break;
                case R.id.txtSetEmployee:
                    setEmployeeToAllItem();
                    break;
                case R.id.txtSetDevices:
                    setDeviceToAllItem();
                    break;
            }
        }
    }

    /**
    *描述: 显示连线工艺
    *日期：2023-05-06
    **/
    private void showProcessDesc(){
        String sAllProcess="";
        for(int i=0;i<mapResponseList.size();i++){
            String sSelect = (String)mapResponseList.get(i).get("Select");
            if(sSelect.equals("Y")){
                String sProcess = (String)mapResponseList.get(i).get("Process");
                String sDesc = sProcess;
                if(sAllProcess.equals("")||sAllProcess.isEmpty()){
                    sAllProcess = sDesc;
                }else{
                    sAllProcess = sAllProcess+"->"+ sDesc;
                }
            }
        }

        textProcessDesc.setText(sAllProcess);
    }

    /**
    *描述: 检核人员和设备是否完整
    *日期：2023-05-06
    **/
    private boolean checkText(){
        //更新工序状态,主要区分首序、中间序和末序
        if(strMenuitem.equals("N")){
            updateConnectProcess();
        }

        for(int i= 0;i<mapResponseList.size();i++){
            String sCheck = (String)mapResponseList.get(i).get("Select");
            String sConnectProcess = (String)mapResponseList.get(i).get("ConnectProcess");
            String sProcess = (String)mapResponseList.get(i).get("Process");
            String sDevice = (String)mapResponseList.get(i).get("Device");
            String sEmployee = (String)mapResponseList.get(i).get("Employee");
            if(sCheck.equals("Y")){
                if(sDevice.equals("")||sDevice.isEmpty()){
                    strMessage = "工序:"+sProcess+"必须指派设备";
                    return false;
                }else{
                    if(sEmployee.equals("")||sEmployee.isEmpty()){
                        strMessage = "工序:"+sProcess+"必须指派人员";
                        return false;
                    }
                }
            }else{
                if(sConnectProcess.equals("INIT")){
                    strMessage = "首工序:"+sProcess+"必须指派人员和设备";
                    return false;
                }
            }
        }

        return true;
    }

    /**
    *描述: 检核连线工艺是否正确,主要检核是否为连续工艺
    *日期：2022/9/27
    **/
    private boolean checkProcess(){
        boolean isSuccess = false;
        String sFirstProcess = strProcess;

        for(int i= 0;i<mapResponseList.size();i++){
            String sCheck = (String)mapResponseList.get(i).get("Select");
            if(sCheck.equals("Y")){
                String sPreProcess = (String)mapResponseList.get(i).get("PreProcess");
                int iIndex = sPreProcess.indexOf("/");
                if(iIndex>-1){
                    String[] arrayPreProcess = sPreProcess.split("/");
                    for(int m=0;m<arrayPreProcess.length;m++){
                        if(!arrayPreProcess[m].equals(sFirstProcess)&&!arrayPreProcess[m].equals("INIT")){
                            for(int j=0;j<mapResponseList.size();j++){
                                String sProcess = (String)mapResponseList.get(j).get("Process");
                                String sCheck2 = (String)mapResponseList.get(j).get("Select");
                                if(sCheck2.equals("Y")){
                                    if(!arrayPreProcess[m].equals(sProcess)){
                                        isSuccess = false;
                                    }else{
                                        isSuccess = true;
                                        break;
                                    }
                                }
                            }
                        }else {
                            isSuccess = true;
                        }
                    }
                }else{
                    if(!sPreProcess.equals(sFirstProcess)&&!sPreProcess.equals("INIT")){
                        for(int j=0;j<mapResponseList.size();j++){
                            String sProcess = (String)mapResponseList.get(j).get("Process");
                            String sCheck2 = (String)mapResponseList.get(j).get("Select");
                            if(sCheck2.equals("Y")){
                                if(!sPreProcess.equals(sProcess)){
                                    isSuccess = false;
                                }else{
                                    isSuccess = true;
                                    break;
                                }
                            }
                        }
                    }else {
                        isSuccess = true;
                    }
                }
            }
        }

        return isSuccess;
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
                    MyToast.myShow(SetProcessActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SetProcessActivity.this,e.getMessage(),0,0);
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
                    MyToast.myShow(SetProcessActivity.this,statusDescription,0,0);
                }
            }
        });
    }

    /**
     *描述: 格式化条件
     *日期：2023-05-23
     **/
    private String formatConditions(String condition,String spilt){
        String strCondition = "";

        int iSpiltIndex = condition.indexOf(spilt); //查找分割索引
        if(iSpiltIndex>-1){
            String[] strArray = condition.split(spilt);
            for(int i=0;i<strArray.length;i++){
                if(strCondition.equals("")||strCondition.isEmpty()){
                    strCondition = "'"+strArray[i]+"'";
                }else{
                    strCondition = strCondition + ",'" +strArray[i]+"'";
                }
            }
        }else{
            strCondition =  "'"+condition+"'";
        }

        strCondition = "("+strCondition+")";

        return strCondition;
    }

    /**
    *描述: 显示工艺数据
    *日期：2022/9/14
    **/
    private void getProcessList(){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductListGet";
                String strType = "6";
                String sCondition = "";
                String sWhere = " sfaaucstus IN ('"+strMenuitem+"')";

                //查询条件
                //如果为未启用,则显示工艺明细;未已启用和中止,则显示任务明细
                if(strMenuitem.equals("N")){
                    String sProductCode = setProcessProductCode.getText().toString();
                    sCondition = " ecbb001 IN ("+formatConditions(sProductCode,"/")+") AND ecbb003>="+strProcessId;
                }else{
                    sCondition = " sfaauc028='"+strConnectDocno+"'";
                    sWhere = sWhere+" AND "+sCondition;
                }

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ sCondition +"\"/&gt;\n"+
                        "&lt;Field name=\"gwhere\" value=\""+ sWhere +"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+ "" +"\"/&gt;\n"+
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
                MyToast.myShow(SetProcessActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    //显示数据
                    showItemData();
                }else{
                    MyToast.myShow(SetProcessActivity.this,statusDescription,0,0);
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
        connectProcessListAdapter = new ConnectProcessListAdapter(mapResponseList,getApplicationContext(),"N");
        setProcessRecyclerView.setAdapter(connectProcessListAdapter);

        //人员选择
        connectProcessListAdapter.setSetEmployeeClickListener(new ConnectProcessListAdapter.SetEmployeeClickListener() {
            @Override
            public void setEmployeeClick(View itemView, int position) {
                DeviceListDialog.Builder alert = new DeviceListDialog.Builder(SetProcessActivity.this);
                alert.setListData(mDatas);
                alert.setTitle("请选择人员");
                alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
                    @Override
                    public void onSelected(String info) {
                        if(!strMenuitem.equals("Y")){
                            try{
                                String sPlanNo = (String)mapResponseList.get(position).get("PlanNO");
                                if(sPlanNo.equals("")||sPlanNo.isEmpty()){
                                    MyAlertDialog.myShowAlertDialog(SetProcessActivity.this,"错误信息","此工序无任务,无法连线生产");
                                }else{
                                    //已中止任务,修改人员默认为产生新任务
                                    if(strMenuitem.equals("C") && !sSelect){
                                        connectProcessListAdapter.clearEmployeeItem(position);
                                        btnSave.setText(getResources().getString(R.string.set_process_button_title3));
                                        strStatus = "Y";
                                        strAction = "updconnect";
                                        sSelect = true;
                                    }

                                    connectProcessListAdapter.updateItem("Employee",info,position,false);
                                    showProcessDesc();
                                }
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }else{
                            MyAlertDialog.myShowAlertDialog(SetProcessActivity.this,"错误信息","已启用任务不可修改人员");
                        }
                    }
                });
                DeviceListDialog mDialog = alert.show();
                //设置Dialog 尺寸
                mDialog.setDialogWindowAttr(0.8, 0.8, SetProcessActivity.this);
            }
        });

        //设备选择
        connectProcessListAdapter.setSetDeviceClickListener(new ConnectProcessListAdapter.SetDeviceClickListener() {
            @Override
            public void setDeviceClick(View itemView, int position) {
                DeviceListDialog.Builder alert = new DeviceListDialog.Builder(SetProcessActivity.this);
                alert.setListData(mDeviceDatas);
                alert.setTitle("请选择设备");
                alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
                    @Override
                    public void onSelected(String info) {
                        if(strMenuitem.equals("N")){
                            try {
                                connectProcessListAdapter.updateItem("Device",info,position,true);
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }else{
                            MyAlertDialog.myShowAlertDialog(SetProcessActivity.this,"错误信息","连线任务不可修改设备");
                        }

                    }
                });
                DeviceListDialog mDialog = alert.show();
                //设置Dialog 尺寸
                mDialog.setDialogWindowAttr(0.8, 0.8, SetProcessActivity.this);
            }
        });

        //清除
        connectProcessListAdapter.setClearClickListener(new ConnectProcessListAdapter.ClearClickListener() {
            @Override
            public void clearClick(View itemView, int position) {
                connectProcessListAdapter.cleareItem(position);
                showProcessDesc();
            }
        });
    }

    /**
    *描述: 批量设置人员
    *日期：2023-05-27
    **/
    private void setEmployeeToAllItem(){
        DeviceListDialog.Builder alert = new DeviceListDialog.Builder(SetProcessActivity.this);
        alert.setListData(mDatas);
        alert.setTitle("请选择人员");
        alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
            @Override
            public void onSelected(String info) {
                if(!strMenuitem.equals("Y")){
                    for(int i=0;i<mapResponseList.size();i++){
                        try{
                            String sPlanNo = (String)mapResponseList.get(i).get("PlanNO");
                            if(sPlanNo.equals("")||sPlanNo.isEmpty()){
                                continue;
                            }else{
                                //已中止任务,修改人员默认为产生新任务
                                if(strMenuitem.equals("C") && !sSelect && i==0){
                                    connectProcessListAdapter.clearAllEmployeeItem();
                                    btnSave.setText(getResources().getString(R.string.set_process_button_title3));
                                    strStatus = "Y";
                                    strAction = "updconnect";
                                    sSelect = true;
                                }

                                connectProcessListAdapter.updateItem("Employee",info,i,false);
                                showProcessDesc();
                            }
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }else{
                    MyAlertDialog.myShowAlertDialog(SetProcessActivity.this,"错误信息","已启用任务不可修改人员");
                }
            }
        });
        DeviceListDialog mDialog = alert.show();
        //设置Dialog 尺寸
        mDialog.setDialogWindowAttr(0.8, 0.8, SetProcessActivity.this);
    }

    /**
    *描述: 批量设置设备
    *日期：2023-05-27
    **/
    private void setDeviceToAllItem(){
        DeviceListDialog.Builder alert = new DeviceListDialog.Builder(SetProcessActivity.this);
        alert.setListData(mDeviceDatas);
        alert.setTitle("请选择设备");
        alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
            @Override
            public void onSelected(String info) {
                if(strMenuitem.equals("N")){
                    for(int i=0;i<mapResponseList.size();i++){
                        try {
                            String sPlanNo = (String)mapResponseList.get(i).get("PlanNO");
                            if(sPlanNo.equals("")||sPlanNo.isEmpty()){
                                continue;
                            }else{
                                connectProcessListAdapter.updateItem("Device",info,i,true);
                            }
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }else{
                    MyAlertDialog.myShowAlertDialog(SetProcessActivity.this,"错误信息","连线任务不可修改设备");
                }

            }
        });
        DeviceListDialog mDialog = alert.show();
        //设置Dialog 尺寸
        mDialog.setDialogWindowAttr(0.8, 0.8, SetProcessActivity.this);
    }

    /**
    *描述: 更新连线工序标识，区分首序、中间序和末序
    *日期：2022/12/4
    **/
    private void updateConnectProcess(){
        String sProcessFlag = "";
        String sFlag = "INIT";

        for(int i=0;i<mapResponseList.size();i++){
            String sProcess1 = (String)mapResponseList.get(i).get("Process");
            String sPreProcess1 = (String)mapResponseList.get(i).get("PreProcess");
            if(sProcess1.equals(strProcess)||sPreProcess1.equals(sProcessFlag)){
                //更新首序连线标识
                mapResponseList.get(i).put("ConnectProcess","INIT");
            }else{
                String sCheck = (String)mapResponseList.get(i).get("Select");
                if(sCheck.equals("Y")) {
                    String sProcess = (String)mapResponseList.get(i).get("Process");

                    //检核是否为最后序
                    sFlag = "END";
                    String sConnectProcess = (String)mapResponseList.get(i).get("ConnectProcess");
                    if(sConnectProcess.equals("")||sConnectProcess.isEmpty()){
                        for(int j=0;j< mapResponseList.size();j++){
                            String sCheck2 = (String)mapResponseList.get(j).get("Select");
                            String sPreProcess = (String)mapResponseList.get(j).get("PreProcess");

                            if(sCheck2.equals("Y")){
                                int iIndex = sPreProcess.indexOf("/");
                                if(iIndex>-1){
                                    String[] arrayPreProcess = sPreProcess.split("/");
                                    for(int m=0;m<arrayPreProcess.length;m++){
                                        if(sProcess.equals(arrayPreProcess[m])){
                                            sFlag = "MID";
                                        }
                                    }
                                }else{
                                    if(sProcess.equals(sPreProcess)){
                                        sFlag = "MID";
                                    }
                                }
                            }
                        }
                    }else{
                        sFlag = sConnectProcess;
                    }

                    mapResponseList.get(i).put("ConnectProcess",sFlag);
                }
            }
        }
    }

    /**
    *描述: 生成XML数据
    *日期：2022/9/16
    **/
    private void genUpdXml(){
        //时间戳
        String sUuid = strConnectDocno;
        if(strAction.equals("updconnect")){
            long timeCurrentTimeMillis = System.currentTimeMillis();
            sUuid = "LX"+timeCurrentTimeMillis;
        }

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
                        "&lt;Field name=\"sfaauc023\" value=\""+ strConnect +"\"/&gt;\n"+  //是否连线
                        "&lt;Field name=\"sfaauc024\" value=\""+ sUserProduct +"\"/&gt;\n"+  //用户导入零件号
                        "&lt;Field name=\"sfaauc028\" value=\""+ sUuid +"\"/&gt;\n"+  //连线单号
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
    *描述: 更新连线任务，更新当前任务生产人员和设备，并插入对应工序任务
    *日期：2022/9/15
    **/
    private void updateTaskData(){
        //生成xml文件
        genUpdXml();

        //检查是否有数据
        if(mRecordSet.equals("")||mRecordSet.isEmpty()){
            MyAlertDialog.myShowAlertDialog(SetProcessActivity.this,"错误信息","无数据需要提交");
            return;
        }

        //显示进度条
        loadingDialog = new LoadingDialog(SetProcessActivity.this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

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
                    MyToast.myShow(SetProcessActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SetProcessActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SetProcessActivity.this, statusDescription, 1, 1);
                    finish();
                }else{
                    MyAlertDialog.myShowAlertDialog(SetProcessActivity.this,"错误信息",statusDescription);
                }

                loadingDialog.dismiss();
            }
        });
    }
}