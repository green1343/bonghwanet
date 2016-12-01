package com.example.android.bonghwa.packet;

public class PacketShareText extends PacketCommand
{
    public long group;
    public long uploader;
    public long time;
    public String text;

    public PacketShareText(){
        setCommand((short) PACKET.PACKET_SHARE_TEXT);
    }

    public PacketShareText(byte[] buf){

        super(buf);

        group = unpackLong();
        uploader = unpackLong();
        time = unpackLong();
        text = unpackString();
    }

    public void getBytes(byte[] buf){

        super.getBytes(buf);

        pack(group);
        pack(uploader);
        pack(time);
        pack(text);
    }
}
