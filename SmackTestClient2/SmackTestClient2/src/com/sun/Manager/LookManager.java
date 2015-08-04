package com.sun.Manager;

/**
 * 总管理类
 * @author liao
 *
 */
public class LookManager {

	public static RoomManager roomManager;//房间管理类

	public static RoomManager getRoomManager() {

		if (roomManager == null) {

			roomManager = new RoomManager();
		}

		return roomManager;
	}

}
