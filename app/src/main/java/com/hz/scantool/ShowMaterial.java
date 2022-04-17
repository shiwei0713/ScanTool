package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.MyToast;

import es.voghdev.pdfviewpager.library.PDFViewPager;
import es.voghdev.pdfviewpager.library.RemotePDFViewPager;
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter;
import es.voghdev.pdfviewpager.library.remote.DownloadFile;
import es.voghdev.pdfviewpager.library.util.FileUtil;

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
    private TextView txtMaterialProductName;
    private TextView txtMaterialUrl;
    private Button btnMaterialFlag1;
    private Button btnMaterialFlag2;
    private Button btnMaterialFlag3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_material);

        //初始化
        initBundle();
        initView();

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

        showFile();
    }

    private void initView(){
        remote_pdf_root = findViewById(R.id.remote_pdf_root);
        txtMaterialProductName = findViewById(R.id.txtMaterialProductName);
        txtMaterialProductName.setText(mProducName);
        txtMaterialUrl = findViewById(R.id.txtMaterialUrl);
        txtMaterialUrl.setText(mUrl);

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
        if(adapter!=null){
            adapter.close();
            adapter=null;
            remote_pdf_root.removeAllViewsInLayout();
        }else{
            //设置监听
            final DownloadFile.Listener listener = this;
            remotePDFViewPager = new RemotePDFViewPager(this,mUrl,listener);
            remotePDFViewPager.setId(R.id.pdfViewPager);
        }
    }

    private void changeUrl(String folder){
        mFolder = folder;
        mUrl = mServer + mFolder + mProducName + ".pdf";
        txtMaterialUrl.setText(mUrl);
    }

    private class flagClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
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
                    changeUrl("/process/");
                    showFile();
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
    }

    @Override
    public void onFailure(Exception e) {
        MyToast.myShow(ShowMaterial.this,"无预览文件",0,0);
    }

    @Override
    public void onProgressUpdate(int progress, int total) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(adapter!=null){
            adapter.close();
        }
    }
}