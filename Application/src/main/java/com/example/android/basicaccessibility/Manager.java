package com.example.android.basicaccessibility;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.format.Time;

import com.example.android.packet.Packet_Sync;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by 초록 on 2015-06-12.
 */
public enum Manager {
    INSTANCE;

    public class File {

        public boolean isDirectory;
        public long time;
        public String filename;

        public File(){
        }

        public File(boolean isDirectory, long time, String filename){
            this.isDirectory = isDirectory;
            this.time = time;
            this.filename = filename;
        }
    }

    public File getNewFile(boolean isDirectory, long time, String filename){
        return new File(isDirectory, time, filename);
    }

    public class GroupInfo{
        public final static int MODE_OPEN = 0;
        public final static int MODE_INVITE = 1;
        public final static int MODE_CLOSED = 2;

        public String name;
        public int mode;
        public ArrayList<File> deletedFiles = new ArrayList<>();
    }

    Context m_context;
    long m_myNumber;
    long m_curGroup = 106423876801L; // TODO : delete

    String m_networkPass = "dafsglokvogzsuiwhbejfgr";

    WifiConfiguration m_configuration;
    WifiApManager m_wifiApManager;
    WifiManager m_wifiManager;

    // save file
    HashMap<Long, GroupInfo> m_groups = new HashMap<>(); // id, name
    HashMap<Long, ArrayList<File>> m_files = new HashMap<>(); // id, files

    public void init(Context context)
    {
        m_context = context;

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

        /*conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedAuthAlgorithms.clear();
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);*/


        //conf.SSID = "\"" + m_networkSSID + "\"";
        conf.preSharedKey = "\"" + m_networkPass + "\"";
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        conf.wepKeys[0] = "\"" + m_networkPass + "\"";
        conf.wepTxKeyIndex = 0;
        conf.preSharedKey = "\"" + m_networkPass + "\"";

        m_configuration = conf;

        m_wifiApManager = new WifiApManager(m_context);
        m_wifiManager = (WifiManager)m_context.getSystemService(Context.WIFI_SERVICE);

        // TODO : delete
        GroupInfo g = new GroupInfo();
        g.name = "test";
        g.mode = GroupInfo.MODE_OPEN;
        m_groups.put(m_curGroup, g);
        checkDirectories();

        if(setClient() == false)
            setServer();
    }

    public void setCurGroup(long group){
        m_curGroup = group;
    }

    public long getCurGroup(){
        return m_curGroup;
    }

    private void getFileList(ArrayList<File> arr, String path) {
        java.io.File fileRoot = new java.io.File(getRoot() + "/" + path);
        arr.add(new File(fileRoot.isDirectory(), fileRoot.lastModified(), path));

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
            for(int i=0; i<s.nextInt(); ++i) {
                long id = s.nextLong();
                GroupInfo g = new GroupInfo();
                g.name = s.next();
                g.mode = s.nextInt();
                for(int j=0; j<s.nextInt(); ++j){
                    File df = new File();
                    df.time = s.nextLong();
                    df.filename = s.next();
                    g.deletedFiles.add(df);
                }
                m_groups.put(id, g);

                m_files.put(id, new ArrayList<File>());

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
                outputStream.write(Integer.toString(g.mode).getBytes());
                outputStream.write(s.getBytes());
                outputStream.write(Integer.toString(g.deletedFiles.size()).getBytes());
                outputStream.write(s.getBytes());
                for(File df : g.deletedFiles){
                    outputStream.write(Long.toString(df.time).getBytes());
                    outputStream.write(s.getBytes());
                    outputStream.write(df.filename.getBytes());
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

    public File getNewDeletedFile(){
        return new File();
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

                ArrayList<File> arr = new ArrayList<File>();
                arr.add(new File(true, file.lastModified(), getGroupPath(id)));
                m_files.put(id, arr);
            }
        }
    }

    public void createGroup(String name, int mode){
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
        g.mode = mode;
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
        String ssid = m_context.getString(R.string.app_name);
        ssid += String.valueOf(m_curGroup);
        m_configuration.SSID = ssid;

        m_wifiApManager.setWifiApEnabled(m_configuration, true);
        WiFiNetwork.INSTANCE.initServer();
    }

    public boolean setClient(){
        if (!m_wifiManager.isWifiEnabled())
            m_wifiManager.setWifiEnabled(true);

        List<ScanResult> results = m_wifiManager.getScanResults();
        String appname = m_context.getString(R.string.app_name);
        String ssid = null;

        for(ScanResult r : results){
            if(r.BSSID.startsWith(appname)){
                long group = Long.valueOf(r.BSSID.substring(appname.length()));
                if(group == m_curGroup){
                    ssid = r.BSSID;
                    break;
                }
            }
        }

        if(ssid == null)
            return false;

        m_configuration.SSID = ssid;

        int id = m_wifiManager.addNetwork(m_configuration);
        boolean result = m_wifiManager.enableNetwork(id, true);
        WiFiNetwork.INSTANCE.initClient();

        return result;
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
        p.files.add(new File(false, ioFile.lastModified(), file));
        WiFiNetwork.INSTANCE.writeAll(p);
    }
}
