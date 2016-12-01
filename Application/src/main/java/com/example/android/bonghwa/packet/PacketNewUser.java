package com.example.android.bonghwa.packet;

import com.example.android.bonghwa.GroupInfo.User;
import com.example.android.bonghwa.Manager;

public class PacketNewUser extends PacketCommand
{
    public long group;
    public long userID;
    public User userInfo = new User();

    public PacketNewUser(){
        setCommand((short) PACKET.PACKET_NEW_USER);
    }

    public PacketNewUser(byte[] buf){

        super(buf);

        group = unpackLong();
        userID = unpackLong();
        userInfo.name = unpackString();
    }

    public void getBytes(byte[] buf){

        super.getBytes(buf);

        pack(group);
        pack(userID);
        pack(userInfo.name);
    }
}
