package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class WarehouseDetailAdapter extends BaseAdapter {
    private List<Map<String,Object>> mData;
    private Context mContext;

    public WarehouseDetailAdapter(List<Map<String,Object>> mData, Context mContext){
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
        WarehouseDetailViewHolder holder = null;
        if(view == null){
            holder = new WarehouseDetailViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_warehouse_detail,viewGroup,false);

            holder.listWarehouseDetailPosition = view.findViewById(R.id.listWarehouseDetailPosition);
            holder.listWarehouseDetailFlag = view.findViewById(R.id.listWarehouseDetailFlag);
            holder.listWarehouseDetailProductCode = view.findViewById(R.id.listWarehouseDetailProductCode);
            holder.listWarehouseDetailProductName = view.findViewById(R.id.listWarehouseDetailProductName);
            holder.listWarehouseDetailProductModel = view.findViewById(R.id.listWarehouseDetailProductModel);
            holder.listWarehouseDetailQuantity = view.findViewById(R.id.listWarehouseDetailQuantity);
            holder.listWarehouseDetailQuantityPcs = view.findViewById(R.id.listWarehouseDetailQuantityPcs);

            view.setTag(holder);
        }else{
            holder=(WarehouseDetailViewHolder)view.getTag();
        }

        //值显示
        holder.listWarehouseDetailFlag.setText(String.valueOf(i+1));
        holder.listWarehouseDetailPosition.setText((String)mData.get(i).get("Position"));
        holder.listWarehouseDetailProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.listWarehouseDetailProductName.setText((String)mData.get(i).get("ProductName"));
        holder.listWarehouseDetailProductModel.setText((String)mData.get(i).get("ProductModel"));
        holder.listWarehouseDetailQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.listWarehouseDetailQuantityPcs.setText((String)mData.get(i).get("QuantityPcs"));

        return view;
    }

    public static class WarehouseDetailViewHolder{
        TextView listWarehouseDetailPosition,listWarehouseDetailFlag,listWarehouseDetailProductCode,listWarehouseDetailProductName,listWarehouseDetailProductModel,listWarehouseDetailQuantity,listWarehouseDetailQuantityPcs;
    }
}
