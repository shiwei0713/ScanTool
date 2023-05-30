package com.hz.scantool.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hz.scantool.R;

public class InputDialog extends Dialog {

    private TextView txtTitle;
    private TextView etQuantity;
    private Button btnCancel,btnOk;
    private Button btnNum1,btnNum2,btnNum3,btnNum4,btnNum5,btnNum6,btnNum7,btnNum8,btnNum9,btnNum0,btnDel;
    private String strTitle,strBtnOkTitle,strBtnCancelTitle;
    private onCancelOnClickListener onCancelOnClickListener;
    private onOkOnClickListener onOkOnClickListener;

    public InputDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input);

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
        etQuantity = findViewById(R.id.etQuantity);
        btnCancel = findViewById(R.id.btnCancel);
        btnOk = findViewById(R.id.btnOk);

        btnNum1 = findViewById(R.id.btnNum1);
        btnNum2 = findViewById(R.id.btnNum2);
        btnNum3 = findViewById(R.id.btnNum3);
        btnNum4 = findViewById(R.id.btnNum4);
        btnNum5 = findViewById(R.id.btnNum5);
        btnNum6 = findViewById(R.id.btnNum6);
        btnNum7 = findViewById(R.id.btnNum7);
        btnNum8 = findViewById(R.id.btnNum8);
        btnNum9 = findViewById(R.id.btnNum9);
        btnNum0 = findViewById(R.id.btnNum0);
        btnDel = findViewById(R.id.btnDel);

        //绑定事件
        btnNum1.setOnClickListener(new btnClickListener());
        btnNum2.setOnClickListener(new btnClickListener());
        btnNum3.setOnClickListener(new btnClickListener());
        btnNum4.setOnClickListener(new btnClickListener());
        btnNum5.setOnClickListener(new btnClickListener());
        btnNum6.setOnClickListener(new btnClickListener());
        btnNum7.setOnClickListener(new btnClickListener());
        btnNum8.setOnClickListener(new btnClickListener());
        btnNum9.setOnClickListener(new btnClickListener());
        btnNum0.setOnClickListener(new btnClickListener());
        btnDel.setOnClickListener(new btnClickListener());
    }

    private void initData(){
        if(strTitle!=null){
            txtTitle.setText(strTitle);
        }

        btnOk.setText(strBtnOkTitle);
        btnCancel.setText(strBtnCancelTitle);
    }

    /**
    *描述: 按钮事件实现
    *日期：2023/4/14
    **/
    private class btnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnNum1:
                    showNum(btnNum1.getText().toString());
                    break;
                case R.id.btnNum2:
                    showNum(btnNum2.getText().toString());
                    break;
                case R.id.btnNum3:
                    showNum(btnNum3.getText().toString());
                    break;
                case R.id.btnNum4:
                    showNum(btnNum4.getText().toString());
                    break;
                case R.id.btnNum5:
                    showNum(btnNum5.getText().toString());
                    break;
                case R.id.btnNum6:
                    showNum(btnNum6.getText().toString());
                    break;
                case R.id.btnNum7:
                    showNum(btnNum7.getText().toString());
                    break;
                case R.id.btnNum8:
                    showNum(btnNum8.getText().toString());
                    break;
                case R.id.btnNum9:
                    showNum(btnNum9.getText().toString());
                    break;
                case R.id.btnNum0:
                    showNum(btnNum0.getText().toString());
                    break;
                case R.id.btnDel:
                    etQuantity.setText("");
                    break;
            }
        }
    }

    /**
    *描述: 输入并显示
    *日期：2023/4/14
    **/
    private void showNum(String num){
        String sQuantity = etQuantity.getText().toString();
        if(sQuantity.equals("")||sQuantity.isEmpty()){
            if(num.equals("0")){
                etQuantity.setText("");
            }else{
                etQuantity.setText(num);
            }
        }else{
            String sInputNum = sQuantity.trim()+num.trim();
            etQuantity.setText(sInputNum);
        }
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

    public TextView getEtQuantity() {
        return etQuantity;
    }

    public interface onCancelOnClickListener{
        void onCancelClick();
    }

    public interface onOkOnClickListener{
        void onOkClick();
    }
}
