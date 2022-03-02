package com.hz.scantool.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hz.scantool.CheckStockDetailActivity;
import com.hz.scantool.R;
import com.hz.scantool.helper.SharedHelper;
import com.hz.scantool.models.UserInfo;

public class DeptConfigDialog {

    private Context mContext;
    private SharedHelper sharedHelper;
    private Handler mHandler;
    private int choiceItem;

    public DeptConfigDialog(Context mContext,Handler mHandler){
        this.mContext = mContext;
        this.mHandler = mHandler;
    }

    public void show(){
        final String[] items = {"10337_薛峰冲压线边仓","10237_凤二冲压线边仓","10137_凤一冲压线边仓"};
        sharedHelper=new SharedHelper(mContext);

        choiceItem = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("请选择部门");
//        builder.setIcon(R.drawable.dialog_error);
        builder.setSingleChoiceItems(items,-1, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                choiceItem = which;
            }
        });

        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(choiceItem != -1){
                    String strDept = items[choiceItem];
                    sharedHelper.saveDept(strDept);

                    Message message = Message.obtain();
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("dept", strDept);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }else{
                    Toast.makeText(mContext,"未选择任何选项，更新失败！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }

}
