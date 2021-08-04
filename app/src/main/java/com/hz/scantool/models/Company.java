package com.hz.scantool.models;

public class Company {

    private Integer id;
    private String code;
    private String site;

    public void setSite(String site){
        this.site = site;
    }

    public Integer getId() {
        switch (site){
            case "武汉华滋东江汽车零部件有限公司":
                id=0;
                break;
            case "成都华滋东江汽车零部件有限公司":
                id=1;
                break;
            case "湘潭华滋东江汽车零部件有限公司":
                id=2;
                break;
            default:
                id=0;
        }
        return id;
    }

    public String getCode() {
        switch (site){
            case "武汉华滋东江汽车零部件有限公司":
                code="HZ10";
                break;
            case "成都华滋东江汽车零部件有限公司":
                code="HZ20";
                break;
            case "湘潭华滋东江汽车零部件有限公司":
                code="HZ50";
                break;
            default:
                code="";
        }

        return code;
    }
}
