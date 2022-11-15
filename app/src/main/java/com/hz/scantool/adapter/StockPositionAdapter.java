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

public class StockPositionAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public StockPositionAdapter(List<Map<String,Object>> mData,Context mContext){
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
        PositionViewHolder holder = null;

        if(view == null) {
            holder = new PositionViewHolder();
            //实例化组件,获取组件
            view = LayoutInflater.from(mContext).inflate(R.layout.list_stock_position, viewGroup, false);

            //初始化控件
            intiView(holder,view);

            view.setTag(holder);
        }else{
            holder = (PositionViewHolder)view.getTag();
        }

        //设置组件显示值
        getData(holder,i,view);

        return view;
    }

    /**
     *描述: 定义ListItem控件
     *日期：2022/7/13
     **/
    public static class PositionViewHolder{
        TextView txtListPositionDesc,txtListPosition;
    }

    /**
     *描述: 初始化ListItem控件
     *日期：2022/7/13
     **/
    private void intiView(PositionViewHolder holder, View view){
        holder.txtListPositionDesc = view.findViewById(R.id.txtListPositionDesc);
        holder.txtListPosition = view.findViewById(R.id.txtListPosition);
    }

    /**
     *描述: 获取ListItem值
     *日期：2022/7/13
     **/
    private void getData(PositionViewHolder holder, int i, View view){
        //设置组件显示值
        holder.txtListPositionDesc.setText((String)mData.get(i).get("Desc"));
        holder.txtListPosition.setText((String)mData.get(i).get("Position"));
    }
}
