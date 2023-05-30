package com.hz.scantool.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity
public class QrcodeEntity {
    @PrimaryKey
    @NonNull
    private String qrcode;
    private String docNo;
    private String productCode;
    private String productName;
    private String planDate;
    private float quantity;
    private String scantime;

    public QrcodeEntity(String qrcode,String docNo,String productCode,String productName,String planDate,float quantity,String scantime){
        this.qrcode = qrcode;
        this.docNo = docNo;
        this.productCode = productCode;
        this.productName = productName;
        this.planDate = planDate;
        this.quantity = quantity;
        this.scantime = scantime;
    }

    @NonNull
    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(@NonNull String qrcode) {
        this.qrcode = qrcode;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getPlanDate() {
        return planDate;
    }

    public void setPlanDate(String planDate) {
        this.planDate = planDate;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getScantime() {
        return scantime;
    }

    public void setScantime(String scantime) {
        this.scantime = scantime;
    }

    public Map<String, Object> getQrcodeListData(){
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("Qrcode", qrcode);
        map.put("Docno", docNo);
        map.put("ProductCode", productCode);
        map.put("ProductName", productName);
        map.put("Quantity", quantity);
        map.put("Date", planDate);

        return map;
    }
}
