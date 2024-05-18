package com.swx.adbcontrol.database.connect;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.swx.adbcontrol.entity.ConnectInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @Author sxcode
 * @Date 2024/5/15 21:37
 */
public class ConnectContact {
    private static final String TAG = "ConnectContact";
    public static final String TABLE_NAME = "connect";
    private static final String[] AVAILABLE_PROJECTION = new String[]{
            ConnectColumns._ID,
            ConnectColumns.IP,
            ConnectColumns.ALIAS,
            ConnectColumns.PORT
    };

    public static final String CREATE_TABLE =
            String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s INTEGER)",
                    TABLE_NAME, ConnectColumns._ID, ConnectColumns.IP, ConnectColumns.ALIAS, ConnectColumns.PORT);

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static void checkColumn(String[] projection) throws IllegalAccessException {
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<>(Arrays.asList(AVAILABLE_PROJECTION));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalAccessException(TAG + "checkColumns() -> Unknown columns in projection");
            }
        }
    }

    public static boolean isExist(SQLiteOpenHelper helper, String ip, Integer port) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String selection = String.format("%s =? AND %s =? ", ConnectColumns.IP, ConnectColumns.PORT);
        String[] selectionArgs = {ip, String.valueOf(port)};
        Cursor cursor = db.query(TABLE_NAME, AVAILABLE_PROJECTION, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public static List<ConnectInstance> query(SQLiteOpenHelper helper) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, AVAILABLE_PROJECTION, null, null, null, null, null, null);

        ArrayList<ConnectInstance> connects = new ArrayList<>();
        if (cursor == null) return null;
        while (cursor.moveToNext()) {
            connects.add(getConnectFromCursor(cursor));
        }
        cursor.close();
        return connects;
    }

    public static void update(SQLiteOpenHelper helper, ConnectInstance connect) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.update(TABLE_NAME, toContentValues(connect), ConnectColumns._ID + " =? ", new String[]{String.valueOf(connect.getId())});
    }

    public static Integer insert(SQLiteOpenHelper helper, ConnectInstance connect) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.insert(TABLE_NAME, null, toContentValues(connect));
        return getLastInsertRowId(db);
    }

    private static Integer getLastInsertRowId(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT last_insert_rowid() from " + TABLE_NAME, null);
        Integer id = null;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    public static void insertOrReplace(SQLiteOpenHelper helper, ConnectInstance connect) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.insertWithOnConflict(TABLE_NAME, null, toContentValues(connect), SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void delete(SQLiteOpenHelper helper, Integer connectId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_NAME, ConnectColumns._ID + " =? ", new String[]{String.valueOf(connectId)});
    }

    public static void delete(SQLiteOpenHelper helper, Integer[] connectIds) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String ids = TextUtils.join(", ", connectIds);
        String deleteQuery = "DELETE FROM " + TABLE_NAME + " WHERE " + ConnectColumns._ID + " IN (" + ids + ")";
        db.execSQL(deleteQuery);
    }

    public static void clear(SQLiteOpenHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    private static ContentValues toContentValues(ConnectInstance connect) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConnectColumns.IP, connect.getIp());
        contentValues.put(ConnectColumns.ALIAS, connect.getAlias());
        contentValues.put(ConnectColumns.PORT, connect.getPort());
        return contentValues;
    }

    private static ConnectInstance getConnectFromCursor(Cursor cursor) {
        ConnectInstance connect = new ConnectInstance();
        connect.setId(cursor.getInt(0));
        connect.setIp(cursor.getString(cursor.getColumnIndexOrThrow(ConnectColumns.IP)));
        connect.setAlias(cursor.getString(cursor.getColumnIndexOrThrow(ConnectColumns.ALIAS)));
        connect.setPort(cursor.getInt(cursor.getColumnIndexOrThrow(ConnectColumns.PORT)));
        return connect;
    }
}
