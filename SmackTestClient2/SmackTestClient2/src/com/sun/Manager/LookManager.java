package com.sun.Manager;

/**
 * �ܹ�����
 * @author liao
 *
 */
public class LookManager {

	public static RoomManager roomManager;//���������

	public static RoomManager getRoomManager() {

		if (roomManager == null) {

			roomManager = new RoomManager();
		}

		return roomManager;
	}

}
