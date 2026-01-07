package com.chessgame.chessserver.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import java.time.LocalDateTime;

import com.chessgame.chessserver.domain.entity.NguoiDung;

import com.chessgame.chessserver.game.GameRoom;
import com.chessgame.chessserver.game.GameSession;

/**
 * Service that manages multiple GameSession instances (rooms).
 * Thread-safe: uses ConcurrentHashMap for room storage and delegates
 * concurrency around GameSession.
 */
@Service
public class GameManager {

	private static final Logger log = LoggerFactory.getLogger(GameManager.class);

	private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

	/**
	 * Create a new room and return its roomId.
	 */
	public String createRoom() {
		String roomId = UUID.randomUUID().toString().substring(0, 8);
		GameRoom gr = new GameRoom(roomId);
		GameSession gs = new GameSession(roomId);
		gr.setGameSession(gs);
		rooms.put(roomId, gr);
		log.info("Created room {}", roomId);
		return roomId;
	}

	/**
	 * Join an existing room. Returns assigned color ("WHITE" or "BLACK").
	 * Throws IllegalArgumentException if room does not exist or is full.
	 */
	public String joinRoom(String roomId, WebSocketSession session) {
		GameRoom gr = rooms.get(roomId);
		if (gr == null)
			throw new IllegalArgumentException("Room not found: " + roomId);

		// Re-join check
		Object userAttr = session.getAttributes().get("user");
		if (userAttr instanceof NguoiDung) {
			NguoiDung nd = (NguoiDung) userAttr;
			var gs = gr.getGameSession();
			if (gs != null) {
				if (gs.getWhitePlayer() != null && gs.getWhitePlayer().getId().equals(nd.getId())) {
					gr.setPlayerWhiteSession(session);
					return "WHITE";
				}
				if (gs.getBlackPlayer() != null && gs.getBlackPlayer().getId().equals(nd.getId())) {
					gr.setPlayerBlackSession(session);
					return "BLACK";
				}
			}
		}

		String assigned = gr.addPlayer(session);
		if (assigned == null)
			throw new IllegalArgumentException("Phòng đã đầy: " + roomId);
		log.info("Session {} joined room {} as {}", session.getId(), roomId, assigned);
		// initialize starting position when room becomes full
		if (gr.isFull()) {
			// initialize engine board for this room
			try {
				gr.getBoard().initializeStandardSetup();
			} catch (Exception ignored) {
			}
		}
		// Bind authenticated domain user (if present) into canonical GameSession
		if (userAttr instanceof NguoiDung) {
			NguoiDung nd = (NguoiDung) userAttr;
			var gs = gr.getGameSession();
			if ("WHITE".equalsIgnoreCase(assigned)) {
				if (gs.getWhitePlayer() == null)
					gs.setWhitePlayer(nd);
			} else if ("BLACK".equalsIgnoreCase(assigned)) {
				if (gs.getBlackPlayer() == null)
					gs.setBlackPlayer(nd);
			}
			// when both players present, set startTime if not already set
			if (gs.getWhitePlayer() != null && gs.getBlackPlayer() != null && gs.getStartTime() == null) {
				gs.setStartTime(LocalDateTime.now());
			}
		} else {
			log.debug("No authenticated user found in session attributes for sessionId={}", session.getId());
		}
		return assigned;
	}

	/**
	 * Remove a room and return whether removal happened.
	 */
	public boolean removeRoom(String roomId) {
		boolean removed = rooms.remove(roomId) != null;
		if (removed)
			log.info("Removed room {}", roomId);
		return removed;
	}

	/**
	 * Lookup a GameSession by id (nullable).
	 */
	public GameSession getRoom(String roomId) {
		GameRoom gr = rooms.get(roomId);
		return gr == null ? null : gr.getGameSession();
	}

	/**
	 * Find the game session that contains the provided WebSocketSession, or null if
	 * none.
	 */
	public GameSession findRoomBySession(org.springframework.web.socket.WebSocketSession session) {
		GameRoom gr = findGameRoomBySession(session);
		return gr == null ? null : gr.getGameSession();
	}

	public GameRoom getGameRoom(String roomId) {
		return rooms.get(roomId);
	}

	public GameRoom findGameRoomBySession(org.springframework.web.socket.WebSocketSession session) {
		if (session == null)
			return null;
		for (GameRoom gr : rooms.values()) {
			if (session.equals(gr.getPlayerWhiteSession()) || session.equals(gr.getPlayerBlackSession()))
				return gr;
		}
		return null;
	}
}
