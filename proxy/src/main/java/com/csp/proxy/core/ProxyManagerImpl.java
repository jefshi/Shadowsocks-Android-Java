package com.csp.proxy.core;

import android.content.Context;
import android.content.Intent;

import com.csp.proxy.constants.ProxyConstants;
import com.csp.proxy.core.config.ProxyConfig;
import com.csp.proxy.core.observer.ProxyObserverable;
import com.csp.utillib.AppUtil;
import com.csp.utillib.EmptyUtil;
import com.csp.utillib.LogCat;

import java.util.List;

/**
 * Created by chenshp on 2018/4/10.
 */
class ProxyManagerImpl implements ProxyManager {
    private static ProxyManager instance;
    private Context mContext;

    private AppManager mAppManager;
    private ProxyConfig proxyConfig;

    private ProxyObserverable observerable;

    public static ProxyManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ProxyManager.class) {
                if (instance == null)
                    instance = new ProxyManagerImpl(context);
            }
        }
        return instance;
    }

    @Override
    public AppManager getAppManager() {
        return mAppManager;
    }

    public ProxyObserverable getObserverable() {
        return observerable;
    }

    private ProxyManagerImpl(Context context) {
        this.mContext = context;
        mAppManager = new AppManager();
        proxyConfig = ProxyConfig.getInstance(); // TODO 不要单例

        observerable = new ProxyObserverable();
    }

    @Override
    public int isPrepareProxy() {
        int result = PREPARE_VALID;
        if (!isValidProxyUrl(LocalVpnService.getProxyUrl()))
            result |= PREPARE_PROXY_URL_INVALID;

        if (LocalVpnService.prepare(mContext) != null)
            result |= PREPARE_VPNSEVICE_INVALID;

        return result;
    }

    @Override
    public Intent prepare() {
        if (LocalVpnService.isRunning()) {
            LocalVpnService.setRunning(false);
            LogCat.e("Proxy running, can't restart!");
            observerable.onStatusChanged(new ProxyState("Proxy running, can't restart!"));
        }

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

        LocalVpnService.setProxyUrl(proxyUrl);
        rebootProxy(false);
    }

    @Override
    public void startProxy() {
        int prepareResult = isPrepareProxy();
        if (prepareResult != PREPARE_VALID) {
            if ((prepareResult & ProxyManager.PREPARE_PROXY_URL_INVALID) == ProxyManager.PREPARE_PROXY_URL_INVALID) {
                try {
                    refreshProxyUrl(ProxyConstants.URL);
                } catch (Exception e) {
                    LogCat.printStackTrace(e);
                }
            } else {
                LocalVpnService.setRunning(false);
                mAppManager.clearProxyApps();
                // stopProxy();
                LogCat.e("proxy hadn't prepare! result = " + prepareResult);
                ProxyState.STATE_INTERRUPT.setException(new Exception("proxy hadn't prepare! result = " + prepareResult));
                observerable.onStatusChanged(ProxyState.STATE_INTERRUPT);
            }
        }

        mContext.startService(new Intent(mContext, LocalVpnService.class));
    }

    @Override
    public void stopProxy() {
        List<ProxyApp> proxyApps = mAppManager.getProxyApps();
        for (ProxyApp app : proxyApps) {
            app.proxyStopping();
        }

        if (LocalVpnService.Instance != null) {
            LocalVpnService.setRunning(false);
            // LocalVpnService.Instance.disconnectVPN();
            // mContext.stopService(new Intent(mContext, LocalVpnService.class));
        }

        for (ProxyApp app : proxyApps) {
            app.proxyStoped();
        }
        mAppManager.clearProxyApps();
    }

    @Override
    public ProxyState getProxyState() {
        return LocalVpnService.sProxyState;
    }

    @Override
    public boolean isProxyRunning() {
        return LocalVpnService.isRunning();
    }

    @Override
    public boolean isGlobalMode() {
        return proxyConfig.isGlobalMode();
    }

    @Override
    public void switchGlobalMode() {
        proxyConfig.setGlobalMode(!proxyConfig.isGlobalMode());
    }

    @Override
    public void switchMultipointMode() {
        proxyConfig.setMultipointMode(!proxyConfig.isMultipointMode());
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
        return mAppManager.getProxyApps();
    }

    @Override
    public boolean isProxyApp(ProxyApp app) {
        return mAppManager.isProxyApp(app);
    }

    @Override
    public ProxyApp getProxyApp(String packageName) {
        return mAppManager.getProxyApp(packageName);
    }

    @Override
    public void addProxyApp(ProxyApp app) {
        if (app == null || isProxyApp(app))
            return;

        app.proxyStarting();
        mAppManager.clearProxyApps();
        mAppManager.addProxyApp(app);
        if (LocalVpnService.isRunning())
            LocalVpnService.proxy_app_add = true;

        try {
            refreshProxyUrl(app.getProxyUrl());
        } catch (Exception e) {
            LogCat.printStackTrace(e);
        }

        rebootProxy(true);
        app.proxyStarted();
    }

    @Override
    public void removeProxyApp(ProxyApp app) {
        if (app == null || !isProxyApp(app))
            return;

        app.proxyStopping();
        mAppManager.removeProxyApp(app);
        boolean existedApp = AppUtil.searchApplication(mContext, app.getPackageName()) != null;

        if (existedApp && LocalVpnService.isRunning())
            LocalVpnService.proxy_app_remove = true;

        if (mAppManager.getProxyApps().size() == 0) {
            LocalVpnService.setRunning(false);
        } else if (existedApp) {
            rebootProxy(true);
        }
        app.proxyStoped();
    }

    /**
     * 重启代理服务
     *
     * @param forceStart true: 服务未启动则直接启动
     */
    private void rebootProxy(boolean forceStart) {
        if (!LocalVpnService.isRunning()) {
            if (forceStart)
                startProxy();
            return;
        }

        LocalVpnService.setRunning(false);
        try {
            Thread.sleep(120);
        } catch (InterruptedException e) {
            LogCat.printStackTrace(e);
        }
        startProxy();
    }
}
