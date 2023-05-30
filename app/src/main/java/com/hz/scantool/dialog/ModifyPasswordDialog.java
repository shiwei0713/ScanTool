package com.hz.scantool.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hz.scantool.R;

public class ModifyPasswordDialog extends Dialog {

    private TextView txtTitle,txtMessage;
    private EditText oldPassword,newPassword,confirmPassword;
    private Button btnCancel,btnOk;
    private String strTitle,strMessage,strBtnOkTitle,strBtnCancelTitle,strPassword,strUser;
    private onCancelOnClickListener onCancelOnClickListener;
    private onOkOnClickListener onOkOnClickListener;

    public ModifyPasswordDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_password_modify);

        //点击空白处取消关闭窗口
        setCanceledOnTouchOutside(false);

        //初始化
        initView();
        initData();
        initEvent();
    }

    public void setCancelOnClickListener(String strTitle, onCancelOnClickListener onCancelOnClickListener){
        this.strBtnCancelTitle = strTitle;
        this.onCancelOnClickListener = onCancelOnClickListener;
    }

    public void setOnOkOnClickListener(String strTitle, onOkOnClickListener onOkOnClickListener){
        this.strBtnOkTitle = strTitle;
        this.onOkOnClickListener = onOkOnClickListener;
    }

    private void initView(){
        txtTitle = findViewById(R.id.txtTitle);
        txtMessage = findViewById(R.id.txtMessage);
        oldPassword = findViewById(R.id.oldPassword);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
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

        oldPassword.setText(strPassword);
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

    public void setPassword(String password){
        this.strPassword = password;
    }

    public void setUser(String user){
        this.strUser = user;
    }

    public String getUserPassword(){
        return newPassword.getText().toString();
    }

    public void setMessage(String message){
        this.strMessage = message;
    }

    public interface onCancelOnClickListener{
        void onCancelClick();
    }

    public interface onOkOnClickListener{
        void onOkClick();
    }
}
