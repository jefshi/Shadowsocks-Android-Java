package com.csp.proxy.core;

import android.content.Context;
import android.util.SparseIntArray;

import com.csp.proxy.ProxyConstants;
import com.csp.proxy.R;
import com.csp.proxy.tcpip.CommonMethods;
import com.csp.utillib.LogCat;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO 可能非核心内容，中国的IP段，用于IP分流。
 */
public class ChinaIpMaskManager {
    final static boolean DEBUG = ProxyConstants.LOG_DEBUG;

    static SparseIntArray ChinaIpMaskDict = new SparseIntArray(3000); // TODO ？？？
    static SparseIntArray MaskDict = new SparseIntArray(); // TODO ？？？

    /**
     * TODO ？？？
     */
    public static boolean isIPInChina(int ip) {
        boolean found = false;
        for (int i = 0; i < MaskDict.size(); i++) {
            int mask = MaskDict.keyAt(i);
            int networkIP = ip & mask;
            int mask2 = ChinaIpMaskDict.get(networkIP);
            if (mask2 == mask) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * TODO 修改方法名，加载中国的IP段，用于IP分流。
     */
    public static void loadFromFile(Context context) {
        int count;
        byte[] buffer = new byte[4096]; // TODO 4096
        InputStream inputStream = context.getResources().openRawResource(R.raw.ipmask);
        try {
            while ((count = inputStream.read(buffer)) > 0) {
                // TODO 每八位读一次
                for (int i = 0; i < count; i += 8) {
                    int ip = CommonMethods.readInt(buffer, i);
                    int mask = CommonMethods.readInt(buffer, i + 4);
                    ChinaIpMaskDict.put(ip, mask);
                    MaskDict.put(mask, mask);
                    if (DEBUG)
                        LogCat.i(CommonMethods.ipIntToString(ip) + '/' + CommonMethods.ipIntToString(mask));
                }
            }
        } catch (IOException e) {
            LogCat.printStackTrace(e);
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogCat.printStackTrace(e);
                }
        }
        if (DEBUG)
            LogCat.i("ChinaIpMask records count: " + ChinaIpMaskDict.size());
    }
}
