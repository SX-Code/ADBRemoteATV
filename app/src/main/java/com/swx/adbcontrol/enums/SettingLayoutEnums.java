package com.swx.adbcontrol.enums;

/**
 * @Author sxcode
 * @Date 2024/5/17 13:33
 */
public enum SettingLayoutEnums {
    NAVIGATION_DIRECTION(1, "方向键"),
    QUICK_ACCESS_APPLICATIONS(3, "应用"),
    QUICK_ACCESS_MEDIA_BUTTONS(4, "媒体按钮"),
    QUICK_ACCESS_NONE(5, "没有东西");

    private int code;
    private String desc;

    SettingLayoutEnums(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int code() {
        return code;
    }

    public String desc() {
        return desc;
    }
}
