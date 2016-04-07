package com.example.android.basicaccessibility;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.TabHost;

import java.io.File;
/* 첫번째 메인 화면 생성*/
public class Main extends TabActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Manager.INSTANCE.init(this);

        Resources res = getResources();
        // Resource object to get Drawables
        TabHost tabHost = getTabHost();
        // The activity TabHost
        TabHost.TabSpec spec;
        // Resusable TabSpec for each tab
        Intent intent;
        // Reusable Intent for each tab
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, GrouplistActivity.class);
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("grouplist").setIndicator("Grouplist",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        tabHost.addTab(spec);/*그룹 리스트 탭 생성*/
        // Do the same for the other tabs
        intent = new Intent().setClass(this, NewGrouplistActivity.class);
        spec = tabHost.newTabSpec("new").setIndicator("New",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        tabHost.addTab(spec);//New 탭 생성
        intent = new Intent().setClass(this, SettingsActivity.class);
        spec = tabHost.newTabSpec("settings").setIndicator("Settings",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        tabHost.addTab(spec); //Setting 탭 생성
        tabHost.setCurrentTab(0);
    }

}
