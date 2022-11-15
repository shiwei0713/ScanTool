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
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class SubSaleDetailAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private PrintClickListener printClickListener;

    public SubSaleDetailAdapter(List<Map<String,Object>> mData, Context mContext,PrintClickListener printClickListener){
        this.mData = mData;
        this.mContext = mContext;
        this.printClickListener = printClickListener;
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

    public String getItemValue(int i,String name){
        return (String) mData.get(i).get(name);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SubSaleDetailViewHolder holder = null;
        if(view == null){
            holder = new SubSaleDetailViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_sub_sale_detail,viewGroup,false);

            holder.saleDetailProductName = view.findViewById(R.id.saleDetailProductName);
            holder.saleDetailFlag = view.findViewById(R.id.saleDetailFlag);
            holder.saleDetailProductCode = view.findViewById(R.id.saleDetailProductCode);
            holder.saleDetailProductModel = view.findViewById(R.id.saleDetailProductModel);
            holder.saleDetailStockId = view.findViewById(R.id.saleDetailStockId);
            holder.saleDetailStock = view.findViewById(R.id.saleDetailStock);
            holder.saleDetailPositionId = view.findViewById(R.id.saleDetailPositionId);
            holder.saleDetailPosition = view.findViewById(R.id.saleDetailPosition);
            holder.saleDetailQuantity = view.findViewById(R.id.saleDetailQuantity);
            holder.saleDetailInventory = view.findViewById(R.id.saleDetailInventory);
            holder.saleDetailQuantityStock = view.findViewById(R.id.saleDetailQuantityStock);
            holder.btnPrint = view.findViewById(R.id.btnPrint);
            holder.saleDetailPackageStock = view.findViewById(R.id.saleDetailPackageStock);
            holder.editsaleDetailQuantity = view.findViewById(R.id.editsaleDetailQuantity);
            holder.editSaleDetailPrintQuantity = view.findViewById(R.id.editSaleDetailPrintQuantity);
            holder.editSaleDetailPrintQuantityStock = view.findViewById(R.id.editSaleDetailPrintQuantityStock);
            holder.saleDetailPrintQuantity = view.findViewById(R.id.saleDetailPrintQuantity);
            holder.saleDetailPrintQuantityStock = view.findViewById(R.id.saleDetailPrintQuantityStock);

            view.setTag(holder);
        }else{
            holder=(SubSaleDetailViewHolder)view.getTag();
        }

        holder.saleDetailProductName.setText((String)mData.get(i).get("ProductName"));
        holder.saleDetailProductCode.setText((String)mData.get(i).get("ProductCode"));
        holder.saleDetailProductModel.setText((String)mData.get(i).get("ProductModels"));
        holder.saleDetailFlag.setText((String)mData.get(i).get("Status"));
        holder.saleDetailStockId.setText((String)mData.get(i).get("StockId"));
        holder.saleDetailStock.setText((String)mData.get(i).get("Stock"));
        holder.saleDetailPositionId.setText((String)mData.get(i).get("PositionId"));
        holder.saleDetailPosition.setText((String)mData.get(i).get("Position"));
        holder.saleDetailQuantity.setText((String)mData.get(i).get("Quantity"));
        holder.saleDetailInventory.setText((String)mData.get(i).get("Inventory"));
        holder.saleDetailQuantityStock.setText((String)mData.get(i).get("QuantityStock"));
        holder.saleDetailPackageStock.setText((String)mData.get(i).get("Packages"));
        holder.saleDetailPrintQuantity.setText((String)mData.get(i).get("PrintQuantity"));
        holder.saleDetailPrintQuantityStock.setText((String)mData.get(i).get("PrintQuantityStock"));

        //修改包装量
        holder.editsaleDetailQuantity.addTextChangedListener(new InputTextWatcher(holder));
        holder.editsaleDetailQuantity.setTag(i);

        //修改出货回仓量
        holder.editSaleDetailPrintQuantity.addTextChangedListener(new InputTextWatcher2(holder));
        holder.editSaleDetailPrintQuantity.setTag(i);
        holder.editSaleDetailPrintQuantityStock.addTextChangedListener(new InputTextWatcher3(holder));
        holder.editSaleDetailPrintQuantityStock.setTag(i);

        //打印
        holder.btnPrint.setOnClickListener(printClickListener);
        holder.btnPrint.setTag(i);

        return view;
    }

    public static class SubSaleDetailViewHolder{
        TextView saleDetailProductName,saleDetailFlag,saleDetailProductCode,saleDetailProductModel,saleDetailQuantity,saleDetailInventory,saleDetailQuantityStock;
        TextView saleDetailStockId,saleDetailStock,saleDetailPositionId,saleDetailPosition,saleDetailPackageStock,saleDetailPrintQuantity,saleDetailPrintQuantityStock;
        EditText editsaleDetailQuantity,editSaleDetailPrintQuantity,editSaleDetailPrintQuantityStock;
        Button btnPrint;
    }

    public static abstract class PrintClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            PrintOnClick((Integer) view.getTag(),view);
        }

        public abstract void PrintOnClick(int position,View v);
    }

    /**
    *描述: 非标包装量
    *日期：2022/10/18
    **/
    private class InputTextWatcher implements TextWatcher {

        private SubSaleDetailViewHolder subSaleDetailViewHolder;

        public InputTextWatcher(SubSaleDetailViewHolder subSaleDetailViewHolder){
            this.subSaleDetailViewHolder = subSaleDetailViewHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            try{
                int posTag =(int)subSaleDetailViewHolder.editsaleDetailQuantity.getTag();
                mData.get(posTag).put("Packages", charSequence.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    /**
     *描述: 出货量
     *日期：2022/10/18
     **/
    private class InputTextWatcher2 implements TextWatcher {

        private SubSaleDetailViewHolder subSaleDetailViewHolder;

        public InputTextWatcher2(SubSaleDetailViewHolder subSaleDetailViewHolder){
            this.subSaleDetailViewHolder = subSaleDetailViewHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            try{
                int posTag2 = (int)subSaleDetailViewHolder.editSaleDetailPrintQuantity.getTag();
                mData.get(posTag2).put("PrintQuantity", charSequence.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    /**
     *描述: 回仓量
     *日期：2022/10/18
     **/
    private class InputTextWatcher3 implements TextWatcher {

        private SubSaleDetailViewHolder subSaleDetailViewHolder;

        public InputTextWatcher3(SubSaleDetailViewHolder subSaleDetailViewHolder){
            this.subSaleDetailViewHolder = subSaleDetailViewHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            try{
                int posTag3 = (int)subSaleDetailViewHolder.editSaleDetailPrintQuantityStock.getTag();
                mData.get(posTag3).put("PrintQuantityStock", charSequence.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
