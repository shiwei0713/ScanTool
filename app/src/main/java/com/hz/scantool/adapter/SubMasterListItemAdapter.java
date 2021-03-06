package com.hz.scantool.adapter;

import android.content.Context;
import android.util.Log;
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


public class SubMasterListItemAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private String mType;
    private ConfirmClickListener mConfirmClickListener;
    private DeleteClickListener mDeleteClickListener;

    public SubMasterListItemAdapter(List<Map<String,Object>> mData, Context mContext,String mType){
        this.mData = mData;
        this.mContext = mContext;
        this.mType = mType;
    }

    public SubMasterListItemAdapter(List<Map<String,Object>> mData, Context mContext,String mType,ConfirmClickListener mConfirmClickListener,DeleteClickListener mDeleteClickListener){
        this.mData = mData;
        this.mContext = mContext;
        this.mType = mType;
        this.mConfirmClickListener = mConfirmClickListener;
        this.mDeleteClickListener = mDeleteClickListener;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(mType.equals("53")){
            convertView = initSaleViewHolder(position,convertView,parent);
        }else{
            convertView = initViewHolder(position,convertView,parent);
        }

        return convertView;
    }

    public String getItemValue(int position){
        return  (String)mData.get(position).get("Docno");
    }

    public static class ViewHolder{
        TextView txtSubContentListDocno;
        TextView txtSubContentListDate;
        TextView txtSubContentListProductCodeTitle;
        TextView txtSubContentListProductCode;
        TextView txtSubContentListProductNameTitle;
        TextView txtSubContentListProductName;
        TextView txtSubContentListProductModelsTitle;
        TextView txtSubContentListProductModels;
        TextView txtSubContentListProducerTitle;
        TextView txtSubContentListProducerId;
        TextView txtSubContentListProducer;
        TextView txtSubContentListStockId;
        TextView txtSubContentListStockTitle;

        TextView txtSubContentListStock;
        TextView txtSubContentListPlannoTitle;
        TextView txtSubContentListPlanno;
        TextView txtSubContentListQuantityTitle;
        TextView txtSubContentListQuantity;
        TextView txtSubContentListQuantityPcsTitle;
        TextView txtSubContentListQuantityPcs;

        Button txtSubContentListBtnConfirm;
        Button txtSubContentListBtnDeleteDoc;

        ImageView imgSubContentList;
        ImageView imgSubContentListAlarm;
        ImageView txtSubContentListStatus;
        String strStatus;
        String strDocStatus;
    }

    public static class SaleViewHolder{
        ImageView imgSubList;
        TextView txtSubListDocno;
        ImageView imgSubListAlarm;
        TextView txtSubListDate;
        TextView txtSubListProductCodeTitle;
        TextView txtSubListProductCode;
        TextView txtSubListProductNameTitle;
        TextView txtSubListProductName;
        TextView txtSubListProductModelsTitle;
        TextView txtSubListProductModels;
        TextView txtSubListProducerTitle;
        TextView txtSubListProducerId;
        TextView txtSubListProducer;
        TextView txtSubListQuantityTitle;
        TextView txtSubListQuantity;
        TextView txtSubListQuantityPcsTitle;
        TextView txtSubListQuantityPcs;
    }

    private View initViewHolder(int position, View convertView, ViewGroup parent){
        ViewHolder holder = null;

        if(convertView == null) {
            holder = new ViewHolder();
            //???????????????,????????????
            convertView = LayoutInflater.from(mContext).inflate(R.layout.sub_master_content_list_item, parent, false);

            holder.txtSubContentListDocno = convertView.findViewById(R.id.txtSubContentListDocno);
            holder.txtSubContentListDate = convertView.findViewById(R.id.txtSubContentListDate);
            holder.txtSubContentListProductCodeTitle = convertView.findViewById(R.id.txtSubContentListProductCodeTitle);
            holder.txtSubContentListProductCode = convertView.findViewById(R.id.txtSubContentListProductCode);
            holder.txtSubContentListProductNameTitle = convertView.findViewById(R.id.txtSubContentListProductNameTitle);
            holder.txtSubContentListProductName = convertView.findViewById(R.id.txtSubContentListProductName);
            holder.txtSubContentListProductModelsTitle = convertView.findViewById(R.id.txtSubContentListProductModelsTitle);
            holder.txtSubContentListProductModels = convertView.findViewById(R.id.txtSubContentListProductModels);
            holder.txtSubContentListProducerTitle = convertView.findViewById(R.id.txtSubContentListProducerTitle);
            holder.txtSubContentListProducerId = convertView.findViewById(R.id.txtSubContentListProducerId);
            holder.txtSubContentListProducer = convertView.findViewById(R.id.txtSubContentListProducer);
            holder.txtSubContentListStockId = convertView.findViewById(R.id.txtSubContentListStockId);
            holder.txtSubContentListStockTitle = convertView.findViewById(R.id.txtSubContentListStockTitle);
            holder.txtSubContentListStock = convertView.findViewById(R.id.txtSubContentListStock);
            holder.txtSubContentListPlannoTitle = convertView.findViewById(R.id.txtSubContentListPlannoTitle);
            holder.txtSubContentListPlanno = convertView.findViewById(R.id.txtSubContentListPlanno);
            holder.txtSubContentListQuantityTitle = convertView.findViewById(R.id.txtSubContentListQuantityTitle);
            holder.txtSubContentListQuantity = convertView.findViewById(R.id.txtSubContentListQuantity);
            holder.txtSubContentListQuantityPcsTitle = convertView.findViewById(R.id.txtSubContentListQuantityPcsTitle);
            holder.txtSubContentListQuantityPcs = convertView.findViewById(R.id.txtSubContentListQuantityPcs);

            holder.txtSubContentListBtnConfirm = convertView.findViewById(R.id.txtSubContentListBtnConfirm);
            holder.txtSubContentListBtnDeleteDoc = convertView.findViewById(R.id.txtSubContentListBtnDeleteDoc);
            holder.imgSubContentList = convertView.findViewById(R.id.imgSubContentList);
            holder.imgSubContentListAlarm = convertView.findViewById(R.id.imgSubContentListAlarm);
            holder.txtSubContentListStatus = convertView.findViewById(R.id.txtSubContentListStatus);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        //?????????????????????
        holder.txtSubContentListDocno.setText((String)mData.get(position).get("Docno"));
        holder.txtSubContentListDate.setText((String)mData.get(position).get("PlanDate"));
        holder.txtSubContentListProductCode.setText((String)mData.get(position).get("ProductCode"));
        holder.txtSubContentListProductName.setText((String)mData.get(position).get("ProductName"));
        holder.txtSubContentListProductModels.setText((String)mData.get(position).get("ProductModels"));
        holder.txtSubContentListProducerId.setText((String)mData.get(position).get("ProducerId"));
        holder.txtSubContentListProducer.setText((String)mData.get(position).get("Producer"));
        holder.txtSubContentListStockId.setText((String)mData.get(position).get("StockId"));
        holder.txtSubContentListStock.setText((String)mData.get(position).get("Stock"));
        holder.txtSubContentListPlanno.setText((String)mData.get(position).get("Planno"));
        holder.txtSubContentListQuantity.setText((String)mData.get(position).get("Quantity"));
        holder.txtSubContentListQuantityPcs.setText((String)mData.get(position).get("QuantityPcs"));

        //??????????????????
        holder.strStatus = (String)mData.get(position).get("Status");
        if(holder.strStatus.equals("Y")){
            holder.txtSubContentListBtnConfirm.setVisibility(View.GONE);
            holder.txtSubContentListStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_check));
        }else{
            if(mType.equals("1")){
                holder.txtSubContentListBtnConfirm.setVisibility(View.GONE);
            }else{
                holder.txtSubContentListBtnConfirm.setVisibility(View.VISIBLE);
            }

            holder.txtSubContentListStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_deal));
        }

        //??????logo??????
        holder.imgSubContentList.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_top_icon));
        holder.imgSubContentListAlarm.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_alarm));

        //????????????
        Log.i("strWhere",mType);
        if(mType.equals("21")){
            holder.strDocStatus = (String)mData.get(position).get("DocStatus");
            if(holder.strDocStatus.equals("Y")) {
                holder.txtSubContentListBtnConfirm.setVisibility(View.GONE);
            }else{
                holder.txtSubContentListBtnConfirm.setVisibility(View.VISIBLE);
            }
        }else{
            holder.txtSubContentListBtnConfirm.setVisibility(View.GONE);
        }

        //??????????????????
        //????????????
        holder.txtSubContentListBtnConfirm.setOnClickListener(mConfirmClickListener);
        holder.txtSubContentListBtnConfirm.setTag(position);
        //????????????
        holder.txtSubContentListBtnDeleteDoc.setOnClickListener(mDeleteClickListener);
        holder.txtSubContentListBtnDeleteDoc.setTag(position);

        return convertView;
    }

    private View initSaleViewHolder(int position, View convertView, ViewGroup parent){
        SaleViewHolder saleViewHolder = null;

        if(convertView == null) {
            saleViewHolder = new SaleViewHolder();
            //???????????????,????????????
            convertView = LayoutInflater.from(mContext).inflate(R.layout.sub_master_list_item, parent, false);

            saleViewHolder.txtSubListDocno = convertView.findViewById(R.id.txtSubListDocno);
            saleViewHolder.txtSubListDate = convertView.findViewById(R.id.txtSubListDate);
            saleViewHolder.txtSubListProductCodeTitle = convertView.findViewById(R.id.txtSubListProductCodeTitle);
            saleViewHolder.txtSubListProductCode = convertView.findViewById(R.id.txtSubListProductCode);
            saleViewHolder.txtSubListProductNameTitle = convertView.findViewById(R.id.txtSubListProductNameTitle);
            saleViewHolder.txtSubListProductName = convertView.findViewById(R.id.txtSubListProductName);
            saleViewHolder.txtSubListProductModelsTitle = convertView.findViewById(R.id.txtSubListProductModelsTitle);
            saleViewHolder.txtSubListProductModels = convertView.findViewById(R.id.txtSubListProductModels);
            saleViewHolder.txtSubListProducerTitle = convertView.findViewById(R.id.txtSubListProducerTitle);
            saleViewHolder.txtSubListProducerId = convertView.findViewById(R.id.txtSubListProducerId);
            saleViewHolder.txtSubListProducer = convertView.findViewById(R.id.txtSubListProducer);
            saleViewHolder.txtSubListQuantityTitle = convertView.findViewById(R.id.txtSubListQuantityTitle);
            saleViewHolder.txtSubListQuantity = convertView.findViewById(R.id.txtSubListQuantity);
            saleViewHolder.txtSubListQuantityPcsTitle = convertView.findViewById(R.id.txtSubListQuantityPcsTitle);
            saleViewHolder.txtSubListQuantityPcs = convertView.findViewById(R.id.txtSubListQuantityPcs);

            saleViewHolder.imgSubList = convertView.findViewById(R.id.imgSubList);
            saleViewHolder.imgSubListAlarm = convertView.findViewById(R.id.imgSubListAlarm);

            convertView.setTag(saleViewHolder);
        }else{
            saleViewHolder = (SaleViewHolder)convertView.getTag();
        }

        //?????????????????????
        saleViewHolder.txtSubListDocno.setText((String)mData.get(position).get("Docno"));
        saleViewHolder.txtSubListDate.setText((String)mData.get(position).get("PlanDate"));
        saleViewHolder.txtSubListProductCode.setText((String)mData.get(position).get("ProductCode"));
        saleViewHolder.txtSubListProductName.setText((String)mData.get(position).get("ProductName"));
        saleViewHolder.txtSubListProductModels.setText((String)mData.get(position).get("ProductModels"));
        saleViewHolder.txtSubListProducerId.setText((String)mData.get(position).get("ProducerId"));
        saleViewHolder.txtSubListProducer.setText((String)mData.get(position).get("Producer"));
        saleViewHolder.txtSubListQuantity.setText((String)mData.get(position).get("Quantity"));
        saleViewHolder.txtSubListQuantityPcs.setText((String)mData.get(position).get("QuantityPcs"));

        //??????logo??????
        saleViewHolder.imgSubList.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_top_icon));
        saleViewHolder.imgSubListAlarm.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_alarm));

        return convertView;
    }

    public static abstract class ConfirmClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            ConfirmOnClick((Integer) view.getTag(),view);
        }

        public abstract void ConfirmOnClick(int position,View v);
    }

    public static abstract class DeleteClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            DeleteClickListener((Integer) view.getTag(),view);
        }

        public abstract void DeleteClickListener(int position,View v);
    }
}

