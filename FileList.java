package com.example.android.basicaccessibility;

import java.io.*;
import java.util.*;
import android.app.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;


public class FileList extends Activity
    implements AdapterView.OnItemClickListener {
        String mRoot = "";
        String mPath = "";
        TextView mTextMsg;
        ListView mListFile;
        ArrayList<String> mArFile;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.file);
            if( isSdCard() == false )
                finish();
            mTextMsg = (TextView)findViewById(R.id.textMessage);
            mRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
            String[] fileList = getFileList(mRoot);
            for(int i=0; i < fileList.length; i++)
                Log.d("tag", fileList[i]);
            initListView();
            fileList2Array(fileList);
        }

    public void initListView() {
        mArFile = new ArrayList<String>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mArFile);

        mListFile = (ListView)findViewById(R.id.listFile);
        mListFile.setAdapter(adapter);
        mListFile.setOnItemClickListener(this);
    }


    public void onItemClick(AdapterView parent, View view, int position, long id) {
        String strItem = mArFile.get(position);
        String strPath = getAbsolutePath(strItem);
        String[] fileList = getFileList(strPath);

        fileList2Array(fileList);
    }


    public String getAbsolutePath(String strFolder) {
        String strPath;
        if( strFolder == ".." ) {

            int pos = mPath.lastIndexOf("/");
            strPath = mPath.substring(0, pos);
        }
        else
            strPath = mPath + "/" + strFolder;
        return strPath;
    }


    public boolean isSdCard() {
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED) == false) {
            Toast.makeText(this, "SD Card does not exist", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public String[] getFileList(String strPath) {
        File fileRoot = new File(strPath);
        if( fileRoot.isDirectory() == false )
            return null;
        mPath = strPath;
        mTextMsg.setText(mPath);
        String[] fileList = fileRoot.list();
        return fileList;
    }


    public void fileList2Array(String[] fileList) {
        if( fileList == null )
            return;

        mArFile.clear();

        if( mRoot.length() < mPath.length() )
            mArFile.add("..");

        for(int i=0; i < fileList.length; i++) {
            Log.d("tag", fileList[i]);
            mArFile.add(fileList[i]);
        }
        ArrayAdapter adapter = (ArrayAdapter)mListFile.getAdapter();
        adapter.notifyDataSetChanged();
    }

}