package com.swx.adbcontrol.utils;

import android.app.Activity;
import android.content.Intent;

import com.swx.adbcontrol.R;

/**
 * @Author sxcode
 * @Date 2024/5/18 23:09
 */
public class ShareUtil {

    public static void shareApk(Activity activity) {
        AutoUpdater.getLatestApkUrl(new HttpUtil.HttpCallback() {
            @Override
            public void success(String content) {
                shareText(activity, "", content);
            }

            @Override
            public void fail(String msg) {
                ToastUtil.showToastThread("分享功能暂时无法使用");
            }
        });
    }

    public static void shareText(Activity activity, String title, String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        if (title.isEmpty()) {
            title = activity.getString(R.string.share_app_to);
        }
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent, title));
    }
}
