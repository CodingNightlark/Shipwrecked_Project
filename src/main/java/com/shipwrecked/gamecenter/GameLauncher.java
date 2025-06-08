package com.shipwrecked.gamecenter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.*;
import java.io.File;
import com.shipwrecked.gamecenter.games.TicTacToeGame;
import com.shipwrecked.gamecenter.games.ChessGame;

public class GameLauncher extends JFrame {
    private Set<String> unlockedGames;
    private JPanel gamesPanel;
    private static final int WINS_TO_UNLOCK = 2;

    // Fun color scheme
    private static final Color BACKGROUND_COLOR = new Color(108, 99, 255);  // Playful purple
    private static final Color BUTTON_COLOR = new Color(255, 122, 89);      // Coral
    private static final Color BUTTON_HOVER_COLOR = new Color(255, 145, 118); // Light coral
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color TITLE_COLOR = Color.BLACK;
    private static final Color LOCKED_COLOR = new Color(180, 180, 180);     // Gray for locked games

    // Custom fonts
    private Font customFont;
    private Font titleFont;
    private Font buttonFont;
    private Font statusFont;

    public GameLauncher() {
        setTitle("Game Center");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(400, 500));

        // Initialize custom fonts
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, 
                new File("resources/ComicNeue-Bold.ttf"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            
            titleFont = customFont.deriveFont(Font.BOLD, 36f);
            buttonFont = customFont.deriveFont(Font.BOLD, 18f);
            statusFont = customFont.deriveFont(Font.PLAIN, 14f);
        } catch (Exception e) {
            titleFont = new Font("Comic Sans MS", Font.BOLD, 36);
            buttonFont = new Font("Comic Sans MS", Font.BOLD, 18);
            statusFont = new Font("Comic Sans MS", Font.PLAIN, 14);
        }

        unlockedGames = new HashSet<>();
        unlockedGames.add("Tic-Tac-Toe");
        unlockedGames.add("Chess");

        // Create main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(
                    0, 0, BACKGROUND_COLOR,
                    w, h, new Color(147, 141, 255)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create title label with black color
        JLabel titleLabel = new JLabel("Game Center", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create games panel with better layout for resizing
        gamesPanel = new JPanel();
        gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
        gamesPanel.setOpaque(false);
        updateGamesPanel();

        // Add scroll pane for games
        JScrollPane scrollPane = new JScrollPane(gamesPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
        pack();
        setSize(400, 500);
        setLocationRelativeTo(null);
    }

    private void updateGamesPanel() {
        gamesPanel.removeAll();
        gamesPanel.add(Box.createVerticalStrut(10));
        addGameButton("Tic-Tac-Toe", true, () -> launchTicTacToe());
        gamesPanel.add(Box.createVerticalStrut(15));
        addGameButton("Chess", true, () -> launchChess());
        gamesPanel.add(Box.createVerticalStrut(10));
        gamesPanel.revalidate();
        gamesPanel.repaint();
    }

    private void addGameButton(String gameName, boolean unlocked, Runnable action) {
        JPanel buttonPanel = new JPanel(new BorderLayout(5, 5));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(BUTTON_HOVER_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(BUTTON_HOVER_COLOR);
                } else {
                    g2d.setColor(BUTTON_COLOR);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                GradientPaint shine = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 50),
                    0, getHeight(), new Color(255, 255, 255, 0)
                );
                g2d.setPaint(shine);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        button.setLayout(new BorderLayout());
        button.setBorder(new EmptyBorder(15, 20, 15, 20));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel nameLabel = new JLabel(gameName);
        nameLabel.setFont(buttonFont);
        nameLabel.setForeground(TEXT_COLOR);
        
        JLabel statusLabel = new JLabel("Available");
        statusLabel.setFont(statusFont);
        statusLabel.setForeground(TEXT_COLOR);
        
        String iconText = switch (gameName) {
            case "Tic-Tac-Toe" -> "❌⭕";
            case "Chess" -> "♟️";
            default -> "🎮";
        };
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(buttonFont);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(nameLabel);
        textPanel.add(statusLabel);
        
        button.add(iconLabel, BorderLayout.WEST);
        button.add(textPanel, BorderLayout.CENTER);
        
        button.setEnabled(true);
        button.addActionListener(e -> action.run());
        
        buttonPanel.add(button, BorderLayout.CENTER);
        gamesPanel.add(buttonPanel);
    }

    private void launchTicTacToe() {
        TicTacToeGame game = new TicTacToeGame(this);
        game.setVisible(true);
        setVisible(false);
    }

    private void launchChess() {
        ChessGame game = new ChessGame(this);
        game.setVisible(true);
        setVisible(false);
    }

    public void checkAndUnlockGames(int playerWins) {
        if (playerWins >= WINS_TO_UNLOCK) {
            unlockedGames.add("Chess");
            updateGamesPanel();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameLauncher launcher = new GameLauncher();
            launcher.setVisible(true);
        });
    }
} 