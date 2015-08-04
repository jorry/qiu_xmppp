package com.slook.smack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.DiscoverItems;

import com.slook.smack.adapter.MultiRoomListAdapter;
import com.slook.smack.utils.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ActivityMultiRoomList extends Activity {
	private String TAG = "ActivityMultiRoomList";
	public final int MENU_MULCHAT = 1;
	private ProgressDialog pd;
	private List<DiscoverItems.Item> listDiscoverItems = null;
	private MultiRoomListAdapter mrlAdapter;
	private ListView lv_RoomList;
	Intent intent;
	private String roomName = "";
	private boolean fristJoin = false;
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 10:
				pd.dismiss();
				if(mrlAdapter == null){
					mrlAdapter = new MultiRoomListAdapter(
							ActivityMultiRoomList.this, listDiscoverItems);
					lv_RoomList.setAdapter(mrlAdapter);
				}else{
					mrlAdapter.notifyDataSetChanged();
					lv_RoomList.invalidate();
				}
				
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multichat);
		listDiscoverItems = new ArrayList<DiscoverItems.Item>();
		pd = new ProgressDialog(this);
		pd.setTitle("��ʾ");
		pd.setMessage("���ڸ����б�");
		pd.show();
		lv_RoomList = (ListView) this.findViewById(R.id.listview);
		new Thread(new Runnable() {
			@Override
			public void run() {
				init();
				Message msg = new Message();
				msg.what = 10;
				handler.sendMessage(msg);
			}
		}).start();
		lv_RoomList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				// ��ȡ�û�����������
				DiscoverItems.Item room = listDiscoverItems.get(position);
				intent = new Intent(ActivityMultiRoomList.this,
						ActivityMultiRoom.class);
				intent.putExtra("jid", room.getEntityID());
				intent.putExtra("action", "join");
				startActivity(intent);
			}
		});
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(fristJoin){
			listDiscoverItems.clear();
			new Thread(new Runnable() {
				@Override
				public void run() {
					init();
					Message msg = new Message();
					msg.what = 10;
					handler.sendMessage(msg);
				}
			}).start();
		}
		fristJoin = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		fristJoin = true;
	}


	/**
	 * ��ʼ�������б�
	 */
	public void init() {
		
		// �����XMPPConnection��ص�ServiceDiscoveryManager
		ServiceDiscoveryManager discoManager = ServiceDiscoveryManager
				.getInstanceFor(Constants.conn);

		// ���ָ��XMPPʵ�����Ŀ
		// ������ӻ��������Ŀ¼������ص���Ŀ
		DiscoverItems discoItems;
		try {
			discoItems = discoManager
					.discoverItems("conference.xmpp.chaoboo.com");
			// ��ñ���ѯ��XMPPʵ���Ҫ�鿴����Ŀ
			Iterator it = discoItems.getItems();
			// ��ʾԶ��XMPPʵ�����Ŀ
			while (it.hasNext()) {
				DiscoverItems.Item item = (DiscoverItems.Item) it.next();
				listDiscoverItems.add(item);
			}
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, MENU_MULCHAT, Menu.NONE, "��������");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_MULCHAT:

			LayoutInflater inflater = getLayoutInflater();
			final View layout = inflater.inflate(R.layout.showcreateroom,
					(ViewGroup) findViewById(R.id.dialog));

			AlertDialog.Builder builder = new Builder(
					ActivityMultiRoomList.this);
			builder.setTitle("�����뷿����");
			builder.setView(layout);
			builder.setPositiveButton("ȷ��", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText et = (EditText) layout
							.findViewById(R.id.et_roomName);
					roomName = et.getText().toString();
					createRoom();
					dialog.dismiss();
				}
			});

			builder.setNegativeButton("ȡ��", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
			break;
		}
		return false;// false��ʾ�������ݵ����ദ��
	}

	private void createRoom() {

		intent = new Intent(ActivityMultiRoomList.this, ActivityMultiRoom.class);
		intent.putExtra("jid", roomName + "@conference.xmpp.chaoboo.com");
		intent.putExtra("action", "create");
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
