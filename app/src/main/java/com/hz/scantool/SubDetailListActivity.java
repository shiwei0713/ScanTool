package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubListDetailAdapter;
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

public class SubDetailListActivity extends AppCompatActivity {

    private ListView listDetailView;
    private TextView txtSubDetailStock;
    private TextView txtSubDetailDeptId;
    private TextView txtSubDetailDept;
    private TextView txtSubDetailPlanDate;
    private TextView txtSubDetailPosition;
    private ImageView imageViewSubDetailLogo;
    private ProgressBar progressSubDetailBar;

    private String strWhere;
    private String strDocType;
    private int intIndex;
    private String statusCode;
    private String statusDescription;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private SubListDetailAdapter subListDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_detail_list);

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subDetailListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(getResources().getString(R.string.master_action5));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化
        initView();
        initQueryCondition();

        //显示储位清单
        getSubDetailListData();
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

    //初始化控件
    private void initView(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        txtSubDetailStock = findViewById(R.id.txtSubDetailStock);
        txtSubDetailDeptId = findViewById(R.id.txtSubDetailDeptId);
        txtSubDetailDept = findViewById(R.id.txtSubDetailDept);
        txtSubDetailPlanDate = findViewById(R.id.txtSubDetailPlanDate);
        txtSubDetailPosition = findViewById(R.id.txtSubDetailPosition);
        listDetailView = findViewById(R.id.subDetailListView);
        progressSubDetailBar = findViewById(R.id.progressSubDetailBar);
        imageViewSubDetailLogo = findViewById(R.id.imageViewSubDetailLogo);

        txtSubDetailStock.setText(bundle.getString("Stock"));
        txtSubDetailDeptId.setText(bundle.getString("DeptId"));
        txtSubDetailDept.setText(bundle.getString("Dept"));
        txtSubDetailPlanDate.setText(bundle.getString("PlanDate"));
        strDocType = bundle.getString("DocType");

        if(strDocType.equals("1")){
            imageViewSubDetailLogo.setImageDrawable(getResources().getDrawable(R.drawable.sub_detail_inside));
        }else{
            imageViewSubDetailLogo.setImageDrawable(getResources().getDrawable(R.drawable.sub_detail_outside));
        }
    }

    //初始化查询条件
    private void initQueryCondition(){
        intIndex = 4;
//        strWhere = " sfaadocno='"+txtSubDetailDocno.getText().toString()+"'";
        strWhere = " sfaa017 = '"+txtSubDetailDeptId.getText().toString()+"' AND sfaa019 = to_date('"+txtSubDetailPlanDate.getText().toString()+"','YYYY-MM-DD')";
    }

    //获取发料储位明细
    private void getSubDetailListData(){
        //显示进度条
        progressSubDetailBar.setVisibility(View.VISIBLE);

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "AppGetStockLot";

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Parameter&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"enterprise\" value=\""+ UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"site\" value=\""+UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"type\" value=\""+intIndex+"\"/&gt;\n"+
                        "&lt;Field name=\"stock\" value=\""+txtSubDetailStock.getText().toString()+"\"/&gt;\n"+
                        "&lt;Field name=\"where\" value=\""+strWhere+"\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Parameter&gt;\n"+
                        "&lt;Document/&gt;\n";
                String strResponse = t100ServiceHelper.getT100Data(requestBody,webServiceName,getApplicationContext(),"");
                mapResponseList = t100ServiceHelper.getT100JsonStockData(strResponse,"productlist");
                mapResponseStatus = t100ServiceHelper.getT100StatusData(strResponse);

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
                            MyToast.myShow(SubDetailListActivity.this,statusDescription,0,0);
                        }else{
                            int progress = progressSubDetailBar.getProgress();
                            progress = progress + 50;
                            progressSubDetailBar.setProgress(progress);
                        }
                    }
                }else{
                    MyToast.myShow(SubDetailListActivity.this,"无备料数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubDetailListActivity.this,"网络错误",0,0);
            }

            @Override
            public void onComplete() {
                subListDetailAdapter = new SubListDetailAdapter(mapResponseList,getApplicationContext());
                listDetailView.setAdapter(subListDetailAdapter);

                //隐藏滚动条
                progressSubDetailBar.setVisibility(View.GONE);
            }
        });
    }
}