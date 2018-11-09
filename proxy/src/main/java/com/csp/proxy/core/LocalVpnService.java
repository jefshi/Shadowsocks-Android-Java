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
    private final boolean IS_DEBUG = ProxyConstants.LOG_DEBUG;

    public static LocalVpnService Instance;
    private static String proxyUrl;
    private static boolean running = false;

    private static int ID;
    private static int LOCAL_IP;
    private static ConcurrentHashMap<onStatusChangedListener, Object> m_OnStatusChangedListeners = new ConcurrentHashMap<onStatusChangedListener, Object>();

    private Thread m_VPNThread;
    private ParcelFileDescriptor m_VPNInterface;
    private TcpProxyServer m_TcpProxyServer;
    private DnsProxy m_DnsProxy;
    private FileOutputStream m_VPNOutputStream;

    private byte[] m_Packet;
    private IPHeader m_IPHeader;
    private TCPHeader m_TCPHeader;
    private UDPHeader m_UDPHeader;
    private ByteBuffer m_DNSBuffer;
    private Handler m_Handler;
    private long m_SentBytes;
    private long m_ReceivedBytes;

    public static ProxyState sProxyState;
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
        ID++;
        m_Handler = new Handler();
        m_Packet = new byte[20000];
        m_IPHeader = new IPHeader(m_Packet, 0);
        m_TCPHeader = new TCPHeader(m_Packet, 20);

        proxyManager = (ProxyManagerImpl) ProxyServer.getProxyManager(this);

        // TODO 未阅读 Begin
        m_UDPHeader = new UDPHeader(m_Packet, 20);
        m_DNSBuffer = ((ByteBuffer) ByteBuffer.wrap(m_Packet).position(28)).slice();
        // TODO 未阅读 End

        sProxyState = ProxyState.STATE_DISCONNECTED;

        Instance = this;

        System.out.printf("New VPNService(%d)\n", ID);
    }

    @Override
    public void onCreate() {
        System.out.printf("VPNService(%s) created.\n", ID);
        // Start a new session by creating a new thread.
        m_VPNThread = new Thread(this, "VPNServiceThread");
        m_VPNThread.start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        LogCat.e("VPNService(%s) destoried.");
        System.out.printf("VPNService(%s) destoried.\n", ID);
        if (m_VPNThread != null) {
            m_VPNThread.interrupt();
        }

        onStatusChanged(ProxyState.STATE_DISCONNECTED);
        AppManager.getInstance().clearProxyApps();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        return super.onStartCommand(intent, flags, startId);
    }

    public interface onStatusChangedListener {
        public void onStatusChanged(String status, Boolean isRunning);

        public void onLogReceived(String logString);
    }

    public static void addOnStatusChangedListener(onStatusChangedListener listener) {
        if (!m_OnStatusChangedListeners.containsKey(listener)) {
            m_OnStatusChangedListeners.put(listener, 1);
        }
    }

    public static void removeOnStatusChangedListener(onStatusChangedListener listener) {
        if (m_OnStatusChangedListeners.containsKey(listener)) {
            m_OnStatusChangedListeners.remove(listener);
        }
    }

    private void onStatusChanged(final String status, final boolean isRunning) {
        m_Handler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : m_OnStatusChangedListeners.entrySet()) {
                    entry.getKey().onStatusChanged(status, isRunning);
                }
            }
        });
    }

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

    public void writeLog(final String format, Object... args) {
        final String logString = String.format(format, args);
        m_Handler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : m_OnStatusChangedListeners.entrySet()) {
                    entry.getKey().onLogReceived(logString);
                }
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
            Editor editor = preferences.edit();
            editor.putString("AppInstallID", appInstallID);
            editor.apply();
        }
        return appInstallID;
    }

//    String getVersionName() {
//        try {
//            PackageManager packageManager = getPackageManager();
//            // getPackageName()是你当前类的包名，0代表是获取版本信息
//            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
//            String version = packInfo.versionName;
//            return version;
//        } catch (Exception e) {
//            return "0.0";
//        }
//    }

    private String getVersionName() {
        String versionName = AppInfoUtils.getVersionName(this);
        return versionName == null ? "0.0" : versionName;
    }

    @Override
    public synchronized void run() {
        try {
            System.out.printf("VPNService(%s) work thread is runing...\n", ID);

            ProxyConfig.AppInstallID = getAppInstallID();//获取安装ID
            ProxyConfig.AppVersion = getVersionName();//获取版本号
            System.out.printf("AppInstallID: %s\n", ProxyConfig.AppInstallID);
            writeLog("Android version: %s", Build.VERSION.RELEASE);
            writeLog("App version: %s", ProxyConfig.AppVersion);


            ChinaIpMaskManager.loadFromFile(this);//加载中国的IP段，用于IP分流。
            waitUntilPreapred();//检查是否准备完毕。

            writeLog("Load config from file ...");
            try {
                ProxyConfig.getInstance().loadFromFile(this);
                writeLog("Load done");
            } catch (Exception e) {
                String errString = e.getMessage();
                if (errString == null || errString.isEmpty()) {
                    errString = e.toString();
                }
                writeLog("Load failed with error: %s", errString);
            }

            m_TcpProxyServer = new TcpProxyServer(0);
            m_TcpProxyServer.start();
            writeLog("LocalTcpServer started.");

            m_DnsProxy = new DnsProxy();
            m_DnsProxy.start();
            writeLog("LocalDnsProxy started.");

            while (true) {
                if (running) {
                    //加载配置文件

                    writeLog("set shadowsocks/(http proxy)");
                    try {
                        ProxyConfig.getInstance().m_ProxyList.clear();
                        ProxyConfig.getInstance().addProxyToList(proxyUrl);
                        writeLog("Proxy is: %s", ProxyConfig.getInstance().getDefaultProxy());
                    } catch (Exception e) {
                        ;
                        String errString = e.getMessage();
                        if (errString == null || errString.isEmpty()) {
                            errString = e.toString();
                        }
                        running = false;
                        onStatusChanged(errString, false);
                        continue;
                    }
                    String welcomeInfoString = ProxyConfig.getInstance().getWelcomeInfo();
                    if (welcomeInfoString != null && !welcomeInfoString.isEmpty()) {
                        writeLog("%s", ProxyConfig.getInstance().getWelcomeInfo());
                    }
                    writeLog("Global mode is " + (ProxyConfig.getInstance().isGlobalMode() ? "on" : "off"));

                    runVPN();
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        } catch (Exception e) {
            e.printStackTrace();
            writeLog("Fatal error: %s", e.toString());
        } finally {
            writeLog("App terminated.");
            dispose();
        }
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
                // if (ipHeader.getSourceIP() == LOCAL_IP) {
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

                    //分析数据，找到host
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
                    // }
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
        builder.setMtu(ProxyConfig.getInstance().getMTU());
        if (IS_DEBUG)
            LogCat.i("setMtu: " + ProxyConfig.getInstance().getMTU());

        IPAddress ipAddress = ProxyConfig.getInstance().getDefaultLocalIP();
        LOCAL_IP = CommonMethods.ipStringToInt(ipAddress.Address);
        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength);
        if (IS_DEBUG)
            LogCat.i("setMtu: " + ipAddress.Address + '/' + ipAddress.PrefixLength);

        for (IPAddress dns : ProxyConfig.getInstance().getDnsList()) {
            builder.addDnsServer(dns.Address);
            if (IS_DEBUG)
                LogCat.i("addDnsServer: " + dns.Address);
        }

        if (ProxyConfig.getInstance().getRouteList().size() > 0) {
            for (IPAddress routeAddress : ProxyConfig.getInstance().getRouteList()) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (AppManager.getInstance().getProxyApps().size() == 0) {
                writeLog("Proxy All Apps");
            }
            for (ProxyApp app : AppManager.getInstance().getProxyApps()) {
                builder.addAllowedApplication("com.vm.shadowsocks");//需要把自己加入代理，不然会无法进行网络连接
                try {
                    builder.addAllowedApplication(app.getPackageName());
                    writeLog("Proxy App: " + app.getPackageName());
                } catch (Exception e) {
                    e.printStackTrace();
                    writeLog("Proxy App Fail: " + app.getPackageName());
                }
            }
        } else {
            writeLog("No Pre-App proxy, due to low Android version.");
        }

//        Intent intent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        builder.setConfigureIntent(pendingIntent);

        builder.setSession(ProxyConfig.getInstance().getSessionName());
        ParcelFileDescriptor pfdDescriptor = builder.establish();
        onStatusChanged(ProxyConfig.getInstance().getSessionName() + getString(R.string.vpn_connected_status), true);
        return pfdDescriptor;
    }

    public void disconnectVPN() {
        try {
            if (m_VPNInterface != null) {
                m_VPNInterface.close();
                m_VPNInterface = null;
            }
        } catch (Exception e) {
            // ignore
        }
        onStatusChanged(ProxyConfig.getInstance().getSessionName() + getString(R.string.vpn_disconnected_status), false);
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
