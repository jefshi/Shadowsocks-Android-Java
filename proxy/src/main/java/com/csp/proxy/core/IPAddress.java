package com.csp.proxy.core;

public class IPAddress {
    public final String Address; // 修改存储类型
    public final int PrefixLength;

    public IPAddress(String address, int prefixLength) {
        this.Address = address;
        this.PrefixLength = prefixLength;
    }

    public IPAddress(String ipAddresString) {
        String[] arrStrings = ipAddresString.split("/");
        String address = arrStrings[0];
        int prefixLength = 32;
        if (arrStrings.length > 1) {
            prefixLength = Integer.parseInt(arrStrings[1]);
        }
        this.Address = address;
        this.PrefixLength = prefixLength;
    }

    @Override
    public String toString() {
        return String.format("%s/%d", Address, PrefixLength); // TODO 修改 toString() 方法
    }

    // TODO 修改 equals 方法
    @Override
    public boolean equals(Object o) {
        return o != null && toString().equals(o.toString());
    }
}