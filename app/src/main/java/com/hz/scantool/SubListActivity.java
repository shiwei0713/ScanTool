package com.hz.scantool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.hz.scantool.adapter.SubListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubListActivity extends AppCompatActivity {

    private ListView listView;
    private List<Map<String,Object>> list;
    private SubListAdapter subListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_list);

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(getResources().getString(R.string.master_action5));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //test
        listView = findViewById(R.id.subListView);
        list = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("StockLocation","1122");
        map.put("ProductCode","1122");
        map.put("ProductName","1122");
        map.put("ProductModels","1122");
        map.put("Dept","1122");
        map.put("Quantity","1122");
        map.put("QuantityPcs","1122");
        list.add(map);
        subListAdapter = new SubListAdapter(list,getApplicationContext());
        listView.setAdapter(subListAdapter);
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


}