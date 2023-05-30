package com.hz.scantool.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder>{

    private List<Map<String,Object>> mData;
    private Context mContext;
    private StartClickListener startClickListener;
    private UpdateClickListener updateClickListener;
    private EmployeeClickListener employeeClickListener;
    private StopClickListener stopClickListener;
    private ClearClickListener clearClickListener;

    public TaskListAdapter(List<Map<String, Object>> mData, Context mContext){
        this.mData = mData;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_task,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListViewHolder holder, int position) {
        //定义按钮样式
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.dialog_del);
        drawable.setBounds(0,0,80,80);
        holder.listBtnClear.setCompoundDrawables(null,drawable,null,null);
        holder.listBtnClear.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));

        //值显示
        holder.imageListViewIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.detail_list_top));
        holder.listStatus.setImageDrawable(mContext.getResources().getDrawable(R.drawable.list_alarm));
        holder.txtPlanDate.setText((String)mData.get(position).get("PlanDate"));
        holder.txtProductName.setText((String)mData.get(position).get("ProductName"));
        holder.txtProductCode.setText((String)mData.get(position).get("ProductCode"));
        holder.txtProductModels.setText((String)mData.get(position).get("ProductModels"));
        holder.txtProcessId.setText((String)mData.get(position).get("ProcessId"));
        holder.txtProcess.setText((String)mData.get(position).get("Process"));
        holder.txtDocno.setText((String)mData.get(position).get("Docno"));
        holder.txtQuantity.setText((String)mData.get(position).get("Quantity"));
        holder.txtEmployee.setText((String)mData.get(position).get("Employee"));
        holder.txtLots.setText((String)mData.get(position).get("Lots"));
        holder.txtPlanno.setText((String)mData.get(position).get("PlanNO"));
        holder.txtVersion.setText((String)mData.get(position).get("Version"));
        String sCheck = (String)mData.get(position).get("Select");
        if(sCheck.equals("Y")){
            holder.checkBoxSel.setChecked(true);
            holder.listTaskBtnStart.setVisibility(View.GONE);
            holder.listTaskBtnStop.setVisibility(View.VISIBLE);
        }else{
            holder.checkBoxSel.setChecked(false);
            holder.listTaskBtnStart.setVisibility(View.VISIBLE);
            holder.listTaskBtnStop.setVisibility(View.GONE);
        }

        //定义事件
        //启用
        holder.listTaskBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startClickListener!=null){
                    startClickListener.startClick(view,position);
                }
            }
        });

        //停用
        holder.listTaskBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stopClickListener!=null){
                    stopClickListener.stopClick(view,position);
                }
            }
        });

        //单开
        holder.listTaskBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(updateClickListener!=null){
                    updateClickListener.updateClick(view,position);
                }
            }
        });

        //人员
        holder.listTaskSetEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(employeeClickListener!=null) {
                    employeeClickListener.employeeClick(view, position);
                }
            }
        });

        //清除数据
        holder.listBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clearClickListener!=null){
                    clearClickListener.clearClick(view, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public String getItemValue(int position,String value){
        return (String)mData.get(position).get(value);
    }

    public class TaskListViewHolder extends RecyclerView.ViewHolder{

        ImageView imageListViewIcon,listStatus;
        TextView txtPlanDate,txtProductName,txtProductCode,txtProductModels,txtProcessId;
        TextView txtProcess,txtQuantity,txtDocno,txtEmployee,txtLots;
        Button listTaskBtnStart,listTaskBtnUpdate,listTaskSetEmployee,listTaskBtnStop,listBtnClear;
        TextView txtPlanno,txtVersion;
        CheckBox checkBoxSel;

        public TaskListViewHolder(@NonNull View itemView) {
            super(itemView);

            imageListViewIcon = itemView.findViewById(R.id.imageListViewIcon);
            listStatus = itemView.findViewById(R.id.listStatus);
            txtPlanDate = itemView.findViewById(R.id.txtPlanDate);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductCode = itemView.findViewById(R.id.txtProductCode);
            txtProductModels = itemView.findViewById(R.id.txtProductModels);
            txtProcessId = itemView.findViewById(R.id.txtProcessId);
            txtProcess= itemView.findViewById(R.id.txtProcess);
            txtDocno = itemView.findViewById(R.id.txtDocno);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtEmployee = itemView.findViewById(R.id.txtEmployee);
            txtLots = itemView.findViewById(R.id.txtLots);
            txtPlanno = itemView.findViewById(R.id.txtPlanno);
            txtVersion = itemView.findViewById(R.id.txtVersion);
            listTaskBtnStart = itemView.findViewById(R.id.listTaskBtnStart);
            listTaskBtnUpdate = itemView.findViewById(R.id.listTaskBtnUpdate);
            listTaskSetEmployee = itemView.findViewById(R.id.listTaskSetEmployee);
            listTaskBtnStop = itemView.findViewById(R.id.listTaskBtnStop);
            listBtnClear = itemView.findViewById(R.id.listBtnClear);
            checkBoxSel = itemView.findViewById(R.id.checkBoxSel);
        }
    }

    /**
    *描述: 定义按钮接口
     * 参数:type->1:启用
     **/
    public interface StartClickListener{
        void startClick(View itemView,int position);
    }

    public void setStartClickListener(StartClickListener startClickListener) {
        this.startClickListener = startClickListener;
    }

    /**
     *描述: 定义按钮接口
     * 参数:type->0:停用
     **/
    public interface StopClickListener{
        void stopClick(View itemView,int position);
    }

    public void setStopClickListener(StopClickListener stopClickListener) {
        this.stopClickListener = stopClickListener;
    }

    /**
     *描述: 定义按钮接口
     * 参数:type->2:单开
     **/
    public interface UpdateClickListener{
        void updateClick(View itemView,int position);
    }

    public void setUpdateClickListener(UpdateClickListener updateClickListener) {
        this.updateClickListener = updateClickListener;
    }

    /**
     *描述: 定义按钮接口
     * 参数:type->3:人员设置
     **/
    public interface EmployeeClickListener{
        void employeeClick(View itemView,int position);
    }

    public void setEmployeeClickListener(EmployeeClickListener employeeClickListener) {
        this.employeeClickListener = employeeClickListener;
    }

    /**
    *描述: 定义按钮接口-清楚
    *日期：2023-05-25
    **/
    public interface ClearClickListener{
        void clearClick(View itemView,int position);
    }

    public void setClearClickListener(ClearClickListener clearClickListener) {
        this.clearClickListener = clearClickListener;
    }

    /**
    *描述: 更新list项数据
    *日期：2023-05-11
    **/
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

    /**
    *描述: 更新list项选中状态
    *日期：2023-05-12
    **/
    public void updateItemStatus(int position,String unsel){
        mData.get(position).put("Select", unsel);
        notifyItemChanged(position);
    }

    //清除人员数据
    public void clearEmployeeItem(int position){
        mData.get(position).put("Employee", "");
        notifyItemChanged(position);
    }

    //清除数据
    public void clearItem(int position){
        mData.get(position).put("Employee", "");
        mData.get(position).put("Select", "N");
        notifyItemChanged(position);
    }

}
