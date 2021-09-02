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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubListAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SubListActivity extends AppCompatActivity {

    private int intIndex;
    private String strTitle;
    private String strWhere;
    private String statusCode;
    private String statusDescription;

    private ListView listView;
    private SubListAdapter subDetailAdapter;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    private Button btnFlag1;
    private Button btnFlag2;
    private Button btnSubQuery;
    private TextView txtSubQueryDeptNameTitle;
    private TextView txtSubLabel1;
    private TextView txtSubLabel2;
    private EditText txtSubQuerybDate;
    private EditText txtSubQueryeDate;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_list);

        //初始化参数
        initBundle();

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

        //初始化控件
        initView();
        initQueryCondition();

        //初始化数据
        getSubListData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏返回按钮事件定义
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //设置查询日期
    private String setQueryDate(int interval){
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,interval);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date date =  calendar.getTime();
        String strDate = simpleDateFormat.format(date);

        return strDate;
    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        intIndex = bundle.getInt("index");
        strTitle = bundle.getString("title");
    }

    private void initView(){
        btnFlag1 = findViewById(R.id.btnFlag1);
        btnFlag2 = findViewById(R.id.btnFlag2);
        btnSubQuery = findViewById(R.id.btnSubQuery);
        txtSubQueryDeptNameTitle = findViewById(R.id.txtSubQueryDeptNameTitle);
        txtSubQuerybDate=findViewById(R.id.txtSubQuerybDate);
        txtSubQueryeDate=findViewById(R.id.txtSubQueryeDate);
        txtSubLabel1 = findViewById(R.id.txtSubLabel1);
        txtSubLabel2 = findViewById(R.id.txtSubLabel2);
        listView = findViewById(R.id.subListView);
        progressBar = findViewById(R.id.progressBar);

        //初始化flag按钮状态
        btnFlag1.setSelected(true);
        btnFlag2.setSelected(false);

        //初始化文本
        if(intIndex == 2){
            txtSubQueryDeptNameTitle.setText(getString(R.string.query_title_dept_in));
        }

        //初始化日期
        txtSubQuerybDate.setText(setQueryDate(1));
        txtSubQueryeDate.setText(setQueryDate(1));

        //绑定事件
        btnFlag1.setOnClickListener(new queryClickListener());
        btnFlag2.setOnClickListener(new queryClickListener());
        btnSubQuery.setOnClickListener(new queryClickListener());
        txtSubLabel1.setOnClickListener(new queryClickListener());
        txtSubLabel2.setOnClickListener(new queryClickListener());
        listView.setOnItemClickListener(new listItemClickListener());
    }

    //初始化查询条件
    private void initQueryCondition(){
        strWhere = "sfaa019 BETWEEN to_date('"+txtSubQuerybDate.getText().toString()+"','YYYY-MM-DD') AND to_date('"+txtSubQueryeDate.getText().toString()+"','YYYY-MM-DD')";
    }

    //按钮单击事件
    private class queryClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnFlag1:
                    btnFlag1.setSelected(true);
                    btnFlag2.setSelected(false);
                    break;
                case R.id.btnFlag2:
                    btnFlag1.setSelected(false);
                    btnFlag2.setSelected(true);
                    break;
                case R.id.btnSubQuery:
                    break;
                case R.id.txtSubLabel1:
                    txtSubQuerybDate.setText(setQueryDate(0));
                    txtSubQueryeDate.setText(setQueryDate(2));
                    break;
                case R.id.txtSubLabel2:
                    txtSubQuerybDate.setText(setQueryDate(0));
                    txtSubQueryeDate.setText(setQueryDate(6));
                    break;
            }

            initQueryCondition();
            getSubListData();
        }
    }

    //行单击事件
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(SubListActivity.this,SubDetailListActivity.class);
            Bundle bundle = new Bundle();

            TextView txtViewDept = view.findViewById(R.id.txtViewDept);
            TextView txtViewDeptId = view.findViewById(R.id.txtViewDeptId);
            TextView txtViewDocno = view.findViewById(R.id.txtViewStock);
            TextView txtViewDate = view.findViewById(R.id.txtViewDate);
            String strDocType = subDetailAdapter.getItem(i,"DocType");

            bundle.putString("Dept",txtViewDept.getText().toString());
            bundle.putString("DeptId",txtViewDeptId.getText().toString());
            bundle.putString("Stock",txtViewDocno.getText().toString());
            bundle.putString("PlanDate",txtViewDate.getText().toString());
            bundle.putString("DocType",strDocType);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private void getSubListData(){
        //显示进度条
        progressBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppWorkOrderListGet";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+intIndex+"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);
                mapResponseList = t100ServiceHelper.getT100JsonWorkOrderData(strResponse,"workorder");

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
                            MyToast.myShow(SubListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = progressBar.getProgress();
                            progress = progress + 50;
                            progressBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubListActivity.this,"无备料数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubListActivity.this,"网络错误",0,0);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onComplete() {
                subDetailAdapter = new SubListAdapter(mapResponseList,getApplicationContext());
                listView.setAdapter(subDetailAdapter);

                progressBar.setVisibility(View.GONE);
            }
        });
    }

}