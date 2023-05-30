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

public class SendTaskListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private SetTaskClickListener mSetTaskClickListener;
    private StartTaskListener mStartTaskListener;
    private StopTaskListener mStopTaskListener;
    private String sStatus;

    public SendTaskListAdapter(List<Map<String,Object>> mData, Context mContext,SetTaskClickListener mSetTaskClickListener,String sStatus,StartTaskListener mStartTaskListener,StopTaskListener mStopTaskListener){
        this.mData = mData;
        this.mContext = mContext;
        this.mSetTaskClickListener = mSetTaskClickListener;
        this.sStatus = sStatus;
        this.mStartTaskListener = mStartTaskListener;
        this.mStopTaskListener = mStopTaskListener;
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

            view = LayoutInflater.from(mContext).inflate(R.layout.list_send_task,viewGroup,false);

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
            holder.txtSubStatus = view.findViewById(R.id.txtSubStatus);
            holder.txtSubFlag = view.findViewById(R.id.txtSubFlag);
            holder.txtVersion = view.findViewById(R.id.txtVersion);
            holder.listSubBtnSet = view.findViewById(R.id.listSubBtnSet);
            holder.btnStartTask = view.findViewById(R.id.btnStartTask);
            holder.btnStopTask = view.findViewById(R.id.btnStopTask);
            holder.txtGroupId = view.findViewById(R.id.txtGroupId);
            holder.txtGroup = view.findViewById(R.id.txtGroup);
            holder.txtProcessEnd = view.findViewById(R.id.txtProcessEnd);
            holder.txtGroupStation = view.findViewById(R.id.txtGroupStation);
            holder.txtStationDocno = view.findViewById(R.id.txtStationDocno);
            holder.txtConnectDocno = view.findViewById(R.id.txtConnectDocno);

            view.setTag(holder);
        }else{
            holder=(SubViewHolder)view.getTag();
        }

        //值显示
        holder.imageListViewIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
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
        holder.status = (String)mData.get(i).get("Status");
        holder.StartStatus = (String)mData.get(i).get("StartStatus");
        holder.CheckStatus = (String)mData.get(i).get("CheckStatus");
        holder.UpStatus = (String)mData.get(i).get("UpStatus");
        holder.ErrorStartStatus = (String)mData.get(i).get("ErrorStartStatus");
        holder.ErrorStopStatus = (String)mData.get(i).get("ErrorStopStatus");
        holder.txtVersion.setText((String)mData.get(i).get("Version"));
        holder.StartTime = (String)mData.get(i).get("StartTime");
        holder.CheckTime = (String)mData.get(i).get("CheckTime");
        holder.UpTime = (String)mData.get(i).get("UpTime");
        holder.ErrorTime = (String)mData.get(i).get("ErrorTime");
        holder.ProductTotal = (String)mData.get(i).get("ProductTotal");
        holder.txtSubStatus.setText(holder.status);
        holder.txtGroupId.setText((String)mData.get(i).get("GroupId"));
        holder.txtGroup.setText((String)mData.get(i).get("Group"));
        holder.txtGroupStation.setText((String)mData.get(i).get("GroupStation"));
        holder.txtStationDocno.setText((String)mData.get(i).get("StationDocno"));

        String sProcessEnd = (String)mData.get(i).get("ProcessEnd");
        holder.txtProcessEnd.setText(sProcessEnd);  //连线标识
        holder.txtConnectDocno.setText((String)mData.get(i).get("ConnectDocno")); //连线单号

        //状态显示
        switch (sStatus){
            case "N":
                holder.listStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.no_list_task));
                holder.listSubBtnSet.setVisibility(View.VISIBLE);
                holder.btnStartTask.setVisibility(View.GONE);
                holder.btnStopTask.setVisibility(View.GONE);
                break;
            case "Y":
                holder.listStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.start_list_task));
                holder.listSubBtnSet.setVisibility(View.GONE);
                holder.btnStartTask.setVisibility(View.GONE);
                holder.btnStopTask.setVisibility(View.VISIBLE);
                break;
            case "C":
                holder.listStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.stop_list_task));
                holder.listSubBtnSet.setVisibility(View.GONE);
                holder.btnStartTask.setVisibility(View.VISIBLE);
                holder.btnStopTask.setVisibility(View.GONE);
                break;
        }

        //开启任务
        holder.btnStartTask.setOnClickListener(mStartTaskListener);
        holder.btnStartTask.setTag(i);

        //中止任务
        holder.btnStopTask.setOnClickListener(mStopTaskListener);
        holder.btnStopTask.setTag(i);

        //连线任务
        holder.listSubBtnSet.setOnClickListener(mSetTaskClickListener);
        holder.listSubBtnSet.setTag(i);

        return view;
    }

    public static class SubViewHolder{
        ImageView imageListViewIcon,listStatus;
        TextView txtPlanDate,txtProductName,txtProductCode,txtProductModels,txtProcessId;
        TextView txtProcess,txtDevice,txtQuantity,txtDocno,txtEmployee,txtLots;
        TextView txtSubStatus,txtSubFlag,txtVersion,txtGroupId,txtGroup;
        TextView txtProcessEnd,txtGroupStation,txtStationDocno,txtConnectDocno;
        Button listSubBtnSet,btnStartTask,btnStopTask;

        String status;
        String StartStatus,CheckStatus,UpStatus,ErrorStartStatus,ErrorStopStatus;
        String StartTime,CheckTime,UpTime,ErrorTime,ProductTotal;

    }

    //连线任务
    public static abstract class SetTaskClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            SetTaskClick((Integer)view.getTag(),view);
        }

        public abstract void SetTaskClick(int position,View view);
    }

    //开启任务
    public static abstract class StartTaskListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            StartTaskClick((Integer)view.getTag(),view);
        }

        public abstract void StartTaskClick(int position,View view);
    }

    //中止任务
    public static abstract class StopTaskListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            StopTaskClick((Integer)view.getTag(),view);
        }

        public abstract void StopTaskClick(int position,View view);
    }
}
