package com.example.android.basicaccessibility;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {        
		super.onCreate(savedInstanceState);        
		TextView textview = new TextView(this);        
		textview.setText("This is the SettingsActivity tab");
		setContentView(textview);    
	}
}