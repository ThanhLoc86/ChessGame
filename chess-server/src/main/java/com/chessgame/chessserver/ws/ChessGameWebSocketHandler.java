package com.chessgame.chessserver.ws;

import com.chessgame.chessserver.model.ChatMessage;
import com.chessgame.chessserver.model.MoveMessage;
import com.chessgame.chessserver.model.StateMessage;
import com.chessgame.chessserver.service.GameManager;
import com.chessgame.chessserver.service.GameResultService;
import com.chessgame.chessserver.game.GameResult;
import java.time.LocalDateTime;
import com.chessgame.chessserver.game.GameRoom;
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
 * WebSocket handler that parses incoming JSON messages and handles MOVE
 * actions.
 * It delegates transport and engine operations to GameManager / GameRoom.
 * GameSession is used only for domain data (players, result) and game-over
 * checks.
 */
@Component
public class ChessGameWebSocketHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(ChessGameWebSocketHandler.class);
	private final ObjectMapper mapper = new ObjectMapper();
	private final GameManager gameManager;
	private final GameResultService gameResultService;

	public ChessGameWebSocketHandler(GameManager gameManager, GameResultService gameResultService) {
		this.gameManager = gameManager;
		this.gameResultService = gameResultService;
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
				case "create_bot" -> handleCreateBotMessage(session);
				case "join" -> {
					String roomId = root.path("roomId").asText(null);
					handleJoinMessage(session, roomId);
				}
				case "chat" -> handleChatMessage(session, root);
				case "resign" -> handleResign(session);
				case "draw_offer" -> handleDrawAction(session, "offer");
				case "draw_accept" -> handleDrawAction(session, "accept");
				case "draw_decline" -> handleDrawAction(session, "decline");
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
		GameRoom gr = gameManager.findGameRoomBySession(session);
		if (gr != null) {
			gr.removePlayer(session);
			// Notify other player if game is not over
			var gs = gr.getGameSession();
			if (gs != null && !gs.isGameOver()) {
				try {
					String msg = mapper.writeValueAsString(java.util.Map.of(
							"type", "chat",
							"sender", "Hệ thống",
							"text", "Đối thủ đã mất kết nối."));
					gr.sendToBoth(msg);
				} catch (Exception ignored) {
				}
			}
			// If both players gone, room could be cleaned up (future optimization)
		}
	}

	private void handleMoveMessage(WebSocketSession session, MoveMessage mm) {
		GameRoom gr = gameManager.findGameRoomBySession(session);
		if (gr == null) {
			sendError(session, "not_in_room");
			return;
		}

		var gs = gr.getGameSession();
		if (gs != null && gs.isGameOver()) {
			sendError(session, "game_over");
			return;
		}

		int[] from = parseSquare(mm.getFrom());
		int[] to = parseSquare(mm.getTo());
		if (from == null || to == null) {
			sendError(session, "bad_square");
			return;
		}

		// determine move from legal moves
		PieceColor colorToMove = gr.getActiveColor();

		// Verify this session is the active player
		boolean isWhiteTurn = colorToMove == PieceColor.WHITE;
		WebSocketSession activeSession = isWhiteTurn ? gr.getPlayerWhiteSession() : gr.getPlayerBlackSession();
		if (activeSession == null || !activeSession.equals(session)) {
			sendError(session, "not_your_turn");
			return;
		}

		java.util.List<Move> legal = gr.getEngine().legalMovesForColor(colorToMove);
		Move matched = null;

		for (Move m : legal) {
			if (m.getFromRow() == from[0] && m.getFromCol() == from[1] &&
					m.getToRow() == to[0] && m.getToCol() == to[1]) {

				if (m.getType() == MoveType.PROMOTION) {
					if (mm.getPromotionPiece() != null) {
						String promoCode = mm.getPromotionPiece().toUpperCase();
						chessengine.piece.PieceType pt = switch (promoCode) {
							case "QUEEN", "Q" -> chessengine.piece.PieceType.QUEEN;
							case "ROOK", "R" -> chessengine.piece.PieceType.ROOK;
							case "BISHOP", "B" -> chessengine.piece.PieceType.BISHOP;
							case "KNIGHT", "N" -> chessengine.piece.PieceType.KNIGHT;
							default -> null;
						};
						if (pt != null && pt.equals(m.getPromotionPiece())) {
							matched = m;
							break;
						}
					}
				} else {
					matched = m;
					break;
				}
			}
		}

		if (matched == null) {
			sendError(session, "illegal_move");
			return;
		}

		try {
			gr.applyEngineMove(matched);
			broadcastState(gr);

			// Check game over
			if (checkGameOver(gr))
				return;

			// Trigger bot move if applicable
			if (gr.isVsBot()) {
				triggerBotMove(gr);
			}
		} catch (Exception ex) {
			log.error("Error applying move: {}", ex.getMessage());
			sendError(session, "internal_error");
		}
	}

	private boolean checkGameOver(GameRoom gr) throws Exception {
		PieceColor nextColor = gr.getActiveColor();
		GameSession gs = gr.getGameSession();
		if (gr.getEngine().isCheckmate(nextColor)) {
			GameResult outcome = (nextColor == PieceColor.WHITE) ? GameResult.BLACK_WIN : GameResult.WHITE_WIN;
			if (gs != null) {
				gs.setResult(outcome);
				gs.setEndTime(LocalDateTime.now());
				if (!gr.isVsBot()) {
					gameResultService.saveResult(gs);
				}
			}
			try {
				String winner = (outcome == GameResult.WHITE_WIN) ? "Trắng" : "Đen";
				String msg = mapper.writeValueAsString(java.util.Map.of(
						"type", "chat",
						"sender", "Hệ thống",
						"text", "Trò chơi kết thúc. " + winner + " thắng!"));
				gr.sendToBoth(msg);
			} catch (Exception ignored) {
			}
			String gm = mapper.writeValueAsString(java.util.Map.of("type", "game_over", "result", outcome.name()));
			gr.sendToBoth(gm);
			return true;
		} else if (gr.getEngine().isStalemate(nextColor)) {
			if (gs != null) {
				gs.setResult(GameResult.DRAW);
				gs.setEndTime(LocalDateTime.now());
				if (!gr.isVsBot()) {
					gameResultService.saveResult(gs);
				}
			}
			try {
				String msg = mapper.writeValueAsString(java.util.Map.of(
						"type", "chat",
						"sender", "Hệ thống",
						"text", "Hòa! Trận đấu kết thúc."));
				gr.sendToBoth(msg);
			} catch (Exception ignored) {
			}
			String gm = mapper.writeValueAsString(java.util.Map.of("type", "game_over", "result", "DRAW"));
			gr.sendToBoth(gm);
			return true;
		}
		return false;
	}

	private void triggerBotMove(GameRoom gr) {
		// Run in separate thread to not block websocket
		new Thread(() -> {
			try {
				Thread.sleep(800); // simulated thinking time

				// Check if game is still active before bot moves (user might have resigned)
				var gs = gr.getGameSession();
				if (gs != null && gs.isGameOver())
					return;

				chessengine.ai.MinimaxBot bot = new chessengine.ai.MinimaxBot(gr.getActiveColor(), 2);
				Move m = bot.findBestMove(gr.getEngine());
				if (m != null) {
					// Check again right before applying
					if (gs != null && gs.isGameOver())
						return;

					gr.applyEngineMove(m);
					broadcastState(gr);
					checkGameOver(gr);
				}
			} catch (Exception e) {
				log.warn("Bot move failed: {}", e.getMessage());
			}
		}).start();
	}

	private void handleCreateBotMessage(WebSocketSession session) {
		try {
			String roomId = gameManager.createRoom();
			GameRoom gr = gameManager.getGameRoom(roomId);
			gr.setVsBot(true);
			String assigned = gr.addPlayer(session); // User is White
			gr.getBoard().initializeStandardSetup();

			// Set bot as Black in GameSession domain if desired
			var gs = gr.getGameSession();
			gs.setStartTime(LocalDateTime.now());

			sendToSession(session, mapper.writeValueAsString(
					java.util.Map.of("type", "assigned_color", "roomId", roomId, "color", assigned)));
			broadcastState(gr);
		} catch (Exception e) {
			sendError(session, "create_bot_failed");
		}
	}

	private void handleResign(WebSocketSession session) {
		GameRoom gr = gameManager.findGameRoomBySession(session);
		if (gr == null)
			return;
		var gs = gr.getGameSession();
		if (gs == null || gs.isGameOver())
			return;

		PieceColor resigningColor;
		if (session.equals(gr.getPlayerWhiteSession())) {
			resigningColor = PieceColor.WHITE;
		} else if (session.equals(gr.getPlayerBlackSession())) {
			resigningColor = PieceColor.BLACK;
		} else {
			return;
		}

		GameResult outcome = (resigningColor == PieceColor.WHITE) ? GameResult.BLACK_WIN : GameResult.WHITE_WIN;

		gs.setResult(outcome);
		gs.setEndTime(LocalDateTime.now());
		if (!gr.isVsBot()) {
			gameResultService.saveResult(gs);
		}

		try {
			String resignedStr = (resigningColor == PieceColor.WHITE) ? "Trắng" : "Đen";
			String winnerStr = (resigningColor == PieceColor.WHITE) ? "Đen" : "Trắng";
			String chatMsg = mapper.writeValueAsString(java.util.Map.of(
					"type", "chat",
					"sender", "Hệ thống",
					"text", resignedStr + " đã đầu hàng. " + winnerStr + " thắng!"));
			gr.sendToBoth(chatMsg);

			String gm = mapper.writeValueAsString(
					java.util.Map.of(
							"type", "game_over",
							"result", outcome.name(),
							"reason", "RESIGNATION",
							"resignedColor", resigningColor.name()));
			gr.sendToBoth(gm);
		} catch (Exception ignored) {
		}
	}

	private void handleDrawAction(WebSocketSession session, String action) {
		GameRoom gr = gameManager.findGameRoomBySession(session);
		if (gr == null)
			return;
		var gs = gr.getGameSession();
		if (gs == null || gs.isGameOver())
			return;

		WebSocketSession other;
		if (session.equals(gr.getPlayerWhiteSession())) {
			other = gr.getPlayerBlackSession();
		} else if (session.equals(gr.getPlayerBlackSession())) {
			other = gr.getPlayerWhiteSession();
		} else {
			return;
		}

		if (other == null)
			return;

		try {
			if ("offer".equals(action)) {
				sendToSession(other, mapper.writeValueAsString(java.util.Map.of("type", "draw_offer")));
			} else if ("accept".equals(action)) {
				gs.setResult(GameResult.DRAW);
				gs.setEndTime(LocalDateTime.now());
				if (!gr.isVsBot()) {
					gameResultService.saveResult(gs);
				}
				gr.sendToBoth(mapper.writeValueAsString(
						java.util.Map.of("type", "game_over", "result", "DRAW", "reason", "AGREEMENT")));
			} else if ("decline".equals(action)) {
				sendToSession(other, mapper.writeValueAsString(java.util.Map.of("type", "draw_decline")));
			}
		} catch (Exception ignored) {
		}
	}

	private void broadcastState(GameRoom gr) {
		try {
			String fen = buildFen(gr);
			StateMessage sm = new StateMessage("state", fen);
			var gs = gr.getGameSession();
			if (gs != null) {
				if (gs.getWhitePlayer() != null) {
					sm.setWhiteName(gs.getWhitePlayer().getTenDangNhap());
					sm.setWhiteElo(gs.getWhitePlayer().getDiemElo());
				} else if (gr.isVsBot()) {
					sm.setWhiteName("Minimax Bot");
					sm.setWhiteElo(1500);
				}

				if (gs.getBlackPlayer() != null) {
					sm.setBlackName(gs.getBlackPlayer().getTenDangNhap());
					sm.setBlackElo(gs.getBlackPlayer().getDiemElo());
				} else if (gr.isVsBot()) {
					sm.setBlackName("Minimax Bot");
					sm.setBlackElo(1500);
				}
			}
			sm.setWhiteTime(gr.getWhiteTimeMs());
			sm.setBlackTime(gr.getBlackTimeMs());
			String text = mapper.writeValueAsString(sm);
			gr.sendToBoth(text);
		} catch (Exception e) {
			log.warn("Failed to broadcast state: {}", e.getMessage());
		}
	}

	private void handleChatMessage(WebSocketSession session, JsonNode root) {
		GameRoom gr = gameManager.findGameRoomBySession(session);
		if (gr == null)
			return;
		Object userAttr = session.getAttributes().get("user");
		if (!(userAttr instanceof com.chessgame.chessserver.domain.entity.NguoiDung))
			return;
		com.chessgame.chessserver.domain.entity.NguoiDung nd = (com.chessgame.chessserver.domain.entity.NguoiDung) userAttr;

		String text = root.path("text").asText("");
		if (text.isBlank())
			return;

		try {
			ChatMessage cm = new ChatMessage(nd.getTenDangNhap(), text);
			gr.sendToBoth(mapper.writeValueAsString(cm));
		} catch (Exception e) {
			log.warn("Failed to send chat message: {}", e.getMessage());
		}
	}

	private int[] parseSquare(String s) {
		if (s == null)
			return null;
		s = s.trim();
		// format "r,c"
		if (s.contains(",")) {
			String[] parts = s.split(",");
			if (parts.length != 2)
				return null;
			try {
				int r = Integer.parseInt(parts[0].trim());
				int c = Integer.parseInt(parts[1].trim());
				if (r < 0 || r >= Board.SIZE || c < 0 || c >= Board.SIZE)
					return null;
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
			if (col < 0 || col > 7 || rk < 1 || rk > 8)
				return null;
			int row = 8 - rk;
			return new int[] { row, col };
		}
		return null;
	}

	private String buildFen(GameRoom gr) {
		var board = gr.getBoard();
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
			if (empty > 0)
				sb.append(empty);
			if (r < Board.SIZE - 1)
				sb.append('/');
		}
		// active color
		PieceColor active = gr.getActiveColor();
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
			Object userAttr = session.getAttributes().get("user");
			if (!(userAttr instanceof com.chessgame.chessserver.domain.entity.NguoiDung)) {
				sendError(session, "unauthorized");
				return;
			}
			com.chessgame.chessserver.domain.entity.NguoiDung nd = (com.chessgame.chessserver.domain.entity.NguoiDung) userAttr;
			String roomId = gameManager.createRoom();
			String assigned = gameManager.joinRoom(roomId, session);
			// reply assigned color and roomId
			String text = mapper.writeValueAsString(
					java.util.Map.of("type", "assigned_color", "roomId", roomId, "color", assigned));
			sendToSession(session, text);
			// ensure GameSession domain has player set
			var gr = gameManager.getGameRoom(roomId);
			if (gr != null) {
				var gs = gr.getGameSession();
				if ("WHITE".equalsIgnoreCase(assigned)) {
					if (gs.getWhitePlayer() == null)
						gs.setWhitePlayer(nd);
				} else if ("BLACK".equalsIgnoreCase(assigned)) {
					if (gs.getBlackPlayer() == null)
						gs.setBlackPlayer(nd);
				}
				if (gs.getWhitePlayer() != null && gs.getBlackPlayer() != null && gs.getStartTime() == null) {
					gs.setStartTime(LocalDateTime.now());
				}
			}
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
			Object userAttr = session.getAttributes().get("user");
			if (!(userAttr instanceof com.chessgame.chessserver.domain.entity.NguoiDung)) {
				sendError(session, "unauthorized");
				return;
			}
			com.chessgame.chessserver.domain.entity.NguoiDung nd = (com.chessgame.chessserver.domain.entity.NguoiDung) userAttr;
			String assigned = gameManager.joinRoom(roomId, session);
			String text = mapper.writeValueAsString(
					java.util.Map.of("type", "assigned_color", "roomId", roomId, "color", assigned));
			sendToSession(session, text);
			// if room is full now, broadcast initial state
			GameRoom gr = gameManager.getGameRoom(roomId);
			// ensure GameSession domain has player set
			if (gr != null) {
				var gs = gr.getGameSession();
				if ("WHITE".equalsIgnoreCase(assigned)) {
					if (gs.getWhitePlayer() == null)
						gs.setWhitePlayer(nd);
				} else if ("BLACK".equalsIgnoreCase(assigned)) {
					if (gs.getBlackPlayer() == null)
						gs.setBlackPlayer(nd);
				}
				if (gs.getWhitePlayer() != null && gs.getBlackPlayer() != null && gs.getStartTime() == null) {
					gs.setStartTime(LocalDateTime.now());
				}
				if (gr.isFull()) {
					broadcastState(gr);
				}
			}
		} catch (IllegalArgumentException ex) {
			sendError(session, ex.getMessage());
		} catch (Exception ex) {
			log.warn("Failed to join room {} for {}: {}", roomId, session.getId(), ex.getMessage());
			sendError(session, "join_failed");
		}
	}

	private void sendToSession(WebSocketSession s, String text) {
		if (s == null || !s.isOpen())
			return;
		try {
			s.sendMessage(new TextMessage(text));
		} catch (IOException e) {
			log.warn("Failed to send message to {}: {}", s.getId(), e.getMessage());
		}
	}

	private void sendError(WebSocketSession session, String code) {
		try {
			StateMessage sm = new StateMessage();
			sm.setType("error");
			sm.setCode(code);
			String text = mapper.writeValueAsString(sm);
			session.sendMessage(new TextMessage(text));
		} catch (IOException e) {
			log.warn("Failed to send error to {}: {}", session.getId(), e.getMessage());
		}
	}
}
