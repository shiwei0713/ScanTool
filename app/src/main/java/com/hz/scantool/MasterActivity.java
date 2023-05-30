package com.hz.scantool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.models.UserInfo;

import java.util.Calendar;
import java.util.TimeZone;

public class MasterActivity extends AppCompatActivity {

    private static final int[] agrTitle={R.string.master_action1,R.string.master_action2,R.string.master_action3,R.string.master_action4,R.string.master_action5,R.string.master_action6,R.string.master_action7,R.string.master_action8,R.string.master_action9,R.string.master_action10,R.string.master_action11};

    TextView txtMasterUser;
    TextView txtMasterDate;
    TextView txtMasterSite;
    TextView txtLoginout;
    Button btnAction1;
    Button btnAction2;
    Button btnAction3;
    Button btnAction4;
    Button btnAction5;
    Button btnAction6;
    Button btnAction7;
    Button btnAction8;
    Button btnAction9,btnAction10,btnAction11;

    Calendar calendar;
    String year;
    String month;
    String day;
    Bundle bundle;
    Intent intent;
    private boolean btnAction1Power;
    private boolean btnAction2Power;
    private boolean btnAction3Power;
    private boolean btnAction4Power;
    private boolean btnAction5Power;
    private boolean btnAction6Power;
    private boolean btnAction7Power;
    private boolean btnAction8Power;
    private boolean btnAction9Power,btnAction10Power,btnAction11Power;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        //初始化显示数据
        initViewData();

        //设置按钮样式
        setBtnStyle();

        //绑定注销按钮
        txtLoginout = findViewById(R.id.txtLoginout);
        txtLoginout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MasterActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //绑定按钮事件
        btnAction1=findViewById(R.id.btnAction1);
        btnAction2=findViewById(R.id.btnAction2);
        btnAction3=findViewById(R.id.btnAction3);
        btnAction4=findViewById(R.id.btnAction4);
        btnAction5=findViewById(R.id.btnAction5);
        btnAction6=findViewById(R.id.btnAction6);
        btnAction7=findViewById(R.id.btnAction7);
        btnAction8=findViewById(R.id.btnAction8);
        btnAction9=findViewById(R.id.btnAction9);
        btnAction10=findViewById(R.id.btnAction10);
        btnAction11=findViewById(R.id.btnAction11);
        btnAction1.setOnClickListener(new btnActionListener());
        btnAction2.setOnClickListener(new btnActionListener());
        btnAction3.setOnClickListener(new btnActionListener());
        btnAction4.setOnClickListener(new btnActionListener());
        btnAction5.setOnClickListener(new btnActionListener());
        btnAction6.setOnClickListener(new btnActionListener());
        btnAction7.setOnClickListener(new btnActionListener());
        btnAction8.setOnClickListener(new btnActionListener());
        btnAction9.setOnClickListener(new btnActionListener());
        btnAction10.setOnClickListener(new btnActionListener());
        btnAction11.setOnClickListener(new btnActionListener());
    }

    //初始化显示数据
    private void initViewData(){

        //初始化用户信息
        txtMasterUser=findViewById(R.id.txtMasterUser);
        txtMasterUser.setText(UserInfo.getUserName(getApplicationContext()));

        //初始化日期
        calendar=Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        year=String.valueOf(calendar.get(Calendar.YEAR));
        month=String.valueOf(calendar.get(Calendar.MONTH)+1);
        day=String.valueOf(calendar.get(Calendar.DATE));
        txtMasterDate=findViewById(R.id.txtMasterDate);
        txtMasterDate.setText(year+"-"+month+"-"+day);

        //初始化公司
        txtMasterSite = findViewById(R.id.txtMasterSite);
        txtMasterSite.setText(UserInfo.getUserSite(getApplicationContext()));

        //初始化权限
        initUserPower();
    }

    private void initUserPower(){
        btnAction1Power =false;    //工序报工
        btnAction2Power =false;    //质量检验
        btnAction3Power =false;    //完工入库  //FQC
        btnAction4Power =false;     //采购入库
        btnAction5Power =false;     //生产备料
        btnAction6Power =false;     //销售出货
        btnAction7Power =false;    //生产协同
        btnAction8Power =false;     //期末盘点
        btnAction9Power =false;     //区域管理
        btnAction10Power =false;     //查询报表
        btnAction11Power =false;     //零件图示

        String strUserPower = UserInfo.getUserPower(getApplicationContext());
        String[] arrayPower = strUserPower.split(",");
        for(String power : arrayPower){
            if(power.equals("10")){
                btnAction1Power =true;    //工序报工
            }

            if(power.equals("11")){
                btnAction2Power =true;    //质量检验
            }

            if(power.equals("12")){
                btnAction3Power =true;    //完工入库
            }

            if(power.equals("13")){
                btnAction4Power =true;    //采购入库
            }

            if(power.equals("14")){
                btnAction5Power =true;    //生产备料
            }

            if(power.equals("15")){
                btnAction6Power =true;    //销售出货
            }

            if(power.equals("16")){
                btnAction7Power =true;    //生产协同
            }

            if(power.equals("17")){
                btnAction8Power =true;    //期末盘点
            }

            if(power.equals("18")){
                btnAction9Power =true;    //区域管理
            }

            if(power.equals("19")){
                btnAction10Power =true;    //查询报表
            }

            if(power.equals("20")){
                btnAction11Power =true;    //零件图示
            }
        }

    }

    //设置导航按钮样式
    private void setBtnStyle(){
        //声明按钮ID和图片ID
        int[] btnId= {R.id.btnAction1,R.id.btnAction2,R.id.btnAction3,R.id.btnAction4,R.id.btnAction5,R.id.btnAction6,R.id.btnAction7,R.id.btnAction8,R.id.btnAction9,R.id.btnAction10,R.id.btnAction11};
        int[] imgId= {R.drawable.master_action1,R.drawable.master_action2,R.drawable.master_action3,R.drawable.master_action4,R.drawable.master_action5,R.drawable.master_action6,R.drawable.master_action7,R.drawable.master_action8,R.drawable.master_action9,R.drawable.master_action10,R.drawable.master_action11};

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

    public class btnActionListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                //工序报工
                case R.id.btnAction1:
                    if(btnAction1Power){
                        intent=new Intent(MasterActivity.this,SubActivity.class);
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[0]));
                        bundle.putInt("count",1);
                        bundle.putInt("index",0);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
                //质量检验
                case R.id.btnAction2:
                    if(btnAction2Power){
                        intent=new Intent(MasterActivity.this,SubMasterActivity.class);
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[1]));
                        bundle.putInt("index",1);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
                //完工入库
                case R.id.btnAction3:
                    if(btnAction3Power){
                        intent=new Intent(MasterActivity.this,SubListActivity.class);
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[2]));
                        bundle.putInt("index",2);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
                //采购入库
                case R.id.btnAction4:
                    if(btnAction4Power){
                        intent=new Intent(MasterActivity.this,SubListActivity.class);
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[3]));
                        bundle.putInt("index",3);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
                //生产领退料
                case R.id.btnAction5:
                    if(btnAction5Power){
                        intent=new Intent(MasterActivity.this,WarehouseListActivity.class); //SubListActivity->WarehouseListActivity
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[4]));
                        bundle.putInt("index",4);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
                //销售出货
                case R.id.btnAction6:
                    if(btnAction6Power){
                        intent=new Intent(MasterActivity.this,SubMasterActivity.class);
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[5]));
//                    bundle.putInt("count",3);
                        bundle.putInt("index",5);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
                //生产协同
                case R.id.btnAction7:
                    if(btnAction7Power){
                        intent=new Intent(MasterActivity.this,SubMasterActivity.class);
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[6]));
                        bundle.putInt("index",6);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
                //月末盘点
                case R.id.btnAction8:
                    if(btnAction8Power){
                        intent=new Intent(MasterActivity.this,CheckStockActivity.class);
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[7]));
                        bundle.putInt("index",7);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
                //区域管理
                case R.id.btnAction9:
                    if(btnAction9Power){
                        intent=new Intent(MasterActivity.this,ProductAreaActivity.class);
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[8]));
                        bundle.putInt("index",8);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
                //查询报表
                case R.id.btnAction10:
                    if(btnAction10Power){
                        intent=new Intent(MasterActivity.this,ReportActivity.class);
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[9]));
                        bundle.putInt("index",9);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
                //零件图示
                case R.id.btnAction11:
                    if(btnAction11Power){
                        intent=new Intent(MasterActivity.this,SubMaterialListActivity.class);
                        bundle=new Bundle();
                        bundle.putString("title",getString(agrTitle[10]));
                        bundle.putInt("index",10);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        MyToast.myShow(MasterActivity.this,"无此功能权限",2,0);
                    }
                    break;
            }
        }
    }
}