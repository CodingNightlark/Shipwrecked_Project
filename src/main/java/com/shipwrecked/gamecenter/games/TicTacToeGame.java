package com.shipwrecked.gamecenter.games;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.awt.Point;
import com.shipwrecked.gamecenter.GameLauncher;

public class TicTacToeGame extends JFrame {
    private JButton[][] buttons;
    private JLabel statusLabel;
    private JLabel scoreLabel; 
    private int playerScore = 0;
    private int computerScore = 0;
    private static final String PLAYER_SYMBOL = "X";
    private static final String COMPUTER_SYMBOL = "O"; 
    private boolean gameOver;
    private GameLauncher launcher;  
    private JButton backButton;

    // Fun color scheme
    private static final Color BACKGROUND_COLOR = new Color(255, 223, 186);  // Soft peach
    private static final Color BUTTON_COLOR = new Color(255, 255, 255);      // White
    private static final Color HOVER_COLOR = new Color(255, 240, 220);       // Light peach
    private static final Color PLAYER_COLOR = new Color(255, 107, 107);      // Coral red
    private static final Color COMPUTER_COLOR = new Color(79, 187, 255);     // Sky blue
    private static final Color TEXT_COLOR = new Color(51, 51, 51);          // Dark gray
    private static final Font GAME_FONT = new Font("Arial", Font.BOLD, 48);
    private static final Font STATUS_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Font SCORE_FONT = new Font("Arial", Font.BOLD, 16);

    public TicTacToeGame(GameLauncher launcher) {
        this.launcher = launcher;
        setTitle("Tic-Tac-Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Create main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                    0, 0, BACKGROUND_COLOR,
                    w, h, new Color(255, 233, 206)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create modern back button
        backButton = new JButton("← Back to Game Center") {
            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setOpaque(true);
                setBackground(new Color(52, 152, 219));
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 14));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        backButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                backButton.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(MouseEvent e) {
                backButton.setBackground(new Color(52, 152, 219));
            }
        });
        backButton.addActionListener(e -> returnToLauncher());
        mainPanel.add(backButton, BorderLayout.NORTH);

        // Initialize game board with modern styling
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        boardPanel.setOpaque(false);
        buttons = new JButton[3][3];

        // Create and style game buttons
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = createGameButton();
                final int row = i;
                final int col = j;
                buttons[i][j].addActionListener(e -> handlePlayerMove(row, col));
                boardPanel.add(buttons[i][j]);
            }
        }

        // Create status panel with modern design
        JPanel statusPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setColor(new Color(255, 255, 255, 180));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Style the status label
        statusLabel = new JLabel("Your turn! Make a move.", SwingConstants.CENTER);
        statusLabel.setFont(STATUS_FONT);
        statusLabel.setForeground(TEXT_COLOR);

        // Style the score label
        scoreLabel = new JLabel("Player: 0 | Computer: 0", SwingConstants.CENTER);
        scoreLabel.setFont(SCORE_FONT);
        scoreLabel.setForeground(TEXT_COLOR);

        // Create modern new game button
        JButton newGameButton = new JButton("New Game") {
            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setOpaque(true);
                setBackground(new Color(46, 204, 113));
                setForeground(Color.WHITE);
                setFont(new Font("Arial", Font.BOLD, 16));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2d.dispose();
            }
        };
        newGameButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                newGameButton.setBackground(new Color(39, 174, 96));
            }
            public void mouseExited(MouseEvent e) {
                newGameButton.setBackground(new Color(46, 204, 113));
            }
        });
        newGameButton.addActionListener(e -> resetGame());

        // Add components to status panel
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(scoreLabel, BorderLayout.CENTER);
        statusPanel.add(newGameButton, BorderLayout.SOUTH);

        // Add panels to main panel
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);
        pack();
        setSize(400, 500);
        setLocationRelativeTo(null);
    }

    private JButton createGameButton() {
        JButton button = new JButton("") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw button background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Draw symbol with animation
                String symbol = getText();
                if (!symbol.isEmpty()) {
                    g2d.setFont(GAME_FONT);
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(symbol)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    
                    if (symbol.equals(PLAYER_SYMBOL)) {
                        g2d.setColor(PLAYER_COLOR);
                    } else {
                        g2d.setColor(COMPUTER_COLOR);
                    }
                    g2d.drawString(symbol, x, y);
                }
                
                g2d.dispose();
            }
        };
        
        button.setPreferredSize(new Dimension(100, 100));
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (button.getText().isEmpty() && !gameOver) {
                    button.setBackground(HOVER_COLOR);
                }
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_COLOR);
            }
        });
        
        return button;
    }

    private void returnToLauncher() {
        launcher.setVisible(true);
        launcher.checkAndUnlockGames(playerScore);
        dispose();
    }

    private void handlePlayerMove(int row, int col) {
        if (!gameOver && buttons[row][col].getText().isEmpty()) {
            buttons[row][col].setText(PLAYER_SYMBOL);
            
            if (checkWinner(PLAYER_SYMBOL)) {
                gameOver = true;
                playerScore++;
                updateScore();
                showWinningAnimation(PLAYER_SYMBOL);
            } else if (isBoardFull()) {
                gameOver = true;
                statusLabel.setText("It's a tie!");
            } else {
                makeComputerMove();
            }
        }
    }

    private void makeComputerMove() {
        statusLabel.setText("Computer's turn...");
        
        // Add a small delay for better UX
        new javax.swing.Timer(500, e -> {
            // 70% chance of making a strategic move, 30% chance of random move
            boolean makeStrategicMove = new Random().nextDouble() < 0.7;
            
            if (makeStrategicMove) {
                // Try to win
                if (tryWinningMove()) {
                    ((javax.swing.Timer)e.getSource()).stop();
                    return;
                }
                
                // Block player's winning move
                if (tryBlockingMove()) {
                    ((javax.swing.Timer)e.getSource()).stop();
                    return;
                }
            }
            
            // Get all available moves
            java.util.List<Point> availableMoves = new ArrayList<>();
            
            // Prioritize center and corners
            Point center = new Point(1, 1);
            java.util.List<Point> corners = Arrays.asList(
                new Point(0, 0), new Point(0, 2),
                new Point(2, 0), new Point(2, 2)
            );
            java.util.List<Point> sides = Arrays.asList(
                new Point(0, 1), new Point(1, 0),
                new Point(1, 2), new Point(2, 1)
            );
            
            // Add moves in priority order
            if (buttons[1][1].getText().isEmpty()) availableMoves.add(center);
            for (Point corner : corners) {
                if (buttons[corner.x][corner.y].getText().isEmpty()) {
                    availableMoves.add(corner);
                }
            }
            for (Point side : sides) {
                if (buttons[side.x][side.y].getText().isEmpty()) {
                    availableMoves.add(side);
                }
            }
            
            if (!availableMoves.isEmpty()) {
                // If making a strategic move, prefer moves from the start of the list
                // If making a random move, choose from anywhere in the list
                Point move;
                if (makeStrategicMove && availableMoves.size() > 1) {
                    // Choose from first two available moves
                    move = availableMoves.get(new Random().nextInt(Math.min(2, availableMoves.size())));
                } else {
                    // Choose completely randomly
                    move = availableMoves.get(new Random().nextInt(availableMoves.size()));
                }
                
                buttons[move.x][move.y].setText(COMPUTER_SYMBOL);
                
                if (checkWinner(COMPUTER_SYMBOL)) {
                    gameOver = true;
                    computerScore++;
                    updateScore();
                    showWinningAnimation(COMPUTER_SYMBOL);
                } else if (isBoardFull()) {
                    gameOver = true;
                    statusLabel.setText("It's a tie!");
                } else {
                    statusLabel.setText("Your turn! Make a move.");
                }
            }
            ((javax.swing.Timer)e.getSource()).stop();
        }).start();
    }

    private boolean tryWinningMove() {
        // Try each empty cell
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().isEmpty()) {
                    // Try the move
                    buttons[i][j].setText(COMPUTER_SYMBOL);
                    if (checkWinner(COMPUTER_SYMBOL)) {
                        // Keep the winning move
                        if (checkWinner(COMPUTER_SYMBOL)) {
                            gameOver = true;
                            computerScore++;
                            updateScore();
                            showWinningAnimation(COMPUTER_SYMBOL);
                        }
                        return true;
                    }
                    // Undo the move
                    buttons[i][j].setText("");
                }
            }
        }
        return false;
    }

    private boolean tryBlockingMove() {
        // Try each empty cell
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().isEmpty()) {
                    // Try the move as the player
                    buttons[i][j].setText(PLAYER_SYMBOL);
                    if (checkWinner(PLAYER_SYMBOL)) {
                        // Block the winning move
                        buttons[i][j].setText(COMPUTER_SYMBOL);
                        return true;
                    }
                    // Undo the move
                    buttons[i][j].setText("");
                }
            }
        }
        return false;
    }

    private void showWinningAnimation(String winner) {
        // Create a list of winning buttons
        java.util.List<JButton> winningButtons = new ArrayList<>();
        
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(winner) &&
                buttons[i][1].getText().equals(winner) &&
                buttons[i][2].getText().equals(winner)) {
                winningButtons.add(buttons[i][0]);
                winningButtons.add(buttons[i][1]);
                winningButtons.add(buttons[i][2]);
                break;
            }
        }
        
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (buttons[0][i].getText().equals(winner) &&
                buttons[1][i].getText().equals(winner) &&
                buttons[2][i].getText().equals(winner)) {
                winningButtons.add(buttons[0][i]);
                winningButtons.add(buttons[1][i]);
                winningButtons.add(buttons[2][i]);
                break;
            }
        }
        
        // Check diagonals
        if (buttons[0][0].getText().equals(winner) &&
            buttons[1][1].getText().equals(winner) &&
            buttons[2][2].getText().equals(winner)) {
            winningButtons.add(buttons[0][0]);
            winningButtons.add(buttons[1][1]);
            winningButtons.add(buttons[2][2]);
        }
        
        if (buttons[0][2].getText().equals(winner) &&
            buttons[1][1].getText().equals(winner) &&
            buttons[2][0].getText().equals(winner)) {
            winningButtons.add(buttons[0][2]);
            winningButtons.add(buttons[1][1]);
            winningButtons.add(buttons[2][0]);
        }

        // Animate winning buttons
        if (!winningButtons.isEmpty()) {
            javax.swing.Timer winTimer = new javax.swing.Timer(100, new ActionListener() {
                private int count = 0;
                private final Color originalColor = winner.equals(PLAYER_SYMBOL) ? PLAYER_COLOR : COMPUTER_COLOR;
                private final Color highlightColor = new Color(255, 215, 0); // Gold color

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (count < 6) { // Flash 3 times
                        for (JButton button : winningButtons) {
                            if (count % 2 == 0) {
                                // Scale up and change color
                                button.setFont(new Font(GAME_FONT.getFamily(), Font.BOLD, GAME_FONT.getSize() + 4));
                                button.setForeground(highlightColor);
                            } else {
                                // Scale down and restore color
                                button.setFont(GAME_FONT);
                                button.setForeground(originalColor);
                            }
                        }
                        count++;
                    } else {
                        ((javax.swing.Timer)e.getSource()).stop();
                        // Show game over dialog after animation
                        showGameOverDialog(winner);
                    }
                }
            });
            winTimer.start();
        }
    }

    private void showGameOverDialog(String winner) {
        String message = winner.equals(PLAYER_SYMBOL) ? "Congratulations! You won!" : "Computer wins!";
        String title = "Game Over";

        // Create custom dialog
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setUndecorated(true);

        // Create content panel with gradient background
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                    0, 0, winner.equals(PLAYER_SYMBOL) ? PLAYER_COLOR : COMPUTER_COLOR,
                    w, h, Color.WHITE
                );
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, w, h, 20, 20);
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setOpaque(false);

        // Add message
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        messageLabel.setForeground(Color.WHITE);
        panel.add(messageLabel, BorderLayout.CENTER);

        // Add buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonsPanel.setOpaque(false);

        // Create styled button
        JButton okButton = new JButton("OK") {
            {
                setPreferredSize(new Dimension(100, 40));
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setFont(new Font("Arial", Font.BOLD, 16));
                setForeground(winner.equals(PLAYER_SYMBOL) ? PLAYER_COLOR : COMPUTER_COLOR);
                setBackground(Color.WHITE);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        okButton.addActionListener(e -> dialog.dispose());
        buttonsPanel.add(okButton);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean checkWinner(String symbol) {
        // Check rows, columns and diagonals
        for (int i = 0; i < 3; i++) {
            if ((buttons[i][0].getText().equals(symbol) && 
                 buttons[i][1].getText().equals(symbol) && 
                 buttons[i][2].getText().equals(symbol)) ||
                (buttons[0][i].getText().equals(symbol) && 
                 buttons[1][i].getText().equals(symbol) && 
                 buttons[2][i].getText().equals(symbol))) {
                return true;
            }
        }
        return (buttons[0][0].getText().equals(symbol) && 
                buttons[1][1].getText().equals(symbol) && 
                buttons[2][2].getText().equals(symbol)) ||
               (buttons[0][2].getText().equals(symbol) && 
                buttons[1][1].getText().equals(symbol) && 
                buttons[2][0].getText().equals(symbol));
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateScore() {
        scoreLabel.setText(String.format("Player: %d | Computer: %d", playerScore, computerScore));
        if (playerScore >= 2) {
            launcher.checkAndUnlockGames(playerScore);
        }
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
        gameOver = false;
        statusLabel.setText("Your turn! Make a move.");
    }
} 