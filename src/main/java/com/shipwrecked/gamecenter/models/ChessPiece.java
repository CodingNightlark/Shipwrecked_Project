package com.shipwrecked.gamecenter.models;

import java.io.Serializable;

public class ChessPiece implements Serializable {
    private PieceType type;
    private boolean isWhite;
    private boolean hasMoved;

    public ChessPiece(PieceType type, boolean isWhite) {
        this.type = type;
        this.isWhite = isWhite;
        this.hasMoved = false;
    }

    public boolean isValidMove(ChessMove move, ChessPiece[][] board) {
        int rowDiff = Math.abs(move.getToRow() - move.getFromRow());
        int colDiff = Math.abs(move.getToCol() - move.getFromCol());

        switch (type) {
            case PAWN:
                return isValidPawnMove(move, board);
            case KNIGHT:
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
            case BISHOP:
                return rowDiff == colDiff && !isPathBlocked(move, board);
            case ROOK:
                return (rowDiff == 0 || colDiff == 0) && !isPathBlocked(move, board);
            case QUEEN:
                return (rowDiff == colDiff || rowDiff == 0 || colDiff == 0) && !isPathBlocked(move, board);
            case KING:
                return rowDiff <= 1 && colDiff <= 1;
            default:
                return false;
        }
    }

    private boolean isValidPawnMove(ChessMove move, ChessPiece[][] board) {
        int direction = isWhite ? -1 : 1;
        int rowDiff = move.getToRow() - move.getFromRow();
        int colDiff = Math.abs(move.getToCol() - move.getFromCol());

        // Basic one square move
        if (colDiff == 0 && rowDiff == direction) {
            return board[move.getToRow()][move.getToCol()] == null;
        }

        // Initial two square move
        if (!hasMoved && colDiff == 0 && rowDiff == 2 * direction) {
            int middleRow = move.getFromRow() + direction;
            return board[move.getToRow()][move.getToCol()] == null && 
                   board[middleRow][move.getFromCol()] == null;
        }

        // Capture move
        if (colDiff == 1 && rowDiff == direction) {
            ChessPiece targetPiece = board[move.getToRow()][move.getToCol()];
            return targetPiece != null && targetPiece.isWhite() != this.isWhite;
        }

        return false;
    }

    private boolean isPathBlocked(ChessMove move, ChessPiece[][] board) {
        int rowDirection = Integer.compare(move.getToRow(), move.getFromRow());
        int colDirection = Integer.compare(move.getToCol(), move.getFromCol());
        
        int currentRow = move.getFromRow() + rowDirection;
        int currentCol = move.getFromCol() + colDirection;
        
        while (currentRow != move.getToRow() || currentCol != move.getToCol()) {
            if (board[currentRow][currentCol] != null) {
                return true;
            }
            currentRow += rowDirection;
            currentCol += colDirection;
        }
        
        return false;
    }

    public String getSymbol() {
        String symbol = switch (type) {
            case KING -> isWhite ? "♔" : "♚";
            case QUEEN -> isWhite ? "♕" : "♛";
            case ROOK -> isWhite ? "♖" : "♜";
            case BISHOP -> isWhite ? "♗" : "♝";
            case KNIGHT -> isWhite ? "♘" : "♞";
            case PAWN -> isWhite ? "♙" : "♟";
        };
        return symbol;
    }

    // Getters and setters
    public PieceType getType() {
        return type;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
} 