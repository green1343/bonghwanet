package com.example.android.bonghwa.packet;

public class PacketShareFileRequestOK extends PacketCommand
{
    public long group;
    public String filename;
    public int port;

    public PacketShareFileRequestOK(){
        setCommand((short) PACKET.PACKET_SHARE_FILE_REQUEST_OK);
    }

    public PacketShareFileRequestOK(byte[] buf){

        super(buf);

        group = unpackLong();
        filename = unpackString();
        port = unpackInt();
    }

    public void getBytes(byte[] buf){

        super.getBytes(buf);

        pack(group);
        pack(filename);
        pack(port);
    }
}
