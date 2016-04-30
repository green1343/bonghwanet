package com.example.android.basicaccessibility.Emergency;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.android.basicaccessibility.R;

public class Emergencytype_firstaid_fracture extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.emergencytype_firstaid_fracture);
		VideoView video = (VideoView)findViewById(R.id.videoview);

		Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.em);
		android.widget.MediaController mediaController= new android.widget.MediaController(this);
		mediaController.setAnchorView(video);


		video.setMediaController(mediaController);

		video.setVideoURI(uri);
		video.requestFocus();
		video.start();
	}
}