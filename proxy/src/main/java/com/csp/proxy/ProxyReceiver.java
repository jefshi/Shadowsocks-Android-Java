package com.csp.proxy;

/**
 * 代理信息接收者接口(观察者)
 * Created by chenshp on 2018/4/10.
 */
public interface ProxyReceiver {
    /**
     * 代理状态变化
     *
     * @param state 代理状态
     */
    void onStatusChanged(ProxyState state);
}