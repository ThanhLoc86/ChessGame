package com.chessgame.chessserver.model;

/**
 * Server -> client state message. Contains a FEN string representing the current board.
 */
public class StateMessage {

	private String type;
	private String fen;

	public StateMessage() {}

	public StateMessage(String type, String fen) {
		this.type = type;
		this.fen = fen;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFen() {
		return fen;
	}

	public void setFen(String fen) {
		this.fen = fen;
	}
}


