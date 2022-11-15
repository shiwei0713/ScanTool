package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class IqcCheckListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private String mType;

    public IqcCheckListAdapter(List<Map<String,Object>> mData, Context mContext,String mType){
        this.mData = mData;
        this.mContext = mContext;
        this.mType = mType;
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
        IqcCheckListViewHolder holder = null;
        if(view == null){
            holder = new IqcCheckListViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_iqc_check,viewGroup,false);

            holder.listIqcSupply = view.findViewById(R.id.listIqcSupply);
            holder.listIqcFlag = view.findViewById(R.id.listIqcFlag);
            holder.listIqcProductCode = view.findViewById(R.id.listIqcProductCode);
            holder.listIqcProductName = view.findViewById(R.id.listIqcProductName);
            holder.listIqcProductModel = view.findViewById(R.id.listIqcProductModel);
            holder.listIqcStock = view.findViewById(R.id.listIqcStock);
            holder.listIqcQuantity = view.findViewById(R.id.listIqcQuantity);
            holder.listIqcUnit = view.findViewById(R.id.listIqcUnit);
            holder.listIqcDate = view.findViewById(R.id.listIqcDate);
            holder.listIqcLots = view.findViewById(R.id.listIqcLots);
            holder.listIqcProductNameTitle = view.findViewById(R.id.listIqcProductNameTitle);
            holder.listIqcProductModelTitle = view.findViewById(R.id.listIqcProductModelTitle);
            holder.linearProductCode = view.findViewById(R.id.linearProductCode);
            holder.linearProductName = view.findViewById(R.id.linearProductName);
            holder.linearProductModel = view.findViewById(R.id.linearProductModel);
            holder.linearStock = view.findViewById(R.id.linearStock);
            holder.linearLots = view.findViewById(R.id.linearLots);
            holder.listIqcLotsTitle = view.findViewById(R.id.listIqcLotsTitle);

            view.setTag(holder);
        }else{
            holder=(IqcCheckListViewHolder)view.getTag();
        }

        //动态显示控件
        showView(holder);

        //值显示
        String sProducer = (String)mData.get(i).get("Producer");
        if(sProducer.equals("")||sProducer.isEmpty()){
            sProducer = (String)mData.get(i).get("Storage");
        }
        holder.listIqcSupply.setText(sProducer);
        holder.listIqcFlag.setText(String.valueOf(i+1));
        holder.listIqcDate.setText((String)mData.get(i).get("PlanDate"));
        holder.listIqcProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.listIqcProductName.setText((String)mData.get(i).get("ProductName"));
        holder.listIqcProductModel.setText((String)mData.get(i).get("ProductModels"));
        holder.listIqcStock.setText((String)mData.get(i).get("Stock"));
        holder.listIqcQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.listIqcUnit.setText((String)mData.get(i).get("Unit"));
        holder.listIqcLots.setText((String)mData.get(i).get("Lot"));

        return view;
    }

    public static class IqcCheckListViewHolder{
        TextView listIqcSupply,listIqcFlag,listIqcProductCode,listIqcProductName,listIqcProductModel,listIqcStock,listIqcQuantity,listIqcUnit,listIqcDate,listIqcLots,listIqcLotsTitle;
        TextView listIqcProductNameTitle,listIqcProductModelTitle;
        LinearLayout linearProductCode,linearProductModel,linearProductName,linearStock,linearLots;
    }

    /**
    *描述: 动态显示/隐藏控件
    *日期：2022/11/9
    **/
    private void showView(IqcCheckListViewHolder holder){
        holder.linearProductCode.setVisibility(View.VISIBLE);
        holder.linearProductName.setVisibility(View.VISIBLE);
        holder.linearProductModel.setVisibility(View.VISIBLE);
        holder.linearStock.setVisibility(View.VISIBLE);

        if(mType.equals("9")){
            holder.linearStock.setVisibility(View.GONE);
            holder.linearLots.setVisibility(View.GONE);
            holder.listIqcProductNameTitle.setText(R.string.iqc_check_adapter_label9);
            holder.listIqcProductModelTitle.setText(R.string.iqc_check_adapter_label10);
        }else{
            holder.listIqcProductNameTitle.setText(R.string.iqc_check_adapter_label2);
            holder.listIqcProductModelTitle.setText(R.string.iqc_check_adapter_label3);
        }
    }
}
