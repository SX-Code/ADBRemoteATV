package com.swx.adbcontrol.utils;

import java.util.Locale;

/**
 * @Author sxcode
 * @Date 2024/5/17 13:52
 */
public class Constant {
    public static final String KEY_SETTING_LAYOUT_NAV = "key_setting_layout_nav";
    public static final String KEY_SETTING_LAYOUT_QUICK_ACCESS = "key_setting_layout_quick_access";
    public static final String KEY_SETTING_BEHAVIOR_HAPTIC_FEEDBACK = "key_setting_behavior_haptic_feedback";
    public static final String KEY_QUICK_ACCESS_ORDER_CHANGE = "key_quick_access_order_change";
    public static final String KEY_QUICK_ACCESS_APP_ONLINE_URL = "key_quick_access_app_online_url";
    public static final String VALUE_ACTIVITY_APP_PRIORITY = "value_activity_app_priority";
    public static final String URL_PREFIX_ZH = "https://gitee.com/SX-Code/ADBRemoteATV/raw/master/"; // Gitee文件地址前缀
    public static final String URL_PREFIX_EN = "https://raw.githubusercontent.com/SX-Code/ADBRemoteATV/main/"; // Github文件地址前缀

    public static final String URL_PREFIX_DOWNLOAD_EN = "https://github.com/SX-Code/ADBRemoteATV/releases/download/"; // Github下载地址
    public static final String URL_PREFIX_DOWNLOAD_ZH = "https://gitee.com/SX-Code/ADBRemoteATV/releases/download/"; // Gitee 下载地址
    public static final String FILE_SUFFIX_DOWNLOAD_GET = "update.json"; // 下载信息文件名
    public static String URL_DOWNLOAD_GET = URL_PREFIX_ZH + FILE_SUFFIX_DOWNLOAD_GET; // 获取下载地址
    public static final String FILE_SUFFIX_APPS = "apps.json"; // APPS仓库文件名
    public static String DEFAULT_QUICK_ACCESS_APP_ONLINE_URL = URL_PREFIX_ZH + FILE_SUFFIX_APPS; // APPS仓库地址
    public static final String SAVE_FILE_NAME = "ADB_Remote_ATV_%s.apk";
    public static final String URL_GITHUB_CODE_PAGE = "https://github.com/SX-Code/ADBRemoteATV"; // Github页面
    public static final String URL_GITHUB_FEEDBACK_PAGE = "https://github.com/SX-Code/ADBRemoteATV/discussions"; // Github讨论页面

    static {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        if (!language.equals("zh")) {
            // 使用Github
            DEFAULT_QUICK_ACCESS_APP_ONLINE_URL = URL_PREFIX_ZH + FILE_SUFFIX_APPS;
            URL_DOWNLOAD_GET = URL_PREFIX_EN + FILE_SUFFIX_DOWNLOAD_GET;
        }
    }
}
