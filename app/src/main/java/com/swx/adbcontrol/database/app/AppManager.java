package com.swx.adbcontrol.database.app;

import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.swx.adbcontrol.entity.AppItem;

import java.util.List;

/**
 * @Author sxcode
 * @Date 2024/5/15 22:17
 */
public class AppManager {
    private final SQLiteOpenHelper dbHelper;

    public AppManager(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean isExist(String url) {
        if (TextUtils.isEmpty(url)) return false;
        return AppContact.isExist(dbHelper, url);
    }

    public List<AppItem> list() {
        return AppContact.query(dbHelper);
    }

    public AppItem insert(AppItem app) {
        if (app == null) return null;
        Integer insertId = AppContact.insert(dbHelper, app);
        app.setId(insertId);
        return app;
    }

    public void update(AppItem app) {
        if (app == null || app.getId() == null) return;
        AppContact.update(dbHelper, app);
    }

    public void batchUpdateOrder(List<AppItem> apps) {
        if (apps.isEmpty()) return;
        AppContact.batchUpdateOrder(dbHelper, apps);
    }

    public void delete(Integer appId) {
        if (appId == null) return;
        AppContact.delete(dbHelper, appId);
    }


    public void clear() {
        AppContact.clear(dbHelper);
    }
}

