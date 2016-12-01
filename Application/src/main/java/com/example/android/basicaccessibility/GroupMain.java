package com.example.android.basicaccessibility;


import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TabHost;
import android.widget.Toast;

import com.example.android.bonghwa.Manager;

public class GroupMain extends TabActivity {

    static public TabHost mobileTabHost;

    static public void setTab(int index){
        mobileTabHost.setCurrentTab(index);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groupmain);

        Manager.INSTANCE.init(this);

        Resources res = getResources();
        // Resource object to get Drawables
        mobileTabHost = getTabHost();
        // The activity TabHost
        TabHost.TabSpec spec;
        // Resusable TabSpec for each tab
        Intent intent;
        // Reusable Intent for each tab
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, GroupHomeActivity.class);
        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = mobileTabHost.newTabSpec("main").setIndicator("Main",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        mobileTabHost.addTab(spec);
        // Do the same for the other tabs
        intent = new Intent().setClass(this, ChattingActivity.class);
        spec = mobileTabHost.newTabSpec("chat").setIndicator("Chat",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        mobileTabHost.addTab(spec);
        intent = new Intent().setClass(this, GallaryActivity.class);
        spec = mobileTabHost.newTabSpec("gallery").setIndicator("Gallery",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        mobileTabHost.addTab(spec);
        intent = new Intent().setClass(this, FileActivity.class);
        spec = mobileTabHost.newTabSpec("file").setIndicator("File",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        mobileTabHost.addTab(spec);
        mobileTabHost.setCurrentTab(0);

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override

    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                Toast.makeText(this, "설정버튼", Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
