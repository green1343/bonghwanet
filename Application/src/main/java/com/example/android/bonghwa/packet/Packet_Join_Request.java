package com.example.android.bonghwa.packet;

import com.example.android.bonghwa.GroupInfo.User;
import com.example.android.bonghwa.Manager;

public class Packet_Join_Request extends Packet_Command
{
    public long group;
    public long userID;
    public User userInfo = new User();

    public Packet_Join_Request(){
        setCommand((short) PACKET.PACKET_JOIN_REQUEST);
    }

    public Packet_Join_Request(byte[] buf){

        super(buf);

        group = unpackLong();
        userID = unpackLong();
        userInfo.name = unpackString();
    }

    public void GetBytes(byte[] buf){

        super.GetBytes(buf);

        pack(group);
        pack(userID);
        pack(userInfo.name);
    }
}
