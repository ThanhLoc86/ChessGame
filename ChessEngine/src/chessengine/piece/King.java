package chessengine.piece;

import chessengine.board.Board;
import chessengine.board.Square;
import chessengine.move.Move;
import chessengine.move.MoveType;
import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(PieceColor color) {
        super(color);
    }

    @Override
    public List<Move> generateLegalMoves(Square from, Board board) {
        List<Move> moves = new ArrayList<>();
        int r = from.getRow();
        int c = from.getCol();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr;
                int nc = c + dc;
                if (nr < 0 || nr >= Board.SIZE || nc < 0 || nc >= Board.SIZE) continue;
                Square dest = board.getSquare(nr, nc);
                if (dest.isEmpty()) {
                    moves.add(new Move(r, c, nr, nc, MoveType.NORMAL));
                } else if (dest.getPiece().getColor() != this.color) {
                    moves.add(new Move(r, c, nr, nc, MoveType.CAPTURE));
                }
            }
        }
        // Castling pseudo-legal generation: only check unmoved and clear path here.
        if (!this.hasMoved()) {
            int kingRow = r;
            // king-side rook at col 7
            if (canCastleShort(board, kingRow)) {
                moves.add(new Move(r, c, kingRow, c + 2, MoveType.CASTLING));
            }
            if (canCastleLong(board, kingRow)) {
                moves.add(new Move(r, c, kingRow, c - 2, MoveType.CASTLING));
            }
        }
        return moves;
    }

    private boolean canCastleShort(Board board, int kingRow) {
        // rook at col 7
        Square rookSquare = board.getSquare(kingRow, 7);
        if (rookSquare.isEmpty()) return false;
        Piece rook = rookSquare.getPiece();
        if (!rook.getName().equals("Rook") || rook.getColor() != this.color || rook.hasMoved()) return false;
        // squares between king and rook must be empty (cols 5 and 6)
        for (int col = 5; col <= 6; col++) {
            if (!board.getSquare(kingRow, col).isEmpty()) return false;
        }
        return true;
    }

    private boolean canCastleLong(Board board, int kingRow) {
        // rook at col 0
        Square rookSquare = board.getSquare(kingRow, 0);
        if (rookSquare.isEmpty()) return false;
        Piece rook = rookSquare.getPiece();
        if (!rook.getName().equals("Rook") || rook.getColor() != this.color || rook.hasMoved()) return false;
        // squares between king and rook must be empty (cols 1,2,3)
        for (int col = 1; col <= 3; col++) {
            if (!board.getSquare(kingRow, col).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "King";
    }

    @Override
    public Piece copy() {
        King k = new King(this.color);
        k.setHasMoved(this.hasMoved);
        return k;
    }
}


