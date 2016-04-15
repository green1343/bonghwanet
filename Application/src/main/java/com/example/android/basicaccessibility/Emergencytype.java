package com.example.android.basicaccessibility;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

public class Emergencytype extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergencytype);

		findViewById(R.id.button7).setOnClickListener(onClickButton);
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
		public void onClick(View v) {

			EditText text=(EditText)findViewById(R.id.editMessage);
			switch (v.getId()) {
				case R.id.button7:
					Intent intent = new Intent(getApplicationContext(), Emergencytype_firstaid.class);
					startActivity(intent);
					break;
				default:
					break;
			}
		}
	};
}