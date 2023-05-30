package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubMaterialAdapter;
import com.hz.scantool.dialog.DeviceListDialog;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.voghdev.pdfviewpager.library.PDFViewPager;
import es.voghdev.pdfviewpager.library.RemotePDFViewPager;
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter;
import es.voghdev.pdfviewpager.library.remote.DownloadFile;
import es.voghdev.pdfviewpager.library.util.FileUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class ShowMaterial extends AppCompatActivity implements DownloadFile.Listener {

    private String strTitle;
    private String mUrl;
    private String mServer;
    private String mFolder;
    private String mProducName;
    private RemotePDFViewPager remotePDFViewPager;
    private PDFPagerAdapter adapter;
    private LinearLayout remote_pdf_root;
    private ProgressBar progressBar;
    private TextView txtMaterialProductName;
    private TextView txtMaterialUrl,txtMaterialProcess;
    private Button btnMaterialFlag1;
    private Button btnMaterialFlag2;
    private Button btnMaterialFlag3;
    private LoadingDialog loadingDialog;
    private String statusCode;
    private String statusDescription;
    private List<String> mDatas;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_material);

        //初始化
        initBundle();
        initView();
        getProcessData();

        //显示文件
        showFile();
    }

    private void initView(){

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.showMaterialToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        remote_pdf_root = findViewById(R.id.remote_pdf_root);
        txtMaterialProductName = findViewById(R.id.txtMaterialProductName);
        txtMaterialProductName.setText(mProducName);
        txtMaterialUrl = findViewById(R.id.txtMaterialUrl);
        progressBar = findViewById(R.id.progressBar);
        txtMaterialUrl.setText(mUrl);
        txtMaterialProcess = findViewById(R.id.txtMaterialProcess);

        btnMaterialFlag1 = findViewById(R.id.btnMaterialFlag1);
        btnMaterialFlag2 = findViewById(R.id.btnMaterialFlag2);
        btnMaterialFlag3 = findViewById(R.id.btnMaterialFlag3);
        btnMaterialFlag1.setSelected(true);
        btnMaterialFlag2.setSelected(false);
        btnMaterialFlag3.setSelected(false);
        //绑定事件
        btnMaterialFlag1.setOnClickListener(new flagClickListener());
        btnMaterialFlag2.setOnClickListener(new flagClickListener());
        btnMaterialFlag3.setOnClickListener(new flagClickListener());
        txtMaterialProcess.setOnClickListener(new flagClickListener());
    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mServer = bundle.getString("url");
        strTitle = bundle.getString("title");
        mProducName = bundle.getString("product_name");
        mFolder = "/materials/";
        mUrl = mServer + mFolder + mProducName + ".pdf";
    }

    private void showFile(){

        if(adapter!=null) {
            adapter.close();
            adapter = null;
            remote_pdf_root.removeAllViewsInLayout();
        }

        final Context context = this;
        final DownloadFile.Listener listener = this;
        remotePDFViewPager = new RemotePDFViewPager(context,mUrl,listener);
        remotePDFViewPager.setId(R.id.pdfViewPager);
    }

    private void changeUrl(String folder){
        mFolder = folder;
        mUrl = mServer + mFolder + mProducName + ".pdf";
        txtMaterialUrl.setText(mUrl);
    }

    private class flagClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            String sProductName = mProducName.replaceAll("/","_");
            int idx = mProducName.indexOf("+");
            if(idx>-1){
                mProducName = sProductName.substring(0,idx);
            }

            switch (view.getId()){
                case R.id.btnMaterialFlag1:
                    btnMaterialFlag1.setSelected(true);
                    btnMaterialFlag2.setSelected(false);
                    btnMaterialFlag3.setSelected(false);
                    changeUrl("/materials/");
                    showFile();
                    break;
                case R.id.btnMaterialFlag2:
                    btnMaterialFlag1.setSelected(false);
                    btnMaterialFlag2.setSelected(true);
                    btnMaterialFlag3.setSelected(false);
                    changeUrl("/same/");
                    showFile();
                    break;
                case R.id.btnMaterialFlag3:
                    btnMaterialFlag1.setSelected(false);
                    btnMaterialFlag2.setSelected(false);
                    btnMaterialFlag3.setSelected(true);
                    String sProcess = txtMaterialProcess.getText().toString().trim();
                    if(sProcess.equals("")||sProcess.isEmpty()){
                        MyToast.myShow(ShowMaterial.this,"请选择工序",0,0);
                    }else{
                        mProducName =  mProducName + "+"+sProcess;
                        changeUrl("/process/");
                        showFile();
                    }
                    break;
                case R.id.txtMaterialProcess:
                    openSelectProcessDialog();
                    break;
            }
        }
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

    @Override
    public void onSuccess(String url, String destinationPath) {
        adapter = new PDFPagerAdapter(this, FileUtil.extractFileNameFromURL(url));
        remotePDFViewPager.setAdapter(adapter);

        remote_pdf_root.removeAllViewsInLayout();
        remote_pdf_root.addView(remotePDFViewPager,LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onFailure(Exception e) {
        MyToast.myShow(ShowMaterial.this,"无此文件,"+e.getLocalizedMessage(),0,0);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onProgressUpdate(int progress, int total) {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(adapter!=null){
            adapter.close();
        }
    }

    /**
     *描述: 选择工序清单
     *日期：2022/7/17
     **/
    public void openSelectProcessDialog() {
        DeviceListDialog.Builder alert = new DeviceListDialog.Builder(ShowMaterial.this);
        alert.setListData(mDatas);
        alert.setTitle("请选择设备");
        alert.setSelectedListiner(new DeviceListDialog.Builder.OnSelectedListiner() {
            @Override
            public void onSelected(String info) {
                txtMaterialProcess.setText(info);
            }
        });
        DeviceListDialog mDialog = alert.show();
        //设置Dialog 尺寸
        mDialog.setDialogWindowAttr(0.8, 0.8, ShowMaterial.this);
    }

    /**
    *描述: 获取工序数据
    *日期：2023/2/20
    **/
    private void getProcessData(){
        mDatas = new ArrayList<>();

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ItemInfoGet";
                String strType = "6";
                String strwhere = " imaal003='"+txtMaterialProductName.getText().toString()+"'";

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
                mapResponseList = t100ServiceHelper.getT100JsonProcessData(strResponse,"iteminfo");

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
                    }
                }else{
                    MyToast.myShow(ShowMaterial.this,"无数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(ShowMaterial.this,e.getLocalizedMessage(),0,0);
            }

            @Override
            public void onComplete() {
                if(statusCode.equals("0")){
                    if(mapResponseList.size()> 0) {
                        //显示单头数据
                        for(int i=0;i<mapResponseList.size();i++){
                            mDatas.add(mapResponseList.get(i).get("Process").toString());
                        }
                    }
                }else{
                    MyToast.myShow(ShowMaterial.this,statusDescription,0,0);
                }
            }
        });
    }
}