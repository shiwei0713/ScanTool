package com.hz.scantool.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DeliveryOrderEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String docNo;
    private String productCode;
    private String productName;
    private String productModels;
    private int quantity;
    private String qrCodeRule;
    private String status;

    public DeliveryOrderEntity(long id,String docNo,String productCode,String productName,String productModels,int quantity,String qrCodeRule,String status){
        this.id=id;
        this.docNo = docNo;
        this.productCode = productCode;
        this.productName = productName;
        this.productModels = productModels;
        this.quantity = quantity;
        this.qrCodeRule = qrCodeRule;
        this.status = status;
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

    public String getQrCodeRule() {
        return qrCodeRule;
    }

    public void setQrCodeRule(String qrCodeRule) {
        this.qrCodeRule = qrCodeRule;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
