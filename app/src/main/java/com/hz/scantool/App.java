package com.hz.scantool;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.Bugly;

public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = getApplicationContext();
        Bugly.init(this,"2163fd28ed",false);
    }

    public static Context getContext() {
        return mContext;
    }
}
