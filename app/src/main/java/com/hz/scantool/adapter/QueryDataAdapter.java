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

public class QueryDataAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public QueryDataAdapter(List<Map<String,Object>> mData, Context mContext){
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
        QueryDataViewHolder holder = null;
        if(view == null){
            holder = new QueryDataViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_query_data,viewGroup,false);

            holder.listQueryDataProductName = view.findViewById(R.id.listQueryDataProductName);
            holder.listQueryDataQuantity = view.findViewById(R.id.listQueryDataQuantity);
            holder.listQueryDataUnit = view.findViewById(R.id.listQueryDataUnit);
            holder.listQueryDataProductCode = view.findViewById(R.id.listQueryDataProductCode);
            holder.listQueryDataProductModel = view.findViewById(R.id.listQueryDataProductModel);
            holder.listQueryDataArea = view.findViewById(R.id.listQueryDataArea);
            holder.listQueryDataProcess = view.findViewById(R.id.listQueryDataProcess);
            holder.listQueryDataFlag = view.findViewById(R.id.listQueryDataFlag);

            view.setTag(holder);
        }else{
            holder=(QueryDataViewHolder)view.getTag();
        }

        //值显示
        holder.listQueryDataProductName.setText((String)mData.get(i).get("ProductName"));
        holder.listQueryDataQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.listQueryDataUnit.setText((String)mData.get(i).get("Unit"));
        holder.listQueryDataProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.listQueryDataProductModel.setText((String)mData.get(i).get("ProductModel"));
        holder.listQueryDataArea.setText((String)mData.get(i).get("Area"));
        holder.listQueryDataProcess.setText((String)mData.get(i).get("Process"));
        String sFlag = (String)mData.get(i).get("Flag");
        if(sFlag.equals("101")){
            holder.listQueryDataFlag.setText("原材料");
        }else if(sFlag.equals("105")){
            holder.listQueryDataFlag.setText("成品");
        }else{
            holder.listQueryDataFlag.setText("半成品");
        }

        return view;
    }

    public static class QueryDataViewHolder{
        TextView listQueryDataProductName,listQueryDataQuantity,listQueryDataUnit,listQueryDataProductCode,listQueryDataProductModel,listQueryDataArea,listQueryDataProcess,listQueryDataFlag;
    }
}
