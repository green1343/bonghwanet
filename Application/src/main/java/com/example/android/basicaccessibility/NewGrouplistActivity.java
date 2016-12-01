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

import com.example.android.bonghwa.Device;
import com.example.android.bonghwa.GroupInfo.Group;
import com.example.android.bonghwa.Manager;
import com.example.android.bonghwa.needclass.MyThread;
import com.example.android.bonghwa.Network;
import com.example.android.bonghwa.packet.PacketJoinRequest;

import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class NewGrouplistActivity extends Activity {

	ArrayAdapter<String> mobileAdapter;
	ListView mobileList;

	MyThread mobileRefreshThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newgrouplist);

		mobileAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item);
		mobileList = (ListView) findViewById(R.id.listView);
		mobileList.setAdapter(mobileAdapter);
		mobileList.setOnItemClickListener(onClickListItem);
	}

	private Handler mobileRefreshHandler = new Handler() {
		public void handleMessage(Message msg) {
			refreshList();
		}
	};

	@Override
	protected void onResume(){
		super.onResume();
		refreshList();

		mobileRefreshThread = new MyThread() {

			public void run() {
				while (!Thread.interrupted() && running) {
					try {
						Message msg = Message.obtain(mobileRefreshHandler, 0 , 1 , 0);
						mobileRefreshHandler.sendMessage(msg);
						Thread.sleep(3000);
					} catch (Throwable t) {
					}
				}
			}
		};

		mobileRefreshThread.start();
	}

	@Override
	protected void onPause(){
		super.onPause();

		mobileRefreshThread.interrupt();
		mobileRefreshThread.running = false;
		mobileRefreshThread = null;
	}

	public void refreshList(){

		// TODO: 중복 제거

		mobileAdapter.clear();

		Device d = Device.INSTANCE;
		HashMap<Long, Group> groups = Manager.INSTANCE.getAllGroups();

		if (!d.getWifiManager().isWifiEnabled())
			d.getWifiManager().setWifiEnabled(true);

		d.getWifiManager().startScan();
		List<ScanResult> results = d.getWifiManager().getScanResults();

		for(ScanResult r : results){
			if(r.SSID.startsWith(d.RESERVED_SSID)){
				StringTokenizer t = new StringTokenizer(r.SSID, "_");
				String idStr = null;
				String indexStr = null;
				String nameStr = null;
				if(t.hasMoreTokens()) t.nextToken();
				if(t.hasMoreTokens()) idStr = t.nextToken();
				if(t.hasMoreTokens()) indexStr = t.nextToken();
				if(t.hasMoreTokens()) nameStr = t.nextToken();

				if(nameStr == null)
					continue;

				if(groups.containsKey(Long.valueOf(idStr)) == false){
					String str = new String(nameStr);
					str += "\t\t";
					str += idStr;
					mobileAdapter.add(str);
					break;
				}
			}
		}

		mobileAdapter.notifyDataSetChanged();
	}

	private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

			StringTokenizer t = new StringTokenizer(mobileAdapter.getItem(arg2), "\t");
			t.nextToken();
			Manager.INSTANCE.setTempObject(Long.valueOf(t.nextToken()));

			AlertDialog.Builder d = new AlertDialog.Builder(Manager.INSTANCE.getContext());
			d.setMessage("그룹에 가입신청 하시겠습니까?");
			d.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Manager.INSTANCE.setCurGroup((Long) Manager.INSTANCE.getTempObject());
					Device.INSTANCE.setClient();
					Manager.INSTANCE.setWatingJoin(true);

					Thread myThread = new Thread(new Runnable() {
						public void run() {
							int cnt = 0;
							while (!Thread.interrupted()) {
								try {
									if (Manager.INSTANCE.getJoinGroup() != null) {
										PacketJoinRequest p = new PacketJoinRequest();
										p.group = Manager.INSTANCE.getCurGroupID();
										p.userID = Manager.INSTANCE.getMyNumber();
										p.userInfo = Manager.INSTANCE.getMyUserInfo();
										Network.INSTANCE.writeAll(p);

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