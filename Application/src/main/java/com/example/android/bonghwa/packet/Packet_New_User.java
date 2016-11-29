package com.example.android.bonghwa.packet;

import com.example.android.bonghwa.GroupInfo.User;
import com.example.android.bonghwa.Manager;

public class Packet_New_User extends Packet_Command
{
    public long group;
    public long userID;
    public User userInfo = new User();

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
