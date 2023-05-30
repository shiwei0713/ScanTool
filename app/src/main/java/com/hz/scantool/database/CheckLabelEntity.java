package com.hz.scantool.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CheckLabelEntity {
    @PrimaryKey
    @NonNull
    private String qrcode;
    private String productCode;
    private String productName;
    private String productModels;
    private String quantity;
    private String processId;
    private String process;

    public CheckLabelEntity(String qrcode,String productCode,String productName,String productModels,String quantity,String processId,String process){
        this.qrcode = qrcode;
        this.productCode = productCode;
        this.productName = productName;
        this.productModels = productModels;
        this.quantity = quantity;
        this.processId = processId;
        this.process = process;
    }

    @NonNull
    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(@NonNull String qrcode) {
        this.qrcode = qrcode;
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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }
}
