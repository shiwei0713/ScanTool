package com.hz.scantool.models;

public class ListItemInfo {
    private String erpPinMing;
    private String erpGuiGe;
    private String erpGongXu;
    private String erpSheBei;
    private String erpDateTime;
    private String erpQty;

    public ListItemInfo(){

    }

    public ListItemInfo(String erpPinMing,String erpGuiGe,String erpGongXu,String erpSheBei,String erpDateTime,String erpQty){

    }

    public String getErpPinMing() {
        return erpPinMing;
    }

    public String getErpGuiGe() {
        return erpGuiGe;
    }

    public String getErpGongXu() {
        return erpGongXu;
    }

    public String getErpSheBei() {
        return erpSheBei;
    }

    public String getErpDateTime() {
        return erpDateTime;
    }

    public String getErpQty() {
        return erpQty;
    }

    public void setErpPinMing(String erpPinMing) {
        this.erpPinMing = erpPinMing;
    }

    public void setErpGuiGe(String erpGuiGe) {
        this.erpGuiGe = erpGuiGe;
    }

    public void setErpGongXu(String erpGongXu) {
        this.erpGongXu = erpGongXu;
    }

    public void setErpSheBei(String erpSheBei) {
        this.erpSheBei = erpSheBei;
    }

    public void setErpDateTime(String erpDateTime) {
        this.erpDateTime = erpDateTime;
    }

    public void setErpQty(String erpQty) {
        this.erpQty = erpQty;
    }
}
