package com.hz.scantool.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class LabelMergeListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public LabelMergeListAdapter(List<Map<String,Object>> mData, Context mContext){
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
        LabelMergeViewHolder holder = null;
        if(view == null){
            holder = new LabelMergeViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_label_merge,viewGroup,false);

            holder.listLabelMergeQrcode = view.findViewById(R.id.listLabelMergeQrcode);
            holder.listLabelMergeProductName = view.findViewById(R.id.listLabelMergeProductName);
            holder.listLabelMergeProcess = view.findViewById(R.id.listLabelMergeProcess);
            holder.listLabelMergeEmployee = view.findViewById(R.id.listLabelMergeEmployee);
            holder.listLabelMergeDevice = view.findViewById(R.id.listLabelMergeDevice);
            holder.listLabelMergePlanDate = view.findViewById(R.id.listLabelMergePlanDate);
            holder.listLabelMergeQty = view.findViewById(R.id.listLabelMergeQty);

            view.setTag(holder);
        }else{
            holder=(LabelMergeViewHolder)view.getTag();
        }

        //值显示
        holder.listLabelMergeQrcode.setText((String)mData.get(i).get("Qrcode"));
        holder.listLabelMergeProductName.setText((String)mData.get(i).get("ProductName"));
        holder.listLabelMergeProcess.setText((String)mData.get(i).get("Process"));
        holder.listLabelMergeEmployee.setText((String)mData.get(i).get("Employee"));
        holder.listLabelMergeDevice.setText((String)mData.get(i).get("Device"));
        holder.listLabelMergePlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.listLabelMergeQty.setText((String)mData.get(i).get("Quantity"));

        return view;
    }

    public static class LabelMergeViewHolder{
        TextView listLabelMergeQrcode;
        TextView listLabelMergeProductName;
        TextView listLabelMergeProcess;
        TextView listLabelMergeEmployee;
        TextView listLabelMergeDevice;
        TextView listLabelMergePlanDate;
        TextView listLabelMergeQty;

    }
}
