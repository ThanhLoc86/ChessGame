package chessengine.ChessGame.game;

import chessengine.ChessGame.board.Board;
import chessengine.ChessGame.board.Square;
import chessengine.ChessGame.move.Move;
import chessengine.ChessGame.move.MoveGenerator;
import chessengine.ChessGame.piece.PieceColor;
import chessengine.ChessGame.piece.King;
import chessengine.ChessGame.piece.Pawn;
import chessengine.ChessGame.move.MoveType;
import chessengine.ChessGame.piece.Piece;
import java.util.Objects;
import java.util.List;

public class Game {
    private final Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private boolean whiteToMove = true;

    public Game() {
        this.board = new Board();
    }

    public Board getBoard() {
        return board;
    }

    /**
     * Apply the given move to the board.
     * TODO: validate more conditions (check, pins, promotion, etc.)
     */
    public void applyMove(Move move) {
        // Only accept moves that are legal (do not leave king in check, and satisfy castling rules)
        PieceColor currentColor = whiteToMove ? PieceColor.WHITE : PieceColor.BLACK;
        List<Move> legalMoves = legalMovesForColor(currentColor);
        Move matchedMove = null;
        for (Move lm : legalMoves) {
            if (lm.getFromRow() == move.getFromRow()
                    && lm.getFromCol() == move.getFromCol()
                    && lm.getToRow() == move.getToRow()
                    && lm.getToCol() == move.getToCol()
                    && lm.getType() == move.getType()) {
                if (move.getType() == MoveType.PROMOTION) {
                    if (move.getPromotionPiece() != null && move.getPromotionPiece().equals(lm.getPromotionPiece())) {
                        matchedMove = lm;
                        break;
                    }
                } else {
                    matchedMove = lm;
                    break;
                }
            }
        }
        if (matchedMove == null) {
            throw new IllegalArgumentException("Illegal move");
        }

        // promotion must specify piece
        if (move.getType() == MoveType.PROMOTION && move.getPromotionPiece() == null) {
            throw new IllegalArgumentException("Promotion move must specify promotionPiece");
        }

        // perform move
        int fr = move.getFromRow();
        int fc = move.getFromCol();
        int tr = move.getToRow();
        int tc = move.getToCol();
        Square fromSq = board.getSquare(fr, fc);
        Square toSq = board.getSquare(tr, tc);
        // special handling: en passant capture
        if (move.getType() == MoveType.EN_PASSANT) {
            // captured pawn is on the fromRow, toCol
            board.getSquare(fr, tc).setPiece(null);
        }

        Piece moving = fromSq.getPiece();
        toSq.setPiece(moving);
        fromSq.setPiece(null);

        // promotion
        if (move.getType() == MoveType.PROMOTION && move.getPromotionPiece() != null) {
            chessengine.ChessGame.piece.PieceType pt = move.getPromotionPiece();
            Piece promoted = switch (pt) {
                case QUEEN -> new chessengine.ChessGame.piece.Queen(moving.getColor());
                case ROOK -> new chessengine.ChessGame.piece.Rook(moving.getColor());
                case BISHOP -> new chessengine.ChessGame.piece.Bishop(moving.getColor());
                case KNIGHT -> new chessengine.ChessGame.piece.Knight(moving.getColor());
                default -> moving;
            };
            promoted.setHasMoved(true);
            toSq.setPiece(promoted);
        }

        // castling: move rook accordingly
        if (move.getType() == chessengine.ChessGame.move.MoveType.CASTLING) {
            // king moved from fc to tc; determine side
            if (tc > fc) {
                // king-side: rook from col 7 to tc-1
                Square rookFrom = board.getSquare(fr, 7);
                Square rookTo = board.getSquare(fr, tc - 1);
                rookTo.setPiece(rookFrom.getPiece());
                rookFrom.setPiece(null);
                if (rookTo.getPiece() != null) rookTo.getPiece().setHasMoved(true);
            } else {
                // queen-side: rook from col 0 to tc+1
                Square rookFrom = board.getSquare(fr, 0);
                Square rookTo = board.getSquare(fr, tc + 1);
                rookTo.setPiece(rookFrom.getPiece());
                rookFrom.setPiece(null);
                if (rookTo.getPiece() != null) rookTo.getPiece().setHasMoved(true);
            }
        }

        // mark moved
        if (moving != null) moving.setHasMoved(true);

        // update en passant target
        board.clearEnPassant();
        if (moving != null && moving instanceof Pawn) {
            int diff = tr - fr;
            if (Math.abs(diff) == 2) {
                // en passant target is the square passed over
                int passedRow = fr + (diff / 2);
                board.setEnPassantTarget(passedRow, fc);
            }
        }

        // switch turn
        whiteToMove = !whiteToMove;
    }

    /**
     * Return all legal moves for current player (filters out moves that leave own king in check).
     */
    public List<Move> legalMovesForCurrentPlayer() {
        PieceColor movingColor = whiteToMove ? PieceColor.WHITE : PieceColor.BLACK;
        return legalMovesForColor(movingColor);
    }

    /**
     * Return legal moves for the specified color.
     */
    public List<Move> legalMovesForColor(PieceColor color) {
        boolean forWhite = color == PieceColor.WHITE;
        MoveGenerator generator = new MoveGenerator();
        List<Move> pseudo = generator.generateAllMoves(board, forWhite);
        java.util.ArrayList<Move> legal = new java.util.ArrayList<>();
        for (Move m : pseudo) {
            // Castling safety checks: king must not be in check, must not pass through or end on attacked square
            if (m.getType() == chessengine.ChessGame.move.MoveType.CASTLING) {
                // if king currently in check, cannot castle
                if (isKingInCheck(board, color)) {
                    continue;
                }
                int fr = m.getFromRow();
                int fc = m.getFromCol();
                int tc = m.getToCol();
                int stepDir = tc > fc ? 1 : -1;
                boolean blockedByAttack = false;
                // simulate king moving one step at a time (including final)
                int steps = Math.abs(tc - fc);
                for (int i = 1; i <= steps; i++) {
                    int stepCol = fc + i * stepDir;
                    Board tmp = board.copy();
                    Square fromTmp = tmp.getSquare(fr, fc);
                    Square toTmp = tmp.getSquare(fr, stepCol);
                    toTmp.setPiece(fromTmp.getPiece());
                    fromTmp.setPiece(null);
                    // do not move rook in this simulation; only king movement matters for checked-through rule
                    if (isKingInCheck(tmp, color)) {
                        blockedByAttack = true;
                        break;
                    }
                }
                if (blockedByAttack) continue;
                // if passed all checks, now perform full simulation (including rook) and final check below
            }

            Board copy = board.copy();
            Square from = copy.getSquare(m.getFromRow(), m.getFromCol());
            Square to = copy.getSquare(m.getToRow(), m.getToCol());
            // handle en passant simulation: remove captured pawn
            if (m.getType() == chessengine.ChessGame.move.MoveType.EN_PASSANT) {
                copy.getSquare(m.getFromRow(), m.getToCol()).setPiece(null);
            }
            // perform move
            to.setPiece(from.getPiece());
            from.setPiece(null);

            // handle promotion simulation
            if (m.getType() == chessengine.ChessGame.move.MoveType.PROMOTION && m.getPromotionPiece() != null) {
                chessengine.ChessGame.piece.PieceType pt = m.getPromotionPiece();
                Piece promoted = switch (pt) {
                    case QUEEN -> new chessengine.ChessGame.piece.Queen(color);
                    case ROOK -> new chessengine.ChessGame.piece.Rook(color);
                    case BISHOP -> new chessengine.ChessGame.piece.Bishop(color);
                    case KNIGHT -> new chessengine.ChessGame.piece.Knight(color);
                    default -> to.getPiece();
                };
                to.setPiece(promoted);
            }

            // handle castling simulation: move rook
            if (m.getType() == chessengine.ChessGame.move.MoveType.CASTLING) {
                int fr = m.getFromRow();
                int fc = m.getFromCol();
                int tc = m.getToCol();
                if (tc > fc) {
                    Square rookFrom = copy.getSquare(fr, 7);
                    Square rookTo = copy.getSquare(fr, tc - 1);
                    rookTo.setPiece(rookFrom.getPiece());
                    rookFrom.setPiece(null);
                } else {
                    Square rookFrom = copy.getSquare(fr, 0);
                    Square rookTo = copy.getSquare(fr, tc + 1);
                    rookTo.setPiece(rookFrom.getPiece());
                    rookFrom.setPiece(null);
                }
            }

            if (!isKingInCheck(copy, color)) {
                legal.add(m);
            }
        }
        return legal;
    }

    public boolean isCheckmate(PieceColor color) {
        List<Move> legal = legalMovesForColor(color);
        return legal.isEmpty() && isKingInCheck(color);
    }

    public boolean isStalemate(PieceColor color) {
        List<Move> legal = legalMovesForColor(color);
        return legal.isEmpty() && !isKingInCheck(color);
    }

    /**
     * Check if the king of the given color is in check on the current board.
     */
    public boolean isKingInCheck(PieceColor color) {
        return isKingInCheck(this.board, color);
    }

    /**
     * Check if the king of the given color is in check on the provided board.
     * A king is in check if any opponent pseudo-legal move attacks the king's square.
     */
    boolean isKingInCheck(Board boardToCheck, PieceColor color) {
        // find king
        int kingR = -1, kingC = -1;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Square s = boardToCheck.getSquare(r, c);
                if (!s.isEmpty() && s.getPiece() instanceof King
                        && s.getPiece().getColor() == color) {
                    kingR = r;
                    kingC = c;
                    break;
                }
            }
            if (kingR != -1) break;
        }
        if (kingR == -1) {
            // no king found; treat as not in check (could also throw)
            return false;
        }

        // generate all opponent pseudo-legal moves
        MoveGenerator gen = new MoveGenerator();
        boolean forWhite = (color == PieceColor.WHITE);
        List<Move> opponentMoves = gen.generateAllMoves(boardToCheck, !forWhite);
        for (Move m : opponentMoves) {
            if (m.getToRow() == kingR && m.getToCol() == kingC) return true;
        }
        return false;
    }
}


