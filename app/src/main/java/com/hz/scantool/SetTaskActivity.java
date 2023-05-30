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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyAlertDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.TaskListAdapter;
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

public class SetTaskActivity extends AppCompatActivity {

    private String strTitle;
    private String statusCode;
    private String statusDescription,strMessage;
    private String strProductName,strProductModels,strProcessId,strProcess,strPlanDocno,strVersion,strPlanDate,strGroupId,strGroup,strQuantity,strEmployee,strDevice,strDocno,strErrorMsg,strLabel;
    private String strMenuitem,strRestart,strStatus;

    private TextView setTaskProductName,setTaskProductModels,setTaskProcessId,setTaskProcess;
    private TextView setTaskPlanDocno,setTaskVersion,setTaskPlanDate,setTaskGroupId,setTaskGroup;
    private TextView setTaskDocno,setTaskQuantity,setTaskEmployee,setTaskDevice,setTaskMsg;
    private ImageButton setTaskEmployeeClear;
    private ImageView imageViewResult;
    private Button btnSave;
    private LoadingDialog loadingDialog;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapResponseDeviceList;
    private List<String> mDatas,mDeviceDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_task);

        //初始化
        initBundle();
        initView();
        showStatus();
        openSearchSelectDialog();
    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = getString(R.string.set_task_title);
        strProductName = bundle.getString("ProductName");
        strProductModels = bundle.getString("ProductModels");
        strProcessId = bundle.getString("ProcessId");
        strProcess = bundle.getString("Process");
        strPlanDocno = bundle.getString("PlanDocno");
        strVersion = bundle.getString("Version");
        strPlanDate = bundle.getString("PlanDate");
        strGroupId = bundle.getString("GroupId");
        strGroup = bundle.getString("Group");
        strDocno = bundle.getString("Docno");
        strQuantity = bundle.getString("Quantity");
        strEmployee = bundle.getString("Employee");
        strDevice = bundle.getString("Device");
        strMenuitem = bundle.getString("menuitem");
        strErrorMsg = bundle.getString("ErrorMsg");
        strLabel = bundle.getString("Label");
    }

    //初始化控件
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.setTaskToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        setTaskProductName = findViewById(R.id.setTaskProductName);
        setTaskProductModels = findViewById(R.id.setTaskProductModels);
        setTaskProcessId = findViewById(R.id.setTaskProcessId);
        setTaskProcess = findViewById(R.id.setTaskProcess);
        setTaskPlanDocno = findViewById(R.id.setTaskPlanDocno);
        setTaskVersion = findViewById(R.id.setTaskVersion);
        setTaskPlanDate = findViewById(R.id.setTaskPlanDate);
        setTaskGroupId = findViewById(R.id.setTaskGroupId);
        setTaskGroup = findViewById(R.id.setTaskGroup);
        setTaskDocno = findViewById(R.id.setTaskDocno);
        setTaskQuantity = findViewById(R.id.setTaskQuantity);
        setTaskEmployee = findViewById(R.id.setTaskEmployee);
        setTaskDevice = findViewById(R.id.setTaskDevice);
        setTaskEmployeeClear = findViewById(R.id.setTaskEmployeeClear);
        setTaskMsg = findViewById(R.id.setTaskMsg);
        imageViewResult = findViewById(R.id.imageViewResult);
        btnSave = findViewById(R.id.btnSave);

        //初始化值
        setTaskProductName.setText(strProductName);
        setTaskProductModels.setText(strProductModels);
        setTaskProcessId.setText(strProcessId);
        setTaskProcess.setText(strProcess);
        setTaskPlanDocno.setText(strPlanDocno);
        setTaskVersion.setText(strVersion);
        setTaskPlanDate.setText(strPlanDate);
        setTaskGroupId.setText(strGroupId);
        setTaskGroup.setText(strGroup);
        setTaskQuantity.setText(strQuantity);
        setTaskEmployee.setText(strEmployee);
        setTaskDevice.setText(strDevice);
        setTaskDocno.setText(strDocno);

        //定义事件
        btnSave.setOnClickListener(new commandClickListener());
        setTaskEmployee.setOnClickListener(new commandClickListener());
        setTaskDevice.setOnClickListener(new commandClickListener());
        setTaskEmployeeClear.setOnClickListener(new commandClickListener());
    }

    /**
    *描述: 显示任务状态图示
    *日期：2023-05-17
    **/
    private void showStatus(){
        //初始化状态
        switch (strMenuitem){
            case "N":
                imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.no_task));
                btnSave.setText(getResources().getString(R.string.set_task_button_title1));
                strRestart = "N";
                strStatus = "Y";
                break;
            case "Y":
                imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.start_task));
                btnSave.setText(getResources().getString(R.string.set_task_button_title2));
                strRestart = "N";
                strStatus = "C";
                showMessage();
                break;
            case "C":
                imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.stop_task));
                btnSave.setText(getResources().getString(R.string.set_task_button_title1));
                strRestart = "Y";
                strStatus = "Y";
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

        setTaskMsg.setText(sMessage);
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
                    if(checkSave()){
                        updateTaskData();
                    }else{
                        MyAlertDialog.myShowAlertDialog(SetTaskActivity.this,"错误信息",strMessage);
                    }
                    break;
                case R.id.setTaskEmployee:
                    showDialog("4",setTaskEmployee,"请选择操作工",mDatas);
                    break;
                case R.id.setTaskDevice:
                    showDialog("3",setTaskDevice,"请选择设备",mDeviceDatas);
                    break;
                case R.id.setTaskEmployeeClear:
                    setTaskEmployee.setText("");
                    break;
            }
        }
    }

    /**
     *描述: 检查人员和设备是否填写完整
     *日期：2022/10/31
     **/
    private boolean checkSave(){
        boolean isSuccess = true;

        //检查设备是否录入
        String sDevice = setTaskDevice.getText().toString();
        if(sDevice.equals("")&&sDevice.isEmpty()){
            strMessage = "设备不可为空";
            return false;
        }

        //检查人员是否录入
        String sEmployee = setTaskEmployee.getText().toString();
        if(sEmployee.equals("")&&sEmployee.isEmpty()){
            strMessage = "人员不可为空";
            return false;
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
                    MyToast.myShow(SetTaskActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SetTaskActivity.this,e.getMessage(),0,0);
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
                    MyToast.myShow(SetTaskActivity.this,statusDescription,0,0);
                }
            }
        });
    }

    /**
     *描述: 显示对话框
     *日期：2023-05-10
     **/
    private void showDialog(String strType,TextView textView, String title,List<String> mDatas){
        DeviceListDialog.Builder alert = new DeviceListDialog.Builder(SetTaskActivity.this);
        alert.setListData(mDatas);
        alert.setTitle(title);
        alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
            @Override
            public void onSelected(String info) {
                //如果为人员,为多选
                String sInfo = "";
                if(strType.equals("4")){
                    String sText = textView.getText().toString();
                    int iEmp = sText.indexOf(info);
                    if(iEmp<=-1){
                        if(sText.equals("")||sText.isEmpty()){
                            sInfo = info;
                        }else{
                            sInfo = sText+"/"+info;
                        }
                    }else{
                        sInfo = sText;
                    }
                }else{
                    sInfo = info;
                }

                textView.setText(sInfo);
            }
        });
        DeviceListDialog mDialog = alert.show();
        //设置Dialog 尺寸
        mDialog.setDialogWindowAttr(0.8, 0.8, SetTaskActivity.this);
    }

    /**
     *描述: 更新派工单数据
     *日期：2022/7/19
     **/
    private void updateTaskData(){
        //显示进度条
        loadingDialog = new LoadingDialog(SetTaskActivity.this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductTaskUpdate";
                String strProcessEnd = "N";
                String action = "upddevice";

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
                        "&lt;Field name=\"sfaauc009\" value=\""+ setTaskDevice.getText().toString() +"\"/&gt;\n"+  //机器编号
                        "&lt;Field name=\"sfaauc002\" value=\""+ setTaskEmployee.getText().toString() +"\"/&gt;\n"+  //生产人员
                        "&lt;Field name=\"sfaauc001\" value=\""+ strVersion +"\"/&gt;\n"+  //版本
                        "&lt;Field name=\"sfaauc011\" value=\""+ strGroupId +"\"/&gt;\n"+  //班次
                        "&lt;Field name=\"sfaauc014\" value=\""+ strPlanDocno +"\"/&gt;\n"+  //计划单号
                        "&lt;Field name=\"sfaauc023\" value=\""+ strProcessEnd +"\"/&gt;\n"+  //是否连线
                        "&lt;Field name=\"sfaaucstus\" value=\""+ strStatus +"\"/&gt;\n"+  //状态码
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
                    MyToast.myShow(SetTaskActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SetTaskActivity.this,e.getMessage(),0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(SetTaskActivity.this, statusDescription, 1, 1);
                    finish();
                }else{
                    MyToast.myShow(SetTaskActivity.this, statusDescription, 0, 1);
                }

                loadingDialog.dismiss();
            }
        });
    }
}