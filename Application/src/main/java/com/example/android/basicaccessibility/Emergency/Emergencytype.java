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
		findViewById(R.id.button3).setOnClickListener(onClickButton);
		findViewById(R.id.button4).setOnClickListener(onClickButton);
		findViewById(R.id.button5).setOnClickListener(onClickButton);
		findViewById(R.id.button6).setOnClickListener(onClickButton);
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
				case R.id.button3:
					Intent intent1 = new Intent(getApplicationContext(), Emergencytype_surge.class);
					startActivity(intent1);
					break;
				case R.id.button4:
					Intent intent2 = new Intent(getApplicationContext(), Emergencytype_typhoon.class);
					startActivity(intent2);
					break;
				case R.id.button5:
					Intent intent3 = new Intent(getApplicationContext(), Emergencytype_volcano.class);
					startActivity(intent3);
					break;
				case R.id.button6:
					Intent intent4 = new Intent(getApplicationContext(), Emergencytype_war.class);
					startActivity(intent4);
					break;
				case R.id.button7:
					Intent intent5 = new Intent(getApplicationContext(), Emergencytype_firstaid.class);
					startActivity(intent5);
					break;
				default:
					break;
			}
		}
	};
}