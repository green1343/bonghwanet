package com.example.android.packet;

import com.example.android.basicaccessibility.Manager;

public class Packet_New_User extends Packet_Command
{
    public long group;
    public long userID;
    public Manager.UserInfo userInfo = Manager.INSTANCE.getNewUserInfo();

    public Packet_New_User(){
        setCommand((short) PACKET.PACKET_NEW_USER);
    }

    public Packet_New_User(byte[] buf){

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
