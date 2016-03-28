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
        tabHost.addTab(spec);
        // Do the same for the other tabs
        intent = new Intent().setClass(this, NewGrouplistActivity.class);
        spec = tabHost.newTabSpec("new").setIndicator("New",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        tabHost.addTab(spec);
        intent = new Intent().setClass(this, SettingsActivity.class);
        spec = tabHost.newTabSpec("settings").setIndicator("Settings",
                res.getDrawable(R.drawable.ic_tab)).setContent(intent);
        tabHost.addTab(spec);
        tabHost.setCurrentTab(0);
    }

    public static final int REQ_FILE_SELECT = 0;
    public static final int REQ_CAMERA_SELECT = 0;

    String cameraTempFilePath;

    void uploadPicture(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_FILE_SELECT);
    }

    void uploadFile(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQ_FILE_SELECT);
    }

    void uploadCameraFile(){
        cameraTempFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp_image.jpg";
        File imageFile = new File(cameraTempFilePath);
        Uri imageFileUri = Uri.fromFile(imageFile);

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
        startActivityForResult(intent, REQ_CAMERA_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(data == null)
            return;

        Manager.INSTANCE.uploadFile(getPath(data.getData()));

        /*if(resultCode == RESULT_OK) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            if (requestCode == REQ_CAMERA_SELECT) {
            }
            else if (requestCode == REQ_FILE_SELECT) {
            }
        }*/
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }
}
