package chessengine.piece;

import chessengine.board.Board;
import chessengine.board.Square;
import chessengine.move.Move;
import chessengine.move.MoveType;
import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
    public Pawn(PieceColor color) {
        super(color);
    }

    @Override
    public PieceType getType() {
        return PieceType.PAWN;
    }

    @Override
    public List<Move> generateLegalMoves(Square from, Board board) {
        List<Move> moves = new ArrayList<>();
        int r = from.getRow();
        int c = from.getCol();
        int dir = (this.color == PieceColor.WHITE) ? -1 : 1;
        int startRow = (this.color == PieceColor.WHITE) ? 6 : 1;

        // forward 1
        int fr = r + dir;
        if (fr >= 0 && fr < Board.SIZE) {
            Square forwardSquare = board.getSquare(fr, c);
            if (forwardSquare.isEmpty()) {
                // promotion by moving forward
                if (fr == 0 || fr == Board.SIZE - 1) {
                    moves.add(new Move(r, c, fr, c, MoveType.PROMOTION, chessengine.piece.PieceType.QUEEN));
                    moves.add(new Move(r, c, fr, c, MoveType.PROMOTION, chessengine.piece.PieceType.ROOK));
                    moves.add(new Move(r, c, fr, c, MoveType.PROMOTION, chessengine.piece.PieceType.BISHOP));
                    moves.add(new Move(r, c, fr, c, MoveType.PROMOTION, chessengine.piece.PieceType.KNIGHT));
                } else {
                    moves.add(new Move(r, c, fr, c, MoveType.NORMAL));

                    // forward 2 from starting position
                    if (r == startRow) {
                        int fr2 = r + 2 * dir;
                        if (fr2 >= 0 && fr2 < Board.SIZE) {
                            Square forwardTwo = board.getSquare(fr2, c);
                            if (forwardTwo.isEmpty()) {
                                moves.add(new Move(r, c, fr2, c, MoveType.NORMAL));
                            }
                        }
                    }
                }
            }
        }

        // captures (including promotion captures)
        int[] dcCandidates = new int[] { -1, 1 };
        for (int dc : dcCandidates) {
            int nc = c + dc;
            int nr = r + dir;
            if (nr < 0 || nr >= Board.SIZE || nc < 0 || nc >= Board.SIZE)
                continue;
            Square target = board.getSquare(nr, nc);
            if (!target.isEmpty() && target.getPiece().getColor() != this.color) {
                if (nr == 0 || nr == Board.SIZE - 1) {
                    moves.add(new Move(r, c, nr, nc, MoveType.PROMOTION, chessengine.piece.PieceType.QUEEN));
                    moves.add(new Move(r, c, nr, nc, MoveType.PROMOTION, chessengine.piece.PieceType.ROOK));
                    moves.add(new Move(r, c, nr, nc, MoveType.PROMOTION, chessengine.piece.PieceType.BISHOP));
                    moves.add(new Move(r, c, nr, nc, MoveType.PROMOTION, chessengine.piece.PieceType.KNIGHT));
                } else {
                    moves.add(new Move(r, c, nr, nc, MoveType.CAPTURE));
                }
            }
        }

        // en passant
        int epRow = board.getEnPassantRow();
        int epCol = board.getEnPassantCol();
        if (epRow >= 0 && epCol >= 0) {
            for (int dc : dcCandidates) {
                int nc = c + dc;
                int nr = r + dir;
                if (nr == epRow && nc == epCol) {
                    // ensure adjacent pawn exists and is opponent
                    if (c + dc >= 0 && c + dc < Board.SIZE) {
                        Square adj = board.getSquare(r, c + dc);
                        if (!adj.isEmpty() && adj.getPiece() instanceof Pawn
                                && adj.getPiece().getColor() != this.color) {
                            moves.add(new Move(r, c, nr, nc, MoveType.EN_PASSANT));
                        }
                    }
                }
            }
        }
        return moves;
    }

    @Override
    public String getName() {
        return "Pawn";
    }

    @Override
    public Piece copy() {
        Pawn p = new Pawn(this.color);
        p.setHasMoved(this.hasMoved);
        return p;
    }
}
