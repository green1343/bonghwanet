package com.example.android.packet;

import android.util.Log;

import com.example.android.basicaccessibility.Manager;
import com.example.android.needclass.BigEndianByteHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kim on 2015-04-07.
 */

public class Packet_Grouplist extends Packet_Command {

    public HashMap<Long, Manager.GroupInfo> groups;

    protected Packet_Grouplist(){
        setCommand((short) PACKET.PACKET_GROUPLIST);
    }

    public Packet_Grouplist(byte[] data)
    {
        super(data);
        setCommand((short) PACKET.PACKET_GROUPLIST);

        groups = new HashMap<>();

        int place = 2;

        try {
            int size1 = BigEndianByteHandler.byteToInt(data, place);
            place += 4;

            for(int i=0; i<size1; ++i){
                long id = BigEndianByteHandler.byteToLong(data, place);
                place += 8;

                Manager.GroupInfo g = Manager.INSTANCE.getNewGroupInfo();

                int size2 = BigEndianByteHandler.byteToInt(data, place);
                place += 4;
                g.name = new String(data, place, size2, "UTF-8");
                place += size2;
                g.mode = BigEndianByteHandler.byteToInt(data, place);
                place += 4;

                g.deletedFiles = new ArrayList<>();

                int size3 = BigEndianByteHandler.byteToInt(data, place);
                place += 4;
                for(int j=0; j<size3; ++j){
                    Manager.File df = Manager.INSTANCE.getNewDeletedFile();
                    int size4 = BigEndianByteHandler.byteToInt(data, place);
                    place += 4;
                    df.filename = new String(data, place, size4, "UTF-8");
                    place += size4;
                    df.time = BigEndianByteHandler.byteToLong(data, place);
                    place += 8;
                    g.deletedFiles.add(df);
                }

                groups.put(id, g);
            }
        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }
    }

    public int GetBytes(byte[] data)
    {
        int place = super.GetBytes(data);
        try {
            System.arraycopy(BigEndianByteHandler.intToByte(groups.size()), 0, data, place, 4);
            place += 4;

            for(Long id : groups.keySet()){
                Manager.GroupInfo g = groups.get(id);

                System.arraycopy(BigEndianByteHandler.longToByte(id), 0, data, place, 8);
                place += 8;

                System.arraycopy(BigEndianByteHandler.intToByte(g.name.length()), 0, data, place, 4);
                place += 4;
                System.arraycopy(g.name.getBytes("UTF-8"), 0, data, place, g.name.length());
                place += g.name.length();
                System.arraycopy(BigEndianByteHandler.intToByte(g.mode), 0, data, place, 4);
                place += 4;

                System.arraycopy(BigEndianByteHandler.intToByte(g.deletedFiles.size()), 0, data, place, 4);
                place += 4;
                for(Manager.File df : g.deletedFiles){
                    System.arraycopy(BigEndianByteHandler.intToByte(df.filename.length()), 0, data, place, 4);
                    place += 4;
                    System.arraycopy(df.filename.getBytes("UTF-8"), 0, data, place, df.filename.length());
                    place += df.filename.length();
                    System.arraycopy(BigEndianByteHandler.longToByte(df.time), 0, data, place, 8);
                    place += 8;
                }
            }
        }catch (Exception e){
            Log.e("Encoding", "Not possible encoding");
        }
        return place;
    }
}
