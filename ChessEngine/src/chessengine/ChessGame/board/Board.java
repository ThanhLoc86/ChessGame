package chessengine.ChessGame.board;

import chessengine.ChessGame.piece.King;
import chessengine.ChessGame.piece.Pawn;
import chessengine.ChessGame.piece.Piece;
import chessengine.ChessGame.piece.PieceColor;
import chessengine.ChessGame.piece.Rook;
import chessengine.ChessGame.piece.Knight;
import chessengine.ChessGame.piece.Bishop;
import chessengine.ChessGame.piece.Queen;
import java.util.Optional;

public class Board {
    public static final int SIZE = 8;

    private final Square[][] squares;
    private int enPassantRow = -1;
    private int enPassantCol = -1;

    public Board() {
        squares = new Square[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                squares[r][c] = new Square(r, c);
            }
        }
    }

    public Optional<Piece> getPieceAt(int row, int col) {
        return Optional.ofNullable(squares[row][col].getPiece());
    }

    public void setPieceAt(int row, int col, Piece piece) {
        squares[row][col].setPiece(piece);
    }

    public Square getSquare(int row, int col) {
        return squares[row][col];
    }

    /**
     * Initialize a standard chess starting position with only pawns and kings.
     * White pieces are placed on rows 6 (pawns) and 7 (king), Black on rows 1 and 0 respectively.
     */
    public void initializeStandardSetup() {
        // clear board just in case
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                squares[r][c].setPiece(null);
            }
        }

        // Pawns
        for (int c = 0; c < SIZE; c++) {
            setPieceAt(6, c, new Pawn(PieceColor.WHITE));
            setPieceAt(1, c, new Pawn(PieceColor.BLACK));
        }

        // Back rank pieces
        // White back rank (row 7)
        setPieceAt(7, 0, new Rook(PieceColor.WHITE));
        setPieceAt(7, 1, new Knight(PieceColor.WHITE));
        setPieceAt(7, 2, new Bishop(PieceColor.WHITE));
        setPieceAt(7, 3, new Queen(PieceColor.WHITE));
        setPieceAt(7, 4, new King(PieceColor.WHITE));
        setPieceAt(7, 5, new Bishop(PieceColor.WHITE));
        setPieceAt(7, 6, new Knight(PieceColor.WHITE));
        setPieceAt(7, 7, new Rook(PieceColor.WHITE));

        // Black back rank (row 0)
        setPieceAt(0, 0, new Rook(PieceColor.BLACK));
        setPieceAt(0, 1, new Knight(PieceColor.BLACK));
        setPieceAt(0, 2, new Bishop(PieceColor.BLACK));
        setPieceAt(0, 3, new Queen(PieceColor.BLACK));
        setPieceAt(0, 4, new King(PieceColor.BLACK));
        setPieceAt(0, 5, new Bishop(PieceColor.BLACK));
        setPieceAt(0, 6, new Knight(PieceColor.BLACK));
        setPieceAt(0, 7, new Rook(PieceColor.BLACK));
    }

    public void clearEnPassant() {
        this.enPassantRow = -1;
        this.enPassantCol = -1;
    }

    public void setEnPassantTarget(int row, int col) {
        this.enPassantRow = row;
        this.enPassantCol = col;
    }

    public int getEnPassantRow() {
        return enPassantRow;
    }

    public int getEnPassantCol() {
        return enPassantCol;
    }

    /**
     * Create a shallow copy of this board (copies piece references).
     * Create a deep-ish copy of this board: copies pieces via Piece.copy() so simulations do not
     * mutate the original piece instances.
     */
    public Board copy() {
        Board nb = new Board();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece p = this.squares[r][c].getPiece();
                nb.squares[r][c].setPiece(p == null ? null : p.copy());
            }
        }
        nb.enPassantRow = this.enPassantRow;
        nb.enPassantCol = this.enPassantCol;
        return nb;
    }
}


