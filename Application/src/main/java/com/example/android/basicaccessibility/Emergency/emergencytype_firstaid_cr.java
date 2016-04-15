package com.example.android.basicaccessibility.Emergency;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.android.basicaccessibility.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class emergencytype_firstaid_cr extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergencytype_firstaid_cr);
		setTitle("심폐소생술");

		Gallery gallery = (Gallery) findViewById(R.id.gallery1);
		MyGalleryAdapter galAdapter = new MyGalleryAdapter(this);
		gallery.setAdapter(galAdapter);
	}

	protected void onResume() {
		super.onResume();

	}

	public class MyGalleryAdapter extends BaseAdapter {
		Context context;
		Integer[] crID ={
				R.drawable.cr_1, R.drawable.cr_2, R.drawable.cr_3, R.drawable.cr_4, R.drawable.cr_5, R.drawable.cr_6, R.drawable.cr_7, R.drawable.cr_8, R.drawable.cr_9,
		};

		public MyGalleryAdapter(Context c) {
			context = c;
		}

		public int getCount() {
			return  crID.length;
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return  0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageview = new ImageView(context);
			imageview.setLayoutParams(new Gallery.LayoutParams(100, 150));
			imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageview.setPadding(5, 5, 5, 5);

			imageview.setImageResource(crID[position]);

			final int pos = position;
			imageview.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					ImageView ivPoster = (ImageView) findViewById(R.id.ivPoster);
					ivPoster.setScaleType(ImageView.ScaleType.FIT_CENTER);
					ivPoster.setImageResource(crID[pos]);
					return false;
				}
			});

			return imageview;
		}
	}
}