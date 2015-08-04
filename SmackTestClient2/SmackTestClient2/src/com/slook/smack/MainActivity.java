package com.slook.smack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;

import com.slook.smack.adapter.RosterAdapter;
import com.slook.smack.model.User;
import com.slook.smack.service.MucService;
import com.slook.smack.utils.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	public final int MENU_MULCHAT = 1;
	/**
	 * �û��б�
	 */
	private List<User> userinfos = new ArrayList<User>();
	private ListView lv_allPerson;
	/**
	 * ������
	 */
	private Roster roster;
	private VCard vCard;
	
	/**
	 * ����������������
	 */
	private RosterAdapter rosterAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final String type = getIntent().getStringExtra("type");
		userinfos.clear();
		lv_allPerson = (ListView) this.findViewById(R.id.listview);
		lv_allPerson.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				if ("1".equals(type)) {
					// ���ͱ������û���JID
					// �жϿգ��ҾͲ��ж��ˡ�������
					Intent data = new Intent();
					data.putExtra("userJid", "liaonaibo1\\40slook.cc"+"@xmpp.chaoboo.com");
					// �����������Լ����ã��������ó�20
					setResult(20, data);
					Toast.makeText(MainActivity.this, "�ѷ�������",
							Toast.LENGTH_LONG).show();
					// �رյ����Activity
					finish();
				} else {
					// ˽��
				}
			}
		});
		
		//��������
		Intent service = new Intent(this,MucService.class);
		startService(service);
		// ��ȡ������
		roster = Constants.conn.getRoster();
		updateRoster();
		rosterAdapter = new RosterAdapter(this, userinfos);
		lv_allPerson.setAdapter(rosterAdapter);
	}

	/**
	 * ��ʼ���û��б�
	 */
	public void updateRoster() {
		// ��ȡ������Ŀ�����ֻ�ǻ�ȡ���к��ѵ���˼
		Collection<RosterEntry> entries = roster.getEntries();
		Log.i(TAG, "���ݳ��ȣ�" + entries.size());
		for (RosterEntry entry : entries) {
			Log.i(TAG, "entry.getUser():" + entry.getUser());
			// �����û�����ȡ��ϯ��Ϣ
			Presence presence = roster.getPresence(entry.getUser());
			User user = new User();
			user.setName(entry.getName());
			user.setUser(entry.getUser());
			user.setType(entry.getType());
			user.setSize(entry.getGroups().size());
			user.setStatus(presence.getStatus());// ״̬
			Log.i(TAG, "�û�״̬:"+ user.getStatus());
			user.setFrom(presence.getFrom());

			userinfos.add(user);
		}
		Log.i(TAG, "�û��б���:" + userinfos.size());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, MENU_MULCHAT, Menu.NONE, "��������");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case MENU_MULCHAT:
			intent = new Intent(this, ActivityMultiRoomList.class);
			startActivity(intent);
			break;
		}
		return false;// false��ʾ�������ݵ����ദ��
	}
}