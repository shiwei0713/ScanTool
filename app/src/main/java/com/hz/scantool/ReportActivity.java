package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ReportActivity extends AppCompatActivity {

    private String strTitle;

    private Button reportAction1,reportAction2,reportAction3,reportAction4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        //初始化
        initBundle();
        intView();
        setBtnStyle();

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

    //设置导航按钮样式
    private void setBtnStyle(){
        //声明按钮ID和图片ID
        int[] btnId = new int[]{R.id.reportAction1, R.id.reportAction2,R.id.reportAction3,R.id.reportAction4};
        int[] titleId = new int[]{R.string.report_button_title1,R.string.report_button_title2,R.string.report_button_title3,R.string.report_button_title4};
        int[] imgId = new int[]{R.drawable.report_icon1, R.drawable.report_icon2, R.drawable.report_icon3,R.drawable.report_icon4};

        //初始化按钮和图片
        Button btnAction;
        Drawable drawable;

        //设置按钮样式
        for(int i=0;i<btnId.length;i++){
            btnAction=findViewById(btnId[i]);
            drawable=getResources().getDrawable(imgId[i]);
            drawable.setBounds(0,0,90,90);
            btnAction.setCompoundDrawables(drawable,null,null,null);
            btnAction.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btnAction.setText(getResources().getString(titleId[i]));
        }
    }

    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strTitle = bundle.getString("title");
    }

    //初始化控件
    private void intView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.reportToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化控件
        reportAction1 = findViewById(R.id.reportAction1);
        reportAction2 = findViewById(R.id.reportAction2);
        reportAction3 = findViewById(R.id.reportAction3);
        reportAction4 = findViewById(R.id.reportAction4);

        //初始化事件
        reportAction1.setOnClickListener(new btnActionListener());
        reportAction2.setOnClickListener(new btnActionListener());
        reportAction3.setOnClickListener(new btnActionListener());
        reportAction4.setOnClickListener(new btnActionListener());
    }

    //按钮单击事件
    private class btnActionListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            Intent intent;
            Bundle bundle = new Bundle();

            switch (view.getId()){
                //区域库存查询
                case R.id.reportAction1:
                    intent = new Intent(ReportActivity.this,QueryDataActivity.class);
                    bundle.putInt("btnId",10);
                    bundle.putString("title",reportAction1.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                //打卡明细查询
                case R.id.reportAction2:
                    intent = new Intent(ReportActivity.this,QueryClockActivity.class);
                    bundle.putInt("btnId",11);
                    bundle.putString("title",reportAction2.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                //标签追踪查询
                case R.id.reportAction3:
                    intent = new Intent(ReportActivity.this,QueryLabelActivity.class);
                    bundle.putInt("btnId",12);
                    bundle.putString("title",reportAction3.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                //报工数据查询
                case R.id.reportAction4:
                    intent = new Intent(ReportActivity.this,QueryProductActivity.class);
                    bundle.putInt("btnId",13);
                    bundle.putString("title",reportAction4.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
            }

        }
    }
}