package com.example.android.bonghwa.GroupInfo;

/**
 * Created by sheep on 2016-11-26.
 */

public class GroupFile {
    public String path;
    public boolean isDirectory;
    public long time;

    public GroupFile(){
    }

    public GroupFile(String path, boolean isDirectory, long time){
        this.path = path;
        this.isDirectory = isDirectory;
        this.time = time;
    }
}
