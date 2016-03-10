package com.example.android.packet;

import com.example.android.needclass.BigEndianByteHandler;

/**
 * Created by Kim on 2015-04-07.
 */

abstract public class Packet_Share extends Packet_Command {

    static protected int SIZE = 26;

    public long group;
    public long uploader;
    public long time;

    protected Packet_Share()
    {
    }

    public Packet_Share(byte[] data)
    {
        super(data);

        int place = 2;
        group = BigEndianByteHandler.byteToLong(data, place);
        place += 8;
        uploader = BigEndianByteHandler.byteToLong(data, place);
        place += 8;
        time = BigEndianByteHandler.byteToLong(data, place);
        place += 8;
    }

    public int GetBytes(byte[] data)
    {
        int place = super.GetBytes(data);

        System.arraycopy(BigEndianByteHandler.longToByte(group), 0, data, place, 8);
        place += 8;
        System.arraycopy(BigEndianByteHandler.longToByte(uploader), 0, data, place, 8);
        place += 8;
        System.arraycopy(BigEndianByteHandler.longToByte(time), 0, data, place, 8);
        place += 8;

        return place;
    }
}
