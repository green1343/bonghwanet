package com.example.android.basicaccessibility;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

		m_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		m_list = (ListView) findViewById(R.id.listView);
		m_list.setAdapter(m_adapter);
		m_list.setOnItemClickListener(onClickListItem);
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
			EditText textFruit=(EditText)findViewById(R.id.editGroupname);
			switch (v.getId()) {
				case R.id.buttonCreate:
					// TODO : delete
					Manager.INSTANCE.createGroup(textFruit.getText().toString());
					refreshList();

					textFruit.setText("");
					break;
			}
		}
	};

	private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

			StringTokenizer t = new StringTokenizer(m_adapter.getItem(arg2), "\t");
			t.nextToken();
			Intent intent = new Intent(getApplicationContext(), Main2.class);
			startActivity(intent);

			Manager.INSTANCE.connect(Long.valueOf(t.nextToken()));
			//Toast.makeText(getApplicationContext(), m_adapter.getItem(arg2), Toast.LENGTH_SHORT).show();
		}
	};

}