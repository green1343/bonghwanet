package com.example.android.basicaccessibility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;

import com.example.android.packet.Packet_Share_Text;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class GallaryActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		setTitle("갤러리 사진");

		Gallery gallery = (Gallery) findViewById(R.id.gallery);
		MyGalleryAdapter galAdapter = new MyGalleryAdapter(this);
		gallery.setAdapter(galAdapter);

		findViewById(R.id.buttonCreate).setOnClickListener(onClickButton);
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
		public void onClick(View v) {

			EditText text=(EditText)findViewById(R.id.editMessage);
			switch (v.getId()) {
				case R.id.buttonCreate:
					uploadCameraFile();
				default:
					break;
			}
		}
	};

	public class MyGalleryAdapter extends BaseAdapter {
		Context context;
		Integer[] posterID = {
				R.drawable.mov11, R.drawable.mov12, R.drawable.mov13, R.drawable.mov14, R.drawable.mov15, R.drawable.mov16
		};

		public MyGalleryAdapter(Context c) {
			context = c;
		}

		public int getCount() {
			return  posterID.length;
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(context);
			imageView.setLayoutParams(new Gallery.LayoutParams(100, 150));
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setPadding(5, 5, 5, 5);

			final int pos = position;
			imageView.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					ImageView ivPoster = (ImageView) findViewById(R.id.ivPoster);
					ivPoster.setScaleType(ImageView.ScaleType.FIT_CENTER);
					ivPoster.setImageResource(posterID[pos]);
					return false;
				}
			});

			imageView.setImageResource(posterID[position]);

			return imageView;
		}
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

	void uploadFil(){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		startActivityForResult(intent, REQ_FILE_SELECT);
	}

	void uploadCameraFile(){
		/*HashMap<Long, Manager.GroupInfo> groups = Manager.INSTANCE.getAllGroups();
		Manager.GroupInfo g = groups.get(groups.keySet());
		String str = new String(g.name);*/
		cameraTempFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bonghwanet/"+"/tmp_image.jpg";
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