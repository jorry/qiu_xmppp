package com.slook.smack;

import org.jivesoftware.smack.packet.Message;

/**
 * @author liao
 * @date 2012-4-15
 * @version 1.0
 */
/*
 * version其中OnContactStateListener是一个事件接口，代码如下：
 */
public interface OnMessageListener {
	/*
	 * @return 处理接收到的消息
	 */
	public void processMessage(Message message);
}
