package com.example.android.packet;

public class Packet_Share_File_Request_OK extends Packet_Command
{
    public long group;
    public String filename;

    public Packet_Share_File_Request_OK(){
        setCommand((short) PACKET.PACKET_SHARE_FILE_REQUEST_OK);
    }

    public Packet_Share_File_Request_OK(byte[] buf){

        super(buf);

        group = unpackLong();
        filename = unpackString();
    }

    public void GetBytes(byte[] buf){

        super.GetBytes(buf);

        pack(group);
        pack(filename);
    }
}
