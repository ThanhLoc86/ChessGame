package chessengine.ai;

import chessengine.board.Board;
import chessengine.game.Game;
import chessengine.move.Move;
import chessengine.piece.Piece;
import chessengine.piece.PieceColor;
import chessengine.piece.PieceType;
import java.util.List;

public class MinimaxBot {

    private final PieceColor color;
    private final int depth;

    public MinimaxBot(PieceColor color, int depth) {
        this.color = color;
        this.depth = depth;
    }

    public Move findBestMove(Game game) {
        List<Move> legal = game.legalMovesForColor(color);
        if (legal.isEmpty())
            return null;

        Move bestMove = null;
        double bestValue = color == PieceColor.WHITE ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (Move m : legal) {
            Game sim = simulateMove(game, m);
            double val = minimax(sim, depth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                    color != PieceColor.WHITE);

            if (color == PieceColor.WHITE) {
                if (val > bestValue) {
                    bestValue = val;
                    bestMove = m;
                }
            } else {
                if (val < bestValue) {
                    bestValue = val;
                    bestMove = m;
                }
            }
        }
        return bestMove;
    }

    private double minimax(Game game, int depth, double alpha, double beta, boolean isMaximizing) {
        if (depth == 0)
            return evaluate(game.getBoard());

        List<Move> moves = game.legalMovesForColor(isMaximizing ? PieceColor.WHITE : PieceColor.BLACK);
        if (moves.isEmpty()) {
            if (game.isKingInCheck(isMaximizing ? PieceColor.WHITE : PieceColor.BLACK)) {
                return isMaximizing ? -10000 : 10000;
            }
            return 0; // Stalemate
        }

        if (isMaximizing) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Move m : moves) {
                double eval = minimax(simulateMove(game, m), depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha)
                    break;
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (Move m : moves) {
                double eval = minimax(simulateMove(game, m), depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha)
                    break;
            }
            return minEval;
        }
    }

    private Game simulateMove(Game original, Move m) {
        Board copyBoard = original.getBoard().copy();
        Game sim = new Game(copyBoard, original.getActiveColor() == PieceColor.WHITE);
        sim.applyMove(m);
        return sim;
    }

    private double evaluate(Board board) {
        double score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getSquare(r, c).getPiece();
                if (p != null) {
                    double val = getPieceValue(p.getType());
                    score += (p.getColor() == PieceColor.WHITE ? val : -val);
                }
            }
        }
        return score;
    }

    private double getPieceValue(PieceType type) {
        return switch (type) {
            case PAWN -> 10;
            case KNIGHT -> 30;
            case BISHOP -> 30;
            case ROOK -> 50;
            case QUEEN -> 90;
            case KING -> 900;
        };
    }
}
