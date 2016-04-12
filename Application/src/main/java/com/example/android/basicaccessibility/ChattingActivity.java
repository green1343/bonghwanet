package com.example.android.basicaccessibility;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.android.packet.Packet_Share_Text;

import java.util.ArrayList;

public class ChattingActivity extends Activity {

	private static ListView m_list;
	public static ChatListAdapter m_adapter = null;
	public static ArrayList<Manager.TextInfo> m_listitem = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatting);

		findViewById(R.id.buttonSend).setOnClickListener(onClickButton);

		m_adapter = new ChatListAdapter(this);
		m_list = (ListView) findViewById(R.id.listView);
		m_list.setAdapter(m_adapter);
		m_list.setOnItemClickListener(onClickListItem);
		m_list.requestFocusFromTouch();

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

		if(m_adapter == null)
			return;

		// TODO : 효율성
		m_adapter.arr.clear();
		for(Manager.TextInfo info : Manager.INSTANCE.getText()){
			//m_adapter.add("" + Manager.INSTANCE.getUserName(info.uploader) + " : " + info.text);
			m_adapter.arr.add(info);
		}

		m_adapter.notifyDataSetChanged();
		m_list.setSelection(100);
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
			public void onClick(View v) {

				EditText text=(EditText)findViewById(R.id.editMessage);
				switch (v.getId()) {
					case R.id.buttonSend:
						Manager.TextInfo info = Manager.INSTANCE.addText(
								Manager.INSTANCE.getCurGroupID(),
								Manager.INSTANCE.getMyNumber(),
								System.currentTimeMillis(),
								text.getText().toString());

						refreshList();

						Packet_Share_Text p = new Packet_Share_Text();
						p.group = Manager.INSTANCE.getCurGroupID();
						p.uploader = info.uploader;
						p.time = info.time;
						p.text = info.text;
						WiFiNetwork.INSTANCE.writeAll(p);

						text.setText("");
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