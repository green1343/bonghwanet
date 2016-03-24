package com.example.android.basicaccessibility;

import com.example.android.packet.PACKET;
import com.example.android.packet.Packet_Command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by 초록 on 2015-10-05.
 */
public enum Network {
    INSTANCE;

    final String SERVER = "192.168.1.60";
    final int PORT = 11000;
    final int TIMEOUT = 10000;

    Socket m_clientSocket;
    private OutputStream m_outstream = null;
    private InputStream m_instream;
    private BufferedReader m_reader;
    private BufferedWriter m_writer;

    NetworkListener m_listener;

    public void init(){

        try {
            m_clientSocket = new Socket(SERVER, PORT);
            //m_clientSocket.setSoTimeout(TIMEOUT);

            //m_writer = new BufferedWriter(new OutputStreamWriter(m_clientSocket.getOutputStream(), "UTF-8"));
            m_outstream = m_clientSocket.getOutputStream();
            m_instream = m_clientSocket.getInputStream();
            m_writer = new BufferedWriter(new OutputStreamWriter(m_clientSocket.getOutputStream()));
            m_reader = new BufferedReader(new InputStreamReader(m_clientSocket.getInputStream()));

            m_listener = new NetworkListener(m_clientSocket.getInputStream());
            m_listener.start();

        } catch (IOException ex) {
            //System.err.println(ex);
        } finally { // dispose
            /*if (m_clientSocket != null) {
                try {
                    m_clientSocket.close();
                } catch (IOException ex) {
                    // ignore
                }
            }*/
        }
    }

    void write(byte [] b)
    {
        if(m_outstream == null)
            return;

        /*String s = new String(b);
        PrintWriter out = new PrintWriter(m_writer, true);
        out.println(s);*/

        try {
            m_outstream.write(b);
            m_outstream.flush();
        }
        catch (IOException ex) {
            //System.err.println(ex);
        }
    }

    void write(Packet_Command p)
    {
        byte[] b = new byte[1024];
        p.GetBytes(b);
        Network.INSTANCE.write(b);
    }

    void destroy()
    {
        /*if(m_clientListener != null && m_clientListener.isAlive())
            m_clientListener.interrupt();
        m_clientListener.setKill();
        m_clientListener = null;*/
    }

    public class NetworkListener extends Thread{

        private InputStream m_stream;
        private boolean m_kill = false;

        public NetworkListener(InputStream stream)
        {
            m_stream = stream;
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
                    byte stream[] = new byte[1024];
                    m_stream.read(stream);
                    Packet_Command cmd = new Packet_Command(stream); // new

                    switch (cmd.getCommand())
                    {
                        case PACKET.PACKET_GROUPLIST: {

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
