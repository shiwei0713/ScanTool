package com.hz.scantool.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.hz.scantool.models.Company;
import com.hz.scantool.R;
import com.hz.scantool.helper.SharedHelper;
import com.hz.scantool.helper.WebServiceHelper;
import com.hz.scantool.printer.NetPrinter;

import java.util.List;
import java.util.Map;

public class ListItemAdapter extends BaseAdapter {

    private List<Map<String,Object>> mData;
    private Context mContext;
    private Integer typeCode;
    private Integer typeIndex;
    private Integer choiceItem;
    private SharedHelper sharedHelper;
    private String userList;
    private String nerworkType;
    private Company company;

    //创建Handler
    private final Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            ViewHolder mHolder = (ViewHolder)msg.obj;
            if(msg.what == 1){
                mHolder.listStatus.setImageDrawable(mContext.getResources().getDrawable(R.drawable.list_status_ok));
                Toast.makeText(mContext,"出货单:"+mHolder.txtListDocno.getText()+"更新成功!",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(mContext,"出货单:"+mHolder.txtListDocno.getText()+"更新失败!",Toast.LENGTH_SHORT).show();
            }
        }
    };

    public ListItemAdapter(List<Map<String,Object>> mData,Context mContext){
        this.mData = mData;
        this.mContext = mContext;
    }

    public void setItemType(int typeCode,int typeIndex,SharedHelper sharedHelper){
        this.typeCode = typeCode;
        this.typeIndex = typeIndex;
        this.sharedHelper = sharedHelper;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if(convertView == null) {
            holder = new ViewHolder();
            //实例化组件,获取组件
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_list_item, parent, false);

            holder.txtListProductCodeTitle = convertView.findViewById(R.id.txtListProductCodeTitle);
            holder.txtListProductModelsTitle = convertView.findViewById(R.id.txtListProductModelsTitle);
            holder.txtListProducerTitle = convertView.findViewById(R.id.txtListProducerTitle);
            holder.txtListProcessTitle = convertView.findViewById(R.id.txtListProcessTitle);
            holder.txtListDeviceTitle = convertView.findViewById(R.id.txtListDeviceTitle);
            holder.txtListStockIdTitle = convertView.findViewById(R.id.txtListStockIdTitle);
            holder.txtListStockTitle = convertView.findViewById(R.id.txtListStockTitle);
            holder.txtListInventoryTitle = convertView.findViewById(R.id.txtListInventoryTitle);
            holder.txtListMouldTitle = convertView.findViewById(R.id.txtListMouldTitle);
            holder.txtListDocnoTitle = convertView.findViewById(R.id.txtListDocnoTitle);
            holder.txtListQuantityTitle = convertView.findViewById(R.id.txtListQuantityTitle);
            holder.txtListQuantityPcsTitle = convertView.findViewById(R.id.txtListQuantityPcsTitle);

            holder.txtListProductCode = convertView.findViewById(R.id.txtListProductCode);
            holder.txtListProductName = convertView.findViewById(R.id.txtListProductName);
            holder.txtListPlanDate = convertView.findViewById(R.id.txtListPlanDate);
            holder.txtListProductModels = convertView.findViewById(R.id.txtListProductModels);
            holder.txtListProducerId = convertView.findViewById(R.id.txtListProducerId);
            holder.txtListProducer = convertView.findViewById(R.id.txtListProducer);
            holder.txtListProcess = convertView.findViewById(R.id.txtListProcess);
            holder.txtListDevice = convertView.findViewById(R.id.txtListDevice);
            holder.txtListStockId = convertView.findViewById(R.id.txtListStockId);
            holder.txtListStock = convertView.findViewById(R.id.txtListStock);
            holder.txtListInventory = convertView.findViewById(R.id.txtListInventory);
            holder.txtListMould = convertView.findViewById(R.id.txtListMould);
            holder.txtListDocno = convertView.findViewById(R.id.txtListDocno);
            holder.txtListQuantity = convertView.findViewById(R.id.txtListQuantity);
            holder.txtListQuantityPcs = convertView.findViewById(R.id.txtListQuantityPcs);

            holder.listStatus = convertView.findViewById(R.id.listStatus);
            holder.listBtnConfirm = convertView.findViewById(R.id.listBtnConfirm);
            holder.listBtnPrint = convertView.findViewById(R.id.listBtnPrint);
            holder.listBtnSend = convertView.findViewById(R.id.listBtnSend);
            holder.listBtnTask = convertView.findViewById(R.id.listBtnTask);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        //初始化标题显示
        initViewTitle(holder);

        //设置组件显示值
        holder.txtListProductCode.setText((String)mData.get(position).get("ProductCode"));
        holder.txtListProductName.setText((String)mData.get(position).get("ProductName"));
        holder.txtListPlanDate.setText((String)mData.get(position).get("PlanDate"));
        holder.txtListProductModels.setText((String)mData.get(position).get("ProductModels"));
        holder.txtListProducerId.setText((String)mData.get(position).get("ProducerId"));
        holder.txtListProducer.setText((String)mData.get(position).get("Producer"));
        holder.txtListProcess.setText((String)mData.get(position).get("Process"));
        holder.txtListDevice.setText((String)mData.get(position).get("Device"));
        holder.txtListStockId.setText((String)mData.get(position).get("StockId"));
        holder.txtListStock.setText((String)mData.get(position).get("Stock"));
        holder.txtListInventory.setText((String)mData.get(position).get("Inventory"));
        holder.txtListMould.setText((String)mData.get(position).get("Mould"));
        holder.txtListDocno.setText((String)mData.get(position).get("Docno"));
        holder.txtListQuantity.setText((String)mData.get(position).get("Quantity"));
        holder.txtListQuantityPcs.setText((String)mData.get(position).get("QuantityPcs"));

        if(typeCode == 5){
            userList = (String)mData.get(position).get("ProductModels");
        }

        holder.strStatus = (String)mData.get(position).get("Status");
        if(holder.strStatus.equals("Y")){
            holder.listStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_ok));
        }else{
            if(holder.strStatus.equals("X")){
                holder.listBtnSend.setVisibility(View.VISIBLE);
                holder.listStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_ng));
            }else {
                holder.listStatus.setImageDrawable(convertView.getResources().getDrawable(R.drawable.list_status_deal));
            }
        }

        //按钮事件
        holder.listBtnConfirm.setOnClickListener(new listOnClickListener(holder,position,(String)mData.get(position).get("Docno")));
        holder.listBtnTask.setOnClickListener(new listOnClickListener(holder,position,(String)mData.get(position).get("Docno")));

        return convertView;
    }

    public static class ViewHolder{
        TextView txtListProductCodeTitle;
        TextView txtListProductModelsTitle;
        TextView txtListProducerTitle;
        TextView txtListProcessTitle;
        TextView txtListDeviceTitle;
        TextView txtListStockIdTitle;
        TextView txtListStockTitle;
        TextView txtListInventoryTitle;
        TextView txtListMouldTitle;
        TextView txtListQuantityTitle;
        TextView txtListQuantityPcsTitle;
        TextView txtListDocnoTitle;

        TextView txtListProductName;
        TextView txtListPlanDate;
        TextView txtListProductCode;
        TextView txtListProductModels;
        TextView txtListProducerId;
        TextView txtListProducer;
        TextView txtListProcess;
        TextView txtListDevice;
        TextView txtListStockId;
        TextView txtListStock;
        TextView txtListInventory;
        TextView txtListMould;
        TextView txtListDocno;
        TextView txtListQuantity;
        TextView txtListQuantityPcs;

        Button listBtnConfirm;
        Button listBtnPrint;
        Button listBtnSend;
        Button listBtnTask;

        ImageView listStatus;
        String strStatus;
    }

    class listOnClickListener implements View.OnClickListener{
        private int position;
        private String qrCode;
        private ViewHolder holder;

        listOnClickListener(ViewHolder holder,int position,String qrCode){
            this.holder = holder;
            this.position = position;
            this.qrCode = qrCode;
        }

        @Override
        public void onClick(View v) {
            //printLabel();
            switch (v.getId()){
                case R.id.listBtnTask:
                    showListDialog(holder);
                    break;
                case R.id.listBtnConfirm:
                    Toast.makeText(mContext,"danji"+position+";CODE:"+qrCode,Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    public void showListDialog(ViewHolder holder){
        final String[] items = userList.split(";");
        choiceItem = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("选择");
        builder.setIcon(R.drawable.dialog_error);
        builder.setSingleChoiceItems(items,-1, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                choiceItem = which;
            }
        });

        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(choiceItem != -1){
                    updateSale(holder,items[choiceItem]);
                    holder.txtListProductName.setText(items[choiceItem]);
                }else{
                    Toast.makeText(mContext,"未选择任何选项，更新失败！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    public void printLabel(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetPrinter netPrinter = new NetPrinter();
                boolean printStatus = netPrinter.openPrinter("192.168.20.118");
                Log.i("erpStatus",String.valueOf(printStatus));
            }
        }).start();
    }

    private void updateSale(ViewHolder holder,String choiceUser){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //初始化网络类型和营运据点
//                company=new Company();
//                Map<String,String> data=sharedHelper.readShared();
//                nerworkType = data.get("network");
//                company.setSite(data.get("userSite"));

                //初始化T100服务名
                String webServiceName = "SaleRequestUpdate";

                //设置传入请求参数
                StringBuilder strWebRequestConten= new StringBuilder();
                strWebRequestConten.append("&lt;Document&gt;\n"+
                        "&lt;RecordSet id=\"1\"&gt;\n"+
                        "&lt;Master name=\"xmdk_t\" node_id=\"1\"&gt;\n"+
                        "&lt;Record&gt;\n"+
                        "&lt;Field name=\"xmdksite\" value=\""+company.getCode()+"\"/&gt;\n"+
                        "&lt;Field name=\"xmdkent\" value=\"10\"/&gt;\n"+
                        "&lt;Field name=\"xmdkdocno\" value=\""+holder.txtListDocno.getText()+"\"/&gt;\n"+
                        "&lt;Field name=\"xmdkud002\" value=\""+choiceUser+"\"/&gt;\n"+
                        "&lt;Field name=\"xmdkud003\" value=\"N\"/&gt;\n"+
                        "&lt;Memo/&gt;\n"+
                        "&lt;Attachment count=\"0\"/&gt;\n"+
                        "&lt;/Record&gt;\n"+
                        "&lt;/Master&gt;\n"+
                        "&lt;/RecordSet&gt;\n"+
                        "&lt;/Document&gt;\n");

                //设置WebService参数
                WebServiceHelper webServiceHelper=new WebServiceHelper();
                webServiceHelper.setWebKey("16baae6c40b922d8ddb12a0320d8ea1d");
                webServiceHelper.setWebTimestamp("20201114083106031");
                webServiceHelper.setWebName(webServiceName);
                webServiceHelper.setWebUrl(nerworkType);
                webServiceHelper.setWebSite(company.getCode());
                webServiceHelper.setWebRequestContent(strWebRequestConten);

                //发送WebService请求,并返回结果
                String strResponse = "";
                try{
                    strResponse=webServiceHelper.sendWebRequest();

                    //获取WebService相应代码
                    Integer iResponseCode=webServiceHelper.getWebResponseCode();
                    Message message = new Message();
                    if(iResponseCode==200){
                        message.what = 1;
                        message.obj = holder;
                    }else{
                        message.what = 0;
                    }
                    mHandler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initViewTitle(ViewHolder holder){
        //设置标题显示值
        String strProductCodeTitle = mContext.getResources().getString(R.string.item_title_product_code);
        String strProcessTitle = mContext.getResources().getString(R.string.item_title_process);
        String strDeviceTitle = mContext.getResources().getString(R.string.item_title_device);
        String strQuantityTitle = mContext.getResources().getString(R.string.item_title_quantity);
        String strQuantityPcsTitle = mContext.getResources().getString(R.string.item_title_quantity_pcs);
        String strDocnoTitle = mContext.getResources().getString(R.string.item_title_docno);
        String strProducerTitle = mContext.getResources().getString(R.string.item_title_department);
        String strProductModels = mContext.getResources().getString(R.string.item_title_models);
        String strStockIdTitle = mContext.getResources().getString(R.string.item_title_stockid);
        String strStockTitle = mContext.getResources().getString(R.string.item_title_stock);
        String strMouldTitle = mContext.getResources().getString(R.string.item_title_mould);
        String strInventory = mContext.getResources().getString(R.string.item_title_inventory);

        //初始化控件
        //typeCode:模块代码 0:工序报工;1:质量检验;2:完工入库;3:采购入库;4:生产备料;5:销售出货;6:生产协同;7:查询报表
        //typeIndex:页签索引
        holder.txtListQuantityPcs.setVisibility(View.GONE);//片数
        holder.txtListProcess.setVisibility(View.GONE);//工序
        holder.txtListDevice.setVisibility(View.GONE);//设备
        holder.txtListStockId.setVisibility(View.GONE);//仓库
        holder.txtListStock.setVisibility(View.GONE);//仓库
        holder.txtListInventory.setVisibility(View.GONE);//库存
        holder.txtListMould.setVisibility(View.GONE);//模具

        holder.txtListQuantityPcsTitle.setVisibility(View.GONE);//片数标题
        holder.txtListProcessTitle.setVisibility(View.GONE); //工序标题
        holder.txtListDeviceTitle.setVisibility(View.GONE);//设备标题
        holder.txtListStockIdTitle.setVisibility(View.GONE);//仓库ID标题
        holder.txtListStockTitle.setVisibility(View.GONE);//仓库标题
        holder.txtListInventoryTitle.setVisibility(View.GONE);//库存标题
        holder.txtListMouldTitle.setVisibility(View.GONE);//模具标题

        holder.listBtnConfirm.setVisibility(View.GONE); //报首检
        holder.listBtnPrint.setVisibility(View.GONE);   //打印标签
        holder.listBtnSend.setVisibility(View.GONE);    //发送报警
        holder.listBtnTask.setVisibility(View.GONE);   //分配任务

        switch (typeCode){
            case 1: //质量检验
                switch (typeIndex){
                    case 1:  //IQC
                        strProducerTitle = mContext.getResources().getString(R.string.item_title_supply);
                        break;
                    case 2:  //PQC
                        holder.txtListProcessTitle.setVisibility(View.VISIBLE); //工序标题
                        holder.txtListDeviceTitle.setVisibility(View.VISIBLE);//设备标题
                        holder.txtListProcess.setVisibility(View.VISIBLE);//工序
                        holder.txtListDevice.setVisibility(View.VISIBLE);//设备
                        break;
                    case 3:  //FQC
                        break;
                    case 4:  //OQC
                        strProducerTitle = mContext.getResources().getString(R.string.item_title_saler);
                        break;
                }

                break;
            case 4: //生产备料
                holder.txtListStock.setVisibility(View.VISIBLE);//仓库
                holder.txtListInventory.setVisibility(View.VISIBLE);//库存
                holder.txtListStockTitle.setVisibility(View.VISIBLE);//仓库标题
                holder.txtListInventoryTitle.setVisibility(View.VISIBLE);//库存标题

                switch (typeIndex) {
                    case 1:  //原材料备料
                        strQuantityTitle = mContext.getResources().getString(R.string.item_title_quantity_kg);
                        holder.txtListQuantityPcs.setVisibility(View.VISIBLE);//片数
                        holder.txtListQuantityPcsTitle.setVisibility(View.VISIBLE);//片数标题
                        break;
                }

                break;
            case 5: //销售出货
                holder.txtListProductCode.setVisibility(View.GONE);//料件编码
                holder.txtListProductModels.setVisibility(View.GONE);//规格
                holder.txtListStockId.setVisibility(View.VISIBLE);//仓库
                holder.txtListStock.setVisibility(View.VISIBLE);//仓库
                holder.txtListQuantityPcs.setVisibility(View.VISIBLE);//数量
                holder.txtListProductCodeTitle.setVisibility(View.GONE);//料件编码标题
                holder.txtListProductModelsTitle.setVisibility(View.GONE);//规格标题
                holder.txtListStockIdTitle.setVisibility(View.VISIBLE);//仓库标题
                holder.txtListStockTitle.setVisibility(View.VISIBLE);//仓库标题
                holder.txtListQuantityPcsTitle.setVisibility(View.VISIBLE);//数量标题

                strProducerTitle = mContext.getResources().getString(R.string.item_title_saler);
                strQuantityPcsTitle = mContext.getResources().getString(R.string.detail_detail_quantity_pcs_title);

                switch (typeIndex) {
                    case 1:  //任务分配
                        holder.listBtnTask.setVisibility(View.VISIBLE);
                        break;
                    case 2:  //销售备货

                        break;
                }

                break;
            case 7:  //期末盘点
                strQuantityTitle = mContext.getResources().getString(R.string.item_title_inventory);
                strQuantityPcsTitle = mContext.getResources().getString(R.string.item_title_label);
                strProducerTitle = mContext.getResources().getString(R.string.item_title_storage);
                holder.txtListStock.setVisibility(View.VISIBLE);//仓库
                holder.txtListStockTitle.setVisibility(View.VISIBLE);//仓库标题
                holder.txtListQuantityPcs.setVisibility(View.VISIBLE);//数量
                holder.txtListQuantityPcsTitle.setVisibility(View.VISIBLE);//数量标题

                holder.listStatus.setVisibility(View.GONE);  //状态图片
        }

        //设置标题显示值
        holder.txtListProductCodeTitle.setText(strProductCodeTitle);
        holder.txtListProductModelsTitle.setText(strProductModels);
        holder.txtListProducerTitle.setText(strProducerTitle);
        holder.txtListProcessTitle.setText(strProcessTitle);
        holder.txtListDeviceTitle.setText(strDeviceTitle);
        holder.txtListStockIdTitle.setText(strStockIdTitle);
        holder.txtListStockTitle.setText(strStockTitle);
        holder.txtListInventoryTitle.setText(strInventory);
        holder.txtListMouldTitle.setText(strMouldTitle);
        holder.txtListDocnoTitle.setText(strDocnoTitle);
        holder.txtListQuantityTitle.setText(strQuantityTitle);
        holder.txtListQuantityPcsTitle.setText(strQuantityPcsTitle);
    }
}

