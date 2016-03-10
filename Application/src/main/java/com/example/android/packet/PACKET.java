package com.example.android.packet;

/**
 * Created by Kim on 2015-03-31.
 */
public class PACKET {

    // PC <-> Mobile
    public final static int PACKET_LOGIN_REQUEST = 0;
    public final static int PACKET_LOGIN_REQUEST_RETURN = 1;

    // Mobile <-> Mobile
    public final static int PACKET_GROUPLIST = 2;
    public final static int PACKET_SYNC = 3;
    public final static int PACKET_SHARE_TEXT = 4;
    public final static int PACKET_SHARE_FILE_REQUEST = 5;
    public final static int PACKET_SHARE_FILE_REQUEST_OK = 6;

}
