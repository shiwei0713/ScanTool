package com.hz.scantool.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hz.scantool.R;
import com.hz.scantool.SubDetailForMultipleActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultipleDetailAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private BadQuantityClickListener mBadQuantityClickListener;

    public MultipleDetailAdapter(List<Map<String,Object>> mData, Context mContext,BadQuantityClickListener mBadQuantityClickListener){
        this.mData = mData;
        this.mContext = mContext;
        this.mBadQuantityClickListener = mBadQuantityClickListener;
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

    public String getQuantity(int i){
        return (String)mData.get(i).get("Quantity");
    }

    public String getBadQuantity(int i){
        return (String)mData.get(i).get("BadQuantity");
    }

    public String getProductDocno(int i){
        return (String)mData.get(i).get("ProductDocno");
    }

    public String getItemValue(int i,String sItem){
        return (String)mData.get(i).get(sItem);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MultipleDetailViewHolder holder = null;

        if(view == null){
            holder = new MultipleDetailViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.detail_show_sub_multiple,viewGroup,false);

            holder.imageMultipleDetailViewIcon = view.findViewById(R.id.imageMultipleDetailViewIcon);
            holder.multipleDetailStatus = view.findViewById(R.id.multipleDetailStatus);
            holder.txtMultipleDetailProductName = view.findViewById(R.id.txtMultipleDetailProductName);
            holder.txtMultipleDetailPlanDate = view.findViewById(R.id.txtMultipleDetailPlanDate);
            holder.txtMultipleDetailProductCode = view.findViewById(R.id.txtMultipleDetailProductCode);
            holder.txtMultipleDetailProductModels = view.findViewById(R.id.txtMultipleDetailProductModels);
            holder.txtMultipleDetailProcessId = view.findViewById(R.id.txtMultipleDetailProcessId);
            holder.txtMultipleDetailProcess = view.findViewById(R.id.txtMultipleDetailProcess);
            holder.txtMultipleDetailDevice = view.findViewById(R.id.txtMultipleDetailDevice);
            holder.txtMultipleDetailQuantity = view.findViewById(R.id.txtMultipleDetailQuantity);
            holder.txtMultipleDetailBadQuantity = view.findViewById(R.id.txtMultipleDetailBadQuantity);
            holder.txtMultipleDetailDocno = view.findViewById(R.id.txtMultipleDetailDocno);
            holder.txtMultipleDetailEmployee = view.findViewById(R.id.txtMultipleDetailEmployee);
            holder.txtMultipleDetailLots = view.findViewById(R.id.txtMultipleDetailLots);
            holder.txtMultipleDetailTray = view.findViewById(R.id.txtMultipleDetailTray);
            holder.txtMultipleDetailPackage = view.findViewById(R.id.txtMultipleDetailPackage);
            holder.txtMultipleDetailStationDocno = view.findViewById(R.id.txtMultipleDetailStationDocno);
            holder.txtMultipleConnectProcess = view.findViewById(R.id.txtMultipleConnectProcess);
            holder.txtMultiplePlanDocno = view.findViewById(R.id.txtMultiplePlanDocno);
            holder.txtMultipleVersion = view.findViewById(R.id.txtMultipleVersion);

            view.setTag(holder);
        }else{
            holder=(MultipleDetailViewHolder)view.getTag();
        }

        //值显示
        holder.imageMultipleDetailViewIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.detail_list_top));
        holder.multipleDetailStatus.setImageDrawable(view.getResources().getDrawable(R.drawable.list_alarm));
        holder.txtMultipleDetailPlanDate.setText((String)mData.get(i).get("PlanDate"));
        holder.txtMultipleDetailProductName.setText((String)mData.get(i).get("ProductName"));
        holder.txtMultipleDetailProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.txtMultipleDetailProductModels.setText((String)mData.get(i).get("ProductModels"));
        holder.txtMultipleDetailProcessId.setText((String)mData.get(i).get("ProcessId"));
        holder.txtMultipleDetailProcess.setText((String)mData.get(i).get("Process"));
        holder.txtMultipleDetailDevice.setText((String)mData.get(i).get("Device"));
        holder.txtMultipleDetailDocno.setText((String)mData.get(i).get("Docno"));
        holder.txtMultipleDetailEmployee.setText((String)mData.get(i).get("Employee"));
        holder.txtMultipleDetailLots.setText((String)mData.get(i).get("Lots"));
        holder.txtMultipleDetailTray.setText((String)mData.get(i).get("Tray"));
        holder.txtMultipleDetailPackage.setText((String)mData.get(i).get("Package"));
        holder.txtMultipleDetailStationDocno.setText((String)mData.get(i).get("StationDocno"));
        holder.txtMultiplePlanDocno.setText((String)mData.get(i).get("PlanNo"));
        holder.txtMultipleVersion.setText((String)mData.get(i).get("Version"));

        //获取连线标识，区分首序、末序，焊接检查连线是否末序录入
        String sConnectProcess = (String)mData.get(i).get("ConnectProcess");
        holder.txtMultipleConnectProcess.setText(sConnectProcess);
        if(sConnectProcess.equals("END")){
            holder.txtMultipleDetailQuantity.setVisibility(View.VISIBLE);
            holder.txtMultipleDetailBadQuantity.setVisibility(View.VISIBLE);
        }else{
            holder.txtMultipleDetailQuantity.setVisibility(View.GONE);
            holder.txtMultipleDetailBadQuantity.setVisibility(View.GONE);
        }

        //冲压检查同模是否可录入
        String sModStatus = (String)mData.get(i).get("ModStatus");
        if(sModStatus.equals("2")||sModStatus.equals("3")){
            if(i>0){
                holder.txtMultipleDetailQuantity.setVisibility(View.GONE);
                holder.txtMultipleDetailBadQuantity.setVisibility(View.GONE);
            }else{
                holder.txtMultipleDetailQuantity.setVisibility(View.VISIBLE);
                holder.txtMultipleDetailBadQuantity.setVisibility(View.VISIBLE);
            }
        }

        //良品
        holder.txtMultipleDetailQuantity.addTextChangedListener(new InputTextWatcher(holder));
        holder.txtMultipleDetailQuantity.setTag(i);

        //废品
        holder.txtMultipleDetailBadQuantity.setOnClickListener(mBadQuantityClickListener);
        holder.txtMultipleDetailBadQuantity.setTag(i);


        return view;
    }

    public static class MultipleDetailViewHolder{
        ImageView imageMultipleDetailViewIcon,multipleDetailStatus;
        TextView txtMultipleDetailProductName,txtMultipleDetailPlanDate,txtMultipleDetailProductCode,txtMultipleDetailProductModels,txtMultipleDetailProcessId;
        TextView txtMultipleDetailProcess,txtMultipleDetailDevice,txtMultipleDetailQuantity,txtMultipleDetailBadQuantity,txtMultipleDetailDocno;
        TextView txtMultipleDetailEmployee,txtMultipleDetailLots,txtMultipleDetailTray,txtMultipleDetailPackage,txtMultipleDetailStationDocno;
        TextView txtMultipleConnectProcess,txtMultiplePlanDocno,txtMultipleVersion;
    }

    public void setBadQuantity(int index,ListView listView,String quantity){
        //获取第一个可见item项的位置
        int visiblePosition = listView.getFirstVisiblePosition();

        //获取指定位置的视图
        View view = listView.getChildAt(index-visiblePosition);
        if(view!=null){
            MultipleDetailViewHolder holder = (MultipleDetailViewHolder)view.getTag();
            holder.txtMultipleDetailBadQuantity = view.findViewById(R.id.txtMultipleDetailBadQuantity);
            holder.txtMultipleDetailBadQuantity.setText(quantity);
            mData.get(index).put("BadQuantity", quantity);
        }
    }

    private class InputTextWatcher implements TextWatcher{

        private MultipleDetailViewHolder multipleDetailViewHolder;

        public InputTextWatcher(MultipleDetailViewHolder multipleDetailViewHolder){
            this.multipleDetailViewHolder = multipleDetailViewHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            try{
                int posTag =(int)multipleDetailViewHolder.txtMultipleDetailQuantity.getTag();
                mData.get(posTag).put("Quantity", charSequence.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    /**
    *描述: 废品录入
    *日期：2023/4/14
    **/
    public static abstract class BadQuantityClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            BadQuantityClick((Integer)view.getTag(),view);
        }

        public abstract void BadQuantityClick(int position,View view);
    }
}
