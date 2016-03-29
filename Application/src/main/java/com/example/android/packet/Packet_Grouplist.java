package com.example.android.packet;

import com.example.android.basicaccessibility.Manager;

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
            g.members = new HashMap<>();
            g.deletedFiles = new HashMap<>();

            int size2 = unpackInt(buf);
            for(int j=0; j<size2; ++j) {
                long userID = unpackLong(buf);
                Manager.UserInfo u = Manager.INSTANCE.getNewUserInfo();
                u.name = unpackString(buf);
                g.members.put(userID, u);
            }

            size2 = unpackInt(buf);
            for(int j=0; j<size2; ++j){
                String filename = unpackString(buf);
                Manager.FileInfo df = Manager.INSTANCE.getNewDeletedFile();
                df.isDirectory = unpackBool(buf);
                df.time = unpackLong(buf);
                g.deletedFiles.put(filename, df);
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

            pack(g.members.size(), buf);
            for(Long userID : g.members.keySet()){
                Manager.UserInfo u = g.members.get(userID);
                pack(userID, buf);
                pack(u.name, buf);
            }

            pack(g.deletedFiles.size(), buf);
            for(String filename : g.deletedFiles.keySet()){
                Manager.FileInfo df = g.deletedFiles.get(filename);
                pack(filename, buf);
                pack(df.isDirectory, buf);
                pack(df.time, buf);
            }
        }
    }
}
