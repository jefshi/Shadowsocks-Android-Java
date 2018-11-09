package com.csp.proxy.core;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;

import com.csp.proxy.constants.ProxyConstants;
import com.csp.proxy.R;
import com.csp.proxy.core.config.IPAddress;
import com.csp.proxy.core.config.ProxyConfig;
import com.csp.proxy.dns.DnsPacket;
import com.csp.proxy.dns.DnsProxy;
import com.csp.proxy.tcpip.CommonMethods;
import com.csp.proxy.tcpip.HttpHostHeaderParser;
import com.csp.proxy.tcpip.IPHeader;
import com.csp.proxy.tcpip.NatSession;
import com.csp.proxy.tcpip.NatSessionManager;
import com.csp.proxy.tcpip.TCPHeader;
import com.csp.proxy.tcpip.TcpProxyServer;
import com.csp.proxy.tcpip.UDPHeader;
import com.csp.utillib.AppInfoUtils;
import com.csp.utillib.LogCat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocalVpnService extends VpnService implements Runnable {
    private final static boolean IS_DEBUG = ProxyConstants.LOG_DEBUG;
    public static LocalVpnService Instance;
    private static String proxyUrl; // 代理配置
    private static boolean running = false;

    private static int LOCAL_IP; // 本地 IP

    private ParcelFileDescriptor m_VPNInterface;
    private FileOutputStream m_VPNOutputStream;


    private TcpProxyServer m_TcpProxyServer;
    private DnsProxy m_DnsProxy;

    private final ProxyConfig proxyConfig = ProxyConfig.getInstance();

    public static boolean proxy_app_add = false;
    public static boolean proxy_app_remove = false;

    public static ProxyState sProxyState;


    // TODO IP 数据报处理
    private byte[] m_Packet; // TODO 读取的 IP 数据报存放地
    private IPHeader m_IPHeader;
    private TCPHeader m_TCPHeader;

    private Handler m_Handler;

    // TODO 未阅读 Begin
    private Thread m_VPNThread;
    private UDPHeader m_UDPHeader;
    private ByteBuffer m_DNSBuffer;
    private long m_SentBytes;
    private long m_ReceivedBytes;
    // TODO 未阅读 End

    private ProxyManagerImpl proxyManager;

    public static boolean isRunning() {
        return running;
    }

    public static void setRunning(boolean running) {
        LocalVpnService.running = running;
    }

    public static String getProxyUrl() {
        return proxyUrl;
    }

    public static void setProxyUrl(String proxyUrl) {
        LocalVpnService.proxyUrl = proxyUrl;
    }

    public LocalVpnService() {
        m_Handler = new Handler();
        m_Packet = new byte[20000]; // 长度定义
        m_IPHeader = new IPHeader(m_Packet, 0);
        m_TCPHeader = new TCPHeader(m_Packet, 20);
        Instance = this;

        proxyManager = (ProxyManagerImpl) ProxyServer.getProxyManager(this);

        // TODO 未阅读 Begin
        m_UDPHeader = new UDPHeader(m_Packet, 20);
        m_DNSBuffer = ((ByteBuffer) ByteBuffer.wrap(m_Packet).position(28)).slice();
        // TODO 未阅读 End

        sProxyState = ProxyState.STATE_DISCONNECTED;
    }

    @Override
    public void onCreate() {
        // Start a new session by creating a new thread.
        m_VPNThread = new Thread(this, "VPNServiceThread");
        m_VPNThread.start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        LogCat.e("VPNService.onDestroy()");
        // TODO 未阅读 Begin 作用未知
        if (m_VPNThread != null) {
            m_VPNThread.interrupt();
        }
        // TODO 未阅读 End

        onStatusChanged(ProxyState.STATE_DISCONNECTED);
        proxyManager.getAppManager().clearProxyApps();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * TODO 修改参数
     */
    public void onStatusChanged(final ProxyState state) {
        if (state.getCode() != ProxyState.CODE_LOG) {
            sProxyState = state;
        }
        m_Handler.post(new Runnable() {
            @Override
            public void run() {
                proxyManager.getObserverable().onStatusChanged(state);
            }
        });
    }

    // TODO 未阅读 Begin
    public void sendUDPPacket(IPHeader ipHeader, UDPHeader udpHeader) {
        try {
            CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader);
            this.m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, ipHeader.getTotalLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // TODO 未阅读 End


    private String getAppInstallID() {
        SharedPreferences preferences = getSharedPreferences("SmartProxy", MODE_PRIVATE);
        String appInstallID = preferences.getString("AppInstallID", null);
        if (appInstallID == null || appInstallID.isEmpty()) {
            appInstallID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("AppInstallID", appInstallID);
            editor.apply();
        }
        return appInstallID;
    }

    private String getVersionName() {
        String versionName = AppInfoUtils.getVersionName();
        return versionName == null ? "0.0" : versionName;
    }

    @Override
    public synchronized void run() {
        try {
            onStatusChanged(new ProxyState("VPNService work thread is runing..."));

            ProxyConfig.AppInstallID = getAppInstallID(); // TODO 获取安装ID，移动到 ProxyConfig 自身中
            ProxyConfig.AppVersion = getVersionName(); // TODO 获取版本号，移动到 ProxyConfig 自身中
            System.out.printf("AppInstallID: %s\n", ProxyConfig.AppInstallID); // TODO 作用未知
            onStatusChanged(new ProxyState("Android version: %s", Build.VERSION.RELEASE));
            onStatusChanged(new ProxyState("App version: %s", ProxyConfig.AppVersion));

            ChinaIpMaskManager.loadFromFile(this);
            waitUntilPreapred(); // TODO VpnService#prepare()

            onStatusChanged(new ProxyState("Load config from file ..."));
            try {
                proxyConfig.loadFromFile(this);
                onStatusChanged(new ProxyState("Load done"));
            } catch (Exception e) {
                String errString = e.getMessage();
                if (errString == null || errString.isEmpty()) {
                    errString = e.toString();
                }
                onStatusChanged(new ProxyState("Load failed with error: %s", errString));
            }

            // TODO 本地 Sock5 代理服务器启动
            m_TcpProxyServer = new TcpProxyServer(0);
            m_TcpProxyServer.start();
            onStatusChanged(new ProxyState("LocalTcpServer started."));

            // TODO ？？？
            m_DnsProxy = new DnsProxy();
            m_DnsProxy.start();
            onStatusChanged(new ProxyState("LocalDnsProxy started."));

            // TODO 不进行 prepare() ？？？ 貌似不是，监听，并读写虚拟网卡 IP 数据包
            while (true) {
                if (running) {
                    //加载配置文件

                    onStatusChanged(new ProxyState("set shadowsocks/(http proxy)"));
                    try {
                        proxyConfig.m_ProxyList.clear();
                        proxyConfig.addProxyToList(proxyUrl); // TODO 调整位置
                        onStatusChanged(new ProxyState("Proxy is: %s", proxyConfig.getDefaultProxy().toString()));
                    } catch (Exception e) {
                        String errString = e.getMessage();
                        if (errString == null || errString.isEmpty()) {
                            errString = e.toString();
                        }
                        running = false;
                        ProxyState.STATE_EXCEPTION.setException(e);
                        onStatusChanged(ProxyState.STATE_EXCEPTION);
                        LogCat.printStackTrace(e);
                        continue;
                    }

                    String welcomeInfoString = proxyConfig.getWelcomeInfo();
                    if (welcomeInfoString != null && !welcomeInfoString.isEmpty()) {
                        onStatusChanged(new ProxyState("%s", proxyConfig.getWelcomeInfo()));
                    }
                    onStatusChanged(new ProxyState("Global mode is " + (proxyConfig.isGlobalMode() ? "on" : "off")));

                    runVPN();
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (Exception e) {
            LogCat.printStackTrace(e);
            onStatusChanged(new ProxyState("Fatal error: %s", e.toString()));
        } finally {
            onStatusChanged(new ProxyState("App terminated."));
            dispose();
        }

        LogCat.e("VPNService.run() end");
    }

    /**
     * TODO Proxy 部分核心处理 Begin
     */

    /**
     * TODO 运行 VpnService 的核心
     *
     * @throws Exception
     */
    private void runVPN() throws Exception {
        m_VPNInterface = establishVPN();
        m_VPNOutputStream = new FileOutputStream(m_VPNInterface.getFileDescriptor());
        FileInputStream in = new FileInputStream(m_VPNInterface.getFileDescriptor());
        int size = 0;
        while (size != -1 && running) {
            while ((size = in.read(m_Packet)) > 0 && running) {
                if (m_DnsProxy.Stopped || m_TcpProxyServer.Stopped) {
                    in.close();
                    throw new Exception("LocalServer stopped.");
                }
                onIPPacketReceived(m_IPHeader, size);
            }
            Thread.sleep(20);
        }
        in.close();
        disconnectVPN();
    }

    /**
     * TODO IP 数据报接受处理，提取
     */
    void onIPPacketReceived(IPHeader ipHeader, int size) throws IOException {
        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                // 过滤非请求 IP 数据报
                if (ipHeader.getSourceIP() != LOCAL_IP)
                    break;

                TCPHeader tcpHeader = m_TCPHeader;
                tcpHeader.m_Offset = ipHeader.getHeaderLength();
                if (tcpHeader.getSourcePort() == m_TcpProxyServer.Port) {// 收到本地TCP服务器数据
                    NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
                    if (session != null) {
                        ipHeader.setSourceIP(ipHeader.getDestinationIP());
                        tcpHeader.setSourcePort(session.RemotePort);
                        ipHeader.setDestinationIP(LOCAL_IP);

                        CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                        m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                        m_ReceivedBytes += size;
                    } else {
                        LogCat.i("NoSession: " + ipHeader.toString() + ' ' + tcpHeader.toString());
                    }
                } else {
                    // TODO TCPHeader 内容为空处理

                    // 添加 NAPT 端口映射
                    int portKey = tcpHeader.getSourcePort();
                    NatSession session = NatSessionManager.getSession(portKey);
                    if (session == null || session.RemoteIP != ipHeader.getDestinationIP() || session.RemotePort != tcpHeader.getDestinationPort()) {
                        session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader.getDestinationPort());
                    }

                    session.LastNanoTime = System.nanoTime();
                    session.PacketSent++; // TODO (不明白)，注意顺序

                    int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
                    if (session.PacketSent == 2 && tcpDataSize == 0) {
                        return; // TODO 丢弃tcp握手的第二个ACK报文。因为客户端发数据的时候也会带上ACK，这样可以在服务器Accept之前分析出HOST信息。
                    }

                    // 分析数据，找到host
                    if (session.BytesSent == 0 && tcpDataSize > 10) {
                        int dataOffset = tcpHeader.m_Offset + tcpHeader.getHeaderLength(); // TODO ？？？
                        String host = HttpHostHeaderParser.parseHost(tcpHeader.m_Data, dataOffset, tcpDataSize);
                        if (host != null) {
                            session.RemoteHost = host;
                        } else {
                            LogCat.i("No host name found: " + session.RemoteHost);
                        }
                    }

                    // 转发给本地TCP服务器
                    ipHeader.setSourceIP(ipHeader.getDestinationIP());
                    ipHeader.setDestinationIP(LOCAL_IP);
                    tcpHeader.setDestinationPort(m_TcpProxyServer.Port);

                    CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                    m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                    session.BytesSent += tcpDataSize;//注意顺序
                    m_SentBytes += size;
                }
                break;
            case IPHeader.UDP:
                // TODO 代码阅读，转发DNS数据包：
                UDPHeader udpHeader = m_UDPHeader;
                udpHeader.m_Offset = ipHeader.getHeaderLength();
                if (ipHeader.getSourceIP() == LOCAL_IP && udpHeader.getDestinationPort() == 53) {
                    m_DNSBuffer.clear();
                    m_DNSBuffer.limit(ipHeader.getDataLength() - 8);
                    DnsPacket dnsPacket = DnsPacket.FromBytes(m_DNSBuffer);
                    if (dnsPacket != null && dnsPacket.Header.QuestionCount > 0) {
                        m_DnsProxy.onDnsRequestReceived(ipHeader, udpHeader, dnsPacket);
                    }
                }
                break;
        }
    }


    /**
     * TODO Proxy VPNService 部分核心处理 End
     */


    /**
     * TODO VpnService 本身使用 Begin
     */

    /**
     * 检查是否准备完毕。
     * TODO 调研 {@link VpnService#prepare(Context)}
     */
    private void waitUntilPreapred() {
        while (prepare(this) != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LogCat.printStackTrace(e);
            }
        }
    }

    /**
     * TODO 提取为 Builder
     *
     * @return
     * @throws Exception
     */
    private ParcelFileDescriptor establishVPN() throws Exception {
        Builder builder = new Builder();
        builder.setMtu(proxyConfig.getMTU());
        if (IS_DEBUG)
            LogCat.i("setMtu: " + proxyConfig.getMTU());

        IPAddress ipAddress = proxyConfig.getDefaultLocalIP();
        LOCAL_IP = CommonMethods.ipStringToInt(ipAddress.Address);
        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength);
        if (IS_DEBUG)
            LogCat.i("setMtu: " + ipAddress.Address + '/' + ipAddress.PrefixLength);

        for (IPAddress dns : proxyConfig.getDnsList()) {
            builder.addDnsServer(dns.Address);
            if (IS_DEBUG)
                LogCat.i("addDnsServer: " + dns.Address);
        }

        if (proxyConfig.getRouteList().size() > 0) {
            for (IPAddress routeAddress : proxyConfig.getRouteList()) {
                builder.addRoute(routeAddress.Address, routeAddress.PrefixLength);
                if (IS_DEBUG)
                    LogCat.i("addRoute: " + routeAddress.Address + '/' + routeAddress.PrefixLength);
            }
            builder.addRoute(CommonMethods.ipIntToString(ProxyConfig.FAKE_NETWORK_IP), 16);

            if (IS_DEBUG)
                LogCat.i("addRoute: " + CommonMethods.ipIntToString(ProxyConfig.FAKE_NETWORK_IP) + '/' + 16);
        } else {
            // TODO 应该有 ProxyConfig 完成
            builder.addRoute("0.0.0.0", 0);
            if (IS_DEBUG)
                LogCat.i("addDefaultRoute: 0.0.0.0/0");
        }

        // TODO SystemProperties，系统设置，获取"net.dns1", "net.dns2", "net.dns3", "net.dns4"
        Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
        Method method = SystemProperties.getMethod("get", new Class[]{String.class});
        ArrayList<String> servers = new ArrayList<>();
        for (String name : new String[]{"net.dns1", "net.dns2", "net.dns3", "net.dns4",}) {
            String value = (String) method.invoke(null, name);
            if (value != null && !"".equals(value) && !servers.contains(value)) {
                servers.add(value);
                if (value.replaceAll("\\d", "").length() == 3) {//防止IPv6地址导致问题
                    builder.addRoute(value, 32);
                } else {
                    builder.addRoute(value, 128);
                }
                if (IS_DEBUG)
                    LogCat.i(name + '=' + value);
            }
        }

        // TODO AppManager.isLollipopOrAbove
        // TODO 允许的使用代理的应用
        List<ProxyApp> proxyApps = proxyManager.getAppManager().getProxyApps();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (proxyApps.size() == 0) {
                onStatusChanged(new ProxyState("Proxy none Apps"));
            }
            builder.addAllowedApplication(getPackageName());// TODO "com.vm.shadowsocks"，需要把自己加入代理，不然会无法进行网络连接
            builder.addAllowedApplication("com.google.android.gms");
            builder.addAllowedApplication("com.google.android.gsf");
            for (ProxyApp app : proxyApps) {
                if (proxyConfig.isMultipointMode() && EmptyUtil.isBank(app.getProxyUrl())) {
                    onStatusChanged(new ProxyState("Proxy App(camouflage): " + app.getPackageName()));
                    continue;
                }

                try {
                    builder.addAllowedApplication(app.getPackageName());
                    onStatusChanged(new ProxyState("Proxy App: " + app.getPackageName()));
                } catch (Exception e) {
                    e.printStackTrace();
                    onStatusChanged(new ProxyState("Proxy App Fail: " + app.getPackageName()));
                }
            }
            // onStatusChanged(ProxyManager.PROXY_APPS_ALLOWED, null);
        } else {
            onStatusChanged(new ProxyState("No Pre-App proxy, due to low Android version."));
        }

        // TODO ？？？
//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        builder.setConfigureIntent(pendingIntent);

        builder.setSession(proxyConfig.getSessionName()); // TODO 更改 VpnService 配置名称
        ParcelFileDescriptor pfdDescriptor = builder.establish();

        if (proxy_app_add) {
            proxy_app_add = false;
            onStatusChanged(ProxyState.STATE_APP_PROXY_ADD);
        } else if (proxy_app_remove) {
            proxy_app_remove = false;
            onStatusChanged(ProxyState.STATE_APP_PROXY_REMOVE);
        } else {
            onStatusChanged(ProxyState.STATE_CONNECTED);
            if (proxyApps.size() > 0)
                onStatusChanged(ProxyState.STATE_APP_PROXY_ADD);
        }

        return pfdDescriptor;
    }

    public void disconnectVPN() {
        try {
            if (m_VPNInterface != null) {
                m_VPNInterface.close();
                m_VPNInterface = null;
            }
        } catch (Exception e) {
            LogCat.printStackTrace(e);
        }

        if (proxyManager.getAppManager().getProxyApps().isEmpty()
                && proxy_app_remove) {
            proxy_app_remove = false;
            onStatusChanged(ProxyState.STATE_APP_PROXY_REMOVE);
        }

        if (!(proxy_app_add | proxy_app_remove)) {
            onStatusChanged(ProxyState.STATE_DISCONNECTED);
            proxyManager.getAppManager().clearProxyApps();
        }

        if (m_VPNOutputStream != null)
            try {
                m_VPNOutputStream.close();
            } catch (IOException e) {
                LogCat.printStackTrace(e);
            }
        this.m_VPNOutputStream = null;
    }

    /**
     * TODO VpnService 本身使用 End
     */


    /**
     * TODO 彻底的停止整个应用
     */
    private synchronized void dispose() {
        // 断开VPN
        disconnectVPN();

        // 停止TcpServer
        if (m_TcpProxyServer != null) {
            m_TcpProxyServer.stop();
            m_TcpProxyServer = null;
            onStatusChanged(new ProxyState("LocalTcpServer stopped."));
        }

        // 停止DNS解析器
        if (m_DnsProxy != null) {
            m_DnsProxy.stop();
            m_DnsProxy = null;
            onStatusChanged(new ProxyState("LocalDnsProxy stopped."));
        }

        stopSelf();
        running = false;
        System.exit(0);
    }

}
