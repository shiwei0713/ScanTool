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

public class DeliveryOrderListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public DeliveryOrderListAdapter(List<Map<String,Object>> mData,Context mContext){
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
        DeliveryOrderViewHolder holder = null;

        if(view == null) {
            holder = new DeliveryOrderViewHolder();
            //实例化组件,获取组件
            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_deliveryorder_list, viewGroup, false);

            //初始化控件
            intiView(holder,view);

            view.setTag(holder);
        }else{
            holder = (DeliveryOrderViewHolder)view.getTag();
        }

        //设置组件显示值
        getData(holder,i,view);

        return view;
    }

    /**
    *描述: 定义ListItem控件
    *日期：2022/5/26
    **/
    public static class DeliveryOrderViewHolder{
        TextView txtDeliveryOrderTray,txtDeliveryOrderProductCode,txtDeliveryOrderProductName,txtDeliveryOrderProductModels;
        TextView txtDeliveryOrderQuantity,txtDeliveryOrderQuantityPcs;
        ImageView imgDeliveryOrderIcon,imgDeliveryOrderStatus;

    }

    /**
    *描述: 初始化ListItem控件
    *日期：2022/5/26
    **/
    private void intiView(DeliveryOrderViewHolder holder,View view){
        holder.txtDeliveryOrderProductCode = view.findViewById(R.id.txtDeliveryOrderProductCode);
        holder.txtDeliveryOrderProductName = view.findViewById(R.id.txtDeliveryOrderProductName);
        holder.txtDeliveryOrderProductModels = view.findViewById(R.id.txtDeliveryOrderProductModels);
        holder.txtDeliveryOrderTray = view.findViewById(R.id.txtDeliveryOrderTray);
        holder.txtDeliveryOrderQuantity = view.findViewById(R.id.txtDeliveryOrderQuantity);
        holder.txtDeliveryOrderQuantityPcs = view.findViewById(R.id.txtDeliveryOrderQuantityPcs);

        holder.imgDeliveryOrderStatus = view.findViewById(R.id.imgDeliveryOrderStatus);
        holder.imgDeliveryOrderIcon = view.findViewById(R.id.imgDeliveryOrderIcon);
    }

    /**
    *描述: 获取ListItem值
    *日期：2022/5/26
    **/
    private void getData(DeliveryOrderViewHolder holder,int i,View view){
        //设置组件显示值
        holder.txtDeliveryOrderTray.setText((String)mData.get(i).get("Tray"));
        holder.txtDeliveryOrderProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtDeliveryOrderProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtDeliveryOrderProductModels.setText((String)mData.get(i).get("ProductModels"));
        int iQuantity = (int)mData.get(i).get("Quantity");
        int iQuantityPcs = (int)mData.get(i).get("QuantityPcs");
        holder.txtDeliveryOrderQuantity.setText(String.valueOf(iQuantity));
        holder.txtDeliveryOrderQuantityPcs.setText(String.valueOf(iQuantityPcs));
        holder.imgDeliveryOrderIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
    }

}
