package com.slook.smack;

import org.jivesoftware.smack.packet.Message;

/**
 * @author liao
 * @date 2012-4-15
 * @version 1.0
 */
/*
 * version����OnContactStateListener��һ���¼��ӿڣ��������£�
 */
public interface OnMessageListener {
	/*
	 * @return ������յ�����Ϣ
	 */
	public void processMessage(Message message);
}
