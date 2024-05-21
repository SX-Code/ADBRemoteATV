package com.swx.adbremote;

import android.app.Application;
import android.content.Context;

/**
 * @Author sxcode
 * @Date 2024/5/17 13:57
 */
public class MainApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
