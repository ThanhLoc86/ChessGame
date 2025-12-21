package ChessClient.ChessGame.move;

public class Move {
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private final MoveType type;
    private final ChessClient.ChessGame.piece.PieceType promotionPiece; // nullable

    public Move(int fromRow, int fromCol, int toRow, int toCol, MoveType type, ChessClient.ChessGame.piece.PieceType promotionPiece) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.type = type;
        this.promotionPiece = promotionPiece;
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, MoveType type) {
        this(fromRow, fromCol, toRow, toCol, type, null);
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public int getToRow() {
        return toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public MoveType getType() {
        return type;
    }

    public ChessClient.ChessGame.piece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public String toString() {
        return String.format("%d,%d -> %d,%d (%s)", fromRow, fromCol, toRow, toCol, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move other = (Move) o;
        if (fromRow != other.fromRow) return false;
        if (fromCol != other.fromCol) return false;
        if (toRow != other.toRow) return false;
        if (toCol != other.toCol) return false;
        if (type != other.type) return false;
        if (promotionPiece == null) return other.promotionPiece == null;
        return promotionPiece.equals(other.promotionPiece);
    }

    @Override
    public int hashCode() {
        int result = fromRow;
        result = 31 * result + fromCol;
        result = 31 * result + toRow;
        result = 31 * result + toCol;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (promotionPiece != null ? promotionPiece.hashCode() : 0);
        return result;
    }
}


