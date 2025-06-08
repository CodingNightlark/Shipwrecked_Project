package com.shipwrecked.gamecenter.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChessGame implements Serializable {
    private ChessPiece[][] board;
    private boolean isWhiteTurn;
    private List<ChessMove> moveHistory;
    private GameStatus status;
    private String winner;

    public ChessGame() {
        this.board = new ChessPiece[8][8];
        this.isWhiteTurn = true;
        this.moveHistory = new ArrayList<>();
        this.status = GameStatus.ACTIVE;
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialize black pieces
        board[0][0] = new ChessPiece(PieceType.ROOK, false);
        board[0][1] = new ChessPiece(PieceType.KNIGHT, false);
        board[0][2] = new ChessPiece(PieceType.BISHOP, false);
        board[0][3] = new ChessPiece(PieceType.QUEEN, false);
        board[0][4] = new ChessPiece(PieceType.KING, false);
        board[0][5] = new ChessPiece(PieceType.BISHOP, false);
        board[0][6] = new ChessPiece(PieceType.KNIGHT, false);
        board[0][7] = new ChessPiece(PieceType.ROOK, false);

        // Black pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = new ChessPiece(PieceType.PAWN, false);
        }

        // Initialize white pieces
        board[7][0] = new ChessPiece(PieceType.ROOK, true);
        board[7][1] = new ChessPiece(PieceType.KNIGHT, true);
        board[7][2] = new ChessPiece(PieceType.BISHOP, true);
        board[7][3] = new ChessPiece(PieceType.QUEEN, true);
        board[7][4] = new ChessPiece(PieceType.KING, true);
        board[7][5] = new ChessPiece(PieceType.BISHOP, true);
        board[7][6] = new ChessPiece(PieceType.KNIGHT, true);
        board[7][7] = new ChessPiece(PieceType.ROOK, true);

        // White pawns
        for (int i = 0; i < 8; i++) {
            board[6][i] = new ChessPiece(PieceType.PAWN, true);
        }
    }

    public boolean isValidMove(ChessMove move) {
        if (move == null || !isValidPosition(move.getFromRow(), move.getFromCol()) || 
            !isValidPosition(move.getToRow(), move.getToCol())) {
            return false;
        }

        ChessPiece piece = board[move.getFromRow()][move.getFromCol()];
        if (piece == null || piece.isWhite() != isWhiteTurn) {
            return false;
        }

        return piece.isValidMove(move, board);
    }

    public void makeMove(ChessMove move) {
        if (!isValidMove(move)) {
            return;
        }

        ChessPiece piece = board[move.getFromRow()][move.getFromCol()];
        ChessPiece capturedPiece = board[move.getToRow()][move.getToCol()];

        // Make the move
        board[move.getToRow()][move.getToCol()] = piece;
        board[move.getFromRow()][move.getFromCol()] = null;

        // Handle pawn promotion
        if (piece.getType() == PieceType.PAWN && (move.getToRow() == 0 || move.getToRow() == 7)) {
            board[move.getToRow()][move.getToCol()] = new ChessPiece(PieceType.QUEEN, piece.isWhite());
        }

        // Check if a king was captured
        if (capturedPiece != null && capturedPiece.getType() == PieceType.KING) {
            this.status = GameStatus.FINISHED;
            this.winner = isWhiteTurn ? "White" : "Black";
        }

        moveHistory.add(move);
        isWhiteTurn = !isWhiteTurn;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    // Getters
    public ChessPiece[][] getBoard() {
        return board;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public List<ChessMove> getMoveHistory() {
        return moveHistory;
    }

    public GameStatus getStatus() {
        return status;
    }

    public String getWinner() {
        return winner;
    }
} 