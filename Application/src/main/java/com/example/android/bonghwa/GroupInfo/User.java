package com.example.android.bonghwa.GroupInfo;

/**
 * Created by sheep on 2016-11-26.
 */

public class User
{
    public final static String DEFAULT_USERNAME = "User";

    public String name;

    public User()
    {
        name = DEFAULT_USERNAME;
    }
    public User(String name)
    {
        this.name = name;
    }
}
