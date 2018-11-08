package com.csp.proxy.core;


import android.content.Context;
import android.os.Build;


import com.csp.proxy.ProxyConstants;
import com.csp.proxy.R;
import com.csp.proxy.tcpip.CommonMethods;
import com.csp.proxy.tunnel.Config;
import com.csp.proxy.tunnel.httpconnect.HttpConnectConfig;
import com.csp.proxy.tunnel.shadowsocks.ShadowsocksConfig;
import com.csp.utillib.LogCat;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyConfig {
    private static ProxyConfig INSTANCE = new ProxyConfig();

    private final static boolean IS_DEBUG = ProxyConstants.LOG_DEBUG; // TODO DEBUG 模式
    public static String AppInstallID;
    public static String AppVersion;
    private final static int FAKE_NETWORK_MASK = CommonMethods.ipStringToInt("255.255.0.0");
    public final static int FAKE_NETWORK_IP = CommonMethods.ipStringToInt("172.25.0.0");

    public ArrayList<IPAddress> m_IpList; // TODO ？？？
    public ArrayList<IPAddress> m_DnsList; // TODO ？？？
    public ArrayList<IPAddress> m_RouteList; // TODO ？？？
    public ArrayList<Config> m_ProxyList; // TODO 代理配置列表
    public HashMap<String, Boolean> m_DomainMap; // TODO ？？？

    private boolean globalMode = false;
    private boolean multipointMode = false;

    private int m_dns_ttl;
    private String m_welcome_info;
    private String m_session_name;
    private String m_user_agent;
    private boolean m_outside_china_use_proxy = true;
    private boolean m_isolate_http_host_header = true;
    private int m_mtu;

    private Timer m_Timer; // TODO ？？？

    public boolean isGlobalMode() {
        return globalMode;
    }

    public void setGlobalMode(boolean globalMode) {
        if (this.globalMode != globalMode) {
            this.globalMode = globalMode;

            // TODO VpnService 为 null 时，回调
            final ProxyState state = new ProxyState("Proxy global mode is " + (this.globalMode ? "on" : "off"));

            if (LocalVpnService.Instance == null) {
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((ProxyManagerImpl) ProxyManagerImpl.getInstance(mContext))
//                                .getObserverable()
//                                .onStatusChanged(state);
//                    }
//                });
                LogCat.i(state);
                return;
            }

            LocalVpnService.Instance.onStatusChanged(state);
        }
    }

    public boolean isMultipointMode() {
        return multipointMode;
    }

    public void setMultipointMode(boolean multipointMode) {
        if (this.multipointMode != multipointMode) {
            this.multipointMode = multipointMode;

            // TODO VpnService 为 null 时，回调
            final ProxyState state = new ProxyState("Proxy multipoint mode is " + (this.multipointMode ? "on" : "off"));

            if (LocalVpnService.Instance == null) {
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((ProxyManagerImpl) ProxyManagerImpl.getInstance(mContext))
//                                .getObserverable()
//                                .onStatusChanged(state);
//                    }
//                });
                LogCat.i(state);
                return;
            }

            LocalVpnService.Instance.onStatusChanged(state);
        }
    }

    public static ProxyConfig getInstance() {
//        if (INSTANCE == null) {
//            synchronized (ProxyConfig.class) {
//                if (INSTANCE == null)
//                    INSTANCE = new ProxyConfig(context);
//            }
//        }
        return INSTANCE;
    }


    public String getWelcomeInfo() {
        return m_welcome_info;
    }

    public int getMTU() {
        if (m_mtu > 1400 && m_mtu <= 20000) {
            return m_mtu;
        } else {
            return 20000;
        }
    }

    public ArrayList<IPAddress> getDnsList() {
        return m_DnsList;
    }

    public ArrayList<IPAddress> getRouteList() {
        return m_RouteList;
    }

    // TODO 未阅读 Begin
    public ProxyConfig() {
        m_IpList = new ArrayList<IPAddress>();
        m_DnsList = new ArrayList<IPAddress>();
        m_RouteList = new ArrayList<IPAddress>();
        m_ProxyList = new ArrayList<Config>();
        m_DomainMap = new HashMap<String, Boolean>();

        m_Timer = new Timer();
        m_Timer.schedule(m_Task, 120000, 120000);//每两分钟刷新一次。
    }

    TimerTask m_Task = new TimerTask() {
        @Override
        public void run() {
            refreshProxyServer();//定时更新dns缓存
        }

        //定时更新dns缓存
        void refreshProxyServer() {
            try {
                for (int i = 0; i < m_ProxyList.size(); i++) {
                    try {
                        Config config = m_ProxyList.get(0);
                        InetAddress address = InetAddress.getByName(config.ServerAddress.getHostName());
                        if (address != null && !address.equals(config.ServerAddress.getAddress())) {
                            config.ServerAddress = new InetSocketAddress(address, config.ServerAddress.getPort());
                        }
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {

            }
        }
    };
    // TODO 未阅读 End

    public Config getDefaultProxy() {
        if (m_ProxyList.size() > 0) {
            return m_ProxyList.get(0);
        } else {
            return null;
        }
    }


    public IPAddress getDefaultLocalIP() {
        if (m_IpList.size() > 0) {
            return m_IpList.get(0);
        } else {
            return new IPAddress("10.8.0.2", 32);
        }
    }


    public String getSessionName() {
        if (m_session_name == null) {
            m_session_name = getDefaultProxy().ServerAddress.getHostName();
        }
        return m_session_name;
    }

    public static boolean isFakeIP(int ip) {
        return (ip & ProxyConfig.FAKE_NETWORK_MASK) == ProxyConfig.FAKE_NETWORK_IP; // TODO 为啥不是 ip == ProxyConfig.FAKE_NETWORK_IP ？？？
    }

    // TODO 未阅读 Begin
    public Config getDefaultTunnelConfig(InetSocketAddress destAddress) {
        return getDefaultProxy();
    }

    public int getDnsTTL() {
        if (m_dns_ttl < 30) {
            m_dns_ttl = 30;
        }
        return m_dns_ttl;
    }

    public String getUserAgent() {
        if (m_user_agent == null || m_user_agent.isEmpty()) {
            m_user_agent = System.getProperty("http.agent");
        }
        return m_user_agent;
    }

    private Boolean getDomainState(String domain) {
        domain = domain.toLowerCase();
        while (domain.length() > 0) {
            Boolean stateBoolean = m_DomainMap.get(domain);
            if (stateBoolean != null) {
                return stateBoolean;
            } else {
                int start = domain.indexOf('.') + 1;
                if (start > 0 && start < domain.length()) {
                    domain = domain.substring(start);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public boolean needProxy(String host, int ip) {
        if (globalMode) {
            return true;
        }
        if (host != null) {
            Boolean stateBoolean = getDomainState(host);
            if (stateBoolean != null) {
                return stateBoolean.booleanValue();
            }
        }

        if (isFakeIP(ip))
            return true;

        if (m_outside_china_use_proxy && ip != 0) {
            return !ChinaIpMaskManager.isIPInChina(ip);
        }
        return false;
    }

    public boolean isIsolateHttpHostHeader() {
        return m_isolate_http_host_header;
    }

    private String[] downloadConfig(String url) throws Exception {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet requestGet = new HttpGet(url);

            requestGet.addHeader("X-Android-MODEL", Build.MODEL);
            requestGet.addHeader("X-Android-SDK_INT", Integer.toString(Build.VERSION.SDK_INT));
            requestGet.addHeader("X-Android-RELEASE", Build.VERSION.RELEASE);
            requestGet.addHeader("X-App-Version", AppVersion);
            requestGet.addHeader("X-App-Install-ID", AppInstallID);
            requestGet.setHeader("User-Agent", System.getProperty("http.agent"));
            HttpResponse response = client.execute(requestGet);

            String configString = EntityUtils.toString(response.getEntity(), "UTF-8");
            String[] lines = configString.split("\\n");
            return lines;
        } catch (Exception e) {
            throw new Exception(String.format("Download config file from %s failed.", url));
        }
    }

    private String[] readConfigFromFile(String path) throws Exception {
        StringBuilder sBuilder = new StringBuilder();
        FileInputStream inputStream = null;
        try {
            byte[] buffer = new byte[8192];
            int count = 0;
            inputStream = new FileInputStream(path);
            while ((count = inputStream.read(buffer)) > 0) {
                sBuilder.append(new String(buffer, 0, count, "UTF-8"));
            }
            return sBuilder.toString().split("\\n");
        } catch (Exception e) {
            throw new Exception(String.format("Can't read config file: %s", path));
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e2) {
                }
            }
        }
    }
    // TODO 未阅读 End

    /**
     * TODO 修改方法名，检查方法内容
     *
     * @throws Exception
     */
    public void loadFromFile(Context context) throws Exception {
        InputStream inputStream = context.getResources().openRawResource(R.raw.config);

        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        inputStream.close();

        loadFromLines(new String(bytes).split("\\r?\\n"));
    }

    // TODO 未阅读 Begin
    public void loadFromUrl(String url) throws Exception {
        String[] lines = null;
        if (url.charAt(0) == '/') {
            lines = readConfigFromFile(url);
        } else {
            lines = downloadConfig(url);
        }
        loadFromLines(lines);
    }
    // TODO 未阅读 End

    /**
     * TODO 代码阅读
     *
     * @param lines
     * @throws Exception
     */
    private void loadFromLines(String[] lines) throws Exception {
        m_IpList.clear();
        m_DnsList.clear();
        m_RouteList.clear();
        m_ProxyList.clear();
        m_DomainMap.clear();

        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            String[] items = line.split("\\s+");
            if (items.length < 2) {
                continue;
            }

            String tagString = items[0].toLowerCase(Locale.ENGLISH).trim();
            try {
                if (!tagString.startsWith("#")) {
                    if (ProxyConfig.IS_DEBUG)
                        LogCat.i(line);

                    if (tagString.equals("ip")) {
                        addIPAddressToList(items, 1, m_IpList); // TODO 在 LocalVpnService 中会设置路由网关 ProxyConfig.FAKE_NETWORK_IP
                    } else if (tagString.equals("dns")) {
                        addIPAddressToList(items, 1, m_DnsList);
                    } else if (tagString.equals("route")) {
                        addIPAddressToList(items, 1, m_RouteList);
                    } else if (tagString.equals("proxy")) {
                        addProxyToList(items, 1);
                    } else if (tagString.equals("direct_domain")) {
                        addDomainToHashMap(items, 1, false);
                    } else if (tagString.equals("proxy_domain")) {
                        addDomainToHashMap(items, 1, true);
                    } else if (tagString.equals("dns_ttl")) {
                        m_dns_ttl = Integer.parseInt(items[1]);
                    } else if (tagString.equals("welcome_info")) {
                        m_welcome_info = line.substring(line.indexOf(" ")).trim(); // TODO 貌似读不进来为空
                    } else if (tagString.equals("session_name")) {
                        m_session_name = items[1];
                    } else if (tagString.equals("user_agent")) {
                        m_user_agent = line.substring(line.indexOf(" ")).trim();
                    } else if (tagString.equals("outside_china_use_proxy")) {
                        m_outside_china_use_proxy = convertToBool(items[1]);
                    } else if (tagString.equals("isolate_http_host_header")) {
                        m_isolate_http_host_header = convertToBool(items[1]);
                    } else if (tagString.equals("mtu")) {
                        m_mtu = Integer.parseInt(items[1]);
                    }
                }
            } catch (Exception e) {
                // TODO Exception 处理
                throw new Exception(String.format("config file parse error: line:%d, tag:%s, error:%s", lineNumber, tagString, e));
            }
        }

        // TODO 去除，查找默认代理。
        if (m_ProxyList.size() == 0) {
            tryAddProxy(lines);
        }
    }

    private void addIPAddressToList(String[] items, int offset, ArrayList<IPAddress> list) {
        for (int i = offset; i < items.length; i++) {
            String item = items[i].trim().toLowerCase();
            if (item.startsWith("#")) {
                break;
            } else {
                IPAddress ip = new IPAddress(item);
                if (!list.contains(ip)) {
                    list.add(ip);
                }
            }
        }
    }

    /**
     * TODO 不需要 HTTP 代理，public 改为 private
     *
     * @param proxyString
     * @throws Exception
     */
    public void addProxyToList(String proxyString) throws Exception {
        Config config = null;
        if (proxyString == null || !proxyString.startsWith("ss://")) {
            // TODO 巨大的 BUG
            LogCat.printStackTrace(new Exception("proxyString.startsWith(\"ss://\") is not : " + proxyString));
            proxyString = ProxyConstants.URL;
        }

        if (proxyString.startsWith("ss://")) {
            config = ShadowsocksConfig.parse(proxyString);
        } else {
            // TODO 不需要 HTTP 代理 Begin
            LogCat.printStackTrace(new Exception("proxyString.startsWith(\"ss://\") is not : " + proxyString));

            if (!proxyString.toLowerCase().startsWith("http://")) {
                proxyString = "http://" + proxyString;
            }
            config = HttpConnectConfig.parse(proxyString);
            // TODO 不需要 HTTP 代理 End
        }

        if (!m_ProxyList.contains(config)) {
            m_ProxyList.add(config);
            m_DomainMap.put(config.ServerAddress.getHostName(), false);
        }
    }

    private void addProxyToList(String[] items, int offset) throws Exception {
        for (int i = offset; i < items.length; i++) {
            addProxyToList(items[i].trim());
        }
    }

    private void addDomainToHashMap(String[] items, int offset, Boolean state) {
        for (int i = offset; i < items.length; i++) {
            String domainString = items[i].toLowerCase().trim();
            if (domainString.charAt(0) == '.') {
                domainString = domainString.substring(1);
            }
            m_DomainMap.put(domainString, state);
        }
    }

    private boolean convertToBool(String valueString) {
        if (valueString == null || valueString.isEmpty())
            return false;
        valueString = valueString.toLowerCase(Locale.ENGLISH).trim();
        if (valueString.equals("on") || valueString.equals("1") || valueString.equals("true") || valueString.equals("yes")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * TODO 去除，不存在查找默认代理的情况，貌似是 HTTP 代理
     *
     * @param lines
     */
    private void tryAddProxy(String[] lines) {
        for (String line : lines) {
            Pattern p = Pattern.compile("proxy\\s+([^:]+):(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(line);
            while (m.find()) {
                // TODO 严重，注释的部分可能导致无法使用
                HttpConnectConfig config = new HttpConnectConfig();
                config.ServerAddress = new InetSocketAddress(m.group(1), Integer.parseInt(m.group(2)));
                if (!m_ProxyList.contains(config)) {
                    m_ProxyList.add(config);
                    m_DomainMap.put(config.ServerAddress.getHostName(), false);
                }
            }
        }
    }
}
