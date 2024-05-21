package com.swx.adbremote.enums;

/**
 * @Author sxcode
 * @Date 2024/5/17 13:33
 */
public enum InputLanguageEnums {
    CHINESE(1, "中文"),
    ENGLISH(2, "英文");

    private final int code;
    private final String desc;

    InputLanguageEnums(int code, String desc) {
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
