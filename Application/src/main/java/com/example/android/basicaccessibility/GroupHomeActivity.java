package com.example.android.basicaccessibility;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GroupHomeActivity extends Activity {

	private ListView m_list;
	public static ArrayAdapter<String> m_adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grouphome);

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

	public static void refreshList(){
		if(m_adapter == null)
			return;

		m_adapter.clear();

		Manager.GroupInfo g = Manager.INSTANCE.getCurGroup();
		for(Long key : g.members.keySet())
			m_adapter.add(Manager.INSTANCE.getUserName(key));

		m_adapter.notifyDataSetChanged();
	}


	private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		}
	};

}