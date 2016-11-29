package com.example.android.bonghwa.GroupInfo;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by sheep on 2016-11-26.
 */

public class Group {
    public long id;
    public String name = new String();
    public HashMap<Long, User> members = new HashMap<>();
    public HashMap<String, GroupFile> files = new HashMap<>(); // id, files
    public HashMap<String, GroupFile> deletedFiles = new HashMap<>();
    public LinkedList<ChatMsg> texts = new LinkedList<>();

    public Group(){}
    public Group(Group g){
        id = g.id;
        name = new String(g.name);
        members = (HashMap<Long, User>)g.members.clone();
        files = (HashMap<String, GroupFile>)g.files.clone();
        deletedFiles = (HashMap<String, GroupFile>)g.deletedFiles.clone();
        texts = (LinkedList<ChatMsg>)g.texts.clone();
    }

    public void merge(Group g){
        if(g == null)
            return;

        members.putAll(g.members);
        files.putAll(g.files);
        deletedFiles.putAll(g.deletedFiles);
        texts.addAll(g.texts);
    }
}
