package com.example.android.packet;

public class Packet_Share_File_Request extends Packet_Command
{
    public long group;
    public String filename;

    public Packet_Share_File_Request(){
        setCommand((short) PACKET.PACKET_SHARE_FILE_REQUEST);
    }

    public Packet_Share_File_Request(byte[] buf){

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
