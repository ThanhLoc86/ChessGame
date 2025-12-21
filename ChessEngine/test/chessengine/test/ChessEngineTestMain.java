package chessengine.test;

import chessengine.ChessGame.board.Board;
import chessengine.ChessGame.game.Game;
import chessengine.ChessGame.move.Move;
import chessengine.ChessGame.move.MoveType;
import chessengine.ChessGame.piece.PieceColor;
import chessengine.ChessGame.piece.PieceType;
import chessengine.ChessGame.piece.Pawn;
import chessengine.ChessGame.piece.Queen;
import chessengine.ChessGame.piece.Rook;
import chessengine.ChessGame.piece.Bishop;
import chessengine.ChessGame.piece.Knight;
import chessengine.ChessGame.piece.King;

import java.lang.reflect.Field;

/**
 * Manual test runner for ChessEngine rules. Run from IDE or command line.
 * Each test prints PASS/FAIL and exception stack on failure.
 */
public class ChessEngineTestMain {
    public static void main(String[] args) throws Exception {
        testPawnOneStep();
        testPawnTwoStep();
        testIllegalKingTwoStep();
        testCastlingKingSide();
        testCastlingBlockedByCheck();
        testCastlingBlockedPassingThroughCheck();
        testEnPassantValid();
        testEnPassantInvalidAfterDelay();
        testPromotionVariants();
        testPromotionMissingPiece();
        testCheckDetection();
        testFoolsMateCheckmate();
        testStalemateSimple();
    }

    static void ok(String name) { System.out.println("[PASS] " + name); }
    static void fail(String name, Exception e) { System.out.println("[FAIL] " + name); e.printStackTrace(); }

    // Helpers to flip turn
    private static void setWhiteToMove(Game g, boolean white) throws Exception {
        Field f = Game.class.getDeclaredField("whiteToMove");
        f.setAccessible(true);
        f.setBoolean(g, white);
    }

    public static void testPawnOneStep() {
        String name = "Pawn one step";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            b.initializeStandardSetup();
            Move m = new Move(6, 0, 5, 0, MoveType.NORMAL);
            g.applyMove(m);
            if (b.getSquare(5,0).isEmpty()) throw new AssertionError("Pawn not moved");
            ok(name);
        } catch (Exception e) { fail(name, e); }
    }

    public static void testPawnTwoStep() {
        String name = "Pawn two step initial";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            b.initializeStandardSetup();
            Move m = new Move(6,1,4,1, MoveType.NORMAL);
            g.applyMove(m);
            if (b.getSquare(4,1).isEmpty()) throw new AssertionError("Pawn not moved two squares");
            ok(name);
        } catch (Exception e) { fail(name, e); }
    }

    public static void testIllegalKingTwoStep() {
        String name = "Illegal king two-step blocked";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            b.initializeStandardSetup();
            Move m = new Move(7,4,5,4, MoveType.NORMAL); // king two squares
            try {
                g.applyMove(m);
                throw new AssertionError("Illegal king move accepted");
            } catch (IllegalArgumentException ex) {
                ok(name);
            }
        } catch (Exception e) { fail(name, e); }
    }

    public static void testCastlingKingSide() {
        String name = "Castling king-side valid";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            // clear and set minimal pieces
            for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
            b.setPieceAt(7,4, new King(PieceColor.WHITE));
            b.setPieceAt(7,7, new Rook(PieceColor.WHITE));
            b.setPieceAt(0,4, new King(PieceColor.BLACK));
            // white to move
            setWhiteToMove(g, true);
            Move m = new Move(7,4,7,6, MoveType.CASTLING);
            g.applyMove(m);
            if (b.getSquare(7,6).isEmpty()) throw new AssertionError("King not at g1 (7,6)");
            if (b.getSquare(7,5).isEmpty()) throw new AssertionError("Rook not at f1 (7,5)");
            ok(name);
        } catch (Exception e) { fail(name, e); }
    }

    public static void testCastlingBlockedByCheck() {
        String name = "Castling blocked when king in check";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
            b.setPieceAt(7,4, new King(PieceColor.WHITE));
            b.setPieceAt(7,7, new Rook(PieceColor.WHITE));
            b.setPieceAt(6,4, new Rook(PieceColor.BLACK)); // checks king
            b.setPieceAt(0,4, new King(PieceColor.BLACK));
            setWhiteToMove(g, true);
            Move m = new Move(7,4,7,6, MoveType.CASTLING);
            try {
                g.applyMove(m);
                throw new AssertionError("Illegal king move accepted");
            } catch (IllegalArgumentException ex) {
                ok(name);
            }
        } catch (Exception e) { fail(name, e); }
    }

    public static void testCastlingBlockedPassingThroughCheck() {
        String name = "Castling blocked when passing through attacked square";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
            b.setPieceAt(7,4, new King(PieceColor.WHITE));
            b.setPieceAt(7,7, new Rook(PieceColor.WHITE));
            b.setPieceAt(0,5, new Rook(PieceColor.BLACK)); // attacks f1 (7,5)
            b.setPieceAt(0,4, new King(PieceColor.BLACK));
            setWhiteToMove(g, true);
            Move m = new Move(7,4,7,6, MoveType.CASTLING);
            try {
                g.applyMove(m);
                throw new AssertionError("Illegal king move accepted");
            } catch (IllegalArgumentException ex) {
                ok(name);
            }
        } catch (Exception e) { fail(name, e); }
    }

    public static void testEnPassantValid() {
        String name = "En passant valid immediate";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
            // setup white pawn at (3,4), black pawn at (1,5)
            b.setPieceAt(3,4, new Pawn(PieceColor.WHITE));
            b.setPieceAt(1,5, new Pawn(PieceColor.BLACK));
            b.setPieceAt(7,7, new King(PieceColor.WHITE));
            b.setPieceAt(0,0, new King(PieceColor.BLACK));
            // set black to move
            setWhiteToMove(g, false);
            Move blackTwo = new Move(1,5,3,5, MoveType.NORMAL);
            g.applyMove(blackTwo);
            // now white can en passant from (3,4) to (2,5)
            setWhiteToMove(g, true);
            Move ep = new Move(3,4,2,5, MoveType.EN_PASSANT);
            g.applyMove(ep);
            if (!(b.getSquare(2,5).getPiece() instanceof Pawn)) throw new AssertionError("En passant capture failed to place pawn");
            if (!b.getSquare(3,5).isEmpty()) throw new AssertionError("Captured pawn not removed");
            ok(name);
        } catch (Exception e) { fail(name, e); }
    }

    public static void testEnPassantInvalidAfterDelay() {
        String name = "En passant invalid after a move";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
            b.setPieceAt(3,4, new Pawn(PieceColor.WHITE));
            b.setPieceAt(1,5, new Pawn(PieceColor.BLACK));
            b.setPieceAt(7,7, new King(PieceColor.WHITE));
            b.setPieceAt(0,0, new King(PieceColor.BLACK));
            setWhiteToMove(g, false);
            g.applyMove(new Move(1,5,3,5, MoveType.NORMAL)); // black two-step
            // white plays some other move (dummy): move white king
            g.applyMove(new Move(7,7,6,7, MoveType.NORMAL));
            // now attempt en passant should be illegal
            try {
                g.applyMove(new Move(3,4,2,5, MoveType.EN_PASSANT));
                throw new AssertionError("Illegal king move accepted");
            } catch (IllegalArgumentException ex) {
                ok(name);
            }
        } catch (Exception e) { fail(name, e); }
    }

    public static void testPromotionVariants() {
        String name = "Promotion variants";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
            b.setPieceAt(1,0, new Pawn(PieceColor.WHITE));
            b.setPieceAt(0,7, new King(PieceColor.BLACK));
            b.setPieceAt(7,7, new King(PieceColor.WHITE));
            setWhiteToMove(g, true);
            // promote to Queen
            g.applyMove(new Move(1,0,0,0, MoveType.PROMOTION, PieceType.QUEEN));
            if (!(b.getSquare(0,0).getPiece() instanceof Queen)) throw new AssertionError("Promotion to Queen failed");
            // reset pawn to test other promotions
            b.setPieceAt(1,1, new Pawn(PieceColor.WHITE));
            g.applyMove(new Move(1,1,0,1, MoveType.PROMOTION, PieceType.ROOK));
            if (!(b.getSquare(0,1).getPiece() instanceof Rook)) throw new AssertionError("Promotion to Rook failed");
            b.setPieceAt(1,2, new Pawn(PieceColor.WHITE));
            g.applyMove(new Move(1,2,0,2, MoveType.PROMOTION, PieceType.BISHOP));
            if (!(b.getSquare(0,2).getPiece() instanceof Bishop)) throw new AssertionError("Promotion to Bishop failed");
            b.setPieceAt(1,3, new Pawn(PieceColor.WHITE));
            g.applyMove(new Move(1,3,0,3, MoveType.PROMOTION, PieceType.KNIGHT));
            if (!(b.getSquare(0,3).getPiece() instanceof Knight)) throw new AssertionError("Promotion to Knight failed");
            ok(name);
        } catch (Exception e) { fail(name, e); }
    }

    public static void testPromotionMissingPiece() {
        String name = "Promotion missing piece blocked";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
            b.setPieceAt(1,0, new Pawn(PieceColor.WHITE));
            b.setPieceAt(0,7, new King(PieceColor.BLACK));
            b.setPieceAt(7,7, new King(PieceColor.WHITE));
            setWhiteToMove(g, true);
            try {
                g.applyMove(new Move(1,0,0,0, MoveType.PROMOTION));
                throw new AssertionError("Illegal king move accepted");
            } catch (IllegalArgumentException ex) {
                ok(name);
            }
        } catch (Exception e) { fail(name, e); }
    }

    public static void testCheckDetection() {
        String name = "Check detection";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
            b.setPieceAt(7,4, new King(PieceColor.WHITE));
            b.setPieceAt(0,4, new King(PieceColor.BLACK));
            b.setPieceAt(6,4, new Rook(PieceColor.BLACK)); // check white king
            if (!g.isKingInCheck(PieceColor.WHITE)) throw new AssertionError("Check not detected");
            ok(name);
        } catch (Exception e) { fail(name, e); }
    }

    public static void testFoolsMateCheckmate() {
        String name = "Fool's mate checkmate";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            b.initializeStandardSetup();
            // 1. f3
            g.applyMove(new Move(6,5,5,5, MoveType.NORMAL));
            // ... e5
            // black move is automatic after applyMove
            g.applyMove(new Move(1,4,3,4, MoveType.NORMAL));
            // 2. g4
            g.applyMove(new Move(6,6,4,6, MoveType.NORMAL));
            // ... Qh4#
            g.applyMove(new Move(0,3,4,7, MoveType.NORMAL));
            if (!g.isCheckmate(PieceColor.WHITE)) throw new AssertionError("Fool's mate not detected as checkmate");
            ok(name);
        } catch (Exception e) { fail(name, e); }
    }

    public static void testStalemateSimple() {
        String name = "Simple stalemate";
        try {
            Game g = new Game();
            Board b = g.getBoard();
            for (int r=0;r<Board.SIZE;r++) for (int c=0;c<Board.SIZE;c++) b.setPieceAt(r,c,null);
            // black king at a8 (0,0), white queen at b6 (1,2), white king at c6 (2,2)
            b.setPieceAt(0,0, new King(PieceColor.BLACK));
            b.setPieceAt(1,2, new Queen(PieceColor.WHITE));
            b.setPieceAt(2,1, new King(PieceColor.WHITE));
            setWhiteToMove(g, false);
            if (!g.isStalemate(PieceColor.BLACK)) throw new AssertionError("Stalemate not detected");
            ok(name);
        } catch (Exception e) { fail(name, e); }
    }
}


