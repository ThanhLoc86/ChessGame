package com.chessgame.chessserver.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.chessgame.chessserver.game.GameSession;

/**
 * Service that manages multiple GameSession instances (rooms).
 * Thread-safe: uses ConcurrentHashMap for room storage and delegates concurrency around GameSession.
 */
@Service
public class GameManager {

	private static final Logger log = LoggerFactory.getLogger(GameManager.class);

	private final Map<String, GameSession> rooms = new ConcurrentHashMap<>();

	/**
	 * Create a new room and return its roomId.
	 */
	public String createRoom() {
		String roomId = UUID.randomUUID().toString().substring(0, 8);
		GameSession gs = new GameSession(roomId);
		rooms.put(roomId, gs);
		log.info("Created room {}", roomId);
		return roomId;
	}

	/**
	 * Join an existing room. Returns assigned color ("WHITE" or "BLACK").
	 * Throws IllegalArgumentException if room does not exist or is full.
	 */
	public String joinRoom(String roomId, WebSocketSession session) {
		GameSession gs = rooms.get(roomId);
		if (gs == null) throw new IllegalArgumentException("Room not found: " + roomId);
		String assigned = gs.addPlayer(session);
		if (assigned == null) throw new IllegalArgumentException("Room is full: " + roomId);
		log.info("Session {} joined room {} as {}", session.getId(), roomId, assigned);
		// initialize starting position when room becomes full
		if (gs.isFull()) {
			gs.getGame().getBoard().initializeStandardSetup();
		}
		return assigned;
	}

	/**
	 * Remove a room and return whether removal happened.
	 */
	public boolean removeRoom(String roomId) {
		boolean removed = rooms.remove(roomId) != null;
		if (removed) log.info("Removed room {}", roomId);
		return removed;
	}

	/**
	 * Lookup a GameSession by id (nullable).
	 */
	public GameSession getRoom(String roomId) {
		return rooms.get(roomId);
	}

	/**
	 * Find the game session that contains the provided WebSocketSession, or null if none.
	 */
	public GameSession findRoomBySession(org.springframework.web.socket.WebSocketSession session) {
		if (session == null) return null;
		for (GameSession gs : rooms.values()) {
			if (session.equals(gs.getPlayerWhite()) || session.equals(gs.getPlayerBlack())) return gs;
		}
		return null;
	}
}


