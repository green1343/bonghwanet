package com.example.android.bonghwa;

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

import com.example.android.basicaccessibility.R;
import com.example.android.bonghwa.GroupInfo.ChatMsg;
import com.example.android.bonghwa.GroupInfo.Group;
import com.example.android.bonghwa.GroupInfo.GroupFile;
import com.example.android.bonghwa.GroupInfo.User;
import com.example.android.bonghwa.needclass.MyThread;
import com.example.android.common.logger.Log;
import com.example.android.bonghwa.packet.Packet_Sync;

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

    Context m_context;
    //long m_myNumber = 1033245828L;
    long m_myNumber = 1071343228L;
    User m_myUser = new User();
    long m_curGroup = 106423876801L; // TODO : delete


    HashMap<Long, Group> m_groups = new HashMap<>(); // id, name
//    HashMap<Long, HashMap<String, GroupFile>> files = new HashMap<>(); // id, files
//    HashMap<Long, LinkedList<ChatMsg>> texts = new HashMap<>();

    Object m_tempObject;

    Random m_random = new Random();

    // join
    boolean m_bWatingJoin = false;
    Group m_joinGroup = null;

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

    public void setJoinGroup(Group g) {
        m_joinGroup = new Group(g);
        setWatingJoin(false);
    }
    
    public Group getJoinGroup(){
        return m_joinGroup;
    }


    public void joinGranted(long id){
        addNewGroup(m_joinGroup);
        m_joinGroup = null;
    }

    public void addNewGroup(Group g){

        if(g.members.containsKey(m_myNumber) == false)
            g.members.put(m_myNumber, m_myUser);

        m_groups.put(g.id, new Group(g));

        writeUserData();
    }

    LinkedList<ChatMsg> getText(long group){
        return m_groups.get(group).texts;
    }

    public List<ChatMsg> getText(){
        return m_groups.get(m_curGroup).texts;
    }

    public void setCurGroup(long group){
        m_curGroup = group;
    }

    public long getCurGroupID(){
        return m_curGroup;
    }

    public Group getCurGroup(){
        return m_groups.get(m_curGroup);
    }

    public Group getGroup(long id){
        return m_groups.get(id);
    }

    public void addUser(long group, long userID, User info){
        m_groups.get(group).members.put(userID, info);
        writeUserData();
    }

    public long getMyNumber(){
        return m_myNumber;
    }

    public User getMyUserInfo(){
        User u = new User();
        u.name = m_myUser.name;
        return u;
    }

    private void getFileList(HashMap<String, GroupFile> arr, String path) {
        java.io.File fileRoot = new java.io.File(getRoot() + "/" + path);
        arr.put(path, new GroupFile(path, fileRoot.isDirectory(), fileRoot.lastModified()));

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
                Group g = new Group();
                g.id = s.nextLong();
                g.name = s.next();

                int size2 = s.nextInt();
                for(int j=0; j<size2; ++j){
                    long userID = s.nextLong();
                    User u = new User();
                    u.name = s.next();
                    g.members.put(userID, u);
                }
                size2 = s.nextInt();
                for(int j=0; j<size2; ++j){
                    String filename = s.next();
                    GroupFile df = new GroupFile();
                    df.isDirectory = s.nextBoolean();
                    df.time = s.nextLong();
                    g.deletedFiles.put(filename, df);
                }
                addNewGroup(g);

                getFileList(g.files, getGroupPath(g.id));
            }

            stream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(m_groups.containsKey(Device.EMERGENCY) == false){
            Group g = new Group();
            g.id = Device.EMERGENCY;
            g.name = "Emergency";
            g.members.put(m_myNumber, m_myUser);
            addNewGroup(g);
        }

        checkDirectories();
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
                Group g = m_groups.get(id);
                outputStream.write(Long.toString(id).getBytes());
                outputStream.write(s.getBytes());
                outputStream.write(g.name.getBytes());
                outputStream.write(s.getBytes());

                outputStream.write(Integer.toString(g.members.size()).getBytes());
                outputStream.write(s.getBytes());
                for(Long userID : g.members.keySet()) {
                    User u = g.members.get(userID);
                    outputStream.write(Long.toString(userID).getBytes());
                    outputStream.write(s.getBytes());
                    outputStream.write(u.name.getBytes());
                    outputStream.write(s.getBytes());
                }

                outputStream.write(Integer.toString(g.deletedFiles.size()).getBytes());
                outputStream.write(s.getBytes());
                for(String filename : g.deletedFiles.keySet()){
                    GroupFile df = g.deletedFiles.get(filename);
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

    public HashMap<Long, Group> getAllGroups(){
        return m_groups;
    }

    public ChatMsg addText(long group, long uploader, long time, String text){
        ChatMsg t = new ChatMsg();
        t.uploader = uploader;
        t.time = time;
        t.text = new String(text);

        for(ChatMsg t2 : m_groups.get(group).texts) {
            if(t.uploader == t2.uploader && t.time == t2.time)
                return null;
        }

        if(group == Device.EMERGENCY) {
            Manager.INSTANCE.sendEmergencySMS(text);
        }

        m_groups.get(group).texts.add(t);

        return t;
    }

    public String getUserName(long id){

        if(id == m_myNumber)
            return Device.DEFAULT_MYNAME;

        if(getCurGroup().members.get(id) != null) {
            String name = getCurGroup().members.get(id).name;
            if (name.compareTo(User.DEFAULT_USERNAME) != 0)
                return name;
        }

        // 주소록
        ContentResolver cr = m_context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode("0" + id));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null)
            return User.DEFAULT_USERNAME;

        String contactName = new String(User.DEFAULT_USERNAME);

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
                if (!file.exists())
                    file.mkdirs();

                dir += "/Pictures";
                file = new java.io.File(dir);
                if (!file.exists())
                    file.mkdirs();

                /*HashMap<String, GroupFile> arr = new HashMap<>();
                arr.put(getGroupPath(id),  new GroupFile(true, file.lastModified()));
                files.put(id, arr);*/
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
        Group g = new Group();
        g.name = name;
        User u = new User();
        g.id = id;
        u.name = m_myUser.name;
        g.members.put(m_myNumber, u);
        addNewGroup(g);
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

    public void uploadPicture(String path){

        StringTokenizer st = new StringTokenizer(path, "/");
        String filename = null;
        while(st.hasMoreTokens())
            filename = st.nextToken();

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
        String path = getGroupPath(m_curGroup) + "/" + file;
        m_groups.get(m_curGroup).files.put(path, new GroupFile(path, false, ioFile.lastModified()));

        writeUserData();

        Packet_Sync p = new Packet_Sync();
        p.group = getCurGroupID();
        p.files.put(path, new GroupFile(path, false, ioFile.lastModified()));
        Network.INSTANCE.writeAll(p);
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
        if(smsNumber == null || smsText == null || sentIntent == null || deliveredIntent == null || mSmsManager == null)
            return;

        try {
            mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
        }catch(Exception e){}
    }

    public void sendEmergencySMS(String text){
        //sendSMS("119", text);
        sendSMS("00000000000", text);
    }
}


