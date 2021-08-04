package com.hz.scantool.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageViewModel extends ViewModel {

    private String strError;
    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private MutableLiveData<Integer> mItemType = new MutableLiveData<>();
    private MutableLiveData<String> mQueryWhere = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            strError = "无数据显示";
            return strError;
        }
    });

    //传入页签索引
    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    //传入菜单类别
    public void setItemType(int type) {
        mItemType.setValue(type);
    }

    //传入查询条件零件号
    public void setQueryWhere(String queryWhere) {
        mQueryWhere.setValue(queryWhere);
    }

    public LiveData<String> getText() {
        return mText;
    }

    //获取显示数据
    public List<Map<String,Object>> getData(String listJson){
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        String strDetailContent = listJson;
        String xmlIndexStr = "t100";

        //按照不同菜单筛选不同数据
        switch (mItemType.getValue()){
            case 1:
                if(mIndex.getValue() == 1){
                    xmlIndexStr="iqc";
                }else if(mIndex.getValue() == 2){
                    xmlIndexStr="pqc";
                }else if(mIndex.getValue() == 3){
                    xmlIndexStr="fqc";
                }else if(mIndex.getValue() == 4){
                    xmlIndexStr="oqc";
                }
                break;
            case 3:
                if(mIndex.getValue() == 1){
                    xmlIndexStr="clrk";
                }else if(mIndex.getValue() == 2) {
                    xmlIndexStr ="wgrk";
                }else if(mIndex.getValue() == 3) {
                    xmlIndexStr ="wwrk";
                }
                break;
            case 4:
                if(mIndex.getValue() == 1){
                    xmlIndexStr="clfl";
                }else if(mIndex.getValue() == 2){
                    xmlIndexStr="ljfl";
                }else if(mIndex.getValue() == 3){
                    xmlIndexStr="cltl";
                }else if(mIndex.getValue() == 4){
                    xmlIndexStr="ljtl";
                }
                break;
            case 5:
                if(mIndex.getValue() == 1){
                    xmlIndexStr="salelist";
                }else if(mIndex.getValue() == 2) {
                    xmlIndexStr ="stocklist";
                }else if(mIndex.getValue() == 3) {
                    xmlIndexStr ="refundlist";
                }
                break;
            case 7:
                if(mIndex.getValue() == 1){
                    xmlIndexStr ="checklist";
                }
                break;
        }
        Integer iStartId = strDetailContent.indexOf(xmlIndexStr,1);
        //处理返回xml
        while(iStartId>-1){
            String strSubContent=strDetailContent.substring(iStartId,strDetailContent.length());
            String strJson=strSubContent.substring(strSubContent.indexOf("value",1)+7,strSubContent.indexOf("&gt;",1)-2);
            Map<String,Object> map = new HashMap<String,Object>();
            try{
                JSONArray jsonArray = new JSONArray(strJson);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                map.put("ProductCode",jsonObject.getString("erpProductCode").trim());
                map.put("ProductName",jsonObject.getString("erpProductName").trim());
                map.put("ProductModels",jsonObject.getString("erpProductModels").trim());
                map.put("ProducerId",jsonObject.getString("erpProducerId").trim());
                map.put("Producer",jsonObject.getString("erpProducer").trim());
//                map.put("ProducerType",jsonObject.getString("erpProducerType").trim());
//                map.put("Device",jsonObject.getString("erpDevice").trim());
                map.put("PlanDate",jsonObject.getString("erpPlanDate").trim());
                map.put("Quantity",jsonObject.getString("erpQuantity").trim());
                map.put("Inventory",jsonObject.getString("erpInventory").trim());
                map.put("QuantityPcs",jsonObject.getString("erpQuantityPcs").trim());
//                map.put("Process",jsonObject.getString("erpProcess").trim());
                map.put("StockId",jsonObject.getString("erpStockId").trim());
                map.put("Stock",jsonObject.getString("erpStock").trim());
//                map.put("Mould",jsonObject.getString("erpMould").trim());
                map.put("Docno",jsonObject.getString("erpDocno").trim());
                map.put("Status",jsonObject.getString("erpStatus").trim());
                list.add(map);
            }catch (Exception e){
                e.printStackTrace();
            }

            Integer iCurrentStartId = strSubContent.indexOf("/Record",1);
            Integer iCurrentEndId = strSubContent.length();

            strDetailContent = strSubContent.substring(iCurrentStartId,iCurrentEndId);
            iStartId = strDetailContent.indexOf(xmlIndexStr,1);

        };

        return list;
    }
}