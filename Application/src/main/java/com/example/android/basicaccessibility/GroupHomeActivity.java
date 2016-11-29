package com.example.android.basicaccessibility;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.android.bonghwa.Device;
import com.example.android.bonghwa.GroupInfo.ChatMsg;
import com.example.android.bonghwa.GroupInfo.Group;
import com.example.android.bonghwa.Manager;
import com.example.android.bonghwa.Network;

public class GroupHomeActivity extends Activity {

	private ListView m_userlist;
	public static ArrayAdapter<String> m_userlistAdapter = null;

	private static ListView m_chatting;
	public static ArrayAdapter<String> m_chattingAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grouphome);

		findViewById(R.id.button8).setOnClickListener(onClickButton);

		m_userlistAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		m_userlist = (ListView) findViewById(R.id.userlist);
		m_userlist.setAdapter(m_userlistAdapter);
		//m_userlist.setOnItemClickListener(onClickListItem);

		m_chattingAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		m_chatting = (ListView) findViewById(R.id.chatting);
		m_chatting.setAdapter(m_chattingAdapter);
		m_chatting.setOnItemClickListener(onClickListItem);

		// 키보드 숨기기
		InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
		public void onClick(View v) {

			EditText text=(EditText)findViewById(R.id.editMessage);
			switch (v.getId()) {
				case R.id.button8:
					if(Network.INSTANCE.isServer())
						Device.INSTANCE.connect(Manager.INSTANCE.getCurGroupID(), false);
					else
						Device.INSTANCE.connect(Manager.INSTANCE.getCurGroupID(), true);
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onResume(){
		super.onResume();

		refreshList();
	}

	public static void refreshList(){
		if(m_userlistAdapter == null)
			return;

		m_userlistAdapter.clear();

		Group g = Manager.INSTANCE.getCurGroup();
		for(Long key : g.members.keySet()) {
			String str = Manager.INSTANCE.getUserName(key);
			if(Network.INSTANCE.getServerID() == key)
				str += "(server)";
			m_userlistAdapter.add(str);
		}

		m_userlistAdapter.notifyDataSetChanged();

		// TODO : 효율성
		m_chattingAdapter.clear();
		for(ChatMsg info : Manager.INSTANCE.getText()){
			m_chattingAdapter.add("" + Manager.INSTANCE.getUserName(info.uploader) + " : " + info.text);
		}

		m_chattingAdapter.notifyDataSetChanged();
		m_chatting.setSelection(100);
	}


	private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			GroupMain.setTab(1);
		}
	};

}