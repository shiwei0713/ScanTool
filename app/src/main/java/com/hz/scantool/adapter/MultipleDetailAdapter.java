package com.hz.scantool.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
    private String mModStatus;
    private Context mContext;

    public MultipleDetailAdapter(List<Map<String,Object>> mData, Context mContext,String mModStatus){
        this.mData = mData;
        this.mContext = mContext;
        this.mModStatus = mModStatus;
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
            holder.txtMultipleDetailDocno = view.findViewById(R.id.txtMultipleDetailDocno);
            holder.txtMultipleDetailEmployee = view.findViewById(R.id.txtMultipleDetailEmployee);
            holder.txtMultipleDetailLots = view.findViewById(R.id.txtMultipleDetailLots);
            holder.txtMultipleProductDocno = view.findViewById(R.id.txtMultipleProductDocno);

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
        holder.status = (String)mData.get(i).get("Status");

        holder.txtMultipleDetailQuantity.addTextChangedListener(new InputTextWatcher(holder));
        holder.txtMultipleDetailQuantity.setTag(i);

        if(mModStatus.equals("2")||mModStatus.equals("3")){
            if(i>0){
                holder.txtMultipleDetailQuantity.setVisibility(View.GONE);
            }else{
                holder.txtMultipleDetailQuantity.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }

    public static class MultipleDetailViewHolder{
        ImageView imageMultipleDetailViewIcon;
        ImageView multipleDetailStatus;
        TextView txtMultipleDetailProductName;
        TextView txtMultipleDetailPlanDate;
        TextView txtMultipleDetailProductCode;
        TextView txtMultipleDetailProductModels;
        TextView txtMultipleDetailProcessId;
        TextView txtMultipleDetailProcess;
        TextView txtMultipleDetailDevice;
        EditText txtMultipleDetailQuantity;
        TextView txtMultipleDetailDocno;
        TextView txtMultipleDetailEmployee;
        TextView txtMultipleDetailLots;
        TextView txtMultipleProductDocno;

        String status;
    }

    //更新数据
    public void updateData(int index, ListView listView,String strProductDocno){
        //获取第一个可见item项的位置
        int visiblePosition = listView.getFirstVisiblePosition();

        //获取指定位置的视图
        View view = listView.getChildAt(index-visiblePosition);
        MultipleDetailViewHolder holder = (MultipleDetailViewHolder)view.getTag();
        holder.txtMultipleProductDocno = view.findViewById(R.id.txtMultipleProductDocno);
        holder.txtMultipleProductDocno.setText(strProductDocno);
        mData.get(index).put("ProductDocno", strProductDocno);
    }

    private class InputTextWatcher implements TextWatcher{

        private MultipleDetailViewHolder multipleDetailViewHolder;

        public InputTextWatcher(MultipleDetailViewHolder multipleDetailViewHolder){
            this.multipleDetailViewHolder = multipleDetailViewHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if(mModStatus.equals("2")||mModStatus.equals("3")){
                int posTag =(int)multipleDetailViewHolder.txtMultipleDetailQuantity.getTag();
                if(posTag>0){
                    MyToast.myShow(mContext,"同模不分左右零件只维护第一行数量",2,0);
                }
            }
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


}
