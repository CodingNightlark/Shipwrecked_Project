package com.shipwrecked.gamecenter.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameCenterController {
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/tictactoe")
    public String tictactoe() {
        return "tictactoe";
    }
} 