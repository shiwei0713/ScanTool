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

public class SubAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public SubAdapter(List<Map<String,Object>> mData, Context mContext){
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
        SubViewHolder holder = null;
        if(view == null){
            holder = new SubViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_show_sub,viewGroup,false);

            holder.imageListViewIcon = view.findViewById(R.id.imageListViewIcon);
            holder.listStatus = view.findViewById(R.id.listStatus);
            holder.txtPlanDate = view.findViewById(R.id.txtPlanDate);
            holder.txtProductName = view.findViewById(R.id.txtProductName);
            holder.txtProductCode = view.findViewById(R.id.txtProductCode);
            holder.txtProductModels = view.findViewById(R.id.txtProductModels);
            holder.txtProcess= view.findViewById(R.id.txtProcess);
            holder.txtDevice= view.findViewById(R.id.txtDevice);
            holder.txtDocno = view.findViewById(R.id.txtDocno);
            holder.txtQuantity = view.findViewById(R.id.txtQuantity);

            view.setTag(holder);
        }else{
            holder=(SubViewHolder)view.getTag();
        }

        //值显示
        holder.imageListViewIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
        holder.listStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.list_alarm));
        holder.txtPlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.txtProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtProductModels.setText((String)mData.get(i).get("ProductModels"));
        holder.txtProcess.setText((String)mData.get(i).get("Process"));
        holder.txtDevice.setText((String)mData.get(i).get("Device"));
        holder.txtDocno.setText((String)mData.get(i).get("Docno"));
        holder.txtQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.status = (String)mData.get(i).get("DocType");

        return view;
    }

    public static class SubViewHolder{
        ImageView imageListViewIcon;
        ImageView listStatus;
        TextView txtPlanDate;
        TextView txtProductName;
        TextView txtProductCode;
        TextView txtProductModels;
        TextView txtProcess;
        TextView txtDevice;
        TextView txtQuantity;
        TextView txtDocno;

        String status;
    }
}
