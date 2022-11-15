package com.hz.scantool.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ProductEntity {
    @PrimaryKey
    @NonNull
    private String productDocno;
    private String planDocno;
    private int planSeq;
    private int version;
    private String productCode;
    private String processId;
    private String process;
    private String devices;
    private String productUser;
    private String lots;
    private float quantity;
    private String planDate;

    public ProductEntity(String productDocno,String planDocno,int planSeq,int version,String productCode,String processId,String process,String devices,String productUser,String lots,float quantity,String planDate){
        this.productDocno = productDocno;
        this.planDocno = planDocno;
        this.planSeq = planSeq;
        this.version = version;
        this.productCode = productCode;
        this.processId = processId;
        this.process = process;
        this.devices = devices;
        this.productUser = productUser;
        this.lots = lots;
        this.quantity = quantity;
        this.planDate = planDate;
    }

    @NonNull
    public String getProductDocno() {
        return productDocno;
    }

    public void setProductDocno(@NonNull String productDocno) {
        this.productDocno = productDocno;
    }

    public String getPlanDocno() {
        return planDocno;
    }

    public void setPlanDocno(String planDocno) {
        this.planDocno = planDocno;
    }

    public int getPlanSeq() {
        return planSeq;
    }

    public void setPlanSeq(int planSeq) {
        this.planSeq = planSeq;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
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

    public String getDevices() {
        return devices;
    }

    public void setDevices(String devices) {
        this.devices = devices;
    }

    public String getProductUser() {
        return productUser;
    }

    public void setProductUser(String productUser) {
        this.productUser = productUser;
    }

    public String getLots() {
        return lots;
    }

    public void setLots(String lots) {
        this.lots = lots;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getPlanDate() {
        return planDate;
    }

    public void setPlanDate(String planDate) {
        this.planDate = planDate;
    }
}
