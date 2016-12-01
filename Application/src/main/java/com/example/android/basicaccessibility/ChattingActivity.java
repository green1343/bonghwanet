package com.example.android.basicaccessibility;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.android.basicaccessibility.Emergency.Emergencytype_earthquake;
import com.example.android.bonghwa.Device;
import com.example.android.bonghwa.GroupInfo.ChatMsg;
import com.example.android.bonghwa.Manager;
import com.example.android.bonghwa.Network;
import com.example.android.bonghwa.packet.PacketShareText;

import java.util.ArrayList;

public class ChattingActivity extends Activity {

	private static ListView mobileList;
	public static ChatListAdapter mobileAdapter = null;
	public static ArrayList<ChatMsg> mobileListitem = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatting);

		findViewById(R.id.buttonSend).setOnClickListener(onClickButton);
		findViewById(R.id.button_in).setOnClickListener(onClickButton);

		mobileAdapter = new ChatListAdapter(this);
		mobileList = (ListView) findViewById(R.id.listView);
		mobileList.setAdapter(mobileAdapter);
		mobileList.setOnItemClickListener(onClickListItem);
		mobileList.requestFocusFromTouch();

		// 키보드 숨기기
		InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	}

	@Override
	protected void onResume(){
		super.onResume();

		refreshList();
	}

	public static void refreshList(){

		if(mobileAdapter == null)
			return;

		// TODO : 효율성
		mobileAdapter.arr.clear();
		for(ChatMsg info : Manager.INSTANCE.getText()){
			//m_userlistAdapter.add("" + Manager.INSTANCE.getUserName(info.uploader) + " : " + info.text);
			mobileAdapter.arr.add(info);
		}

		mobileAdapter.notifyDataSetChanged();
		mobileList.setSelection(100);

		Device.INSTANCE.setTimerZero();
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
			public void onClick(View v) {

				EditText text=(EditText)findViewById(R.id.editMessage);
				switch (v.getId()) {
					case R.id.buttonSend:

						String s = null;

						if(Manager.INSTANCE.getCurGroupID() == Device.EMERGENCY) {
							s = text.getText().toString();
							String gps = Manager.INSTANCE.getGPSAddress();
							if(gps != null)
								s += " / 주소 : " + gps;
						}
						else
							s = text.getText().toString();

						ChatMsg info = Manager.INSTANCE.addText(
								Manager.INSTANCE.getCurGroupID(),
								Manager.INSTANCE.getMyNumber(),
								System.currentTimeMillis(),
								s);
						//DB 추가
						final DBHelper dbHelper = new DBHelper(getApplicationContext(), "chat.db", null, 1);
						dbHelper.insert(Manager.INSTANCE.getCurGroupID(),s,Manager.INSTANCE.getMyNumber());

						refreshList();

						if(info != null) {
							PacketShareText p = new PacketShareText();
							p.group = Manager.INSTANCE.getCurGroupID();
							p.uploader = info.uploader;
							p.time = info.time;
							p.text = info.text;
							Network.INSTANCE.writeAll(p);
						}

						text.setText("");

						break;

					case R.id.button_in:
						Intent intent = new Intent(getApplicationContext(), ChattingHistory.class);
						startActivity(intent);
						break;
				}
			}
	};

	private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		}
	};

}