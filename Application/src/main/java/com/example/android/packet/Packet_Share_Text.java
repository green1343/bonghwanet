package com.example.android.packet;

/**
 * Created by Kim on 2015-04-03.
 */
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

        group = unpackLong(buf);
        uploader = unpackLong(buf);
        time = unpackLong(buf);
        text = unpackString(buf);
    }

    public void GetBytes(byte[] buf){

        super.GetBytes(buf);

        pack(group, buf);
        pack(uploader, buf);
        pack(time, buf);
        pack(text, buf);
    }
}
