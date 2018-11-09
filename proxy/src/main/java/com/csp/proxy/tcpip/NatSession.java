package com.csp.proxy.tcpip;

public class NatSession {
    public int RemoteIP; // 目的 IP
    public short RemotePort; // 目的 Port
    public String RemoteHost;
    public int BytesSent;
    public int PacketSent; // 发包次数
    public long LastNanoTime; // NAPT 最近使用时间
}
