package chessengine.ChessGame.piece;

import chessengine.ChessGame.board.Board;
import chessengine.ChessGame.board.Square;
import chessengine.ChessGame.move.Move;
import chessengine.ChessGame.move.MoveType;
import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    public Knight(PieceColor color) {
        super(color);
    }

    @Override
    public List<Move> generateLegalMoves(Square from, Board board) {
        List<Move> moves = new ArrayList<>();
        int r = from.getRow();
        int c = from.getCol();
        int[][] deltas = {
            {2,1},{2,-1},{-2,1},{-2,-1},
            {1,2},{1,-2},{-1,2},{-1,-2}
        };
        for (int[] d : deltas) {
            int nr = r + d[0], nc = c + d[1];
            if (nr < 0 || nr >= Board.SIZE || nc < 0 || nc >= Board.SIZE) continue;
            Square dest = board.getSquare(nr, nc);
            if (dest.isEmpty()) {
                moves.add(new Move(r, c, nr, nc, MoveType.NORMAL));
            } else if (dest.getPiece().getColor() != this.color) {
                moves.add(new Move(r, c, nr, nc, MoveType.CAPTURE));
            }
        }
        return moves;
    }

    @Override
    public String getName() {
        return "Knight";
    }

    @Override
    public Piece copy() {
        Knight k = new Knight(this.color);
        k.setHasMoved(this.hasMoved);
        return k;
    }
}


