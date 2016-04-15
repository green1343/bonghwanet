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

import java.util.Comparator;
import java.util.HashMap;
import java.util.StringTokenizer;

public class GrouplistActivity extends Activity {

	private ListView m_list;
	public static ArrayAdapter<String> m_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grouplist);

		findViewById(R.id.buttonCreate).setOnClickListener(onClickButton);
		findViewById(R.id.buttonEmergency).setOnClickListener(onClickButton);

		m_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		m_list = (ListView) findViewById(R.id.listView);
		m_list.setAdapter(m_adapter);
		m_list.setOnItemClickListener(onClickListItem);

		m_list.requestFocus();

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
		m_adapter.clear();
		HashMap<Long, Manager.GroupInfo> groups = Manager.INSTANCE.getAllGroups();
		for(Long id : groups.keySet()) {
			if(id == Manager.EMERGENCY)
				continue;

			Manager.GroupInfo g = groups.get(id);
			String str = new String(g.name);
			str += "\t\t";
			str += id;
			m_adapter.add(str);
		}
		m_adapter.sort(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareTo(rhs);
			}
		});
		m_adapter.notifyDataSetChanged();
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
					Manager.INSTANCE.connect(Manager.EMERGENCY);

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

			StringTokenizer t = new StringTokenizer(m_adapter.getItem(arg2), "\t");
			t.nextToken();
			Manager.INSTANCE.connect(Long.valueOf(t.nextToken()));

			Intent intent = new Intent(getApplicationContext(), GroupMain.class);
			startActivity(intent);

		}
	};

}