package com.hz.scantool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hz.scantool.helper.SharedHelper;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

public class MasterActivity extends AppCompatActivity {

    private static final int[] agrTitle={R.string.master_action1,R.string.master_action2,R.string.master_action3,R.string.master_action4,R.string.master_action5,R.string.master_action6,R.string.master_action7,R.string.master_action8};

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
    SharedHelper sharedHelper;
    Context mContext;
    Calendar calendar;
    String year;
    String month;
    String day;
    Bundle bundle;
    Intent intent;

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
        btnAction1.setOnClickListener(new btnActionListener());
        btnAction2.setOnClickListener(new btnActionListener());
        btnAction3.setOnClickListener(new btnActionListener());
        btnAction4.setOnClickListener(new btnActionListener());
        btnAction5.setOnClickListener(new btnActionListener());
        btnAction6.setOnClickListener(new btnActionListener());
        btnAction7.setOnClickListener(new btnActionListener());
        btnAction8.setOnClickListener(new btnActionListener());
    }

    //初始化显示数据
    private void initViewData(){
        //初始化存储信息
        mContext=getApplicationContext();
        sharedHelper=new SharedHelper(mContext);
        Map<String,String> data=sharedHelper.readShared();

        //初始化用户信息
        txtMasterUser=findViewById(R.id.txtMasterUser);
        txtMasterUser.setText(data.get("userName"));

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
        txtMasterSite.setText(data.get("userSite"));
    }

    //设置导航按钮样式
    private void setBtnStyle(){
        //声明按钮ID和图片ID
        int[] btnId= {R.id.btnAction1,R.id.btnAction2,R.id.btnAction3,R.id.btnAction4,R.id.btnAction5,R.id.btnAction6,R.id.btnAction7,R.id.btnAction8};
        int[] imgId= {R.drawable.master_action1,R.drawable.master_action2,R.drawable.master_action3,R.drawable.master_action4,R.drawable.master_action5,R.drawable.master_action6,R.drawable.master_action7,R.drawable.master_action8};

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
                    intent=new Intent(MasterActivity.this,ListActivity.class);
                    bundle=new Bundle();
                    bundle.putString("title",getString(agrTitle[0]));
                    bundle.putInt("count",1);
                    bundle.putInt("index",0);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                //质量检验
                case R.id.btnAction2:
                    intent=new Intent(MasterActivity.this,ListActivity.class);
                    bundle=new Bundle();
                    bundle.putString("title",getString(agrTitle[1]));
                    bundle.putInt("count",4);
                    bundle.putInt("index",1);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                //完工入库
                case R.id.btnAction3:
                    intent=new Intent(MasterActivity.this,ListActivity.class);
                    bundle=new Bundle();
                    bundle.putString("title",getString(agrTitle[2]));
                    bundle.putInt("count",1);
                    bundle.putInt("index",2);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                //采购入库
                case R.id.btnAction4:
                    intent=new Intent(MasterActivity.this,ListActivity.class);
                    bundle=new Bundle();
                    bundle.putString("title",getString(agrTitle[3]));
                    bundle.putInt("count",3);
                    bundle.putInt("index",3);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                //生产领退料
                case R.id.btnAction5:
                    intent=new Intent(MasterActivity.this,ListActivity.class);
                    bundle=new Bundle();
                    bundle.putString("title",getString(agrTitle[4]));
                    bundle.putInt("count",4);
                    bundle.putInt("index",4);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                //销售出货
                case R.id.btnAction6:
                    intent=new Intent(MasterActivity.this,ListActivity.class);
                    bundle=new Bundle();
                    bundle.putString("title",getString(agrTitle[5]));
                    bundle.putInt("count",3);
                    bundle.putInt("index",5);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                //生产协同
                case R.id.btnAction7:
                    intent=new Intent(MasterActivity.this,ListActivity.class);
                    bundle=new Bundle();
                    bundle.putString("title",getString(agrTitle[6]));
                    bundle.putInt("count",2);
                    bundle.putInt("index",6);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                //月末盘点
                case R.id.btnAction8:
                    intent=new Intent(MasterActivity.this,ListActivity.class);
                    bundle=new Bundle();
                    bundle.putString("title",getString(agrTitle[7]));
                    bundle.putInt("count",1);
                    bundle.putInt("index",7);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
            }
        }
    }
}