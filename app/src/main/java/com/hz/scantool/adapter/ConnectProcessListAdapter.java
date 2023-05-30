package com.hz.scantool.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class ConnectProcessListAdapter extends RecyclerView.Adapter<ConnectProcessListAdapter.ConnectProcessListViewHolder> {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private String mType;
    private SetEmployeeClickListener setEmployeeClickListener;
    private SetDeviceClickListener setDeviceClickListener;
    private ClearClickListener clearClickListener;

    public ConnectProcessListAdapter(List<Map<String, Object>> mData, Context mContext, String mType){
        this.mData = mData;
        this.mContext = mContext;
        this.mType = mType;
    }

    @NonNull
    @Override
    public ConnectProcessListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConnectProcessListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_process_list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectProcessListViewHolder holder, int position) {
        //定义按钮样式
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.dialog_del);
        drawable.setBounds(0,0,80,80);
        holder.listBtnClear.setCompoundDrawables(null,drawable,null,null);
        holder.listBtnClear.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));

        String sSelect = (String)mData.get(position).get("Select");
        boolean isCheck = false;
        if(sSelect.equals("Y")){
            isCheck = true;
        }
        holder.checkBoxSel.setChecked(isCheck);
        holder.txtAdapterSeq.setText(String.valueOf(position+1));
        holder.txtProcessId.setText((String)mData.get(position).get("ProcessId"));
        holder.txtProcess.setText((String)mData.get(position).get("Process"));
        holder.txtPreProcess.setText((String)mData.get(position).get("PreProcess"));
        holder.txtAdapterEmployee.setText((String)mData.get(position).get("Employee"));
        holder.txtAdapterDevices.setText((String)mData.get(position).get("Device"));
        holder.txtGroupStation.setText((String)mData.get(position).get("GroupStation"));
        holder.txtDocno.setText((String)mData.get(position).get("Docno"));
        holder.txtPlanNo.setText((String)mData.get(position).get("PlanNO"));
        holder.txtVersion.setText((String)mData.get(position).get("Version"));

        //绑定事件
        //人员
        holder.listBtnSetEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(setEmployeeClickListener!=null){
                    setEmployeeClickListener.setEmployeeClick(view,position);
                }
            }
        });

        //设备
        holder.listBtnSetDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(setDeviceClickListener!=null){
                    setDeviceClickListener.setDeviceClick(view,position);
                }
            }
        });

        //清除
        holder.listBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clearClickListener!=null){
                    clearClickListener.clearClick(view,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ConnectProcessListViewHolder extends RecyclerView.ViewHolder{

        CheckBox checkBoxSel;
        TextView txtProcessId,txtProcess,txtPreProcess,txtAdapterEmployee,txtAdapterDevices,txtAdapterSeq;
        TextView txtGroupStation,txtDocno,txtPlanNo,txtVersion;
        Button listBtnSetEmployee,listBtnSetDevice,listBtnClear;

        public ConnectProcessListViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxSel = itemView.findViewById(R.id.checkBoxSel);
            txtProcess = itemView.findViewById(R.id.txtAdapterProcess);
            txtProcessId = itemView.findViewById(R.id.txtAdapterProcessId);
            txtPreProcess = itemView.findViewById(R.id.txtAdapterPreProcess);
            txtAdapterEmployee = itemView.findViewById(R.id.txtAdapterEmployee);
            txtAdapterDevices = itemView.findViewById(R.id.txtAdapterDevices);
            listBtnSetEmployee = itemView.findViewById(R.id.listBtnSetEmployee);
            listBtnSetDevice =itemView.findViewById(R.id.listBtnSetDevice);
            listBtnClear = itemView.findViewById(R.id.listBtnClear);
            txtAdapterSeq = itemView.findViewById(R.id.txtAdapterSeq);
            txtGroupStation = itemView.findViewById(R.id.txtGroupStation);
            txtDocno = itemView.findViewById(R.id.txtDocno);
            txtPlanNo = itemView.findViewById(R.id.txtPlanNo);
            txtVersion = itemView.findViewById(R.id.txtVersion);
        }
    }

    //设置人员
    public interface SetEmployeeClickListener{
        void setEmployeeClick(View itemView,int position);
    }

    public void setSetEmployeeClickListener(SetEmployeeClickListener setEmployeeClickListener) {
        this.setEmployeeClickListener = setEmployeeClickListener;
    }

    //设置设备
    public interface SetDeviceClickListener{
        void setDeviceClick(View itemView,int position);
    }

    public void setSetDeviceClickListener(SetDeviceClickListener setDeviceClickListener) {
        this.setDeviceClickListener = setDeviceClickListener;
    }

    //清除数据
    public interface ClearClickListener{
        void clearClick(View itemView,int position);
    }

    public void setClearClickListener(ClearClickListener clearClickListener) {
        this.clearClickListener = clearClickListener;
    }

    //更新数据
    public void updateItem(String item, String value, int position, boolean cover){
        String sUpdValue = "";

        if(cover){
            sUpdValue = value;
        }else{
            String sEmployee = (String)mData.get(position).get(item);
            int iEmp = sEmployee.indexOf(value);
            if(iEmp<=-1){
                if(sEmployee.equals("")||sEmployee.isEmpty()){
                    sUpdValue = value;
                }else{
                    sUpdValue = sEmployee+"/"+value;
                }
            }else{
                sUpdValue = sEmployee;
            }
        }

        mData.get(position).put(item, sUpdValue);
        mData.get(position).put("Select", "Y");
        notifyItemChanged(position);
    }

    //清除数据
    public void cleareItem(int position){
        mData.get(position).put("Employee", "");
        mData.get(position).put("Device", "");
        mData.get(position).put("Select", "N");
        notifyItemChanged(position);
    }

    //清除人员数据
    public void clearEmployeeItem(int position){
        mData.get(position).put("Employee", "");
        notifyItemChanged(position);
    }

    //清除所有人员数据
    public void clearAllEmployeeItem(){
        for(int i=0;i<mData.size();i++){
            mData.get(i).put("Employee", "");
            notifyItemChanged(i);
        }
    }
}
