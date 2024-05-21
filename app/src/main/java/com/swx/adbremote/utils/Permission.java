package com.swx.adbremote.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

/**
 * @Author sxcode
 * @Date 2024/5/19 14:26
 */
public class Permission {
    public static final int REQUEST_CODE = 100;
    private final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET
    };
    private final ArrayList<String> permissionList = new ArrayList<>();

    public interface PermissionCallback {
        void allGrant();
    }

    public void checkPermissions(Activity activity, PermissionCallback callback) {
        //6.0才用动态权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission);
                }
            }
            if (permissionList.isEmpty()) {
                callback.allGrant();
            } else {
                requestPermission(activity);
            }
        }
    }

    public void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[0]), REQUEST_CODE);
    }
}
