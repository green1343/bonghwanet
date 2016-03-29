package com.example.android.packet;

import com.example.android.basicaccessibility.Manager;

/**
 * Created by Kim on 2015-04-03.
 */
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

        group = unpackLong(buf);
        userID = unpackLong(buf);
        userInfo.name = unpackString(buf);
    }

    public void GetBytes(byte[] buf){

        super.GetBytes(buf);

        pack(group, buf);
        pack(userID, buf);
        pack(userInfo.name, buf);
    }
}
