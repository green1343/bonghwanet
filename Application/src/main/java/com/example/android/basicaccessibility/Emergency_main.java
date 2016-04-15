package com.example.android.basicaccessibility;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

/* 첫번째 메인 화면 생성*/
public class Emergency_main extends TabActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_main);

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
        intent = new Intent().setClass(this, Emergencytype.class);
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("emergencytype").setIndicator("Emergency type",
                res.getDrawable(R.drawable.emergancy)).setContent(intent);
        tabHost.addTab(spec);/*그룹 리스트 탭 생성*/
        // Do the same for the other tabs
        intent = new Intent().setClass(this, ChattingActivity.class);
        spec = tabHost.newTabSpec("emergency chat").setIndicator("Emergency Chat",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        tabHost.addTab(spec);
        intent = new Intent().setClass(this, Emergency_Battery.class);
        spec = tabHost.newTabSpec("battery").setIndicator("Battery",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        tabHost.addTab(spec); //Setting 탭 생성
        tabHost.setCurrentTab(0);
    }

}
