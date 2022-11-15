package com.hz.scantool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hz.scantool.R;

import java.util.List;
import java.util.Map;

public class PrinterListAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private UpdateClickListener mUpdateClickListener;

    public PrinterListAdapter(List<Map<String,Object>> mData, Context mContext,UpdateClickListener mUpdateClickListener){
        this.mData = mData;
        this.mContext = mContext;
        this.mUpdateClickListener = mUpdateClickListener;
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

    public String getItemValue(int i,String name) {
        return (String)mData.get(i).get(name);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        PrinterListViewHolder holder = null;
        if(view == null){
            holder = new PrinterListViewHolder();

            view = LayoutInflater.from(mContext).inflate(R.layout.adapter_printer_list,viewGroup,false);

            holder.detailView = view.findViewById(R.id.detailView);
            holder.txtPrinterIp = view.findViewById(R.id.txtPrinterIp);
            holder.txtDevies = view.findViewById(R.id.txtDevies);
            holder.txtPrinerServer = view.findViewById(R.id.txtPrinerServer);
            holder.printerIcon = view.findViewById(R.id.printerIcon);
            holder.txtResult = view.findViewById(R.id.txtResult);
            holder.printerResart = view.findViewById(R.id.printerResart);

            view.setTag(holder);
        }else{
            holder=(PrinterListViewHolder)view.getTag();
        }

        //默认隐藏详细页
//        holder.detailView.setVisibility(View.GONE);

        //事件绑定
        holder.printerResart.setOnClickListener(mUpdateClickListener);
        holder.printerResart.setTag(i);

        //值显示
        holder.printerIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.printer_list_top));
        holder.txtPrinterIp.setText((String)mData.get(i).get("PrinterIp"));
        holder.txtDevies.setText((String)mData.get(i).get("Device"));
        holder.txtPrinerServer.setText((String)mData.get(i).get("PrinterServer"));

        return view;
    }

    public static class PrinterListViewHolder{
        LinearLayout detailView;
        TextView txtPrinterIp,txtDevies,txtPrinerServer,txtResult;
        ImageView printerIcon;
        Button printerResart;
    }

    //更新数据
    public void updateData(int index, View view,String msg){
        //获取指定位置的视图
        PrinterListViewHolder holder = (PrinterListViewHolder)view.getTag();
        holder.txtResult.setText(msg.replace("_","\n"));
    }

    //重启设备
    public static abstract class UpdateClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            UpdateClick((Integer)view.getTag(),view);
        }

        public abstract void UpdateClick(int position,View view);
    }
}
