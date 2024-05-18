package com.swx.adbcontrol.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @Author sxcode
 * @Date 2024/5/18 1:24
 * <a href="https://www.cnblogs.com/lihuawei/p/15328883.html">...</a>
 */
public class HttpUtil {
    private static final String TAG = "HttpUtil";

    public static interface HttpCallback {
        public void success(String content);
        public void fail(String msg);
    }

    public static String getStringContent(String url_path) {
        try {
            URL url = new URL(url_path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");//请求方法
            connection.setDoInput(true);//输入流
            int code = connection.getResponseCode();
            if (code == 200) {
                //服务器已准备好，可以取出流，流直接转换成字符串
                return ChangeInputStream(connection.getInputStream());
            }
        } catch (Exception e) {
            Log.e(TAG, "getJsonContent: e", e);
        }
        return "";
    }

    private static String ChangeInputStream(InputStream inputStream) {
        String stringContent = "";
        //字节数组流，表示在内存地址当中可以显示出来，可以把数据写入内存当中
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int len = 0;
        byte[] data = new byte[1024];
        try {
            while ((len = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, len);
            }
            //outputStream流——>写入ByteArrayOutputStream——>转换成字节数组
            stringContent = outputStream.toString();
        } catch (IOException e) {
            Log.e(TAG, "ChangeInputStream: e", e);
        }
        return stringContent;
    }
}
