package com.hz.scantool.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class DiffQtyListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;

    public DiffQtyListAdapter(List<Map<String,Object>> mData, Context mContext){
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

    public String getQuantity(int i){
        return (String)mData.get(i).get("Quantity");
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        DiffQtyViewHolder holder = null;
        if(view == null){
            holder = new DiffQtyViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.list_diff_qty,viewGroup,false);

            holder.listDiffQtyQrcode = view.findViewById(R.id.listDiffQtyQrcode);
            holder.listDiffQtyProductName = view.findViewById(R.id.listDiffQtyProductName);
            holder.listDiffQtyQpa = view.findViewById(R.id.listDiffQtyQpa);
            holder.listDiffQtyQuantity = view.findViewById(R.id.listDiffQtyQuantity);
            holder.editDiffQtyQuantity = view.findViewById(R.id.editDiffQtyQuantity);

            view.setTag(holder);
        }else{
            holder=(DiffQtyViewHolder)view.getTag();
        }

        //值显示
        holder.listDiffQtyQrcode.setText((String)mData.get(i).get("Qrcode"));
        holder.listDiffQtyProductName.setText((String)mData.get(i).get("ProductName"));
        holder.listDiffQtyQpa.setText((String)mData.get(i).get("Process"));
        holder.listDiffQtyQuantity.setText((String)mData.get(i).get("Quantity"));

        //事件处理
        holder.editDiffQtyQuantity.addTextChangedListener(new InputTextWatcher(holder));
        holder.editDiffQtyQuantity.setTag(i);

        return view;
    }

    public static class DiffQtyViewHolder{
        TextView listDiffQtyQrcode;
        TextView listDiffQtyProductName;
        TextView listDiffQtyQpa;
        TextView listDiffQtyQuantity;
        EditText editDiffQtyQuantity;

    }

    private class InputTextWatcher implements TextWatcher {

        private DiffQtyViewHolder diffQtyViewHolder;

        public InputTextWatcher(DiffQtyViewHolder diffQtyViewHolder){
            this.diffQtyViewHolder = diffQtyViewHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            try{
                int posTag =(int)diffQtyViewHolder.editDiffQtyQuantity.getTag();
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
