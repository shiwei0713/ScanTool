package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class SubMasterListItemAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private String strType;

    public SubMasterListItemAdapter(List<Map<String,Object>> mData, Context mContext,String strType){
        this.mData = mData;
        this.mContext = mContext;
        this.strType = strType;
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

        ViewHolder holder = null;

        if(convertView == null) {
            holder = new ViewHolder();
            //实例化组件,获取组件
            convertView = LayoutInflater.from(mContext).inflate(R.layout.sub_master_content_list_item, parent, false);

            holder.txtSubContentListDocno = convertView.findViewById(R.id.txtSubContentListDocno);
            holder.txtSubContentListDate = convertView.findViewById(R.id.txtSubContentListDate);
            holder.txtSubContentListProductCodeTitle = convertView.findViewById(R.id.txtSubContentListProductCodeTitle);
            holder.txtSubContentListProductCode = convertView.findViewById(R.id.txtSubContentListProductCode);
            holder.txtSubContentListProductModelsTitle = convertView.findViewById(R.id.txtSubContentListProductModelsTitle);
            holder.txtSubContentListProductModels = convertView.findViewById(R.id.txtSubContentListProductModels);
            holder.txtSubContentListProducerTitle = convertView.findViewById(R.id.txtSubContentListProducerTitle);
            holder.txtSubContentListProducerId = convertView.findViewById(R.id.txtSubContentListProducerId);
            holder.txtSubContentListProducer = convertView.findViewById(R.id.txtSubContentListProducer);
            holder.txtSubContentListStockId = convertView.findViewById(R.id.txtSubContentListStockId);
            holder.txtSubContentListStockTitle = convertView.findViewById(R.id.txtSubContentListStockTitle);
            holder.txtSubContentListStock = convertView.findViewById(R.id.txtSubContentListStock);
            holder.txtSubContentListPlannoTitle = convertView.findViewById(R.id.txtSubContentListPlannoTitle);
            holder.txtSubContentListPlanno = convertView.findViewById(R.id.txtSubContentListPlanno);
            holder.txtSubContentListQuantityTitle = convertView.findViewById(R.id.txtSubContentListQuantityTitle);
            holder.txtSubContentListQuantity = convertView.findViewById(R.id.txtSubContentListQuantity);
            holder.txtSubContentListQuantityPcsTitle = convertView.findViewById(R.id.txtSubContentListQuantityPcsTitle);
            holder.txtSubContentListQuantityPcs = convertView.findViewById(R.id.txtSubContentListQuantityPcs);

            holder.txtSubContentListBtnDelete = convertView.findViewById(R.id.txtSubContentListBtnDelete);
            holder.imgSubContentList = convertView.findViewById(R.id.imgSubContentList);
            holder.imgSubContentListAlarm = convertView.findViewById(R.id.imgSubContentListAlarm);
            holder.txtSubContentListStatus = convertView.findViewById(R.id.txtSubContentListStatus);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        //设置组件显示值
        holder.txtSubContentListDocno.setText((String)mData.get(position).get("Docno"));
        holder.txtSubContentListDate.setText((String)mData.get(position).get("PlanDate"));
        holder.txtSubContentListProductCode.setText((String)mData.get(position).get("ProductCode"));
        holder.txtSubContentListProductModels.setText((String)mData.get(position).get("ProductModels"));
        holder.txtSubContentListProducerId.setText((String)mData.get(position).get("ProducerId"));
        holder.txtSubContentListProducer.setText((String)mData.get(position).get("Producer"));
        holder.txtSubContentListStockId.setText((String)mData.get(position).get("StockId"));
        holder.txtSubContentListStock.setText((String)mData.get(position).get("Stock"));
        holder.txtSubContentListPlanno.setText((String)mData.get(position).get("Planno"));
        holder.txtSubContentListQuantity.setText((String)mData.get(position).get("Quantity"));
        holder.txtSubContentListQuantityPcs.setText((String)mData.get(position).get("QuantityPcs"));

        //状态图片显示
        holder.strStatus = (String)mData.get(position).get("Status");
        if(holder.strStatus.equals("Y")){
            holder.txtSubContentListBtnDelete.setVisibility(View.GONE);
            holder.txtSubContentListStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_check));
        }else{
            if(strType.equals("1")){
                holder.txtSubContentListBtnDelete.setVisibility(View.GONE);
            }else{
                holder.txtSubContentListBtnDelete.setVisibility(View.VISIBLE);
            }

            holder.txtSubContentListStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_deal));
        }

        //导肮logo显示
        holder.imgSubContentList.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_top_icon));
        holder.imgSubContentListAlarm.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_alarm));

        //空值控件隐藏
        initView(holder,position);

        return convertView;
    }

    //空值控件隐藏
    private void initView(ViewHolder holder,int position){
        //供应商/部门ID
        String strProducerId=(String)mData.get(position).get("ProducerId");
        if(strProducerId.isEmpty()){
            holder.txtSubContentListProducerId.setVisibility(View.GONE);
        }else{
            holder.txtSubContentListProducerId.setVisibility(View.VISIBLE);
        }

        //供应商/部门名称
        String strProducer = (String)mData.get(position).get("Producer");
        if(strProducer.isEmpty()){
            holder.txtSubContentListProducer.setVisibility(View.GONE);
        }else{
            holder.txtSubContentListProducer.setVisibility(View.VISIBLE);
        }

        //库位ID
        String strStockId = (String)mData.get(position).get("StockId");
        if(strStockId.isEmpty()){
            holder.txtSubContentListStockId.setVisibility(View.GONE);
        }else{
            holder.txtSubContentListStockId.setVisibility(View.VISIBLE);
        }

        //库位名称
        String strStock = (String)mData.get(position).get("Stock");
        if(strStock.isEmpty()){
            holder.txtSubContentListStock.setVisibility(View.GONE);
        }else{
            holder.txtSubContentListStock.setVisibility(View.VISIBLE);
        }

//        //工单单号
//        String strPlanno = (String)mData.get(position).get("Planno");
//        if(strPlanno.isEmpty()){
//            holder.txtSubContentListPlanno.setVisibility(View.GONE);
//        }else{
//            holder.txtSubContentListPlanno.setVisibility(View.VISIBLE);
//        }

        //片数
        String strQuantityPcs = (String)mData.get(position).get("QuantityPcs");
        if(strQuantityPcs.isEmpty()){
            holder.txtSubContentListQuantityPcs.setVisibility(View.GONE);
        }else{
            holder.txtSubContentListQuantityPcs.setVisibility(View.VISIBLE);
        }
    }

    public static class ViewHolder{
        TextView txtSubContentListDocno;
        TextView txtSubContentListDate;
        TextView txtSubContentListProductCodeTitle;
        TextView txtSubContentListProductCode;
        TextView txtSubContentListProductModelsTitle;
        TextView txtSubContentListProductModels;
        TextView txtSubContentListProducerTitle;
        TextView txtSubContentListProducerId;
        TextView txtSubContentListProducer;
        TextView txtSubContentListStockId;
        TextView txtSubContentListStockTitle;

        TextView txtSubContentListStock;
        TextView txtSubContentListPlannoTitle;
        TextView txtSubContentListPlanno;
        TextView txtSubContentListQuantityTitle;
        TextView txtSubContentListQuantity;
        TextView txtSubContentListQuantityPcsTitle;
        TextView txtSubContentListQuantityPcs;

        Button txtSubContentListBtnDelete;

        ImageView imgSubContentList;
        ImageView imgSubContentListAlarm;
        ImageView txtSubContentListStatus;
        String strStatus;
    }
}

