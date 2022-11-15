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

public class SearchSelectAdapter extends BaseAdapter {

    private List<String> mData;
    private Context mContext;
    private LayoutInflater inflater;

    public SearchSelectAdapter(Context mContext,List<String> mData){
        this.mData = mData;
        this.mContext = mContext;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int i) {
        String sItem = mData.get(i);
        int index = sItem.indexOf(":");
        String sReturnItem;
        if(index!=-1){
            String[] arrItem = sItem.split(":");
            sReturnItem = arrItem[0];
        }else{
            sReturnItem = sItem;
        }

        return sReturnItem;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SearchSelectViewHolder holder = null;
        if(view == null){
            view = inflater.inflate(R.layout.list_select_single_device, null);
            holder = new SearchSelectViewHolder(view);
            view.setTag(holder);
        }else{
            holder=(SearchSelectViewHolder)view.getTag();
        }

        holder.info.setText(mData.get(i));

        return view;
    }

    public static class SearchSelectViewHolder{
        TextView info;
        public SearchSelectViewHolder(View view) {
            info = view.findViewById(R.id.tv_select_info);
        }
    }
}
