package com.csp.proxy.core;

import com.csp.proxy.constants.ProxyConstants;
import com.csp.utillib.DateUtils;

public class ProxyApp {
    private String packageName;
    private String proxyUrl;
    private boolean boosted;
    private int proxyState;
    private long beginClock;

    public ProxyApp() {
        proxyStoped();
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    public boolean isBoosted() {
        return boosted;
    }

    public void setBoosted(boolean boosted) {
        this.boosted = boosted;
    }

    public int getProxyState() {
        return proxyState;
    }

    public void setProxyState(int proxyState) {
        this.proxyState = proxyState;
    }

    public long getBeginClock() {
        return beginClock;
    }

    public void setBeginClock(long beginClock) {
        this.beginClock = beginClock;
    }

    @Override
    public String toString() {
        return !ProxyConstants.TOSTRING_DEBUG ? super.toString() : "ProxyApp{" +
                "packageName='" + packageName + '\'' +
                ", proxyUrl='" + proxyUrl + '\'' +
                ", boosted=" + boosted +
                ", proxyState=" + proxyState +
                ", beginClock=" + beginClock +
                '}';
    }

    /**
     * 代理开始中
     */
    void proxyStarting() {
        boosted = false;
        proxyState = ProxyConstants.STATE_PROXY_STARTING;
        this.beginClock = 0;
    }

    /**
     * 开始代理
     */
    public void proxyStarted() {
        boosted = true;
        proxyState = ProxyConstants.STATE_PROXY_STARTED;
        this.beginClock = DateUtils.getNowClock();
    }

    /**
     * 停止代理中
     */
    void proxyStopping() {
        boosted = true;
        proxyState = ProxyConstants.STATE_PROXY_STOPPTING;
    }

    /**
     * 停止代理
     */
    public void proxyStoped() {
        boosted = false;
        proxyState = ProxyConstants.STATE_PROXY_STOPTED;
        this.beginClock = 0;
    }

    /**
     * 获取加速时间
     */
    public long getBoostedTime() {
        return boosted ? DateUtils.getNowClock() - beginClock : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof ProxyApp))
            return false;

        ProxyApp other = (ProxyApp) obj;

        if (packageName == null)
            return other.getPackageName() == null;
        else
            return packageName.equals(other.getPackageName());
    }
}
