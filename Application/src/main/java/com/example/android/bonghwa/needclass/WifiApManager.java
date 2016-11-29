package com.example.android.bonghwa.needclass;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016-02-19.
 */
public class WifiApManager {

    public static final int WIFI_AP_STATE_FAILED = 4;

    private final WifiManager mWifiManager;

    public WifiApManager(Context context) {
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
    }

    public boolean setWifiApEnabled(WifiConfiguration config,
                                    boolean enabled) {

        try {
            if (enabled) { // disable WiFi in any case
                mWifiManager.setWifiEnabled(false);
            }

            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class,
                    boolean.class);
            return (Boolean) method.invoke(mWifiManager, config, enabled);
        } catch (Exception e) {
            return false;
        }
    }

    public int getWifiApState() {
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "getWifiApState");
            return (Integer) method.invoke(mWifiManager);
        } catch (Exception e) {
            return WIFI_AP_STATE_FAILED;
        }
    }
}


