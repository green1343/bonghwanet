package com.example.android.basicaccessibility;

import android.os.Handler;
import android.os.Message;
import android.util.Pair;

import com.example.android.packet.PACKET;
import com.example.android.packet.Packet_Command;
import com.example.android.packet.Packet_Grouplist;
import com.example.android.packet.Packet_Share_File_Request;
import com.example.android.packet.Packet_Share_File_Request_OK;
import com.example.android.packet.Packet_Share_Text;
import com.example.android.packet.Packet_Sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by 초록 on 2015-10-05.
 */
public enum WiFiNetwork {
    INSTANCE;

    final String SERVERADDRESS = "192.168.43.1";
    final int PORT = 11000;
    final int TIMEOUT = 10000;
    final int BUFFERSIZE = 1024;

    int m_index = 0;

    Server m_server = null;
    Client m_client = null;
    HashMap<Integer, Pair<NetworkSpeaker, NetworkListener>> m_threads = new HashMap<>();

    private Handler m_handler = new Handler() {
        public void handleMessage(Message msg) {
            Pair<Integer, Packet_Command> pair = (Pair)msg.obj;
            m_threads.get(pair.first).first.write(pair.second);
        }
    };

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
    }

    public void initServer(){

        clearAll();

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            m_server = new Server(serverSocket);
            m_server.start();
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

    public void initClient(){
        clearAll();

        m_client = new Client();
        m_client.start();
    }

    public void write(Packet_Command p, int index)
    {
        Message msg = Message.obtain(m_handler, 0 , 1 , 0);
        msg.obj = new Pair<Integer, Packet_Command>(index, p);
        m_handler.sendMessage(msg);
    }

    public void writeAll(Packet_Command p)
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
                    NetworkSpeaker speaker = new NetworkSpeaker(m_index, socket.getOutputStream(), m_handler);
                    NetworkListener listener = new NetworkListener(m_index, socket.getInputStream(), m_handler);
                    m_threads.put(m_index, new Pair<>(speaker, listener));
                    ++m_index;
                    listener.start();

                    Packet_Grouplist p = new Packet_Grouplist();
                    p.groups = (HashMap<Long, Manager.GroupInfo>)Manager.INSTANCE.getAllGroups().clone();
                    WiFiNetwork.INSTANCE.write(p, m_index-1);

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

    public class Client extends Thread{

        private boolean m_kill = false;

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
                    Socket clientSocket = new Socket(SERVERADDRESS, PORT);
                    NetworkSpeaker speaker = new NetworkSpeaker(m_index, clientSocket.getOutputStream(), m_handler);
                    NetworkListener listener = new NetworkListener(m_index, clientSocket.getInputStream(), m_handler);
                    m_threads.put(m_index, new Pair<>(speaker, listener));
                    ++m_index;
                    listener.start();

                    Packet_Grouplist p = new Packet_Grouplist();
                    p.groups = (HashMap<Long, Manager.GroupInfo>)Manager.INSTANCE.getAllGroups().clone();
                    WiFiNetwork.INSTANCE.write(p, m_index - 1);

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

    public class NetworkSpeaker extends Thread{

        int m_index;
        OutputStream m_outStream;
        Handler m_handler;
        boolean m_kill = false;

        public NetworkSpeaker(int index, OutputStream outstream, Handler handler)
        {
            m_index = index;
            m_outStream = outstream;
            m_handler = handler;
        }

        public void write(Packet_Command p)
        {
            if(m_outStream == null)
                return;

            byte[] b = new byte[BUFFERSIZE];
            p.GetBytes(b);

            try {
                m_outStream.write(b);
                m_outStream.flush();
                if(p.getCommand() == PACKET.PACKET_SHARE_FILE_REQUEST_OK){
                    writeFile(((Packet_Share_File_Request_OK)p).filename);
                }
            }
            catch (IOException ex) {
                //System.err.println(ex);
            }
        }

        void writeFile(String filename)
        {
            try {
                File f = new File(filename);
                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis);

                int len;
                byte[] buf = new byte[BUFFERSIZE];
                while ((len = bis.read(buf)) != -1) {
                    m_outStream.write(buf, 0, len);
                }

                m_outStream.flush();
            }
            catch (IOException ex) {
                //System.err.println(ex);
            }
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

    public class NetworkListener extends Thread{

        int m_index;
        InputStream m_inStream;
        Handler m_handler;
        boolean m_kill = false;

        public NetworkListener(int index, InputStream instream, Handler handler)
        {
            m_index = index;
            m_inStream = instream;
            m_handler = handler;
        }

        @Override
        public void run()
        {
            while(!Thread.interrupted())
            {
                try {
                    byte stream[] = new byte[BUFFERSIZE];
                    m_inStream.read(stream);
                    Packet_Command cmd = new Packet_Command(stream); // new

                    switch (cmd.getCommand())
                    {
                        case PACKET.PACKET_SHARE_TEXT: {
                            Packet_Share_Text p = new Packet_Share_Text(stream);
                            System.out.println(p.text);
                            break;
                        }
                        case PACKET.PACKET_GROUPLIST: {
                            Packet_Grouplist p = new Packet_Grouplist(stream);
                            for(Long id : Manager.INSTANCE.getAllGroups().keySet()){
                                if(p.groups.get(id) != null){
                                    // TODO : send sync
                                }
                            }
                            break;
                        }
                        case PACKET.PACKET_SYNC: {
                            Packet_Sync p = new Packet_Sync(stream);

                            break;
                        }
                        case PACKET.PACKET_SHARE_FILE_REQUEST:{
                            Packet_Share_File_Request p = new Packet_Share_File_Request(stream);
                            Packet_Share_File_Request_OK reply = new Packet_Share_File_Request_OK();
                            reply.filename = p.filename;
                            Message msg = Message.obtain(m_handler, 0 , 1 , 0);
                            msg.obj = new Pair<Integer, Packet_Command>(m_index, reply);
                            m_handler.sendMessage(msg);
                        }
                        case PACKET.PACKET_SHARE_FILE_REQUEST_OK: {
                            Packet_Share_File_Request_OK p = new Packet_Share_File_Request_OK(stream);
                            try {
                                File f = new File(p.filename);
                                FileOutputStream fos = new FileOutputStream(f);
                                BufferedOutputStream bos = new BufferedOutputStream(fos);

                                // 바이트 데이터를 전송받으면서 기록
                                int len;
                                byte[] buf = new byte[BUFFERSIZE];
                                while ((len = m_inStream.read(buf)) != -1) {
                                    bos.write(buf, 0, len);
                                }

                                bos.flush();
                            }
                            catch(IOException e){
                            }
                            
                            break;
                        }
                        default:
                            break;
                    }
                } catch (IOException ex) {
                    //System.err.println(ex);
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

}
