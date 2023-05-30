package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.database.HzDb;
import com.hz.scantool.database.QrcodeEntity;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.text.SimpleDateFormat;
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

public class IqcCheckLabelDetailActivity extends AppCompatActivity {

    private static final int CHECKDETAIL = 1002;

    private String strTitle;

    private HzDb hzDb;
    private String dataBaseName = "HzDb";
    private String statusCode,statusDescription;
    private String strQrcode,strDocNo,strProductCode,strProductName,strProductModel,strPlanDate,strQuantity,strStatus;
    private TextView iqcCheckDetailProductCode,iqcCheckDetailProductName,iqcCheckDetailProductModels,iqcCheckDetailQuantity,iqcCheckDetailQrcode,iqcCheckDetailPlanDate,iqcCheckDetailDcono;
    private Button btnIqcCheckDetailCancel,btnIqcCheckDetailFinish;
    private ImageView imageViewResult;
    private LoadingDialog loadingDialog;
    private QrcodeEntity qrcodeEntity;
    private List<Map<String,Object>> mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iqc_check_label_detail);

        //初始化
        initBundle();
        initView();
        initText();
        initListener();
        initDataBase();

    }

    /**
     *描述: 初始化控件
     *日期：2022/11/3
     **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.iqcCheckDetailToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        strTitle = getResources().getString(R.string.iqc_check_title);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        iqcCheckDetailProductCode = findViewById(R.id.iqcCheckDetailProductCode);
        iqcCheckDetailProductName = findViewById(R.id.iqcCheckDetailProductName);
        iqcCheckDetailProductModels = findViewById(R.id.iqcCheckDetailProductModels);
        iqcCheckDetailQuantity = findViewById(R.id.iqcCheckDetailQuantity);
        iqcCheckDetailQrcode = findViewById(R.id.iqcCheckDetailQrcode);
        iqcCheckDetailPlanDate = findViewById(R.id.iqcCheckDetailPlanDate);
        iqcCheckDetailDcono = findViewById(R.id.iqcCheckDetailDcono);
        btnIqcCheckDetailCancel = findViewById(R.id.btnIqcCheckDetailCancel);
        btnIqcCheckDetailFinish = findViewById(R.id.btnIqcCheckDetailFinish);
        imageViewResult = findViewById(R.id.imageViewResult);

    }

    /**
     *描述: 初始化事件
     *日期：2022/11/3
     **/
    private void initListener(){
        btnIqcCheckDetailFinish.setOnClickListener(new btnClickListener());
        btnIqcCheckDetailCancel.setOnClickListener(new btnClickListener());
    }

    /**
     *描述: 初始化传入参数
     *日期：2022/11/3
     **/
    private void initBundle(){

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strQrcode = bundle.getString("qrcode");
        strDocNo = bundle.getString("docNo");
        strProductCode = bundle.getString("productCode");
        strProductName = bundle.getString("productName");
        strProductModel = bundle.getString("productModel");
        strPlanDate = bundle.getString("planDate");
        strQuantity = bundle.getString("quantity");
        strStatus = bundle.getString("status");
    }

    /**
    *描述: 初始化显示值
    *日期：2022/11/4
    **/
    private void initText(){
        iqcCheckDetailProductCode.setText(strProductCode);
        iqcCheckDetailProductName.setText(strProductName);
        iqcCheckDetailProductModels.setText(strProductModel);
        iqcCheckDetailQuantity.setText(strQuantity);
        iqcCheckDetailQrcode.setText(strQrcode);
        iqcCheckDetailPlanDate.setText(strPlanDate);
        iqcCheckDetailDcono.setText(strDocNo);

        if(strStatus.equals("Y")){
            imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_ok));
        }else{
            imageViewResult.setImageDrawable(getResources().getDrawable(R.drawable.detail_status_deal));
        }
    }

    /**
     *描述: 按钮单击事件
     *日期：2022/11/3
     **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnIqcCheckDetailFinish:
                    saveData("Y");
                    break;
                case R.id.btnIqcCheckDetailCancel:
                    finish();
                    break;
            }
        }
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
     *描述: 初始化本地数据库
     *日期：2022/11/3
     **/
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this, HzDb.class,dataBaseName).build();
    }

    /**
     *描述: 保存数据
     *日期：2022/11/3
     **/
    private void saveData(String status){
        //显示进度条
        if(loadingDialog == null){
            loadingDialog = new LoadingDialog(IqcCheckLabelDetailActivity.this,"数据保存中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<String>(){
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {

                //初始化T100服务名:cwssp028
                String webServiceName = "CheckUpdateQc";
                String sCheckType = "UPD_IQC";

                //扫描时间
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
                String currentDate = simpleDateFormat.format(new Date());

                //发送服务器请求
                T100ServiceHelper t100ServiceHelper = new T100ServiceHelper();
                String requestBody = "&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"bcacuc_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcacucsite\" value=\""+ UserInfo.getUserSiteId(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcacucent\" value=\""+UserInfo.getUserEnterprise(getApplicationContext())+"\"/&gt;\n"+
                        "&lt;Field name=\"bcacucmodid\" value=\""+ UserInfo.getUserId(getApplicationContext()) +"\"/&gt;\n"+  //异动人员
                        "&lt;Field name=\"bcacuc001\" value=\""+ strQrcode.trim() +"\"/&gt;\n"+  //条码编号
                        "&lt;Field name=\"bcacuc002\" value=\""+ strProductCode +"\"/&gt;\n"+  //料件编码
                        "&lt;Field name=\"bcacuc003\" value=\""+ strQuantity +"\"/&gt;\n"+  //条码数量
                        "&lt;Field name=\"bcacuc004\" value=\""+ strDocNo +"\"/&gt;\n"+  //扫描批次
                        "&lt;Field name=\"bcacuc005\" value=\""+ sCheckType +"\"/&gt;\n"+  //扫描类别
                        "&lt;Field name=\"bcacuc006\" value=\""+ status +"\"/&gt;\n"+  //状态
                        "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"bcacucseq\" value=\"1.0\"/&gt;\n"+
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
                for(Map<String,Object> mStatus: mapResponseStatus){
                    statusCode = mStatus.get("statusCode").toString();
                    statusDescription = mStatus.get("statusDescription").toString();
                }

                if(statusCode.equals("0")&&status.equals("Y")){
                    float quantity = 0;
                    if(!strQuantity.equals("")&&!strQuantity.isEmpty()){
                        quantity = Float.parseFloat(strQuantity);
                    }
                    qrcodeEntity = new QrcodeEntity(strQrcode,strDocNo,strProductCode,strProductName,strPlanDate,quantity,currentDate);
                    hzDb.qrcodeDao().insert(qrcodeEntity);

                    int iCount = hzDb.qrcodeDao().getCount(strQrcode);
                    if(iCount>0){
                        statusCode = "0";
                        statusDescription = "检验成功";
                    }else{
                        statusCode = "-1";
                        statusDescription = "检验失败,数据未写入";
                    }
                }

                e.onNext(statusCode);
                e.onNext(statusDescription);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    MyToast.myShow(IqcCheckLabelDetailActivity.this,statusDescription,1,0);
                    Intent intent = new Intent();
                    intent.putExtra("result",statusCode);
                    setResult(CHECKDETAIL,intent);
                    IqcCheckLabelDetailActivity.this.finish();
                }else{
                    MyToast.myShow(IqcCheckLabelDetailActivity.this,statusDescription,0,0);
                }

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}