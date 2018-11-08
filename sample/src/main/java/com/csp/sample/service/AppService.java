package com.csp.sample.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.csp.utillib.BuildConfig;
import com.csp.utillib.CalendarFormat;
import com.csp.utillib.LogWriter;

import java.io.File;

/**
 * Created by luffy on 2018/4/18.
 */

public class AppService extends Service {
    private boolean DEBUG = BuildConfig.DEBUG;
    private LogWriter mLogWriter;

    public static void start(Context context) {
        Intent intent = new Intent(context, AppService.class);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG && mLogWriter == null) {
            String date = CalendarFormat.getNowDateFormat(CalendarFormat.Format.DATE_FORMAT_0);
            String datetime = CalendarFormat.getNowDateFormat(CalendarFormat.Format.DATETIME_FORMAT_0);

            File sdCard = Environment.getExternalStorageDirectory();
            File logFile = new File(sdCard, "APK/Log/" + date + "/Shadowsocks_" + datetime + ".txt");
            mLogWriter = new LogWriter(logFile);
        }

        if (DEBUG)
            mLogWriter.run();
    }

    @Override
    public void onDestroy() {
        if (DEBUG) {
            mLogWriter.quit();
            mLogWriter = null;
        }

        super.onDestroy();
    }
}
