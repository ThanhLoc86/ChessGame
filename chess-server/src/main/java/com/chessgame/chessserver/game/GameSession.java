package com.chessgame.chessserver.game;

import com.chessgame.chessserver.domain.entity.NguoiDung;
import java.time.LocalDateTime;

/**
 * Represents the canonical (type-safe) state of a finished or ongoing game.
 * This class does NOT depend on WebSocket, repositories or IO. It only holds game state.
 */
public class GameSession {

	private final String roomId;
	private NguoiDung whitePlayer;
	private NguoiDung blackPlayer;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private GameResult result;

	public GameSession(String roomId) {
		this.roomId = roomId;
	}

	public String getRoomId() {
		return roomId;
	}

	public NguoiDung getWhitePlayer() {
		return whitePlayer;
	}

	public void setWhitePlayer(NguoiDung whitePlayer) {
		this.whitePlayer = whitePlayer;
	}

	public NguoiDung getBlackPlayer() {
		return blackPlayer;
	}

	public void setBlackPlayer(NguoiDung blackPlayer) {
		this.blackPlayer = blackPlayer;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public GameResult getResult() {
		return result;
	}

	public void setResult(GameResult result) {
		this.result = result;
	}

	public boolean isGameOver() {
		return this.result != null;
	}
}


