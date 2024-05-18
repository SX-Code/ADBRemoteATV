package com.swx.adbcontrol.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.swx.adbcontrol.R;
import com.swx.adbcontrol.activity.settings.SettingApplicationsActivity;
import com.swx.adbcontrol.activity.settings.SettingBehaviorActivity;
import com.swx.adbcontrol.activity.settings.SettingLayoutActivity;
import com.swx.adbcontrol.components.QuestionDialog;
import com.swx.adbcontrol.utils.AutoUpdater;
import com.swx.adbcontrol.utils.ShareUtil;
import com.swx.adbcontrol.utils.ToastUtil;

import java.util.ArrayList;

/**
 * @Author sxcode
 * @Date 2024/5/16 21:30
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 100;
    public QuestionDialog permissionDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_activity_setting), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }

    private void init() {
        findViewById(R.id.btn_setting_back).setOnClickListener(this);
        findViewById(R.id.layout_setting_layout).setOnClickListener(this);
        findViewById(R.id.layout_setting_behavior).setOnClickListener(this);
        findViewById(R.id.layout_setting_mgr_apps).setOnClickListener(this);
        findViewById(R.id.layout_setting_share).setOnClickListener(this);
        findViewById(R.id.layout_setting_update).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_setting_back) {
            finish();
        } else if (id == R.id.layout_setting_layout) {
            Intent intent = new Intent(this, SettingLayoutActivity.class);
            startActivity(intent);
        } else if (id == R.id.layout_setting_behavior) {
            Intent intent = new Intent(this, SettingBehaviorActivity.class);
            startActivity(intent);
        } else if (id == R.id.layout_setting_mgr_apps) {
            Intent intent = new Intent(this, SettingApplicationsActivity.class);
            startActivity(intent);
        } else if (id == R.id.layout_setting_share) {
            ShareUtil.shareApk(this);
        } else if (id == R.id.layout_setting_update) {
            updateApp();
        }
    }

    private void openGithubCode() {

    }

    private void updateApp() {
        try {
            //6.0才用动态权限
            if (Build.VERSION.SDK_INT >= 23) {
                String[] permissions = {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.INTERNET
                };
                ArrayList<String> permissionList = new ArrayList<>();
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionList.add(permission);
                    }
                }
                if (permissionList.isEmpty()) {
                    //说明权限都已经通过，可以更新
                    //自动更新
                    AutoUpdater manager = new AutoUpdater(SettingActivity.this);
                    manager.checkUpdate();
                } else {
                    ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
                }
            }
        } catch (Exception e) {
            ToastUtil.showShort("更新失败");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermission = false;
        if (REQUEST_CODE == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermission = true;
                }
            }
            if (hasPermission) {
                // 跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
                permissionDialog();
            } else {
                // 全部权限通过，可以进行下一步操作
                AutoUpdater manager = new AutoUpdater(SettingActivity.this);
                manager.checkUpdate();
            }
        }
    }

    private void permissionDialog() {
        if (permissionDialog == null) {
            permissionDialog = QuestionDialog.
                    build(this, "提示信息",
                            "当前应用缺少必要权限，该功能暂时无法使用。如若需要，请单击【确定】按钮前往设置中心进行权限授权。")
                    .setOnButtonClickListener(new QuestionDialog.OnButtonClickListener() {
                        @Override
                        public void onPositiveClick(View view) {
                            cancelPermissionDialog();
                            Uri packageURI = Uri.parse("package:" + getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }

                        @Override
                        public void onNegativeClick(View view) {
                            cancelPermissionDialog();
                        }
                    });

        }
        permissionDialog.show();
    }

    private void cancelPermissionDialog() {
        permissionDialog.cancel();
    }
}
