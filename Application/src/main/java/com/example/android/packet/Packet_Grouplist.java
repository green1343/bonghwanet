package com.example.android.packet;

import com.example.android.basicaccessibility.Manager;

import java.util.HashMap;

public class Packet_Grouplist extends Packet_Command {

    public HashMap<Long, Manager.GroupInfo> groups = new HashMap<>();

    public Packet_Grouplist(){
        setCommand((short) PACKET.PACKET_GROUPLIST);
    }

    public Packet_Grouplist(byte[] buf)
    {
        super(buf);

        int size1 = unpackInt();
        for(int i=0; i<size1; ++i){
            long id = unpackLong();

            Manager.GroupInfo g = Manager.INSTANCE.getNewGroupInfo();

            g.name = unpackString();
            g.members = new HashMap<>();
            g.deletedFiles = new HashMap<>();

            int size2 = unpackInt();
            for(int j=0; j<size2; ++j) {
                long userID = unpackLong();
                Manager.UserInfo u = Manager.INSTANCE.getNewUserInfo();
                u.name = unpackString();
                g.members.put(userID, u);
            }

            size2 = unpackInt();
            for(int j=0; j<size2; ++j){
                String filename = unpackString();
                Manager.FileInfo df = Manager.INSTANCE.getNewDeletedFile();
                df.isDirectory = unpackBool();
                df.time = unpackLong();
                g.deletedFiles.put(filename, df);
            }

            groups.put(id, g);
        }
    }

    public void GetBytes(byte[] buf)
    {
        super.GetBytes(buf);

        pack(groups.size());

        for(Long id : groups.keySet()){
            Manager.GroupInfo g = groups.get(id);

            pack(id);
            pack(g.name);

            pack(g.members.size());
            for(Long userID : g.members.keySet()){
                Manager.UserInfo u = g.members.get(userID);
                pack(userID);
                pack(u.name);
            }

            pack(g.deletedFiles.size());
            for(String filename : g.deletedFiles.keySet()){
                Manager.FileInfo df = g.deletedFiles.get(filename);
                pack(filename);
                pack(df.isDirectory);
                pack(df.time);
            }
        }
    }
}
