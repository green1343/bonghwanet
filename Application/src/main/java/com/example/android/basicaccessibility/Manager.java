package com.example.android.basicaccessibility;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.example.android.packet.Packet_Sync;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by 초록 on 2015-06-12.
 */
public enum Manager {
    INSTANCE;

    public final static String RESERVED_SSID = "bhn";
    public final static String DEFAULT_USERNAME = "User";

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
        public String name;
        public HashMap<Long, UserInfo> members = new HashMap<>();
        public HashMap<String, FileInfo> deletedFiles = new HashMap<>();

        public GroupInfo(){}
        public GroupInfo(GroupInfo g){
            name = g.name;
            members = (HashMap<Long, UserInfo>)g.members.clone();
            deletedFiles = (HashMap<String, FileInfo>)g.deletedFiles.clone();
        }
    }

    Context m_context;
    long m_myNumber;
    UserInfo m_myUserInfo = new UserInfo();
    long m_curGroup = 106423876801L; // TODO : delete

    String m_networkPass = "dafsglokvogzsuiwhbejfgr";

    WifiConfiguration m_configuration;
    WifiApManager m_wifiApManager;
    WifiManager m_wifiManager;

    HashMap<Long, GroupInfo> m_groups = new HashMap<>(); // id, name
    HashMap<Long, HashMap<String, FileInfo>> m_files = new HashMap<>(); // id, files

    Object m_tempObject;

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

        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedAuthAlgorithms.clear();
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        /*conf.preSharedKey = "\"" + m_networkPass + "\"";
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);*/
/*
        conf.wepKeys[0] = "\"" + m_networkPass + "\"";
        conf.wepTxKeyIndex = 0;
        conf.preSharedKey = "\"" + m_networkPass + "\"";
*/
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

    public void joinGranted(long id){
        m_groups.put(id, m_joinGroup);
    }

    public void setCurGroup(long group){
        m_curGroup = group;
    }

    public long getCurGroup(){
        return m_curGroup;
    }

    public void addUser(long group, long userID, UserInfo info){
        m_groups.get(group).members.put(userID, info);
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
                m_groups.put(id, g);

                m_files.put(id, new HashMap<String, FileInfo>());

                for(Long id2 : m_files.keySet()){
                    getFileList(m_files.get(id2), getGroupPath(id2));
                }
            }

            stream.close();

        } catch (Exception e) {
            e.printStackTrace();
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

    public FileInfo getNewDeletedFile(){
        return new FileInfo();
    }

    public GroupInfo getNewGroupInfo(){
        return new GroupInfo();
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
        m_groups.put(id, g);

        checkDirectories();
    }

    public String getRoot(){
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + m_context.getString(R.string.app_name);
    }

    public String getRealGroupPath(long id){
        if(m_groups.containsKey(id)){
            String storage = Environment.getExternalStorageState();
            if ( storage.equals(Environment.MEDIA_MOUNTED))
                return getRoot() + "/" + m_groups.get(id) + "_" + id;
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
                return "" + m_groups.get(id) + "_" + id;
            else
                return null;
        }
        else
            return null;
    }

    public void setServer(){
        String ssid = RESERVED_SSID;
        ssid += "_";
        ssid += String.valueOf(m_curGroup);
        ssid += "_";
        ssid += m_groups.get(m_curGroup).name;
        m_configuration.SSID = ssid;

        m_wifiApManager.setWifiApEnabled(m_configuration, true);
        WiFiNetwork.INSTANCE.initServer();
    }

    public boolean setClient(){

        m_wifiManager.startScan();
        List<ScanResult> results = m_wifiManager.getScanResults();
        String ssid = null;

        for(ScanResult r : results){
            if(r.SSID.startsWith(RESERVED_SSID)){
                StringTokenizer t = new StringTokenizer(r.SSID, "_");
                String idStr = null;
                String nameStr = null;
                if(t.hasMoreTokens()) t.nextToken();
                if(t.hasMoreTokens()) idStr = t.nextToken();
                if(t.hasMoreTokens()) nameStr = t.nextToken();

                if(nameStr == null)
                    continue;

                long group = Long.valueOf(idStr);
                if(group == m_curGroup){
                    ssid = r.SSID;
                    break;
                }
            }
        }

        if(ssid == null)
            return false;

        m_configuration.SSID = ssid;

        int id = m_wifiManager.addNetwork(m_configuration);
        boolean result = m_wifiManager.enableNetwork(id, true);

        if(result)
            WiFiNetwork.INSTANCE.initClient();

        return result;
    }

    public void connect(long group){

        setCurGroup(group);

        if(setClient() == false)
            setServer();
    }

    public void uploadFile(String path){

        StringTokenizer st = new StringTokenizer(path, "/");
        String filename = null;
        while(st.hasMoreTokens())
            filename = st.nextToken();

        String newPath = getRealGroupPath(getCurGroup()) + "/" + filename;
        copyFile(path, newPath);
        sendSync(newPath);
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

    public void sendSync(String file){
        Packet_Sync p = new Packet_Sync();
        p.group = getCurGroup();
        java.io.File ioFile = new java.io.File(getRoot() + "/" + file);
        p.files.put(file, new FileInfo(false, ioFile.lastModified()));
        WiFiNetwork.INSTANCE.writeAll(p);
    }
}
