package com.sun.Manager;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smackx.muc.MultiUserChat;

public class RoomManager {
	/**房间集合*/
	public Map<String, MultiUserChat> rooms = new HashMap<String, MultiUserChat>();

}
