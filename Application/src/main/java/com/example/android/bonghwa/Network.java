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
import com.example.android.bonghwa.packet.Packet_Command;
import com.example.android.bonghwa.packet.Packet_Grouplist;
import com.example.android.bonghwa.packet.Packet_Join_Request;
import com.example.android.bonghwa.packet.Packet_New_User;
import com.example.android.bonghwa.packet.Packet_Share_File_Request;
import com.example.android.bonghwa.packet.Packet_Share_File_Request_OK;
import com.example.android.bonghwa.packet.Packet_Share_Text;
import com.example.android.bonghwa.packet.Packet_Sync;

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

    int m_index = 0;

    Server m_server = null;
    Client m_client = null;
    HashMap<Integer, Pair<NetworkSpeaker, NetworkListener>> m_threads = new HashMap<>();

    long m_serverID = 0;

    private Handler m_handler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                Pair<Integer, Packet_Command> pair = (Pair) msg.obj;
                m_threads.get(pair.first).first.write(pair.second);
            }
            catch(Exception e){}
        }
    };

    private Handler m_joinHandler = new Handler() {
        public void handleMessage(Message msg) {

            Packet_Join_Request p = (Packet_Join_Request)msg.obj;
            Manager.INSTANCE.setTempObject(p);

            String message = new String();
            message = Manager.INSTANCE.getUserName(p.userID);
            message += "님이 가입신청 하셨습니다.\n수락하시겠습니까?";

            AlertDialog.Builder d = new AlertDialog.Builder(Manager.INSTANCE.getContext());
            d.setMessage(message);
            d.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Packet_Join_Request p = (Packet_Join_Request)Manager.INSTANCE.getTempObject();

                    User u = new User();
                    u.name = p.userInfo.name;
                    Manager.INSTANCE.addUser(p.group, p.userID, u);

                    GroupHomeActivity.refreshList();

                    Packet_New_User reply = new Packet_New_User();
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

    private Handler m_refreshChatting = new Handler() {
        public void handleMessage(Message msg) {
            ChattingActivity.refreshList();
            GroupHomeActivity.refreshList();
        }
    };

    private Handler m_refreshFile = new Handler() {
        public void handleMessage(Message msg) {
            GallaryActivity.refreshList();
            FileActivity.refreshList();
        }
    };

    private Handler m_refreshHome = new Handler() {
        public void handleMessage(Message msg) {
            GroupHomeActivity.refreshList();
        }
    };

    public boolean isServer(){
        return m_server != null;
    }
    public boolean isClient(){ return !isServer(); }

    public long getServerID(){return m_serverID;}

    public HashMap<Integer, Pair<NetworkSpeaker, NetworkListener>> getAllThreads(){
        return m_threads;
    }

    private void clearAll(){

        m_index = 0;

        if(m_server != null)
            m_server.setKill();
        if(m_client != null)
            m_client.setKill();

        for(Pair<NetworkSpeaker, NetworkListener> p : m_threads.values()){
            p.first.setKill();
            p.second.setKill();
        }

        m_threads.clear();

        m_server = null;
        m_client = null;

        m_serverID = 0;
    }

    public void initServer(int port){

        clearAll();

        try {
            PORT = port;
            ServerSocket serverSocket = new ServerSocket(port);
            m_server = new Server(serverSocket);
            m_server.start();
            m_serverID = Manager.INSTANCE.getMyNumber();

            Message msg = Message.obtain(m_refreshHome, 0 , 1 , 0);
            m_refreshHome.sendMessage(msg);
        } catch (IOException ex) {
            //System.err.println(ex);
        } finally { // dispose
            /*if (m_serverSocket != null) {
                try {
                    m_serverSocket.close();
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
        m_client = new Client();
        m_client.start();
    }

    public synchronized void killThread(int index){

        if(m_threads.get(index) == null)
            return;

        m_threads.get(index).first.setKill();
        m_threads.get(index).second.setKill();
        m_threads.remove(index);
    }


    public synchronized void write(Packet_Command p, int index)
    {
        Message msg = Message.obtain(m_handler, 0 , 1 , 0);
        msg.obj = new Pair<Integer, Packet_Command>(index, p);
        m_handler.sendMessage(msg);
    }

    public synchronized void writeAll(Packet_Command p)
    {
        for(Integer index : m_threads.keySet()){
            Message msg = Message.obtain(m_handler, 0 , 1 , 0);
            msg.obj = new Pair<Integer, Packet_Command>(index, p);
            m_handler.sendMessage(msg);
        }
    }

    void destroy()
    {
        /*if(m_clientListener != null && m_clientListener.isAlive())
            m_clientListener.interrupt();
        m_clientListener.setKill();
        m_clientListener = null;*/
    }

    public class Server extends Thread{

        private ServerSocket m_server;
        private boolean m_kill = false;

        public Server(ServerSocket server)
        {
            m_server = server;
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
                    Socket socket = m_server.accept();
                    NetworkSpeaker speaker = new NetworkSpeaker(m_index, socket.getOutputStream());
                    NetworkListener listener = new NetworkListener(m_index, socket.getInputStream());
                    m_threads.put(m_index, new Pair<>(speaker, listener));
                    ++m_index;
                    speaker.start();
                    listener.start();

                    Packet_Grouplist p = new Packet_Grouplist();
                    p.id = Manager.INSTANCE.getMyNumber();
                    p.groups = (HashMap<Long, Group>)Manager.INSTANCE.getAllGroups().clone();
                    Network.INSTANCE.write(p, m_index-1);

                } catch(IOException e){
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                if(m_kill)
                    break;

            }	// End of while() loop

            // Finalize
            //finalizeThread();

        }	// End of run()

        void setKill(){
            m_kill = true;
        }
    }

    public class Client extends Thread{

        private boolean m_kill = false;
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
                    speaker = new NetworkSpeaker(m_index, clientSocket.getOutputStream());
                    listener = new NetworkListener(m_index, clientSocket.getInputStream());
                    m_threads.put(m_index, new Pair<>(speaker, listener));
                    ++m_index;
                    speaker.start();
                    listener.start();

                    Packet_Grouplist p = new Packet_Grouplist();
                    p.id = Manager.INSTANCE.getMyNumber();
                    p.groups = (HashMap<Long, Group>)Manager.INSTANCE.getAllGroups().clone();
                    Network.INSTANCE.write(p, m_index - 1);

                    break;

                } catch(IOException e){
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                if(m_kill)
                    break;

            }	// End of while() loop

            // Finalize
            //finalizeThread();

        }	// End of run()

        void setKill(){
            m_kill = true;
        }
    }

    public class FileServer extends Thread{

        private ServerSocket m_server;
        private String m_filename = new String();
        private boolean m_bWrite = false;

        public FileServer(ServerSocket server, String filename, boolean write)
        {
            m_server = server;
            m_filename = filename;
            m_bWrite = write;
        }

        /*****************************************************
         *		Main loop
         ******************************************************/

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Socket socket = m_server.accept();

                    OutputStream outStream = socket.getOutputStream();
                    InputStream inStream = socket.getInputStream();
                    if (m_bWrite) {
                        String path = Manager.INSTANCE.getRoot() + "/" + m_filename;
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
                        String path = Manager.INSTANCE.getRoot() + "/" + m_filename;
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

                        StringTokenizer st = new StringTokenizer(m_filename, "/");
                        String filename = null;
                        while (st.hasMoreTokens())
                            filename = st.nextToken();

                        if(m_filename.contains("Pictures/"))
                            Manager.INSTANCE.addNewFile("Pictures/" + filename);
                        else
                            Manager.INSTANCE.addNewFile(filename);

                        Message msg = Message.obtain(m_refreshFile, 0, 1, 0);
                        m_refreshFile.sendMessage(msg);
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
        String m_filename = new String();
        boolean m_bWrite = false;
        int m_port = 0;

        public FileClient(String filename, int port, boolean write)
        {
            m_filename = filename;
            m_port = port;
            m_bWrite = write;
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
                    clientSocket = new Socket(SERVERADDRESS, m_port);
                    OutputStream outStream = clientSocket.getOutputStream();
                    InputStream inStream = clientSocket.getInputStream();
                    if (m_bWrite) {
                        String path = Manager.INSTANCE.getRoot() + "/" + m_filename;
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
                        String path = Manager.INSTANCE.getRoot() + "/" + m_filename;
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

                        StringTokenizer st = new StringTokenizer(m_filename, "/");
                        String filename = null;
                        while (st.hasMoreTokens())
                            filename = st.nextToken();

                        if(m_filename.contains("Pictures/"))
                            Manager.INSTANCE.addNewFile("Pictures/" + filename);
                        else
                            Manager.INSTANCE.addNewFile(filename);

                        Message msg = Message.obtain(m_refreshFile, 0, 1, 0);
                        m_refreshFile.sendMessage(msg);
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

        int m_index;
        OutputStream m_outStream;
        boolean m_kill = false;

        public NetworkSpeaker(int index, OutputStream outstream)
        {
            m_index = index;
            m_outStream = outstream;
        }

        public void write(Packet_Command p)
        {
            if(m_outStream == null || p.getCommand() == 0)
                return;

            byte[] b = new byte[BUFFERSIZE];
            p.GetBytes(b);

            try {
                m_outStream.write(b, 0, p.place);
                //m_outStream.write(b, 0, BUFFERSIZE);
                m_outStream.flush();
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
                    m_outStream.write(buf, 0, len);
                    m_outStream.flush();
                }

                // TODO : 수정
                byte[] junk = new byte[BUFFERSIZE];
                for(int i=0; i<3; ++i)
                    junk[i] = -1;

                m_outStream.write(buf, 0, 3);
                m_outStream.flush();

                //m_outStream.close();

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

                if(m_kill)
                    break;

            }	// End of while() loop

            // Finalize
            //finalizeThread();

        }	// End of run()

        void setKill(){
            m_kill = true;
        }
    }

    // TODO : 파일전송 멀티스레드
    public class NetworkListener extends Thread{

        int m_index;
        InputStream m_inStream;
        boolean m_kill = false;

        public NetworkListener(int index, InputStream instream)
        {
            m_index = index;
            m_inStream = instream;
        }

        @Override
        public void run()
        {
            while(!Thread.interrupted())
            {
                try {
                    byte stream[] = new byte[BUFFERSIZE];
                    int result = m_inStream.read(stream);
                    if (result == -1) {
                        killThread(m_index);
                        if (isClient())
                            Device.INSTANCE.createClientThread();
                        break;
                    }

                    while (true) {
                        Packet_Command cmd = new Packet_Command(stream); // new
                        int len = 0;

                        switch (cmd.getCommand()) {
                            case PACKET.PACKET_JOIN_REQUEST: {
                                Packet_Join_Request p = new Packet_Join_Request(stream);

                                Message msg = Message.obtain(m_joinHandler, 0, 1, 0);
                                msg.obj = p;
                                m_joinHandler.sendMessage(msg);

                                len = p.place;
                                break;
                            }
                            case PACKET.PACKET_NEW_USER: {
                                Packet_New_User p = new Packet_New_User(stream);
                                if (p.userID == Manager.INSTANCE.getMyNumber())
                                    Manager.INSTANCE.joinGranted(p.group);
                                else
                                    Manager.INSTANCE.addUser(p.group, p.userID, p.userInfo);

                                Message msg = Message.obtain(m_refreshHome, 0, 1, 0);
                                m_refreshHome.sendMessage(msg);

                                len = p.place;
                                break;
                            }
                            case PACKET.PACKET_GROUPLIST: {
                                Manager m = Manager.INSTANCE;

                                Packet_Grouplist p = new Packet_Grouplist(stream);
                                for (Long id : m.getAllGroups().keySet()) {
                                    if (p.groups.containsKey(id)) {
                                        Group g1 = m.getAllGroups().get(id);
                                        Group g2 = p.groups.get(id);

                                        g1.merge(g2);
                                        m.getAllGroups().put(id, g1);

                                        // send sync
                                        Packet_Sync reply = new Packet_Sync();
                                        reply.group = id;
                                        reply.files.putAll(Manager.INSTANCE.getGroup(id).files);
                                        write(reply, m_index);
                                    }
                                }

                                if (m.isWatingJoin()) {
                                    Group g = p.groups.get(m.getCurGroupID());
                                    if (g != null)
                                        m.setJoinGroup(g);
                                }

                                if (isClient()) {
                                    m_serverID = p.id;
                                    Message msg = Message.obtain(m_refreshHome, 0, 1, 0);
                                    m_refreshHome.sendMessage(msg);
                                }

                                len = p.place;
                                break;
                            }
                            case PACKET.PACKET_SYNC: {

                                Packet_Sync p = new Packet_Sync(stream);

                                LinkedList<ChatMsg> texts = Manager.INSTANCE.getText(p.group);
                                for (ChatMsg t : texts) {
                                    Packet_Share_Text reply = new Packet_Share_Text();
                                    reply.group = p.group;
                                    reply.text = t.text;
                                    reply.time = t.time;
                                    reply.uploader = t.uploader;
                                    write(reply, m_index);
                                }

                                HashMap<String, GroupFile> files = Manager.INSTANCE.getGroup(p.group).files;
                                if (files == null)
                                    break;

                                for (String key : p.files.keySet()) {
                                    if (files.containsKey(key) == false) {
                                        Packet_Share_File_Request reply = new Packet_Share_File_Request();
                                        reply.group = p.group;
                                        reply.filename = key;
                                        reply.port = Manager.INSTANCE.getRandomInt(2000, 15000);
                                        write(reply, m_index);

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
                                Packet_Share_File_Request p = new Packet_Share_File_Request(stream);
                                Packet_Share_File_Request_OK reply = new Packet_Share_File_Request_OK();
                                reply.group = p.group;
                                reply.filename = p.filename;
                                reply.port = p.port;
                                write(reply, m_index);

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
                                Packet_Share_File_Request_OK p = new Packet_Share_File_Request_OK(stream);
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
                                    while ((len2 = m_inStream.read(buf)) > 0) {

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

                                    Message msg = Message.obtain(m_refreshFile, 0, 1, 0);
                                    m_refreshFile.sendMessage(msg);*/

                                } catch (Exception e) {
                                }

                                len = p.place;
                                break;
                            }
                            case PACKET.PACKET_SHARE_TEXT: {
                                Packet_Share_Text p = new Packet_Share_Text(stream);
                                Manager.INSTANCE.addText(p.group, p.uploader, p.time, p.text);

                                Message msg = Message.obtain(m_refreshChatting, 0, 1, 0);
                                m_refreshChatting.sendMessage(msg);

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

                if(m_kill)
                    break;

            }	// End of while() loop

            // Finalize
            //finalizeThread();

        }	// End of run()

        void setKill(){
            m_kill = true;
        }
    }

}
