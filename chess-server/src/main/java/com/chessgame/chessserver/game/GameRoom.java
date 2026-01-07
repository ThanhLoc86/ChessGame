package com.chessgame.chessserver.game;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import chessengine.game.Game;
import chessengine.move.Move;
import chessengine.piece.PieceColor;
import java.io.IOException;

/**
 * Lightweight container that ties a WebSocket-backed connection (two sessions)
 * to a canonical GameSession (game state).
 */
public class GameRoom {

	private final String roomId;
	private GameSession gameSession;
	private volatile WebSocketSession playerWhiteSession;
	private volatile WebSocketSession playerBlackSession;
	private final Game engine;
	private int moveCount = 0;
	private long whiteTimeMs = 600000; // 10 mins default
	private long blackTimeMs = 600000;
	private long lastMoveTime = 0;
	private boolean vsBot = false;

	public GameRoom(String roomId) {
		this.roomId = roomId;
		this.engine = new Game();
	}

	public String getRoomId() {
		return roomId;
	}

	public GameSession getGameSession() {
		return gameSession;
	}

	public void setGameSession(GameSession gs) {
		this.gameSession = gs;
	}

	public synchronized String addPlayer(WebSocketSession session) {
		if (playerWhiteSession == null) {
			playerWhiteSession = session;
			return "WHITE";
		}
		if (playerBlackSession == null) {
			playerBlackSession = session;
			return "BLACK";
		}
		return null;
	}

	public synchronized void removePlayer(WebSocketSession session) {
		if (session == null)
			return;
		if (session.equals(playerWhiteSession))
			playerWhiteSession = null;
		if (session.equals(playerBlackSession))
			playerBlackSession = null;
	}

	public boolean isFull() {
		return playerWhiteSession != null && playerBlackSession != null;
	}

	public WebSocketSession getPlayerWhiteSession() {
		return playerWhiteSession;
	}

	public WebSocketSession getPlayerBlackSession() {
		return playerBlackSession;
	}

	public void setPlayerWhiteSession(WebSocketSession s) {
		this.playerWhiteSession = s;
	}

	public void setPlayerBlackSession(WebSocketSession s) {
		this.playerBlackSession = s;
	}

	public Game getEngine() {
		return engine;
	}

	public synchronized void applyEngineMove(Move move) {
		long now = System.currentTimeMillis();
		if (lastMoveTime > 0) {
			long elapsed = now - lastMoveTime;
			if (engine.getActiveColor() == PieceColor.WHITE) {
				whiteTimeMs -= elapsed;
			} else {
				blackTimeMs -= elapsed;
			}
		}
		lastMoveTime = now;
		engine.applyMove(move);
		moveCount++;
	}

	public int getMoveCount() {
		return moveCount;
	}

	public chessengine.board.Board getBoard() {
		return engine.getBoard();
	}

	public PieceColor getActiveColor() {
		return engine.getActiveColor();
	}

	public void sendToBoth(String text) {
		sendToSession(playerWhiteSession, text);
		sendToSession(playerBlackSession, text);
	}

	private void sendToSession(WebSocketSession s, String text) {
		if (s == null || !s.isOpen())
			return;
		try {
			s.sendMessage(new TextMessage(text));
		} catch (IOException e) {
			// ignore
		}
	}

	public long getWhiteTimeMs() {
		return whiteTimeMs;
	}

	public long getBlackTimeMs() {
		return blackTimeMs;
	}

	public boolean isVsBot() {
		return vsBot;
	}

	public void setVsBot(boolean vsBot) {
		this.vsBot = vsBot;
	}
}
