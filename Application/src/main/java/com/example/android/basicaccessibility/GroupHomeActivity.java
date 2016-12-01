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

	private ListView mobileUserlist;
	public static ArrayAdapter<String> mobileUserlistAdapter = null;

	private static ListView mobileChatting;
	public static ArrayAdapter<String> mobileChattingAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grouphome);

		findViewById(R.id.button8).setOnClickListener(onClickButton);

		mobileUserlistAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		mobileUserlist = (ListView) findViewById(R.id.userlist);
		mobileUserlist.setAdapter(mobileUserlistAdapter);
		//mobileUserlist.setOnItemClickListener(onClickListItem);

		mobileChattingAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		mobileChatting = (ListView) findViewById(R.id.chatting);
		mobileChatting.setAdapter(mobileChattingAdapter);
		mobileChatting.setOnItemClickListener(onClickListItem);

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
		if(mobileUserlistAdapter == null)
			return;

		mobileUserlistAdapter.clear();

		Group g = Manager.INSTANCE.getCurGroup();
		for(Long key : g.members.keySet()) {
			String str = Manager.INSTANCE.getUserName(key);
			if(Network.INSTANCE.getServerID() == key)
				str += "(server)";
			mobileUserlistAdapter.add(str);
		}

		mobileUserlistAdapter.notifyDataSetChanged();

		// TODO : 효율성
		mobileChattingAdapter.clear();
		for(ChatMsg info : Manager.INSTANCE.getText()){
			mobileChattingAdapter.add("" + Manager.INSTANCE.getUserName(info.uploader) + " : " + info.text);
		}

		mobileChattingAdapter.notifyDataSetChanged();
		mobileChatting.setSelection(100);
	}


	private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			GroupMain.setTab(1);
		}
	};

}