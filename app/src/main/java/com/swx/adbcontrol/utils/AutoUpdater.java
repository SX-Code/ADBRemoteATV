package com.swx.adbcontrol.utils;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.swx.adbcontrol.MainApplication;
import com.swx.adbcontrol.R;
import com.swx.adbcontrol.components.UpdateDialog;
import com.swx.adbcontrol.entity.UpdateInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.PublicKey;
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
    private static final int NO_NEED = 4;
    private static final int DOWN_ERROR = 5;
    private static final int CHECK_ERROR = 6;
    private int progress;
    private final String saveFileName;
    private final Context mContext;
    private Uri apkFileUri;
    private UpdateDialog updateDialog;

    public AutoUpdater(Context context) {
        mContext = context;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        saveFileName = String.format(Constant.SAVE_FILE_NAME, format.format(new Date()));
    }

    /**
     * 获取最新APK的下载链接
     */
    public static void getLatestApkUrl(HttpUtil.HttpCallback callback) {
        ThreadPoolService.newTask(() -> {
            UpdateInfo updateInfo = getUpdateInfo();
            if (updateInfo == null) {
                callback.fail("error content");
                return;
            }
            String downloadUrl = updateInfo.getUrl();
            if (ValidationUtil.verifyUrl(downloadUrl)) {
                callback.success(downloadUrl);
            } else {
                callback.fail("empty url!");
            }
        });
    }

    /**
     * 获取更新信息，注意该网络请求方法需要在子线程中调用
     *
     * @return 更新信息
     */
    public static UpdateInfo getUpdateInfo() {
        String jsonContent = HttpUtil.getStringContent(Constant.URL_DOWNLOAD_GET);
        return BeanUtil.getUpdateInfoByJsonArray(jsonContent);
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
            UpdateInfo updateInfo = getUpdateInfo();
            if (updateInfo == null || updateInfo.getUrl().isEmpty()) {
                mHandler.sendEmptyMessage(CHECK_ERROR);
                return;
            }
            String config = updateInfo.getUrl(); // 获取下载链接
            apkUrl = config;
            String apkName = config.substring(config.lastIndexOf('/') + 1);
            String[] split = apkName.replace(".apk", "").split("_");
            versionName = split[split.length - 1];
            if (Long.parseLong(localVersion) >= Long.parseLong(versionName)) {
                mHandler.sendEmptyMessage(SHOWDOWN);
            } else {
                mHandler.sendEmptyMessage(NO_NEED);
            }
        });
    }

    public void downloadApk() {
        ThreadPoolService.newTask(() -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                downloadApkForP();
            } else {
                downloadApkForQ();
            }
        });
    }

    /**
     * 该方法在Android14中也可使用，但Android10以上，推荐使用downloadApkForQ
     */
    private void downloadApkForP() {
        // /storage/emulated/0/Downloads/
        File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), saveFileName);
        try {
            URL url;
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
            if (apkFile.exists()) {
                // 转url
                apkFileUri = getFileUri(apkFile);
            }
            int count = 0;
            byte[] buf = new byte[1024];
            Message message = null;
            while (!intercept) {
                int numRead = ins.read(buf);
                count += numRead;
                progress = (int) (((float) count / length) * 100);
                message = mHandler.obtainMessage();
                message.what = DOWN_UPDATE;
                message.arg1 = progress;
                mHandler.sendMessage(message);
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
            mHandler.sendEmptyMessage(DOWN_ERROR);
            Log.e(TAG, "downloadApk: e", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void downloadApkForQ() {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFileName);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            apkFileUri = mContext.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
            if (apkFileUri == null) {
                throw new RuntimeException("获取uri失败");
            }

            URL url;
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] tm = {new MyX509TrustManager()};
            sslContext.init(null, tm, new SecureRandom());
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            url = new URL(apkUrl);
            URLConnection conn = url.openConnection();
            conn.connect();
            int length = conn.getContentLength();
            InputStream ins = conn.getInputStream();
            OutputStream fos = mContext.getContentResolver().openOutputStream(apkFileUri);
            if (fos == null) {
                throw new RuntimeException("获取OutputStream失败");
            }
            int count = 0;
            byte[] buf = new byte[1024];
            Message message = null;
            while (!intercept) {
                int numRead = ins.read(buf);
                count += numRead;
                progress = (int) (((float) count / length) * 100);
                message = mHandler.obtainMessage();
                message.what = DOWN_UPDATE;
                message.arg1 = progress;
                mHandler.sendMessage(message);
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
            mHandler.sendEmptyMessage(DOWN_ERROR);
            Log.e(TAG, "downloadApk: e", e);
        }
    }

    private Uri getFileUri(File apkFile) {
        if (apkFile == null) return null;
        if (!apkFile.exists()) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//判断版本大于等于7.0
            //如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24，使用FileProvider兼容安装apk
            String packageName = mContext.getApplicationContext().getPackageName();
            String authority = packageName + ".fileProvider";
            return FileProvider.getUriForFile(mContext, authority, apkFile);
        } else {
            return Uri.fromFile(apkFile);
        }

    }


    public void installAPK() {
        try {
            if (apkFileUri == null) {
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 安装完成后打开新版本
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 给目标应用一个临时授权
            intent.setDataAndType(apkFileUri, "application/vnd.android.package-archive");
            mContext.startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());//安装完之后会提示”完成” “打开”
        } catch (Exception e) {
            Log.e(TAG, "installAPK: e", e);
        }
    }

    private void clearApk() {
        if (apkFileUri == null) return;
        mContext.getContentResolver().delete(apkFileUri, null, null);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NO_NEED:
                    ToastUtil.showShort(mContext.getString(R.string.text_no_need_update));
                    break;
                case SHOWDOWN:
                    showUpdateDialog();
                    break;
                case DOWN_UPDATE:
                    updateDialog.setProgress(msg.arg1);
                    break;
                case DOWN_OVER:
                    if (updateDialog != null) updateDialog.hide();
                    ToastUtil.showShort("下载完成");
                    installAPK();
                    break;
                case DOWN_ERROR:
                    updateDialog.hide();
                    clearApk();
                    ToastUtil.showShort("下载失败");
                    break;
                case CHECK_ERROR:
                    ToastUtil.showShort("获取更新信息失败");
                    break;
                default:
                    break;
            }
        }
    };


    private void showUpdateDialog() {
        if (updateDialog == null) {
            updateDialog = new UpdateDialog(mContext);
            updateDialog.setOnButtonClickListener(new UpdateDialog.OnButtonClickListener() {
                @Override
                public void onNegativeClick(View view) {
                    intercept = true; // 打断下载
                    updateDialog.hide();
                    clearApk(); // 清除apk
                }
            });
        }
        updateDialog.show();
        downloadApk();
    }


    @SuppressLint("CustomX509TrustManager")
    public static class MyX509TrustManager implements X509TrustManager {

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
