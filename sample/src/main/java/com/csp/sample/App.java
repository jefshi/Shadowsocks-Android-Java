package com.csp.sample;

import android.app.Application;
import android.content.Context;

import com.csp.sample.proxy.BoostApp;
import com.csp.sample.proxy.BoosterServer;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chenshp on 2018/6/6.
 */

public class App extends Application {
    private static Context sContext;
    private static List<BoostApp> mBoostApps;

    public static Context getContext() {
        return sContext;
    }

    public static List<BoostApp> getmBoostApps() {
        return mBoostApps;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
        mBoostApps = new ArrayList<>();
        BoosterServer.getInstance();
    }
}
