package com.example.android.packet;

import com.example.android.basicaccessibility.Manager;

import java.util.HashMap;

public class Packet_Sync extends Packet_Command {

    public long group;
    public HashMap<String, Manager.FileInfo> files = new HashMap<>();

    public Packet_Sync(){
        setCommand((short) PACKET.PACKET_SYNC);
    }

    public Packet_Sync(byte[] buf)
    {
        super(buf);

        group = unpackLong();

        int size = unpackInt();
        for(int i=0; i<size; ++i){
            String filename = unpackString();
            boolean isDirectory = unpackBool();
            long time = unpackLong();

            files.put(filename, Manager.INSTANCE.getNewFileInfo(isDirectory, time));
        }
    }

    public void GetBytes(byte[] buf)
    {
        super.GetBytes(buf);

        pack(group);
        pack(files.size());
        for(String filename : files.keySet()) {
            Manager.FileInfo f = files.get(filename);
            pack(filename);
            pack(f.isDirectory);
            pack(f.time);
        }
    }
}
