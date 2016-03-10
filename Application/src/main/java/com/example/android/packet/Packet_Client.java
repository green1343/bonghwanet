package com.example.android.packet;

import android.util.Log;

import com.example.android.needclass.BigEndianByteHandler;

/**
 * Created by Kim on 2015-04-07.
 */

abstract public class Packet_Client extends Packet_Command {
    static public int s_id = 0;
    static public String s_auth = "        "; // 8글자

    protected Packet_Client()
    {
    }

    public Packet_Client(byte[] data)
    {
        super(data);

        int place = 2;
        s_id = BigEndianByteHandler.byteToInt(data, place);
        // s_id = BitConverter.ToInt32(data, place);

        place += 4;

        //s_auth = Encoding.ASCII.GetString(data, place, 8);
        try {
            //s_auth = new String(data, place, 8, "UTF-8");
            s_auth = new String(data, place, 8);
        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }


    }

    public int GetBytes(byte[] data)
    {
        int place = super.GetBytes(data);

        System.arraycopy(BigEndianByteHandler.intToByte(s_id), 0, data, place, 4);
        place += 4;

        if (s_auth.length() != 8)
            //Buffer.BlockCopy(Encoding.ASCII.GetBytes("        "), 0, data, place, 8);
            System.exit(0);
        else {
            //Buffer.BlockCopy(Encoding.ASCII.GetBytes(s_auth), 0, data, place, 8); 소스, 소스시작장소, 목적, 목적시작장소, 사이즈
            try {
                byte bytearr[] = s_auth.getBytes();
                //byte bytearr[] = s_auth.getBytes("utf-8");
                System.arraycopy(bytearr,0,data,place,8);
                //data = ByteBuffer.
            }catch (Exception e){
                Log.e("Encoding","Not possible encoding");
            }

        }
        place += 8;

        return place;
    }
}
