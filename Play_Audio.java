package com.example.android.packet;



//package jay.media;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;
import jay.dencode.Decoder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class Play_Audio extends Thread
{
    private final static String TAG = "LanAudioPlay";

    protected AudioTrack m_out_trk;
    private volatile Thread runner;
    private Decoder decoder;
    protected DatagramSocket udp_socket;
    protected RtpPacket rtp_packet;
    protected RtpSocket rtp_socket;
    protected int SampleRate = 16000;
    // protected int listenport;
    protected final int mFrameSize = 320;
    protected final int Rtphead = 12;
    protected final int GO_TIMEOUT = 1000;
    protected int codectype = 1;
    protected byte[] m_out_bytes;
    protected int m, vm = 1;

    // speech preprocessor
    /** 현재의 순서 번호를 가져온다. */
    protected int gseq = 0;

    /** 마지막으로 유효한 일련 번호 */
    protected int currentseq = 0;

    /**
     * 현재 하위 8의 일련번호를 가져온다. getseq = gseq & 0xff;
     */
    protected int getseq;
    protected int expseq;

    /** 획득한 일련 번호 사이에 두배의 차이(무효) */
    protected int gap;

    /** 에코 취소 버퍼 패킷 번호 ( sipUA 기본 20 ) */
    protected int ec_buffer_pkgs = 0;

    public static float good, late, lost, loss, loss2;

    // used for echo calc

    public Play_Audio(DatagramSocket socket, int codectype, int SampleRate, int ec_buffer_pkgs)
    {
        Log.i(TAG, "new LanAudioPlay() socket port=" + socket.getPort());

        try {
            this.SampleRate = SampleRate;
            this.codectype = codectype;
            this.ec_buffer_pkgs = ec_buffer_pkgs;
            int m_out_buf_size = AudioTrack.getMinBufferSize(SampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            m_out_trk = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SampleRate,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, m_out_buf_size * 2/* 10 */,
                    AudioTrack.MODE_STREAM);
            udp_socket = socket;
            Log.i(TAG, "Audio track m_out_buf_size is " + m_out_buf_size);
            // m_out_trk.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startThread()
    {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
        }
    }

    public void stopThread()
    {
        if (runner != null) {
            Thread moribund = runner;
            runner = null;
            moribund.interrupt();
            this.free();
        }
    }

    public void run()
    {
        Log.d(TAG, "LanAudioPlay running..");

        // byte[] buffer = new byte[mFrameSize + Rtphead];
        byte[] buffer = new byte[1024 + Rtphead];

        rtp_packet = new RtpPacket(buffer, 0);
        rtp_packet.setPayloadType(codectype);

        try {
            rtp_socket = new RtpSocket(this.udp_socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        decoder = new Decoder(codectype, ec_buffer_pkgs);
        decoder.startThread();

        Log.d(TAG, "#### 1");
        try {
            rtp_socket.receive(rtp_packet);// // RTP는 데이터 스트림을 수신
            Log.i(TAG, "@@@@@@@@ rtp_socket receive port: " + rtp_socket.getDatagramSocket().getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "#### 2");
        m_out_trk.play();
        Log.d(TAG, "#### 3");
        System.gc();
        Log.d(TAG, "#### 4");
        empty();

        while (Thread.currentThread() == runner) {
            long ms = System.currentTimeMillis();

            try {
                rtp_socket.receive(rtp_packet);// RTP는 데이터 스트림을 수신
            } catch (IOException e) {
                e.printStackTrace();
            }

            gseq = rtp_packet.getSequenceNumber();//  RTP 패킷의 Sequence Number[]
            // rtp_packet.setSequenceNumber(seqn++)
            if (currentseq == gseq) {// 마지막으로 유효한 일련 번호
                m++;
                continue;
            }
            lostandgood();
            // Log.d("LanAudioPlay", "lost:" + lost+" good:"+good);// 패킷 손실률

            if (decoder.isIdle()) {
                decoder.putData(System.currentTimeMillis(), buffer, Rtphead, rtp_packet.getPayloadLength());// 디코딩 버퍼 쓰기
                // Log.i(TAG, "Write " + rtp_packet.getPayloadLength() +
                // " size data to decoder");
            }

            // if (decoder.isGetData() == true) {
            while (decoder.isGetData() == true) {/* decoder가 데이터를 갖는경우, 제거 */
                short[] s_bytes_pkg = decoder.getData().clone();
                m_out_trk.write(s_bytes_pkg, 0, s_bytes_pkg.length);// 기록버퍼 재생
            }
            Log.e(TAG, " 재생시간 데이터 작성   " + (System.currentTimeMillis() - ms));
        }
        Log.d("LanAudioPlay", "lost:" + lost + " good:" + good);// 패킷 손실률
    }

    void empty()
    {
        Log.d(TAG, "empty()");
        try {
            rtp_socket.getDatagramSocket().setSoTimeout(1);
            for (;;)
                rtp_socket.receive(rtp_packet);
        } catch (IOException e) {
        }
        try {
            rtp_socket.getDatagramSocket().setSoTimeout(GO_TIMEOUT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        currentseq = 0;
    }

    void lostandgood()
    {
        if (currentseq != 0) {
            getseq = gseq & 0xff;
            expseq = ++currentseq & 0xff;
            if (m == LanAudioRecord.m)
                vm = m;
            gap = (getseq - expseq) & 0xff;
            if (gap > 0) {
                if (gap > 100)
                    gap = 1;
                loss += gap;
                lost += gap;
                good += gap - 1;
                loss2++;
            } else {
                if (m < vm) {
                    loss++;
                    loss2++;
                }
            }
            good++;
            if (good > 110) {
                good *= 0.99;
                lost *= 0.99;
                loss *= 0.99;
                loss2 *= 0.99;
                late *= 0.99;
            }
        }
        m = 1;
        currentseq = gseq;
    }

    public void free()
    {
        m_out_trk.stop();
        decoder.stopThread();
    }

}
