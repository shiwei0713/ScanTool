package com.hz.scantool.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class ProductMaterialAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public ProductMaterialAdapter(List<Map<String,Object>> mData, Context mContext){
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

            view = LayoutInflater.from(mContext).inflate(R.layout.list_product_material,viewGroup,false);

            holder.listMaterialSeq = view.findViewById(R.id.listMaterialSeq);
            holder.listMaterialProductCode = view.findViewById(R.id.listMaterialProductCode);
//            holder.listMaterialProcessId = view.findViewById(R.id.listMaterialProcessId);
            holder.listMaterialProcess = view.findViewById(R.id.listMaterialProcess);
            holder.listMaterialFlag = view.findViewById(R.id.listMaterialFlag);
            holder.listMaterialQuantity = view.findViewById(R.id.listMaterialQuantity);

            view.setTag(holder);
        }else{
            holder=(ProductMaterialViewHolder)view.getTag();
        }

        //值显示
        holder.listMaterialSeq.setText(String.valueOf(i+1));
        holder.listMaterialProductCode.setText((String)mData.get(i).get("ProductName"));
//        holder.listMaterialProcessId.setText((String)mData.get(i).get("ProcessId"));
        holder.listMaterialProcess.setText((String)mData.get(i).get("Process"));
        holder.listMaterialFlag.setText((String)mData.get(i).get("Attribute"));
        holder.listMaterialQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.sStatus = (String)mData.get(i).get("Status");

        //颜色区分状态
        if(holder.sStatus.equals("Y")){
            holder.listMaterialSeq.setTextColor(Color.RED);
            holder.listMaterialProductCode.setTextColor(Color.RED);
            holder.listMaterialProcessId.setTextColor(Color.RED);
            holder.listMaterialProcess.setTextColor(Color.RED);
            holder.listMaterialQuantity.setTextColor(Color.RED);
            holder.listMaterialFlag.setTextColor(Color.RED);
        }

        return view;
    }

    public static class ProductMaterialViewHolder{
        TextView listMaterialSeq;
        TextView listMaterialProductCode;
        TextView listMaterialProcessId;
        TextView listMaterialProcess;
        TextView listMaterialFlag;
        TextView listMaterialQuantity;

        String sStatus;

    }
}
