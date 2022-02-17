package com.hz.scantool.printer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import static com.hz.scantool.printer.Constant.MESSAGE_UPDATE_PARAMETER;

public class WifiParameterConfig {
    private Context mContext;
    private Handler mHandler;
    private String strIp;
    private String strPort;

    public WifiParameterConfig(Context mContext, Handler handler, String strIp, String strPort){
        this.mContext = mContext;
        this.mHandler=handler;
        this.strIp = strIp;
        this.strPort = strPort;
    }

    public void WifiConnect(){
        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Message message = Message.obtain();
                        message.what = MESSAGE_UPDATE_PARAMETER;
                        Bundle bundle = new Bundle();
                        bundle.putString("Ip", strIp);
                        bundle.putString("Port", strPort);
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
        alertDialog.setCanceledOnTouchOutside(false);
    }
}
