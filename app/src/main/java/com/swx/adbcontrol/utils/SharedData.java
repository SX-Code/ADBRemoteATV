package com.swx.adbcontrol.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.swx.adbcontrol.MainApplication;

import java.util.Map;

/**
 * @Author sxcode
 * @Date 2024/5/17 13:41
 * <a href="https://blog.csdn.net/qq_29293605/article/details/121416784">...</a>
 */
public class SharedData {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private final static String SHARE_FILE_NAME = "setting_adb_control_app";

    private static SharedData sharedData;

    public static SharedData getInstance() {
        if (sharedData == null) {
            sharedData = new SharedData(MainApplication.getContext());
        }
        return sharedData;
    }

    private SharedData(Context context) {
        sp = context.getSharedPreferences(SHARE_FILE_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    private SharedData(Context context, String fileName, int mode) {
        sp = context.getSharedPreferences(fileName, mode);
        editor = sp.edit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    // long and float, ignore

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public Map<String, ?> getAll() {
        return sp.getAll();
    }

    public SharedData put(String key, Object value) {
        if (value == null) return this;
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        }else if (value instanceof String) {
            editor.putString(key, (String) value);
        }else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        }else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        }else {
            editor.putString(key, value.toString());
        }
        return this;
    }

    public SharedData remove(String key) {
        editor.remove(key);
        return this;
    }

    public SharedData clear() {
        editor.clear();
        return this;
    }

    public boolean contains(String key) {
        return sp.contains(key);
    }

    public boolean commit() {
        return editor.commit();
    }


}
