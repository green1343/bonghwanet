package com.example.android.basicaccessibility;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.bonghwa.Device;
import com.example.android.bonghwa.GroupInfo.ChatMsg;
import com.example.android.bonghwa.Manager;
import com.example.android.bonghwa.Network;
import com.example.android.bonghwa.packet.PacketShareText;

import java.util.ArrayList;

public class ChattingHistory extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatting_list);

		final DBHelper dbHelper = new DBHelper(getApplicationContext(), "chat.db", null, 1);
		final TextView result = (TextView) findViewById(R.id.result);

		result.setText(dbHelper.getResult());

	}

}