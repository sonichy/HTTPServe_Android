package com.hty.httpserve;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {
    static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    static Context getContext() {
        return mContext;
    }

}