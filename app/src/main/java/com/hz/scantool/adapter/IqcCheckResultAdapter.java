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

public class IqcCheckResultAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public IqcCheckResultAdapter(List<Map<String,Object>> mData, Context mContext){
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
        IqcCheckResultViewHolder holder = null;
        if(view == null){
            holder = new IqcCheckResultViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_iqc_check_result,viewGroup,false);

            holder.listIqcCheckDate = view.findViewById(R.id.listIqcCheckDate);
            holder.listIqcCheckProductName = view.findViewById(R.id.listIqcCheckProductName);
            holder.listIqcCheckQuantity = view.findViewById(R.id.listIqcCheckQuantity);

            view.setTag(holder);
        }else{
            holder=(IqcCheckResultViewHolder)view.getTag();
        }

        //值显示
        holder.listIqcCheckDate.setText((String)mData.get(i).get("Date"));
        holder.listIqcCheckProductName.setText((String)mData.get(i).get("ProductName"));
        float fQuantity = (float)mData.get(i).get("Quantity");
        holder.listIqcCheckQuantity.setText(String.valueOf(fQuantity));

        return view;
    }

    public static class IqcCheckResultViewHolder{
        TextView listIqcCheckDate,listIqcCheckProductName,listIqcCheckQuantity;
    }
}
