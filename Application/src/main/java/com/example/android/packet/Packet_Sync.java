package com.example.android.packet;

import com.example.android.basicaccessibility.Manager;

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

    public Packet_Sync(byte[] buf)
    {
        super(buf);

        group = unpackLong(buf);

        int size = unpackInt(buf);
        for(int i=0; i<size; ++i){
            boolean isDirectory = unpackBool(buf);
            long time = unpackLong(buf);
            String filename = unpackString(buf);

            files.add(Manager.INSTANCE.getNewFile(isDirectory, time, filename));
        }
    }

    public void GetBytes(byte[] buf)
    {
        super.GetBytes(buf);

        pack(group, buf);
        pack(files.size(), buf);
        for(Manager.File f : files) {
            pack(f.isDirectory, buf);
            pack(f.time, buf);
            pack(f.filename, buf);
        }
    }
}
