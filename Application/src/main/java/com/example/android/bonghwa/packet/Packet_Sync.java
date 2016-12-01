package com.example.android.bonghwa.packet;

import com.example.android.bonghwa.GroupInfo.GroupFile;
import com.example.android.bonghwa.Manager;

import java.util.HashMap;

public class PacketSync extends PacketCommand {

    public long group;
    public HashMap<String, GroupFile> files = new HashMap<>();

    public PacketSync(){
        setCommand((short) PACKET.PACKET_SYNC);
    }

    public PacketSync(byte[] buf)
    {
        super(buf);

        group = unpackLong();

        int size = unpackInt();
        for(int i=0; i<size; ++i){
            String filename = unpackString();
            boolean isDirectory = unpackBool();
            long time = unpackLong();

            files.put(filename, new GroupFile(filename, isDirectory, time));
        }
    }

    public void getBytes(byte[] buf)
    {
        super.getBytes(buf);

        pack(group);
        pack(files.size());
        for(String filename : files.keySet()) {
            GroupFile f = files.get(filename);
            pack(filename);
            pack(f.isDirectory);
            pack(f.time);
        }
    }
}
