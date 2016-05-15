package com.example.android.basicaccessibility;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class GroupHomeActivity extends Activity {

	private ListView m_list;
	public static ArrayAdapter<String> m_adapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grouphome);

		findViewById(R.id.button8).setOnClickListener(onClickButton);

		m_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		m_list = (ListView) findViewById(R.id.listView);
		m_list.setAdapter(m_adapter);
		m_list.setOnItemClickListener(onClickListItem);
	}

	Button.OnClickListener onClickButton = new View.OnClickListener() {
		public void onClick(View v) {

			EditText text=(EditText)findViewById(R.id.editMessage);
			switch (v.getId()) {
				case R.id.button8:
					String str1 = Manager.INSTANCE.connect(Manager.INSTANCE.getCurGroupID());
					Toast toast1 = Toast.makeText(getApplicationContext(), "내상태:"+str1, Toast.LENGTH_LONG);
					toast1.show();
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