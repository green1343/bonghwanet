package com.example.android.packet;

import android.util.Log;

import com.example.android.needclass.BigEndianByteHandler;

/**
 * Created by Kim on 2015-04-03.
 */
public class Packet_Share_File_Request extends Packet_Share
{
    public String filename;

    public Packet_Share_File_Request(){
        setCommand((short) PACKET.PACKET_SHARE_FILE_REQUEST);
    }

    public Packet_Share_File_Request(byte[] data){

        super(data);

        setCommand((short) PACKET.PACKET_SHARE_FILE_REQUEST);

        int place = Packet_Share.SIZE;
        int size = BigEndianByteHandler.byteToInt(data, place);
        place += 4;
        try {
            filename = new String(data, place, size, "UTF-8");
        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }
    }

    public int GetBytes(byte[] data){

        int place = super.GetBytes(data);

        try{
            System.arraycopy(BigEndianByteHandler.intToByte(filename.length()), 0, data, place, 4);
            place += 4;
            System.arraycopy(filename.getBytes("UTF-8"), 0, data, place, filename.length());
            place += filename.length();

        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }
        return place;
    }
}
