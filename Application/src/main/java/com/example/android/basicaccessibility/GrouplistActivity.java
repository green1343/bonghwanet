package com.example.android.basicaccessibility;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.android.basicaccessibility.Emergency.Emergency_main;
import com.example.android.bonghwa.Device;
import com.example.android.bonghwa.GroupInfo.Group;
import com.example.android.bonghwa.Manager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.StringTokenizer;

public class GrouplistActivity extends Activity {

	private ListView mobileList;
	public static ArrayAdapter<String> mobileAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grouplist);

		findViewById(R.id.buttonCreate).setOnClickListener(onClickButton);
		findViewById(R.id.buttonEmergency).setOnClickListener(onClickButton);

		mobileAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		mobileList = (ListView) findViewById(R.id.listView);
		mobileList.setAdapter(mobileAdapter);
		mobileList.setOnItemClickListener(onClickListItem);

		mobileList.requestFocus();

		//InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		//imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

		// 키보드 숨기기
		InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	}

	@Override
	protected void onResume(){
		super.onResume();

		refreshList();
	}

	private void refreshList(){
		mobileAdapter.clear();
		HashMap<Long, Group> groups = Manager.INSTANCE.getAllGroups();
		for(Long id : groups.keySet()) {
			if(id == Device.EMERGENCY)
				continue;

			Group g = groups.get(id);
			String str = new String(g.name);
			str += "\t\t";
			str += id;
			mobileAdapter.add(str);
		}
		mobileAdapter.sort(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareTo(rhs);
			}
		});
		mobileAdapter.notifyDataSetChanged();
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
		public void onClick(View v) {
			// 키보드 숨기기
			InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
			immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

			EditText textFruit=(EditText)findViewById(R.id.editGroupname);
			switch (v.getId()) {
				case R.id.buttonCreate:
					Manager.INSTANCE.createGroup(textFruit.getText().toString());
					refreshList();

					textFruit.setText("");
					break;

				case R.id.buttonEmergency:
					Device.INSTANCE.connect(Device.EMERGENCY, false);

					Intent intent = new Intent(getApplicationContext(), Emergency_main.class);
					startActivity(intent);
					break;
			}
		}
	};

	private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

			// 키보드 숨기기
			InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
			immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

			StringTokenizer t = new StringTokenizer(mobileAdapter.getItem(arg2), "\t");
			t.nextToken();
			Device.INSTANCE.connect(Long.valueOf(t.nextToken()), false);

			Intent intent = new Intent(getApplicationContext(), GroupMain.class);
			startActivity(intent);

		}
	};

}