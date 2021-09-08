package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class SubListDetailAdapter extends BaseAdapter {
    private List<Map<String,Object>> mData;
    private Context mContext;

    public SubListDetailAdapter(List<Map<String,Object>> mData, Context mContext){
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SubListDetailViewHolder holder = null;
        if(view==null){
            holder = new SubListDetailViewHolder();

            view= LayoutInflater.from(mContext).inflate(R.layout.sub_list_detail_item,viewGroup,false);

            holder.txtSubListItemStockLocation = view.findViewById(R.id.txtSubListItemStockLocation);
            holder.txtSubListItemPlanDate = view.findViewById(R.id.txtSubListItemPlanDate);
            holder.txtSubListItemProductCode = view.findViewById(R.id.txtSubListItemProductCode);
            holder.txtSubListItemProductName = view.findViewById(R.id.txtSubListItemProductName);
            holder.txtSubListItemProductModels = view.findViewById(R.id.txtSubListItemProductModels);
            holder.txtSubListItemDept = view.findViewById(R.id.txtSubListItemDept);
            holder.txtSubListItemQuantity = view.findViewById(R.id.txtSubListItemQuantity);
            holder.txtSubListItemQuantityPcs = view.findViewById(R.id.txtSubListItemQuantityPcs);

            holder.imgSubListIcon=view.findViewById(R.id.imgSubListIcon);

            view.setTag(holder);

        }else{
            holder = (SubListDetailViewHolder)view.getTag();
        }

        holder.imgSubListIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
        holder.txtSubListItemStockLocation.setText((String)mData.get(i).get("StockLocation"));
        holder.txtSubListItemPlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.txtSubListItemProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtSubListItemProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtSubListItemProductModels.setText((String)mData.get(i).get("ProductModels"));
//        holder.txtSubListItemDept.setText((String)mData.get(i).get("Dept"));
        holder.txtSubListItemQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.txtSubListItemQuantityPcs.setText((String)mData.get(i).get("QuantityPcs"));

        return view;
    }

    public static class SubListDetailViewHolder {
        TextView txtSubListItemStockLocation;
        TextView txtSubListItemPlanDate;
        TextView txtSubListItemProductCode;
        TextView txtSubListItemProductName;
        TextView txtSubListItemProductModels;
        TextView txtSubListItemDept;
        TextView txtSubListItemQuantity;
        TextView txtSubListItemQuantityPcs;

        ImageView imgSubListIcon;
    }
}