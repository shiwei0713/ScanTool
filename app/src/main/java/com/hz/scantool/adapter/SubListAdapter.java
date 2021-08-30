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

public class SubListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public SubListAdapter(List<Map<String,Object>> mData, Context mContext){
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
        SubListViewHolder holder = null;
        if(view == null){
            holder = new SubListViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.sub_list_item,viewGroup,false);
            holder.imageViewLogo = view.findViewById(R.id.imageViewLogo);
            holder.txtViewDeptId = view.findViewById(R.id.txtViewDeptId);
            holder.txtViewDept = view.findViewById(R.id.txtViewDept);
            holder.txtViewStock = view.findViewById(R.id.txtViewStock);
            holder.txtViewDate = view.findViewById(R.id.txtViewDate);

            view.setTag(holder);
        }else{
            holder=(SubListViewHolder)view.getTag();
        }

        holder.txtViewDeptId.setText((String)mData.get(i).get("DeptId"));
        holder.txtViewDept.setText((String)mData.get(i).get("Dept"));
        holder.txtViewStock.setText((String)mData.get(i).get("Stock"));
        holder.txtViewDate.setText((String)mData.get(i).get("PlanDate"));
        holder.status = (String)mData.get(i).get("DocType");

        if(holder.status.equals("1")){
            holder.imageViewLogo.setImageDrawable(view.getResources().getDrawable(R.drawable.sub_detail_inside));
        }else{
            holder.imageViewLogo.setImageDrawable(view.getResources().getDrawable(R.drawable.sub_detail_outside));
        }

        return view;
    }

    public String getItem(int i,String s){
        return mData.get(i).get(s).toString();
    }

    public static class SubListViewHolder{
        ImageView imageViewLogo;
        TextView txtViewDeptId;
        TextView txtViewDept;
        TextView txtViewStock;
        TextView txtViewDate;
        String status;
    }
}
