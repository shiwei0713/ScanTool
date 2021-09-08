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

public class MainListItemAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public MainListItemAdapter(List<Map<String,Object>> mData,Context mContext){
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public int getCount(String strStatus){
        int mCount = 0;

        for(Map<String,Object> data: mData){
            String mStatus = data.get("Status").toString();
            if(mStatus.equals(strStatus)){
                mCount += 1;
            }
        }

        return mCount;
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
        MainViewHolder holder = null;

        if(view == null) {
            holder = new MainViewHolder();
            //实例化组件,获取组件
            view = LayoutInflater.from(mContext).inflate(R.layout.main_list_item, viewGroup, false);

            holder.imageMainListIcon = view.findViewById(R.id.imageMainListIcon);
            holder.imageMainListDateIcon = view.findViewById(R.id.imageMainListDateIcon);

            holder.txtMainListProductCodeTitle = view.findViewById(R.id.txtMainListProductCodeTitle);
            holder.txtMainListProductModelsTitle = view.findViewById(R.id.txtMainListProductModelsTitle);
            holder.txtMainListStorageTitle = view.findViewById(R.id.txtMainListStorageTitle);
            holder.txtMainListStockIdTitle = view.findViewById(R.id.txtMainListStockIdTitle);
            holder.txtMainListStockTitle = view.findViewById(R.id.txtMainListStockTitle);
            holder.txtMainListDocnoTitle = view.findViewById(R.id.txtMainListDocnoTitle);
            holder.txtMainListQuantityTitle = view.findViewById(R.id.txtMainListQuantityTitle);
            holder.txtMainListQuantityPcsTitle = view.findViewById(R.id.txtMainListQuantityPcsTitle);

            holder.txtMainListProductCode = view.findViewById(R.id.txtMainListProductCode);
            holder.txtMainListProductName = view.findViewById(R.id.txtMainListProductName);
            holder.txtMainListPlanDate = view.findViewById(R.id.txtMainListPlanDate);
            holder.txtMainListProductModels = view.findViewById(R.id.txtMainListProductModels);
            holder.txtMainListStorageId = view.findViewById(R.id.txtMainListStorageId);
            holder.txtMainListStorage = view.findViewById(R.id.txtMainListStorage);
            holder.txtMainListStockId = view.findViewById(R.id.txtMainListStockId);
            holder.txtMainListStock = view.findViewById(R.id.txtMainListStock);
            holder.txtMainListDocno = view.findViewById(R.id.txtMainListDocno);
            holder.txtMainListQuantity = view.findViewById(R.id.txtMainListQuantity);
            holder.txtMainListQuantityPcs = view.findViewById(R.id.txtMainListQuantityPcs);

            view.setTag(holder);
        }else{
            holder = (MainViewHolder)view.getTag();
        }

        //初始化标题显示
        initViewTitle(holder);

        //设置组件显示值
        holder.imageMainListIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.list_top_icon));
        holder.imageMainListDateIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.list_alarm));

        holder.txtMainListProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtMainListProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtMainListPlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.txtMainListProductModels.setText((String)mData.get(i).get("ProductModels"));
        holder.txtMainListStorageId.setText((String)mData.get(i).get("StorageId"));
        holder.txtMainListStorage.setText((String)mData.get(i).get("Storage"));
        holder.txtMainListStockId.setText((String)mData.get(i).get("StockId"));
        holder.txtMainListStock.setText((String)mData.get(i).get("Stock"));
        holder.txtMainListDocno.setText((String)mData.get(i).get("Docno"));
        holder.strStatus = mData.get(i).get("Status").toString();

        //数量处理
        String strQuantity = mData.get(i).get("Quantity").toString();
        String strQuantityPcs = mData.get(i).get("QuantityPcs").toString();
        if(strQuantity.isEmpty()){
            strQuantity = "0";
        }
        if(strQuantityPcs.isEmpty()){
            strQuantityPcs = "0";
        }
        holder.txtMainListQuantity.setText(strQuantity);
        holder.txtMainListQuantityPcs.setText(strQuantityPcs);

        return view;
    }

    public static class MainViewHolder{
        TextView txtMainListProductCodeTitle;
        TextView txtMainListProductModelsTitle;
        TextView txtMainListStorageTitle;
        TextView txtMainListStockIdTitle;
        TextView txtMainListStockTitle;
        TextView txtMainListDocnoTitle;
        TextView txtMainListQuantityTitle;
        TextView txtMainListQuantityPcsTitle;

        TextView txtMainListProductName;
        TextView txtMainListPlanDate;
        TextView txtMainListProductCode;
        TextView txtMainListProductModels;
        TextView txtMainListStorageId;
        TextView txtMainListStorage;
        TextView txtMainListStockId;
        TextView txtMainListStock;
        TextView txtMainListDocno;
        TextView txtMainListQuantity;
        TextView txtMainListQuantityPcs;

        ImageView imageMainListIcon;
        ImageView imageMainListDateIcon;

        String strStatus;
    }

    private void initViewTitle(MainViewHolder holder){
        //设置标题显示值
        String strMainProductCodeTitle = mContext.getResources().getString(R.string.item_title_product_code);
        String strMainQuantityTitle = mContext.getResources().getString(R.string.item_title_inventory);
        String strMainQuantityPcsTitle = mContext.getResources().getString(R.string.item_title_label);
        String strMainDocnoTitle = mContext.getResources().getString(R.string.item_title_docno);
        String strMainProducerTitle = mContext.getResources().getString(R.string.item_title_storage);
        String strMainProductModels = mContext.getResources().getString(R.string.item_title_models);
        String strMainStockIdTitle = mContext.getResources().getString(R.string.item_title_stockid);
        String strMainStockTitle = mContext.getResources().getString(R.string.item_title_stock);


        //设置标题显示值
        holder.txtMainListProductCodeTitle.setText(strMainProductCodeTitle);
        holder.txtMainListProductModelsTitle.setText(strMainProductModels);
        holder.txtMainListStorageTitle.setText(strMainProducerTitle);
        holder.txtMainListStockIdTitle.setText(strMainStockIdTitle);
        holder.txtMainListStockTitle.setText(strMainStockTitle);
        holder.txtMainListDocnoTitle.setText(strMainDocnoTitle);
        holder.txtMainListQuantityTitle.setText(strMainQuantityTitle);
        holder.txtMainListQuantityPcsTitle.setText(strMainQuantityPcsTitle);
    }
}
