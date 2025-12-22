package chessengine.move;

import chessengine.board.Board;
import chessengine.board.Square;
import chessengine.piece.Piece;
import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    /**
     * Generate all possible moves for the given side by delegating to each piece.
     * This provides basic move generation for Pawn and King per requirements.
     */
    public List<Move> generateAllMoves(Board board, boolean forWhite) {
        List<Move> result = new ArrayList<>();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Square sq = board.getSquare(r, c);
                if (!sq.isEmpty()) {
                    Piece p = sq.getPiece();
                    boolean isWhitePiece = p.getColor() == chessengine.piece.PieceColor.WHITE;
                    if ((forWhite && isWhitePiece) || (!forWhite && !isWhitePiece)) {
                        result.addAll(p.generateLegalMoves(sq, board));
                    }
                }
            }
        }
        return result;
    }
}


