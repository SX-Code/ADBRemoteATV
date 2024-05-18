package com.swx.adbcontrol.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.swx.adbcontrol.MainApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @Author sxcode
 * @Date 2024/5/18 23:20
 * <a href="https://blog.csdn.net/a497785609/article/details/113896609">...</a>
 */
public class AutoUpdater {
    private static final String TAG = "UpdateUtil";
    private String apkUrl;
    private boolean intercept = false; // 下载中断
    public static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private static final int SHOWDOWN = 3;
    private int progress;
    private File apkFile;
    private Context mContext;

    public AutoUpdater(Context context) {
        mContext = context;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        String saveFileName = String.format(Constant.SAVE_FILE_NAME, format.format(new Date()));
        apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), saveFileName);
    }

    /**
     * 获取最新APK的下载链接
     */
    public static void getLatestApkUrl(HttpUtil.HttpCallback callback) {
        ThreadPoolService.newTask(() -> {
            String downloadUrl = HttpUtil.getStringContent(Constant.URL_LATEST_APK_INFO);
            if (ValidationUtil.verifyUrl(downloadUrl)) {
                callback.success(downloadUrl);
            } else {
                callback.fail("empty content!");
            }
        });
    }

    /**
     * 检查更新
     */
    public void checkUpdate() {
        ThreadPoolService.newTask(() -> {
            String localVersion = "1";
            try {
                localVersion = MainApplication.getContext().getPackageManager().getPackageInfo(MainApplication.getContext().getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "checkUpdate: e", e);
            }
            String versionName = "1";
            String config = HttpUtil.getStringContent(Constant.URL_CHECK_VERSION);
            if (config.isEmpty()) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Matcher matcher = Pattern.compile("\"versionName\":\\s*\"(?<m>[^\"]*?)\"").matcher(config);
                if (matcher.find()) {
                    String v = matcher.group("m");
                    if (v != null) {
                        versionName = v.replace("v1.0.", "");
                    }
                }
            }
            if (Long.parseLong(localVersion) < Long.parseLong(versionName)) {
                mHandler.sendEmptyMessage(SHOWDOWN);
            }
        });
    }

    public void downloadApk() {
        ThreadPoolService.newTask(() -> {
            URL url;
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManager[] tm = {new MyX509TrustManager()};
                sslContext.init(null, tm, new SecureRandom());
                SSLSocketFactory ssf = sslContext.getSocketFactory();

                url = new URL(apkUrl);
                URLConnection conn = url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream ins = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(apkFile);
                int count = 0;
                byte[] buf = new byte[1024];
                while (!intercept) {
                    int numRead = ins.read(buf);
                    count += numRead;
                    progress = (int) (((float) count / length) * 100);
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numRead <= 0) {
                        // 下载完成
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numRead);
                }
                fos.close();
                ins.close();
            } catch (Exception e) {
                Log.e(TAG, "downloadApk: e", e);
            }
        });
    }

    public void installAPK() {
        try {
            if (!apkFile.exists()) {
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 安装完成后打开新版本
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 给目标应用一个临时授权
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于等于7.0
                //如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24，使用FileProvider兼容安装apk
                String packageName = mContext.getApplicationContext().getPackageName();
                String authority = packageName + ".fileprovider";
                Uri apkUri = FileProvider.getUriForFile(mContext, authority, apkFile);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }
            mContext.startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());//安装完之后会提示”完成” “打开”
        } catch (Exception e) {
            Log.e(TAG, "installAPK: e", e);
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOWDOWN:
                    break;
                case DOWN_UPDATE:
                    break;
                case DOWN_OVER:
                    ToastUtil.showShort("下载完成");
                    installAPK();
                    break;
                default:
                    break;
            }
        }
    };

    public static class MyX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
