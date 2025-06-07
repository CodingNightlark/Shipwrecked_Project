import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.*;

public class GameLauncher extends JFrame {
    private Set<String> unlockedGames;
    private JPanel gamesPanel;
    private static final int WINS_TO_UNLOCK = 2; // Best of three means 2 wins

    public GameLauncher() {
        setTitle("Game Center");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        unlockedGames = new HashSet<>();
        // For testing: unlock all games
        unlockedGames.add("Tic-Tac-Toe");
        unlockedGames.add("Dinosaur Game");
        unlockedGames.add("Chess");

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Create title label
        JLabel titleLabel = new JLabel("Game Center", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create games panel
        gamesPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        gamesPanel.setBackground(new Color(240, 240, 240));
        updateGamesPanel();

        // Add scroll pane for games
        JScrollPane scrollPane = new JScrollPane(gamesPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
        pack();
        setSize(400, 500);
        setLocationRelativeTo(null);
    }

    private void updateGamesPanel() {
        gamesPanel.removeAll();

        // Add all game buttons (all unlocked for testing)
        addGameButton("Tic-Tac-Toe", true, () -> launchTicTacToe());
        addGameButton("Dinosaur Game", true, () -> launchDinoGame());
        addGameButton("Chess", true, () -> launchChess());

        gamesPanel.revalidate();
        gamesPanel.repaint();
    }

    private void addGameButton(String gameName, boolean unlocked, Runnable action) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        
        JLabel nameLabel = new JLabel(gameName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // For testing: all games show as available
        JLabel statusLabel = new JLabel("Available");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setBorder(new EmptyBorder(0, 10, 5, 10));
        statusLabel.setForeground(new Color(46, 204, 113));

        button.add(nameLabel, BorderLayout.NORTH);
        button.add(statusLabel, BorderLayout.SOUTH);
        
        button.setEnabled(true); // All buttons enabled for testing
        button.setPreferredSize(new Dimension(350, 60));
        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());
        
        gamesPanel.add(button);
    }

    private void launchTicTacToe() {
        TicTacToeGame game = new TicTacToeGame(this);
        game.setVisible(true);
        setVisible(false);
    }

    private void launchDinoGame() {
        DinoGame game = new DinoGame(this);
        game.setVisible(true);
        setVisible(false);
    }

    private void launchChess() {
        ChessGame game = new ChessGame(this);
        game.setVisible(true);
        setVisible(false);
    }

    public void checkAndUnlockGames(int playerWins) {
        // For testing: comment out the unlocking logic
        /*
        if (playerWins >= WINS_TO_UNLOCK) {
            if (!unlockedGames.contains("Dinosaur Game")) {
                unlockedGames.add("Dinosaur Game");
                JOptionPane.showMessageDialog(this,
                    "Congratulations! You've unlocked the Dinosaur Game!",
                    "New Game Unlocked!",
                    JOptionPane.INFORMATION_MESSAGE); 
            } else if (!unlockedGames.contains("Chess")) {
                unlockedGames.add("Chess");
                JOptionPane.showMessageDialog(this,
                    "Congratulations! You've unlocked Chess!",
                    "New Game Unlocked!", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            updateGamesPanel();
        }
        */ 
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            GameLauncher launcher = new GameLauncher();
            launcher.setVisible(true);
        });
    }
} 