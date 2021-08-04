package com.hz.scantool.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hz.scantool.R;

public class MyToast extends Toast{
    private static Toast myToast;

    public MyToast(Context context) {
        super(context);
    }

    public static void myShow(Context context,String message,int type){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.customize_toast,null);

        LinearLayout linearToast = view.findViewById(R.id.linearToast);
        TextView txtToastText = view.findViewById(R.id.txtToastText);
        ImageView imageToastIcon = view.findViewById(R.id.imageToastIcon);
        linearToast.setBackground(context.getResources().getDrawable(R.drawable.toast_style));
        txtToastText.setText(message);

        if(type == 1){
            imageToastIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.success));
            linearToast.setBackground(context.getResources().getDrawable(R.drawable.toast_success_style));
        }else{
            if(type == 0){
                imageToastIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.error));
            }else{
                imageToastIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.warn));
            }
        }


        myToast = new Toast(context);
        myToast.setView(view);
        myToast.setDuration(Toast.LENGTH_SHORT);
//        myToast.setGravity(Gravity.CENTER,0,0);
        myToast.show();
    }
}
