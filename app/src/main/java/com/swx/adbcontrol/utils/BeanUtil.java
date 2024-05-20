package com.swx.adbcontrol.utils;

import android.util.Log;

import com.swx.adbcontrol.entity.AppItem;
import com.swx.adbcontrol.entity.UpdateInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author sxcode
 * @Date 2024/5/18 1:27
 */
public class BeanUtil {
    private static final String TAG = "JsonTools";

    /**
     * 原生的JSON解析方式，数组包含多个对象
     *
     * @param jsonString JSON字符串
     * @return APP list
     */
    public static ArrayList<AppItem> getAppsByJsonArray(String jsonString) {
        ArrayList<AppItem> apps = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                AppItem app = new AppItem((String) jsonObject.get("icon"), (String) jsonObject.get("name"), (String) jsonObject.get("url"));
                apps.add(app);
            }
        } catch (Exception e) {
            Log.e(TAG, "getAppsByJsonArray: e", e);
        }
        return apps;
    }

    public static UpdateInfo getUpdateInfoByJsonArray(String jsonString) {
        if (jsonString.isEmpty()) return null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return new UpdateInfo((String) jsonObject.get("info"), (String) jsonObject.get("url"));
        } catch (JSONException e) {
            Log.e(TAG, "getUpdateInfoByJsonArray: e", e);
        }
        return null;
    }

    /**
     * Object转List的正确使用方式
     *
     * @param obj   obj (list)
     * @param clazz list的子元素的class
     * @param <T>   泛型
     */
    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }
}
