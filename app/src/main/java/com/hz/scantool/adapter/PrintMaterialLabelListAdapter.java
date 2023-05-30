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

public class PrintMaterialLabelListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public PrintMaterialLabelListAdapter(List<Map<String,Object>> mData, Context mContext){
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
        PrintMaterialViewHolder holder = null;
        if(view == null){
            holder = new PrintMaterialViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_material_label_print,viewGroup,false);

            holder.listMaterialLabelProductName = view.findViewById(R.id.listMaterialLabelProductName);
            holder.listMaterialLabelSeq = view.findViewById(R.id.listMaterialLabelSeq);
            holder.listMaterialLabelProductCode = view.findViewById(R.id.listMaterialLabelProductCode);
            holder.listMaterialLabelProductModel = view.findViewById(R.id.listMaterialLabelProductModel);
            holder.listMaterialLabelMaterialCode = view.findViewById(R.id.listMaterialLabelMaterialCode);
            holder.listMaterialLabelMaterialName = view.findViewById(R.id.listMaterialLabelMaterialName);
            holder.listMaterialLabelMaterialModel = view.findViewById(R.id.listMaterialLabelMaterialModel);
            holder.listMaterialLabelQpa = view.findViewById(R.id.listMaterialLabelQpa);
            holder.listMaterialLabelMaterialSize= view.findViewById(R.id.listMaterialLabelMaterialSize);

            view.setTag(holder);
        }else{
            holder=(PrintMaterialViewHolder)view.getTag();
        }

        //值显示
        holder.listMaterialLabelProductName.setText((String)mData.get(i).get("ProductName"));
        holder.listMaterialLabelSeq.setText(String.valueOf(i+1));
        holder.listMaterialLabelProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.listMaterialLabelProductModel.setText((String)mData.get(i).get("ProductModels"));
        holder.listMaterialLabelMaterialCode.setText((String)mData.get(i).get("MaterialCode"));
        holder.listMaterialLabelMaterialName.setText((String)mData.get(i).get("MaterialName"));
        holder.listMaterialLabelMaterialModel.setText((String)mData.get(i).get("MaterialModels"));
        holder.listMaterialLabelQpa.setText((String)mData.get(i).get("Qpa"));
        holder.listMaterialLabelMaterialSize.setText((String)mData.get(i).get("MaterialSize"));

        return view;
    }

    public static class PrintMaterialViewHolder{
        TextView listMaterialLabelProductName,listMaterialLabelSeq,listMaterialLabelProductCode,listMaterialLabelProductModel,listMaterialLabelMaterialCode,listMaterialLabelMaterialName,listMaterialLabelMaterialModel,listMaterialLabelQpa,listMaterialLabelMaterialSize;
    }
}
