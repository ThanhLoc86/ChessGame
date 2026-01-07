package com.chessgame.chessserver.model;

/**
 * Message representing a move.
 * - type: message type identifier (e.g., "move")
 * - from: algebraic or coordinate string identifying origin (e.g., "e2" or
 * "6,4")
 * - to: target square
 */
public class MoveMessage {
	private String type;
	private String from;
	private String to;
	private String promotionPiece;

	public MoveMessage() {
	}

	public MoveMessage(String type, String from, String to) {
		this.type = type;
		this.from = from;
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getPromotionPiece() {
		return promotionPiece;
	}

	public void setPromotionPiece(String promotionPiece) {
		this.promotionPiece = promotionPiece;
	}
}
