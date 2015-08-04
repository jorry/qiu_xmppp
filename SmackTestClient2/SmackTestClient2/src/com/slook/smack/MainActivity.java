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
	 * 用户列表
	 */
	private List<User> userinfos = new ArrayList<User>();
	private ListView lv_allPerson;
	/**
	 * 花名册
	 */
	private Roster roster;
	private VCard vCard;
	
	/**
	 * 花名册数据适配器
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
					// 传送被邀请用户的JID
					// 判断空，我就不判断了。。。。
					Intent data = new Intent();
					data.putExtra("userJid", "liaonaibo1\\40slook.cc"+"@xmpp.chaoboo.com");
					// 请求代码可以自己设置，这里设置成20
					setResult(20, data);
					Toast.makeText(MainActivity.this, "已发出邀请",
							Toast.LENGTH_LONG).show();
					// 关闭掉这个Activity
					finish();
				} else {
					// 私聊
				}
			}
		});
		
		//开启服务
		Intent service = new Intent(this,MucService.class);
		startService(service);
		// 获取花名册
		roster = Constants.conn.getRoster();
		updateRoster();
		rosterAdapter = new RosterAdapter(this, userinfos);
		lv_allPerson.setAdapter(rosterAdapter);
	}

	/**
	 * 初始化用户列表
	 */
	public void updateRoster() {
		// 获取所有条目，大概只是获取所有好友的意思
		Collection<RosterEntry> entries = roster.getEntries();
		Log.i(TAG, "数据长度：" + entries.size());
		for (RosterEntry entry : entries) {
			Log.i(TAG, "entry.getUser():" + entry.getUser());
			// 根据用户名获取出席信息
			Presence presence = roster.getPresence(entry.getUser());
			User user = new User();
			user.setName(entry.getName());
			user.setUser(entry.getUser());
			user.setType(entry.getType());
			user.setSize(entry.getGroups().size());
			user.setStatus(presence.getStatus());// 状态
			Log.i(TAG, "用户状态:"+ user.getStatus());
			user.setFrom(presence.getFrom());

			userinfos.add(user);
		}
		Log.i(TAG, "用户列表长度:" + userinfos.size());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, MENU_MULCHAT, Menu.NONE, "多人聊天");
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
		return false;// false表示继续传递到父类处理
	}
}