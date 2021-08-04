package com.hz.scantool.printer;

public class PrinterCmd {

    //初始化打印机
    public String setPrinterPos(){
        return new StringBuffer().append((char)27).append((char)64).toString();
    }

    //回车换行
    public String setPrinterEnter(){
        return new StringBuffer().append((char)10).toString();
    }

    //对齐模式
    //0:左对齐 1:中对齐 2:右对齐
    public String setPrinterAlign(int align){
        return new StringBuffer().append((char)27).append((char)97).append((char)align).toString();
    }

    //字体大小
    //0:正常大小 1:两倍高 2:两倍宽 3:两倍大小 4:三倍高 5:三倍宽 6:三倍大小 7:四倍高 8:四倍宽 9:四倍大小 10:五倍高 11:五倍宽 12:五倍大小
    public String setPrinterFontSize(int fontsize){
        String strCmd = "";

        //设置字体大小
        switch (fontsize){
            case -1:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)0).toString();
                break;
            case 0:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)0).toString();
                break;
            case 1:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)1).toString();
                break;
            case 2:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)16).toString();
                break;
            case 3:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)17).toString();
                break;
            case 4:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)2).toString();
                break;
            case 5:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)32).toString();
                break;
            case 6:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)34).toString();
                break;
            case 7:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)3).toString();
                break;
            case 8:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)48).toString();
                break;
            case 9:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)51).toString();
                break;
            case 10:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)4).toString();
                break;
            case 11:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)64).toString();
                break;
            case 12:
                strCmd = new StringBuffer().append((char)29).append((char)33).append((char)68).toString();
                break;
        }

        return strCmd;
    }


}
