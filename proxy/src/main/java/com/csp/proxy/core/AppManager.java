package com.csp.proxy.core;

import com.csp.utillib.EmptyUtil;

import java.util.ArrayList;
import java.util.List;

public class AppManager {
    private List<ProxyApp> proxyApps = new ArrayList<>(); // 被代理了的应用列表

    /**
     * 获取被代理应用列表
     *
     * @return null: 表示全部代理
     */
    public List<ProxyApp> getProxyApps() {
        return proxyApps;
    }

    public AppManager() {
    }

    /**
     * 获取指定代理应用
     *
     * @param packageName 指定应用的包名
     * @return 指定代理应用，null：应用未被代理
     */
    public ProxyApp getProxyApp(String packageName) {
        if (EmptyUtil.isBank(packageName))
            return null;

        for (ProxyApp proxyApp : proxyApps) {
            if (packageName.equals(proxyApp.getPackageName()))
                return proxyApp;
        }
        return null;
    }

    /**
     * 指定应用是否被代理
     *
     * @param app 指定应用
     * @return true: 被代理
     */
    public boolean isProxyApp(ProxyApp app) {
        return getProxyApp(app.getPackageName()) != null;
    }

    /**
     * 移除指定需要被代理的应用
     *
     * @param app 指定应用
     */
    public void removeProxyApp(ProxyApp app) {
        for (ProxyApp proxyApp : proxyApps) {
            if (proxyApp.equals(app)) {
                proxyApps.remove(proxyApp);
                break;
            }
        }
    }

    /**
     * 添加指定需要被代理的应用
     *
     * @param app 指定应用
     */
    public void addProxyApp(ProxyApp app) {
        proxyApps.add(app);
    }

    /**
     * 清空所有代理应用
     */
    public void clearProxyApps() {
        proxyApps.clear();
    }
}
