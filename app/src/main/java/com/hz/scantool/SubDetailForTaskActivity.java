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
    private String strWhere,strWhereProduct,strFlag;
    private boolean isLoadMore;
    private int iRows,iEveryRow,iCount;
    private int iStartCount,iEndCount;

    private Button btnSave,btnCancel,btnSetDevice,btnFlag1,btnFlag2,btnFlag3,btnQuery,btnClear;
    private TextView subTaskProductCode,subTaskProductName,subTaskProductModels,subTaskProcessId,subTaskProcess;
    private TextView subTaskProductDocno,subTaskVersion,subTaskDevice,subTaskPlanDate,subTaskGroupId,subTaskGroup;
    private TextView subTaskEmployee,subTaskEmployee2,subTaskEmployee3,subTaskEmployee4,subTaskEmployee5,subTaskEmployee6;
    private ImageButton subTaskEmployeeClear,subTaskEmployeeClear2,subTaskEmployeeClear3,subTaskEmployeeClear4,subTaskEmployeeClear5,subTaskEmployeeClear6;
    private ProgressBar progressBar;
//    private ListView subTaskView;
    private LoadListView subTaskLoadView;
    private SubAdapter subAdapter;
    private ScrollView viewBasic;
    private EditText inputDevice,inputProductName;
    private CheckBox checkBoxThree,checkBoxSeven;

    private List<Map<String,Object>> mapResponseList,mapResponseStatus,mapResponseDeviceList,mapResponseDeviceEmpList;
    private List<Map<String,Object>> mSearchList;
    private LoadingDialog loadingDialog;
    private List<String> mDataDevice,mDataEmployee,mDataPlanEmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_for_task);

        //初始化
        initBundle();
        initRows();
        initView();

        //初始化选择数据源
        mDataDevice = initData("3"," 1=1");
        mDataEmployee = initData("4"," 1=1");

        //初始化数据
        strFlag = "N";
        setWhereCondition("N");
        getSubListData("11",strWhere,strFlag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sub_menu_print,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
//            case R.id.action_scan:
//                //调用zxing扫码界面
//                IntentIntegrator intentIntegrator = new IntentIntegrator(SubDetailForTaskActivity.this);
//                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
//                //开始扫描
//                intentIntegrator.initiateScan();
//                break;
            case android.R.id.home:
                finish();
                break;
            case R.id.action_print:
                Intent intent = new Intent(SubDetailForTaskActivity.this,SurplusMaterialListActivity.class);
                startActivity(intent);
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
        subTaskProductCode = findViewById(R.id.subTaskProductCode);
        subTaskProductName = findViewById(R.id.subTaskProductName);
        subTaskProductModels = findViewById(R.id.subTaskProductModels);
        subTaskProcessId = findViewById(R.id.subTaskProcessId);
        subTaskProcess = findViewById(R.id.subTaskProcess);
        subTaskProductDocno = findViewById(R.id.subTaskProductDocno);
        subTaskPlanDate = findViewById(R.id.subTaskPlanDate);
        subTaskGroupId = findViewById(R.id.subTaskGroupId);
        subTaskGroup = findViewById(R.id.subTaskGroup);
        subTaskVersion = findViewById(R.id.subTaskVersion);
        subTaskEmployee = findViewById(R.id.subTaskEmployee);
        subTaskEmployee2 = findViewById(R.id.subTaskEmployee2);
        subTaskEmployee3 = findViewById(R.id.subTaskEmployee3);
        subTaskEmployee4 = findViewById(R.id.subTaskEmployee4);
        subTaskEmployee5 = findViewById(R.id.subTaskEmployee5);
        subTaskEmployee6 = findViewById(R.id.subTaskEmployee6);
        subTaskEmployeeClear = findViewById(R.id.subTaskEmployeeClear);
        subTaskEmployeeClear2 = findViewById(R.id.subTaskEmployeeClear2);
        subTaskEmployeeClear3 = findViewById(R.id.subTaskEmployeeClear3);
        subTaskEmployeeClear4 = findViewById(R.id.subTaskEmployeeClear4);
        subTaskEmployeeClear5 = findViewById(R.id.subTaskEmployeeClear5);
        subTaskEmployeeClear6 = findViewById(R.id.subTaskEmployeeClear6);
        subTaskDevice = findViewById(R.id.subTaskDevice);
        inputDevice = findViewById(R.id.inputDevice);
        inputProductName = findViewById(R.id.inputProductName);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnSetDevice = findViewById(R.id.btnSetDevice);
        btnFlag1 = findViewById(R.id.btnFlag1);
        btnFlag2 = findViewById(R.id.btnFlag2);
        btnFlag3 = findViewById(R.id.btnFlag3);
        progressBar = findViewById(R.id.progressBar);
//        subTaskView = findViewById(R.id.subTaskView);
        subTaskLoadView = findViewById(R.id.subTaskLoadView);
        viewBasic = findViewById(R.id.viewBasic);
        btnQuery = findViewById(R.id.btnQuery);
        btnClear = findViewById(R.id.btnClear);
        checkBoxThree = findViewById(R.id.checkBoxThree);
        checkBoxSeven = findViewById(R.id.checkBoxSeven);

        //初始化控件显示
        showView(false);

        //定义事件
        btnSave.setOnClickListener(new commandClickListener());
        btnCancel.setOnClickListener(new commandClickListener());
        subTaskEmployee.setOnClickListener(new commandClickListener());
        subTaskEmployee2.setOnClickListener(new commandClickListener());
        subTaskEmployee3.setOnClickListener(new commandClickListener());
        subTaskEmployee4.setOnClickListener(new commandClickListener());
        subTaskEmployee5.setOnClickListener(new commandClickListener());
        subTaskEmployee6.setOnClickListener(new commandClickListener());
        subTaskEmployeeClear.setOnClickListener(new commandClickListener());
        subTaskEmployeeClear2.setOnClickListener(new commandClickListener());
        subTaskEmployeeClear3.setOnClickListener(new commandClickListener());
        subTaskEmployeeClear4.setOnClickListener(new commandClickListener());
        subTaskEmployeeClear5.setOnClickListener(new commandClickListener());
        subTaskEmployeeClear6.setOnClickListener(new commandClickListener());
        btnSetDevice.setOnClickListener(new commandClickListener());
        btnFlag1.setOnClickListener(new commandClickListener());
        btnFlag2.setOnClickListener(new commandClickListener());
        btnFlag3.setOnClickListener(new commandClickListener());
//        subTaskView.setOnItemClickListener(new listItemClickListener());
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
    *描述: 显示隐藏控件
    *日期：2022/7/19
    **/
    private void showView(boolean isOpen){
        if(isOpen){
            viewBasic.setVisibility(View.VISIBLE);
        }else{
            viewBasic.setVisibility(View.GONE);
        }
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
            subAdapter.notifyDataSetChanged();

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
            String sStatus = txtSubStatus.getText().toString();
            String sConnect = txtProcessEnd.getText().toString();

            if(sStatus.equals("Y")){
                showView(false);
                MyToast.myShow(SubDetailForTaskActivity.this,"命令已启用,无法变更设备和人员",0,0);
            }else{
                //连线生产禁止直接启用
                if(sConnect.equals("Y")){
                    showView(false);
                    MyToast.myShow(SubDetailForTaskActivity.this,"连线任务,请使用连线生产功能启用",0,0);
                }else{
                    //初始化控件显示
                    showView(true);

                    //人员显示
                    showEmployee(txtEmployee.getText().toString());

                    subTaskProductCode.setText(txtProductCode.getText().toString());
                    subTaskProductName.setText(txtProductName.getText().toString());
                    subTaskProductModels.setText(txtProductModels.getText().toString());
                    subTaskProcessId.setText(txtProcessId.getText().toString());
                    subTaskProcess.setText(txtProcess.getText().toString());
                    subTaskProductDocno.setText(txtDocno.getText().toString());
                    subTaskVersion.setText(txtVersion.getText().toString());
                    subTaskDevice.setText(txtDevice.getText().toString());
                    subTaskPlanDate.setText(txtPlanDate.getText().toString());
                    subTaskGroupId.setText(txtGroupId.getText().toString());
                    subTaskGroup.setText(txtGroup.getText().toString());
                }
            }
        }
    }

    /**
    *描述: 获取人员显示
    *日期：2022/7/20
    **/
    private void showEmployee(String sEmployee){
        subTaskEmployee.setText("");
        subTaskEmployee2.setText("");
        subTaskEmployee3.setText("");
        subTaskEmployee4.setText("");
        subTaskEmployee5.setText("");
        subTaskEmployee6.setText("");
        int index = sEmployee.indexOf("/");
        if(index!=-1){
            String[] arrEmployee = sEmployee.split("/");
            TextView textView;
            int[] empId = new int[]{R.id.subTaskEmployee, R.id.subTaskEmployee2, R.id.subTaskEmployee3, R.id.subTaskEmployee4,R.id.subTaskEmployee5,R.id.subTaskEmployee6};
            for(int j=0;j<arrEmployee.length;j++){
                textView = findViewById(empId[j]);
                textView.setText(arrEmployee[j]);
            }
        }else{
            subTaskEmployee.setText(sEmployee);
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
    *描述: 检查人员和设备是否填写完整
    *日期：2022/10/31
    **/
    private boolean checkSave(){
        boolean isSuccess = true;
        strMessage = "人员不可为空";
        String[] textView = new String[]{subTaskEmployee.getText().toString(), subTaskEmployee2.getText().toString(), subTaskEmployee3.getText().toString(), subTaskEmployee4.getText().toString(),subTaskEmployee5.getText().toString(),subTaskEmployee6.getText().toString()};

        //检查设备是否录入
        String sDevice = subTaskDevice.getText().toString();
        if(sDevice.equals("")&&sDevice.isEmpty()){
            strMessage = "设备不可为空";
            return false;
        }

        //检查人员是否录入
        for(int i=0;i<textView.length;i++){
            if(!textView[i].equals("")&&!textView[i].isEmpty()){
                return true;
            }else{
                isSuccess = false;
            }
        }

        return isSuccess;
    }

    /**
    *描述: 按钮事件实现
    *日期：2022/7/19
    **/
    private class commandClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnSave:
                    if(checkSave()){
                        updateTaskData(1,view,"upddevice","Y");
                    }else{
                        MyToast.myShow(SubDetailForTaskActivity.this,strMessage,0,0);
                    }
                    break;
                case R.id.btnCancel:
                    //隐藏详细页
                    showView(false);
                    break;
                case R.id.subTaskEmployee:
                    openSearchSelectDialog(subTaskEmployee,"请选择操作工",mDataEmployee);
                    break;
                case R.id.subTaskEmployee2:
                    openSearchSelectDialog(subTaskEmployee2,"请选择操作工",mDataEmployee);
                    break;
                case R.id.subTaskEmployee3:
                    openSearchSelectDialog(subTaskEmployee3,"请选择操作工",mDataEmployee);
                    break;
                case R.id.subTaskEmployee4:
                    openSearchSelectDialog(subTaskEmployee4,"请选择操作工",mDataEmployee);
                    break;
                case R.id.subTaskEmployee5:
                    openSearchSelectDialog(subTaskEmployee5,"请选择操作工",mDataEmployee);
                    break;
                case R.id.subTaskEmployee6:
                    openSearchSelectDialog(subTaskEmployee6,"请选择操作工",mDataEmployee);
                    break;
                case R.id.subTaskEmployeeClear:
                    subTaskEmployee.setText("");
                    break;
                case R.id.subTaskEmployeeClear2:
                    subTaskEmployee2.setText("");
                    break;
                case R.id.subTaskEmployeeClear3:
                    subTaskEmployee3.setText("");
                    break;
                case R.id.subTaskEmployeeClear4:
                    subTaskEmployee4.setText("");
                    break;
                case R.id.subTaskEmployeeClear5:
                    subTaskEmployee5.setText("");
                    break;
                case R.id.subTaskEmployeeClear6:
                    subTaskEmployee6.setText("");
                    break;
                case R.id.btnSetDevice:
                    openSearchSelectDialog(subTaskDevice,"请选择设备",mDataDevice);
                    break;
                case R.id.btnFlag1:  //未启用
                    showView(false);
                    initRows();
                    initList();
                    btnFlag1.setSelected(true);
                    btnFlag2.setSelected(false);
                    btnFlag3.setSelected(false);
                    setWhereCondition("N");
                    getSubListData("11",strWhere,"N");
                    break;
                case R.id.btnFlag2: //已启用
                    showView(false);
                    initRows();
                    initList();
                    btnFlag1.setSelected(false);
                    btnFlag2.setSelected(true);
                    btnFlag3.setSelected(false);
                    setWhereCondition("Y");
                    getSubListData("11",strWhere,"Y");
                    break;
                case R.id.btnFlag3: //已中止
                    showView(false);
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

    private void initList(){
        if(mapResponseList!=null){
            mapResponseList.clear();
        }
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
        subTaskDevice.setText(qrContent.trim());

//        //依据设备自动带出预排人员
//        showDeviceEmpData();
    }

    /**
     *描述: 选择设备清单
     *日期：2022/7/17
     **/
    public void openSearchSelectDialog(TextView textView, String title, List<String> mDatas) {
        DeviceListDialog.Builder alert = new DeviceListDialog.Builder(SubDetailForTaskActivity.this);
        alert.setListData(mDatas);
        alert.setTitle(title);
        alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
            @Override
            public void onSelected(String info) {
                textView.setText(info);

//                if(textView == subTaskDevice){
//                    //依据设备自动带出预排人员
//                    showDeviceEmpData();
//                }
            }
        });
        DeviceListDialog mDialog = alert.show();
        //设置Dialog 尺寸
        mDialog.setDialogWindowAttr(0.8, 0.8, SubDetailForTaskActivity.this);
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
                    MyToast.myShow(SubDetailForTaskActivity.this,"执行接口错误",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailForTaskActivity.this,e.getMessage(),0,0);
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
                    MyToast.myShow(SubDetailForTaskActivity.this,statusDescription,0,0);
                }
            }
        });

        return mDatas;
    }

    /**
    *描述: 依据设备自动带出之前派工人员
    *日期：2022/7/20
    **/
    private void showDeviceEmpData() {

        Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {

                //初始化T100服务名
                String webServiceName = "StockGet";
                String strType = "5";
                String strwhere = " sfaauc009='"+subTaskDevice.getText().toString()+"'";

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
                mapResponseDeviceEmpList = t100ServiceHelper.getT100JsonDeviceData(strResponse,"stockinfo");

                e.onNext(mapResponseStatus);
                if(mapResponseDeviceEmpList.size()>0){
                    e.onNext(mapResponseDeviceEmpList);
                }
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
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseDeviceEmpList.size()> 0) {
                        //显示单头数据
                        String sDeviceEmp = "";
                        for(int i=0;i<mapResponseDeviceEmpList.size();i++){
                            sDeviceEmp = mapResponseDeviceEmpList.get(i).get("DeviceId").toString();
                        }

                        //更新UI字段显示
                        showEmployee(sDeviceEmp);
                    }
                }else{
                    MyToast.myShow(SubDetailForTaskActivity.this,statusDescription,0,0);
                }
            }
        });
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
                MyToast.myShow(SubDetailForTaskActivity.this,e.getMessage(),0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList!=null){
//                        if(mSearchList==null){
//                            mSearchList = new ArrayList<Map<String,Object>>();
//                        }else{
//                            mSearchList.clear();
//                        }

//                        //获取查询条件
//                        String sInputProductName = inputProductName.getText().toString();
//
//                        //按照查询条件筛选
//                        if(sInputProductName.equals("")&&sInputProductName.isEmpty()){
//                            mSearchList = mapResponseList;
//                            Log.i("TaskActivity","sInputProductName:"+sInputProductName);
//                        }else{
//                            if(mapResponseList.size()>0){
//                                for (int i = 0; i < mapResponseList.size(); i++) {
//                                    String sFlag = "Y";
//                                    String sProductName = (String)mapResponseList.get(i).get("ProductName");
//                                    int indexProduct = sProductName.indexOf(sInputProductName);
//
//                                    //存在匹配的数据
//                                    if(indexProduct==-1){
//                                        sFlag = "N";
//                                        continue;
//                                    }
//
//                                    if(sFlag.equals("Y")){
//                                        mSearchList.add(mapResponseList.get(i));
//                                        Log.i("TaskActivity","sProductName:"+sProductName);
//                                    }
//                                }
//                            }
//                        }

                        //填充清单
//                        if(mSearchList.size()>0){
                            subAdapter = new SubAdapter(mapResponseList,getApplicationContext(),mStartTaskClickListener,mStopTaskClickListener,mSetTaskClickListener,"ZZ");
                            subTaskLoadView.setAdapter(subAdapter);

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
//                        }else{
//                            subAdapter = new SubAdapter(mapResponseList,getApplicationContext(),mStartTaskClickListener,mStopTaskClickListener,"ZZ");
//                            subTaskView.setAdapter(subAdapter);
//                            MyToast.myShow(SubDetailForTaskActivity.this,"无数据",0,0);
//                        }

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
            }
        });
    }

    /**
    *描述: 启用命令
    *日期：2022/7/19
    **/
    private SubAdapter.StartTaskClickListener mStartTaskClickListener = new SubAdapter.StartTaskClickListener() {

        @Override
        public void StartTaskClick(int position, View view) {
            String strstatus = subAdapter.getItemValue(position,"Status");
            String strProcessEnd = subAdapter.getItemValue(position,"ProcessEnd");

            if(strProcessEnd.equals("Y")){
                MyToast.myShow(SubDetailForTaskActivity.this,"连线生产不可直接启用,请点击连线生产启用",0,0);
            }else{
                if(strstatus.equals("Y")){
                    MyToast.myShow(SubDetailForTaskActivity.this,"此命令已启动,不需重复启动",0,0);
                }else{
                    updateTaskData(position,view,"updstatus","Y");
                }
            }
        }
    };

    /**
     *描述: 中止命令
     *日期：2022/7/19
     **/
    private SubAdapter.StopTaskClickListener mStopTaskClickListener = new SubAdapter.StopTaskClickListener() {


        @Override
        public void StopTaskClick(int position, View view) {
            String strstatus = subAdapter.getItemValue(position,"Status");

            if(strstatus.equals("C")){
                MyToast.myShow(SubDetailForTaskActivity.this,"此命令已中止,不需重复中止",0,0);
            }else{
                updateTaskData(position,view,"updstatus","C");
            }
        }
    };

    /**
    *描述: 连线生产
    *日期：2022/9/14
    **/
    private SubAdapter.SetTaskClickListener mSetTaskClickListener = new SubAdapter.SetTaskClickListener() {
        @Override
        public void SetTaskClick(int position, View view) {
            //获取控件
            Button listSubBtnSet = view.findViewById(R.id.listSubBtnSet);

            //初始化值
            String strProductName = subAdapter.getItemValue(position,"ProductName");
            String strProductCode = subAdapter.getItemValue(position,"ProductCode");
            String strProductModels = subAdapter.getItemValue(position,"ProductModels");
            String strProcessId = subAdapter.getItemValue(position,"ProcessId");
            String strProcess = subAdapter.getItemValue(position,"Process");
            String strDevice = subAdapter.getItemValue(position,"Device");
            String strDocno = subAdapter.getItemValue(position,"Docno");
            String strVersion = subAdapter.getItemValue(position,"Version");
            String strFlag = subAdapter.getItemValue(position,"Flag");
            String strPlanDate = subAdapter.getItemValue(position,"PlanDate");
            String strGroupId = subAdapter.getItemValue(position,"GroupId");
            String strGroup = subAdapter.getItemValue(position,"Group");
            String strProcessEnd = subAdapter.getItemValue(position,"ProcessEnd");
            String strProcessInitId = subAdapter.getItemValue(position,"ProcessInitId");
            String strProcessInit = subAdapter.getItemValue(position,"ProcessInit");

            //连线标识
            if(strProcessEnd.equals("Y")){
                strProcessId = strProcessInitId;
                strProcess = strProcessInit;
                strDevice = "";
            }

            Intent intent = new Intent(SubDetailForTaskActivity.this,SetProcessActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("title",listSubBtnSet.getText().toString());
            bundle.putString("productName",strProductName);
            bundle.putString("productCode",strProductCode);
            bundle.putString("productModels",strProductModels);
            bundle.putString("processId",strProcessId);
            bundle.putString("process",strProcess);
            bundle.putString("device",strDevice);
            bundle.putString("docno",strDocno);
            bundle.putString("version",strVersion);
            bundle.putString("flag",strFlag);
            bundle.putString("plandate",strPlanDate);
            bundle.putString("groupid",strGroupId);
            bundle.putString("group",strGroup);
            bundle.putString("connect",strProcessEnd);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };

    /**
    *描述: 更新派工单数据
    *日期：2022/7/19
    **/
    private void updateTaskData(int position, View view,String action,String status){
        //显示进度条
        loadingDialog = new LoadingDialog(SubDetailForTaskActivity.this,"数据提交中",R.drawable.dialog_loading);
        loadingDialog.show();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ProductTaskUpdate";

                String strDocno = "";
                String strProcessId = "";
                String strProcess = "";
                String strDevice = "";
                String strVersion = "";
                String strEmployee = "";
                String strPlanDate = "";
                String strGroupId = "";
                String strProcessEnd = "";

                if(action.equals("updstatus")){
                    strDocno = subAdapter.getItemValue(position,"Docno");
                    strProcessId = subAdapter.getItemValue(position,"ProcessId");
                    strProcess = subAdapter.getItemValue(position,"Process");
                    strDevice = subAdapter.getItemValue(position,"Device");
                    strVersion = subAdapter.getItemValue(position,"Version");
                    strEmployee = subAdapter.getItemValue(position,"Employee");
                    strPlanDate = subAdapter.getItemValue(position,"PlanDate");
                    strGroupId = subAdapter.getItemValue(position,"GroupId");
                    strProcessEnd = subAdapter.getItemValue(position,"ProcessEnd");
                }else{
                    strDocno = subTaskProductDocno.getText().toString();
                    strProcessId = subTaskProcessId.getText().toString();
                    strProcess = subTaskProcess.getText().toString();
                    strDevice = subTaskDevice.getText().toString();
                    strVersion = subTaskVersion.getText().toString();
                    strPlanDate = subTaskPlanDate.getText().toString();
                    strGroupId = subTaskGroupId.getText().toString();
                    strEmployee = subTaskEmployee.getText().toString()+"/"+subTaskEmployee2.getText().toString()+"/"+subTaskEmployee3.getText().toString()+"/"+subTaskEmployee4.getText().toString();
                }

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
                        "&lt;Field name=\"sfaauc002\" value=\""+ strEmployee +"\"/&gt;\n"+  //生产人员
                        "&lt;Field name=\"sfaauc001\" value=\""+ strVersion +"\"/&gt;\n"+  //版本
                        "&lt;Field name=\"sfaauc011\" value=\""+ strGroupId +"\"/&gt;\n"+  //班次
                        "&lt;Field name=\"sfaauc023\" value=\""+ strProcessEnd +"\"/&gt;\n"+  //是否连线
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