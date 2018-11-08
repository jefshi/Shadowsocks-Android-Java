package com.csp.utillib;

import android.os.SystemClock;

/**
 * Created by chenshp on 2018/4/13.
 */

public class DateUtils {
    /**
     * 获取当前时间，当前时间统一使用该方法
     */
    public static long getNowClock() {
        return SystemClock.elapsedRealtime();
    }
}
