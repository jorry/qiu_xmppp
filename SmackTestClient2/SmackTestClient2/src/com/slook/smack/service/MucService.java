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
		// ����ע������¼������������롢�û�״̬�������ҳ�Աװ����Ա�б�䶯
		setInviterListener();
	}

	/**
	 * ���������
	 */
	private void setInviterListener() {
		Log.i(TAG, "�����ķ������");
		MultiUserChat.addInvitationListener(Constants.conn,
				new InvitationListener() {
					// ��Ӧ���������ӡ� ����JID�����������������ݡ����롢��Ϣ
					@Override
					public void invitationReceived(Connection conn,
							String room, String inviter, String reason,
							String password, Message message) {

						Log.i(TAG, "�յ����� " + inviter + " �����������롣���븽�����ݣ�"
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
		// ��������
	}
}
