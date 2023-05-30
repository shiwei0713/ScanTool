package com.hz.scantool.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hz.scantool.R;

public class MyAlertDialog extends AlertDialog {

    private TextView txtTitle,txtMessage;
    private Button btnCancel,btnOk;
    private String strTitle,strMessage,strBtnOkTitle,strBtnCancelTitle;
    private onCancelOnClickListener onCancelOnClickListener;
    private onOkOnClickListener onOkOnClickListener;
    private static ConfirmResult confirmResult;

    public MyAlertDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_alert_my);

        //点击空白处取消关闭窗口
        setCanceledOnTouchOutside(false);

        //初始化
        initView();
        initData();
        initEvent();
    }

    public void setCancelOnClickListener(String strTitle,onCancelOnClickListener onCancelOnClickListener){
        this.strBtnCancelTitle = strTitle;
        this.onCancelOnClickListener = onCancelOnClickListener;
    }

    public void setOnOkOnClickListener(String strTitle,onOkOnClickListener onOkOnClickListener){
        this.strBtnOkTitle = strTitle;
        this.onOkOnClickListener = onOkOnClickListener;
    }

    private void initView(){
        txtTitle = findViewById(R.id.txtTitle);
        txtMessage = findViewById(R.id.txtMessage);
        btnCancel = findViewById(R.id.btnCancel);
        btnOk = findViewById(R.id.btnOk);
    }

    private void initData(){
        if(strTitle!=null){
            txtTitle.setText(strTitle);
        }

        if(strMessage!=null){
            txtMessage.setText(strMessage);
        }

        btnOk.setText(strBtnOkTitle);
        btnCancel.setText(strBtnCancelTitle);
    }

    private void initEvent(){
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onOkOnClickListener!=null){
                    onOkOnClickListener.onOkClick();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onCancelOnClickListener!=null){
                    onCancelOnClickListener.onCancelClick();
                }
            }
        });
    }

    public void setTitle(String title){
        this.strTitle = title;
    }

    public void setMessage(String message){
        this.strMessage = message;
    }

    public void setConfirmResult(ConfirmResult confirmResult){
        this.confirmResult = confirmResult;
    }

    public interface onCancelOnClickListener{
        void onCancelClick();
    }

    public interface onOkOnClickListener{
        void onOkClick();
    }

    public static void myShowAlertDialog(Context context,String title, String msg){
        MyAlertDialog myAlertDialog = new MyAlertDialog(context);
        myAlertDialog.setTitle(title);
        myAlertDialog.setMessage(msg);
        myAlertDialog.setOnOkOnClickListener("确定", new onOkOnClickListener() {
            @Override
            public void onOkClick() {
                myAlertDialog.dismiss();
            }
        });
        myAlertDialog.setCancelOnClickListener("取消", new onCancelOnClickListener() {
            @Override
            public void onCancelClick() {
                myAlertDialog.dismiss();
            }
        });

        myAlertDialog.show();
    }

    public static void myShowConfirmDialog(Context context,String title, String msg,String strAction,String strActionId,String qcstatus,String qrcode){
        MyAlertDialog myConfirmDialog = new MyAlertDialog(context);
        myConfirmDialog.setTitle(title);
        myConfirmDialog.setMessage(msg);
        myConfirmDialog.setOnOkOnClickListener("确定", new onOkOnClickListener() {
            @Override
            public void onOkClick() {
                confirmResult.OnOkConfirm(strAction,strActionId,qcstatus,qrcode);
                myConfirmDialog.dismiss();
            }
        });
        myConfirmDialog.setCancelOnClickListener("取消", new onCancelOnClickListener() {
            @Override
            public void onCancelClick() {
                confirmResult.OnCancelConfirm();
                myConfirmDialog.dismiss();
            }
        });

        myConfirmDialog.show();
    }

    public static abstract class ConfirmResult{
        public abstract void OnOkConfirm(String strAction,String strActionId,String qcstatus,String qrcode);
        public abstract void OnCancelConfirm();
    }
}
