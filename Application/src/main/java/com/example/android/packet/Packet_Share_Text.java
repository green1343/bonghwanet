package com.example.android.packet;

import android.util.Log;

import com.example.android.needclass.BigEndianByteHandler;
import com.example.android.needclass.Tuple4;

import java.util.ArrayList;

/**
 * Created by Kim on 2015-04-03.
 */
public class Packet_Share_Text extends Packet_Share
{
    public String text;

    public Packet_Share_Text(){
        setCommand((short) PACKET.PACKET_SHARE_TEXT);
    }

    public Packet_Share_Text(byte[] data){

        super(data);

        setCommand((short) PACKET.PACKET_SHARE_TEXT);

        int place = Packet_Share.SIZE;
        int size = BigEndianByteHandler.byteToInt(data, place);
        place += 4;
        try {
            text = new String(data, place, size, "UTF-8");
            place += size;
        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }
    }

    public int GetBytes(byte[] data){

        int place = super.GetBytes(data);

        try{
            System.arraycopy(BigEndianByteHandler.intToByte(text.length()), 0, data, place, 4);
            place += 4;
            System.arraycopy(text.getBytes("UTF-8"), 0, data, place, text.length());
            place += text.length();

        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }
        return place;
    }
}
