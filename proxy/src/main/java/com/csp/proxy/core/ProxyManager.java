package com.csp.proxy.core;

import android.content.Context;
import android.content.Intent;

import com.csp.proxy.core.observer.Observerable;

import java.util.List;

/**
 * 对外代理接口
 * Created by chenshp on 2018/4/10.
 */
public interface ProxyManager {
    /**
     * 代理加速准备状态
     */
    int PREPARE_VALID = 0x0; // 已准备完成
    int PREPARE_PROXY_URL_INVALID = 0x1; // 代理配置无效
    int PREPARE_VPNSEVICE_INVALID = 0x2; // 未请求 {@link android.net.VpnService#prepare(Context)}

    /**
     * 代理是否已经准备
     *
     * @return {@link #PREPARE_VALID}, {@link #PREPARE_PROXY_URL_INVALID}, {@link #PREPARE_VPNSEVICE_INVALID}
     */
    int isPrepareProxy();

    /**
     * @return {@link android.net.VpnService#prepare(Context)}
     */
    Intent prepare();

    /**
     * 代理配置 URL 是否有效
     *
     * @param proxyUrl 代理配置 URL
     * @return true: 有效
     */
    boolean isValidProxyUrl(String proxyUrl);

    /**
     * 代理准备
     *
     * @param proxyUrl 代理配置 URL, {@link #isValidProxyUrl(String)}
     */
    void refreshProxyUrl(String proxyUrl) throws Exception;

    /**
     * 启动代理
     */
    void startProxy();

    /**
     * 停止代理
     */
    void stopProxy();

    /**
     * 获取代理状态
     *
     * @return 代理状态
     */
    ProxyState getProxyState();

    /**
     * 代理是否运行
     *
     * @return true: 运行中
     */
    @Deprecated
    boolean isProxyRunning();

    /**
     * 是否是全局模式
     *
     * @return true: 是
     */
    boolean isGlobalMode();

    /**
     * 切换全局模式，默认关闭
     */
    void switchGlobalMode();

    /**
     * 切换多点加速模式，默认关闭
     */
    void switchMultipointMode();

    /**
     * @see Observerable#registerReceiver(ProxyReceiver)
     */
    void registerReceiver(ProxyReceiver receiver);

    /**
     * @see Observerable#unregisterReceiver(ProxyReceiver)
     */
    void unregisterReceiver(ProxyReceiver receiver);

    /**
     * @see AppManager#getProxyApps()
     */
    List<ProxyApp> getProxyApps();

    /**
     * @see AppManager#isProxyApp(ProxyApp)
     */
    boolean isProxyApp(ProxyApp app);

    /**
     * @see AppManager#getProxyApp(String)
     */
    ProxyApp getProxyApp(String packageName);

    /**
     * @see AppManager#addProxyApp(ProxyApp)
     */
    void addProxyApp(ProxyApp app);

    /**
     * @see AppManager#removeProxyApp(ProxyApp)
     */
    void removeProxyApp(ProxyApp app);
}
