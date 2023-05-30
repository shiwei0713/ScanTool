package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class AttendanceListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public AttendanceListAdapter(List<Map<String,Object>> mData, Context mContext){
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
        AttendanceListViewHolder holder = null;
        if(view == null){
            holder = new AttendanceListViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_attendance,viewGroup,false);

            holder.listAttendanceFlag = view.findViewById(R.id.listAttendanceFlag);
            holder.listAttendanceTime = view.findViewById(R.id.listAttendanceTime);
            holder.listAttendanceState = view.findViewById(R.id.listAttendanceState);
            holder.listAttendanceDept = view.findViewById(R.id.listAttendanceDept);
            holder.listAttendanceAlias = view.findViewById(R.id.listAttendanceAlias);
            holder.listAttendanceWeek = view.findViewById(R.id.listAttendanceWeek);

            view.setTag(holder);
        }else{
            holder=(AttendanceListViewHolder)view.getTag();
        }

        //值显示
        holder.listAttendanceFlag.setText(String.valueOf(i+1));
        holder.listAttendanceTime.setText((String)mData.get(i).get("PlanDate"));
        holder.listAttendanceState.setText((String)mData.get(i).get("DeptId"));
        holder.listAttendanceDept.setText((String)mData.get(i).get("Dept"));
        holder.listAttendanceAlias.setText((String)mData.get(i).get("StockId"));
        holder.listAttendanceWeek.setText((String)mData.get(i).get("Stock"));

        return view;
    }

    public static class AttendanceListViewHolder{
        TextView listAttendanceFlag,listAttendanceTime,listAttendanceState,listAttendanceDept,listAttendanceAlias,listAttendanceWeek;
    }
}
