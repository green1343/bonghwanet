package com.example.android.basicaccessibility;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.example.android.common.logger.Log;
import com.example.android.packet.Packet_Sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by 초록 on 2015-06-12.
 */
public enum Manager {
    INSTANCE;

    public final static String RESERVED_SSID = "bhn";
    public final static String DEFAULT_MYNAME = "Me";
    public final static String DEFAULT_USERNAME = "User";
    public final static String EMERGENCY_SSID = "emergencybhn";

    public final static long EMERGENCY = -1;

    public class FileInfo {

        public boolean isDirectory;
        public long time;

        public FileInfo(){
        }

        public FileInfo(boolean isDirectory, long time){
            this.isDirectory = isDirectory;
            this.time = time;
        }
    }

    public FileInfo getNewFileInfo(boolean isDirectory, long time){
        return new FileInfo(isDirectory, time);
    }

    public class UserInfo{
        public String name;
    }

    public UserInfo getNewUserInfo(){
        return new UserInfo();
    }

    public class GroupInfo{
        public String name = new String();
        public HashMap<Long, UserInfo> members = new HashMap<>();
        public HashMap<String, FileInfo> deletedFiles = new HashMap<>();

        public GroupInfo(){}
        public GroupInfo(GroupInfo g){
            name = new String(g.name);
            members = (HashMap<Long, UserInfo>)g.members.clone();
            deletedFiles = (HashMap<String, FileInfo>)g.deletedFiles.clone();
        }

        public void merge(GroupInfo g){
            if(g == null)
                return;

            members.putAll(g.members);
            deletedFiles.putAll(g.deletedFiles);
        }
    }

    public class TextInfo{
        public long uploader;
        public long time;
        public String text;

        public TextInfo(){}
    }

    public TextInfo getNewTextInfo(){
        return new TextInfo();
    }

    Context m_context;
    long m_myNumber = 1033245828L;
    UserInfo m_myUserInfo = new UserInfo();
    long m_curGroup = 106423876801L; // TODO : delete

    String m_curBSSID = new String();

    String m_networkPass = "dafsglokvogzsuiwhbejfgr";

    WifiConfiguration m_configuration;
    WifiApManager m_wifiApManager;
    WifiManager m_wifiManager;

    HashMap<Long, GroupInfo> m_groups = new HashMap<>(); // id, name
    HashMap<Long, HashMap<String, FileInfo>> m_files = new HashMap<>(); // id, files
    HashMap<Long, LinkedList<TextInfo>> m_texts = new HashMap<>();

    Object m_tempObject;

    Random m_random = new Random();

    // join
    boolean m_bWatingJoin = false;
    GroupInfo m_joinGroup = null;

    public void init(Context context)
    {
        m_context = context;

        m_myUserInfo.name = DEFAULT_USERNAME;

        //writeUserData();
        readUserData();

        // environment
        String storage = Environment.getExternalStorageState();
        if ( storage.equals(Environment.MEDIA_MOUNTED)) {
            String root = getRoot();
            java.io.File file = new java.io.File(root);
            if( !file.exists() )  // 원하는 경로에 폴더가 있는지 확인
                file.mkdirs();

            for(Long id : m_groups.keySet()){
                String dir = root + "/" + id + "_" + m_groups.get(id);
                file = new java.io.File(dir);
                if( !file.exists() )  // 원하는 경로에 폴더가 있는지 확인
                    file.mkdirs();
            }
        }

        TelephonyManager telManager = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
        if(telManager.getLine1Number() != null)
            m_myNumber = Long.valueOf(telManager.getLine1Number());

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

    public Context getContext(){
        return m_context;
    }

    public Object getTempObject(){
        return m_tempObject;
    }

    public void setTempObject(Object obj){
        m_tempObject = obj;
    }

    public boolean isWatingJoin(){
        return m_bWatingJoin;
    }

    public void setWatingJoin(boolean b){
        m_bWatingJoin = b;
    }

    public void setJoinGroup(GroupInfo g){
        m_joinGroup = new GroupInfo(g);
        setWatingJoin(false);
    }

    public GroupInfo getJoinGroup(){
        return m_joinGroup;
    }


    public void joinGranted(long id){
        addNewGroup(id, m_joinGroup);
        m_joinGroup = null;
    }

    public void addNewGroup(long id, GroupInfo g){

        if(g.members.containsKey(m_myNumber) == false)
            g.members.put(m_myNumber, m_myUserInfo);

        m_groups.put(id, new GroupInfo(g));
        m_files.put(id, new HashMap<String, FileInfo>());
        m_texts.put(id, new LinkedList<TextInfo>());

        writeUserData();
    }

    HashMap<Long, LinkedList<TextInfo>> getAllTexts(){return m_texts;}

    List<TextInfo> getText(long group){
        return m_texts.get(group);
    }

    public List<TextInfo> getText(){
        return m_texts.get(m_curGroup);
    }

    public void setCurGroup(long group){
        m_curGroup = group;
    }

    public long getCurGroupID(){
        return m_curGroup;
    }

    public GroupInfo getCurGroup(){
        return m_groups.get(m_curGroup);
    }

    public GroupInfo getGroup(long id){
        return m_groups.get(id);
    }

    public void addUser(long group, long userID, UserInfo info){
        m_groups.get(group).members.put(userID, info);
        writeUserData();
    }

    public long getMyNumber(){
        return m_myNumber;
    }

    public UserInfo getMyUserInfo(){
        UserInfo u = new UserInfo();
        u.name = m_myUserInfo.name;
        return u;
    }

    public WifiManager getWifiManager(){
        return m_wifiManager;
    }

    private void getFileList(HashMap<String, FileInfo> arr, String path) {
        java.io.File fileRoot = new java.io.File(getRoot() + "/" + path);
        arr.put(path, new FileInfo(fileRoot.isDirectory(), fileRoot.lastModified()));

        if( fileRoot.isDirectory() == false )
            return;

        String[] list = fileRoot.list();
        for(String file : list){
            String next = new String(path);
            next += "/";
            next += file;
            getFileList(arr, next);
        }
    }

    public void readUserData()
    {
        FileInputStream stream;

        try {
            stream = m_context.openFileInput("config.txt");
            Scanner s = new Scanner(stream);

            m_groups.clear();
            int size1 = s.nextInt();
            for(int i=0; i<size1; ++i) {
                long id = s.nextLong();
                GroupInfo g = new GroupInfo();
                g.name = s.next();

                int size2 = s.nextInt();
                for(int j=0; j<size2; ++j){
                    long userID = s.nextLong();
                    UserInfo u = new UserInfo();
                    u.name = s.next();
                    g.members.put(userID, u);
                }
                size2 = s.nextInt();
                for(int j=0; j<size2; ++j){
                    String filename = s.next();
                    FileInfo df = new FileInfo();
                    df.isDirectory = s.nextBoolean();
                    df.time = s.nextLong();
                    g.deletedFiles.put(filename, df);
                }
                addNewGroup(id, g);

                for(Long id2 : m_files.keySet()){
                    getFileList(m_files.get(id2), getGroupPath(id2));
                }
            }

            stream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(m_groups.containsKey(EMERGENCY) == false){
            GroupInfo g = new GroupInfo();
            g.name = "Emergency";
            g.members.put(m_myNumber, m_myUserInfo);
            addNewGroup(EMERGENCY, g);
        }
    }

    public void writeUserData()
    {
        FileOutputStream outputStream;
        String s = new String(" ");

        try {
            outputStream = m_context.openFileOutput("config.txt", Context.MODE_PRIVATE);
            outputStream.write(Integer.toString(m_groups.size()).getBytes());
            outputStream.write(s.getBytes());
            for(Long id : m_groups.keySet()){
                GroupInfo g = m_groups.get(id);
                outputStream.write(Long.toString(id).getBytes());
                outputStream.write(s.getBytes());
                outputStream.write(g.name.getBytes());
                outputStream.write(s.getBytes());

                outputStream.write(Integer.toString(g.members.size()).getBytes());
                outputStream.write(s.getBytes());
                for(Long userID : g.members.keySet()) {
                    UserInfo u = g.members.get(userID);
                    outputStream.write(Long.toString(userID).getBytes());
                    outputStream.write(s.getBytes());
                    outputStream.write(u.name.getBytes());
                    outputStream.write(s.getBytes());
                }

                outputStream.write(Integer.toString(g.deletedFiles.size()).getBytes());
                outputStream.write(s.getBytes());
                for(String filename : g.deletedFiles.keySet()){
                    FileInfo df = g.deletedFiles.get(filename);
                    outputStream.write(filename.getBytes());
                    outputStream.write(s.getBytes());
                    outputStream.write(Boolean.toString(df.isDirectory).getBytes());
                    outputStream.write(s.getBytes());
                    outputStream.write(Long.toString(df.time).getBytes());
                    outputStream.write(s.getBytes());
                }
            }
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<Long, GroupInfo> getAllGroups(){
        return m_groups;
    }

    public HashMap<Long, HashMap<String, FileInfo>> getAllFiles(){
        return m_files;
    }

    public FileInfo getNewDeletedFile(){
        return new FileInfo();
    }

    public GroupInfo getNewGroupInfo(){
        return new GroupInfo();
    }

    public TextInfo addText(long group, long uploader, long time, String text){
        TextInfo t = new TextInfo();
        t.uploader = uploader;
        t.time = time;
        t.text = new String(text);

        for(TextInfo t2 : m_texts.get(group)) {
            if(t.uploader == t2.uploader && t.time == t2.time)
                return null;
        }

        if(group == EMERGENCY) {
            Manager.INSTANCE.sendEmergencySMS(text);
        }

        m_texts.get(group).add(t);

        return t;
    }

    public String getUserName(long id){

        if(id == m_myNumber)
            return DEFAULT_MYNAME;

        if(getCurGroup().members.get(id) != null) {
            String name = getCurGroup().members.get(id).name;
            if (name.compareTo(Manager.DEFAULT_USERNAME) != 0)
                return name;
        }

        // 주소록
        ContentResolver cr = m_context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode("0" + id));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null)
            return DEFAULT_USERNAME;

        String contactName = new String(DEFAULT_USERNAME);

        if(cursor.moveToFirst())
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

        if(cursor != null && !cursor.isClosed())
            cursor.close();

        return contactName;
    }

    public void checkDirectories(){
        String storage = Environment.getExternalStorageState();
        if ( storage.equals(Environment.MEDIA_MOUNTED)) {
            for(Long id : m_groups.keySet()) {
                String dir = getRealGroupPath(id);
                java.io.File file = new java.io.File(dir);
                if (!file.exists())  // 원하는 경로에 폴더가 있는지 확인
                    file.mkdirs();

                HashMap<String, FileInfo> arr = new HashMap<>();
                arr.put(getGroupPath(id),  new FileInfo(true, file.lastModified()));
                m_files.put(id, arr);
            }
        }
    }

    public void createGroup(String name){
        long max = 0;
        for(Long id : m_groups.keySet()){
            if(id / 100 == m_myNumber){
                long index = id % 100;
                if(index > max)
                    max = index;
            }
        }

        long id = (m_myNumber * 100) + max + 1;
        GroupInfo g = new GroupInfo();
        g.name = name;
        UserInfo u = new UserInfo();
        u.name = m_myUserInfo.name;
        g.members.put(m_myNumber, u);
        addNewGroup(id, g);
        m_groups.put(id, g);

        checkDirectories();
        writeUserData();
    }

    public String getRoot(){
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + m_context.getString(R.string.app_name);
    }

    public String getRealGroupPath(long id){
        if(m_groups.containsKey(id)){
            String storage = Environment.getExternalStorageState();
            if ( storage.equals(Environment.MEDIA_MOUNTED))
                return getRoot() + "/" + m_groups.get(id).name + "_" + id;
            else
                return null;
        }
        else
            return null;
    }

    public String getGroupPath(long id){
        if(m_groups.containsKey(id)){
            String storage = Environment.getExternalStorageState();
            if ( storage.equals(Environment.MEDIA_MOUNTED))
                return "" + m_groups.get(id).name + "_" + id;
            else
                return null;
        }
        else
            return null;
    }

    public void setServer(){
        String ssid;

        if(m_curGroup == EMERGENCY)
            ssid = EMERGENCY_SSID;
        else {
            ssid = RESERVED_SSID;
            ssid += "_";
            ssid += String.valueOf(m_curGroup);
            ssid += "_";
            ssid += m_groups.get(m_curGroup).name;
        }

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
            WiFiNetwork.INSTANCE.initServer();
            onConnectEnd();
        }
    };

    public boolean setClient(){

        m_wifiManager.startScan();
        List<ScanResult> results = m_wifiManager.getScanResults();
        if(results == null)
            return false;

        String ssid = null;
        String bssid = null;

        if(m_curGroup == EMERGENCY) {
            for (ScanResult r : results) {
                if (r.SSID.startsWith(EMERGENCY_SSID)) {
                    ssid = r.SSID;
                    bssid = r.BSSID;

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
                    if (t.hasMoreTokens()) t.nextToken();
                    if (t.hasMoreTokens()) idStr = t.nextToken();
                    if (t.hasMoreTokens()) nameStr = t.nextToken();

                    if (nameStr == null)
                        continue;

                    long group = Long.valueOf(idStr);
                    if (group == m_curGroup) {
                        ssid = r.SSID;
                        bssid = r.BSSID;

                        if(bssid.compareTo(m_curBSSID) != 0)
                            break;
                    }
                }
            }
        }

        if(ssid == null)
            return false;

        m_configuration.SSID = "\"" + ssid + "\"";
        m_configuration.BSSID = bssid;

        int id = m_wifiManager.addNetwork(m_configuration);
        boolean result = m_wifiManager.enableNetwork(id, true);

        if(result) {
            m_curBSSID = bssid;
            createClientThread();
        }

        return result;
    }

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
            WiFiNetwork.INSTANCE.initClient(ipAddress);
            onConnectEnd();
        }
    };

    MyThread m_timerThread = null;

    public String connect(long group){

        setCurGroup(group);

        if(setClient() == false) {
            setServer();
            return "Server";
        }
        else
            return "Client";
    }

    public void onConnectEnd(){
        /*if(m_curGroup == EMERGENCY){
            m_timerThread = new MyThread() {
                int r = getRandomInt(10, 20);
                public void run() {
                    while (!Thread.interrupted() && running) {
                        try {
                            Thread.sleep(1000);

                            if(WiFiNetwork.INSTANCE.isServer() && WiFiNetwork.INSTANCE.getAllThreads().size() >= 1)
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

    public synchronized int getRandomInt(int s, int e){
        return m_random.nextInt(e - s) + s;
    }

    public void uploadFile(String path){

        StringTokenizer st = new StringTokenizer(path, "/");
        String filename = null;
        while(st.hasMoreTokens())
            filename = st.nextToken();

        String newPath = getRealGroupPath(getCurGroupID()) + "/" + filename;
        copyFile(path, newPath);
        addNewFile(filename);
    }

    public void checkPictureDirectory(){
        String dir = getRealGroupPath(getCurGroupID()) + "/Pictures";
        File file = new java.io.File(dir);
        if( !file.exists() )
            file.mkdirs();
    }

    public void uploadPicture(String path){

        StringTokenizer st = new StringTokenizer(path, "/");
        String filename = null;
        while(st.hasMoreTokens())
            filename = st.nextToken();

        checkPictureDirectory();

        String newPath = getRealGroupPath(getCurGroupID()) + "/Pictures/" + filename;
        copyFile(path, newPath);
        addNewFile("Pictures/" + filename);
    }

    public void uploadCamera(String path){

        StringTokenizer st = new StringTokenizer(path, "/");
        String filename = null;
        while(st.hasMoreTokens())
            filename = st.nextToken();

        addNewFile("Pictures/" + filename);
    }

    private void copyFile(String from , String to){
        try {
            FileInputStream fis = new FileInputStream(from);
            FileOutputStream newfos = new FileOutputStream(to);
            int readcount=0;
            byte[] buffer = new byte[1024];
            while((readcount = fis.read(buffer,0,1024))!= -1){
                newfos.write(buffer,0,readcount);
            }
            newfos.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addNewFile(String file){
        java.io.File ioFile = new java.io.File(getRealGroupPath(m_curGroup) + "/" + file);
        m_files.get(m_curGroup).put(getGroupPath(m_curGroup) + "/" + file, new FileInfo(false, ioFile.lastModified()));

        writeUserData();

        Packet_Sync p = new Packet_Sync();
        p.group = getCurGroupID();
        p.files.put(getGroupPath(m_curGroup) + "/" + file, new FileInfo(false, ioFile.lastModified()));
        WiFiNetwork.INSTANCE.writeAll(p);
    }

    LocationManager m_locManager = null;
    Location m_location = null;

    public void setupGPS(){

        // Acquire a reference to the system Location Manager
        m_locManager = (LocationManager) m_context.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        boolean isGPSEnabled = m_locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        boolean isNetworkEnabled = m_locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled="+ isGPSEnabled);
        Log.d("Main", "isNetworkEnabled="+ isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                m_location = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        m_locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        m_locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    /**
     * 위도와 경도 기반으로 주소를 리턴하는 메서드
     */
    public String getGPSAddress() {      //gps_주소찾기.
        if(m_location == null)
            return null;

        String address = null;

        double lat = m_location.getLatitude();
        double lng = m_location.getLongitude();


        //위치정보를 활용하기 위한 구글 API 객체
        Geocoder geocoder = new Geocoder(m_context, Locale.getDefault());     //에러시, 여기확인

        //주소 목록을 담기 위한 HashMap
        List<Address> list = null;

        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list == null) {
            Log.e("getAddress", "주소 데이터 얻기 실패");
            return null;
        }
        if (list.size() > 0) {
            Address addr = list.get(0);
            address = addr.getCountryName() + " "    //국가이름
                    + addr.getAdminArea() + " "      //도(경기도, 충청남도...) / 서울은 null값
                    // + addr.getPostalCode() + " "    //우편번호
                    + addr.getLocality() + " "       //시 이름
                    + addr.getThoroughfare() + " "   //동이름
                    + addr.getFeatureName();          //번지
        }

        return address;
    }

    public void sendSMS(String smsNumber, String smsText){
        PendingIntent sentIntent = PendingIntent.getBroadcast(m_context, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(m_context, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        if(smsNumber == null || smsText == null || sentIntent == null || deliveredIntent == null)
            return;

        /**
         * SMS가 발송될때 실행
         * When the SMS massage has been sent
         */
        m_context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Toast.makeText(m_context, "전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(m_context, "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(m_context, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(m_context, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(m_context, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        /**
         * SMS가 도착했을때 실행
         * When the SMS massage has been delivered
         */
        m_context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        // 도착 완료
                        Toast.makeText(m_context, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // 도착 안됨
                        Toast.makeText(m_context, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_DELIVERED_ACTION"));

        SmsManager mSmsManager = SmsManager.getDefault();
        if(mSmsManager == null)
            return;
        mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
    }

    public void sendEmergencySMS(String text){
        //sendSMS("119", text);
        sendSMS("00000000000", text);
    }
}


