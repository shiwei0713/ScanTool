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

public class LabelListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public LabelListAdapter(List<Map<String,Object>> mData,Context mContext){
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

    /**
    *描述: 汇总计算标签数量
    *日期：2022/5/30
    **/
    public int getTotal(String sType){
        int iTotal = 0;

        for(int i=0;i<mData.size();i++){
            String sTray = (String)mData.get(i).get("Tray");

            if(sTray.equals(sType)){
                int iQuantity = (int)mData.get(i).get("Quantity");
                iTotal = iTotal + iQuantity;
            }
        }

        return iTotal;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LabelViewHolder holder = null;

        if(view == null) {
            holder = new LabelViewHolder();
            //实例化组件,获取组件
            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_label_list, viewGroup, false);

            //初始化控件
            intiView(holder,view);

            view.setTag(holder);
        }else{
            holder = (LabelViewHolder)view.getTag();
        }

        //设置组件显示值
        getData(holder,i,view);

        return view;
    }

    /**
     *描述: 定义ListItem控件
     *日期：2022/5/26
     **/
    public static class LabelViewHolder{
        TextView txtLabelTray,txtLabelProductName,txtLabelProductCode,txtLabelModels;
        TextView txtLabelQuantity,txtLabelQuantityPcs;
        ImageView imgLabelIcon,imgLabelStatus;

    }

    /**
     *描述: 初始化ListItem控件
     *日期：2022/5/28
     **/
    private void intiView(LabelViewHolder holder, View view){
        holder.txtLabelTray = view.findViewById(R.id.txtLabelTray);
        holder.txtLabelProductName = view.findViewById(R.id.txtLabelProductName);
        holder.txtLabelProductCode = view.findViewById(R.id.txtLabelProductCode);
        holder.txtLabelModels = view.findViewById(R.id.txtLabelModels);
        holder.txtLabelQuantity = view.findViewById(R.id.txtLabelQuantity);
        holder.txtLabelQuantityPcs = view.findViewById(R.id.txtLabelQuantityPcs);

        holder.imgLabelIcon = view.findViewById(R.id.imgLabelIcon);
        holder.imgLabelStatus = view.findViewById(R.id.imgLabelStatus);
    }

    /**
     *描述: 获取ListItem值
     *日期：2022/5/28
     **/
    private void getData(LabelViewHolder holder, int i, View view){
        //设置组件显示值
        holder.txtLabelTray.setText((String)mData.get(i).get("Docno"));
        holder.txtLabelProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtLabelProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtLabelModels.setText((String)mData.get(i).get("ProductModels"));
        int iQuantity = (int)mData.get(i).get("Quantity");
        int iQuantityPcs = (int)mData.get(i).get("QuantityPcs");
        holder.txtLabelQuantity.setText(String.valueOf(iQuantity));
        holder.txtLabelQuantityPcs.setText(String.valueOf(iQuantityPcs));
        holder.imgLabelIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
    }
}
