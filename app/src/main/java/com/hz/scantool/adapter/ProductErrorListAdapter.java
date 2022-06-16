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

public class ProductErrorListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public ProductErrorListAdapter(List<Map<String,Object>> mData, Context mContext){
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
        ProductErrorViewHolder holder = null;
        if(view == null){
            holder = new ProductErrorViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_product_error,viewGroup,false);

            holder.imageLabelIcon = view.findViewById(R.id.imageLabelIcon);
            holder.txtErrorProductCode = view.findViewById(R.id.txtErrorProductCode);
            holder.txtErrorProductName = view.findViewById(R.id.txtErrorProductName);
            holder.txtErrorProductModels = view.findViewById(R.id.txtErrorProductModels);
            holder.txtProductErrorProcessId = view.findViewById(R.id.txtProductErrorProcessId);
            holder.txtProductErrorProcess = view.findViewById(R.id.txtProductErrorProcess);
            holder.txtProductErrorDocno = view.findViewById(R.id.txtProductErrorDocno);
            holder.txtProductErrorEmployee = view.findViewById(R.id.txtProductErrorEmployee);
            holder.txtProductErrorPlanDate = view.findViewById(R.id.txtProductErrorPlanDate);
            holder.txtProductErrorDevice = view.findViewById(R.id.txtProductErrorDevice);
            holder.txtProductErrorVersion = view.findViewById(R.id.txtProductErrorVersion);
            holder.txtProductErrorSeq = view.findViewById(R.id.txtProductErrorSeq);

            view.setTag(holder);
        }else{
            holder=(ProductErrorViewHolder)view.getTag();
        }

        //值显示
        holder.imageLabelIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
        holder.txtErrorProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtErrorProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtErrorProductModels.setText((String)mData.get(i).get("ProductModels"));
        holder.txtProductErrorProcessId.setText((String)mData.get(i).get("ProcessId"));
        holder.txtProductErrorProcess.setText((String)mData.get(i).get("Process"));
        holder.txtProductErrorDocno.setText((String)mData.get(i).get("Docno"));
        holder.txtProductErrorEmployee.setText((String)mData.get(i).get("Employee"));
        holder.txtProductErrorPlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.txtProductErrorDevice.setText((String)mData.get(i).get("Device"));
        holder.txtProductErrorVersion.setText((String)mData.get(i).get("Version"));
        holder.txtProductErrorSeq.setText((String)mData.get(i).get("Seq"));

        return view;
    }

    public static class ProductErrorViewHolder{
        ImageView imageLabelIcon;
        TextView txtErrorProductCode,txtErrorProductName,txtErrorProductModels,txtProductErrorProcessId,txtProductErrorProcess,txtProductErrorDocno;
        TextView txtProductErrorEmployee,txtProductErrorPlanDate,txtProductErrorDevice,txtProductErrorVersion,txtProductErrorSeq;
    }
}
