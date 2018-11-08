package com.csp.sample.proxy;

import android.graphics.drawable.Drawable;

import com.csp.proxy.core.ProxyApp;


/**
 * 代理应用
 * Created by chenshp on 2018/4/25.
 */
public class BoostApp extends ProxyApp {
    private Drawable appIcon;
    private String appLabel;

    public Drawable getAppIcon() {
        return this.appIcon;
    }

    public String getAppLabel() {
        return this.appLabel;
    }

    public void setAppIcon(Drawable var1) {
        this.appIcon = var1;
    }

    public void setAppLabel(String var1) {
        this.appLabel = var1;
    }
}
