package com.example.android.packet;

import android.util.Log;

import com.example.android.needclass.BigEndianByteHandler;

/**
 * Created by Kim on 2015-04-07.
 */
public class Packet_Login_Request_Return extends  Packet_Client{
    public int id;
    public String auth;

    public Packet_Login_Request_Return(){}
    public Packet_Login_Request_Return(byte data[])
    {
        setCommand((short) PACKET.PACKET_LOGIN_REQUEST_RETURN);

        int place = 2;
        id = BigEndianByteHandler.byteToInt(data, place);
        // s_id = BitConverter.ToInt32(data, place);

        place += 4;

        //s_auth = Encoding.ASCII.GetString(data, place, 8);
        try {
            auth = new String(data, place, 8, "UTF-8");

        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }
    }

    public int GetBytes(byte[] data){

        int place = super.GetBytes(data);

        try{
            System.arraycopy(BigEndianByteHandler.intToByte(id), 0, data, place, 4);
            place += 4;

            System.arraycopy(BigEndianByteHandler.intToByte(auth.length()), 0, data, place, 4);
            place += 4;

            System.arraycopy(auth.getBytes("UTF-8"), 0, data, place, auth.length());
            //temp = BigEndianByteHandler.byteToInt(data,place);
            place += auth.length();

        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }
        return place;
    }
}
