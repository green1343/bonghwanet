package com.example.android.bonghwa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.example.android.bonghwa.needclass.MyThread;
import com.example.android.bonghwa.needclass.WifiApManager;

import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by sheep on 2016-11-26.
 */

public enum Device {
    INSTANCE;

    Context m_context;

    public final static String RESERVED_SSID = "bhn";
    public final static String DEFAULT_MYNAME = "Me";
    public final static String EMERGENCY_SSID = "emergencybhn";

    public final static long EMERGENCY = -1;

    String m_networkPass = "dafsglokvogzsuiwhbejfgr";

    String m_curBSSID = new String();

    WifiConfiguration m_configuration;
    WifiApManager m_wifiApManager;
    WifiManager m_wifiManager;

    public WifiManager getWifiManager(){
        return m_wifiManager;
    }

    public void init(Context context)
    {
        m_context = context;

        WifiConfiguration conf = new WifiConfiguration();

        // open
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedAuthAlgorithms.clear();
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        // wep
        /*conf.preSharedKey = "\"" + m_networkPass + "\"";
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);*/

        //conf.wepKeys[0] = "\"" + m_networkPass + "\"";
        //conf.wepTxKeyIndex = 0;

        // wpa
        /*conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        conf.wepKeys[0] = m_networkPass;
        conf.wepTxKeyIndex = 0;*/

        m_configuration = conf;

        m_wifiApManager = new WifiApManager(m_context);
        m_wifiManager = (WifiManager)m_context.getSystemService(Context.WIFI_SERVICE);

        if (!m_wifiManager.isWifiEnabled())
            m_wifiManager.setWifiEnabled(true);
    }


    int _setServerIndex = 0;
    int _setServerPort = 0;

    public void setServer(){
        String ssid;

        _setServerPort = Manager.INSTANCE.getRandomInt(2000, 15000);

        if(Manager.INSTANCE.getCurGroupID() == EMERGENCY) {
            ssid = EMERGENCY_SSID;
        }
        else {
            ssid = RESERVED_SSID;
            ssid += "_";
            ssid += String.valueOf(Manager.INSTANCE.getCurGroupID());
            ssid += "_";
            ssid += Manager.INSTANCE.getCurGroup().name;
        }
        ssid += "_";
        ssid += String.valueOf(_setServerIndex);
        ssid += "_";
        ssid += String.valueOf(_setServerPort);

        m_configuration.SSID = ssid;

        m_wifiApManager.setWifiApEnabled(m_configuration, true);

        Thread t = new Thread(new Runnable() {
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        if(m_wifiApManager.getWifiApState() == 13) {
                            Message msg = Message.obtain(m_initServerHandler, 0, 1, 0);
                            m_initServerHandler.sendMessage(msg);
                            break;
                        }
                        Thread.sleep(1000);
                    } catch (Throwable t) {
                    }
                }
            }
        });
        t.start();
    }

    private Handler m_initServerHandler = new Handler() {
        public void handleMessage(Message msg) {
            Network.INSTANCE.initServer(_setServerPort);
            onConnectEnd();
        }
    };

    boolean _setClientStart = false;
    boolean _setClientEnd = false;
    boolean _setClientResult = false;
    boolean _setClientChange = false;

    public void setClient(){

        m_wifiManager.startScan();

        _setClientStart = true;
        _setClientEnd = false;
        _setServerIndex = 0;

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        m_context.registerReceiver(wifiReceiver, filter);
    }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) && _setClientStart) {
                List<ScanResult> results = m_wifiManager.getScanResults();
                if(results == null) {
                    _setClientResult = false;
                    _setClientEnd = true;
                    return;
                }
                String ssid = null;
                String bssid = null;

                if(Manager.INSTANCE.getCurGroupID() == EMERGENCY) {
                    for (ScanResult r : results) {
                        if (r.SSID.startsWith(EMERGENCY_SSID)) {
                            StringTokenizer t = new StringTokenizer(r.SSID, "_");
                            String indexStr = null;
                            String portStr = null;
                            if (t.hasMoreTokens()) t.nextToken();
                            if (t.hasMoreTokens()) indexStr = t.nextToken();
                            if (t.hasMoreTokens()) portStr = t.nextToken();

                            ssid = r.SSID;
                            bssid = r.BSSID;

                            int index = Integer.valueOf(indexStr) + 1;
                            if(index > _setServerIndex)
                                _setServerIndex = index;

                            _setServerPort = Integer.valueOf(portStr);

                            if(bssid.compareTo(m_curBSSID) != 0)
                                break;
                        }
                    }
                }
                else {
                    for (ScanResult r : results) {
                        if (r.SSID.startsWith(RESERVED_SSID)) {
                            StringTokenizer t = new StringTokenizer(r.SSID, "_");
                            String idStr = null;
                            String nameStr = null;
                            String indexStr = null;
                            String portStr = null;
                            if (t.hasMoreTokens()) t.nextToken();
                            if (t.hasMoreTokens()) idStr = t.nextToken();
                            if (t.hasMoreTokens()) nameStr = t.nextToken();
                            if (t.hasMoreTokens()) indexStr = t.nextToken();
                            if (t.hasMoreTokens()) portStr = t.nextToken();

                            if (nameStr == null)
                                continue;

                            // TODO : 수정
                            long group = Long.valueOf(idStr);
                            if (group == Manager.INSTANCE.getCurGroupID()) {
                                ssid = r.SSID;
                                bssid = r.BSSID;

                                int index = Integer.valueOf(indexStr) + 1;
                                if(index > _setServerIndex)
                                    _setServerIndex = index;

                                _setServerPort = Integer.valueOf(portStr);

                                if(bssid.compareTo(m_curBSSID) != 0)
                                    break;
                            }
                        }
                    }
                }

                if(ssid == null || (_setClientChange && bssid.compareTo(m_curBSSID) == 0)) {
                    _setClientStart = false;
                    _setClientEnd = true;
                    _setClientResult = false;
                    _setClientChange = false;
                    return;
                }

                m_configuration.SSID = "\"" + ssid + "\"";
                m_configuration.BSSID = bssid;

                int id = m_wifiManager.addNetwork(m_configuration);
                boolean result = m_wifiManager.enableNetwork(id, true);

                if(result) {
                    m_curBSSID = bssid;
                    createClientThread();
                }

                _setClientStart = false;
                _setClientEnd = true;
                _setClientResult = result;
                _setClientChange = false;
            }
        }
    };

    void createClientThread(){

        Thread t = new Thread(new Runnable() {
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        DhcpInfo dhcp = m_wifiManager.getDhcpInfo();
                        WifiInfo info = m_wifiManager.getConnectionInfo();
                        if(info.getBSSID().compareTo(m_curBSSID) == 0 && dhcp.gateway != 0) {
                            int serverIP = dhcp.gateway;
                            String ipAddress = String.format(
                                    "%d.%d.%d.%d",
                                    (serverIP & 0xff),
                                    (serverIP >> 8 & 0xff),
                                    (serverIP >> 16 & 0xff),
                                    (serverIP >> 24 & 0xff));

                            Message msg = Message.obtain(m_initClientHandler, 0, 1, 0);
                            msg.obj = ipAddress;
                            m_initClientHandler.sendMessage(msg);
                            break;
                        }
                        Thread.sleep(1000);
                    } catch (Throwable t) {
                    }
                }
            }
        });
        t.start();
    }

    private Handler m_initClientHandler = new Handler() {
        public void handleMessage(Message msg) {
            String ipAddress = (String)msg.obj;
            Network.INSTANCE.initClient(ipAddress, _setServerPort);
            onConnectEnd();
        }
    };

    public boolean isClientConnected(){
        return _setClientResult;
    }

    MyThread m_timerThread = null;

    public void connect(long group, boolean change){

        if (!m_wifiManager.isWifiEnabled())
            m_wifiManager.setWifiEnabled(true);

        if(group != Manager.INSTANCE.getCurGroupID() || Network.INSTANCE.isServer()) {
            _setServerIndex = 0;
            m_curBSSID = new String("");
        }

        Manager.INSTANCE.setCurGroup(group);

        _setClientChange = change;

        setClient();

        MyThread m_connectThread = new MyThread() {
            public void run() {
                while (!interrupted() && running) {
                    try {
                        if(_setClientEnd){
                            if(isClientConnected() == false) {
                                setServer();
                                Message msg = Message.obtain(m_toastHandler, 0, 1, 0);
                                msg.obj = "Server";
                                m_toastHandler.sendMessage(msg);
                            }
                            else {
                                Message msg = Message.obtain(m_toastHandler, 0, 1, 0);
                                msg.obj = "Client";
                                m_toastHandler.sendMessage(msg);
                            }

                            break;
                        }
                        sleep(300);
                    } catch (Throwable t) {
                        break;
                    }
                }
            }
        };
        m_connectThread.start();
    }

    private Handler m_toastHandler = new Handler() {
        public void handleMessage(Message msg) {
            String message = (String)msg.obj;
            Toast toast1 = Toast.makeText(m_context, message, Toast.LENGTH_LONG);
            toast1.show();
        }
    };

    public void onConnectEnd(){
        /*if(m_curGroup == EMERGENCY){
            m_timerThread = new MyThread() {
                int r = getRandomInt(10, 20);
                public void run() {
                    while (!Thread.interrupted() && running) {
                        try {
                            Thread.sleep(1000);

                            if(Network.INSTANCE.isServer() && Network.INSTANCE.getAllThreads().size() >= 1)
                                continue;

                            setData(getData() + 1);
                            if(getData() > r) {
                                connect(m_curGroup);
                                break;
                            }
                        } catch (Throwable t) {
                        }
                    }
                }
            };
            m_timerThread.start();
        }*/
    }

    public void setTimerZero(){
        if(m_timerThread != null)
            m_timerThread.setData(0);
    }

}
