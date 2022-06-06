package com.hz.scantool.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity
public class LabelEntity {
    @PrimaryKey
    @NonNull
    private String docNo;
    private String productCode;
    private String productName;
    private String productModels;
    private int quantity;
    private int quantityPcs;
    private String tray;
    private String status;

    public LabelEntity(String docNo,String productCode,String productName,String productModels,int quantity,int quantityPcs,String tray,String status){
        this.docNo = docNo;
        this.productCode = productCode;
        this.productName = productName;
        this.productModels = productModels;
        this.quantity = quantity;
        this.quantityPcs = quantityPcs;
        this.tray = tray;
        this.status = status;
    }

    @NonNull
    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(@NonNull String docNo) {
        this.docNo = docNo;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductModels() {
        return productModels;
    }

    public void setProductModels(String productModels) {
        this.productModels = productModels;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantityPcs() {
        return quantityPcs;
    }

    public void setQuantityPcs(int quantityPcs) {
        this.quantityPcs = quantityPcs;
    }

    public String getTray() {
        return tray;
    }

    public void setTray(String tray) {
        this.tray = tray;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getListData(){
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("Docno", docNo);
        map.put("ProductCode", productCode);
        map.put("ProductName", productName);
        map.put("ProductModels", productModels);
        map.put("Quantity", quantity);
        map.put("QuantityPcs", quantityPcs);
        map.put("Tray", tray);
        map.put("Status", status);

        return map;
    }
}
