package com.hz.scantool.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

@Entity
public class MergeLabelEntity {
    @PrimaryKey
    @NonNull
    private String qrcode;
    private String productDocno;
    private String planDocno;
    private int planSeq;
    private int version;
    private String productCode;
    private String productName;
    private String processId;
    private String process;
    private String devices;
    private String productUser;
    private String quantity;
    private String planDate;
    private String typeDesc;
    private int moreOrLess;

    public MergeLabelEntity(String qrcode,String productDocno,String planDocno,int planSeq,int version,String productCode,String productName,String processId,String process,String devices,String productUser,String quantity,String planDate,String typeDesc,int moreOrLess){
        this.qrcode = qrcode;
        this.productDocno = productDocno;
        this.planDocno = planDocno;
        this.planSeq = planSeq;
        this.version = version;
        this.productCode = productCode;
        this.productName = productName;
        this.processId = processId;
        this.process = processId;
        this.devices = devices;
        this.productUser = productUser;
        this.quantity = quantity;
        this.planDate = planDate;
        this.typeDesc = typeDesc;
        this.moreOrLess = moreOrLess;
    }

    @NonNull
    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(@NonNull String qrcode) {
        this.qrcode = qrcode;
    }

    public String getProductDocno() {
        return productDocno;
    }

    public void setProductDocno(String productDocno) {
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPlanDate() {
        return planDate;
    }

    public void setPlanDate(String planDate) {
        this.planDate = planDate;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    public int getMoreOrLess() {
        return moreOrLess;
    }

    public void setMoreOrLess(int moreOrLess) {
        this.moreOrLess = moreOrLess;
    }

    public String getDiffQrcode(){
        if(this.quantity.equals("")||this.quantity.isEmpty()){
            this.quantity = "0";
        }
        int iQuantity = Integer.parseInt(this.quantity);
        iQuantity = iQuantity * moreOrLess;

        return this.qrcode+":"+String.valueOf(iQuantity);
    }

    public Map<String, Object> getMergeLabelListData(){
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("Qrcode", qrcode);
        map.put("ProductName", productName);
        map.put("Process", process);
        map.put("Employee", productUser);
        map.put("Quantity", quantity);
        map.put("Device", devices);
        map.put("PlanDate", planDate);
        map.put("TypeDesc",typeDesc);
        map.put("MoreOrLess",moreOrLess);

        return map;
    }
}
