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

public class SubAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private UpdateClickListener mUpdateClickListener;
    private String strCommand;

    public SubAdapter(List<Map<String,Object>> mData, Context mContext,String strCommand){
        this.mData = mData;
        this.mContext = mContext;
        this.strCommand = strCommand;
    }

    public SubAdapter(List<Map<String,Object>> mData, Context mContext,UpdateClickListener mUpdateClickListener,String strCommand){
        this.mData = mData;
        this.mContext = mContext;
        this.mUpdateClickListener = mUpdateClickListener;
        this.strCommand = strCommand;
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

    public String getItemValue(int position,String strValue){
        return  (String)mData.get(position).get(strValue);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SubViewHolder holder = null;
        if(view == null){
            holder = new SubViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_show_sub,viewGroup,false);

            holder.imageListViewIcon = view.findViewById(R.id.imageListViewIcon);
            holder.listStatus = view.findViewById(R.id.listStatus);
            holder.txtPlanDate = view.findViewById(R.id.txtPlanDate);
            holder.txtProductName = view.findViewById(R.id.txtProductName);
            holder.txtProductCode = view.findViewById(R.id.txtProductCode);
            holder.txtProductModels = view.findViewById(R.id.txtProductModels);
            holder.txtProcessId = view.findViewById(R.id.txtProcessId);
            holder.txtProcess= view.findViewById(R.id.txtProcess);
            holder.txtDevice= view.findViewById(R.id.txtDevice);
            holder.txtDocno = view.findViewById(R.id.txtDocno);
            holder.txtQuantity = view.findViewById(R.id.txtQuantity);
            holder.txtEmployee = view.findViewById(R.id.txtEmployee);
            holder.txtLots = view.findViewById(R.id.txtLots);
            holder.listSubStatus = view.findViewById(R.id.listSubStatus);
            holder.txtSubStatus = view.findViewById(R.id.txtSubStatus);
            holder.listSubBtnConfirm = view.findViewById(R.id.listSubBtnConfirm);
            holder.txtSubFlag = view.findViewById(R.id.txtSubFlag);
            holder.txtSubModStatus = view.findViewById(R.id.txtSubModStatus);
            holder.txtSubOperateCount = view.findViewById(R.id.txtSubOperateCount);
            holder.txtSubPrintCount = view.findViewById(R.id.txtSubPrintCount);
            holder.txtVersion = view.findViewById(R.id.txtVersion);

            view.setTag(holder);
        }else{
            holder=(SubViewHolder)view.getTag();
        }

        //值显示
        holder.imageListViewIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
        holder.listStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.list_alarm));
        holder.listSubStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.ready_product));
        holder.listSubBtnConfirm.setVisibility(View.GONE);
        holder.txtPlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.txtProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtProductModels.setText((String)mData.get(i).get("ProductModels"));
        holder.txtProcessId.setText((String)mData.get(i).get("ProcessId"));
        holder.txtProcess.setText((String)mData.get(i).get("Process"));
        holder.txtDevice.setText((String)mData.get(i).get("Device"));
        holder.txtDocno.setText((String)mData.get(i).get("Docno"));
        holder.txtQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.txtEmployee.setText((String)mData.get(i).get("Employee"));
        holder.txtLots.setText((String)mData.get(i).get("Lots"));
        holder.txtSubFlag.setText((String)mData.get(i).get("Flag"));
        holder.txtSubModStatus.setText((String)mData.get(i).get("ModStatus"));
        holder.txtSubOperateCount.setText((String)mData.get(i).get("OperateCount"));
        holder.txtSubPrintCount.setText((String)mData.get(i).get("PrintCount"));
        holder.status = (String)mData.get(i).get("Status");
        holder.StartStatus = (String)mData.get(i).get("StartStatus");
        holder.CheckStatus = (String)mData.get(i).get("CheckStatus");
        holder.UpStatus = (String)mData.get(i).get("UpStatus");
        holder.ErrorStartStatus = (String)mData.get(i).get("ErrorStartStatus");
        holder.ErrorStopStatus = (String)mData.get(i).get("ErrorStopStatus");
        holder.txtVersion.setText((String)mData.get(i).get("Version"));
        holder.txtSubStatus.setText(holder.status);

        if(holder.status.equals("F")){
            holder.listSubBtnConfirm.setVisibility(View.VISIBLE);
            holder.listSubStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.first_check));
        }else{
            holder.listSubBtnConfirm.setVisibility(View.GONE);
            holder.listSubStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.ready_checkpqc));
        }

        holder.listSubBtnConfirm.setOnClickListener(mUpdateClickListener);
        holder.listSubBtnConfirm.setTag(i);

        return view;
    }

    public static class SubViewHolder{
        ImageView imageListViewIcon;
        ImageView listStatus;
        TextView txtPlanDate;
        TextView txtProductName;
        TextView txtProductCode;
        TextView txtProductModels;
        TextView txtProcessId;
        TextView txtProcess;
        TextView txtDevice;
        TextView txtQuantity;
        TextView txtDocno;
        TextView txtEmployee;
        TextView txtLots;
        TextView txtSubOperateCount;
        TextView txtSubPrintCount;
        ImageView listSubStatus;
        TextView txtSubStatus;
        Button listSubBtnConfirm;
        TextView txtSubFlag;
        TextView txtSubModStatus;
        TextView txtVersion;

        String status;
        String StartStatus;
        String CheckStatus;
        String UpStatus;
        String ErrorStartStatus;
        String ErrorStopStatus;
    }

    public static abstract class UpdateClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            UpdateClick((Integer)view.getTag(),view);
        }

        public abstract void UpdateClick(int position,View view);
    }
}
