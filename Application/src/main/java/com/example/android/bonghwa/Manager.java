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
import com.example.android.bonghwa.packet.PacketSync;

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

    Context mobileContext;
    //long mobileMyNumber = 1033245828L;
    long mobileMyNumber = 1071343228L;
    User mobileMyUser = new User();
    long mobileCurGroup = 106423876801L; // TODO : delete


    HashMap<Long, Group> mobileGroups = new HashMap<>(); // id, name
//    HashMap<Long, HashMap<String, GroupFile>> files = new HashMap<>(); // id, files
//    HashMap<Long, LinkedList<ChatMsg>> texts = new HashMap<>();

    Object mobileTempObject;

    Random mobileRandom = new Random();

    // join
    boolean mobileBooleanWatingJoin = false;
    Group mobileJoinGroup = null;

    public void init(Context context)
    {
        mobileContext = context;

        //writeUserData();
        readUserData();

        // environment
        String storage = Environment.getExternalStorageState();
        if ( storage.equals(Environment.MEDIA_MOUNTED)) {
            String root = getRoot();
            java.io.File file = new java.io.File(root);
            if( !file.exists() )  // 원하는 경로에 폴더가 있는지 확인
                file.mkdirs();

            for(Long id : mobileGroups.keySet()){
                String dir = root + "/" + id + "_" + mobileGroups.get(id);
                file = new java.io.File(dir);
                if( !file.exists() )  // 원하는 경로에 폴더가 있는지 확인
                    file.mkdirs();
            }
        }

        TelephonyManager telManager = (TelephonyManager)context.getSystemService(context.TELEPHONY_SERVICE);
        if(telManager.getLine1Number() != null)
            mobileMyNumber = Long.valueOf(telManager.getLine1Number());
    }

    public Context getContext(){
        return mobileContext;
    }

    public Object getTempObject(){
        return mobileTempObject;
    }

    public void setTempObject(Object obj){
        mobileTempObject = obj;
    }

    public boolean isWatingJoin(){
        return mobileBooleanWatingJoin;
    }

    public void setWatingJoin(boolean b){
        mobileBooleanWatingJoin = b;
    }

    public void setJoinGroup(Group g) {
        mobileJoinGroup = new Group(g);
        setWatingJoin(false);
    }
    
    public Group getJoinGroup(){
        return mobileJoinGroup;
    }


    public void joinGranted(long id){
        addNewGroup(mobileJoinGroup);
        mobileJoinGroup = null;
    }

    public void addNewGroup(Group g){

        if(g.members.containsKey(mobileMyNumber) == false)
            g.members.put(mobileMyNumber, mobileMyUser);

        mobileGroups.put(g.id, new Group(g));

        writeUserData();
    }

    LinkedList<ChatMsg> getText(long group){
        return mobileGroups.get(group).texts;
    }

    public List<ChatMsg> getText(){
        return mobileGroups.get(mobileCurGroup).texts;
    }

    public void setCurGroup(long group){
        mobileCurGroup = group;
    }

    public long getCurGroupID(){
        return mobileCurGroup;
    }

    public Group getCurGroup(){
        return mobileGroups.get(mobileCurGroup);
    }

    public Group getGroup(long id){
        return mobileGroups.get(id);
    }

    public void addUser(long group, long userID, User info){
        mobileGroups.get(group).members.put(userID, info);
        writeUserData();
    }

    public long getMyNumber(){
        return mobileMyNumber;
    }

    public User getMyUserInfo(){
        User u = new User();
        u.name = mobileMyUser.name;
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
            stream = mobileContext.openFileInput("config.txt");
            Scanner s = new Scanner(stream);

            mobileGroups.clear();
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

        if(mobileGroups.containsKey(Device.EMERGENCY) == false){
            Group g = new Group();
            g.id = Device.EMERGENCY;
            g.name = "Emergency";
            g.members.put(mobileMyNumber, mobileMyUser);
            addNewGroup(g);
        }

        checkDirectories();
    }

    public void writeUserData()
    {
        FileOutputStream outputStream;
        String s = new String(" ");

        try {
            outputStream = mobileContext.openFileOutput("config.txt", Context.MODE_PRIVATE);
            outputStream.write(Integer.toString(mobileGroups.size()).getBytes());
            outputStream.write(s.getBytes());
            for(Long id : mobileGroups.keySet()){
                Group g = mobileGroups.get(id);
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
        return mobileGroups;
    }

    public ChatMsg addText(long group, long uploader, long time, String text){
        ChatMsg t = new ChatMsg();
        t.uploader = uploader;
        t.time = time;
        t.text = new String(text);

        for(ChatMsg t2 : mobileGroups.get(group).texts) {
            if(t.uploader == t2.uploader && t.time == t2.time)
                return null;
        }

        if(group == Device.EMERGENCY) {
            Manager.INSTANCE.sendEmergencySMS(text);
        }

        mobileGroups.get(group).texts.add(t);

        return t;
    }

    public String getUserName(long id){

        if(id == mobileMyNumber)
            return Device.DEFAULT_MYNAME;

        if(getCurGroup().members.get(id) != null) {
            String name = getCurGroup().members.get(id).name;
            if (name.compareTo(User.DEFAULT_USERNAME) != 0)
                return name;
        }

        // 주소록
        ContentResolver cr = mobileContext.getContentResolver();
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
            for(Long id : mobileGroups.keySet()) {
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
        for(Long id : mobileGroups.keySet()){
            if(id / 100 == mobileMyNumber){
                long index = id % 100;
                if(index > max)
                    max = index;
            }
        }

        long id = (mobileMyNumber * 100) + max + 1;
        Group g = new Group();
        g.name = name;
        User u = new User();
        g.id = id;
        u.name = mobileMyUser.name;
        g.members.put(mobileMyNumber, u);
        addNewGroup(g);
        mobileGroups.put(id, g);

        checkDirectories();
        writeUserData();
    }

    public String getRoot(){
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mobileContext.getString(R.string.app_name);
    }

    public String getRealGroupPath(long id){
        if(mobileGroups.containsKey(id)){
            String storage = Environment.getExternalStorageState();
            if ( storage.equals(Environment.MEDIA_MOUNTED))
                return getRoot() + "/" + mobileGroups.get(id).name + "_" + id;
            else
                return null;
        }
        else
            return null;
    }

    public String getGroupPath(long id){
        if(mobileGroups.containsKey(id)){
            String storage = Environment.getExternalStorageState();
            if ( storage.equals(Environment.MEDIA_MOUNTED))
                return "" + mobileGroups.get(id).name + "_" + id;
            else
                return null;
        }
        else
            return null;
    }

    public synchronized int getRandomInt(int s, int e){
        return mobileRandom.nextInt(e - s) + s;
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
        java.io.File ioFile = new java.io.File(getRealGroupPath(mobileCurGroup) + "/" + file);
        String path = getGroupPath(mobileCurGroup) + "/" + file;
        mobileGroups.get(mobileCurGroup).files.put(path, new GroupFile(path, false, ioFile.lastModified()));

        writeUserData();

        PacketSync p = new PacketSync();
        p.group = getCurGroupID();
        p.files.put(path, new GroupFile(path, false, ioFile.lastModified()));
        Network.INSTANCE.writeAll(p);
    }

    LocationManager mobileLocManager = null;
    Location mobileLocation = null;

    public void setupGPS(){

        // Acquire a reference to the system Location Manager
        mobileLocManager = (LocationManager) mobileContext.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        boolean isGPSEnabled = mobileLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        boolean isNetworkEnabled = mobileLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled="+ isGPSEnabled);
        Log.d("Main", "isNetworkEnabled="+ isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                mobileLocation = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        mobileLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        mobileLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    /**
     * 위도와 경도 기반으로 주소를 리턴하는 메서드
     */
    public String getGPSAddress() {      //gps_주소찾기.
        if(mobileLocation == null)
            return null;

        String address = null;

        double lat = mobileLocation.getLatitude();
        double lng = mobileLocation.getLongitude();


        //위치정보를 활용하기 위한 구글 API 객체
        Geocoder geocoder = new Geocoder(mobileContext, Locale.getDefault());     //에러시, 여기확인

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
        PendingIntent sentIntent = PendingIntent.getBroadcast(mobileContext, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(mobileContext, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        if(smsNumber == null || smsText == null || sentIntent == null || deliveredIntent == null)
            return;

        /**
         * SMS가 발송될때 실행
         * When the SMS massage has been sent
         */
        mobileContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        // 전송 성공
                        Toast.makeText(mobileContext, "전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Toast.makeText(mobileContext, "전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Toast.makeText(mobileContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Toast.makeText(mobileContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Toast.makeText(mobileContext, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        /**
         * SMS가 도착했을때 실행
         * When the SMS massage has been delivered
         */
        mobileContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        // 도착 완료
                        Toast.makeText(mobileContext, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // 도착 안됨
                        Toast.makeText(mobileContext, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
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


