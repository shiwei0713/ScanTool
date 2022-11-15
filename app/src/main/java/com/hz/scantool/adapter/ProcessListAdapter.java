package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public ProcessListAdapter(List<Map<String,Object>> mData, Context mContext){
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

            view.setTag(holder);
        }else{
            holder = (ProcessListViewHolder)view.getTag();
        }

        String sSelect = (String)mData.get(i).get("Select");
        boolean isCheck = false;
        if(sSelect.equals("Y")){
            isCheck = true;
        }

        holder.checkBoxSel.setChecked(isCheck);
        holder.txtProcessId.setText((String)mData.get(i).get("ProcessId"));
        holder.txtProcess.setText((String)mData.get(i).get("Process"));
        holder.txtPreProcess.setText((String)mData.get(i).get("PreProcess"));

        return view;
    }

    public static class ProcessListViewHolder{
        CheckBox checkBoxSel;
        TextView txtProcessId,txtProcess,txtPreProcess,txtAdapterEmployee,txtAdapterDevices;
    }

    //更新数据
    public void updateData(int index, ListView listView,String employee,String devices){
        //获取第一个可见item项的位置
        int visiblePosition = listView.getFirstVisiblePosition();

        //获取指定位置的视图
        View view = listView.getChildAt(index-visiblePosition);
        ProcessListViewHolder holder = (ProcessListViewHolder)view.getTag();

        //生产人员
        holder.txtAdapterEmployee = view.findViewById(R.id.txtAdapterEmployee);
        holder.txtAdapterEmployee.setText(employee);
        mData.get(index).put("Employee", employee);

        //设备
        holder.txtAdapterDevices = view.findViewById(R.id.txtAdapterDevices);
        holder.txtAdapterDevices.setText(devices);
        mData.get(index).put("Devices", devices);
    }
}
