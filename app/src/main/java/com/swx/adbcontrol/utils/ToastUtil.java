package com.swx.adbcontrol.utils;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil {
    private static Toast toast;
    private static CharSequence oldMessage;
    private static Application sContext;
    private static Long oneTime = 0L;
    private static Long twoTime = 0L;

    public static void init(Application application) {
        sContext = application;
    }
    // https://blog.csdn.net/liang_duo_yu/article/details/115076048
    public static void showShort(CharSequence sequence) {
        if (toast == null) {
            toast = Toast.makeText(sContext, sequence, Toast.LENGTH_SHORT);
            toast.show();
            oldMessage = sequence;
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (twoTime - oneTime > 2000) {
                // 判断toast上一次显示的时间和这次的显示时间如果大于2000
                toast.setText(sequence);
                toast.show();
                oneTime = twoTime;
                oldMessage = sequence;
            } else {
                // 小于2000
                if (!oldMessage.equals(sequence)) {
                    // 两次提示内容不一致，则提示
                    toast.setText(sequence);
                    toast.show();
                    oneTime = twoTime;
                }
            }
//            toast.setText(sequence);
//            toast.cancel();
//            toast = Toast.makeText(sContext, sequence, Toast.LENGTH_LONG);
//            toast.setDuration(Toast.LENGTH_SHORT);
        }

    }

    public static void showLong(CharSequence sequence) {
        if (toast == null) {
            toast = Toast.makeText(sContext, sequence, Toast.LENGTH_LONG);
        } else {
            toast.cancel();
            toast = Toast.makeText(sContext, sequence, Toast.LENGTH_LONG);
        }
        toast.show();
    }



    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToastThread(String message) {
        if ("main".equals(Thread.currentThread().getName())) {
            showShort(message);
        } else {
            Handler uiThread = new Handler(Looper.getMainLooper());
            uiThread.post(() -> {
                showShort(message);
            });
        }
    }
}
