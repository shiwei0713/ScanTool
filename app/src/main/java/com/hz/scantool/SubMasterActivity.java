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

public class SubMasterActivity extends AppCompatActivity {

    Button subMasterAction1;
    Button subMasterAction2;
    Button subMasterAction3;
    Button subMasterAction4;
    Button subMasterAction5;
    Button subMasterAction6;
    Button subMasterAction7;

    private Intent intent;
    private Bundle bundle;
    private int intIndex;
    private String strTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_master);

        //获取传入参数
        initBundle();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subMasterToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化导航按钮样式
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
        int[] btnId;
        int[] imgId;

        //按照不同导航显示对应按钮
        //生产协同：6；质量检验：1
        if(intIndex == 1) {
            btnId = new int[]{R.id.subMasterAction1, R.id.subMasterAction2, R.id.subMasterAction5, R.id.subMasterAction6};
            imgId = new int[]{R.drawable.sub_master5, R.drawable.sub_master6, R.drawable.sub_master7, R.drawable.sub_master8};
        }else{
            btnId= new int[]{R.id.subMasterAction1, R.id.subMasterAction2, R.id.subMasterAction3, R.id.subMasterAction4};
            imgId= new int[]{R.drawable.sub_master1, R.drawable.sub_master2, R.drawable.sub_master3, R.drawable.sub_master4};
        }

        //初始化按钮和图片
        Button btnAction;
        Drawable drawable;

        //设置按钮样式
        for(int i=0;i<btnId.length;i++){
            btnAction=findViewById(btnId[i]);
            drawable=getResources().getDrawable(imgId[i]);
            drawable.setBounds(0,0,128,128);
            btnAction.setCompoundDrawables(null,drawable,null,null);
            btnAction.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }

    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        intIndex = bundle.getInt("index");
        strTitle = bundle.getString("title");
    }

    //初始化控件
    private void intView(){
        subMasterAction1 = findViewById(R.id.subMasterAction1);
        subMasterAction2 = findViewById(R.id.subMasterAction2);
        subMasterAction3 = findViewById(R.id.subMasterAction3);
        subMasterAction4 = findViewById(R.id.subMasterAction4);
        subMasterAction5 = findViewById(R.id.subMasterAction5);
        subMasterAction6 = findViewById(R.id.subMasterAction6);
        subMasterAction7 = findViewById(R.id.subMasterAction7);

        subMasterAction1.setOnClickListener(new btnActionListener());
        subMasterAction2.setOnClickListener(new btnActionListener());
        subMasterAction3.setOnClickListener(new btnActionListener());
        subMasterAction4.setOnClickListener(new btnActionListener());

        //按照不同导航显示对应按钮
        //生产协同：6；质量检验：1
        if(intIndex == 6) {
            subMasterAction5.setVisibility(View.GONE);
            subMasterAction6.setVisibility(View.GONE);
            subMasterAction7.setVisibility(View.GONE);
        }
    }

    //按钮单击事件
    private class btnActionListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            intent = new Intent(SubMasterActivity.this,SubMasterContentActivity.class);
            bundle = new Bundle();

            switch (view.getId()){
                case R.id.subMasterAction1:
                    bundle.putInt("btnId",R.id.subMasterAction1);
                    bundle.putString("title",subMasterAction1.getText().toString());
                    break;
                case R.id.subMasterAction2:
                    bundle.putInt("btnId",R.id.subMasterAction2);
                    bundle.putString("title",subMasterAction2.getText().toString());
                    break;
                case R.id.subMasterAction3:
                    bundle.putInt("btnId",R.id.subMasterAction3);
                    bundle.putString("title",subMasterAction3.getText().toString());
                    break;
                case R.id.subMasterAction4:
                    bundle.putInt("btnId",R.id.subMasterAction4);
                    bundle.putString("title",subMasterAction4.getText().toString());
                    break;
            }

            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}