package com.example.android.packet;

/**
 * Created by Kim on 2015-04-03.
 */
public class Packet_Share_File_Request extends Packet_Command
{
    public long group;
    public long uploader;
    public long time;
    public String filename;

    public Packet_Share_File_Request(){
        setCommand((short) PACKET.PACKET_SHARE_FILE_REQUEST);
    }

    public Packet_Share_File_Request(byte[] buf){

        super(buf);

        group = unpackLong();
        uploader = unpackLong();
        time = unpackLong();
        filename = unpackString();
    }

    public void GetBytes(byte[] buf){

        super.GetBytes(buf);

        pack(group);
        pack(uploader);
        pack(time);
        pack(filename);
    }
}
