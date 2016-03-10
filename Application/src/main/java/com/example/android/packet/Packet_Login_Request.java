package com.example.android.packet;

import android.util.Log;

import com.example.android.needclass.BigEndianByteHandler;

/**
 * Created by Kim on 2015-04-03.
 */
public class Packet_Login_Request extends Packet_Client
{
    public String id;
    public String pw;

    public Packet_Login_Request(){
        setCommand((short) PACKET.PACKET_LOGIN_REQUEST);
    }

    public Packet_Login_Request(byte[] data){

        super(data);

        setCommand((short) PACKET.PACKET_LOGIN_REQUEST);

        try {
            int place = 14;
            int size;
            size = BigEndianByteHandler.byteToInt(data, place);
            place += 4;
            id = new String(data, place, size, "UTF-8");
            place += size;
            size = BigEndianByteHandler.byteToInt(data, place);
            place += 4;
            id = new String(data, place, size, "UTF-8");
            place += size;
        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }
    }

    public int GetBytes(byte[] data){

        int place = super.GetBytes(data);

        try{
            System.arraycopy(BigEndianByteHandler.intToByte(id.length()), 0, data, place, 4);
            place += 4;

            System.arraycopy(id.getBytes("UTF-8"), 0, data, place, id.length());
            //temp = BigEndianByteHandler.byteToInt(data,place);
            place += id.length();

            System.arraycopy(BigEndianByteHandler.intToByte(pw.length()), 0, data, place, 4);
            place += 4;

            //temp = m_PassWord.getBytes("UTF-8");_
            System.arraycopy(pw.getBytes("UTF-8"), 0, data, place, pw.length());
            place += pw.length();


            //m_string = Encoding.ASCII.GetString(data, place, m_stringSize);

            //m_string = new String(data, place, m_stringSize, "UTF-8");
        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }
        return place;
    }
}
