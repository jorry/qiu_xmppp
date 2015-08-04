package com.slook.smack.service;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.slook.smack.ActivityMultiRoom;
import com.slook.smack.utils.Constants;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MucService extends Service {

	protected static final String TAG = "MucService";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 这里注册监听事件，监听被邀请、用户状态、聊天室成员装、成员列表变动
		setInviterListener();
	}

	/**
	 * 被邀请监听
	 */
	private void setInviterListener() {
		Log.i(TAG, "创建的服务监听");
		MultiUserChat.addInvitationListener(Constants.conn,
				new InvitationListener() {
					// 对应参数：连接、 房间JID、房间名、附带内容、密码、消息
					@Override
					public void invitationReceived(Connection conn,
							String room, String inviter, String reason,
							String password, Message message) {

						Log.i(TAG, "收到来自 " + inviter + " 的聊天室邀请。邀请附带内容："
								+ reason);

						Intent intent = new Intent(MucService.this,
								ActivityMultiRoom.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("jid", room);
						intent.putExtra("action", "join");
						startActivity(intent);
					}
				});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 销毁所有
	}
}
