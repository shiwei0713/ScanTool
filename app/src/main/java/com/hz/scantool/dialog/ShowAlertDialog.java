package com.hz.scantool.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.hz.scantool.R;

public class ShowAlertDialog {

    public static void myShow(Context context,String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setTitle("提示");
        builder.setIcon(R.drawable.dialog_error);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }
}
