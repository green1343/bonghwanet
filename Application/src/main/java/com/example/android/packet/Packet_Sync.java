package com.example.android.packet;

import com.example.android.basicaccessibility.Manager;

import java.util.HashMap;

/**
 * Created by Kim on 2015-04-07.
 */

public class Packet_Sync extends Packet_Command {

    public long group;
    public HashMap<String, Manager.FileInfo> files = new HashMap<>();

    public Packet_Sync(){
        setCommand((short) PACKET.PACKET_SYNC);
    }

    public Packet_Sync(byte[] buf)
    {
        super(buf);

        group = unpackLong(buf);

        int size = unpackInt(buf);
        for(int i=0; i<size; ++i){
            String filename = unpackString(buf);
            boolean isDirectory = unpackBool(buf);
            long time = unpackLong(buf);

            files.put(filename, Manager.INSTANCE.getNewFileInfo(isDirectory, time));
        }
    }

    public void GetBytes(byte[] buf)
    {
        super.GetBytes(buf);

        pack(group, buf);
        pack(files.size(), buf);
        for(String filename : files.keySet()) {
            Manager.FileInfo f = files.get(filename);
            pack(filename, buf);
            pack(f.isDirectory, buf);
            pack(f.time, buf);
        }
    }
}
