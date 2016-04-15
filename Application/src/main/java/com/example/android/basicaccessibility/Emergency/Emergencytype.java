package com.example.android.basicaccessibility.Emergency;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.android.basicaccessibility.R;

public class Emergencytype extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergencytype);

		findViewById(R.id.button2).setOnClickListener(onClickButton);
		findViewById(R.id.button7).setOnClickListener(onClickButton);
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
		public void onClick(View v) {

			EditText text=(EditText)findViewById(R.id.editMessage);
			switch (v.getId()) {
				case R.id.button2:
					Intent intent = new Intent(getApplicationContext(), Emergencytype_earthquake.class);
					startActivity(intent);
					break;
				case R.id.button7:
					Intent intent1 = new Intent(getApplicationContext(), Emergencytype_firstaid.class);
					startActivity(intent1);
					break;
				default:
					break;
			}
		}
	};
}