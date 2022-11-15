package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class SurplusLabelListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private PrintClickListener printClickListener;

    public SurplusLabelListAdapter(List<Map<String,Object>> mData, Context mContext,PrintClickListener printClickListener){
        this.mData = mData;
        this.mContext = mContext;
        this.printClickListener = printClickListener;
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
        SurplusLabelViewHolder holder = null;
        if(view == null){
            holder = new SurplusLabelViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_label_surplus,viewGroup,false);

            holder.listLabelSurplusProductName = view.findViewById(R.id.listLabelSurplusProductName);
            holder.listLabelSurplusFlag = view.findViewById(R.id.listLabelSurplusFlag);
            holder.listLabelSurplusProductModel = view.findViewById(R.id.listLabelSurplusProductModel);
            holder.listLabelSurplusProcessId = view.findViewById(R.id.listLabelSurplusProcessId);
            holder.listLabelSurplusProcess = view.findViewById(R.id.listLabelSurplusProcess);
            holder.listLabelSurplusEmployee = view.findViewById(R.id.listLabelSurplusEmployee);
            holder.listLabelSurplusPlanDate = view.findViewById(R.id.listLabelSurplusPlanDate);
            holder.listLabelSurplusDevice = view.findViewById(R.id.listLabelSurplusDevice);
            holder.listLabelSurplusQty = view.findViewById(R.id.listLabelSurplusQty);
            holder.listLabelSurplusPlanNo = view.findViewById(R.id.listLabelSurplusPlanNo);
            holder.listLabelSurplusVersion = view.findViewById(R.id.listLabelSurplusVersion);
            holder.listLabelSurplusProductDocno = view.findViewById(R.id.listLabelSurplusProductDocno);
            holder.listLabelSurplusGroup = view.findViewById(R.id.listLabelSurplusGroup);
            holder.listLabelSurplusProductSize = view.findViewById(R.id.listLabelSurplusProductSize);
            holder.btnPrint = view.findViewById(R.id.btnPrint);
            holder.viewLabelSurplusProductSize = view.findViewById(R.id.viewLabelSurplusProductSize);
            holder.listLabelSurplusQrcode = view.findViewById(R.id.listLabelSurplusQrcode);

            view.setTag(holder);
        }else{
            holder=(SurplusLabelViewHolder)view.getTag();
        }

        //值显示
        holder.listLabelSurplusProductName.setText((String)mData.get(i).get("ProductName"));
        holder.listLabelSurplusFlag.setText((String)mData.get(i).get("Flag"));
        holder.listLabelSurplusProductModel.setText((String)mData.get(i).get("ProductModels"));
        holder.listLabelSurplusProcessId.setText((String)mData.get(i).get("ProcessId"));
        holder.listLabelSurplusProcess.setText((String)mData.get(i).get("Process"));
        holder.listLabelSurplusEmployee.setText((String)mData.get(i).get("Employee"));
        holder.listLabelSurplusPlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.listLabelSurplusDevice.setText((String)mData.get(i).get("Device"));
        holder.listLabelSurplusQty.setText((String)mData.get(i).get("Quantity"));
        holder.listLabelSurplusPlanNo.setText((String)mData.get(i).get("Docno"));
        holder.listLabelSurplusVersion.setText((String)mData.get(i).get("Version"));
        holder.listLabelSurplusProductDocno.setText((String)mData.get(i).get("ProductDocno"));
        holder.listLabelSurplusGroup.setText((String)mData.get(i).get("Group"));
        holder.listLabelSurplusQrcode.setText((String)mData.get(i).get("Qrcode"));

        //尺寸
        String sSize = (String)mData.get(i).get("Size");
        if(sSize.equals("")||sSize.isEmpty()){
            holder.viewLabelSurplusProductSize.setVisibility(View.GONE);
        }else{
            holder.viewLabelSurplusProductSize.setVisibility(View.VISIBLE);
            holder.listLabelSurplusProductSize.setText((String)mData.get(i).get("ProductModels")+"*"+sSize);
            holder.listLabelSurplusProductModel.setText((String)mData.get(i).get("FeaturesName"));
        }

        //打印
        holder.btnPrint.setOnClickListener(printClickListener);
        holder.btnPrint.setTag(i);

        return view;
    }

    public static class SurplusLabelViewHolder{
        TextView listLabelSurplusProductName;
        TextView listLabelSurplusFlag;
        TextView listLabelSurplusProductModel;
        TextView listLabelSurplusProcessId,listLabelSurplusProcess;
        TextView listLabelSurplusEmployee;
        TextView listLabelSurplusPlanDate;
        TextView listLabelSurplusDevice;
        TextView listLabelSurplusQty;
        TextView listLabelSurplusPlanNo;
        TextView listLabelSurplusVersion;
        TextView listLabelSurplusProductDocno;
        TextView listLabelSurplusGroup;
        TextView listLabelSurplusProductSize;
        TextView listLabelSurplusQrcode;
        Button btnPrint;
        LinearLayout viewLabelSurplusProductSize;
    }

    /**
    *描述: 打印事件
    *日期：2022/10/25
    **/
    public static abstract class PrintClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            PrintOnClick((Integer) view.getTag(),view);
        }

        public abstract void PrintOnClick(int position,View v);
    }
}
