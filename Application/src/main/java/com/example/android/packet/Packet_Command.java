package com.example.android.packet;

public class Packet_Command {

    private short m_command;
    private byte[] buf;
    public int place = 0;

    public Packet_Command()
    {
        m_command = 0;
    }

    public Packet_Command(short command)
    {
        m_command = command;
    }

    public Packet_Command(byte[] buf)
    {
        this.buf = buf;
        m_command = unpackShort();
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
        this.buf = buf;
        pack(m_command);
    }

    protected void pack(boolean value){
        buf[place] = (byte)(value ? 1 : 0);
        ++place;
    }

    protected void pack(short value){
        buf[place + 1] = (byte)(value & 0xff);
        buf[place + 0] = (byte)((value>>8) & 0xff);
        place += 2;
    }

    protected void pack(int value){
        buf[place + 3] = (byte)(value & 0xff);
        buf[place + 2] = (byte)((value>>8) & 0xff);
        buf[place + 1] = (byte)((value>>16) & 0xff);
        buf[place + 0] = (byte)((value>>24) & 0xff);
        place += 4;
    }

    protected void pack(long value){
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

    protected void pack(float value){
        pack(Float.floatToIntBits(value));
    }

    protected void pack(double value){
        pack(Double.doubleToLongBits(value));
    }

    protected void pack(String value){
        pack(value.length());
        byte [] str = value.getBytes();
        for(int i=0; i<str.length; ++i)
            buf[place+i] = str[i];
        place += str.length;
    }

    protected boolean unpackBool(){
        boolean result = buf[place] != 0;
        ++place;
        return result;
    }

    protected short unpackShort(){
        short result = (short)(((buf[place]&0xff) << 8) | (buf[place+1]&0xff));
        place += 2;
        return result;
    }

    protected int unpackInt(){
        int result = ((buf[place]&0xff) << 24) | ((buf[place+1]&0xff) << 16) |
                ((buf[place+2]&0xff) << 8) | (buf[place+3]&0xff);
        place += 4;
        return result;
    }

    protected long unpackLong(){
        long num1 = unpackInt();
        long num2 = unpackInt();
        return (num1 << 32) | (num2 & 0xffffffffL);
    }

    protected float unpackFloat(){
        int i = unpackInt();
        return Float.intBitsToFloat(i);
    }

    protected double unpackDouble(){
        long i = unpackLong();
        return Double.longBitsToDouble(i);
    }

    protected String unpackString(){
        int size = unpackInt();
        String result = new String();
        for(int i=0; i<size; ++i)
            result += (char)buf[place+i];
        place += size;
        return result;
    }
}