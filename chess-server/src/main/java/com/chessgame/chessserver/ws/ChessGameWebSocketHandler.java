package com.chessgame.chessserver.ws;

import com.chessgame.chessserver.model.MoveMessage;
import com.chessgame.chessserver.model.StateMessage;
import com.chessgame.chessserver.service.GameManager;
import com.chessgame.chessserver.game.GameSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import chessengine.board.Board;
import chessengine.board.Square;
import chessengine.move.Move;
import chessengine.move.MoveType;
import chessengine.piece.Piece;
import chessengine.piece.PieceColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * WebSocket handler that parses incoming JSON messages and handles MOVE actions.
 * It delegates room lookup and game state to GameManager / GameSession.
 */
@Component
public class ChessGameWebSocketHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(ChessGameWebSocketHandler.class);
	private final ObjectMapper mapper = new ObjectMapper();
	private final GameManager gameManager;

	public ChessGameWebSocketHandler(GameManager gameManager) {
		this.gameManager = gameManager;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		log.info("WebSocket connection established: sessionId={}", session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		String payload = message.getPayload();
		try {
			JsonNode root = mapper.readTree(payload);
			String type = root.path("type").asText("");
			switch (type.toLowerCase()) {
				case "move" -> handleMoveMessage(session, mapper.treeToValue(root, MoveMessage.class));
				case "create" -> handleCreateMessage(session);
				case "join" -> {
					String roomId = root.path("roomId").asText(null);
					handleJoinMessage(session, roomId);
				}
				default -> log.info("Ignored message type={} from sessionId={}", type, session.getId());
			}
		} catch (Exception e) {
			log.warn("Failed to process message from {}: {}", session.getId(), e.getMessage());
			sendError(session, "invalid_message");
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		log.info("WebSocket connection closed: sessionId={} status={}", session.getId(), status);
	}

	private void handleMoveMessage(WebSocketSession session, MoveMessage mm) {
		GameSession gs = gameManager.findRoomBySession(session);
		if (gs == null) {
			sendError(session, "not_in_room");
			return;
		}

		// parse squares (support "r,c" or algebraic like "e2")
		int[] from = parseSquare(mm.getFrom());
		int[] to = parseSquare(mm.getTo());
		if (from == null || to == null) {
			sendError(session, "bad_square");
			return;
		}

		Move mv = new Move(from[0], from[1], to[0], to[1], MoveType.NORMAL);
		try {
			gs.applyMove(mv); // may throw IllegalArgumentException if illegal
			// build FEN and broadcast state
			String fen = buildFen(gs);
			StateMessage sm = new StateMessage("state", fen);
			String text = mapper.writeValueAsString(sm);
			sendToSession(gs.getPlayerWhite(), text);
			sendToSession(gs.getPlayerBlack(), text);
		} catch (IllegalArgumentException ex) {
			sendError(session, "illegal_move");
		} catch (Exception ex) {
			log.error("Error applying move: {}", ex.getMessage());
			sendError(session, "internal_error");
		}
	}

	private int[] parseSquare(String s) {
		if (s == null) return null;
		s = s.trim();
		// format "r,c"
		if (s.contains(",")) {
			String[] parts = s.split(",");
			if (parts.length != 2) return null;
			try {
				int r = Integer.parseInt(parts[0].trim());
				int c = Integer.parseInt(parts[1].trim());
				if (r < 0 || r >= Board.SIZE || c < 0 || c >= Board.SIZE) return null;
				return new int[] { r, c };
			} catch (NumberFormatException e) {
				return null;
			}
		}
		// algebraic like "e2"
		if (s.length() == 2) {
			char file = s.charAt(0);
			char rank = s.charAt(1);
			int col = file - 'a';
			int rk = rank - '0';
			if (col < 0 || col > 7 || rk < 1 || rk > 8) return null;
			int row = 8 - rk;
			return new int[] { row, col };
		}
		return null;
	}

	private String buildFen(GameSession gs) {
		var game = gs.getGame();
		var board = game.getBoard();
		StringBuilder sb = new StringBuilder();
		for (int r = 0; r < Board.SIZE; r++) {
			int empty = 0;
			for (int c = 0; c < Board.SIZE; c++) {
				Square sq = board.getSquare(r, c);
				Piece p = sq.getPiece();
				if (p == null) {
					empty++;
				} else {
					if (empty > 0) {
						sb.append(empty);
						empty = 0;
					}
					String code = pieceCode(p);
					if (p.getColor() == PieceColor.WHITE) {
						sb.append(code.toUpperCase());
					} else {
						sb.append(code.toLowerCase());
					}
				}
			}
			if (empty > 0) sb.append(empty);
			if (r < Board.SIZE - 1) sb.append('/');
		}
		// active color
		PieceColor active = gs.getActiveColor();
		sb.append(' ').append(active == PieceColor.WHITE ? 'w' : 'b');
		// minimal FEN fields for now
		sb.append(" - - 0 1");
		return sb.toString();
	}

	private String pieceCode(Piece p) {
		return switch (p.getClass().getSimpleName()) {
			case "King" -> "k";
			case "Queen" -> "q";
			case "Rook" -> "r";
			case "Bishop" -> "b";
			case "Knight" -> "n";
			case "Pawn" -> "p";
			default -> "x";
		};
	}

	private void handleCreateMessage(WebSocketSession session) {
		try {
			String roomId = gameManager.createRoom();
			String assigned = gameManager.joinRoom(roomId, session);
			// reply assigned color and roomId
			String text = mapper.writeValueAsString(java.util.Map.of("type", "assigned_color", "roomId", roomId, "color", assigned));
			sendToSession(session, text);
		} catch (Exception ex) {
			log.warn("Failed to create room for {}: {}", session.getId(), ex.getMessage());
			sendError(session, "create_failed");
		}
	}

	private void handleJoinMessage(WebSocketSession session, String roomId) {
		if (roomId == null || roomId.isBlank()) {
			sendError(session, "roomId_required");
			return;
		}
		try {
			String assigned = gameManager.joinRoom(roomId, session);
			String text = mapper.writeValueAsString(java.util.Map.of("type", "assigned_color", "roomId", roomId, "color", assigned));
			sendToSession(session, text);
			// if room is full now, broadcast initial state
			GameSession gs = gameManager.getRoom(roomId);
			if (gs != null && gs.isFull()) {
				String fen = buildFen(gs);
				String stateText = mapper.writeValueAsString(new StateMessage("state", fen));
				sendToSession(gs.getPlayerWhite(), stateText);
				sendToSession(gs.getPlayerBlack(), stateText);
			}
		} catch (IllegalArgumentException ex) {
			sendError(session, ex.getMessage());
		} catch (Exception ex) {
			log.warn("Failed to join room {} for {}: {}", roomId, session.getId(), ex.getMessage());
			sendError(session, "join_failed");
		}
	}

	private void sendToSession(WebSocketSession s, String text) {
		if (s == null || !s.isOpen()) return;
		try {
			s.sendMessage(new TextMessage(text));
		} catch (IOException e) {
			log.warn("Failed to send message to {}: {}", s.getId(), e.getMessage());
		}
	}

	private void sendError(WebSocketSession session, String code) {
		try {
			String text = mapper.writeValueAsString(new StateMessage("error", code));
			session.sendMessage(new TextMessage(text));
		} catch (IOException e) {
			log.warn("Failed to send error to {}: {}", session.getId(), e.getMessage());
		}
	}
}


