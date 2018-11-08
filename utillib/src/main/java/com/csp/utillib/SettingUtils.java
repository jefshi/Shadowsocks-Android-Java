package com.csp.utillib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;

/**
 * Description: 网络工具类
 * <p>Create Date: 2018/04/13
 * <p>Modify Date: 无
 *
 * @author csp
 * @version 1.0.0
 * @since AndroidUtils 1.0.0
 */
@SuppressWarnings("unused")
public class SettingUtils {
    /**
     * 跳转到相应界面
     *
     * @param context     context
     * @param intent      intent
     * @param requestCode 请求码
     */
    private static void start(Context context, Intent intent, int requestCode) {
        if (context instanceof Activity)
            ((Activity) context).startActivityForResult(intent, requestCode);
        else
            context.startActivity(intent);
    }

    /**
     * 跳转到 Wifi 设置界面
     *
     * @see #start(Context, Intent, int)
     */
    @SuppressLint("ObsoleteSdkInt")
    public static void startWifiSetting(@NonNull Context context, int requestCode) {
        Intent intent = new Intent();

        if (Build.VERSION.SDK_INT > 10)
            intent.setAction(Settings.ACTION_WIFI_SETTINGS);
        else
            intent.setClassName("com.android.settings", Settings.ACTION_WIFI_SETTINGS);

        if (context instanceof Application) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        start(context, intent, requestCode);
    }

    /**
     * 跳转到应用信息界面
     *
     * @see #start(Context, Intent, int)
     */
    public static void startAppInformationSetting(@NonNull Context context, int requestCode) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        start(context, intent, requestCode);
    }

    /**
     * 跳转到悬浮窗权限设置界面
     *
     * @see #start(Context, Intent, int)
     */
    public static void startFloatingPermissionSetting(@NonNull Context context, int requestCode) {
        String action = Build.VERSION.SDK_INT >= 23
                ? Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                : Settings.ACTION_APPLICATION_DETAILS_SETTINGS;

        Intent intent = new Intent();
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.setAction(action);

        start(context, intent, requestCode);
    }

    /**
     * 跳转到 VPN 设置界面
     *
     * @see #start(Context, Intent, int)
     */
    public static void startVpnSetting(@NonNull Context context, int requestCode) {
        Intent intent = new Intent();
        intent.setAction("android.net.vpn.SETTINGS");
        start(context, intent, requestCode);
    }
}
