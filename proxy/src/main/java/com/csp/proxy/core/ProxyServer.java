package com.csp.proxy.core;

import android.content.Context;

/**
 * 对外获取 ProxyManager 对象
 * Created by chenshp on 2018/4/11.
 */
public class ProxyServer {
    /**
     * 获取 ProxyManager 对象
     *
     * @param context context
     * @return ProxyManager 对象
     */
    public static ProxyManager getProxyManager(Context context) {
        return ProxyManagerImpl.getInstance(context);
    }
}
