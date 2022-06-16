package com.hz.scantool.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class PrintLabelListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public PrintLabelListAdapter(List<Map<String,Object>> mData, Context mContext){
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
        PrintLabelViewHolder holder = null;
        if(view == null){
            holder = new PrintLabelViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_print_label,viewGroup,false);

            holder.imageLabelIcon = view.findViewById(R.id.imageLabelIcon);
            holder.txtLabelQrcode = view.findViewById(R.id.txtLabelQrcode);
            holder.txtLabelPlanDate = view.findViewById(R.id.txtLabelPlanDate);
            holder.txtLabelProductCode = view.findViewById(R.id.txtLabelProductCode);
            holder.txtLabelProductName = view.findViewById(R.id.txtLabelProductName);
            holder.txtLabelProductModels = view.findViewById(R.id.txtLabelProductModels);
            holder.txtLabelProcessId = view.findViewById(R.id.txtLabelProcessId);
            holder.txtLabelProcess = view.findViewById(R.id.txtLabelProcess);
            holder.txtLabelDocno = view.findViewById(R.id.txtLabelDocno);
            holder.txtLabelQuantity = view.findViewById(R.id.txtLabelQuantity);

            view.setTag(holder);
        }else{
            holder=(PrintLabelViewHolder)view.getTag();
        }

        //值显示
        holder.imageLabelIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
        holder.txtLabelQrcode.setText((String)mData.get(i).get("Qrcode"));
        holder.txtLabelPlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.txtLabelProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtLabelProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtLabelProductModels.setText((String)mData.get(i).get("ProductModels"));
        holder.txtLabelProcessId.setText((String)mData.get(i).get("ProcessId"));
        holder.txtLabelProcess.setText((String)mData.get(i).get("Process"));
        holder.txtLabelDocno.setText((String)mData.get(i).get("Docno"));
        holder.txtLabelQuantity.setText((String)mData.get(i).get("Quantity"));

        return view;
    }

    public static class PrintLabelViewHolder{
        ImageView imageLabelIcon;
        TextView txtLabelQrcode,txtLabelPlanDate,txtLabelProductCode,txtLabelProductModels,txtLabelProductName,txtLabelProcessId;
        TextView txtLabelProcess,txtLabelDocno,txtLabelQuantity;
    }
}
