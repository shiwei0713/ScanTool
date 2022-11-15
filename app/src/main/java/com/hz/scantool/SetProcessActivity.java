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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hz.scantool.adapter.ListItemAdapter;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.ProcessListAdapter;
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

public class SetProcessActivity extends AppCompatActivity {

    private String strTitle;
    private String strProductName,strProductCode,strProductModels,strProcessId,strProcess,strDevice,strDocno,strVersion,strFlag,strPlanDate,strGroupId,strGroup,strConnect;
    private String statusCode;
    private String statusDescription;
    private String mRecordSet;

    private TextView setProcessProductCode,setProcessProductName,setProcessProductModels,setProcessProcessId,setProcessProcess,setProcessProductDocno,setProcessVersion,setProcessDevice,setProcessFlag;
    private Button btnSave,btnUpdateEmployee;
    private TextView setProcessEmployee,setProcessEmployee2,setProcessEmployee3,setProcessEmployee4,setProcessEmployee5,setProcessEmployee6;
    private ImageButton setProcessEmployeeClear,setProcessEmployeeClear2,setProcessEmployeeClear3,setProcessEmployeeClear4,setProcessEmployeeClear5,setProcessEmployeeClear6;
    private TextView setProcessPlanDate,setProcessGroupId,setProcessGroup,setProcessIndex;
    private ProgressBar progressBar;
    private ListView setProcessView;
    private ProcessListAdapter processListAdapter;
    private LoadingDialog loadingDialog;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapResponseDeviceList;
    private List<String> mDataDevice,mDataEmployee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_process);

        //初始化
        initBundle();
        initView();

        //初始化选择数据源
        mDataDevice = initData("3"," 1=1");
        mDataEmployee = initData("4"," 1=1");

        //初始化工艺显示
        getProcessList();

    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        strProductName = bundle.getString("productName");
        strProductCode = bundle.getString("productCode");
        strProductModels = bundle.getString("productModels");
        strProcessId = bundle.getString("processId");
        strProcess = bundle.getString("process");
        strDevice = bundle.getString("device");
        strDocno = bundle.getString("docno");
        strVersion = bundle.getString("version");
        strFlag = bundle.getString("flag");
        strPlanDate = bundle.getString("plandate");
        strGroupId = bundle.getString("groupid");
        strGroup = bundle.getString("group");
        strConnect = bundle.getString("connect");
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

        //初始化控件
        setProcessProductCode = findViewById(R.id.setProcessProductCode);
        setProcessProductName = findViewById(R.id.setProcessProductName);
        setProcessProductModels = findViewById(R.id.setProcessProductModels);
        setProcessProcessId = findViewById(R.id.setProcessProcessId);
        setProcessProcess = findViewById(R.id.setProcessProcess);
        setProcessProductDocno = findViewById(R.id.setProcessProductDocno);
        setProcessVersion = findViewById(R.id.setProcessVersion);
        setProcessDevice = findViewById(R.id.setProcessDevice);
        setProcessFlag = findViewById(R.id.setProcessFlag);
        setProcessIndex = findViewById(R.id.setProcessIndex);

        btnSave = findViewById(R.id.btnSave);
        btnUpdateEmployee = findViewById(R.id.btnUpdateEmployee);
        progressBar = findViewById(R.id.progressBar);
        setProcessView = findViewById(R.id.setProcessView);

        setProcessEmployee = findViewById(R.id.setProcessEmployee);
        setProcessEmployee2 = findViewById(R.id.setProcessEmployee2);
        setProcessEmployee3 = findViewById(R.id.setProcessEmployee3);
        setProcessEmployee4 = findViewById(R.id.setProcessEmployee4);
        setProcessEmployee5 = findViewById(R.id.setProcessEmployee5);
        setProcessEmployee6 = findViewById(R.id.setProcessEmployee6);
        setProcessEmployeeClear = findViewById(R.id.setProcessEmployeeClear);
        setProcessEmployeeClear2 = findViewById(R.id.setProcessEmployeeClear2);
        setProcessEmployeeClear3 = findViewById(R.id.setProcessEmployeeClear3);
        setProcessEmployeeClear4 = findViewById(R.id.setProcessEmployeeClear4);
        setProcessEmployeeClear5 = findViewById(R.id.setProcessEmployeeClear5);
        setProcessEmployeeClear6 = findViewById(R.id.setProcessEmployeeClear6);

        setProcessPlanDate = findViewById(R.id.setProcessPlanDate);
        setProcessGroupId = findViewById(R.id.setProcessGroupId);
        setProcessGroup = findViewById(R.id.setProcessGroup);

        //初始化值
        setProcessProductCode.setText(strProductCode);
        setProcessProductName.setText(strProductName);
        setProcessProductModels.setText(strProductModels);
        setProcessProcessId.setText(strProcessId);
        setProcessProcess.setText(strProcess);
        setProcessProductDocno.setText(strDocno);
        setProcessVersion.setText(strVersion);
        setProcessDevice.setText(strDevice);
        setProcessFlag.setText(strFlag);
        setProcessPlanDate.setText(strPlanDate);
        setProcessGroupId.setText(strGroupId);
        setProcessGroup.setText(strGroup);

        btnSave.setOnClickListener(new commandClickListener());
        btnUpdateEmployee.setOnClickListener(new commandClickListener());
        setProcessDevice.setOnClickListener(new commandClickListener());
        setProcessEmployee.setOnClickListener(new commandClickListener());
        setProcessEmployee2.setOnClickListener(new commandClickListener());
        setProcessEmployee3.setOnClickListener(new commandClickListener());
        setProcessEmployee4.setOnClickListener(new commandClickListener());
        setProcessEmployee5.setOnClickListener(new commandClickListener());
        setProcessEmployee6.setOnClickListener(new commandClickListener());
        setProcessEmployeeClear.setOnClickListener(new commandClickListener());
        setProcessEmployeeClear2.setOnClickListener(new commandClickListener());
        setProcessEmployeeClear3.setOnClickListener(new commandClickListener());
        setProcessEmployeeClear4.setOnClickListener(new commandClickListener());
        setProcessEmployeeClear5.setOnClickListener(new commandClickListener());
        setProcessEmployeeClear6.setOnClickListener(new commandClickListener());
        setProcessView.setOnItemClickListener(new listItemClickListener());

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
                case R.id.setProcessDevice:
                    openSearchSelectDialog(setProcessDevice,"请选择设备",mDataDevice);
                    break;
                case R.id.setProcessEmployee:
                    openSearchSelectDialog(setProcessEmployee,"请选择操作工",mDataEmployee);
                    break;
                case R.id.setProcessEmployee2:
                    openSearchSelectDialog(setProcessEmployee2,"请选择操作工",mDataEmployee);
                    break;
                case R.id.setProcessEmployee3:
                    openSearchSelectDialog(setProcessEmployee3,"请选择操作工",mDataEmployee);
                    break;
                case R.id.setProcessEmployee4:
                    openSearchSelectDialog(setProcessEmployee4,"请选择操作工",mDataEmployee);
                    break;
                case R.id.setProcessEmployee5:
                    openSearchSelectDialog(setProcessEmployee5,"请选择操作工",mDataEmployee);
                    break;
                case R.id.setProcessEmployee6:
                    openSearchSelectDialog(setProcessEmployee6,"请选择操作工",mDataEmployee);
                    break;
                case R.id.setProcessEmployeeClear:
                    setProcessEmployee.setText("");
                    break;
                case R.id.setProcessEmployeeClear2:
                    setProcessEmployee2.setText("");
                    break;
                case R.id.setProcessEmployeeClear3:
                    setProcessEmployee3.setText("");
                    break;
                case R.id.setProcessEmployeeClear4:
                    setProcessEmployee4.setText("");
                    break;
                case R.id.setProcessEmployeeClear5:
                    setProcessEmployee5.setText("");
                    break;
                case R.id.setProcessEmployeeClear6:
                    setProcessEmployee6.setText("");
                    break;
                case R.id.btnSave:
                    if(checkText()){
                        if(!checkProcess()){
                            MyToast.myShow(SetProcessActivity.this,"连线生产,工艺需连续",2,0);
                        }else{
                            updateTaskData();
                        }
                    }else{
                        MyToast.myShow(SetProcessActivity.this,"设备和人员必须输入",2,0);
                    }
                    break;
                case R.id.btnUpdateEmployee:
                    setProcessEmployee();
                    break;
            }
        }
    }

    /**
    *描述: 选择连线生产工序
    *日期：2022/9/29
    **/
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            CheckBox checkBoxSel = view.findViewById(R.id.checkBoxSel);
            checkBoxSel.toggle();
            String sIsCheck = "N";

            for(int j=0;j<mapResponseList.size();j++){
                if(i==j){
                    if(checkBoxSel.isChecked()){
                        sIsCheck = "Y";
                    }else{
                        sIsCheck = "N";
                    }
                }else{
                    sIsCheck = (String)mapResponseList.get(j).get("Select");
                    if(sIsCheck.equals("")||sIsCheck.isEmpty()){
                        sIsCheck = "N";
                    }
                }
                mapResponseList.get(j).put("Select",sIsCheck);
            }

            //更新人员显示
            setProcessIndex.setText(String.valueOf(i));

        }
    }

    /**
    *描述: 检核输入内容是否完整
    *日期：2022/9/29
    **/
    private boolean checkText(){
        String sDevice = setProcessDevice.getText().toString();
        String sEmployee = setProcessEmployee.getText().toString();
        String sEmployee2 = setProcessEmployee2.getText().toString();
        String sEmployee3 = setProcessEmployee3.getText().toString();
        String sEmployee4 = setProcessEmployee4.getText().toString();
        String sEmployee5 = setProcessEmployee5.getText().toString();
        String sEmployee6 = setProcessEmployee6.getText().toString();

        if(sDevice.equals("")||sDevice.isEmpty()){
            return false;
        }else{
            if(sEmployee.equals("")&&sEmployee.isEmpty()&&sEmployee2.equals("")&&sEmployee2.isEmpty()&&sEmployee3.equals("")&&sEmployee3.isEmpty()&&sEmployee4.equals("")&&sEmployee4.isEmpty()&&sEmployee5.equals("")&&sEmployee5.isEmpty()&&sEmployee6.equals("")&&sEmployee6.isEmpty()){
                return false;
            }
        }

        return true;
    }

    /**
    *描述: 获取设置人员
    *日期：2022/10/12
    **/
    private String getEmployee(){
        String sEmp="";
        String[] textView = new String[]{setProcessEmployee.getText().toString(), setProcessEmployee2.getText().toString(), setProcessEmployee3.getText().toString(), setProcessEmployee4.getText().toString(),setProcessEmployee5.getText().toString(),setProcessEmployee6.getText().toString()};

        for(int i=0;i<textView.length;i++){
            if(!textView[i].equals("")&&!textView[i].isEmpty()){
                if(sEmp.equals("")||sEmp.isEmpty()){
                    sEmp = textView[i];
                }else{
                    sEmp = sEmp+"/"+textView[i];
                }
            }
        }

        return sEmp;
    }

    /**
    *描述: 设置连线工序生产人员
    *日期：2022/10/11
    **/
    private void setProcessEmployee(){
        String sIndex = setProcessIndex.getText().toString();
        if(sIndex.equals("")||sIndex.isEmpty()){
            MyToast.myShow(SetProcessActivity.this,"请先选择要设置人员的工序",2,0);
        }else{
            if(getEmployee().isEmpty()||getEmployee().equals("")){
                MyToast.myShow(SetProcessActivity.this,"请先选择人员",2,0);
            }else{
                String sDevices = setProcessDevice.getText().toString();
                if(sDevices.isEmpty()||sDevices.equals("")){
                    MyToast.myShow(SetProcessActivity.this,"请先选择设备",2,0);
                }else{
                    int iIndex = Integer.parseInt(sIndex);
                    processListAdapter.updateData(iIndex,setProcessView,getEmployee(),sDevices);
                }
            }
        }

        //清除设置人员
        setProcessEmployee.setText("");
        setProcessEmployee2.setText("");
        setProcessEmployee3.setText("");
        setProcessEmployee4.setText("");
        setProcessEmployee5.setText("");
        setProcessEmployee6.setText("");

        //清除设备
        setProcessDevice.setText("");
    }

    /**
    *描述: 检核人员是否选择
    *日期：2022/10/12
    **/


    /**
    *描述: 检核连线工艺是否正确,主要检核是否为连续工艺
    *日期：2022/9/27
    **/
    private boolean checkProcess(){
        boolean isSuccess = false;
        String sFirstProcess = setProcessProcess.getText().toString();

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
     *描述: 选择设备清单
     *日期：2022/7/17
     **/
    public void openSearchSelectDialog(TextView textView, String title, List<String> mDatas) {
        DeviceListDialog.Builder alert = new DeviceListDialog.Builder(SetProcessActivity.this);
        alert.setListData(mDatas);
        alert.setTitle(title);
        alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
            @Override
            public void onSelected(String info) {
                textView.setText(info);
            }
        });
        DeviceListDialog mDialog = alert.show();
        //设置Dialog 尺寸
        mDialog.setDialogWindowAttr(0.8, 0.8, SetProcessActivity.this);
    }

    /**
     *描述: 初始化选择数据
     *日期：2022/7/17
     **/
    private List<String> initData(String strType,String strwhere) {
        List<String> mDatas = new ArrayList<>();

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "StockGet";

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
                mapResponseDeviceList = t100ServiceHelper.getT100JsonDeviceData(strResponse,"stockinfo");

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
                            if(strType.equals("4")){
                                //人员信息
                                sData = mapResponseDeviceList.get(i).get("DeviceId").toString()+":"+mapResponseDeviceList.get(i).get("Device").toString();
                            }else{
                                sData = mapResponseDeviceList.get(i).get("DeviceId").toString();
                            }
                            mDatas.add(sData);
                        }
                    }
                }else{
                    MyToast.myShow(SetProcessActivity.this,statusDescription,0,0);
                }
            }
        });

        return mDatas;
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
                String webServiceName = "ItemInfoGet";
                String strType = "7";
                String strwhere = "";

                //查询条件
                String sProductCode = setProcessProductCode.getText().toString();
                int iIndex = sProductCode.indexOf("/");
                if(iIndex>-1){
                    String[] arrayProductCode = sProductCode.split("/");
                    for(int i=0;i<arrayProductCode.length;i++){
                        if(strwhere.equals("")||strwhere.isEmpty()){
                            strwhere = "'"+arrayProductCode[i]+"'";
                        }else{
                            strwhere = strwhere+","+"'"+arrayProductCode[i]+"'";
                        }
                    }
                }else{
                    strwhere = "'"+sProductCode+"'";
                }
                String sCondition = " ecbb001 in ("+strwhere+") and ecbb003>"+setProcessProcessId.getText().toString();

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+ strType +"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+ sCondition +"\"/&gt;\n"+
                        "&lt;Field name=\"qrcode\" value=\""+ "" +"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonItemProcessData(strResponse,"iteminfo");

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
                    if(mapResponseList.size()>0){
                        processListAdapter = new ProcessListAdapter(mapResponseList,getApplicationContext());
                        setProcessView.setAdapter(processListAdapter);
                    }

                }else{
                    MyToast.myShow(SetProcessActivity.this,statusDescription,0,0);
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
    *描述: 生成XML数据
    *日期：2022/9/16
    **/
    private void genXml(){
        //生成数据集合
        String status = "Y";
        String action = "updtask";
        String sDevice = setProcessDevice.getText().toString();
        String sFlag = "INIT";
        String strEmployee = getEmployee();

        mRecordSet = "&lt;Master name=\"sfaauc_t\" node_id=\"1\"&gt;\n"+
                "&lt;Record&gt;\n"+
                "&lt;Field name=\"sfaaucsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                "&lt;Field name=\"sfaaucent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                "&lt;Field name=\"sfaaucmodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                "&lt;Field name=\"sfaaucdocno\" value=\""+ strDocno +"\"/&gt;\n"+  //工单单号
                "&lt;Field name=\"sfaaucdocdt\" value=\""+ strPlanDate +"\"/&gt;\n"+  //单据日期
                "&lt;Field name=\"sfaauc007\" value=\""+ strProcessId +"\"/&gt;\n"+  //工序项次
                "&lt;Field name=\"sfaauc008\" value=\""+ strProcess +"\"/&gt;\n"+  //工序号
                "&lt;Field name=\"sfaauc009\" value=\""+ sDevice +"\"/&gt;\n"+  //机器编号
                "&lt;Field name=\"sfaauc002\" value=\""+ strEmployee +"\"/&gt;\n"+  //生产人员
                "&lt;Field name=\"sfaauc001\" value=\""+ strVersion +"\"/&gt;\n"+  //版本
                "&lt;Field name=\"sfaauc011\" value=\""+ strGroupId +"\"/&gt;\n"+  //班次
                "&lt;Field name=\"sfaauc021\" value=\""+ sFlag +"\"/&gt;\n"+  //连线标识
                "&lt;Field name=\"sfaauc023\" value=\""+ strConnect +"\"/&gt;\n"+  //是否连线
                "&lt;Field name=\"sfaaucstus\" value=\""+ status +"\"/&gt;\n"+  //状态码
                "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //执行动作
                "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                "&lt;Record&gt;\n"+
                "&lt;Field name=\"sfaaucseq\" value=\"1.0\"/&gt;\n"+
                "&lt;/Record&gt;\n"+
                "&lt;/Detail&gt;\n"+
                "&lt;Memo/&gt;\n"+
                "&lt;Attachment count=\"0\"/&gt;\n"+
                "&lt;/Record&gt;\n"+
                "&lt;/Master&gt;\n";

        int iSeq = 2;
        for(int i= 0;i< mapResponseList.size();i++){
            String sCheck = (String)mapResponseList.get(i).get("Select");
            if(sCheck.equals("Y")) {
                sFlag = "END";
                String sProcessId = (String)mapResponseList.get(i).get("ProcessId");
                String sProcess = (String)mapResponseList.get(i).get("Process");
                String sEmp = (String)mapResponseList.get(i).get("Employee");
                if(sEmp.equals("")||sEmp.isEmpty()){
                    sEmp = strEmployee;
                }

                String sProcessDevice = (String)mapResponseList.get(i).get("Devices");
                if(sEmp.equals("")||sEmp.isEmpty()){
                    sProcessDevice = sDevice;
                }

                //检核是否为最后序
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

                mRecordSet = mRecordSet + "&lt;Master name=\"sfaauc_t\" node_id=\""+iSeq+"\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"sfaaucsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sfaaucent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"sfaaucmodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"sfaaucdocno\" value=\""+ strDocno +"\"/&gt;\n"+  //工单单号
                        "&lt;Field name=\"sfaaucdocdt\" value=\""+ strPlanDate +"\"/&gt;\n"+  //单据日期
                        "&lt;Field name=\"sfaauc007\" value=\""+ sProcessId +"\"/&gt;\n"+  //工序项次
                        "&lt;Field name=\"sfaauc008\" value=\""+ sProcess +"\"/&gt;\n"+  //工序号
                        "&lt;Field name=\"sfaauc009\" value=\""+ sProcessDevice +"\"/&gt;\n"+  //机器编号
                        "&lt;Field name=\"sfaauc002\" value=\""+ sEmp +"\"/&gt;\n"+  //生产人员
                        "&lt;Field name=\"sfaauc001\" value=\""+ strVersion +"\"/&gt;\n"+  //版本
                        "&lt;Field name=\"sfaauc011\" value=\""+ strGroupId +"\"/&gt;\n"+  //班次
                        "&lt;Field name=\"sfaauc021\" value=\""+ sFlag +"\"/&gt;\n"+  //连线标识
                        "&lt;Field name=\"sfaauc023\" value=\""+ strConnect +"\"/&gt;\n"+  //是否连线
                        "&lt;Field name=\"sfaaucstus\" value=\""+ status +"\"/&gt;\n"+  //状态码
                        "&lt;Field name=\"act\" value=\""+ action +"\"/&gt;\n"+  //执行动作
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
        genXml();

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
                }else{
                    MyToast.myShow(SetProcessActivity.this, statusDescription, 0, 1);
                }

                loadingDialog.dismiss();
            }
        });
    }
}