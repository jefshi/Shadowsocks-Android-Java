package com.csp.sample;

import android.app.Application;
import android.content.Context;

import com.csp.proxy.core.ProxyManager;
import com.csp.proxy.core.ProxyServer;
import com.csp.sample.proxy.BoostApp;
import com.csp.sample.proxy.BoosterServer;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chenshp on 2018/6/6.
 */

public class App extends Application {
    private static Context sContext;
    private static List<BoostApp> sBoostApps;
    private static ProxyManager sProxyManager;

    public static Context getContext() {
        return sContext;
    }

    public static List<BoostApp> getBoostApps() {
        return sBoostApps;
    }

    public static ProxyManager getProxyManager() {
        return sProxyManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
        sBoostApps = new ArrayList<>();
        BoosterServer.getInstance();

        sProxyManager = ProxyServer.getProxyManager(App.getContext());
    }
}
