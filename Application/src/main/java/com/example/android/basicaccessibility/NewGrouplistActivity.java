package com.example.android.basicaccessibility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.packet.Packet_Join_Request;

import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class NewGrouplistActivity extends Activity {

	ArrayAdapter<String> m_adapter;
	ListView m_list;

	MyThread m_refreshThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newgrouplist);

		m_adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		m_list = (ListView) findViewById(R.id.listView);
		m_list.setAdapter(m_adapter);
		m_list.setOnItemClickListener(onClickListItem);
	}

	private Handler m_refreshHandler = new Handler() {
		public void handleMessage(Message msg) {
			refreshList();
		}
	};

	@Override
	protected void onResume(){
		super.onResume();
		refreshList();

		m_refreshThread = new MyThread() {

			public void run() {
				while (!Thread.interrupted() && running) {
					try {
						Message msg = Message.obtain(m_refreshHandler, 0 , 1 , 0);
						m_refreshHandler.sendMessage(msg);
						Thread.sleep(3000);
					} catch (Throwable t) {
					}
				}
			}
		};

		m_refreshThread.start();
	}

	@Override
	protected void onPause(){
		super.onPause();

		m_refreshThread.interrupt();
		m_refreshThread.running = false;
		m_refreshThread = null;
	}

	public void refreshList(){

		// TODO: 중복 제거

		m_adapter.clear();

		Manager m = Manager.INSTANCE;
		HashMap<Long, Manager.GroupInfo> groups = m.getAllGroups();

		if (!m.getWifiManager().isWifiEnabled())
			m.getWifiManager().setWifiEnabled(true);

		m.getWifiManager().startScan();
		List<ScanResult> results = m.getWifiManager().getScanResults();

		for(ScanResult r : results){
			if(r.SSID.startsWith(m.RESERVED_SSID)){
				StringTokenizer t = new StringTokenizer(r.SSID, "_");
				String idStr = null;
				String nameStr = null;
				if(t.hasMoreTokens()) t.nextToken();
				if(t.hasMoreTokens()) idStr = t.nextToken();
				if(t.hasMoreTokens()) nameStr = t.nextToken();

				if(nameStr == null)
					continue;

				if(groups.containsKey(Long.valueOf(idStr)) == false){
					String str = new String(nameStr);
					str += "\t\t";
					str += idStr;
					m_adapter.add(str);
					break;
				}
			}
		}

		m_adapter.notifyDataSetChanged();
	}

	private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

			StringTokenizer t = new StringTokenizer(m_adapter.getItem(arg2), "\t");
			t.nextToken();
			Manager.INSTANCE.setTempObject(Long.valueOf(t.nextToken()));

			AlertDialog.Builder d = new AlertDialog.Builder(Manager.INSTANCE.getContext());
			d.setMessage("그룹에 가입신청 하시겠습니까?");
			d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Manager.INSTANCE.setCurGroup((Long) Manager.INSTANCE.getTempObject());
					if (Manager.INSTANCE.setClient()) {

						Manager.INSTANCE.setWatingJoin(true);

						Thread myThread = new Thread(new Runnable() {
							public void run() {
								int cnt = 0;
								while (!Thread.interrupted()) {
									try {
										if (Manager.INSTANCE.getJoinGroup() != null) {
											Packet_Join_Request p = new Packet_Join_Request();
											p.group = Manager.INSTANCE.getCurGroupID();
											p.userID = Manager.INSTANCE.getMyNumber();
											p.userInfo = Manager.INSTANCE.getMyUserInfo();
											WiFiNetwork.INSTANCE.writeAll(p);

											break;
										}
										Thread.sleep(1000);

										++cnt;
										if(cnt > 20)
											break;

									} catch (Throwable t) {
									}
								}
							}
						});

						myThread.start();
					}
				}
			});
			d.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Manager.INSTANCE.setTempObject(null);
					dialog.cancel();
				}
			});
			d.show();

			//Toast.makeText(getApplicationContext(), m_userlistAdapter.getItem(arg2), Toast.LENGTH_SHORT).show();
		}
	};
}