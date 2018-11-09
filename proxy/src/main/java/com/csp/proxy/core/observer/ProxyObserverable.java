package com.csp.proxy.core.observer;

import com.csp.proxy.core.ProxyReceiver;
import com.csp.proxy.core.ProxyState;

import java.util.ArrayList;
import java.util.List;

/**
 * 代理信息接口(被观察者)
 * Created by chenshp on 2018/4/10.
 */
public class ProxyObserverable implements Observerable {
    private List<ProxyReceiver> proxyListeners = new ArrayList<>();

    @Override
    public void registerReceiver(ProxyReceiver receiver) {
        if (!proxyListeners.contains(receiver)) {
            proxyListeners.add(receiver);
        }
    }

    @Override
    public void unregisterReceiver(ProxyReceiver receiver) {
        if (proxyListeners.contains(receiver)) {
            proxyListeners.remove(receiver);
        }
    }

    @Override
    public void onStatusChanged(ProxyState state) {
        for (ProxyReceiver listener : proxyListeners) {
            if (listener != null)
                listener.onStatusChanged(state);
        }
    }
}