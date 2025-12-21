package ChessClient.ChessGame.piece;

import ChessClient.ChessGame.board.Board;
import ChessClient.ChessGame.board.Square;
import ChessClient.ChessGame.move.Move;
import java.util.List;

public abstract class Piece {
    protected final PieceColor color;
    protected boolean hasMoved = false;

    public Piece(PieceColor color) {
        this.color = color;
    }

    public PieceColor getColor() {
        return color;
    }

    /**
     * Generate legal moves for this piece from the given square on the provided board.
     * Implementation left as TODO in skeleton for complex rules.
     */
    public abstract List<Move> generateLegalMoves(Square from, Board board);

    /**
     * Human-readable name of the piece (e.g., "King", "Pawn").
     */
    public abstract String getName();

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    /**
     * Create a copy of this piece (used by Board.copy() to avoid mutating originals).
     */
    public abstract Piece copy();
}


