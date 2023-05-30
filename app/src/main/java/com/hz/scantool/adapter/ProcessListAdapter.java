package com.hz.scantool.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private String mType;
    private SetEmployeeClickListener setEmployeeClickListener;
    private SetDeviceClickListener setDeviceClickListener;
    private ClearClickListener clearClickListener;

    public ProcessListAdapter(List<Map<String,Object>> mData, Context mContext,String mType,SetEmployeeClickListener setEmployeeClickListener,SetDeviceClickListener setDeviceClickListener,ClearClickListener clearClickListener){
        this.mData = mData;
        this.mContext = mContext;
        this.mType = mType;
        this.setEmployeeClickListener = setEmployeeClickListener;
        this.setDeviceClickListener = setDeviceClickListener;
        this.clearClickListener = clearClickListener;
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
        ProcessListViewHolder holder = null;
        if(view==null){
            holder = new ProcessListViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_process_list,viewGroup,false);

            holder.checkBoxSel = view.findViewById(R.id.checkBoxSel);
            holder.txtProcess = view.findViewById(R.id.txtAdapterProcess);
            holder.txtProcessId = view.findViewById(R.id.txtAdapterProcessId);
            holder.txtPreProcess = view.findViewById(R.id.txtAdapterPreProcess);
            holder.txtAdapterEmployee = view.findViewById(R.id.txtAdapterEmployee);
            holder.txtAdapterDevices = view.findViewById(R.id.txtAdapterDevices);
            holder.listBtnSetEmployee = view.findViewById(R.id.listBtnSetEmployee);
            holder.listBtnSetDevice =view.findViewById(R.id.listBtnSetDevice);
            holder.listBtnClear = view.findViewById(R.id.listBtnClear);
            holder.txtAdapterSeq = view.findViewById(R.id.txtAdapterSeq);

            view.setTag(holder);
        }else{
            holder = (ProcessListViewHolder)view.getTag();
        }

        //定义按钮样式
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.dialog_del);
        drawable.setBounds(0,0,80,80);
        holder.listBtnClear.setCompoundDrawables(null,drawable,null,null);
        holder.listBtnClear.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));

        //绑定事件
        holder.listBtnSetEmployee.setOnClickListener(setEmployeeClickListener);
        holder.listBtnSetEmployee.setTag(i);
        holder.listBtnSetDevice.setOnClickListener(setDeviceClickListener);
        holder.listBtnSetDevice.setTag(i);
        holder.listBtnClear.setOnClickListener(clearClickListener);
        holder.listBtnClear.setTag(i);

        String sSelect = (String)mData.get(i).get("Select");
        boolean isCheck = false;
        if(sSelect.equals("Y")){
            isCheck = true;
        }
        holder.checkBoxSel.setChecked(isCheck);
        holder.txtAdapterSeq.setText(String.valueOf(i+1));

        if(mType.equals("Y")){
            holder.txtProcessId.setText((String)mData.get(i).get("ProcessId"));
            holder.txtProcess.setText((String)mData.get(i).get("Process"));
            holder.txtPreProcess.setText((String)mData.get(i).get("PreProcess"));
            holder.txtAdapterEmployee.setText((String)mData.get(i).get("Employee"));
            holder.txtAdapterDevices.setText((String)mData.get(i).get("Device"));
        }else{
            holder.checkBoxSel.setChecked(isCheck);
            holder.txtProcessId.setText((String)mData.get(i).get("ProcessId"));
            holder.txtProcess.setText((String)mData.get(i).get("Process"));
            holder.txtPreProcess.setText((String)mData.get(i).get("PreProcess"));
            holder.txtAdapterEmployee.setText((String)mData.get(i).get("Employee"));
            holder.txtAdapterDevices.setText((String)mData.get(i).get("Device"));
        }

        return view;
    }

    public static class ProcessListViewHolder{
        CheckBox checkBoxSel;
        TextView txtProcessId,txtProcess,txtPreProcess,txtAdapterEmployee,txtAdapterDevices,txtAdapterSeq;
        Button listBtnSetEmployee,listBtnSetDevice,listBtnClear;
    }

    //更新人员数据
    public void updateData(int index, ListView listView,String employee){
        //获取第一个可见item项的位置
        int visiblePosition = listView.getFirstVisiblePosition();

        //获取指定位置的视图
        View view = listView.getChildAt(index-visiblePosition);
        ProcessListViewHolder holder = (ProcessListViewHolder)view.getTag();
        Log.i("ViewHolderLog",String.valueOf(visiblePosition)+":"+String.valueOf(index-visiblePosition));

        //生产人员
        holder.txtAdapterEmployee = view.findViewById(R.id.txtAdapterEmployee);
        holder.checkBoxSel = view.findViewById(R.id.checkBoxSel);
        holder.txtAdapterSeq = view.findViewById(R.id.txtAdapterSeq);
        String sEmployee = holder.txtAdapterEmployee.getText().toString();
        int iEmp = sEmployee.indexOf(employee);
        if(iEmp<=-1){
            String sUpdEmployee = "";
            if(sEmployee.equals("")||sEmployee.isEmpty()){
                sUpdEmployee = employee;
            }else{
                sUpdEmployee = sEmployee+"/"+employee;
            }

            holder.txtAdapterEmployee.setText(sUpdEmployee);
            mData.get(index).put("Employee", sUpdEmployee);
            mData.get(index).put("Select", "Y");
            holder.checkBoxSel.setChecked(true);
        }
    }

    //更新设备数据
    public void updateDeviceData(int index, ListView listView,String devices){
        //获取第一个可见item项的位置
        int visiblePosition = listView.getFirstVisiblePosition();

        //获取指定位置的视图
        View view = listView.getChildAt(index-visiblePosition);
        ProcessListViewHolder holder = (ProcessListViewHolder)view.getTag();

        //设备
        holder.txtAdapterDevices = view.findViewById(R.id.txtAdapterDevices);
        holder.txtAdapterDevices.setText(devices);
        mData.get(index).put("Device", devices);
    }

    //清除数据,更新视图
    public void updateClearData(int index, ListView listView){
        //获取第一个可见item项的位置
        int visiblePosition = listView.getFirstVisiblePosition();

        //获取指定位置的视图
        View view = listView.getChildAt(index-visiblePosition);
        ProcessListViewHolder holder = (ProcessListViewHolder)view.getTag();

        holder.txtAdapterEmployee = view.findViewById(R.id.txtAdapterEmployee);
        holder.txtAdapterDevices = view.findViewById(R.id.txtAdapterDevices);
        holder.checkBoxSel = view.findViewById(R.id.checkBoxSel);
        mData.get(index).put("Employee", "");
        mData.get(index).put("Select", "N");
        mData.get(index).put("Device", "");
        holder.checkBoxSel.setChecked(false);
        holder.txtAdapterEmployee.setText("");
        holder.txtAdapterDevices.setText("");
    }

    //设置人员
    public static abstract class SetEmployeeClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            SetEmployeeClick((Integer)view.getTag(),view);
        }

        public abstract void SetEmployeeClick(int position,View view);
    }

    //设置设备
    public static abstract class SetDeviceClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            SetDeviceClick((Integer)view.getTag(),view);
        }

        public abstract void SetDeviceClick(int position,View view);
    }

    //清除数据
    public static abstract class ClearClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            ClearClick((Integer)view.getTag(),view);
        }

        public abstract void ClearClick(int position,View view);
    }
}
