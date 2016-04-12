package com.example.android.basicaccessibility;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.TextView;

import java.io.File;

public class FileActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {        
		super.onCreate(savedInstanceState);
		TextView textview = new TextView(this);        
		textview.setText("This is the FileActivity tab");
		setContentView(textview);    
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