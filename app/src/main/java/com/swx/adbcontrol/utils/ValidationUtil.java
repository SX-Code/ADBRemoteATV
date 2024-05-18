package com.swx.adbcontrol.utils;

import android.text.TextUtils;

/**
 * @Author sxcode
 * @Date 2024/5/18 20:31
 */
public class ValidationUtil {
    private static final String regIp = "^([1-9]|[1-9]\\d|1\\d{2}|2[0-1]\\d|22[0-3])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$";
    private static final String regUrl = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    public static final String regStartPath = "^([a-zA-Z_][a-zA-Z0-9_]*[.])*([a-zA-Z_][a-zA-Z0-9_]*)/[a-zA-Z0-9_.]*$";

    public static boolean verifyIpv4(String ip) {
        if (TextUtils.isEmpty(ip)) return false;
        return ip.matches(regIp);
    }

    public static boolean verifyPort(String port) {
        try {
            int i = Integer.parseInt(port);
            return i >= 0 && i <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean verifyUrl(String url) {
        if (TextUtils.isEmpty(url)) return false;
        return url.matches(regUrl);
    }

    /**
     * 检验 ADB 启动 APP 参数
     * @param startPath 启动路径 类似 con.xxx.xxx/.xx.xx.xxActivity
     */
    public static boolean verifyStartPath(String startPath) {
        if (TextUtils.isEmpty(startPath)) return false;
        return startPath.matches(regStartPath);
    }
}
