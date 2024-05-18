package com.swx.adbcontrol.entity;

/**
 * @Author sxcode
 * @Date 2024/5/17 18:15
 */
public class AppItem {
    private Integer id;
    private String icon;
    private String name;
    private String url;
    private Integer priority;

    public AppItem(String icon, String name, String url) {
        this.icon = icon;
        this.name = name;
        this.url = url;
    }

    public AppItem(Integer id, String icon, String name, String url) {
        this.id = id;
        this.icon = icon;
        this.name = name;
        this.url = url;
    }

    public AppItem() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void update(AppItem app) {
        this.name = app.getName();
        this.icon = app.getIcon();
        this.url = app.getUrl();
    }
}
