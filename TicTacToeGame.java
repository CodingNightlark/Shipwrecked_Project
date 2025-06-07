import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

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
 
    public TicTacToeGame(GameLauncher launcher) {
        this.launcher = launcher;
        setTitle("Tic-Tac-Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Create back button
        backButton = new JButton("← Back to Game Center");
        backButton.setFont(new Font("Arial", Font.BOLD, 12));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> returnToLauncher());
        mainPanel.add(backButton, BorderLayout.NORTH);

        // Initialize game board
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        boardPanel.setBackground(new Color(240, 240, 240));
        buttons = new JButton[3][3];

        // Create and style game buttons
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 40));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].setBackground(Color.WHITE);
                
                final int row = i;
                final int col = j;
                buttons[i][j].addActionListener(e -> handlePlayerMove(row, col));
                boardPanel.add(buttons[i][j]);
            }
        }

        // Create status panel
        JPanel statusPanel = new JPanel(new BorderLayout(10, 10));
        statusPanel.setBackground(new Color(240, 240, 240));
        statusLabel = new JLabel("Your turn! Make a move.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel = new JLabel("Player: 0 | Computer: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Create new game button
        JButton newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        newGameButton.setFocusPainted(false);
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

    private void returnToLauncher() {
        launcher.setVisible(true);
        launcher.checkAndUnlockGames(playerScore);
        dispose();
    }

    private void handlePlayerMove(int row, int col) {
        if (gameOver || !buttons[row][col].getText().isEmpty()) {
            return;
        }
 
        // Player's move
        buttons[row][col].setText(PLAYER_SYMBOL);
        buttons[row][col].setForeground(new Color(41, 128, 185)); // Blue for player

        if (checkWinner(PLAYER_SYMBOL)) {
            gameOver = true;
            playerScore++;
            updateScore();
            statusLabel.setText("You won! 🎉");
            return;
        }

        if (isBoardFull()) {
            gameOver = true;
            statusLabel.setText("It's a draw! 🤝"); 
            return;
        }

        // Computer's move
        statusLabel.setText("Computer is thinking...");
        SwingUtilities.invokeLater(() -> {
            makeComputerMove();
            if (checkWinner(COMPUTER_SYMBOL)) {
                gameOver = true;
                computerScore++;
                updateScore();
                statusLabel.setText("Computer won! 😔");
            } else if (isBoardFull()) {
                gameOver = true;
                statusLabel.setText("It's a draw! 🤝");
            } else {
                statusLabel.setText("Your turn! Make a move.");
            }
        });
    }

    private void makeComputerMove() {
        // Try to win
        if (findWinningMove(COMPUTER_SYMBOL)) {
            return;
        }

        // Block player's winning move
        if (findWinningMove(PLAYER_SYMBOL)) {
            return;
        }

        // Try to take center
        if (buttons[1][1].getText().isEmpty()) {
            makeMove(1, 1);
            return;
        }

        // Take any corner
        int[][] corners = {{0,0}, {0,2}, {2,0}, {2,2}};
        for (int[] corner : corners) {
            if (buttons[corner[0]][corner[1]].getText().isEmpty()) {
                makeMove(corner[0], corner[1]);
                return;
            }
        }

        // Take any available side
        int[][] sides = {{0,1}, {1,0}, {1,2}, {2,1}};
        for (int[] side : sides) {
            if (buttons[side[0]][side[1]].getText().isEmpty()) {
                makeMove(side[0], side[1]);
                return;
            }
        }
    }

    private boolean findWinningMove(String symbol) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().isEmpty()) {
                    buttons[i][j].setText(symbol);
                    if (checkWinner(symbol)) {
                        if (symbol.equals(COMPUTER_SYMBOL)) {
                            buttons[i][j].setForeground(new Color(231, 76, 60)); // Red for computer
                            return true;
                        }
                        buttons[i][j].setText("");
                        makeMove(i, j);
                        return true;
                    }
                    buttons[i][j].setText("");
                }
            }
        }
        return false;
    }

    private void makeMove(int row, int col) {
        buttons[row][col].setText(COMPUTER_SYMBOL);
        buttons[row][col].setForeground(new Color(231, 76, 60)); // Red for computer
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