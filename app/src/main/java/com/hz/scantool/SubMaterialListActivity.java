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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hz.scantool.adapter.LoadingDialog;
import com.hz.scantool.adapter.MyToast;
import com.hz.scantool.adapter.SubAdapter;
import com.hz.scantool.adapter.SubMaterialAdapter;
import com.hz.scantool.helper.T100ServiceHelper;
import com.hz.scantool.models.UserInfo;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class SubMaterialListActivity extends AppCompatActivity {

    private String strTitle;
    private int intIndex;
    private EditText txtQueryCondition;
    private Button btnMaterialQuery;
    private ListView subMaterialListView;
    private LoadingDialog loadingDialog;
    private String statusCode;
    private String statusDescription;
    private List<Map<String,Object>> mapResponseList;
    private List<Map<String,Object>> mapResponseStatus;
    private SubMaterialAdapter subMaterialAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_material_list);

        //初始化
        initBundle();
        initView();

        //获取工具栏
        Toolbar toolbar=findViewById(R.id.subMaterialListToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //获取清单
        getItemListData("");
    }

    private void initView(){
        txtQueryCondition = findViewById(R.id.txtQueryCondition);
        btnMaterialQuery = findViewById(R.id.btnMaterialQuery);
        subMaterialListView = findViewById(R.id.subMaterialListView);

        btnMaterialQuery.setOnClickListener(new btnActionListener());
        subMaterialListView.setOnItemClickListener(new listItemClickListener());
    }

    //初始化传入参数
    private void initBundle(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        intIndex = bundle.getInt("index");
        strTitle = bundle.getString("title");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(intIndex != 4){
            getMenuInflater().inflate(R.menu.sub_menu,menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //工具栏按钮事件定义
        switch (item.getItemId()){
            case R.id.action_scan:
                //调用zxing扫码界面
                IntentIntegrator intentIntegrator = new IntentIntegrator(SubMaterialListActivity.this);
                intentIntegrator.setTimeout(5000);
                intentIntegrator.setDesiredBarcodeFormats();  //IntentIntegrator.QR_CODE
                //开始扫描
                intentIntegrator.initiateScan();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE){
            IntentResult intentResult = IntentIntegrator.parseActivityResult(resultCode,data);
            String qrContent = intentResult.getContents();
            Intent intent = null;

            if(qrContent!=null && qrContent.length()!=0){

            }else{
                MyToast.myShow(this,"条码错误,请重新扫描"+qrContent,0,0);
            }
        }
    }

    //行单击事件
    private class listItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            TextView txtViewProductUrl = view.findViewById(R.id.txtViewProductUrl);
            TextView txtViewProductName = view.findViewById(R.id.txtViewProductName);

            Intent intent = new Intent(SubMaterialListActivity.this,ShowMaterial.class);
            Bundle bundle=new Bundle();
            bundle.putString("url",txtViewProductUrl.getText().toString());
            bundle.putString("title",strTitle);
            bundle.putString("product_name",txtViewProductName.getText().toString());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public class btnActionListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnMaterialQuery:
                    String sQueryCondition = txtQueryCondition.getText().toString();
                    String sCondition = " imaal003 LIKE '%"+sQueryCondition+"%'";
                    if(sQueryCondition.equals("")||sQueryCondition.isEmpty()){
                        sCondition = "";
                    }
                    getItemListData(sCondition);
                    break;
            }
        }
    }

    //获取清单
    private void getItemListData(String strwhere){
        //显示进度条
        if(loadingDialog==null){
            loadingDialog = new LoadingDialog(SubMaterialListActivity.this,"数据查询中",R.drawable.dialog_loading);
            loadingDialog.show();
        }

        Observable.create(new ObservableOnSubscribe<List<Map<String,Object>>>(){
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> e) throws Exception {
                //初始化T100服务名
                String webServiceName = "ItemInfoGet";
                String strType = "1";

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
                mapResponseList = t100ServiceHelper.getT100JsonItemData(strResponse,"iteminfo");

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

                        if(!statusCode.equals("0")){
                            MyToast.myShow(SubMaterialListActivity.this,statusDescription,0,0);
                        }
                    }
                }else{
                    MyToast.myShow(SubMaterialListActivity.this,"无数据",2,0);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyToast.myShow(SubMaterialListActivity.this,"网络错误",0,0);
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                subMaterialAdapter = new SubMaterialAdapter(mapResponseList,getApplicationContext());
                subMaterialListView.setAdapter(subMaterialAdapter);

                loadingDialog.dismiss();
                loadingDialog = null;
            }
        });
    }
}