package com.example.android.basicaccessibility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.android.bonghwa.Manager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GallaryActivity extends Activity {

	GridView mobileGridView = null;
	static MyGridAdapter mobileAdapter = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		setTitle("갤러리 사진");

		mobileGridView = (GridView) findViewById(R.id.gridView1);
		mobileAdapter = new MyGridAdapter(this);
		mobileGridView.setAdapter(mobileAdapter);

		findViewById(R.id.buttonCreate1).setOnClickListener(onClickButton);
		findViewById(R.id.buttonCreate2).setOnClickListener(onClickButton);

		mobileAdapter.notifyDataSetChanged();
		mobileGridView.setAdapter(mobileAdapter);
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
		public void onClick(View v) {

			EditText text=(EditText)findViewById(R.id.editMessage);
			switch (v.getId()) {
				case R.id.buttonCreate1:
					uploadPicture();
					break;
				case R.id.buttonCreate2:
					uploadCameraFile();
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onResume(){
		super.onResume();

		refreshList();
	}

	public static void refreshList(){
		if(mobileAdapter == null)
			return;

		mobileAdapter.refreshList();
	}

	public class MyGridAdapter extends BaseAdapter {
		Context context;

		String sysDir = null;
		File[] sysFiles = null;

		public void refreshList(){
			sysDir = Manager.INSTANCE.getRealGroupPath(Manager.INSTANCE.getCurGroupID()) + "/Pictures";
			sysFiles = (new File(sysDir).listFiles());

			notifyDataSetChanged();
		}

		public MyGridAdapter(Context c) {
			context = c;
			refreshList();
		}

		public int getCount() {
			if(sysFiles == null)
				return 0;

			return sysFiles.length;
			//return posterID.length;
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ImageView imageView = new ImageView(context);
			imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setPadding(5, 5, 5, 5);

			//imageView.setImageResource(posterID[position]);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			Bitmap src = BitmapFactory.decodeFile(sysFiles[position].toString(), options);
			imageView.setImageBitmap(src);

			//imageView.setImageURI(Uri.parse(sysFiles[position].toString()));

			final int pos = position;
			imageView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					View dialogView = (View) View.inflate(GallaryActivity.this, R.layout.dialog, null);
					AlertDialog.Builder dlg = new AlertDialog.Builder(GallaryActivity.this);

					ImageView ivPoster = (ImageView) dialogView.findViewById(R.id.ivPoster);

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 2;
					Bitmap src = BitmapFactory.decodeFile(sysFiles[pos].toString(), options);
					ivPoster.setImageBitmap(src);

					//ivPoster.setImageURI(Uri.parse(sysFiles[pos].toString()));
					//ivPoster.setImageResource(posterID[pos]);
					dlg.setIcon(R.drawable.ic_launcher);
					dlg.setView(dialogView);
					//dlg.setNegativeButton("닫기", null);
					dlg.show();
				}
			});

			return imageView;
		}
	}

	public static final int REQ_FILE_SELECT = 0;
	public static final int REQ_CAMERA_SELECT = 1;

	String cameraTempFilePath;

	void uploadPicture(){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, REQ_FILE_SELECT);
	}

	public String getDateString()
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
		String str_date = "bonghwa_"+df.format(new Date());

		return str_date;
	}

	void uploadCameraFile(){
		/*HashMap<Long, Group> groups = Manager.INSTANCE.getAllGroups();
		Group g = groups.get(groups.keySet());
		String str = new String(g.name);*/
		cameraTempFilePath = Manager.INSTANCE.getRealGroupPath(Manager.INSTANCE.getCurGroupID())+"/Pictures/"+getDateString()+".jpg";
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

		if(requestCode == REQ_FILE_SELECT)
			Manager.INSTANCE.uploadPicture(getPath(data.getData()));
		else if(requestCode == REQ_CAMERA_SELECT)
			Manager.INSTANCE.uploadCamera(cameraTempFilePath);

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
		Cursor cursor = getContentResolver().query(uri, null, null, null, null );
		cursor.moveToNext();
		String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
		return path;
	}
}