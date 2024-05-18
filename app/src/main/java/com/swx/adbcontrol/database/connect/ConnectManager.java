package com.swx.adbcontrol.database.connect;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.swx.adbcontrol.entity.ConnectInstance;

import java.util.Collections;
import java.util.List;

/**
 * @Author sxcode
 * @Date 2024/5/15 22:17
 */
public class ConnectManager {
    private final SQLiteOpenHelper dbHelper;

    public ConnectManager(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean isExist(String ip, Integer port) {
        if (TextUtils.isEmpty(ip) || port == null) return false;
        return ConnectContact.isExist(dbHelper, ip, port);
    }

    public List<ConnectInstance> list() {
        return ConnectContact.query(dbHelper);
    }

    public ConnectInstance insert(ConnectInstance connect) {
        if (connect == null) return null;
        Integer insertId = ConnectContact.insert(dbHelper, connect);
        connect.setId(insertId);
        return connect;
    }

    public void update(ConnectInstance connect) {
        if (connect == null || connect.getId() == null) return;
        ConnectContact.update(dbHelper, connect);
    }

    public void delete(Integer connectId) {
        if (connectId == null) return;
        ConnectContact.delete(dbHelper, connectId);
    }

    public void delete(Integer[] connectIds) {
        if (connectIds == null || connectIds.length == 0) return;
        ConnectContact.delete(dbHelper, connectIds);
    }

    public void clear() {
        ConnectContact.clear(dbHelper);
    }
}

