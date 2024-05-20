package com.swx.adbcontrol.entity;

/**
 * @Author sxcode
 * @Date 2024/5/20 10:39
 */
public class UpdateInfo {
    private String info;
    private String url;

    public UpdateInfo() {
    }

    public UpdateInfo(String info, String url) {
        this.info = info;
        this.url = url;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
