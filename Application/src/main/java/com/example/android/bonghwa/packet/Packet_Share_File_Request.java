package com.example.android.bonghwa.packet;

public class Packet_Share_File_Request extends Packet_Command
{
    public long group;
    public String filename;
    public int port;

    public Packet_Share_File_Request(){
        setCommand((short) PACKET.PACKET_SHARE_FILE_REQUEST);
    }

    public Packet_Share_File_Request(byte[] buf){

        super(buf);

        group = unpackLong();
        filename = unpackString();
        port = unpackInt();
    }

    public void GetBytes(byte[] buf){

        super.GetBytes(buf);

        pack(group);
        pack(filename);
        pack(port);
    }
}
