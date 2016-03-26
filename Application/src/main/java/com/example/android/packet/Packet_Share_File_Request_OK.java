package com.example.android.packet;

/**
 * Created by Kim on 2015-04-03.
 */
public class Packet_Share_File_Request_OK extends Packet_Command
{
    public long group;
    public long uploader;
    public long time;
    public String filename;

    public Packet_Share_File_Request_OK(){
        setCommand((short) PACKET.PACKET_SHARE_FILE_REQUEST_OK);
    }

    public Packet_Share_File_Request_OK(byte[] buf){

        super(buf);

        group = unpackLong(buf);
        uploader = unpackLong(buf);
        time = unpackLong(buf);
        filename = unpackString(buf);
    }

    public void GetBytes(byte[] buf){

        super.GetBytes(buf);

        pack(group, buf);
        pack(uploader, buf);
        pack(time, buf);
        pack(filename, buf);
    }
}
