package com.swx.adbremote.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.swx.adbremote.R;
import com.swx.adbremote.activity.settings.SettingApplicationsActivity;
import com.swx.adbremote.activity.settings.SettingBehaviorActivity;
import com.swx.adbremote.activity.settings.SettingLayoutActivity;
import com.swx.adbremote.components.QuestionDialog;
import com.swx.adbremote.utils.AutoUpdater;
import com.swx.adbremote.utils.Constant;
import com.swx.adbremote.utils.Permission;
import com.swx.adbremote.utils.ShareUtil;
import com.swx.adbremote.utils.ToastUtil;

/**
 * @Author sxcode
 * @Date 2024/5/16 21:30
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

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
        findViewById(R.id.layout_setting_fallback).setOnClickListener(this);
        findViewById(R.id.layout_setting_github).setOnClickListener(this);
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
        } else if (id == R.id.layout_setting_fallback) {
            openUrl(Constant.URL_GITHUB_FEEDBACK_PAGE);
        } else if (id == R.id.layout_setting_github) {
            openUrl(Constant.URL_GITHUB_CODE_PAGE);
        }
    }

    private void openUrl(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void updateApp() {
        try {
            Permission permission = new Permission();
            permission.checkPermissions(this, () -> {
                // 说明权限都已经通过，可以更新, 自动更新
                AutoUpdater manager = new AutoUpdater(SettingActivity.this);
                manager.checkUpdate();
            });
        } catch (Exception e) {
            ToastUtil.showShort(SettingActivity.this.getString(R.string.text_update_failed));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermission = false;
        if (Permission.REQUEST_CODE == requestCode) {
            for (int grantResult : grantResults) {
                if (grantResult == -1) {
                    hasPermission = true;
                    break;
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
                    build(this, this.getString(R.string.text_prompt_message),
                            this.getString(R.string.text_permission_msg))
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
