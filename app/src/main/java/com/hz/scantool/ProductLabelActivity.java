package com.hz.scantool;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;

public class ProductLabelActivity extends AppCompatActivity {

    private static final String SCANACTION="com.android.server.scannerservice.broadcast";

    private String strTitle,strPlanno,strVersion;
    private String statusCode;
    private String statusDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_label);

        //初始化
        initBundle();
        initView();

    }

    /**
    *描述: 初始化参数
    *日期：2023/3/18
    **/
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
        strPlanno = bundle.getString("planNo");
        strVersion = bundle.getString("version");
    }

    /**
    *描述: 初始化控件
    *日期：2023/3/18
    **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.productLabelToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}