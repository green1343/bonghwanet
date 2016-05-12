package com.example.android.basicaccessibility;

import java.io.*;
import java.util.*;
import android.app.*;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.*;
import android.view.*;
import android.widget.*;


public class FileActivity extends Activity implements AdapterView.OnItemClickListener {

	static String mRoot = null;
	static String mPath = null;
	static TextView mTextMsg = null;
	static ListView mListFile = null;
	static ArrayList<String> mArFile = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file);
		if( isSdCard() == false )
			finish();
		mTextMsg = (TextView)findViewById(R.id.textMessage);
		mRoot = Manager.INSTANCE.getRealGroupPath(Manager.INSTANCE.getCurGroupID());
		String[] fileList = getFileList(mRoot);
		for(int i=0; i < fileList.length; i++)
			Log.d("tag", fileList[i]);
		initListView();
		fileList2Array(fileList);

		findViewById(R.id.buttonUpload).setOnClickListener(onClickButton);
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
		public void onClick(View v) {

			EditText text=(EditText)findViewById(R.id.editMessage);
			switch (v.getId()) {
				case R.id.buttonUpload:
					uploadFile();
					break;
				default:
					break;
			}
		}
	};

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


	static public String[] getFileList(String strPath) {
		File fileRoot = new File(strPath);
		if( fileRoot.isDirectory() == false )
			return null;
		mPath = strPath;
		mTextMsg.setText(mPath);
		String[] fileList = fileRoot.list();
		return fileList;
	}


	static public void fileList2Array(String[] fileList) {
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

	static public void refreshList(){
		if(mPath == null)
			return;

		String[] fileList = getFileList(mPath);
		fileList2Array(fileList);
	}

	public static final int REQ_FILE_SELECT = 0;

	void uploadFile(){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		startActivityForResult(intent, REQ_FILE_SELECT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(data == null)
			return;

		String path = getPath(data.getData());
		if(path == null)
			return;

		Manager.INSTANCE.uploadFile(path);

		refreshList();

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