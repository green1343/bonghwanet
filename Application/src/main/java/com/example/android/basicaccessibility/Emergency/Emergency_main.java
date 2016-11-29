package com.example.android.basicaccessibility.Emergency;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import com.example.android.basicaccessibility.ChattingActivity;
import com.example.android.bonghwa.Manager;
import com.example.android.basicaccessibility.R;

/* 첫번째 메인 화면 생성*/
public class Emergency_main extends TabActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_main);

        Resources res = getResources();
        // Resource object to get Drawables
        TabHost tabHost = getTabHost();
        // The activity TabHost
        TabHost.TabSpec spec;
        // Resusable TabSpec for each tab
        Intent intent;
        // Reusable Intent for each tab
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, Emergencytype.class);
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("type").setIndicator("Type",
                res.getDrawable(R.drawable.emergancy)).setContent(intent);
        tabHost.addTab(spec);/*그룹 리스트 탭 생성*/
        // Do the same for the other tabs
        intent = new Intent().setClass(this, ChattingActivity.class);
        spec = tabHost.newTabSpec("chat").setIndicator("Chat",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);

        Manager.INSTANCE.setupGPS();
    }

}
