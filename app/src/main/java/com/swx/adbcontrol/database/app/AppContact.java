package com.swx.adbcontrol.database.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.swx.adbcontrol.entity.AppItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @Author sxcode
 * @Date 2024/5/15 21:37
 */
public class AppContact {
    private static final String TAG = "AppContact";
    public static final String TABLE_NAME = "app";
    private static final String[] AVAILABLE_PROJECTION = new String[]{
            AppColumns._ID,
            AppColumns.NAME,
            AppColumns.ICON,
            AppColumns.URL,
            AppColumns.PRIORITY
    };

    public static final String CREATE_TABLE =
            String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT, %s, INTEGER)",
                    TABLE_NAME, AppColumns._ID, AppColumns.NAME, AppColumns.ICON, AppColumns.URL, AppColumns.PRIORITY);

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

    public static boolean isExist(SQLiteOpenHelper helper, String url) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, AVAILABLE_PROJECTION, AppColumns.URL + " =? ", new String[]{url}, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    public static List<AppItem> query(SQLiteOpenHelper helper) {
        ArrayList<AppItem> connects = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            cursor = db.query(TABLE_NAME, AVAILABLE_PROJECTION, null, null, null, null, null, null);
            if (cursor == null) return null;
            while (cursor.moveToNext()) {
                connects.add(getAppFromCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "query: e", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return connects;
    }

    public static void update(SQLiteOpenHelper helper, AppItem app) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.update(TABLE_NAME, toAppValues(app), AppColumns._ID + " =? ", new String[]{String.valueOf(app.getId())});
    }

    public static void batchUpdateOrder(SQLiteOpenHelper helper, List<AppItem> apps) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        String sql = "UPDATE " + TABLE_NAME + " SET " + AppColumns.PRIORITY + " = %s WHERE " + AppColumns._ID + " = %s";
        try {
            for (AppItem app : apps) {
                db.execSQL(String.format(sql, app.getPriority(), app.getId()));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "batchUpdateOrder: ", e);
        } finally {
            db.endTransaction();
        }
    }

    public static Integer insert(SQLiteOpenHelper helper, AppItem app) {
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.insert(TABLE_NAME, null, toAppValues(app));
            return getLastInsertRowId(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    public static void insertOrReplace(SQLiteOpenHelper helper, AppItem app) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.insertWithOnConflict(TABLE_NAME, null, toAppValues(app), SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static void delete(SQLiteOpenHelper helper, Integer appId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_NAME, AppColumns._ID + " =? ", new String[]{String.valueOf(appId)});
    }

    public static void clear(SQLiteOpenHelper helper) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    private static ContentValues toAppValues(AppItem app) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppColumns.NAME, app.getName());
        contentValues.put(AppColumns.ICON, app.getIcon());
        contentValues.put(AppColumns.URL, app.getUrl());
        contentValues.put(AppColumns.PRIORITY, app.getPriority());
        return contentValues;
    }

    private static AppItem getAppFromCursor(Cursor cursor) {
        AppItem app = new AppItem();
        app.setId(cursor.getInt(0));
        app.setName(cursor.getString(cursor.getColumnIndexOrThrow(AppColumns.NAME)));
        app.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(AppColumns.ICON)));
        app.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(AppColumns.URL)));
        app.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(AppColumns.PRIORITY)));
        return app;
    }
}
