package chessengine.piece;

import chessengine.board.Board;
import chessengine.board.Square;
import chessengine.move.Move;
import chessengine.move.MoveType;
import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {
    public Queen(PieceColor color) {
        super(color);
    }

    @Override
    public List<Move> generateLegalMoves(Square from, Board board) {
        List<Move> moves = new ArrayList<>();
        int r = from.getRow();
        int c = from.getCol();
        int[][] directions = {
            {1,0}, {-1,0}, {0,1}, {0,-1},
            {1,1}, {1,-1}, {-1,1}, {-1,-1}
        };
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
        return "Queen";
    }

    @Override
    public Piece copy() {
        Queen q = new Queen(this.color);
        q.setHasMoved(this.hasMoved);
        return q;
    }
}


