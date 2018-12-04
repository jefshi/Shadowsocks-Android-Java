package com.csp.proxy.core;

import com.csp.proxy.constants.ProxyConstants;

/**
 * Created by chenshp on 2018/4/16.
 */

public class ProxyState {
    private int code;
    private boolean running;
    private String content;
    private Exception exception;
    private String packageName; // TODO STATE_APP_PROXY_ADD or STATE_APP_PROXY_REMOVE

    // TODO 去除 Begin
    @Deprecated
    public ProxyState(String content, Object... fill) {
        this.code = CODE_LOG;
        this.content = String.format(content, fill);
    }
    // TODO 去除 End

    public ProxyState(String content) {
        this.code = CODE_LOG;
        this.content = content;
    }

    private ProxyState(int code, boolean running) {
        this.code = code;
        this.running = running;
    }

    private ProxyState(int code, String content, boolean running) {
        this.code = code;
        this.content = content;
        this.running = running;
    }

    public int getCode() {
        return code;
    }

    public String getContent() {
        return content;
    }

    public boolean isRunning() {
        return running;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception e) {
        if (code == CODE_LOG
                || code == CODE_EXCEPTION
                || code == CODE_INTERRUPT)
            this.exception = e;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    private String getStateName() {
        switch (code) {
            case CODE_LOG:
                return "CODE_LOG";
            case CODE_DISCONNECTED:
                return "CODE_DISCONNECTED";
            case CODE_INTERRUPT:
                return "CODE_INTERRUPT";
//            case CODE_APPS_ALLOWED:
//                return "CODE_APPS_ALLOWED";
            case CODE_CONNECTED:
                return "CODE_CONNECTED";
            case CODE_APP_PROXY_ADD:
                return "CODE_APP_PROXY_ADD";
            case CODE_APP_PROXY_REMOVE:
                return "CODE_APP_PROXY_REMOVE";
            case CODE_EXCEPTION:
                return "CODE_EXCEPTION";
            default:
                return "default";
        }
    }

    @Override
    public String toString() {
        return !ProxyConstants.TOSTRING_DEBUG ? super.toString() : "ProxyState{" +
                "code=" + getStateName() +
                ", running=" + running +
                ", content='" + content + '\'' +
                ", exception=" + exception +
                ", packageName=" + packageName +
                '}';
    }

    /**
     * TODO 代理状态
     */
    public static final int CODE_LOG = 0;
    public static final int CODE_DISCONNECTED = 10;
    public static final int CODE_INTERRUPT = 20;
    // public static final int CODE_APPS_ALLOWED = 30;
    public static final int CODE_CONNECTED = 50;
    public static final int CODE_APP_PROXY_ADD = 60;
    public static final int CODE_APP_PROXY_REMOVE = 61;
    public static final int CODE_EXCEPTION = 70;

    public static final ProxyState STATE_DISCONNECTED = new ProxyState(CODE_DISCONNECTED,"STATE_DISCONNECTED", false); // 代理服务已断开连接
    public static final ProxyState STATE_INTERRUPT = new ProxyState(CODE_INTERRUPT, "STATE_INTERRUPT",false); // 代理服务已中止连接
    // public static final ProxyState STATE_APPS_ALLOWED = new ProxyState(CODE_APPS_ALLOWED, false); // 被代理的应用已限制，TODO，存在风险
    public static final ProxyState STATE_CONNECTED = new ProxyState(CODE_CONNECTED,"STATE_CONNECTED", true); // 代理服务已连接
    public static final ProxyState STATE_APP_PROXY_ADD = new ProxyState(CODE_APP_PROXY_ADD, "STATE_APP_PROXY_ADD",true); // 被代理应用追加，代理服务仍然连接
    public static final ProxyState STATE_APP_PROXY_REMOVE = new ProxyState(CODE_APP_PROXY_REMOVE, "STATE_APP_PROXY_REMOVE",true); // 被代理应用移除，代理服务仍然连接
    public static final ProxyState STATE_EXCEPTION = new ProxyState(CODE_EXCEPTION, "STATE_EXCEPTION",true); // 发生异常(不停止)，代理服务仍然连接
}
