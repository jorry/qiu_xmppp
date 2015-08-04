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
	 * 聊天室成员
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
	 * 房间ID
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
				// 新消息
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
					System.out.println("保存了历史消息");
				}
			}
				break;
			case MEMBER:
				if (memberAdapter == null) {
					// 更行成员列表
					memberAdapter = new MemberAdapter(ActivityMultiRoom.this,
							affiliates);
					lv_Members.setAdapter(memberAdapter);
				} else {
					memberAdapter.notifyDataSetChanged();
					lv_Members.invalidate();
				}
				Log.i(TAG, "成员列表 " + affiliates.size() + " 个！");
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiroom);

		// 旧消息
		sp = getSharedPreferences("history", Context.MODE_PRIVATE);
		// 后面服务名称必需是创建房间的那个服务
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
			System.out.println("房间号：" + jid);
			if ("join".equals(action)) {
				// 进入房间后的nickname(昵称)
				String nickName = Constants.vCard.getNickName().toString();
				muc.join(nickName);
				Log.v(TAG, "join success");
			} else {
				// 创建房间并加入
				createRoom(jid);
				Log.v(TAG, "create success");
			}
			// 开启子线程加载成员
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
			Log.i(TAG, "邀请人了");
			String userjid = data.getExtras().getString("userJid");
			if (userjid != null || !"".equals(userjid)) {
				muc.invite(userjid, "来谈谈人生");
			}
			break;
		}
	}

	/**
	 * 获取聊天室的所有成员
	 */
	private void getAllMember() {
		Log.i(TAG, "获取聊天室的所有成员");
		affiliates.clear();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Iterator<String> it = muc.getOccupants();
					while (it.hasNext()) {
						String name = it.next();
						name = name.substring(name.indexOf("/") + 1);
						affiliates.add("[空闲]" + name);
						Log.i(TAG, "成员名字;" + name);
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
		// 发送消息
		case R.id.send: {
			String msg = et_Message.getText().toString();
			if (!"".equals(msg)) {
				try {
					muc.sendMessage(msg);
				} catch (Exception e) {
					Log.i(TAG, "发送异常");
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
			System.out.println("历史消息：" + history);
			et_Record.setText(history + newMessage);
			break;
		}
	}

	/**
	 * 往消息框中添加信息
	 * 
	 * @param from
	 * @param msg
	 */
	private void receiveMsg(String from, String msg) {
		Log.v(TAG, "添加消息： " + from + " :" + msg);
		et_Record.setText(et_Record.getText() + from + ":" + msg + "\n");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 把所有对象变NULL
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
		menu.add(1, MENU_MULCHAT, Menu.NONE, "邀请");
		menu.add(2, MENU_DESTROY, Menu.NONE, "销毁");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// 长按ListView弹出的对话框
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
			// 私聊
			break;
		case R.id.mn_Grant:
			// 授权
			break;
		case R.id.mn_kick:
			// 踢人
			try {
				String nickName = affiliates.get(id);
				muc.kickParticipant(nickName
						.substring(nickName.indexOf("]") + 1), "看你不爽就 踢了你");
				getAllMember();
				android.os.Message msg = new android.os.Message();
				msg.what = MEMBER;
				handler.sendMessage(msg);
				Toast.makeText(this, "他被T了", Toast.LENGTH_LONG).show();
			} catch (XMPPException e) {
				e.printStackTrace();
				Toast.makeText(this, "你没有权利踢人", Toast.LENGTH_LONG).show();
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
			// 销毁房间，根据JID
			try {
				muc.destroy("不要了，销毁掉！", jid);
			} catch (XMPPException e) {
				e.printStackTrace();
				Log.i(TAG, "销毁失败");
			}
			intent = new Intent(ActivityMultiRoom.this, MainActivity.class);
			startActivity(intent);
			break;
		}
		return false;// false表示继续传递到父类处理
	}

	/**
	 * 创建房间
	 */
	public void createRoom(String room) {
		// 使用XMPPConnection创建一个MultiUserChat
		// MultiUserChat muc = new MultiUserChat(Constants.conn, room
		// + "@conference.xmpp.chaoboo.com");
		try {
			// 创建聊天室
			muc.create(Constants.vCard.getNickName().toString());
			// 获得聊天室的配置表单
			Form form = muc.getConfigurationForm();
			System.out.println("form:" + form.toString());
			// 根据原始表单创建一个要提交的新表单。
			Form submitForm = form.createAnswerForm();
			// 向要提交的表单添加默认答复
			for (Iterator fields = form.getFields(); fields.hasNext();) {
				FormField field = (FormField) fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType())
						&& field.getVariable() != null) {
					// 设置默认值作为答复
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}
			// 设置聊天室的新拥有者
			// List owners = new ArrayList();
			// owners.add("liaonaibo2\\40slook.cc");
			// owners.add("liaonaibo1\\40slook.cc");
			// submitForm.setAnswer("muc#roomconfig_roomowners", owners);
			// 设置聊天室是持久聊天室，即将要被保存下来
			submitForm.setAnswer("muc#roomconfig_persistentroom", true);
			// 房间仅对成员开放
			submitForm.setAnswer("muc#roomconfig_membersonly", false);
			// 允许占有者邀请其他人
			submitForm.setAnswer("muc#roomconfig_allowinvites", true);
			// 能够发现占有者真实 JID 的角色
			// submitForm.setAnswer("muc#roomconfig_whois", "anyone");
			// 登录房间对话
			submitForm.setAnswer("muc#roomconfig_enablelogging", true);
			// 仅允许注册的昵称登录
			submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
			// 允许使用者修改昵称
			submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
			// 允许用户注册房间
			submitForm.setAnswer("x-muc#roomconfig_registration", false);
			// 发送已完成的表单（有默认值）到服务器来配置聊天室
			muc.sendConfigurationForm(submitForm);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	/**
	 * PacketListener 通过一个规定的过滤器提供一个机制来监听数据包
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
			System.out.println("消息格式:" + packet.toXML());
			Message message = (Message) packet;
			String from = message.getFrom();

			if (message.getBody() != null) {
				DelayInformation inf = (DelayInformation) message.getExtension(
						"x", "jabber:x:delay");
				System.out.println("判断消息");
				if (inf == null && count >= 1) {
					System.out.println("新消息来了");
					isHistory = true;
				} else {
					System.out.println("这是旧的消息");
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
	 * 聊天室成员的监听器
	 * 
	 * @author 廖乃波
	 * 
	 */
	class MyParticipantStatusListener implements ParticipantStatusListener {

		@Override
		public void adminGranted(String arg0) {
			Log.i(TAG, "执行了adminGranted方法:" + arg0);
		}

		@Override
		public void adminRevoked(String arg0) {
			Log.i(TAG, "执行了adminRevoked方法:" + arg0);
		}

		@Override
		public void banned(String arg0, String arg1, String arg2) {
			Log.i(TAG, "执行了banned方法:" + arg0);
		}

		@Override
		public void joined(String arg0) {
			Log.i(TAG, "执行了joined方法:" + arg0 + "加入了房间");
			getAllMember();
			android.os.Message msg = new android.os.Message();
			msg.what = MEMBER;
			handler.sendMessage(msg);
		}

		@Override
		public void kicked(String arg0, String arg1, String arg2) {
			Log.i(TAG, "执行了kicked方法:" + arg0 + "被踢出房间");
		}

		@Override
		public void left(String arg0) {
			String lefter = arg0.substring(arg0.indexOf("/") + 1);
			Log.i(TAG, "执行了left方法:" + lefter + "离开的房间");
			getAllMember();
			android.os.Message msg = new android.os.Message();
			msg.what = MEMBER;
			handler.sendMessage(msg);
		}

		@Override
		public void membershipGranted(String arg0) {
			Log.i(TAG, "执行了membershipGranted方法:" + arg0);
		}

		@Override
		public void membershipRevoked(String arg0) {
			Log.i(TAG, "执行了membershipRevoked方法:" + arg0);
		}

		@Override
		public void moderatorGranted(String arg0) {
			Log.i(TAG, "执行了moderatorGranted方法:" + arg0);
		}

		@Override
		public void moderatorRevoked(String arg0) {
			Log.i(TAG, "执行了moderatorRevoked方法:" + arg0);
		}

		@Override
		public void nicknameChanged(String arg0, String arg1) {
			Log.i(TAG, "执行了nicknameChanged方法:" + arg0);
		}

		@Override
		public void ownershipGranted(String arg0) {
			Log.i(TAG, "执行了ownershipGranted方法:" + arg0);
		}

		@Override
		public void ownershipRevoked(String arg0) {
			Log.i(TAG, "执行了ownershipRevoked方法:" + arg0);
		}

		@Override
		public void voiceGranted(String arg0) {
			Log.i(TAG, "执行了voiceGranted方法:" + arg0);
		}

		@Override
		public void voiceRevoked(String arg0) {
			Log.i(TAG, "执行了voiceRevoked方法:" + arg0);
		}
	}

	/**
	 * 
	 ****************************************** 
	 * @author 廖乃波 文件名称 : MyPacketListener.java 创建时间 : 2012-4-24 下午08:32:13 文件描述
	 ****************************************** 
	 */
	public class MyPacketListener implements PacketListener {

		@Override
		public void processPacket(Packet arg0) {
			// 线上--------------chat
			// 忙碌--------------dnd
			// 离开--------------away
			// 隐藏--------------xa
			Presence presence = (Presence) arg0;
			// PacketExtension pe = presence.getExtension("x",
			// "http://jabber.org/protocol/muc#user");
			String LogKineName = presence.getFrom().toString();
			String kineName = LogKineName
					.substring(LogKineName.indexOf("/") + 1);
			String stats = "";
			if ("chat".equals(presence.getMode().toString())) {
				stats = "[线上]";
			}
			if ("dnd".equals(presence.getMode().toString())) {
				stats = "[忙碌]";
			}
			if ("away".equals(presence.getMode().toString())) {
				stats = "[离开]";
			}
			if ("xa".equals(presence.getMode().toString())) {
				stats = "[隐藏]";
			}

			for (int i = 0; i < affiliates.size(); i++) {
				String name = affiliates.get(i);
				if (kineName.equals(name.substring(name.indexOf("]") + 1))) {
					affiliates.set(i, stats + kineName);
					System.out.println("状态改变成：" + affiliates.get(i));
					android.os.Message msg = new android.os.Message();
					msg.what = MEMBER;
					handler.sendMessage(msg);
					break;
				}
			}
		}
	}
}
