package com.slook.smack;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import com.slook.smack.service.MucService;
import com.slook.smack.utils.Constants;
import com.slook.smack.utils.NetUitl;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
	private static final String TAG = "LoginActivity";
	private ProgressDialog pd;
	private static final int CONECTION_SUCCESS = 10;
	private static final int FAEILDL = 20;

	private EditText et_UserName;
	private EditText et_PassWord;
	private String username;
	private String password;
	SharedPreferences sp = null;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case CONECTION_SUCCESS:
				// ȡ�����ȿ�
				pd.dismiss();
				// ����������
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
				break;
			case FAEILDL:

				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		sp = getSharedPreferences("history", Context.MODE_PRIVATE);
		et_PassWord = (EditText) this.findViewById(R.id.et_passWord);
		et_UserName = (EditText) this.findViewById(R.id.et_userName);

		String u = sp.getString("username", "");
		String p = sp.getString("password", "");
		et_PassWord.setText(p);
		et_UserName.setText(u);
		Button btn_login = (Button) this.findViewById(R.id.btn_login);
		// ���ȿ���ʾ
		pd = new ProgressDialog(this);
		pd.setTitle("��ʾ");
		pd.setMessage("���Ժ�...");
		btn_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pd.show();
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Editor editor = sp.edit();
							editor.putString("username", username);
							editor.putString("password", password);
							editor.commit();
							BuilderString(et_UserName.getText().toString());
							password = et_PassWord.getText().toString();
							System.out.println("�û���Ϣ: " + username + ":" + password);
							// ���ӷ�����
							Constants.conn = NetUitl.getConnection(
									Constants.SERVERDOMAIN, 5222);
							Constants.conn.login(username, password);
							
							VCard vCard = new VCard();
							vCard.load(Constants.conn);
							if("".equals(vCard.getNickName()) || null == vCard.getNickName()){
								System.out.println("�ǳ��ǿյ�");
								vCard.setNickName("���ֵ���ķè");
							}
							Constants.vCard = vCard;
							Log.i(TAG, "��½�ɹ�");
							// ������Ϣ֪ͨui����
							Message msg = new Message();
							msg.what = CONECTION_SUCCESS;
							handler.sendMessage(msg);
						} catch (XMPPException e) {
							e.printStackTrace();
							Log.i(TAG, "��½ʧ��");
						}
					}
				}).start();
			}
		});
	}

	/**
	 * ת���û����е�@����
	 * 
	 * @param user
	 */
	public void BuilderString(String user) {
		if (user.contains("@")) {
			String[] userArray = user.split("@");
			username = userArray[0].toString() + "\\40"
					+ userArray[1].toString();
		}
	}
}
