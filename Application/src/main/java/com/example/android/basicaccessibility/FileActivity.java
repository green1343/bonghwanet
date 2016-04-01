package com.example.android.basicaccessibility;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class FileActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {        
		super.onCreate(savedInstanceState);        
		TextView textview = new TextView(this);        
		textview.setText("This is the FileActivity tab");
		setContentView(textview);    
	}
}