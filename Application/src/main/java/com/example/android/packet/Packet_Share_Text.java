package com.example.android.packet;

public class Packet_Share_Text extends Packet_Command
{
    public long group;
    public long uploader;
    public long time;
    public String text;

    public Packet_Share_Text(){
        setCommand((short) PACKET.PACKET_SHARE_TEXT);
    }

    public Packet_Share_Text(byte[] buf){

        super(buf);

        group = unpackLong();
        uploader = unpackLong();
        time = unpackLong();
        text = unpackString();
    }

    public void GetBytes(byte[] buf){

        super.GetBytes(buf);

        pack(group);
        pack(uploader);
        pack(time);
        pack(text);
    }
}
