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
    private int intType;
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
        int[] titleId;

        //按照不同导航显示对应按钮
        //生产协同：6;质量检验：1;销售出货：5
        if(intIndex == 1) {
            btnId = new int[]{R.id.subMasterAction1, R.id.subMasterAction2, R.id.subMasterAction5, R.id.subMasterAction7};
            imgId = new int[]{R.drawable.sub_master5, R.drawable.sub_master6, R.drawable.sub_master7,  R.drawable.sub_master9};
            titleId = new int[]{R.string.tab_product5,R.string.tab_product6,R.string.tab_product7,R.string.tab_product9};
        }else{
            if(intIndex == 6){
                btnId= new int[]{R.id.subMasterAction1, R.id.subMasterAction2, R.id.subMasterAction5,R.id.subMasterAction3, R.id.subMasterAction4};
                imgId= new int[]{R.drawable.sub_master1, R.drawable.sub_master10,R.drawable.sub_master2, R.drawable.sub_master3, R.drawable.sub_master4};
                titleId = new int[]{R.string.tab_product1,R.string.tab_product10,R.string.tab_product2,R.string.tab_product3,R.string.tab_product4};
            }else{
                btnId= new int[]{R.id.subMasterAction1, R.id.subMasterAction2, R.id.subMasterAction5,R.id.subMasterAction6,R.id.subMasterAction3};
                imgId= new int[]{R.drawable.sub_master11, R.drawable.sub_master12,R.drawable.sub_master13,R.drawable.sub_master8,R.drawable.sub_master14};
                titleId = new int[]{R.string.tab_product11,R.string.tab_product12,R.string.tab_product13,R.string.tab_product8,R.string.tab_product14};
            }
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
            btnAction.setText(getResources().getString(titleId[i]));
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
        subMasterAction5.setOnClickListener(new btnActionListener());
        subMasterAction6.setOnClickListener(new btnActionListener());
        subMasterAction7.setOnClickListener(new btnActionListener());

        //按照不同导航显示对应按钮
        //生产协同：6;质量检验：1;销售出货：5
        if(intIndex == 6) {
            subMasterAction6.setVisibility(View.GONE);
            subMasterAction7.setVisibility(View.GONE);
        }else{
            if(intIndex==1){
                subMasterAction3.setVisibility(View.GONE);
                subMasterAction4.setVisibility(View.GONE);
                subMasterAction6.setVisibility(View.GONE);
            }else{
                subMasterAction4.setVisibility(View.GONE);
                subMasterAction7.setVisibility(View.GONE);
            }
        }
    }

    //按钮单击事件
    private class btnActionListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            int btnId = 0;
            bundle = new Bundle();

            switch (view.getId()){
                case R.id.subMasterAction1:
                    //生产协同：6;质量检验：1;销售出货：5
                    if(intIndex == 6) {
                        //车间退料
                        intent = new Intent(SubMasterActivity.this,SubMasterDetailActivity.class);
                        btnId = 61;
                    }else{
                        if(intIndex==1){
                            //IQC检验
                            intent = new Intent(SubMasterActivity.this, SubMasterListActivity.class);
                            btnId = 11;
                        }else{
                            //任务分配
                            intent = new Intent(SubMasterActivity.this,SubMasterDetailActivity.class);
                            btnId = 51;
                        }
                    }
                    bundle.putInt("btnId",btnId);
                    bundle.putString("title",subMasterAction1.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                case R.id.subMasterAction2:
                    //生产协同：6;质量检验：1;销售出货：5
                    if(intIndex == 6) {
                        //生产入库
                        intent = new Intent(SubMasterActivity.this,SubMasterContentActivity.class);
                        btnId = 62;
                    }else{
                        if(intIndex == 1){
                            //PQC检验
                            intent = new Intent(SubMasterActivity.this, SubMasterListActivity.class);
                            btnId = 12;
                        }else{
                            //销售备货
                            intent = new Intent(SubMasterActivity.this,SubMasterListActivity.class);
                            btnId = 52;
                        }
                    }
                    bundle.putInt("btnId",btnId);
                    bundle.putString("title",subMasterAction2.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                case R.id.subMasterAction3:
                    //生产协同：6;质量检验：1;销售出货：5
                    if(intIndex == 6) {
                        //缺料明细
                        intent = new Intent(SubMasterActivity.this,SubMasterDetailActivity.class);
                        btnId = 63;
                        bundle.putInt("btnId",btnId);
                        bundle.putString("title",subMasterAction3.getText().toString());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        if(intIndex==5){
                            //异常备货
                            intent = new Intent(SubMasterActivity.this,SubMasterListActivity.class);
                            btnId = 53;
                            bundle.putInt("btnId",btnId);
                            bundle.putString("title",subMasterAction3.getText().toString());
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }
                    break;
                case R.id.subMasterAction4:
                    //生产协同：6;质量检验：1;销售出货：5
                    if(intIndex == 6) {
                        //计划超时
                        intent = new Intent(SubMasterActivity.this,SubMasterDetailActivity.class);
                        btnId = 64;
                        bundle.putInt("btnId",btnId);
                        bundle.putString("title",subMasterAction4.getText().toString());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                    break;
                case R.id.subMasterAction5:
                    //生产协同：6;质量检验：1;销售出货：5
                    if(intIndex == 6) {
                        //模具安装
                        intent = new Intent(SubMasterActivity.this,SubMasterDetailActivity.class);
                        btnId = 65;
                    }else{
                        if(intIndex==1){
                            //FQC检验
                            intent = new Intent(SubMasterActivity.this, SubMasterListActivity.class);
                            btnId = 15;
                        }else{
                            //销售退回
                            intent = new Intent(SubMasterActivity.this,SubMasterDetailActivity.class);
                            btnId = 55;
                        }
                    }
                    bundle.putInt("btnId",btnId);
                    bundle.putString("title",subMasterAction5.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                case R.id.subMasterAction6:
                    //生产协同：6;质量检验：1;销售出货：5
                    if(intIndex == 5) {
                        //OQC检验
                        intent = new Intent(SubMasterActivity.this, SubMasterListActivity.class);
                        btnId = 16;
                        bundle.putInt("btnId",btnId);
                        bundle.putString("title",subMasterAction6.getText().toString());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                    break;
                case R.id.subMasterAction7:
                    //生产协同：6;质量检验：1;销售出货：5
                    if(intIndex == 1) {
                        //库存检验
                        intent = new Intent(SubMasterActivity.this, SubMasterListActivity.class);
                        btnId = 17;
                        bundle.putInt("btnId",btnId);
                        bundle.putString("title",subMasterAction7.getText().toString());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                    break;
            }

        }
    }
}