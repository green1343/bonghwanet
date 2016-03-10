package com.example.android.packet;

import com.example.android.needclass.BigEndianByteHandler;

/**
 * Created by Kim on 2015-03-31.
 */

public class Packet_Command {

    private short command;

    public Packet_Command()
    {
        command = 0;
    }

    public Packet_Command(short command)
    {
        this.command = command;
    }

    public Packet_Command(byte[] data)
    {
        //command = BitConverter.ToInt16(data, 0); // 바이트를 쇼트로
        command = BigEndianByteHandler.byteToShort(data);
    }

    public short getCommand()
    {
        return command;
    }

    protected void setCommand(short command)
    {
        this.command = command;
    }

    public int GetBytes(byte[] data)
    {
        int place = 0;
        System.arraycopy(BigEndianByteHandler.shortToByte(command), 0, data, place, 2);
        place += 2;

        return place;
    }
}
