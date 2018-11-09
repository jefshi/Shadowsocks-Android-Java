package com.csp.proxy.constants;

import com.csp.proxy.BuildConfig;

/**
 * 代理常量
 * Created by chenshp on 2018/4/10.
 */
public interface ProxyConstants {
    /**
     * 调试模式开关，发版请设置为 false
     * false：代码混淆压缩后，将日志信息清除
     */
    boolean LOG_DEBUG = BuildConfig.DEBUG; // 调试模式，true：用于开启日志打印
    boolean TOSTRING_DEBUG = BuildConfig.DEBUG; // 调试模式，true：用于 toString() 显示详细内容

    String URL = "ss://aes-256-cfb:cylj2018@148.153.44.2:8342";

    // TODO 是否需要？？？
    int STATE_PROXY_STARTING = 0;
    int STATE_PROXY_STARTED = 1;
    int STATE_PROXY_STOPPTING = 2;
    int STATE_PROXY_STOPTED = 3;
}
