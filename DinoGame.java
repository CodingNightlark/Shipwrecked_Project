import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.Random;

public class DinoGame extends JFrame {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 400;
    private static final int GROUND_Y = 300;
    private static final int DINO_X = 100;
    private static final int OBSTACLE_WIDTH = 30;
    private static final int JUMP_VELOCITY = -15;
    private static final int GRAVITY = 1;
    private static final Color SKY_COLOR = new Color(135, 206, 235);
    private static final Color GROUND_COLOR = new Color(139, 69, 19);
    private static final Color DINO_COLOR = new Color(50, 205, 50);
    private static final Color DINO_EYE_COLOR = new Color(255, 255, 255);
    private static final Color DINO_PUPIL_COLOR = new Color(33, 33, 33);
    private static final Color OBSTACLE_COLOR = new Color(34, 139, 34);
    private static final Color SCORE_COLOR = new Color(44, 62, 80);
    private static final Font SCORE_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font GAME_OVER_FONT = new Font("Arial", Font.BOLD, 48);

    private int dinoY = GROUND_Y;
    private int dinoVelocity = 0;
    private boolean isJumping = false;
    private ArrayList<Rectangle> obstacles = new ArrayList<>();
    private Timer gameTimer;
    private int score = 0;
    private boolean gameOver = false;
    private GameLauncher launcher;
    private Random random = new Random();
    private Color[] cloudColors = {
        new Color(255, 255, 255, 200),
        new Color(245, 245, 245, 180),
        new Color(240, 240, 240, 160)
    };
    private ArrayList<Cloud> clouds = new ArrayList<>();

    private static class Cloud {
        int x, y;
        int width, height;
        Color color;
        
        Cloud(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.width = 40 + new Random().nextInt(60);
            this.height = 20 + new Random().nextInt(30);
            this.color = color;
        }
    }

    public DinoGame(GameLauncher launcher) {
        this.launcher = launcher;
        setTitle("Dino Jump Game");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // Add modern back button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        
        JButton backButton = new JButton("← Back to Menu") {
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
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
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
        backButton.setPreferredSize(new Dimension(150, 35));
        topPanel.add(backButton);

        // Create game panel
        JPanel gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw sky gradient
                GradientPaint skyGradient = new GradientPaint(
                    0, 0, SKY_COLOR,
                    0, GROUND_Y, new Color(173, 216, 230)
                );
                g2d.setPaint(skyGradient);
                g2d.fillRect(0, 0, getWidth(), GROUND_Y);

                // Draw clouds
                for (Cloud cloud : clouds) {
                    g2d.setColor(cloud.color);
                    g2d.fillOval(cloud.x, cloud.y, cloud.width, cloud.height);
                }

                // Draw ground with texture
                g2d.setColor(GROUND_COLOR);
                g2d.fillRect(0, GROUND_Y, getWidth(), getHeight() - GROUND_Y);
                g2d.setColor(new Color(101, 67, 33));
                for (int x = 0; x < getWidth(); x += 30) {
                    g2d.drawLine(x, GROUND_Y, x + 15, GROUND_Y);
                }

                // Draw dino with shadow and details
                drawDino(g2d);

                // Draw obstacles with shadow
                for (Rectangle obstacle : obstacles) {
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.fillRoundRect(obstacle.x + 3, obstacle.y + 3, 
                                    obstacle.width, obstacle.height, 8, 8);
                    g2d.setColor(OBSTACLE_COLOR);
                    g2d.fillRoundRect(obstacle.x, obstacle.y, 
                                    obstacle.width, obstacle.height, 8, 8);
                }

                // Draw score
                g2d.setFont(SCORE_FONT);
                g2d.setColor(SCORE_COLOR);
                g2d.drawString("Score: " + score, WINDOW_WIDTH - 150, 40);

                // Draw game over message
                if (gameOver) {
                    g2d.setFont(GAME_OVER_FONT);
                    String gameOverText = "Game Over!";
                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth(gameOverText)) / 2;
                    int textY = getHeight() / 2;
                    
                    // Draw text shadow
                    g2d.setColor(new Color(0, 0, 0, 100));
                    g2d.drawString(gameOverText, textX + 2, textY + 2);
                    
                    // Draw main text
                    g2d.setColor(new Color(192, 57, 43));
                    g2d.drawString(gameOverText, textX, textY);
                    
                    // Draw restart instruction
                    g2d.setFont(new Font("Arial", Font.BOLD, 20));
                    String restartText = "Press SPACE to restart";
                    fm = g2d.getFontMetrics();
                    textX = (getWidth() - fm.stringWidth(restartText)) / 2;
                    g2d.setColor(new Color(44, 62, 80));
                    g2d.drawString(restartText, textX, textY + 40);
                }
            }
        };
        gamePanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        // Layout setup
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);

        // Initialize clouds
        for (int i = 0; i < 5; i++) {
            addNewCloud();
        }

        // Game controls
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (gameOver) {
                        resetGame();
                    } else if (!isJumping) {
                        isJumping = true;
                        dinoVelocity = JUMP_VELOCITY;
                    }
                }
            }
        });

        // Game timer
        gameTimer = new Timer(16, e -> {
            if (!gameOver) {
                updateGame();
                gamePanel.repaint();
            }
        });
        gameTimer.start();

        setFocusable(true);
        pack();
    }

    private void addNewCloud() {
        int x = random.nextInt(WINDOW_WIDTH);
        int y = random.nextInt(GROUND_Y - 100);
        clouds.add(new Cloud(x, y, cloudColors[random.nextInt(cloudColors.length)]));
    }

    private void updateGame() {
        // Update dino position
        if (isJumping) {
            dinoY += dinoVelocity;
            dinoVelocity += GRAVITY;
            
            if (dinoY >= GROUND_Y) {
                dinoY = GROUND_Y;
                isJumping = false;
                dinoVelocity = 0;
            }
        }

        // Update clouds
        for (Cloud cloud : clouds) {
            cloud.x -= 1;
            if (cloud.x + cloud.width < 0) {
                cloud.x = WINDOW_WIDTH;
                cloud.y = random.nextInt(GROUND_Y - 100);
            }
        }

        // Update obstacles
        for (Rectangle obstacle : obstacles) {
            obstacle.x -= 5;
        }
        
        // Remove off-screen obstacles
        obstacles.removeIf(obstacle -> obstacle.x + obstacle.width < 0);

        // Add new obstacles
        if (obstacles.isEmpty() || 
            obstacles.get(obstacles.size() - 1).x < WINDOW_WIDTH - 300) {
            int height = 30 + random.nextInt(50);
            obstacles.add(new Rectangle(
                WINDOW_WIDTH,
                GROUND_Y - height + 50,
                OBSTACLE_WIDTH,
                height
            ));
        }

        // Check collisions
        Rectangle dinoRect = new Rectangle(DINO_X, dinoY, 40, 50);
        for (Rectangle obstacle : obstacles) {
            if (dinoRect.intersects(obstacle)) {
                gameOver = true;
                gameTimer.stop();
                break;
            }
        }

        // Update score
        score++;
    }

    private void resetGame() {
        dinoY = GROUND_Y;
        dinoVelocity = 0;
        isJumping = false;
        obstacles.clear();
        score = 0;
        gameOver = false;
        gameTimer.start();
    }

    private void returnToLauncher() {
        gameTimer.stop();
        dispose();
        launcher.setVisible(true);
    }

    private void drawDino(Graphics2D g2d) {
        // Shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(DINO_X + 5, dinoY + 5, 40, 50, 10, 10);

        // Body
        g2d.setColor(DINO_COLOR);
        g2d.fillRoundRect(DINO_X, dinoY, 40, 50, 10, 10);

        // Neck and head
        g2d.fillRoundRect(DINO_X + 25, dinoY - 15, 20, 25, 8, 8);
        g2d.fillOval(DINO_X + 35, dinoY - 25, 25, 25);

        // Eye
        g2d.setColor(DINO_EYE_COLOR);
        g2d.fillOval(DINO_X + 45, dinoY - 20, 10, 10);
        g2d.setColor(DINO_PUPIL_COLOR);
        g2d.fillOval(DINO_X + 48, dinoY - 18, 4, 4);

        // Smile
        g2d.setColor(DINO_PUPIL_COLOR);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawArc(DINO_X + 45, dinoY - 15, 12, 8, 180, 180);

        // Spikes on back
        int[] spikeX = new int[]{
            DINO_X + 30, DINO_X + 25, DINO_X + 20, 
            DINO_X + 15, DINO_X + 10
        };
        int[] spikeTopY = new int[]{
            dinoY - 5, dinoY - 8, dinoY - 10, 
            dinoY - 8, dinoY - 5
        };
        for (int i = 0; i < spikeX.length; i++) {
            g2d.setColor(new Color(34, 177, 76));
            int[] xPoints = {
                spikeX[i] - 5, spikeX[i], spikeX[i] + 5
            };
            int[] yPoints = {
                dinoY + 5, spikeTopY[i], dinoY + 5
            };
            g2d.fillPolygon(xPoints, yPoints, 3);
        }

        // Legs
        if (!isJumping) {
            long currentTime = System.currentTimeMillis();
            boolean isLeftLegForward = (currentTime / 200) % 2 == 0;
            
            // Left leg
            g2d.setColor(DINO_COLOR);
            if (isLeftLegForward) {
                g2d.fillRoundRect(DINO_X + 5, dinoY + 45, 12, 20, 6, 6);
            } else {
                g2d.fillRoundRect(DINO_X + 5, dinoY + 45, 12, 15, 6, 6);
            }
            
            // Right leg
            if (!isLeftLegForward) {
                g2d.fillRoundRect(DINO_X + 25, dinoY + 45, 12, 20, 6, 6);
            } else {
                g2d.fillRoundRect(DINO_X + 25, dinoY + 45, 12, 15, 6, 6);
            }
        } else {
            g2d.setColor(DINO_COLOR);
            g2d.fillRoundRect(DINO_X + 5, dinoY + 45, 12, 15, 6, 6);
            g2d.fillRoundRect(DINO_X + 25, dinoY + 45, 12, 15, 6, 6);
        }
    }
} 