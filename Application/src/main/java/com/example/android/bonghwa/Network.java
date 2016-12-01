package com.example.android.bonghwa;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;

import com.example.android.basicaccessibility.ChattingActivity;
import com.example.android.basicaccessibility.FileActivity;
import com.example.android.basicaccessibility.GallaryActivity;
import com.example.android.basicaccessibility.GroupHomeActivity;
import com.example.android.bonghwa.GroupInfo.ChatMsg;
import com.example.android.bonghwa.GroupInfo.Group;
import com.example.android.bonghwa.GroupInfo.GroupFile;
import com.example.android.bonghwa.GroupInfo.User;
import com.example.android.bonghwa.packet.PACKET;
import com.example.android.bonghwa.packet.PacketCommand;
import com.example.android.bonghwa.packet.PacketGrouplist;
import com.example.android.bonghwa.packet.PacketJoinRequest;
import com.example.android.bonghwa.packet.PacketNewUser;
import com.example.android.bonghwa.packet.PacketShareFileRequest;
import com.example.android.bonghwa.packet.PacketShareFileRequestOK;
import com.example.android.bonghwa.packet.PacketShareText;
import com.example.android.bonghwa.packet.PacketSync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Created by 초록 on 2015-10-05.
 */
public enum Network {
    INSTANCE;

    String SERVERADDRESS = "192.168.43.1";
    public static int PORT = 11000;
    public static final int TIMEOUT = 10000;
    public static final int BUFFERSIZE = 1024;

    int mobileIndex = 0;

    Server mobileServer = null;
    Client mobileClient = null;
    HashMap<Integer, Pair<NetworkSpeaker, NetworkListener>> mobileThreads = new HashMap<>();

    long mobileServerID = 0;

    private Handler mobileHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                Pair<Integer, PacketCommand> pair = (Pair) msg.obj;
                mobileThreads.get(pair.first).first.write(pair.second);
            }
            catch(Exception e){}
        }
    };

    private Handler mobileJoinHandler = new Handler() {
        public void handleMessage(Message msg) {

            PacketJoinRequest p = (PacketJoinRequest)msg.obj;
            Manager.INSTANCE.setTempObject(p);

            String message = new String();
            message = Manager.INSTANCE.getUserName(p.userID);
            message += "님이 가입신청 하셨습니다.\n수락하시겠습니까?";

            AlertDialog.Builder d = new AlertDialog.Builder(Manager.INSTANCE.getContext());
            d.setMessage(message);
            d.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PacketJoinRequest p = (PacketJoinRequest)Manager.INSTANCE.getTempObject();

                    User u = new User();
                    u.name = p.userInfo.name;
                    Manager.INSTANCE.addUser(p.group, p.userID, u);

                    GroupHomeActivity.refreshList();

                    PacketNewUser reply = new PacketNewUser();
                    reply.group = p.group;
                    reply.userID = p.userID;
                    reply.userInfo.name = new String(p.userInfo.name);

                    writeAll(reply);
                }
            });
            d.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            d.show();
        }
    };

    private Handler mobileRefreshChatting = new Handler() {
        public void handleMessage(Message msg) {
            ChattingActivity.refreshList();
            GroupHomeActivity.refreshList();
        }
    };

    private Handler mobileRefreshFile = new Handler() {
        public void handleMessage(Message msg) {
            GallaryActivity.refreshList();
            FileActivity.refreshList();
        }
    };

    private Handler mobileRefreshHome = new Handler() {
        public void handleMessage(Message msg) {
            GroupHomeActivity.refreshList();
        }
    };

    public boolean isServer(){
        return mobileServer != null;
    }
    public boolean isClient(){ return !isServer(); }

    public long getServerID(){return mobileServerID;}

    public HashMap<Integer, Pair<NetworkSpeaker, NetworkListener>> getAllThreads(){
        return mobileThreads;
    }

    private void clearAll(){

        mobileIndex = 0;

        if(mobileServer != null)
            mobileServer.setKill();
        if(mobileClient != null)
            mobileClient.setKill();

        for(Pair<NetworkSpeaker, NetworkListener> p : mobileThreads.values()){
            p.first.setKill();
            p.second.setKill();
        }

        mobileThreads.clear();

        mobileServer = null;
        mobileClient = null;

        mobileServerID = 0;
    }

    public void initServer(int port){

        clearAll();

        try {
            PORT = port;
            ServerSocket serverSocket = new ServerSocket(port);
            mobileServer = new Server(serverSocket);
            mobileServer.start();
            mobileServerID = Manager.INSTANCE.getMyNumber();

            Message msg = Message.obtain(mobileRefreshHome, 0 , 1 , 0);
            mobileRefreshHome.sendMessage(msg);
        } catch (IOException ex) {
            //System.err.println(ex);
        } finally { // dispose
            /*if (mobileServerSocket != null) {
                try {
                    mobileServerSocket.close();
                } catch (IOException ex) {
                    // ignore
                }
            }*/
        }
    }

    public void initClient(String serverAddress, int port){
        clearAll();

        SERVERADDRESS = serverAddress;
        PORT = port;
        mobileClient = new Client();
        mobileClient.start();
    }

    public synchronized void killThread(int index){

        if(mobileThreads.get(index) == null)
            return;

        mobileThreads.get(index).first.setKill();
        mobileThreads.get(index).second.setKill();
        mobileThreads.remove(index);
    }


    public synchronized void write(PacketCommand p, int index)
    {
        Message msg = Message.obtain(mobileHandler, 0 , 1 , 0);
        msg.obj = new Pair<Integer, PacketCommand>(index, p);
        mobileHandler.sendMessage(msg);
    }

    public synchronized void writeAll(PacketCommand p)
    {
        for(Integer index : mobileThreads.keySet()){
            Message msg = Message.obtain(mobileHandler, 0 , 1 , 0);
            msg.obj = new Pair<Integer, PacketCommand>(index, p);
            mobileHandler.sendMessage(msg);
        }
    }

    void destroy()
    {
        /*if(mobileClientListener != null && mobileClientListener.isAlive())
            mobileClientListener.interrupt();
        mobileClientListener.setKill();
        mobileClientListener = null;*/
    }

    public class Server extends Thread{

        private ServerSocket mobileServer;
        private boolean mobileKill = false;

        public Server(ServerSocket server)
        {
            mobileServer = server;
        }

        /*****************************************************
         *		Main loop
         ******************************************************/

        @Override
        public void run()
        {
            while(!Thread.interrupted())
            {
                try {
                    Socket socket = mobileServer.accept();
                    NetworkSpeaker speaker = new NetworkSpeaker(mobileIndex, socket.getOutputStream());
                    NetworkListener listener = new NetworkListener(mobileIndex, socket.getInputStream());
                    mobileThreads.put(mobileIndex, new Pair<>(speaker, listener));
                    ++mobileIndex;
                    speaker.start();
                    listener.start();

                    PacketGrouplist p = new PacketGrouplist();
                    p.id = Manager.INSTANCE.getMyNumber();
                    p.groups = (HashMap<Long, Group>)Manager.INSTANCE.getAllGroups().clone();
                    Network.INSTANCE.write(p, mobileIndex-1);

                } catch(IOException e){
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                if(mobileKill)
                    break;

            }	// End of while() loop

            // Finalize
            //finalizeThread();

        }	// End of run()

        void setKill(){
            mobileKill = true;
        }
    }

    public class Client extends Thread{

        private boolean mobileKill = false;
        Socket clientSocket;
        NetworkSpeaker speaker;
        NetworkListener listener;

        public Client()
        {
        }

        /*****************************************************
         *		Main loop
         ******************************************************/

        @Override
        public void run()
        {
            while(!Thread.interrupted())
            {
                try {
                    clientSocket = new Socket(SERVERADDRESS, PORT);
                    speaker = new NetworkSpeaker(mobileIndex, clientSocket.getOutputStream());
                    listener = new NetworkListener(mobileIndex, clientSocket.getInputStream());
                    mobileThreads.put(mobileIndex, new Pair<>(speaker, listener));
                    ++mobileIndex;
                    speaker.start();
                    listener.start();

                    PacketGrouplist p = new PacketGrouplist();
                    p.id = Manager.INSTANCE.getMyNumber();
                    p.groups = (HashMap<Long, Group>)Manager.INSTANCE.getAllGroups().clone();
                    Network.INSTANCE.write(p, mobileIndex - 1);

                    break;

                } catch(IOException e){
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                if(mobileKill)
                    break;

            }	// End of while() loop

            // Finalize
            //finalizeThread();

        }	// End of run()

        void setKill(){
            mobileKill = true;
        }
    }

    public class FileServer extends Thread{

        private ServerSocket mobileServer;
        private String mobileFilename = new String();
        private boolean mobileBooleanWrite = false;

        public FileServer(ServerSocket server, String filename, boolean write)
        {
            mobileServer = server;
            mobileFilename = filename;
            mobileBooleanWrite = write;
        }

        /*****************************************************
         *		Main loop
         ******************************************************/

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Socket socket = mobileServer.accept();

                    OutputStream outStream = socket.getOutputStream();
                    InputStream inStream = socket.getInputStream();
                    if (mobileBooleanWrite) {
                        String path = Manager.INSTANCE.getRoot() + "/" + mobileFilename;
                        FileInputStream fis = new FileInputStream(path);
                        BufferedInputStream bis = new BufferedInputStream(fis);

                        int len;
                        byte[] buf = new byte[BUFFERSIZE];
                        while ((len = bis.read(buf)) != -1) {
                            outStream.write(buf, 0, len);
                            outStream.flush();
                        }

                        outStream.close();

                        bis.close();
                        fis.close();
                    }
                    else{
                        String path = Manager.INSTANCE.getRoot() + "/" + mobileFilename;
                        FileOutputStream fos = new FileOutputStream(path);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);

                        // 바이트 데이터를 전송받으면서 기록
                        int len;
                        byte[] buf = new byte[BUFFERSIZE];
                        while ((len = inStream.read(buf)) != -1) {
                            bos.write(buf, 0, len);
                            bos.flush();
                        }

                        bos.close();
                        fos.close();

                        StringTokenizer st = new StringTokenizer(mobileFilename, "/");
                        String filename = null;
                        while (st.hasMoreTokens())
                            filename = st.nextToken();

                        if(mobileFilename.contains("Pictures/"))
                            Manager.INSTANCE.addNewFile("Pictures/" + filename);
                        else
                            Manager.INSTANCE.addNewFile(filename);

                        Message msg = Message.obtain(mobileRefreshFile, 0, 1, 0);
                        mobileRefreshFile.sendMessage(msg);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    public class FileClient extends Thread{

        Socket clientSocket;
        String mobileFilename = new String();
        boolean mobileBooleanWrite = false;
        int mobilePort = 0;

        public FileClient(String filename, int port, boolean write)
        {
            mobileFilename = filename;
            mobilePort = port;
            mobileBooleanWrite = write;
        }

        /*****************************************************
         *		Main loop
         ******************************************************/

        @Override
        public void run()
        {
            while(!Thread.interrupted())
            {
                boolean result = true;

                try {
                    clientSocket = new Socket(SERVERADDRESS, mobilePort);
                    OutputStream outStream = clientSocket.getOutputStream();
                    InputStream inStream = clientSocket.getInputStream();
                    if (mobileBooleanWrite) {
                        String path = Manager.INSTANCE.getRoot() + "/" + mobileFilename;
                        FileInputStream fis = new FileInputStream(path);
                        BufferedInputStream bis = new BufferedInputStream(fis);

                        int len;
                        byte[] buf = new byte[BUFFERSIZE];
                        while ((len = bis.read(buf)) != -1) {
                            outStream.write(buf, 0, len);
                            outStream.flush();
                        }

                        outStream.close();

                        bis.close();
                        fis.close();
                    }
                    else{
                        String path = Manager.INSTANCE.getRoot() + "/" + mobileFilename;
                        FileOutputStream fos = new FileOutputStream(path);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);

                        // 바이트 데이터를 전송받으면서 기록
                        int len;
                        byte[] buf = new byte[BUFFERSIZE];
                        while ((len = inStream.read(buf)) != -1) {
                            bos.write(buf, 0, len);
                            bos.flush();
                        }

                        bos.close();
                        fos.close();

                        StringTokenizer st = new StringTokenizer(mobileFilename, "/");
                        String filename = null;
                        while (st.hasMoreTokens())
                            filename = st.nextToken();

                        if(mobileFilename.contains("Pictures/"))
                            Manager.INSTANCE.addNewFile("Pictures/" + filename);
                        else
                            Manager.INSTANCE.addNewFile(filename);

                        Message msg = Message.obtain(mobileRefreshFile, 0, 1, 0);
                        mobileRefreshFile.sendMessage(msg);
                    }

                } catch(IOException e){
                    result = false;
                }

                if(result)
                    break;

            }
        }
    }

    public class NetworkSpeaker extends Thread{

        int mobileIndex;
        OutputStream mobileOutStream;
        boolean mobileKill = false;

        public NetworkSpeaker(int index, OutputStream outstream)
        {
            mobileIndex = index;
            mobileOutStream = outstream;
        }

        public void write(PacketCommand p)
        {
            if(mobileOutStream == null || p.getCommand() == 0)
                return;

            byte[] b = new byte[BUFFERSIZE];
            p.getBytes(b);

            try {
                mobileOutStream.write(b, 0, p.place);
                //mobileOutStream.write(b, 0, BUFFERSIZE);
                mobileOutStream.flush();
            }
            catch (IOException ex) {
                //System.err.println(ex);
            }
        }

        void writeFile(String filename)
        {
            try {/*
                if (isServer()) {
                    ServerSocket socket = new ServerSocket(FILEPORT);
                    FileServer server = new FileServer(socket, filename, true);
                    server.start();
                }
                else{
                    FileClient client = new FileClient(filename, true);
                    client.start();
                }*/
            }
            catch(Exception e){
            }

            /*try {
                String path = Manager.INSTANCE.getRoot() + "/" + filename;
                FileInputStream fis = new FileInputStream(path);
                BufferedInputStream bis = new BufferedInputStream(fis);

                int len;
                byte[] buf = new byte[BUFFERSIZE];
                while ((len = bis.read(buf)) != -1) {
                    mobileOutStream.write(buf, 0, len);
                    mobileOutStream.flush();
                }

                // TODO : 수정
                byte[] junk = new byte[BUFFERSIZE];
                for(int i=0; i<3; ++i)
                    junk[i] = -1;

                mobileOutStream.write(buf, 0, 3);
                mobileOutStream.flush();

                //mobileOutStream.close();

                bis.close();
                fis.close();
            }
            catch (IOException ex) {
                //System.err.println(ex);
            }
            */
        }

        @Override
        public void run()
        {
            while(!Thread.interrupted())
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                if(mobileKill)
                    break;

            }	// End of while() loop

            // Finalize
            //finalizeThread();

        }	// End of run()

        void setKill(){
            mobileKill = true;
        }
    }

    // TODO : 파일전송 멀티스레드
    public class NetworkListener extends Thread{

        int mobileIndex;
        InputStream mobileInStream;
        boolean mobileKill = false;

        public NetworkListener(int index, InputStream instream)
        {
            mobileIndex = index;
            mobileInStream = instream;
        }

        @Override
        public void run()
        {
            while(!Thread.interrupted())
            {
                try {
                    byte stream[] = new byte[BUFFERSIZE];
                    int result = mobileInStream.read(stream);
                    if (result == -1) {
                        killThread(mobileIndex);
                        if (isClient())
                            Device.INSTANCE.createClientThread();
                        break;
                    }

                    while (true) {
                        PacketCommand cmd = new PacketCommand(stream); // new
                        int len = 0;

                        switch (cmd.getCommand()) {
                            case PACKET.PACKET_JOIN_REQUEST: {
                                PacketJoinRequest p = new PacketJoinRequest(stream);

                                Message msg = Message.obtain(mobileJoinHandler, 0, 1, 0);
                                msg.obj = p;
                                mobileJoinHandler.sendMessage(msg);

                                len = p.place;
                                break;
                            }
                            case PACKET.PACKET_NEW_USER: {
                                PacketNewUser p = new PacketNewUser(stream);
                                if (p.userID == Manager.INSTANCE.getMyNumber())
                                    Manager.INSTANCE.joinGranted(p.group);
                                else
                                    Manager.INSTANCE.addUser(p.group, p.userID, p.userInfo);

                                Message msg = Message.obtain(mobileRefreshHome, 0, 1, 0);
                                mobileRefreshHome.sendMessage(msg);

                                len = p.place;
                                break;
                            }
                            case PACKET.PACKET_GROUPLIST: {
                                Manager m = Manager.INSTANCE;

                                PacketGrouplist p = new PacketGrouplist(stream);
                                for (Long id : m.getAllGroups().keySet()) {
                                    if (p.groups.containsKey(id)) {
                                        Group g1 = m.getAllGroups().get(id);
                                        Group g2 = p.groups.get(id);

                                        g1.merge(g2);
                                        m.getAllGroups().put(id, g1);

                                        // send sync
                                        PacketSync reply = new PacketSync();
                                        reply.group = id;
                                        reply.files.putAll(Manager.INSTANCE.getGroup(id).files);
                                        write(reply, mobileIndex);
                                    }
                                }

                                if (m.isWatingJoin()) {
                                    Group g = p.groups.get(m.getCurGroupID());
                                    if (g != null)
                                        m.setJoinGroup(g);
                                }

                                if (isClient()) {
                                    mobileServerID = p.id;
                                    Message msg = Message.obtain(mobileRefreshHome, 0, 1, 0);
                                    mobileRefreshHome.sendMessage(msg);
                                }

                                len = p.place;
                                break;
                            }
                            case PACKET.PACKET_SYNC: {

                                PacketSync p = new PacketSync(stream);

                                LinkedList<ChatMsg> texts = Manager.INSTANCE.getText(p.group);
                                for (ChatMsg t : texts) {
                                    PacketShareText reply = new PacketShareText();
                                    reply.group = p.group;
                                    reply.text = t.text;
                                    reply.time = t.time;
                                    reply.uploader = t.uploader;
                                    write(reply, mobileIndex);
                                }

                                HashMap<String, GroupFile> files = Manager.INSTANCE.getGroup(p.group).files;
                                if (files == null)
                                    break;

                                for (String key : p.files.keySet()) {
                                    if (files.containsKey(key) == false) {
                                        PacketShareFileRequest reply = new PacketShareFileRequest();
                                        reply.group = p.group;
                                        reply.filename = key;
                                        reply.port = Manager.INSTANCE.getRandomInt(2000, 15000);
                                        write(reply, mobileIndex);

                                        if (isServer()) {
                                            ServerSocket socket = new ServerSocket(reply.port);
                                            FileServer server = new FileServer(socket, reply.filename, false);
                                            server.start();
                                        }
                                    }
                                }

                                // TODO : 전송 완료 확인
                                files.putAll(p.files);

                                len = p.place;
                                break;
                            }
                            case PACKET.PACKET_SHARE_FILE_REQUEST: {
                                PacketShareFileRequest p = new PacketShareFileRequest(stream);
                                PacketShareFileRequestOK reply = new PacketShareFileRequestOK();
                                reply.group = p.group;
                                reply.filename = p.filename;
                                reply.port = p.port;
                                write(reply, mobileIndex);

                                if (isServer()) {
                                    ServerSocket socket = new ServerSocket(p.port);
                                    FileServer server = new FileServer(socket, p.filename, true);
                                    server.start();
                                }
                                else{
                                    FileClient client = new FileClient(p.filename, p.port, true);
                                    client.start();
                                }

                                len = p.place;
                                break;
                            }
                            case PACKET.PACKET_SHARE_FILE_REQUEST_OK: {
                                PacketShareFileRequestOK p = new PacketShareFileRequestOK(stream);
                                try {
                                    if(isClient()){
                                        FileClient client = new FileClient(p.filename, p.port, false);
                                        client.start();
                                    }
                                    /*
                                    String path = Manager.INSTANCE.getRoot() + "/" + p.filename;
                                    FileOutputStream fos = new FileOutputStream(path);
                                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                                    // 바이트 데이터를 전송받으면서 기록
                                    int len2;
                                    byte[] buf = new byte[BUFFERSIZE];
                                    while ((len2 = mobileInStream.read(buf)) > 0) {

                                        boolean last = false;
                                        if (3 <= len2 && len2 < BUFFERSIZE) {
                                            if (buf[len2 - 1] == -1 &&
                                                    buf[len2 - 2] == -1 &&
                                                    buf[len2 - 3] == -1)
                                                last = true;
                                        }

                                        if(last)
                                            len2 -= 3;

                                        bos.write(buf, 0, len2);
                                        bos.flush();

                                        if(last)
                                            break;
                                    }

                                    bos.close();
                                    fos.close();

                                    StringTokenizer st = new StringTokenizer(p.filename, "/");
                                    String filename = null;
                                    while (st.hasMoreTokens())
                                        filename = st.nextToken();

                                    if(p.filename.contains("Pictures/"))
                                        Manager.INSTANCE.addNewFile("Pictures/" + filename);
                                    else
                                        Manager.INSTANCE.addNewFile(filename);

                                    Message msg = Message.obtain(mobileRefreshFile, 0, 1, 0);
                                    mobileRefreshFile.sendMessage(msg);*/

                                } catch (Exception e) {
                                }

                                len = p.place;
                                break;
                            }
                            case PACKET.PACKET_SHARE_TEXT: {
                                PacketShareText p = new PacketShareText(stream);
                                Manager.INSTANCE.addText(p.group, p.uploader, p.time, p.text);

                                Message msg = Message.obtain(mobileRefreshChatting, 0, 1, 0);
                                mobileRefreshChatting.sendMessage(msg);

                                if (isServer()) {
                                    writeAll(p);
                                }

                                len = p.place;
                                break;
                            }
                            default:
                                break;
                        }

                        if(len > 0)
                            System.arraycopy(stream, len, stream, 0, BUFFERSIZE-len);
                        else
                            break;
                    }

                }catch(IOException ex){
                    //System.err.println(ex);
                }

                if(mobileKill)
                    break;

            }	// End of while() loop

            // Finalize
            //finalizeThread();

        }	// End of run()

        void setKill(){
            mobileKill = true;
        }
    }

}
