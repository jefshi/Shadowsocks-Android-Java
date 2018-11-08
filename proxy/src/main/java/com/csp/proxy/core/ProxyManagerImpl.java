package com.csp.proxy.core;

import android.content.Context;
import android.content.Intent;

import com.csp.proxy.core.observer.ProxyObserverable;
import com.csp.utillib.EmptyUtil;

import java.util.List;

/**
 * Created by chenshp on 2018/4/10.
 * TODO 不需要接口
 */
class ProxyManagerImpl implements ProxyManager {
    private static ProxyManager instance;
    private Context mContext;

    private AppProxyManager appProxyManager;
    private ProxyConfig proxyConfig;

    private ProxyObserverable observerable;

    public ProxyObserverable getObserverable() {
        return observerable;
    }

    public static ProxyManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ProxyManager.class) {
                if (instance == null)
                    instance = new ProxyManagerImpl(context);
            }
        }
        return instance;
    }

    private ProxyManagerImpl(Context context) {
        this.mContext = context;
        appProxyManager = AppProxyManager.getInstance(); // TODO 不要单例
//        proxyConfig = ProxyConfig.getInstance(); // TODO 不要单例

        observerable = new ProxyObserverable();
    }

    @Override
    public int isPrepareProxy() {
        int result = PREPARE_VALID;
//        if (!isValidProxyUrl(LocalVpnService.getProxyUrl()))
//            result |= PREPARE_PROXY_URL_INVALID;
//
//        if (LocalVpnService.prepare(mContext) != null)
//            result |= PREPARE_VPNSEVICE_INVALID;

        return result;
    }

    @Override
    public Intent prepare() {
//        if (LocalVpnService.isRunning())
//            throw new Error("Proxy running, can't restart!"); // TODO

        return LocalVpnService.prepare(mContext);
    }

    @Override
    public boolean isValidProxyUrl(String proxyUrl) {
        return !EmptyUtil.isBank(proxyUrl)
                && proxyUrl.startsWith("ss://"); // TODO 有效规则变更
    }

    @Override
    public void refreshProxyUrl(String proxyUrl) throws Exception {
        if (!isValidProxyUrl(proxyUrl))
            throw new Exception("please sure proxy url is valid!");

//        LocalVpnService.setProxyUrl(proxyUrl);
        rebootProxy(false);
    }

    @Override
    public void startProxy() {
        if (isPrepareProxy() != PREPARE_VALID) {
            stopProxy();
            throw new Error("proxy hadn't prepare!");
        }

        mContext.startService(new Intent(mContext, LocalVpnService.class));
    }

    @Override
    public void stopProxy() {
        if (LocalVpnService.Instance != null) {
//            LocalVpnService.setRunning(false);
            LocalVpnService.Instance.disconnectVPN();
            mContext.stopService(new Intent(mContext, LocalVpnService.class));
        }

        appProxyManager.clearProxyApps();
    }

    @Override
    public ProxyState getProxyState() {
//        return LocalVpnService.sProxyState;
        return null;
    }

    @Override
    public boolean isProxyRunning() {
//        return LocalVpnService.isRunning();
        return false;
    }

    @Override
    public void switchGlobalMode() {
//        proxyConfig.setGlobalMode(!proxyConfig.isGlobalMode());
    }

    @Override
    public void switchMultipointMode() {
//        proxyConfig.setMultipointMode(!proxyConfig.isMultipointMode());
    }

    @Override
    public void registerReceiver(ProxyReceiver receiver) {
        observerable.registerReceiver(receiver);
    }

    @Override
    public void unregisterReceiver(ProxyReceiver receiver) {
        observerable.unregisterReceiver(receiver);
    }

    @Override
    public List<ProxyApp> getProxyApps() {
        return appProxyManager.getProxyApps();
    }

    @Override
    public boolean isProxyApp(ProxyApp app) {
        return appProxyManager.isProxyApp(app);
    }

    @Override
    public ProxyApp getProxyApp(String packageName) {
        return appProxyManager.getProxyApp(packageName);
    }

    @Override
    public void addProxyApp(ProxyApp app) {
        if (app == null || isProxyApp(app))
            return;

        app.proxyStarting();
        appProxyManager.addProxyApp(app);
//        if (LocalVpnService.isRunning())
//            LocalVpnService.proxy_app_add = true;

        rebootProxy(true);
        app.proxyStarted();
    }

    @Override
    public void removeProxyApp(ProxyApp app) {
        if (app == null || !isProxyApp(app))
            return;

        app.proxyStopping();
        appProxyManager.removeProxyApp(app);
//        boolean existedApp = AppUtils.searchApplication(mContext, app.getPackageName()) != null;
//
//        if (existedApp && LocalVpnService.isRunning())
//            LocalVpnService.proxy_app_remove = true;
//
//        if (appProxyManager.getProxyApps().size() == 0) {
//            LocalVpnService.setRunning(false);
//        } else if (existedApp) {
//            rebootProxy(true);
//        }
        app.proxyStoped();
    }

    /**
     * 重启代理服务
     *
     * @param forceStart true: 服务未启动则直接启动
     */
    private void rebootProxy(boolean forceStart) {
//        if (!LocalVpnService.isRunning()) {
//            if (forceStart)
//                startProxy();
//            return;
//        }
//
//        LocalVpnService.setRunning(false);
//        try {
//            Thread.sleep(120);
//        } catch (InterruptedException e) {
//            LogCat.printStackTrace(e);
//        }
//        startProxy();
    }
}
