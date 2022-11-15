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

public class PrintStockLabelListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public PrintStockLabelListAdapter(List<Map<String,Object>> mData, Context mContext){
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
        PrintStockLabelViewHolder holder = null;
        if(view == null){
            holder = new PrintStockLabelViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_print_stock_label,viewGroup,false);

            holder.txtLabelProductCode = view.findViewById(R.id.txtLabelProductCode);
            holder.txtLabelProductName = view.findViewById(R.id.txtLabelProductName);
            holder.txtLabelProductModels = view.findViewById(R.id.txtLabelProductModels);
            holder.txtLabelStockId = view.findViewById(R.id.txtLabelStockId);
            holder.txtLabelStock = view.findViewById(R.id.txtLabelStock);
            holder.txtLabelPositionId = view.findViewById(R.id.txtLabelPositionId);
            holder.txtLabelPosition = view.findViewById(R.id.txtLabelPosition);
            holder.txtLabelPackages = view.findViewById(R.id.txtLabelPackages);
            holder.txtLabelModPackages = view.findViewById(R.id.txtLabelModPackages);
            holder.txtLabelQuantity = view.findViewById(R.id.txtLabelQuantity);

            view.setTag(holder);
        }else{
            holder=(PrintStockLabelViewHolder)view.getTag();
        }

        //值显示
        holder.txtLabelProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtLabelProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtLabelProductModels.setText((String)mData.get(i).get("ProductModels"));
        holder.txtLabelStockId.setText((String)mData.get(i).get("StockId"));
        holder.txtLabelStock.setText((String)mData.get(i).get("Stock"));
        holder.txtLabelPositionId.setText((String)mData.get(i).get("PositionId"));
        holder.txtLabelPosition.setText((String)mData.get(i).get("Position"));
        holder.txtLabelPackages.setText((String)mData.get(i).get("Packages"));
        holder.txtLabelModPackages.setText((String)mData.get(i).get("ModPackages"));
        holder.txtLabelQuantity.setText((String)mData.get(i).get("Quantity"));

        return view;
    }

    public static class PrintStockLabelViewHolder{
        TextView txtLabelProductCode,txtLabelProductModels,txtLabelProductName,txtLabelStockId,txtLabelStock;
        TextView txtLabelPositionId,txtLabelPosition,txtLabelQuantity,txtLabelPackages,txtLabelModPackages;
    }
}
