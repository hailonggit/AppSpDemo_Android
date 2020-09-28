package com.anji.appsp.sdktest;

import android.app.Application;

import com.anji.appsp.sdk.AppSpConfig;

public class AppContext extends Application {
    private static AppContext mInstance;
    public static final String appKey = "674c0872fc4e4dd3be383b2dc3c37417";

    public static AppContext getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppSpConfig.getInstance().init(this, appKey);
        mInstance = this;
    }

}
