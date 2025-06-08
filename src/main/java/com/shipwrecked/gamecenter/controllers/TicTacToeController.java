package com.shipwrecked.gamecenter.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tictactoe")
public class TicTacToeController {
    
    private final Map<String, GameState> games = new HashMap<>();
    
    @PostMapping("/new")
    public ResponseEntity<Map<String, Object>> newGame() {
        String gameId = UUID.randomUUID().toString();
        GameState game = new GameState();
        games.put(gameId, game);
        
        Map<String, Object> response = new HashMap<>();
        response.put("gameId", gameId);
        response.put("board", game.getBoard());
        response.put("currentPlayer", game.getCurrentPlayer());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{gameId}/move")
    public ResponseEntity<Map<String, Object>> makeMove(
            @PathVariable String gameId,
            @RequestParam int position) {
        
        GameState game = games.get(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!game.isValidMove(position)) {
            return ResponseEntity.badRequest().build();
        }
        
        game.makeMove(position);
        
        Map<String, Object> response = new HashMap<>();
        response.put("board", game.getBoard());
        response.put("currentPlayer", game.getCurrentPlayer());
        response.put("winner", game.getWinner());
        response.put("draw", game.isDraw());
        
        return ResponseEntity.ok(response);
    }
    
    private static class GameState {
        private final String[] board;
        private String currentPlayer;
        private String winner;
        private boolean draw;
        
        public GameState() {
            this.board = new String[9];
            this.currentPlayer = "X";
            this.winner = null;
            this.draw = false;
        }
        
        public boolean isValidMove(int position) {
            return position >= 0 && position < 9 && board[position] == null && winner == null && !draw;
        }
        
        public void makeMove(int position) {
            board[position] = currentPlayer;
            checkWin();
            if (!draw && winner == null) {
                currentPlayer = currentPlayer.equals("X") ? "O" : "X";
            }
        }
        
        private void checkWin() {
            int[][] winPatterns = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Columns
                {0, 4, 8}, {2, 4, 6}             // Diagonals
            };
            
            for (int[] pattern : winPatterns) {
                if (board[pattern[0]] != null &&
                    board[pattern[0]].equals(board[pattern[1]]) &&
                    board[pattern[0]].equals(board[pattern[2]])) {
                    winner = board[pattern[0]];
                    return;
                }
            }
            
            // Check for draw
            boolean allFilled = true;
            for (String cell : board) {
                if (cell == null) {
                    allFilled = false;
                    break;
                }
            }
            draw = allFilled && winner == null;
        }
        
        public String[] getBoard() {
            return board;
        }
        
        public String getCurrentPlayer() {
            return currentPlayer;
        }
        
        public String getWinner() {
            return winner;
        }
        
        public boolean isDraw() {
            return draw;
        }
    }
} 