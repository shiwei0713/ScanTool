package com.hz.scantool.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hz.scantool.R;
import com.hz.scantool.SetProcessActivity;

public class ConnectProcessSetDialog extends Dialog {

    private TextView txtTitle;
    private TextView txtEmployee,setProcessEmployee,setProcessDevices;
    private Button btnCancel,btnOk;
    private String strTitle,strBtnOkTitle,strBtnCancelTitle;
    private onCancelOnClickListener onCancelOnClickListener;
    private onOkOnClickListener onOkOnClickListener;
    private Context mContext;

    public ConnectProcessSetDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_connect_process_set);

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
        txtEmployee = findViewById(R.id.txtEmployee);
        setProcessEmployee = findViewById(R.id.setProcessEmployee);
        setProcessDevices = findViewById(R.id.setProcessDevices);
        btnCancel = findViewById(R.id.btnCancel);
        btnOk = findViewById(R.id.btnOk);

        //绑定事件
        setProcessEmployee.setOnClickListener(new btnClickListener());
        setProcessDevices.setOnClickListener(new btnClickListener());

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
                case R.id.setProcessEmployee:

                    break;
                case R.id.setProcessDevices:
                    break;
            }
        }
    }

    /**
    *描述: 输入并显示
    *日期：2023/4/14
    **/
    private void showEmployee(String emp){
        String sEmployee = txtEmployee.getText().toString();
        if(sEmployee.equals("")||sEmployee.isEmpty()){
            txtEmployee.setText(emp);
        }else{
            String sInputEmployee = sEmployee.trim()+"/"+emp.trim();
            txtEmployee.setText(sInputEmployee);
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

    public TextView getEmployee() {
        return txtEmployee;
    }

    public interface onCancelOnClickListener{
        void onCancelClick();
    }

    public interface onOkOnClickListener{
        void onOkClick();
    }
}
