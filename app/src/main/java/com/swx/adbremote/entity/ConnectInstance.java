package com.swx.adbremote.entity;

public class ConnectInstance {
    private Integer id;
    private String alias;
    private Integer port;
    private String ip;
    private boolean select = false;
    private boolean active = false;

    private boolean connecting = false;

    public ConnectInstance() {
    }

    public ConnectInstance(String alias, String ip, Integer port) {
        this.alias = alias;
        this.port = port;
        this.ip = ip;
    }

    public ConnectInstance(Integer id, String alias, String ip, Integer port) {
        this.id = id;
        this.alias = alias;
        this.port = port;
        this.ip = ip;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isConnecting() {
        return connecting;
    }

    public void setConnecting(boolean connecting) {
        this.connecting = connecting;
    }

    public boolean equals(ConnectInstance instance) {
        return instance != null && this.getIp().equals(instance.getIp()) && this.getPort().equals(instance.getPort());
    }

    public void update(ConnectInstance connectInstance) {
        this.ip = connectInstance.getIp();
        this.port = connectInstance.getPort();
        this.alias = connectInstance.getAlias();
        // 没有更改IP和端口，继续保持激活状态
        if (!this.equals(connectInstance)) {
            this.active = false;
        }
        this.connecting = false;
    }

    public void setData(String alias, String ip, Integer port) {
        this.alias = alias;
        this. ip = ip;
        this.port = port;
    }

    public void clearData() {
        this.id = null;
        this.alias = null;
        this.port = null;
    }
}
