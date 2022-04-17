package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class SubMaterialAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public SubMaterialAdapter(List<Map<String,Object>> mData, Context mContext){
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
        SubMaterialViewHolder holder = null;
        if(view == null){
            holder = new SubMaterialViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_material_sub,viewGroup,false);

            holder.imgMaterialStatus = view.findViewById(R.id.imgMaterialStatus);
            holder.txtMaterialProductCode = view.findViewById(R.id.txtMaterialProductCode);
            holder.txtViewProductName = view.findViewById(R.id.txtViewProductName);
            holder.txtViewProductModel = view.findViewById(R.id.txtViewProductModel);
            holder.txtViewProductUrl = view.findViewById(R.id.txtViewProductUrl);

            view.setTag(holder);
        }else{
            holder=(SubMaterialViewHolder)view.getTag();
        }

        //值显示
        holder.imgMaterialStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.material_ok));
        holder.txtMaterialProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtViewProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtViewProductModel.setText((String)mData.get(i).get("ProductModels"));
        holder.txtViewProductUrl.setText((String)mData.get(i).get("Url"));
        holder.status = (String)mData.get(i).get("Status");

        if(holder.status.equals("104")){
            holder.imgMaterialStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.material_ng));
        }

        return view;
    }

    public static class SubMaterialViewHolder{
        ImageView imgMaterialStatus;
        TextView txtMaterialProductCode;
        TextView txtViewProductName;
        TextView txtViewProductModel;
        TextView txtViewProductUrl;

        String status;
    }
}
