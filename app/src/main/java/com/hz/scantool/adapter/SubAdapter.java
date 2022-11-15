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

import io.reactivex.internal.fuseable.HasUpstreamObservableSource;

public class SubAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private UpdateClickListener mUpdateClickListener;
    private CancelClickListener mCancelClickListener;
    private StartTaskClickListener mStartTaskClickListener;
    private StopTaskClickListener mStopTaskClickListener;
    private SetTaskClickListener mSetTaskClickListener;
    private String strCommand;

    public SubAdapter(List<Map<String,Object>> mData, Context mContext,String strCommand){
        this.mData = mData;
        this.mContext = mContext;
        this.strCommand = strCommand;
    }

    public SubAdapter(List<Map<String,Object>> mData, Context mContext,UpdateClickListener mUpdateClickListener,CancelClickListener mCancelClickListener,String strCommand){
        this.mData = mData;
        this.mContext = mContext;
        this.mUpdateClickListener = mUpdateClickListener;
        this.mCancelClickListener = mCancelClickListener;
        this.strCommand = strCommand;
    }

    public SubAdapter(List<Map<String,Object>> mData, Context mContext,StartTaskClickListener mStartTaskClickListener,StopTaskClickListener mStopTaskClickListener,SetTaskClickListener mSetTaskClickListener,String strCommand){
        this.mData = mData;
        this.mContext = mContext;
        this.mStartTaskClickListener = mStartTaskClickListener;
        this.mStopTaskClickListener = mStopTaskClickListener;
        this.mSetTaskClickListener = mSetTaskClickListener;
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
            holder.listSubBtnUnConfirm = view.findViewById(R.id.listSubBtnUnConfirm);
            holder.txtSubFlag = view.findViewById(R.id.txtSubFlag);
            holder.txtSubModStatus = view.findViewById(R.id.txtSubModStatus);
            holder.txtSubOperateCount = view.findViewById(R.id.txtSubOperateCount);
            holder.txtSubPrintCount = view.findViewById(R.id.txtSubPrintCount);
            holder.txtVersion = view.findViewById(R.id.txtVersion);
            holder.listSubBtnStart = view.findViewById(R.id.listSubBtnStart);
            holder.listSubBtnStop = view.findViewById(R.id.listSubBtnStop);
            holder.listSubBtnSet = view.findViewById(R.id.listSubBtnSet);
            holder.txtGroupId = view.findViewById(R.id.txtGroupId);
            holder.txtGroup = view.findViewById(R.id.txtGroup);
            holder.txtProcessEnd = view.findViewById(R.id.txtProcessEnd);
            holder.imageProcessIcon = view.findViewById(R.id.imageProcessIcon);
            holder.imageVersionIcon = view.findViewById(R.id.imageVersionIcon);
            holder.listSubQcStatus = view.findViewById(R.id.listSubQcStatus);
            holder.txtInputStatus = view.findViewById(R.id.txtInputStatus);
            holder.txtCheckMaterial = view.findViewById(R.id.txtCheckMaterial);

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
        holder.StartTime = (String)mData.get(i).get("StartTime");
        holder.CheckTime = (String)mData.get(i).get("CheckTime");
        holder.UpTime = (String)mData.get(i).get("UpTime");
        holder.ErrorTime = (String)mData.get(i).get("ErrorTime");
        holder.ProductTotal = (String)mData.get(i).get("ProductTotal");
        holder.txtSubStatus.setText(holder.status);
        holder.txtGroupId.setText((String)mData.get(i).get("GroupId"));
        holder.txtGroup.setText((String)mData.get(i).get("Group"));
        holder.txtInputStatus.setText((String)mData.get(i).get("InputStatus"));
        holder.txtCheckMaterial.setText((String)mData.get(i).get("CheckMaterial"));
        holder.imageVersionIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.version));

        //连线标识
        String sProcessStatus = (String)mData.get(i).get("ProcessEnd");
        holder.txtProcessEnd.setText(sProcessStatus);
        if(sProcessStatus.equals("Y")){
            holder.imageProcessIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.process_connect));
        }else{
            holder.imageProcessIcon.setImageDrawable(null);
        }

        //组长分配任务
        if(strCommand.equals("ZZ")){
            holder.listSubBtnConfirm.setVisibility(View.GONE);
            holder.listSubBtnUnConfirm.setVisibility(View.GONE);
            holder.listSubBtnStart.setVisibility(View.VISIBLE);
            holder.listSubBtnStop.setVisibility(View.VISIBLE);
            holder.listSubBtnSet.setVisibility(View.VISIBLE);
            holder.listSubQcStatus.setVisibility(View.GONE);
        }else{
            holder.listSubBtnConfirm.setVisibility(View.VISIBLE);
            holder.listSubBtnStart.setVisibility(View.GONE);
            holder.listSubBtnStop.setVisibility(View.GONE);
            holder.listSubBtnSet.setVisibility(View.GONE);
            holder.listSubQcStatus.setVisibility(View.VISIBLE);

            //首检检验结果
            String sQcStatus = (String)mData.get(i).get("QcStatus");
            if(sQcStatus.equals("NG")){
                holder.listSubQcStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_status_ng));
                holder.listSubBtnUnConfirm.setVisibility(View.GONE);
            }else{
                holder.listSubQcStatus.setImageDrawable(null);
                holder.listSubBtnUnConfirm.setVisibility(View.VISIBLE);
            }
        }

        if(holder.status.equals("F")){
            holder.listSubBtnConfirm.setVisibility(View.VISIBLE);
            holder.listSubStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.first_check));
        }else{
            holder.listSubBtnConfirm.setVisibility(View.GONE);
            holder.listSubBtnUnConfirm.setVisibility(View.GONE);
            holder.listSubStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.ready_checkpqc));
        }

        //首检合格
        holder.listSubBtnConfirm.setOnClickListener(mUpdateClickListener);
        holder.listSubBtnConfirm.setTag(i);

        //首检异常
        holder.listSubBtnUnConfirm.setOnClickListener(mCancelClickListener);
        holder.listSubBtnUnConfirm.setTag(i);

        //开启任务
        holder.listSubBtnStart.setOnClickListener(mStartTaskClickListener);
        holder.listSubBtnStart.setTag(i);

        //中止任务
        holder.listSubBtnStop.setOnClickListener(mStopTaskClickListener);
        holder.listSubBtnStop.setTag(i);

        //连线生产
        holder.listSubBtnSet.setOnClickListener(mSetTaskClickListener);
        holder.listSubBtnSet.setTag(i);

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
        ImageView listSubStatus,imageProcessIcon,imageVersionIcon,listSubQcStatus;
        TextView txtSubStatus;
        Button listSubBtnConfirm,listSubBtnUnConfirm,listSubBtnStart,listSubBtnStop,listSubBtnSet;
        TextView txtSubFlag;
        TextView txtSubModStatus;
        TextView txtVersion;
        TextView txtGroupId,txtGroup,txtProcessEnd,txtInputStatus,txtCheckMaterial;

        String status;
        String StartStatus;
        String CheckStatus;
        String UpStatus;
        String ErrorStartStatus;
        String ErrorStopStatus;

        String StartTime;
        String CheckTime;
        String UpTime;
        String ErrorTime;
        String ProductTotal;
    }

    //首检合格
    public static abstract class UpdateClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            UpdateClick((Integer)view.getTag(),view);
        }

        public abstract void UpdateClick(int position,View view);
    }

    //首检异常
    public static abstract class CancelClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            CancelClick((Integer)view.getTag(),view);
        }

        public abstract void CancelClick(int position,View view);
    }

    //开启任务
    public static abstract class StartTaskClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            StartTaskClick((Integer)view.getTag(),view);
        }

        //开启任务
        public abstract void StartTaskClick(int position,View view);
    }

    //中止任务
    public static abstract class StopTaskClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            StopTaskClick((Integer)view.getTag(),view);
        }

        //中止任务
        public abstract void StopTaskClick(int position,View view);
    }

    //连线生产
    public static abstract class SetTaskClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            SetTaskClick((Integer)view.getTag(),view);
        }

        //连线生产
        public abstract void SetTaskClick(int position,View view);
    }
}
