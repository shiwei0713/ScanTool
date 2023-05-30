package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class UserTaskListAdapter extends RecyclerView.Adapter<UserTaskListAdapter.UserTaskListViewHolder>{

    private List<Map<String,Object>> mData;
    private Context mContext;
    private OnItemClickListener onItemClickListener;
    private RecyclerView recyclerView;

    public UserTaskListAdapter(List<Map<String, Object>> mData, Context mContext){
        this.mData = mData;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public UserTaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        recyclerView = (RecyclerView)parent;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user_task_list,parent,false);

        //item单击事件
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = recyclerView.getChildAdapterPosition(view);
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(view,position);
                }
            }
        });

        return new UserTaskListAdapter.UserTaskListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserTaskListViewHolder holder, int position) {
        //绑定值显示
        holder.imageListViewIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.detail_list_top));
        holder.listStatus.setImageDrawable(mContext.getResources().getDrawable(R.drawable.start_list_task));
        holder.txtPlanDate.setText((String)mData.get(position).get("PlanDate"));
        holder.txtProductName.setText((String)mData.get(position).get("ProductName"));
        holder.txtProductCode.setText((String)mData.get(position).get("ProductCode"));
        holder.txtProductModels.setText((String)mData.get(position).get("ProductModels"));
        holder.txtProcessId.setText((String)mData.get(position).get("ProcessId"));
        holder.txtProcess.setText((String)mData.get(position).get("Process"));
        holder.txtDevice.setText((String)mData.get(position).get("Device"));
        holder.txtDocno.setText((String)mData.get(position).get("Docno"));
        holder.txtQuantity.setText((String)mData.get(position).get("Quantity"));
        holder.txtEmployee.setText((String)mData.get(position).get("Employee"));
        holder.txtLots.setText((String)mData.get(position).get("Lots"));
        holder.txtSubFlag.setText((String)mData.get(position).get("Planno"));
        holder.txtVersion.setText((String)mData.get(position).get("Version"));
        holder.txtGroupId.setText((String)mData.get(position).get("GroupId"));
        holder.txtGroup.setText((String)mData.get(position).get("Group"));
        holder.txtGroupStation.setText((String)mData.get(position).get("GroupStation"));
        holder.txtStationDocno.setText((String)mData.get(position).get("StationDocno"));
        holder.txtProcessEnd.setText((String)mData.get(position).get("ConnectStatus"));  //连线标识
        holder.txtConnectDocno.setText((String)mData.get(position).get("ConnectDocno")); //连线单号
        holder.txtConnectProcess.setText((String)mData.get(position).get("ConnectProcess")); //连线工序状态
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public String getItemValue(int i,String item){
        return (String)mData.get(i).get(item);
    }

    public class UserTaskListViewHolder extends RecyclerView.ViewHolder{
        ImageView imageListViewIcon,listStatus;
        TextView txtPlanDate,txtProductName,txtProductCode,txtProductModels,txtProcessId;
        TextView txtProcess,txtDevice,txtQuantity,txtDocno,txtEmployee,txtLots;
        TextView txtSubStatus,txtSubFlag,txtVersion,txtGroupId,txtGroup;
        TextView txtProcessEnd,txtGroupStation,txtStationDocno,txtConnectDocno,txtConnectProcess;

        public UserTaskListViewHolder(@NonNull View itemView) {
            super(itemView);

            imageListViewIcon = itemView.findViewById(R.id.imageListViewIcon);
            listStatus = itemView.findViewById(R.id.listStatus);
            txtPlanDate = itemView.findViewById(R.id.txtPlanDate);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductCode = itemView.findViewById(R.id.txtProductCode);
            txtProductModels = itemView.findViewById(R.id.txtProductModels);
            txtProcessId = itemView.findViewById(R.id.txtProcessId);
            txtProcess= itemView.findViewById(R.id.txtProcess);
            txtDevice= itemView.findViewById(R.id.txtDevice);
            txtDocno = itemView.findViewById(R.id.txtDocno);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtEmployee = itemView.findViewById(R.id.txtEmployee);
            txtLots = itemView.findViewById(R.id.txtLots);
            txtSubStatus = itemView.findViewById(R.id.txtSubStatus);
            txtSubFlag = itemView.findViewById(R.id.txtSubFlag);
            txtVersion = itemView.findViewById(R.id.txtVersion);
            txtGroupId = itemView.findViewById(R.id.txtGroupId);
            txtGroup = itemView.findViewById(R.id.txtGroup);
            txtProcessEnd = itemView.findViewById(R.id.txtProcessEnd);
            txtGroupStation = itemView.findViewById(R.id.txtGroupStation);
            txtStationDocno = itemView.findViewById(R.id.txtStationDocno);
            txtConnectDocno = itemView.findViewById(R.id.txtConnectDocno);
            txtConnectProcess = itemView.findViewById(R.id.txtConnectProcess);
        }
    }

    //行单击事件接口
    public interface OnItemClickListener{
        void onItemClick(View itemView,int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
