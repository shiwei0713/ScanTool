package com.hz.scantool.adapter;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hz.scantool.R;

public class LoadingDialog extends Dialog {

    private String mMessage;  //加载文字
    private int mImageId;   //图片ID
    private boolean mCancelable;
    private RotateAnimation mRotateAnimation;

    public LoadingDialog(@NonNull Context context,String message,int imageId) {
        this(context, R.style.LoadingDialog,message,imageId,false);
    }

    public LoadingDialog(@NonNull Context context,int themeResId,String message,int imageId,boolean cancelable){
        super(context,themeResId);

        mMessage=message;
        mImageId=imageId;
        mCancelable=cancelable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_loading);
        //设置窗口大小
        WindowManager windowManager = getWindow().getWindowManager();
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        WindowManager.LayoutParams attributes = getWindow().getAttributes();

        //设置窗口背景透明度
        attributes.alpha=0.3f;
        //设置窗口宽高为屏幕的1/3
        attributes.width = screenWidth / 3;
        attributes.height = attributes.width;
        getWindow().setAttributes(attributes);
        setCancelable(mCancelable);
        TextView txtLoading = findViewById(R.id.txtLoading);
        ImageView ivLoading = findViewById(R.id.ivLoading);
        txtLoading.setText(mMessage);
        ivLoading.setImageResource(mImageId);
        ivLoading.measure(0,0);

        //设置选择动画
        mRotateAnimation = new RotateAnimation(0,360,ivLoading.getMeasuredWidth()/2,ivLoading.getMeasuredHeight()/2);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setDuration(1000);
        mRotateAnimation.setRepeatCount(-1);
        ivLoading.startAnimation(mRotateAnimation);
    }

    @Override
    public void dismiss() {
        mRotateAnimation.cancel();
        super.dismiss();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            return mCancelable;
        }else if(keyCode==KeyEvent.KEYCODE_ENTER){
            return mCancelable;
        }

        return super.onKeyDown(keyCode, event);
    }
}
