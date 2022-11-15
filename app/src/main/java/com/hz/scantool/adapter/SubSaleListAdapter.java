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

public class SubSaleListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public SubSaleListAdapter(List<Map<String,Object>> mData, Context mContext){
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
        SubSaleListViewHolder holder = null;
        if(view == null){
            holder = new SubSaleListViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_sub_sale_list,viewGroup,false);

            holder.imgLabelIcon = view.findViewById(R.id.imgLabelIcon);
            holder.txtSaler = view.findViewById(R.id.txtSaler);
            holder.txtDate = view.findViewById(R.id.txtDate);
            holder.txtDocno = view.findViewById(R.id.txtDocno);

            view.setTag(holder);
        }else{
            holder=(SubSaleListViewHolder)view.getTag();
        }

        holder.imgLabelIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
        holder.txtSaler.setText((String)mData.get(i).get("Producer"));
        holder.txtDate.setText((String)mData.get(i).get("PlanDate"));
        holder.txtDocno.setText((String)mData.get(i).get("Docno"));

        return view;
    }

    public static class SubSaleListViewHolder{
        ImageView imgLabelIcon;
        TextView txtSaler,txtDate,txtDocno;
    }
}
