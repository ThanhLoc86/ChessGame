package com.chessgame.chessserver.game;

import org.springframework.web.socket.WebSocketSession;
import chessengine.game.Game;
import chessengine.piece.PieceColor;
import chessengine.move.Move;

/**
 * Represents a single chess match (room).
 * - Holds the room id and references to the two player sessions (white / black).
 * - Owns an instance of the chessengine Game which contains board state and move validation.
 *
 * Note: This class does NOT perform WebSocket I/O. Sessions are stored as references so higher-level
 * services can route messages to the correct participants.
 */
public class GameSession {

	private final String roomId;
	private volatile WebSocketSession playerWhite;
	private volatile WebSocketSession playerBlack;
	private final Game game;
	private int moveCount = 0;

	/**
	 * Create a new GameSession with a fresh chessengine.Game.
	 */
	public GameSession(String roomId) {
		this.roomId = roomId;
		this.game = new Game();
	}

	public String getRoomId() {
		return roomId;
	}

	public Game getGame() {
		return game;
	}

	/**
	 * Add a player to the session.
	 * If white seat is empty the player is assigned white, otherwise assigned black if available.
	 * Returns the assigned color as "WHITE" or "BLACK", or null if the room is already full.
	 */
	public synchronized String addPlayer(WebSocketSession session) {
		if (playerWhite == null) {
			playerWhite = session;
			return "WHITE";
		}
		if (playerBlack == null) {
			playerBlack = session;
			return "BLACK";
		}
		return null;
	}

	/**
	 * Remove a player reference if it matches the provided session.
	 */
	public synchronized void removePlayer(WebSocketSession session) {
		if (session == null) return;
		if (session.equals(playerWhite)) playerWhite = null;
		if (session.equals(playerBlack)) playerBlack = null;
	}

	/**
	 * Returns true when both player slots are occupied.
	 */
	public synchronized boolean isFull() {
		return playerWhite != null && playerBlack != null;
	}

	/**
	 * Apply a move using the chess-engine Game.
	 * The chessengine is authoritative: it will throw IllegalArgumentException for illegal moves.
	 */
	public synchronized void applyMove(Move move) {
		game.applyMove(move);
		moveCount++;
	}

	/**
	 * Return the active color to move according to internal move count.
	 * Starts with WHITE to move (moveCount == 0).
	 */
	public synchronized PieceColor getActiveColor() {
		return (moveCount % 2 == 0) ? PieceColor.WHITE : PieceColor.BLACK;
	}

	public WebSocketSession getPlayerWhite() {
		return playerWhite;
	}

	public WebSocketSession getPlayerBlack() {
		return playerBlack;
	}
}


