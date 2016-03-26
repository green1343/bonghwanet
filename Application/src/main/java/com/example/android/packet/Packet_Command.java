package com.example.android.packet;

public class Packet_Command {

    private short m_command;
    public int place;

    public Packet_Command()
    {
        m_command = 0;
        place = 0;
    }

    public Packet_Command(short command)
    {
        m_command = command;
        place = 0;
    }

    public Packet_Command(byte[] buf)
    {
        m_command = unpackShort(buf);
        place = 0;
    }

    public short getCommand()
    {
        return m_command;
    }

    public void setCommand(short command)
    {
        m_command = command;
    }

    public void GetBytes(byte[] buf)
    {
        pack(m_command, buf);
    }

    protected void pack(boolean value, byte[] buf){
        buf[place] = (byte)(value ? 1 : 0);
        ++place;
    }

    protected void pack(short value, byte[] buf){
        buf[place + 1] = (byte)(value & 0xff);
        buf[place + 0] = (byte)((value>>8) & 0xff);
        place += 2;
    }

    protected void pack(int value, byte[] buf){
        buf[place + 3] = (byte)(value & 0xff);
        buf[place + 2] = (byte)((value>>8) & 0xff);
        buf[place + 1] = (byte)((value>>16) & 0xff);
        buf[place + 0] = (byte)((value>>24) & 0xff);
        place += 4;
    }

    protected void pack(long value, byte[] buf){
        buf[place + 7] = (byte)(value & 0xff);
        buf[place + 6] = (byte)((value>>8) & 0xff);
        buf[place + 5] = (byte)((value>>16) & 0xff);
        buf[place + 4] = (byte)((value>>24) & 0xff);
        buf[place + 3] = (byte)((value>>32) & 0xff);
        buf[place + 2] = (byte)((value>>40) & 0xff);
        buf[place + 1] = (byte)((value>>48) & 0xff);
        buf[place + 0] = (byte)((value>>56) & 0xff);
        place += 8;
    }

    protected void pack(float value, byte[] buf){
        pack(Float.floatToIntBits(value), buf);
    }

    protected void pack(double value, byte[] buf){
        pack(Double.doubleToLongBits(value), buf);
    }

    protected void pack(String value, byte[] buf){
        pack(value.length(), buf);
        byte [] str = value.getBytes();
        for(int i=0; i<str.length; ++i)
            buf[place+i] = str[i];
        place += str.length;
    }

    protected boolean unpackBool(byte[] buf){
        return buf[place] != 0;
    }

    protected short unpackShort(byte[] buf){
        return (short)(((buf[place]&0xff) << 8) | (buf[place+1]&0xff));
    }

    protected int unpackInt(byte[] buf){
        return ((buf[place]&0xff) << 24) | ((buf[place+1]&0xff) << 16) |
                ((buf[place+2]&0xff) << 8) | (buf[place+3]&0xff);
    }

    protected long unpackLong(byte[] buf){
        long num1 = unpackInt(buf);
        long num2 = unpackInt(buf);
        return (num1 << 32) | (num2 & 0xffffffffL);
    }

    protected float unpackFloat(byte[] buf){
        int i = unpackInt(buf);
        return Float.intBitsToFloat(i);
    }

    protected double unpackDouble(byte[] buf){
        long i = unpackLong(buf);
        return Double.longBitsToDouble(i);
    }

    protected String unpackString(byte[] buf){
        int size = unpackInt(buf);
        String result = new String();
        for(int i=0; i<size; ++i)
            result += (char)buf[place+i];
        place += size;
        return result;
    }
}