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

    public HashMap<Long, Manager.GroupInfo> groups = new HashMap<>();

    public Packet_Grouplist(){
        setCommand((short) PACKET.PACKET_GROUPLIST);
    }

    public Packet_Grouplist(byte[] buf)
    {
        super(buf);

        int size1 = unpackInt(buf);
        for(int i=0; i<size1; ++i){
            long id = unpackLong(buf);

            Manager.GroupInfo g = Manager.INSTANCE.getNewGroupInfo();

            g.name = unpackString(buf);
            g.mode = unpackInt(buf);
            g.deletedFiles = new ArrayList<>();

            int size3 = unpackInt(buf);
            for(int j=0; j<size3; ++j){
                Manager.File df = Manager.INSTANCE.getNewDeletedFile();
                int size4 = unpackInt(buf);
                df.filename = unpackString(buf);
                df.time = unpackLong(buf);
                g.deletedFiles.add(df);
            }

            groups.put(id, g);
        }
    }

    public void GetBytes(byte[] buf)
    {
        super.GetBytes(buf);

        pack(groups.size(), buf);

        for(Long id : groups.keySet()){
            Manager.GroupInfo g = groups.get(id);

            pack(id, buf);
            pack(g.name, buf);
            pack(g.mode, buf);
            pack(g.deletedFiles.size(), buf);

            for(Manager.File df : g.deletedFiles){
                pack(df.filename, buf);
                pack(df.time, buf);
            }
        }
    }
}
