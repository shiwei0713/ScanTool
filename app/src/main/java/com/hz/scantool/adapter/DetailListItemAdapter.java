package com.hz.scantool.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.hz.scantool.models.Company;
import com.hz.scantool.R;
import com.hz.scantool.helper.SharedHelper;
import com.hz.scantool.helper.WebServiceHelper;

import java.util.List;
import java.util.Map;

public class DetailListItemAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private SharedHelper sharedHelper;
    private String nerworkType;
    private Company company;
    private String userCode;
    private String strDocno;
    private Integer type;

    //创建Handler
    private final Handler dHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            DetailViewHolder holder = (DetailViewHolder)msg.obj;
            int position = msg.arg1;
            if(msg.what == 1){
                holder.strStatus = (String)mData.get(position).put("Status","Y");
                holder.detailListStatus.setImageDrawable(mContext.getResources().getDrawable(R.drawable.list_status_dealok));
            }else{
                Toast.makeText(mContext,"更新失败!",Toast.LENGTH_SHORT).show();
            }
        }
    };

    public DetailListItemAdapter(List<Map<String,Object>> mData,Context mContext,SharedHelper sharedHelper,String strDocno,int type){
        this.mData = mData;
        this.mContext = mContext;
        this.sharedHelper = sharedHelper;
        this.strDocno = strDocno;
        this.type = type;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DetailViewHolder holder = null;

        if(convertView == null) {
            holder = new DetailViewHolder();
            //实例化组件,获取组件
            convertView = LayoutInflater.from(mContext).inflate(R.layout.detail_list_item, parent, false);

            holder.txtDetailProductCode = convertView.findViewById(R.id.txtDetailProductCode);
            holder.txtDetailProductName = convertView.findViewById(R.id.txtDetailProductName);
            holder.txtDetailProductModels = convertView.findViewById(R.id.txtDetailProductModels);
            holder.txtDetailStockLocation = convertView.findViewById(R.id.txtDetailStockLocation);
            holder.txtDetailStockBatch = convertView.findViewById(R.id.txtDetailStockBatch);
            holder.txtDetailPlanDate = convertView.findViewById(R.id.txtDetailPlanDate);
            holder.txtDetailInventory = convertView.findViewById(R.id.txtDetailInventory);
            holder.txtDetailQuantity = convertView.findViewById(R.id.txtDetailQuantity);
            holder.txtDetailQuantityPcs = convertView.findViewById(R.id.txtDetailQuantityPcs);

            holder.detailListStatus = convertView.findViewById(R.id.detailListStatus);
            holder.imageDetailListViewIcon = convertView.findViewById(R.id.imageDetailListViewIcon);

            holder.listDetailBtnConfirm = convertView.findViewById(R.id.listDetailBtnConfirm);

            convertView.setTag(holder);
        }else{
            holder = (DetailViewHolder)convertView.getTag();
        }

        //初始化控件
        intiView(holder);

        //设置标题显示值
        String strProductCode = mContext.getResources().getString(R.string.detail_content_title1);
        String strProductName = mContext.getResources().getString(R.string.detail_title6);
        String strProductModels = mContext.getResources().getString(R.string.detail_content_title2);
        String strStockBatchTitle = mContext.getResources().getString(R.string.item_title_stock_batch);
        String strStockDateTitle = mContext.getResources().getString(R.string.item_title_date);
        String strQuantityTitle = mContext.getResources().getString(R.string.item_title_quantity);
        String strInventoryTitle = mContext.getResources().getString(R.string.detail_inventory);
        String strQuantityPcsTitle = mContext.getResources().getString(R.string.detail_detail_quantity_pcs_title);

        //设置组件显示值
        holder.txtDetailProductCode.setText(strProductCode + (String)mData.get(position).get("ProductCode"));
        holder.txtDetailProductName.setText(strProductName + (String)mData.get(position).get("ProductName"));
        holder.txtDetailProductModels.setText(strProductModels + (String)mData.get(position).get("ProductModels"));
        holder.txtDetailStockLocation.setText((String)mData.get(position).get("StockLocation"));
        holder.txtDetailStockBatch.setText(strStockBatchTitle + (String)mData.get(position).get("StockBatch"));
        holder.txtDetailPlanDate.setText(strStockDateTitle + (String)mData.get(position).get("PlanDate"));
        holder.txtDetailInventory.setText(strInventoryTitle + (String)mData.get(position).get("Inventory"));
        holder.txtDetailQuantity.setText(strQuantityTitle + (String)mData.get(position).get("Quantity"));
        holder.txtDetailQuantityPcs.setText(strQuantityPcsTitle + (String)mData.get(position).get("QuantityPcs"));

        //设置变量值
        holder.strProductCode = (String)mData.get(position).get("ProductCode");
        holder.strStockId = (String)mData.get(position).get("StockId");
        holder.strStockLocationId = (String)mData.get(position).get("StockLocationId");

        //状态显示
        holder.imageDetailListViewIcon.setImageDrawable(convertView.getResources().getDrawable(R.drawable.detail_list_top));
        holder.strStatus = (String)mData.get(position).get("Status");
        if(holder.strStatus.equals("Y")){
            holder.detailListStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_dealok));
        }else{
            if(holder.strStatus.equals("X")){
                holder.detailListStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_ng));
            }else {
                if(holder.strStatus.equals("S")){
                    holder.detailListStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_ok_red));
                }else{
                    holder.detailListStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_deal));
                }
            }
        }

        //按钮事件
        holder.listDetailBtnConfirm.setOnClickListener(new listDetailOnClick(holder,position));
        Log.i("QRCODE","getView:strStockLocationId:"+holder.txtDetailStockLocation.getText()+",strStatus:"+holder.strStatus);

        return convertView;
    }

    public static class DetailViewHolder{
        TextView txtDetailProductCode;
        TextView txtDetailProductName;
        TextView txtDetailProductModels;

        TextView txtDetailStockLocation;
        TextView txtDetailStockBatch;
        TextView txtDetailPlanDate;
        TextView txtDetailInventory;
        TextView txtDetailQuantity;
        TextView txtDetailQuantityPcs;

        ImageView imageDetailListViewIcon;
        ImageView detailListStatus;

        Button listDetailBtnConfirm;

        String strStatus;
        String strProductCode;
        String strStockId;
        String strStockLocationId;
    }

    private void intiView(DetailViewHolder holder){
        holder.txtDetailProductCode.setVisibility(View.GONE);
        holder.txtDetailPlanDate.setVisibility(View.GONE);
        holder.txtDetailInventory.setVisibility(View.GONE);
        holder.listDetailBtnConfirm.setVisibility(View.GONE);

        if(type == 1){
            holder.listDetailBtnConfirm.setVisibility(View.GONE);
            holder.txtDetailStockBatch.setVisibility(View.GONE);
            holder.txtDetailQuantityPcs.setVisibility(View.GONE);
        }else{
//            holder.listDetailBtnConfirm.setVisibility(View.VISIBLE);
        }
    }

    class listDetailOnClick implements View.OnClickListener{
        private DetailViewHolder holder;
        private int position;

        listDetailOnClick(DetailViewHolder holder,int position){
            this.holder = holder;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if(holder.strStatus.equals("Y")){
                Toast.makeText(mContext,"请不要重复操作,此储位已经完成备货!",Toast.LENGTH_SHORT).show();
            }else{
                sendData(holder,position);
            }
        }
    }

    public void sendData(DetailViewHolder holder,int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String strDetailContent = "";
                    //初始化网络类型和营运据点
//                    company=new Company();
//                    Map<String,String> data=sharedHelper.readShared();
//                    nerworkType = data.get("network");
//                    userCode = data.get("userId");
//                    company.setSite(data.get("userSite"));

                    //初始化T100服务名
                    String webServiceName = "SaleRequestUpdate";

                    //设置传入请求参数
                    StringBuilder strWebRequestConten= new StringBuilder();
                    strWebRequestConten.append("&lt;Document&gt;\n"+
                            "&lt;RecordSet id=\"1\"&gt;\n"+
                            "&lt;Master name=\"xmdk_t\" node_id=\"1\"&gt;\n"+
                            "&lt;Record&gt;\n"+
                            "&lt;Field name=\"xmdksite\" value=\""+company.getCode()+"\"/&gt;\n"+
                            "&lt;Field name=\"xmdkent\" value=\"10\"/&gt;\n"+
                            "&lt;Field name=\"xmdkdocno\" value=\""+strDocno+"\"/&gt;\n"+
                            "&lt;Field name=\"xmdkud002\" value=\""+userCode+"\"/&gt;\n"+
                            "&lt;Detail name=\"s_detail1\" node_id=\"1_1\"&gt;\n"+
                            "&lt;Record&gt;\n"+
                            "&lt;Field name=\"xmdm001\" value=\""+holder.strProductCode+"\"/&gt;\n"+
                            "&lt;Field name=\"xmdm005\" value=\""+holder.strStockId+"\"/&gt;\n"+
                            "&lt;Field name=\"xmdm006\" value=\""+holder.strStockLocationId+"\"/&gt;\n"+
                            "&lt;/Record&gt;\n"+
                            "&lt;/Detail&gt;\n"+
                            "&lt;Memo/&gt;\n"+
                            "&lt;Attachment count=\"0\"/&gt;\n"+
                            "&lt;/Record&gt;\n"+
                            "&lt;/Master&gt;\n"+
                            "&lt;/RecordSet&gt;\n"+
                            "&lt;/Document&gt;\n");

                    //设置WebService参数
                    WebServiceHelper webServiceHelper=new WebServiceHelper();
                    webServiceHelper.setWebKey("16baae6c40b922d8ddb12a0320d8ea1d");
                    webServiceHelper.setWebTimestamp("20201114083106031");
                    webServiceHelper.setWebName(webServiceName);
                    webServiceHelper.setWebUrl(nerworkType);
                    webServiceHelper.setWebSite(company.getCode());
                    webServiceHelper.setWebRequestContent(strWebRequestConten);

                    //发送WebService请求,并返回结果
                    String strResponse = "";
                    try{
                        strResponse=webServiceHelper.sendWebRequest();

                        //获取WebService相应代码
                        Integer iResponseCode=webServiceHelper.getWebResponseCode();
                        Message message = new Message();
                        if(iResponseCode==200){
                            message.what = 1;
                            message.arg1 = position;
                            message.obj = holder;
                        }else{
                            message.what = 0;
                        }
                        dHandler.sendMessage(message);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
