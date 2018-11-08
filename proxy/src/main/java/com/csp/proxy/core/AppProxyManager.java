package com.csp.proxy.core;

import com.csp.utillib.EmptyUtil;

import java.util.ArrayList;
import java.util.List;

public class AppProxyManager {
    // public static boolean isLollipopOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP; // TODO 取消

    private static AppProxyManager instance; // TODO 取消
    private static final String PROXY_APPS = "PROXY_APPS";

//    private Context mContext;
    private List<ProxyApp> proxyApps = new ArrayList<>(); // 被代理了的应用列表

    /**
     * 获取被代理应用列表
     *
     * @return null: 表示全部代理
     */
    public List<ProxyApp> getProxyApps() {
        return proxyApps;
    }

    private AppProxyManager() {
//        this.mContext = context;
//        readProxyAppsList();
    }

    public static AppProxyManager getInstance() {
        if (instance == null) {
            synchronized (AppProxyManager.class) {
                if (instance == null)
                    instance = new AppProxyManager();
            }
        }
        return instance;
    }

    /**
     * 获取指定代理应用
     *
     * @param packageName 指定应用的包名
     * @return 指定代理应用，null：应用未被代理
     */
    public ProxyApp getProxyApp(String packageName) {
        if (EmptyUtil.isBank(packageName))
            return null;

        for (ProxyApp proxyApp : proxyApps) {
            if (packageName.equals(proxyApp.getPackageName()))
                return proxyApp;
        }
        return null;
    }

    /**
     * 指定应用是否被代理
     *
     * @param app 指定应用
     * @return true: 被代理
     */
    public boolean isProxyApp(ProxyApp app) {
        return getProxyApp(app.getPackageName()) != null;
    }

    /**
     * 移除指定需要被代理的应用
     *
     * @param app 指定应用
     */
    public void removeProxyApp(ProxyApp app) {
        for (ProxyApp proxyApp : proxyApps) {
            if (proxyApp.equals(app)) {
                proxyApps.remove(proxyApp);
                break;
            }
        }
//        writeProxyAppsList();
    }

    /**
     * 添加指定需要被代理的应用
     *
     * @param app 指定应用
     */
    public void addProxyApp(ProxyApp app) {
        proxyApps.add(app);
//        writeProxyAppsList();
    }

    /**
     * 清空所有代理应用
     */
    public void clearProxyApps() {
        proxyApps.clear();
//        writeProxyAppsList();
    }
//
//    private void readProxyAppsList() {
//        SharedPreferences preferences = mContext.getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE); // TODO 修改 SharedPreferences 文件名
//        String tmpString = preferences.getString(PROXY_APPS, "");
//        try {
//            if (proxyApps != null) {
//                proxyApps.clear();
//            }
//            if (tmpString.isBank()) {
//                return;
//            }
//            // TODO JSON --> gson，如果应用不存在，则清空
//            JSONArray jsonArray = new JSONArray(tmpString);
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject object = jsonArray.getJSONObject(i);
//                ProxyApp appInfo = new ProxyApp();
//                appInfo.setPackageName(object.getString("package_name"));
//                proxyApps.add(appInfo);
//            }
//        } catch (Exception e) {
//            LogCat.printStackTrace(e);
//        }
//    }
//
//    private void writeProxyAppsList() {
//        SharedPreferences preferences = mContext.getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE); // TODO 修改 SharedPreferences 文件名
//        try {
//            // TODO JSON --> gson
//            JSONArray jsonArray = new JSONArray();
//            for (int i = 0; i < proxyApps.size(); i++) {
//                JSONObject object = new JSONObject();
//                ProxyApp appInfo = proxyApps.get(i);
//                object.put("package_name", appInfo.getPackageName());
//                jsonArray.put(object);
//            }
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putString(PROXY_APPS, jsonArray.toString());
//            editor.apply();
//        } catch (Exception e) {
//            LogCat.printStackTrace(e);
//        }
//    }
}
