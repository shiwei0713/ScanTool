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

public class WarehouseListAdapter extends BaseAdapter {
    private List<Map<String,Object>> mData;
    private Context mContext;
    private String mType;

    public WarehouseListAdapter(List<Map<String,Object>> mData, Context mContext,String mType){
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
        WarehouseListViewHolder holder = null;
        if(view == null){
            holder = new WarehouseListViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_warehouse,viewGroup,false);

            holder.listWarehouseFlag = view.findViewById(R.id.listWarehouseFlag);
            holder.listWarehouseDocno = view.findViewById(R.id.listWarehouseDocno);
            holder.listWarehouseDate = view.findViewById(R.id.listWarehouseDate);
            holder.listWarehouseDeptId = view.findViewById(R.id.listWarehouseDeptId);
            holder.listWarehouseDept = view.findViewById(R.id.listWarehouseDept);
            holder.listWarehouseStockId = view.findViewById(R.id.listWarehouseStockId);
            holder.listWarehouseStock = view.findViewById(R.id.listWarehouseStock);
            holder.listWarehouseDeptTitle = view.findViewById(R.id.listWarehouseDeptTitle);
            holder.listWarehouseStockTitle = view.findViewById(R.id.listWarehouseStockTitle);

            view.setTag(holder);
        }else{
            holder=(WarehouseListViewHolder)view.getTag();
        }

        //标题显示
        showView(holder);

        //值显示
        holder.listWarehouseFlag.setText(String.valueOf(i+1));
        holder.listWarehouseDocno.setText((String)mData.get(i).get("Docno"));
        holder.listWarehouseDate.setText((String)mData.get(i).get("PlanDate"));
        holder.listWarehouseDeptId.setText((String)mData.get(i).get("DeptId"));
        holder.listWarehouseDept.setText((String)mData.get(i).get("Dept"));
        holder.listWarehouseStockId.setText((String)mData.get(i).get("StockId"));
        holder.listWarehouseStock.setText((String)mData.get(i).get("Stock"));

        return view;
    }

    public static class WarehouseListViewHolder{
        TextView listWarehouseFlag,listWarehouseDocno,listWarehouseDate,listWarehouseDeptId,listWarehouseDept,listWarehouseStockId,listWarehouseStock;
        TextView listWarehouseDeptTitle,listWarehouseStockTitle;
    }

    /**
     *描述: 动态显示控件
     *日期：2022/11/14
     **/
    private void showView(WarehouseListViewHolder holder){
        if(mType.equals("9")){
            //退料
            holder.listWarehouseDeptTitle.setText(R.string.warehous_adapter_list_label3);
            holder.listWarehouseStockTitle.setText(R.string.warehous_adapter_list_label4);
        }else{
            //领料
            holder.listWarehouseDeptTitle.setText(R.string.warehous_adapter_list_label1);
            holder.listWarehouseStockTitle.setText(R.string.warehous_adapter_list_label2);
        }
    }
}
