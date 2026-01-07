package com.chessgame.chessserver.model;

/**
 * Server -> client state message. Contains a FEN string representing the
 * current board.
 */
public class StateMessage {
	private String type;
	private String fen;
	private String whiteName;
	private Integer whiteElo;
	private String blackName;
	private Integer blackElo;
	private Long whiteTime; // ms remaining
	private Long blackTime; // ms remaining
	private String code;

	public StateMessage() {
	}

	public StateMessage(String type, String fen) {
		this.type = type;
		this.fen = fen;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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

	public String getWhiteName() {
		return whiteName;
	}

	public void setWhiteName(String whiteName) {
		this.whiteName = whiteName;
	}

	public Integer getWhiteElo() {
		return whiteElo;
	}

	public void setWhiteElo(Integer whiteElo) {
		this.whiteElo = whiteElo;
	}

	public String getBlackName() {
		return blackName;
	}

	public void setBlackName(String blackName) {
		this.blackName = blackName;
	}

	public Integer getBlackElo() {
		return blackElo;
	}

	public void setBlackElo(Integer blackElo) {
		this.blackElo = blackElo;
	}

	public Long getWhiteTime() {
		return whiteTime;
	}

	public void setWhiteTime(Long whiteTime) {
		this.whiteTime = whiteTime;
	}

	public Long getBlackTime() {
		return blackTime;
	}

	public void setBlackTime(Long blackTime) {
		this.blackTime = blackTime;
	}
}
