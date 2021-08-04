package com.hz.scantool.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hz.scantool.models.Company;
import com.hz.scantool.adapter.ListItemAdapter;
import com.hz.scantool.R;
import com.hz.scantool.helper.SharedHelper;
import com.hz.scantool.helper.WebServiceHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SECTION_TYPE = "section_type";
    private static final String ARG_SECTION_WHERE = "section_where";
    private int index;
    private int type;
    private String qrContent;
    private String nerworkType;
    Company company;
    Context mContext;
    SharedHelper sharedHelper;
    private PageViewModel pageViewModel;
    private ListView listView;
    private ListItemAdapter itemAdapter;
    public static String listJson = "";
    Bundle bundle;
    Intent intent;

    public static PlaceholderFragment newInstance(int index,int type) {
        //创建Fragment对象，并传入参数
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        bundle.putInt(ARG_SECTION_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    //创建Handler
    private final Handler listHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            listJson = (String)msg.obj;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化存储信息
        mContext=getContext();
        sharedHelper=new SharedHelper(mContext);

        //创建ViewModel对象,并传入参数至ViewModel
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        index = 1;
        type = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
            type = getArguments().getInt(ARG_SECTION_TYPE);
        }

        /*
        传入变量至ViewModel
        1、index为页签索引
        2、type为页签类型
        3、查询条件:零件号
        */
        pageViewModel.setIndex(index);
        pageViewModel.setItemType(type);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        final TextView textView = root.findViewById(R.id.section_label);

        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //执行网络请求线程
                new Thread(getErpJsonThread).start();

                //获取显示数据
                List<Map<String,Object>> list = pageViewModel.getData(listJson);
                if(list.isEmpty()){
                    textView.setText(s);
                }else {
                    //初始化ListView
                    listView = (ListView) root.findViewById(R.id.section_listview);
                    itemAdapter = new ListItemAdapter(list,getActivity());
                    itemAdapter.setItemType(type,index,sharedHelper);
                    listView.setAdapter(itemAdapter);

                    /*
                    listview长按事件
                     */
                    //ListView的item注册ContextMenu
//                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//                        @Override
//                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                            builder.setMessage("确定删除记录?");
//                            builder.setTitle("提示");
//                            builder.setIcon(R.drawable.alert_icon);
//
//                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    TextView txtListCode = view.findViewById(R.id.txtListName3);
//                                    Toast.makeText(getActivity(),"第"+position+"行"+txtListCode.getText(),Toast.LENGTH_LONG).show();
//                                }
//                            });
//
//                            builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
//
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            });
//
//                            builder.create().show();
//
//                            //为false会同时执行单击事件，为true只执行长按事件
//                            return true;
//                        }
//                    });

                    //ListView子项单击事件
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                            TextView txtListDocno = view.findViewById(R.id.txtListDocno);
//                            TextView txtListProductCode = view.findViewById(R.id.txtListProductCode);
//                            TextView txtListProductName = view.findViewById(R.id.txtListProductName);
//                            TextView txtListProductModels = view.findViewById(R.id.txtListProductModels);
//                            TextView txtListProducerId = view.findViewById(R.id.txtListProducerId);
//                            TextView txtListProducer = view.findViewById(R.id.txtListProducer);
//                            TextView txtListStockId = view.findViewById(R.id.txtListStockId);
//                            TextView txtListStock = view.findViewById(R.id.txtListStock);
//                            TextView txtListQuantity = view.findViewById(R.id.txtListQuantity);
//                            TextView txtListQuantityPcs = view.findViewById(R.id.txtListQuantityPcs);
//                            switch (type){
//                                case 1:
//                                    intent = new Intent(getActivity(), DetailActivity.class);
//                                    break;
//                                //生产备料
//                                case 4:
//                                    intent = new Intent(getActivity(), DetailListActivity.class);
//                                    break;
//                                //销售出货
//                                case 5:
//                                    intent = new Intent(getActivity(), DetailListActivity.class);
//                                    break;
//                            }
//                            //设置传入参数
//                            bundle=new Bundle();
//                            bundle.putString("txtListDocno",txtListDocno.getText().toString().trim());
//                            bundle.putString("txtListProductCode",txtListProductCode.getText().toString().trim());
//                            bundle.putString("txtListProductName",txtListProductName.getText().toString().trim());
//                            bundle.putString("txtListProductModels",txtListProductModels.getText().toString().trim());
//                            bundle.putString("txtListProducerId",txtListProducerId.getText().toString().trim());
//                            bundle.putString("txtListProducer",txtListProducer.getText().toString().trim());
//                            bundle.putString("txtListStockId",txtListStockId.getText().toString().trim());
//                            bundle.putString("txtListStock",txtListStock.getText().toString().trim());
//                            bundle.putString("txtListQuantity",txtListQuantity.getText().toString().trim());
//                            bundle.putString("txtListQuantityPcs",txtListQuantityPcs.getText().toString().trim());
//                            bundle.putInt("index",type);
//                            intent.putExtras(bundle);
//                            startActivity(intent);
                        }
                    });
                }
            }
        });

        return root;
    }

    //增加线程获取webservice数据
    Runnable getErpJsonThread = new Runnable(){

        @Override
        public void run() {
            try{
                Message message = new Message();
                message.obj = getErpJson();
                listHandler.sendMessage(message);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    //获取T100 JSON数据
    public String getErpJson() throws IOException {
        String strDetailContent = "";
        //初始化网络类型和营运据点
        company=new Company();
        Map<String,String> data=sharedHelper.readShared();
        nerworkType = data.get("network");
        company.setSite(data.get("userSite"));

        //设置传入请求参数
        StringBuilder strWebRequestConten= new StringBuilder();
        strWebRequestConten.append("&lt;Parameter&gt;\n"+
                                    "&lt;Record&gt;\n"+
                                    "&lt;Field name=\"enterprise\" value=\"10\"/&gt;\n"+
                                    "&lt;Field name=\"site\" value=\""+company.getCode()+"\"/&gt;\n"+
                                    "&lt;Field name=\"type\" value=\""+type+"\"/&gt;\n"+
                                    "&lt;/Record&gt;\n"+
                                    "&lt;/Parameter&gt;\n"+
                                    "&lt;Document/&gt;\n");

        //设置WebService参数
        WebServiceHelper webServiceHelper=new WebServiceHelper();
        webServiceHelper.setWebKey("16baae6c40b922d8ddb12a0320d8ea1d");
        webServiceHelper.setWebTimestamp("20201114083106031");
        webServiceHelper.setWebName("AppListGet");
        webServiceHelper.setWebUrl(nerworkType);
        webServiceHelper.setWebSite(company.getCode());
        webServiceHelper.setWebRequestContent(strWebRequestConten);

        //发送WebService请求,并返回结果
        String strResponse=webServiceHelper.sendWebRequest();

        //获取WebService相应代码
        Integer iResponseCode=webServiceHelper.getWebResponseCode();

        if(iResponseCode==200){
            String strContent = strResponse.replaceAll("&amp;quot;","\"");
            strDetailContent = strContent.substring(strContent.indexOf("Detail",1),strContent.indexOf("/Detail",1));
        }

        return strDetailContent;
    }
}