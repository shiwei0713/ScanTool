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

public class QueryAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public QueryAdapter(List<Map<String,Object>> mData, Context mContext){
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
        ProductMaterialViewHolder holder = null;
        if(view == null){
            holder = new ProductMaterialViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_query_product,viewGroup,false);

            holder.listQueryDate = view.findViewById(R.id.listQueryDate);
            holder.listQueryProductName = view.findViewById(R.id.listQueryProductName);
            holder.listQueryProcess = view.findViewById(R.id.listQueryProcess);
            holder.listQueryQuantity = view.findViewById(R.id.listQueryQuantity);

            view.setTag(holder);
        }else{
            holder=(ProductMaterialViewHolder)view.getTag();
        }

        //值显示
        holder.listQueryDate.setText((String)mData.get(i).get("Date"));
        holder.listQueryProductName.setText((String)mData.get(i).get("ProductName"));
        holder.listQueryProcess.setText((String)mData.get(i).get("Process"));
        holder.listQueryQuantity.setText((String)mData.get(i).get("Quantity"));

        return view;
    }

    public static class ProductMaterialViewHolder{
        TextView listQueryDate,listQueryProductName,listQueryProcess,listQueryQuantity;
    }
}
