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

public class QueryProductAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public QueryProductAdapter(List<Map<String,Object>> mData, Context mContext){
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

            view = LayoutInflater.from(mContext).inflate(R.layout.list_query_produc_summary,viewGroup,false);

            holder.listQueryProductSummaryProductName = view.findViewById(R.id.listQueryProductSummaryProductName);
            holder.listQueryProductSummaryPlanDate = view.findViewById(R.id.listQueryProductSummaryPlanDate);
            holder.listQueryProductSummarySeq = view.findViewById(R.id.listQueryProductSummarySeq);
            holder.listQueryProductSummaryProductCode = view.findViewById(R.id.listQueryProductSummaryProductCode);
            holder.listQueryProductSummaryProcess = view.findViewById(R.id.listQueryProductSummaryProcess);
            holder.listQueryProductSummaryDevice = view.findViewById(R.id.listQueryProductSummaryDevice);
            holder.listQueryProductSummaryStartTime = view.findViewById(R.id.listQueryProductSummaryStartTime);
            holder.listQueryProductSummaryEndTime = view.findViewById(R.id.listQueryProductSummaryEndTime);
            holder.listQueryProductSummaryError = view.findViewById(R.id.listQueryProductSummaryError);
            holder.listQueryProductSummaryCount = view.findViewById(R.id.listQueryProductSummaryCount);
            holder.listQueryProductSummaryPlanQuantity = view.findViewById(R.id.listQueryProductSummaryPlanQuantity);
            holder.listQueryProductSummaryQuantity = view.findViewById(R.id.listQueryProductSummaryQuantity);
            holder.listQueryProductSummaryBadQuantity = view.findViewById(R.id.listQueryProductSummaryBadQuantity);
            holder.listQueryProductSummaryEmployee = view.findViewById(R.id.listQueryProductSummaryEmployee);

            view.setTag(holder);
        }else{
            holder=(QueryDataViewHolder)view.getTag();
        }

        //值显示
        holder.listQueryProductSummaryProductName.setText((String)mData.get(i).get("ProductName"));
        holder.listQueryProductSummaryPlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.listQueryProductSummarySeq.setText(String.valueOf(i+1));
        holder.listQueryProductSummaryProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.listQueryProductSummaryProcess.setText((String)mData.get(i).get("Process"));
        holder.listQueryProductSummaryDevice.setText((String)mData.get(i).get("Device"));
        holder.listQueryProductSummaryStartTime.setText((String)mData.get(i).get("StartTime"));
        holder.listQueryProductSummaryEndTime.setText((String)mData.get(i).get("EndTime"));
        holder.listQueryProductSummaryError.setText((String)mData.get(i).get("ErrorMintiue"));
        holder.listQueryProductSummaryCount.setText((String)mData.get(i).get("MaterialCount"));
        holder.listQueryProductSummaryPlanQuantity.setText((String)mData.get(i).get("PlanQuantity"));
        holder.listQueryProductSummaryQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.listQueryProductSummaryBadQuantity.setText((String)mData.get(i).get("BadQuantity"));
        holder.listQueryProductSummaryEmployee.setText((String)mData.get(i).get("Employee"));

        return view;
    }

    public static class QueryDataViewHolder{
        TextView listQueryProductSummaryProductName,listQueryProductSummaryPlanDate,listQueryProductSummarySeq,listQueryProductSummaryProductCode;
        TextView listQueryProductSummaryProcess,listQueryProductSummaryDevice,listQueryProductSummaryStartTime,listQueryProductSummaryEndTime,listQueryProductSummaryError;
        TextView listQueryProductSummaryCount,listQueryProductSummaryPlanQuantity,listQueryProductSummaryQuantity,listQueryProductSummaryBadQuantity,listQueryProductSummaryEmployee;
    }
}
