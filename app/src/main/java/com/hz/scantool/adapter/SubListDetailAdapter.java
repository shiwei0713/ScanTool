package com.hz.scantool.adapter;

import android.content.Context;
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

public class SubListDetailAdapter extends BaseAdapter {
    private List<Map<String,Object>> mData;
    private Context mContext;

    public SubListDetailAdapter(List<Map<String,Object>> mData, Context mContext){
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
        SubListDetailViewHolder holder = null;
        if(view==null){
            holder = new SubListDetailViewHolder();

            view= LayoutInflater.from(mContext).inflate(R.layout.sub_list_detail_item,viewGroup,false);

            holder.txtSubListItemStockLocation = view.findViewById(R.id.txtSubListItemStockLocation);
            holder.txtSubListItemPlanDate = view.findViewById(R.id.txtSubListItemPlanDate);
            holder.txtSubListItemProductCode = view.findViewById(R.id.txtSubListItemProductCode);
            holder.txtSubListItemProductName = view.findViewById(R.id.txtSubListItemProductName);
            holder.txtSubListItemProductModels = view.findViewById(R.id.txtSubListItemProductModels);
            holder.txtSubListItemDept = view.findViewById(R.id.txtSubListItemDept);
            holder.txtSubListItemQuantity = view.findViewById(R.id.txtSubListItemQuantity);
            holder.txtSubListItemQuantityPcs = view.findViewById(R.id.txtSubListItemQuantityPcs);
            holder.txtSubListItemScanQuantity = view.findViewById(R.id.txtSubListItemScanQuantity);
            holder.txtSubListItemScanQuantityPcs = view.findViewById(R.id.txtSubListItemScanQuantityPcs);

            holder.imgSubListIcon=view.findViewById(R.id.imgSubListIcon);

            view.setTag(holder);

        }else{
            holder = (SubListDetailViewHolder)view.getTag();
        }

        holder.imgSubListIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
        holder.txtSubListItemStockLocation.setText((String)mData.get(i).get("StockLocation"));
        holder.txtSubListItemPlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.txtSubListItemProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtSubListItemProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtSubListItemProductModels.setText((String)mData.get(i).get("ProductModels"));
        holder.txtSubListItemQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.txtSubListItemQuantityPcs.setText((String)mData.get(i).get("QuantityPcs"));

        holder.txtSubListItemScanQuantity.setText((String)mData.get(i).get("ScanQuantity"));
        holder.txtSubListItemScanQuantityPcs.setText((String)mData.get(i).get("ScanQuantityPcs"));

        int fScanQuantity = Integer.parseInt(mData.get(i).get("ScanQuantity").toString());
        if(fScanQuantity>0){
            holder.txtSubListItemStockLocation.setTextColor(mContext.getResources().getColor(R.color.master_loginout));
        }

        return view;
    }

    //更新数据
    public String updateData(int index, ListView listView){
        //获取第一个可见item项的位置
        int visiblePosition = listView.getFirstVisiblePosition();

        //获取指定位置的视图
        View view = listView.getChildAt(index-visiblePosition);
        SubListDetailViewHolder holder = (SubListDetailViewHolder)view.getTag();
        holder.txtSubListItemStockLocation = view.findViewById(R.id.txtSubListItemStockLocation);
        holder.txtSubListItemQuantity = view.findViewById(R.id.txtSubListItemQuantity);
        holder.txtSubListItemScanQuantity = view.findViewById(R.id.txtSubListItemScanQuantity);
        holder.txtSubListItemScanQuantityPcs = view.findViewById(R.id.txtSubListItemScanQuantityPcs);

        return setmData(holder,index);
    }

    private String setmData(SubListDetailViewHolder holder,int index){
        String strStatus;
        int fScanQuantityOld;
        int fScanQuantityNew;
        int fScanQuantity;
        int fQuantity;

        Map<String, Object> map = mData.get(index);
        fQuantity = Integer.valueOf(holder.txtSubListItemQuantity.getText().toString());
        fScanQuantityOld = Integer.valueOf(holder.txtSubListItemScanQuantity.getText().toString());
        fScanQuantityNew = Integer.valueOf(map.get("ScanQuantity").toString());
        fScanQuantity = fScanQuantityOld + fScanQuantityNew;

        if(fQuantity>=fScanQuantity){
            holder.txtSubListItemStockLocation.setTextColor(mContext.getResources().getColor(R.color.master_loginout));
            holder.txtSubListItemScanQuantity.setText(String.valueOf(fScanQuantity));
            holder.txtSubListItemScanQuantityPcs.setText(map.get("ScanQuantityPcs").toString());
            strStatus = "Y";
            if(fQuantity==fScanQuantity){
                strStatus = "S";
            }
        }else{
            strStatus = "X";
        }

        return strStatus;

    }

    public static class SubListDetailViewHolder {
        TextView txtSubListItemStockLocation;
        TextView txtSubListItemPlanDate;
        TextView txtSubListItemProductCode;
        TextView txtSubListItemProductName;
        TextView txtSubListItemProductModels;
        TextView txtSubListItemDept;
        TextView txtSubListItemQuantity;
        TextView txtSubListItemQuantityPcs;
        TextView txtSubListItemScanQuantity;
        TextView txtSubListItemScanQuantityPcs;

        ImageView imgSubListIcon;

        String strStockLocation;
        String strProductCode;
        Float fQuantity;
    }
}
