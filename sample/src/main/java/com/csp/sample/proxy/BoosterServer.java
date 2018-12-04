package com.csp.sample.proxy;

import android.content.Intent;

import com.csp.proxy.core.ProxyApp;
import com.csp.proxy.core.ProxyManager;
import com.csp.proxy.core.ProxyReceiver;
import com.csp.proxy.core.ProxyServer;
import com.csp.sample.App;
import com.csp.utillib.LogCat;

/**
 * 代理模块使用
 * Created by chenshp on 2018/4/17.
 */
@SuppressWarnings("unused")
public class BoosterServer {
    public ProxyManager proxyManager;

    private BoosterServer() {
        proxyManager = ProxyServer.getProxyManager(App.getContext());
        proxyManager.switchMultipointMode();
    }

    private static class Instance {
        private static BoosterServer instance = new BoosterServer();
    }

    public static BoosterServer getInstance() {
        return Instance.instance;
    }

    /**
     * 代理准备完成
     *
     * @return true: 需要准备，请调用 {@link #prepareProxy()}
     */
    public boolean isPrepareProxy() {
        int flag = proxyManager.isPrepareProxy();
        if ((flag & ProxyManager.PREPARE_PROXY_URL_INVALID) == ProxyManager.PREPARE_PROXY_URL_INVALID)
            refreshProxyUrl();

        return (flag & ProxyManager.PREPARE_VPNSEVICE_INVALID) != ProxyManager.PREPARE_VPNSEVICE_INVALID;
    }

    /**
     * @see ProxyManager#prepare()
     */
    public Intent prepareProxy() {
        return proxyManager.prepare();
    }

    /**
     * @see ProxyManager#stopProxy()
     */
    public void stopProxy() {
        proxyManager.stopProxy();
    }

    /**
     * @see ProxyManager#registerReceiver(ProxyReceiver)
     */
    public void registerReceiver(ProxyReceiver receiver) {
        proxyManager.registerReceiver(receiver);
    }

    /**
     * @see ProxyManager#unregisterReceiver(ProxyReceiver)
     */
    public void unregisterReceiver(ProxyReceiver receiver) {
        proxyManager.unregisterReceiver(receiver);
    }

    /**
     * @see ProxyManager#getProxyApp(String)
     */
    public BoostApp getBoostApp(String packageName) {
        return (BoostApp) proxyManager.getProxyApp(packageName);
    }

    /**
     * @see ProxyManager#isProxyApp(ProxyApp)
     */
    public boolean isBoostApp(BoostApp app) {
        return proxyManager.isProxyApp(app);
    }

    /**
     * @see ProxyManager#addProxyApp(ProxyApp)
     */
    public void addBoostApp(BoostApp app) {
        if (app == null)
            return;

        app.setProxyUrl(Constant.URL);
        proxyManager.addProxyApp(app);
    }

    /**
     * @see ProxyManager#removeProxyApp(ProxyApp)
     */
    public void removeBoostApp(BoostApp app) {
        if (app == null)
            return;

        proxyManager.removeProxyApp(app);
    }

    /**
     * 获取加速时长
     */
    public void refreshProxyUrl() {
//        TblBoosterOperate tpuo = TblBoosterOperate.getInstance();
//        TblBooster tblBooster = tpuo.getValidUrl();
        try {
            String url = Constant.URL;
//            String url = tblBooster.getUrl();
//            if (EmptyUtil.isBank(url))
//                url = Constant.Booster.URL;

            proxyManager.refreshProxyUrl(url);
//            tpuo.useProxyUrl(tblBooster, true);
        } catch (Exception e) {
            LogCat.printStackTrace(e);
        }
    }

    /**
     * 同步指定应用加速信息
     *
     * @param boostApp 指定应用
     */
    public static void syncBoostApp(BoostApp boostApp) {
        BoostApp proxyApp = BoosterServer.getInstance().getBoostApp(boostApp.getPackageName());
        if (proxyApp != null) {
            boostApp.setBoosted(proxyApp.isBoosted());
            boostApp.setProxyState(proxyApp.getProxyState());
            boostApp.setBeginClock(proxyApp.getBeginClock());
//            boostApp.setUseBooster(proxyApp.getUseBooster());
        } else {
            boostApp.proxyStoped();
        }
    }
}
