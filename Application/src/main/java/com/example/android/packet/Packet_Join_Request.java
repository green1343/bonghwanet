package com.example.android.packet;

import com.example.android.basicaccessibility.Manager;

public class Packet_Join_Request extends Packet_Command
{
    public long group;
    public long userID;
    public Manager.UserInfo userInfo = Manager.INSTANCE.getNewUserInfo();

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
