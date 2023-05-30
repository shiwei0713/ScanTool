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

            holder.listMaterialQpa = view.findViewById(R.id.listMaterialQpa);
            holder.listMaterialProductCode = view.findViewById(R.id.listMaterialProductCode);
            holder.listMaterialProductName = view.findViewById(R.id.listMaterialProductName);
            holder.listMaterialProcess = view.findViewById(R.id.listMaterialProcess);
            holder.listMaterialFlag = view.findViewById(R.id.listMaterialFlag);
            holder.listMaterialQuantity = view.findViewById(R.id.listMaterialQuantity);
            holder.listMaterialUsedQuantity = view.findViewById(R.id.listMaterialUsedQuantity);
            holder.listMaterialUnUsedQuantity = view.findViewById(R.id.listMaterialUnUsedQuantity);
            holder.listMaterialPrintQuantity = view.findViewById(R.id.listMaterialPrintQuantity);
            holder.listMaterialAvialQuantity = view.findViewById(R.id.listMaterialAvialQuantity);
            holder.listMaterialDiffQuantity = view.findViewById(R.id.listMaterialDiffQuantity);
            holder.listMaterialBadQuantity = view.findViewById(R.id.listMaterialBadQuantity);
            holder.listMaterialEmp = view.findViewById(R.id.listMaterialEmp);
            holder.listMaterialProductType = view.findViewById(R.id.listMaterialProductType);
            holder.listMaterialProductDocno = view.findViewById(R.id.listMaterialProductDocno);

            view.setTag(holder);
        }else{
            holder=(ProductMaterialViewHolder)view.getTag();
        }

        //值显示
        String sFlag = (String)mData.get(i).get("Attribute");
        holder.listMaterialQpa.setText((String)mData.get(i).get("Qpa"));
        holder.listMaterialProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.listMaterialProductName.setText((String)mData.get(i).get("ProductName"));
        holder.listMaterialProcess.setText((String)mData.get(i).get("Process"));
        holder.listMaterialFlag.setText(sFlag);
        holder.listMaterialQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.listMaterialUsedQuantity.setText((String)mData.get(i).get("UsedQuantity"));
        holder.listMaterialUnUsedQuantity.setText((String)mData.get(i).get("UnUsedQuantity"));
        holder.listMaterialPrintQuantity.setText((String)mData.get(i).get("PrintQuantity"));
        holder.listMaterialAvialQuantity.setText((String)mData.get(i).get("AvialQuantity"));
        holder.listMaterialDiffQuantity.setText((String)mData.get(i).get("DiffQuantity"));
        holder.listMaterialBadQuantity.setText((String)mData.get(i).get("BadQuantity"));
        holder.listMaterialProductDocno.setText((String)mData.get(i).get("Docno"));
        holder.sStatus = (String)mData.get(i).get("Status");
        holder.listMaterialEmp.setText((String)mData.get(i).get("Employee"));
        if(sFlag.equals("BL")){
            holder.listMaterialProductType.setText("半成品");
        }else{
            holder.listMaterialProductType.setText("工序件");
        }

        //颜色区分状态
        if(holder.sStatus.equals("Y")){
            holder.listMaterialQpa.setTextColor(Color.RED);
            holder.listMaterialProductCode.setTextColor(Color.RED);
            holder.listMaterialProcessId.setTextColor(Color.RED);
            holder.listMaterialProcess.setTextColor(Color.RED);
            holder.listMaterialQuantity.setTextColor(Color.RED);
            holder.listMaterialFlag.setTextColor(Color.RED);
        }

        return view;
    }

    public static class ProductMaterialViewHolder{
        TextView listMaterialQpa;
        TextView listMaterialProductCode,listMaterialProductName,listMaterialProductType;
        TextView listMaterialProcessId;
        TextView listMaterialProcess;
        TextView listMaterialFlag,listMaterialEmp;
        TextView listMaterialQuantity,listMaterialUsedQuantity,listMaterialUnUsedQuantity;
        TextView listMaterialPrintQuantity,listMaterialAvialQuantity,listMaterialDiffQuantity,listMaterialBadQuantity,listMaterialProductDocno;

        String sStatus;

    }
}
