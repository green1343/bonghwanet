package com.example.android.basicaccessibility;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class NewGrouplistActivity extends Activity {

	private ListView m_list;
	public static ArrayAdapter<String> m_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newgrouplist);

		m_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		m_list = (ListView) findViewById(R.id.listView);
		m_list.setAdapter(m_adapter);
		m_list.setOnItemClickListener(onClickListItem);
	}

	@Override
	protected void onResume(){
		super.onResume();

	}

	private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Toast.makeText(getApplicationContext(), m_adapter.getItem(arg2), Toast.LENGTH_SHORT).show();
		}
	};
}