package com.csp.sample;

import android.app.Application;
import android.content.Context;


/**
 * Created by chenshp on 2018/6/6.
 */

public class App extends Application {
    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
    }
}
