package com.example.android.bonghwa.needclass;

/**
 * Created by sheep on 2016-04-14.
 */
public class MyThread extends Thread {

    public boolean running = true;
    public int data = 0;

    public synchronized int getData(){return data;}
    public synchronized void setData(int data){
        this.data = data;
    }
}
