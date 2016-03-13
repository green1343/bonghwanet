package com.example.android.packet;

import android.util.Log;

import com.example.android.basicaccessibility.Manager;
import com.example.android.needclass.BigEndianByteHandler;

import java.util.ArrayList;

/**
 * Created by Kim on 2015-04-07.
 */

public class Packet_Sync extends Packet_Command {

    public long group;
    public ArrayList<Manager.File> files = new ArrayList<>();

    public Packet_Sync(){
        setCommand((short) PACKET.PACKET_SYNC);
    }

    public Packet_Sync(byte[] data)
    {
        super(data);
        setCommand((short) PACKET.PACKET_SYNC);

        int place = 2;
        group = BigEndianByteHandler.byteToLong(data, place);
        place += 8;
        int size1 = BigEndianByteHandler.byteToInt(data, place);
        place += 4;
        for(int i=0; i<size1; ++i) {
            boolean isDirectory = BigEndianByteHandler.byteToBool(data, place);
            place += 1;
            long time = BigEndianByteHandler.byteToLong(data, place);
            place += 8;
            int size2 = BigEndianByteHandler.byteToInt(data, place);
            place += 4;
            String filename = new String();
            try {
                filename = new String(data, place, size2, "UTF-8");
            }catch (Exception e){
                Log.e("Encoding", "Not possible encoding");
            }
            files.add(Manager.INSTANCE.getNewFile(isDirectory, time, filename));
            place += 8;
        }
    }

    public int GetBytes(byte[] data)
    {
        int place = super.GetBytes(data);

        System.arraycopy(BigEndianByteHandler.longToByte(group), 0, data, place, 8);
        place += 8;

        System.arraycopy(BigEndianByteHandler.intToByte(files.size()), 0, data, place, 4);
        place += 4;
        for(Manager.File f : files) {
            System.arraycopy(BigEndianByteHandler.boolToByte(f.isDirectory), 0, data, place, 1);
            place += 1;
            System.arraycopy(BigEndianByteHandler.longToByte(f.time), 0, data, place, 8);
            place += 8;
            System.arraycopy(BigEndianByteHandler.intToByte(f.filename.length()), 0, data, place, 4);
            place += 4;
            try{
                System.arraycopy(f.filename.getBytes("UTF-8"), 0, data, place, f.filename.length());
                place += f.filename.length();
            }catch (Exception e){
                Log.e("Encoding", "Not possible encoding");
            }
        }

        return place;
    }
}
