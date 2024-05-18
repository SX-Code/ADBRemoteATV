package com.swx.adbcontrol.database;

import android.content.Context;

import com.swx.adbcontrol.MainApplication;
import com.swx.adbcontrol.database.app.AppManager;
import com.swx.adbcontrol.database.connect.ConnectManager;


public class DBManager {
    private static volatile DBManager dbManager;
    private final ConnectManager connectManager;
    private final AppManager appManager;

    public static DBManager getInstance() {
        synchronized (DBManager.class) {
            if (dbManager == null) {
                dbManager = new DBManager(MainApplication.getContext());
            }
        }
        return dbManager;
    }

    private DBManager(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        connectManager = new ConnectManager(dbHelper);
        appManager = new AppManager(dbHelper);
    }

    public ConnectManager getConnectManager() {
        return this.connectManager;
    }

    public AppManager getAppManager() {
        return this.appManager;
    }
}
