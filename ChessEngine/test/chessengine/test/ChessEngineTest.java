package chessengine.test;

import chessengine.ChessGame.piece.King;
import chessengine.ChessGame.piece.PieceColor;
import chessengine.ChessGame.piece.Knight;
import chessengine.ChessGame.piece.Pawn;
import chessengine.ChessGame.piece.Bishop;
import chessengine.ChessGame.piece.Queen;
import chessengine.ChessGame.piece.Rook;
import chessengine.ChessGame.piece.PieceType;
import chessengine.ChessGame.board.Board;
import chessengine.ChessGame.game.Game;
import chessengine.ChessGame.move.Move;
import chessengine.ChessGame.move.MoveType;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class ChessEngineTest {
    private void setWhiteToMove(Game g, boolean white) throws Exception {
        Field f = Game.class.getDeclaredField("whiteToMove");
        f.setAccessible(true);
        f.setBoolean(g, white);
    }

    @Test
    public void pawnOneStep() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        b.initializeStandardSetup();
        Move m = new Move(6,0,5,0, MoveType.NORMAL);
        g.applyMove(m);
        assertNotNull(b.getSquare(5,0).getPiece());
    }

    @Test
    public void pawnTwoStep() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        b.initializeStandardSetup();
        Move m = new Move(6,1,4,1, MoveType.NORMAL);
        g.applyMove(m);
        assertNotNull(b.getSquare(4,1).getPiece());
    }

    @Test
    public void illegalKingTwoStep() {
        Game g = new Game();
        Board b = g.getBoard();
        b.initializeStandardSetup();
        Move m = new Move(7,4,5,4, MoveType.NORMAL);
        try {
            g.applyMove(m);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void castlingKingSideValid() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
        b.setPieceAt(7,4, new King(PieceColor.WHITE));
        b.setPieceAt(7,7, new Rook(PieceColor.WHITE));
        b.setPieceAt(0,4, new King(PieceColor.BLACK));
        setWhiteToMove(g, true);
        Move m = new Move(7,4,7,6, MoveType.CASTLING);
        g.applyMove(m);
        assertNotNull(b.getSquare(7,6).getPiece());
        assertNotNull(b.getSquare(7,5).getPiece());
    }

    @Test
    public void castlingBlockedByCheck() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
        b.setPieceAt(7,4, new King(PieceColor.WHITE));
        b.setPieceAt(7,7, new Rook(PieceColor.WHITE));
        b.setPieceAt(6,4, new Rook(PieceColor.BLACK));
        b.setPieceAt(0,4, new King(PieceColor.BLACK));
        setWhiteToMove(g, true);
        Move m = new Move(7,4,7,6, MoveType.CASTLING);
        try {
            g.applyMove(m);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void castlingBlockedPassingThroughCheck() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
        b.setPieceAt(7,4, new King(PieceColor.WHITE));
        b.setPieceAt(7,7, new Rook(PieceColor.WHITE));
        b.setPieceAt(0,5, new Rook(PieceColor.BLACK));
        b.setPieceAt(0,4, new King(PieceColor.BLACK));
        setWhiteToMove(g, true);
        Move m = new Move(7,4,7,6, MoveType.CASTLING);
        try {
            g.applyMove(m);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void enPassantValid() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
        b.setPieceAt(3,4, new Pawn(PieceColor.WHITE));
        b.setPieceAt(1,5, new Pawn(PieceColor.BLACK));
        b.setPieceAt(7,7, new King(PieceColor.WHITE));
        b.setPieceAt(0,0, new King(PieceColor.BLACK));
        setWhiteToMove(g, false);
        g.applyMove(new Move(1,5,3,5, MoveType.NORMAL));
        setWhiteToMove(g, true);
        g.applyMove(new Move(3,4,2,5, MoveType.EN_PASSANT));
        assertNotNull(b.getSquare(2,5).getPiece());
        assertTrue(b.getSquare(3,5).isEmpty());
    }

    @Test
    public void enPassantInvalidAfterDelay() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
        b.setPieceAt(3,4, new Pawn(PieceColor.WHITE));
        b.setPieceAt(1,5, new Pawn(PieceColor.BLACK));
        b.setPieceAt(7,7, new King(PieceColor.WHITE));
        b.setPieceAt(0,0, new King(PieceColor.BLACK));
        setWhiteToMove(g, false);
        g.applyMove(new Move(1,5,3,5, MoveType.NORMAL));
        // white plays another move
        g.applyMove(new Move(7,7,6,7, MoveType.NORMAL));
        try {
            g.applyMove(new Move(3,4,2,5, MoveType.EN_PASSANT));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void promotionVariants() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
        b.setPieceAt(1,0, new Pawn(PieceColor.WHITE));
        b.setPieceAt(0,7, new King(PieceColor.BLACK));
        b.setPieceAt(7,7, new King(PieceColor.WHITE));
        setWhiteToMove(g, true);
        g.applyMove(new Move(1,0,0,0, MoveType.PROMOTION, PieceType.QUEEN));
        assertTrue(b.getSquare(0,0).getPiece() instanceof Queen);
        setWhiteToMove(g, true);
        b.setPieceAt(1,1, new Pawn(PieceColor.WHITE));
        g.applyMove(new Move(1,1,0,1, MoveType.PROMOTION, PieceType.ROOK));
        assertTrue(b.getSquare(0,1).getPiece() instanceof Rook);
        setWhiteToMove(g, true);
        b.setPieceAt(1,2, new Pawn(PieceColor.WHITE));
        g.applyMove(new Move(1,2,0,2, MoveType.PROMOTION, PieceType.BISHOP));
        assertTrue(b.getSquare(0,2).getPiece() instanceof Bishop);
        setWhiteToMove(g, true);
        b.setPieceAt(1,3, new Pawn(PieceColor.WHITE));
        g.applyMove(new Move(1,3,0,3, MoveType.PROMOTION, PieceType.KNIGHT));
        assertTrue(b.getSquare(0,3).getPiece() instanceof Knight);
        setWhiteToMove(g, true);
    }

    @Test
    public void promotionMissingPiece() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
        b.setPieceAt(1,0, new Pawn(PieceColor.WHITE));
        b.setPieceAt(0,7, new King(PieceColor.BLACK));
        b.setPieceAt(7,7, new King(PieceColor.WHITE));
        setWhiteToMove(g, true);
        try {
            g.applyMove(new Move(1,0,0,0, MoveType.PROMOTION));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void checkDetection() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
        b.setPieceAt(7,4, new King(PieceColor.WHITE));
        b.setPieceAt(0,4, new King(PieceColor.BLACK));
        b.setPieceAt(6,4, new Rook(PieceColor.BLACK));
        assertTrue(g.isKingInCheck(PieceColor.WHITE));
    }

    @Test
    public void foolsMateCheckmate() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        b.initializeStandardSetup();
        g.applyMove(new Move(6,5,5,5, MoveType.NORMAL)); // f3
        g.applyMove(new Move(1,4,3,4, MoveType.NORMAL)); // e5
        g.applyMove(new Move(6,6,4,6, MoveType.NORMAL)); // g4
        g.applyMove(new Move(0,3,4,7, MoveType.NORMAL)); // Qh4#
        assertTrue(g.isCheckmate(PieceColor.WHITE));
    }

    @Test
    public void stalemateSimple() throws Exception {
        Game g = new Game();
        Board b = g.getBoard();
        for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
        b.setPieceAt(0,0, new King(PieceColor.BLACK));
        b.setPieceAt(1,2, new Queen(PieceColor.WHITE));
        b.setPieceAt(2,1, new King(PieceColor.WHITE));
        setWhiteToMove(g, false);
        assertTrue(g.isStalemate(PieceColor.BLACK));
    }
}





