package com.shipwrecked.gamecenter.controllers;

import com.shipwrecked.gamecenter.models.ChessGame;
import com.shipwrecked.gamecenter.models.ChessMove;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/chess")
public class ChessController {
    
    @GetMapping
    public String chess(Model model, HttpSession session) {
        ChessGame game = (ChessGame) session.getAttribute("chessGame");
        if (game == null) {
            game = new ChessGame();
            session.setAttribute("chessGame", game);
        }
        model.addAttribute("game", game);
        return "chess";
    }

    @PostMapping("/move")
    @ResponseBody
    public ChessGame makeMove(@RequestBody ChessMove move, HttpSession session) {
        ChessGame game = (ChessGame) session.getAttribute("chessGame");
        if (game != null && game.isValidMove(move)) {
            game.makeMove(move);
            session.setAttribute("chessGame", game);
        }
        return game;
    }

    @PostMapping("/reset")
    @ResponseBody
    public ChessGame resetGame(HttpSession session) {
        ChessGame game = new ChessGame();
        session.setAttribute("chessGame", game);
        return game;
    }
} 