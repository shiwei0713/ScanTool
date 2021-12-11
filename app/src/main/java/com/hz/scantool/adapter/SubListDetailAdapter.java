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
            holder.txtSubListItemProductSize = view.findViewById(R.id.txtSubListItemProductSize);
            holder.txtSubListItemDept = view.findViewById(R.id.txtSubListItemDept);
            holder.txtSubListItemQuantity = view.findViewById(R.id.txtSubListItemQuantity);
            holder.txtSubListItemQuantityPcs = view.findViewById(R.id.txtSubListItemQuantityPcs);
            holder.txtSubListItemScanQuantity = view.findViewById(R.id.txtSubListItemScanQuantity);
            holder.txtSubListItemScanQuantityPcs = view.findViewById(R.id.txtSubListItemScanQuantityPcs);
            holder.txtSubListItemBatch = view.findViewById(R.id.txtSubListItemBatch);
            holder.txtSubListItemWeight = view.findViewById(R.id.txtSubListItemWeight);
            holder.txtSubListItemStatus = view.findViewById(R.id.txtSubListItemStatus);
            holder.txtSubListItemProduct = view.findViewById(R.id.txtSubListItemProduct);

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
        holder.txtSubListItemProductSize.setText((String)mData.get(i).get("ProductSize"));
        holder.txtSubListItemQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.txtSubListItemQuantityPcs.setText((String)mData.get(i).get("QuantityPcs"));
        holder.txtSubListItemBatch.setText((String)mData.get(i).get("StockBatch"));
        holder.txtSubListItemWeight.setText((String)mData.get(i).get("Weight"));

        holder.txtSubListItemScanQuantity.setText((String)mData.get(i).get("ScanQuantity"));
        holder.txtSubListItemScanQuantityPcs.setText((String)mData.get(i).get("ScanQuantityPcs"));
        holder.txtSubListItemStatus.setText((String)mData.get(i).get("Status"));   //是否已经扫描成功
        holder.txtSubListItemProduct.setText((String)mData.get(i).get("Product"));

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
        holder.txtSubListItemWeight = view.findViewById(R.id.txtSubListItemWeight);

        return setmData(holder,index);
    }

    private String setmData(SubListDetailViewHolder holder,int index){
        String strStatus = "";
        float fScanQuantityOld = 0;
        float fScanQuantityNew= 0;
        float fScanQuantity= 0;
        float fQuantity= 0;
        float fScanWeight= 0;
        float fWeight= 0;

        Map<String, Object> map = mData.get(index);
        String strQuantity = holder.txtSubListItemQuantity.getText().toString();
        String strScanQuantity = holder.txtSubListItemScanQuantity.getText().toString();
        String strScanQuantityNew = map.get("ScanQuantity").toString();
        String strWeight = holder.txtSubListItemWeight.getText().toString();
        String strScanWeight = map.get("Weight").toString();
        String strItemStatus = map.get("Status").toString();
        if(strQuantity.isEmpty()){
            strQuantity = "0";
        }
        if(strScanQuantity.isEmpty()){
            strScanQuantity = "0";
        }
        if(strScanQuantityNew.isEmpty()){
            strScanQuantityNew = "0";
        }
        if(strWeight.isEmpty()){
            strWeight = "0";
        }
        if(strScanWeight.isEmpty()){
            strScanWeight = "0";
        }
        if(strItemStatus.isEmpty()){
            strItemStatus = "N";
        }

        fQuantity = Float.valueOf(strQuantity);
        fScanQuantityOld = Float.valueOf(strScanQuantity);
        fScanQuantityNew = Float.valueOf(strScanQuantityNew);
        fScanQuantity = fScanQuantityOld + fScanQuantityNew;

        fWeight = Float.valueOf(strWeight);
        fScanWeight = Float.valueOf(strScanWeight);

        if(strItemStatus.equals("N")){
            if(fWeight>=fScanWeight){
                holder.txtSubListItemStockLocation.setTextColor(mContext.getResources().getColor(R.color.master_loginout));
                holder.txtSubListItemScanQuantity.setText(String.valueOf(fScanQuantity));
                holder.txtSubListItemScanQuantityPcs.setText(map.get("ScanQuantityPcs").toString());
                strStatus = "Y";
                if(fWeight==fScanWeight){
                    strStatus = "S";
                    strItemStatus = "Y";
                    mData.get(index).put("Status",strItemStatus);
                    holder.txtSubListItemStatus.setText(map.get("Status").toString());
                }
            }else{
                strStatus = "X";
            }
        }else{
            strStatus = "Z";
        }

        return strStatus;

    }

    //更新单号数据
    public void updateDocno(int index, ListView listView,String strDocno){
        //获取第一个可见item项的位置
        int visiblePosition = listView.getFirstVisiblePosition();

        //获取指定位置的视图
        View view = listView.getChildAt(index-visiblePosition);
        SubListDetailViewHolder holder = (SubListDetailViewHolder)view.getTag();
        holder.txtSubListItemDocno = view.findViewById(R.id.txtSubListItemDocno);
        holder.txtSubListItemDocno.setText(strDocno);
    }

    public static class SubListDetailViewHolder {
        TextView txtSubListItemStockLocation;
        TextView txtSubListItemPlanDate;
        TextView txtSubListItemProductCode;
        TextView txtSubListItemProductName;
        TextView txtSubListItemProductModels;
        TextView txtSubListItemProductSize;
        TextView txtSubListItemDept;
        TextView txtSubListItemQuantity;
        TextView txtSubListItemQuantityPcs;
        TextView txtSubListItemScanQuantity;
        TextView txtSubListItemScanQuantityPcs;
        TextView txtSubListItemBatch;
        TextView txtSubListItemWeight;
        TextView txtSubListItemStatus;
        TextView txtSubListItemDocno;
        TextView txtSubListItemProduct;

        ImageView imgSubListIcon;
    }
}
