/**
*描述: 销售出货表
*日期：2022/5/28
**/
package com.hz.scantool.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(primaryKeys={"id","docNo","qrCode"})
public class DeliveryOrderEntity {

    private long id;

    @NonNull
    private String docNo;
    @NonNull
    private String qrCode;
    private String salerId;
    private String saler;
    private String productCode;
    private String productName;
    private String productModels;
    private int quantity;
    private int quantityPcs;
    private String qrCodeRule;
    private String saleQrCode;
    private String tray;
    private String status;
    private String desc;

    public DeliveryOrderEntity(long id,String docNo,String salerId,String saler,String productCode,String productName,String productModels,int quantity,int quantityPcs,String qrCodeRule,String tray,String status,String qrCode,String saleQrCode,String desc){
        this.id=id;
        this.docNo = docNo;
        this.salerId = salerId;
        this.saler = saler;
        this.productCode = productCode;
        this.productName = productName;
        this.productModels = productModels;
        this.quantity = quantity;
        this.quantityPcs = quantityPcs;
        this.qrCodeRule = qrCodeRule;
        this.tray = tray;
        this.status = status;
        this.qrCode = qrCode;
        this.saleQrCode = saleQrCode;
        this.desc = desc;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public String getSalerId() {
        return salerId;
    }

    public void setSalerId(String salerId) {
        this.salerId = salerId;
    }

    public String getSaler() {
        return saler;
    }

    public void setSaler(String saler) {
        this.saler = saler;
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

    public String getQrCodeRule() {
        return qrCodeRule;
    }

    public void setQrCodeRule(String qrCodeRule) {
        this.qrCodeRule = qrCodeRule;
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

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getSaleQrCode() {
        return saleQrCode;
    }

    public void setSaleQrCode(String saleQrCode) {
        this.saleQrCode = saleQrCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Map<String, Object> getListData(){
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("Docno", docNo);
        map.put("SalerId", salerId);
        map.put("Saler", saler);
        map.put("CodeRule", qrCodeRule);
        map.put("ProductCode", productCode);
        map.put("ProductName", productName);
        map.put("ProductModels", productModels);
        map.put("Quantity", quantity);
        map.put("QuantityPcs", quantityPcs);
        map.put("Tray", tray);
        map.put("QrCode", qrCode);
        map.put("saleQrCode", saleQrCode);
        map.put("desc", desc);
        map.put("Status", status);

        return map;
    }
}
