package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class SubListAdapter extends BaseAdapter {

    private String mType;
    private List<Map<String,Object>> mData;
    private Context mContext;

    public SubListAdapter(List<Map<String,Object>> mData, Context mContext,String mType){
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
        SubListViewHolder holder = null;
        if(view == null){
            holder = new SubListViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.sub_list_item,viewGroup,false);

            holder.imageViewLogo = view.findViewById(R.id.imageViewLogo);
            holder.imageViewStatus = view.findViewById(R.id.imageViewStatus);
            holder.txtViewDeptTtile = view.findViewById(R.id.txtViewDeptTtile);
            holder.txtViewDeptId = view.findViewById(R.id.txtViewDeptId);
            holder.txtViewDept = view.findViewById(R.id.txtViewDept);
            holder.txtViewStockTitle= view.findViewById(R.id.txtViewStockTitle);
            holder.txtViewStockId= view.findViewById(R.id.txtViewStockId);
            holder.txtViewStock = view.findViewById(R.id.txtViewStock);
            holder.txtViewDate = view.findViewById(R.id.txtViewDate);
            holder.txtViewDocnoTitle = view.findViewById(R.id.txtViewDocnoTitle);
            holder.txtViewDocno = view.findViewById(R.id.txtViewDocno);
            holder.txtViewProductNameTitle = view.findViewById(R.id.txtViewProductNameTitle);
            holder.txtViewProductName = view.findViewById(R.id.txtViewProductName);
            holder.txtViewQuantityTitle = view.findViewById(R.id.txtViewQuantityTitle);
            holder.txtViewQuantity = view.findViewById(R.id.txtViewQuantity);

            view.setTag(holder);
        }else{
            holder=(SubListViewHolder)view.getTag();
        }

        //标题显示
        holder.txtViewDocnoTitle.setVisibility(View.GONE);
        holder.txtViewDocno.setVisibility(View.GONE);
        holder.txtViewProductNameTitle.setVisibility(View.GONE);
        holder.txtViewProductName.setVisibility(View.GONE);
        holder.txtViewQuantityTitle.setVisibility(View.GONE);
        holder.txtViewQuantity.setVisibility(View.GONE);
        holder.imageViewStatus.setVisibility(View.GONE);
        if(mType.equals("2")){
            //完工入库明细
            holder.txtViewDeptTtile.setText(mContext.getResources().getString(R.string.sub_list_dept_title2));
            holder.txtViewStockTitle.setText(mContext.getResources().getString(R.string.sub_list_stock_title2));
            holder.txtViewDocnoTitle.setVisibility(View.VISIBLE);
            holder.txtViewDocno.setVisibility(View.VISIBLE);
            holder.txtViewProductNameTitle.setVisibility(View.VISIBLE);
            holder.txtViewProductName.setVisibility(View.VISIBLE);
            holder.txtViewQuantityTitle.setVisibility(View.VISIBLE);
            holder.txtViewQuantity.setVisibility(View.VISIBLE);
            holder.imageViewStatus.setVisibility(View.VISIBLE);

            holder.txtViewProductName.setText((String)mData.get(i).get("ProductName"));
            holder.txtViewQuantity.setText((String)mData.get(i).get("Quantity"));
        }else if(mType.equals("3")){
            holder.txtViewDeptTtile.setText(mContext.getResources().getString(R.string.sub_list_dept_title3));
            holder.txtViewStockTitle.setText(mContext.getResources().getString(R.string.sub_list_stock_title3));
            holder.txtViewDocnoTitle.setVisibility(View.VISIBLE);
            holder.txtViewDocno.setVisibility(View.VISIBLE);
        }else if(mType.equals("31")){
            holder.txtViewDeptTtile.setText(mContext.getResources().getString(R.string.sub_list_dept_title31));
            holder.txtViewStockTitle.setText(mContext.getResources().getString(R.string.sub_list_stock_title31));
        }else if(mType.equals("4")){
            holder.txtViewDocnoTitle.setVisibility(View.VISIBLE);
            holder.txtViewDocno.setVisibility(View.VISIBLE);
            holder.txtViewDeptTtile.setText(mContext.getResources().getString(R.string.sub_list_dept_title4));
            holder.txtViewStockTitle.setText(mContext.getResources().getString(R.string.sub_list_stock_title4));
        }else if(mType.equals("41")){
            holder.txtViewDeptTtile.setText(mContext.getResources().getString(R.string.sub_list_dept_title41));
            holder.txtViewStockTitle.setText(mContext.getResources().getString(R.string.sub_list_stock_title41));
        }

        //值显示
        holder.txtViewDeptId.setText((String)mData.get(i).get("DeptId"));
        holder.txtViewDept.setText((String)mData.get(i).get("Dept"));
        holder.txtViewStockId.setText((String)mData.get(i).get("StockId"));
        holder.txtViewStock.setText((String)mData.get(i).get("Stock"));
        holder.txtViewDate.setText((String)mData.get(i).get("PlanDate"));
        holder.txtViewDocno.setText((String)mData.get(i).get("Docno"));
        holder.status = (String)mData.get(i).get("DocType");
        holder.docStatus = (String)mData.get(i).get("Status");

        if(holder.status.equals("1")){
            holder.imageViewLogo.setImageDrawable(view.getResources().getDrawable(R.drawable.sub_detail_inside));
        }else{
            holder.imageViewLogo.setImageDrawable(view.getResources().getDrawable(R.drawable.sub_detail_outside));
        }

        if(holder.docStatus.equals("S")){
            holder.imageViewStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.status_post));
        }else{
            holder.imageViewStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.status_confirm));
        }

        return view;
    }

    public String getItem(int i,String s){
        return mData.get(i).get(s).toString();
    }

    public static class SubListViewHolder{
        ImageView imageViewLogo;
        ImageView imageViewStatus;
        TextView txtViewDeptTtile;
        TextView txtViewDeptId;
        TextView txtViewDept;
        TextView txtViewStockTitle;
        TextView txtViewStockId;
        TextView txtViewStock;
        TextView txtViewDate;
        TextView txtViewDocnoTitle;
        TextView txtViewDocno;
        TextView txtViewProductNameTitle;
        TextView txtViewProductName;
        TextView txtViewQuantityTitle;
        TextView txtViewQuantity;
        String status;
        String docStatus;
    }
}
