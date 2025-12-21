package chessengine.ChessGame.piece;

import chessengine.ChessGame.board.Board;
import chessengine.ChessGame.board.Square;
import chessengine.ChessGame.move.Move;
import chessengine.ChessGame.move.MoveType;
import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {
    public Bishop(PieceColor color) {
        super(color);
    }

    @Override
    public List<Move> generateLegalMoves(Square from, Board board) {
        List<Move> moves = new ArrayList<>();
        int r = from.getRow();
        int c = from.getCol();
        int[][] directions = { {1,1}, {1,-1}, {-1,1}, {-1,-1} };
        for (int[] d : directions) {
            int dr = d[0], dc = d[1];
            int nr = r + dr, nc = c + dc;
            while (nr >= 0 && nr < Board.SIZE && nc >= 0 && nc < Board.SIZE) {
                Square dest = board.getSquare(nr, nc);
                if (dest.isEmpty()) {
                    moves.add(new Move(r, c, nr, nc, MoveType.NORMAL));
                } else {
                    if (dest.getPiece().getColor() != this.color) {
                        moves.add(new Move(r, c, nr, nc, MoveType.CAPTURE));
                    }
                    break; // blocked
                }
                nr += dr;
                nc += dc;
            }
        }
        return moves;
    }

    @Override
    public String getName() {
        return "Bishop";
    }

    @Override
    public Piece copy() {
        Bishop b = new Bishop(this.color);
        b.setHasMoved(this.hasMoved);
        return b;
    }
}


