package com.csp.proxy.core.observer;

import com.csp.proxy.core.ProxyReceiver;
import com.csp.proxy.core.ProxyState;

/**
 * 代理信息接口(被观察者)接口
 * Created by chenshp on 2018/4/10.
 */
public interface Observerable {
    /**
     * 注册代理信息接收器
     *
     * @param receiver 代理信息接收器
     */
    void registerReceiver(ProxyReceiver receiver);

    /**
     * 移除代理信息接收器
     *
     * @param receiver 代理信息接收器
     */
    void unregisterReceiver(ProxyReceiver receiver);

    /**
     * 代理状态变化
     *
     * @param state 代理状态
     */
    void onStatusChanged(ProxyState state);
}