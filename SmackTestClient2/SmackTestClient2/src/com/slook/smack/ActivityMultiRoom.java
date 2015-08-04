package com.slook.smack;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.packet.DelayInformation;

import com.slook.smack.adapter.MemberAdapter;
import com.slook.smack.utils.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ActivityMultiRoom extends Activity implements OnClickListener {

	private String TAG = "ActivityMultiRoom";
	private final int RECEIVE = 1;
	private final int MEMBER = 2;
	public final int MENU_MULCHAT = 1;
	public final int MENU_DESTROY = 2;
	private Button send;
	private Button showHistory;
	private EditText et_Record, et_Message;
	private ListView lv_Members;
	/**
	 * �����ҳ�Ա
	 */
	private List<String> affiliates = new ArrayList<String>();
	private MultiUserChat muc;
	private MessageReceiver mUpdateMessage;
	private MemberAdapter memberAdapter;
	private boolean isHistory = false;
	private int count = 0;
	private String history = "";
	SharedPreferences sp = null;
	/**
	 * ����ID
	 */
	private String jid;

	private ChatPacketListener chatListener;
	private MyPacketListener myPacketListener;
	private MyParticipantStatusListener myParticipantStatusListener;

	public Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case RECEIVE: {
				// ����Ϣ
				Bundle bd = msg.getData();
				String from = bd.getString("from");
				String body = bd.getString("body");
				history += from + ":" + msg + "\n";
				if (isHistory) {
					receiveMsg(from, body);
				} else {
					Editor editor = sp.edit();
					editor.putString("historyMessage", history);
					editor.commit();
					System.out.println("��������ʷ��Ϣ");
				}
			}
				break;
			case MEMBER:
				if (memberAdapter == null) {
					// ���г�Ա�б�
					memberAdapter = new MemberAdapter(ActivityMultiRoom.this,
							affiliates);
					lv_Members.setAdapter(memberAdapter);
				} else {
					memberAdapter.notifyDataSetChanged();
					lv_Members.invalidate();
				}
				Log.i(TAG, "��Ա�б� " + affiliates.size() + " ����");
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiroom);

		// ����Ϣ
		sp = getSharedPreferences("history", Context.MODE_PRIVATE);
		// ����������Ʊ����Ǵ���������Ǹ�����
		jid = getIntent().getStringExtra("jid");
		send = (Button) findViewById(R.id.send);
		showHistory = (Button) findViewById(R.id.showHistory);
		et_Record = (EditText) findViewById(R.id.record);
		et_Message = (EditText) findViewById(R.id.message);
		lv_Members = (ListView) this.findViewById(R.id.listview);
		send.setOnClickListener(this);
		showHistory.setOnClickListener(this);
		muc = new MultiUserChat(Constants.conn, jid);
		chatListener = new ChatPacketListener(muc);
		myPacketListener = new MyPacketListener();
		myParticipantStatusListener = new MyParticipantStatusListener();
		String action = getIntent().getStringExtra("action");
		try {
			System.out.println("����ţ�" + jid);
			if ("join".equals(action)) {
				// ���뷿����nickname(�ǳ�)
				String nickName = Constants.vCard.getNickName().toString();
				muc.join(nickName);
				Log.v(TAG, "join success");
			} else {
				// �������䲢����
				createRoom(jid);
				Log.v(TAG, "create success");
			}
			// �������̼߳��س�Ա
			getAllMember();
			muc.addMessageListener(chatListener);
			muc.addParticipantListener(myPacketListener);
			muc.addParticipantStatusListener(myParticipantStatusListener);

		} catch (XMPPException e) {
			e.printStackTrace();
		}
		registerForContextMenu(lv_Members);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 20:
			Log.i(TAG, "��������");
			String userjid = data.getExtras().getString("userJid");
			if (userjid != null || !"".equals(userjid)) {
				muc.invite(userjid, "��̸̸����");
			}
			break;
		}
	}

	/**
	 * ��ȡ�����ҵ����г�Ա
	 */
	private void getAllMember() {
		Log.i(TAG, "��ȡ�����ҵ����г�Ա");
		affiliates.clear();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Iterator<String> it = muc.getOccupants();
					while (it.hasNext()) {
						String name = it.next();
						name = name.substring(name.indexOf("/") + 1);
						affiliates.add("[����]" + name);
						Log.i(TAG, "��Ա����;" + name);
					}

					android.os.Message msg = new android.os.Message();
					msg.what = MEMBER;
					handler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// ������Ϣ
		case R.id.send: {
			String msg = et_Message.getText().toString();
			if (!"".equals(msg)) {
				try {
					muc.sendMessage(msg);
				} catch (Exception e) {
					Log.i(TAG, "�����쳣");
					e.printStackTrace();
				} finally {
					et_Message.setText("");
				}
			}
		}
			break;
		case R.id.showHistory:
			String newMessage = et_Message.getText().toString();

			String history = sp.getString("historyMessage", null);
			System.out.println("��ʷ��Ϣ��" + history);
			et_Record.setText(history + newMessage);
			break;
		}
	}

	/**
	 * ����Ϣ���������Ϣ
	 * 
	 * @param from
	 * @param msg
	 */
	private void receiveMsg(String from, String msg) {
		Log.v(TAG, "�����Ϣ�� " + from + " :" + msg);
		et_Record.setText(et_Record.getText() + from + ":" + msg + "\n");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// �����ж����NULL
		muc.removeMessageListener(chatListener);
		muc.removeParticipantListener(myPacketListener);
		muc.removeParticipantStatusListener(myParticipantStatusListener);
		chatListener = null;
		myPacketListener = null;
		myParticipantStatusListener = null;
		muc.leave();
		muc = null;
		affiliates = null;
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, MENU_MULCHAT, Menu.NONE, "����");
		menu.add(2, MENU_DESTROY, Menu.NONE, "����");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// ����ListView�����ĶԻ���
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.notemenu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int id = (int) info.id;
		switch (item.getItemId()) {
		case R.id.mn_tall:
			// ˽��
			break;
		case R.id.mn_Grant:
			// ��Ȩ
			break;
		case R.id.mn_kick:
			// ����
			try {
				String nickName = affiliates.get(id);
				muc.kickParticipant(nickName
						.substring(nickName.indexOf("]") + 1), "���㲻ˬ�� ������");
				getAllMember();
				android.os.Message msg = new android.os.Message();
				msg.what = MEMBER;
				handler.sendMessage(msg);
				Toast.makeText(this, "����T��", Toast.LENGTH_LONG).show();
			} catch (XMPPException e) {
				e.printStackTrace();
				Toast.makeText(this, "��û��Ȩ������", Toast.LENGTH_LONG).show();
			}
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case MENU_MULCHAT:
			intent = new Intent(ActivityMultiRoom.this, MainActivity.class);
			intent.putExtra("type", "1");
			startActivityForResult(intent, 0);
			break;
		case MENU_DESTROY:
			// ���ٷ��䣬����JID
			try {
				muc.destroy("��Ҫ�ˣ����ٵ���", jid);
			} catch (XMPPException e) {
				e.printStackTrace();
				Log.i(TAG, "����ʧ��");
			}
			intent = new Intent(ActivityMultiRoom.this, MainActivity.class);
			startActivity(intent);
			break;
		}
		return false;// false��ʾ�������ݵ����ദ��
	}

	/**
	 * ��������
	 */
	public void createRoom(String room) {
		// ʹ��XMPPConnection����һ��MultiUserChat
		// MultiUserChat muc = new MultiUserChat(Constants.conn, room
		// + "@conference.xmpp.chaoboo.com");
		try {
			// ����������
			muc.create(Constants.vCard.getNickName().toString());
			// ��������ҵ����ñ�
			Form form = muc.getConfigurationForm();
			System.out.println("form:" + form.toString());
			// ����ԭʼ������һ��Ҫ�ύ���±���
			Form submitForm = form.createAnswerForm();
			// ��Ҫ�ύ�ı����Ĭ�ϴ�
			for (Iterator fields = form.getFields(); fields.hasNext();) {
				FormField field = (FormField) fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType())
						&& field.getVariable() != null) {
					// ����Ĭ��ֵ��Ϊ��
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}
			// ���������ҵ���ӵ����
			// List owners = new ArrayList();
			// owners.add("liaonaibo2\\40slook.cc");
			// owners.add("liaonaibo1\\40slook.cc");
			// submitForm.setAnswer("muc#roomconfig_roomowners", owners);
			// �����������ǳ־������ң�����Ҫ����������
			submitForm.setAnswer("muc#roomconfig_persistentroom", true);
			// ������Գ�Ա����
			submitForm.setAnswer("muc#roomconfig_membersonly", false);
			// ����ռ��������������
			submitForm.setAnswer("muc#roomconfig_allowinvites", true);
			// �ܹ�����ռ������ʵ JID �Ľ�ɫ
			// submitForm.setAnswer("muc#roomconfig_whois", "anyone");
			// ��¼����Ի�
			submitForm.setAnswer("muc#roomconfig_enablelogging", true);
			// ������ע����ǳƵ�¼
			submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
			// ����ʹ�����޸��ǳ�
			submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
			// �����û�ע�᷿��
			submitForm.setAnswer("x-muc#roomconfig_registration", false);
			// ��������ɵı�����Ĭ��ֵ����������������������
			muc.sendConfigurationForm(submitForm);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	/**
	 * PacketListener ͨ��һ���涨�Ĺ������ṩһ���������������ݰ�
	 * 
	 * @author liaonaibo
	 * 
	 */
	class ChatPacketListener implements PacketListener {
		private String _number;
		private Date _lastDate;
		private MultiUserChat _muc;
		private String _roomName;

		public ChatPacketListener(MultiUserChat muc) {
			_number = "0";
			_lastDate = new Date(0);
			_muc = muc;
			_roomName = muc.getRoom();
		}

		@Override
		public void processPacket(Packet packet) {
			System.out.println("��Ϣ��ʽ:" + packet.toXML());
			Message message = (Message) packet;
			String from = message.getFrom();

			if (message.getBody() != null) {
				DelayInformation inf = (DelayInformation) message.getExtension(
						"x", "jabber:x:delay");
				System.out.println("�ж���Ϣ");
				if (inf == null && count >= 1) {
					System.out.println("����Ϣ����");
					isHistory = true;
				} else {
					System.out.println("���Ǿɵ���Ϣ");
				}
				android.os.Message msg = new android.os.Message();
				msg.what = RECEIVE;
				Bundle bd = new Bundle();
				bd.putString("from", from);
				bd.putString("body", message.getBody());
				msg.setData(bd);
				handler.sendMessage(msg);
			}
			count++;
		}
	}

	/**
	 * �����ҳ�Ա�ļ�����
	 * 
	 * @author ���˲�
	 * 
	 */
	class MyParticipantStatusListener implements ParticipantStatusListener {

		@Override
		public void adminGranted(String arg0) {
			Log.i(TAG, "ִ����adminGranted����:" + arg0);
		}

		@Override
		public void adminRevoked(String arg0) {
			Log.i(TAG, "ִ����adminRevoked����:" + arg0);
		}

		@Override
		public void banned(String arg0, String arg1, String arg2) {
			Log.i(TAG, "ִ����banned����:" + arg0);
		}

		@Override
		public void joined(String arg0) {
			Log.i(TAG, "ִ����joined����:" + arg0 + "�����˷���");
			getAllMember();
			android.os.Message msg = new android.os.Message();
			msg.what = MEMBER;
			handler.sendMessage(msg);
		}

		@Override
		public void kicked(String arg0, String arg1, String arg2) {
			Log.i(TAG, "ִ����kicked����:" + arg0 + "���߳�����");
		}

		@Override
		public void left(String arg0) {
			String lefter = arg0.substring(arg0.indexOf("/") + 1);
			Log.i(TAG, "ִ����left����:" + lefter + "�뿪�ķ���");
			getAllMember();
			android.os.Message msg = new android.os.Message();
			msg.what = MEMBER;
			handler.sendMessage(msg);
		}

		@Override
		public void membershipGranted(String arg0) {
			Log.i(TAG, "ִ����membershipGranted����:" + arg0);
		}

		@Override
		public void membershipRevoked(String arg0) {
			Log.i(TAG, "ִ����membershipRevoked����:" + arg0);
		}

		@Override
		public void moderatorGranted(String arg0) {
			Log.i(TAG, "ִ����moderatorGranted����:" + arg0);
		}

		@Override
		public void moderatorRevoked(String arg0) {
			Log.i(TAG, "ִ����moderatorRevoked����:" + arg0);
		}

		@Override
		public void nicknameChanged(String arg0, String arg1) {
			Log.i(TAG, "ִ����nicknameChanged����:" + arg0);
		}

		@Override
		public void ownershipGranted(String arg0) {
			Log.i(TAG, "ִ����ownershipGranted����:" + arg0);
		}

		@Override
		public void ownershipRevoked(String arg0) {
			Log.i(TAG, "ִ����ownershipRevoked����:" + arg0);
		}

		@Override
		public void voiceGranted(String arg0) {
			Log.i(TAG, "ִ����voiceGranted����:" + arg0);
		}

		@Override
		public void voiceRevoked(String arg0) {
			Log.i(TAG, "ִ����voiceRevoked����:" + arg0);
		}
	}

	/**
	 * 
	 ****************************************** 
	 * @author ���˲� �ļ����� : MyPacketListener.java ����ʱ�� : 2012-4-24 ����08:32:13 �ļ�����
	 ****************************************** 
	 */
	public class MyPacketListener implements PacketListener {

		@Override
		public void processPacket(Packet arg0) {
			// ����--------------chat
			// æµ--------------dnd
			// �뿪--------------away
			// ����--------------xa
			Presence presence = (Presence) arg0;
			// PacketExtension pe = presence.getExtension("x",
			// "http://jabber.org/protocol/muc#user");
			String LogKineName = presence.getFrom().toString();
			String kineName = LogKineName
					.substring(LogKineName.indexOf("/") + 1);
			String stats = "";
			if ("chat".equals(presence.getMode().toString())) {
				stats = "[����]";
			}
			if ("dnd".equals(presence.getMode().toString())) {
				stats = "[æµ]";
			}
			if ("away".equals(presence.getMode().toString())) {
				stats = "[�뿪]";
			}
			if ("xa".equals(presence.getMode().toString())) {
				stats = "[����]";
			}

			for (int i = 0; i < affiliates.size(); i++) {
				String name = affiliates.get(i);
				if (kineName.equals(name.substring(name.indexOf("]") + 1))) {
					affiliates.set(i, stats + kineName);
					System.out.println("״̬�ı�ɣ�" + affiliates.get(i));
					android.os.Message msg = new android.os.Message();
					msg.what = MEMBER;
					handler.sendMessage(msg);
					break;
				}
			}
		}
	}
}
