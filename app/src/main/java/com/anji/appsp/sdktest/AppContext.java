package com.anji.appsp.sdktest;

import android.app.Application;

public class AppContext extends Application {
    private static AppContext mInstance;

    public static AppContext getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

}
