package com.chessgame.chessserver.model;

/**
 * Message sent by client to join a room.
 * Fields are simple POJO properties to be serialized as JSON.
 */
public class JoinMessage {

	private String type;
	private String roomId;

	public JoinMessage() {}

	public JoinMessage(String type, String roomId) {
		this.type = type;
		this.roomId = roomId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
}


