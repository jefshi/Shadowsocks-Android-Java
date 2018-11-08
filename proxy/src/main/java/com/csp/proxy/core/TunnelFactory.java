package com.csp.proxy.core;

import com.csp.proxy.tunnel.httpconnect.HttpConnectTunnel;
import com.csp.proxy.tunnel.shadowsocks.ShadowsocksTunnel;
import com.csp.proxy.tunnel.Config;
import com.csp.proxy.tunnel.RawTunnel;
import com.csp.proxy.tunnel.Tunnel;
import com.csp.proxy.tunnel.httpconnect.HttpConnectConfig;
import com.csp.proxy.tunnel.shadowsocks.ShadowsocksConfig;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TunnelFactory {

    public static Tunnel wrap(SocketChannel channel, Selector selector) {
        return new RawTunnel(channel, selector);
    }

    public static Tunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector) throws Exception {
        if (destAddress.isUnresolved()) {
            Config config = ProxyConfig.getInstance().getDefaultTunnelConfig(destAddress);
            if (config instanceof HttpConnectConfig) {
                return new HttpConnectTunnel((HttpConnectConfig) config, selector);
            } else if (config instanceof ShadowsocksConfig) {
                return new ShadowsocksTunnel((ShadowsocksConfig) config, selector);
            }
            throw new Exception("The config is unknow.");
        } else {
            return new RawTunnel(destAddress, selector);
        }
    }

}
