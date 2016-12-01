package com.example.android.bonghwa.packet;

import com.example.android.bonghwa.GroupInfo.Group;
import com.example.android.bonghwa.GroupInfo.GroupFile;
import com.example.android.bonghwa.Manager;
import com.example.android.bonghwa.GroupInfo.User;

import java.util.HashMap;

public class PacketGrouplist extends PacketCommand {

    public long id;
    public HashMap<Long, Group> groups = new HashMap<>();

    public PacketGrouplist(){
        setCommand((short) PACKET.PACKET_GROUPLIST);
    }

    public PacketGrouplist(byte[] buf)
    {
        super(buf);

        id = unpackLong();

        int size1 = unpackInt();
        for(int i=0; i<size1; ++i){
            long gid = unpackLong();

            Group g = new Group();
            g.id = gid;

            g.name = unpackString();

            g.members = new HashMap<>();
            g.files = new HashMap<>();
            g.deletedFiles = new HashMap<>();

            int size2 = unpackInt();
            for(int j=0; j<size2; ++j) {
                long userID = unpackLong();
                User u = new User();
                u.name = unpackString();
                g.members.put(userID, u);
            }

            size2 = unpackInt();
            for(int j=0; j<size2; ++j){
                String filename = unpackString();
                boolean isDirectory = unpackBool();
                long time = unpackLong();

                g.deletedFiles.put(filename, new GroupFile(filename, isDirectory, time));
            }

            groups.put(gid, g);
        }
    }

    public void getBytes(byte[] buf)
    {
        super.getBytes(buf);

        pack(id);
        pack(groups.size());

        for(Long gid : groups.keySet()){
            pack(gid);
            Group g = groups.get(gid);

            pack(g.name);

            pack(g.members.size());
            for(Long userID : g.members.keySet()){
                User u = g.members.get(userID);
                pack(userID);
                pack(u.name);
            }

            pack(g.deletedFiles.size());
            for(GroupFile gf : g.deletedFiles.values())
            {
                pack(gf.path);
                pack(gf.isDirectory);
                pack(gf.time);
            }
        }
    }
}
