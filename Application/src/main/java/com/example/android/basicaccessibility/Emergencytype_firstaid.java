package com.example.android.basicaccessibility;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Emergencytype_firstaid extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergencytype_firstaid);

		findViewById(R.id.button3).setOnClickListener(onClickButton);
		findViewById(R.id.button4).setOnClickListener(onClickButton);
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
		public void onClick(View v) {

			EditText text=(EditText)findViewById(R.id.editMessage);
			switch (v.getId()) {
				case R.id.button3:
					Intent intent = new Intent(getApplicationContext(), Emergencytype_firstaid_fracture.class);
					startActivity(intent);
					break;
				case R.id.button4:
					Intent intent2 = new Intent(getApplicationContext(), Emergencytype_firstaid_abrasion.class);
					startActivity(intent2);
					break;
				default:
					break;
			}
		}
	};
}