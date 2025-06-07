import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.Random;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class DinoGame extends JFrame {
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 400;
    private static final double GROUND_HEIGHT_RATIO = 0.75; // Ground at 75% of window height
    private static final int DINO_X_RATIO = 100; // Distance from left edge
    private static final int OBSTACLE_WIDTH = 30;
    private static final int BIRD_WIDTH = 40;
    private static final int BIRD_HEIGHT = 30;
    private static final int JUMP_VELOCITY = -15;
    private static final int GRAVITY = 1;
    private static final int DUCK_HEIGHT = 30;  // Height when ducking
    private static final int NORMAL_HEIGHT = 50; // Normal dino height

    // Colors for day/night cycle
    private static final Color DAY_SKY_START = new Color(135, 206, 235);
    private static final Color DAY_SKY_END = new Color(173, 216, 230);
    private static final Color NIGHT_SKY_START = new Color(25, 25, 112);
    private static final Color NIGHT_SKY_END = new Color(0, 0, 139);
    private static final Color GROUND_COLOR = new Color(200, 230, 180); // Lighter, more pleasant green
    private static final Color DINO_COLOR = new Color(50, 205, 50);
    private static final Color BIRD_COLOR = new Color(255, 69, 0);
    private static final Color OBSTACLE_COLOR = new Color(34, 139, 34);
    private static final Color SCORE_COLOR = new Color(44, 62, 80);
    private static final Font SCORE_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font GAME_OVER_FONT = new Font("Arial", Font.BOLD, 48);
    private static final Color CACTUS_COLOR = new Color(40, 120, 80);  // Dark green for cacti
    private static final int HIGH_SCORE_FILE_VERSION = 1;
    private static int highScore = 0;

    private int groundY;  // Will be calculated based on window height
    private int dinoY;    // Will be calculated based on groundY
    private int dinoHeight = NORMAL_HEIGHT;
    private int dinoVelocity = 0;
    private boolean isJumping = false;
    private boolean isDucking = false;
    private ArrayList<Rectangle> obstacles = new ArrayList<>();
    private ArrayList<Bird> birds = new ArrayList<>();
    private Timer gameTimer;
    private int score = 0;
    private boolean gameOver = false;
    private GameLauncher launcher;
    private Random random = new Random();
    private float timeOfDay = 0; // 0 to 1, where 0 is dawn, 0.5 is noon, and 1 is dusk
    private boolean isNight = false;
    private ArrayList<Star> stars = new ArrayList<>();
    private ArrayList<Cloud> clouds = new ArrayList<>();
    
    // Cloud colors
    private static final Color[] CLOUD_COLORS = {
        new Color(255, 255, 255, 200),
        new Color(245, 245, 245, 180),
        new Color(240, 240, 240, 160)
    };

    private static class Cloud {
        int x, y;
        int width, height;
        Color color;
        
        Cloud(int x, int y) {
            this.x = x;
            this.y = y;
            this.width = 40 + new Random().nextInt(60);
            this.height = 20 + new Random().nextInt(30);
            this.color = CLOUD_COLORS[new Random().nextInt(CLOUD_COLORS.length)];
        }
    }

    private static class Bird {
        int x, y;
        boolean flapUp;
        int flapCount;
        
        Bird(int x, int y) {
            this.x = x;
            this.y = y;
            this.flapUp = true;
            this.flapCount = 0;
        }
        
        void updateFlap() {
            if (++flapCount >= 10) {
                flapUp = !flapUp;
                flapCount = 0;
            }
        }
        
        void draw(Graphics2D g2d) {
            // Draw bird body
            g2d.setColor(BIRD_COLOR);
            g2d.fillOval(x, y, BIRD_WIDTH - 10, BIRD_HEIGHT - 10);
            
            // Draw wings
            int wingY = y + (flapUp ? -5 : 5);
            g2d.fillOval(x - 5, wingY, 20, 10);
            g2d.fillOval(x + 15, wingY, 20, 10);
            
            // Draw beak
            g2d.setColor(BIRD_COLOR.darker());
            int[] xPoints = {x + BIRD_WIDTH - 5, x + BIRD_WIDTH + 5, x + BIRD_WIDTH - 5};
            int[] yPoints = {y + 5, y + 10, y + 15};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
    }

    private static class Star {
        int x, y;
        float brightness;
        
        Star(int x, int y) {
            this.x = x;
            this.y = y;
            this.brightness = new Random().nextFloat();
        }
        
        void twinkle() {
            brightness = Math.max(0.2f, Math.min(1.0f, 
                brightness + (new Random().nextFloat() - 0.5f) * 0.1f));
        }
    }

    public DinoGame(GameLauncher launcher) {
        this.launcher = launcher;
        loadHighScore();
        setTitle("Dino Jump Game");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setLocationRelativeTo(null);

        // Add component listener for window resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateGameDimensions();
            }
        });

        // Initialize game dimensions
        updateGameDimensions();

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
                drawGame((Graphics2D) g);
            }
        };
        gamePanel.setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

        // Layout setup
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);

        // Initialize clouds
        for (int i = 0; i < 5; i++) {
            addNewCloud();
        }

        // Initialize stars
        initializeStars();

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
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    isDucking = true;
                    dinoHeight = DUCK_HEIGHT;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    isDucking = false;
                    dinoHeight = NORMAL_HEIGHT;
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
        int x = random.nextInt(getWidth());
        int y = random.nextInt(groundY - 100);
        clouds.add(new Cloud(x, y));
    }

    private void updateGame() {
        // Update time of day
        timeOfDay += 0.0001f;
        if (timeOfDay >= 1) {
            timeOfDay = 0;
            isNight = !isNight;
        }

        // Update dino position
        if (isJumping) {
            dinoY += dinoVelocity;
            dinoVelocity += GRAVITY;
            
            if (dinoY >= groundY) {
                dinoY = groundY;
                isJumping = false;
                dinoVelocity = 0;
            }
        }

        // Update clouds
        for (Cloud cloud : clouds) {
            cloud.x -= 1;
            if (cloud.x + cloud.width < 0) {
                cloud.x = getWidth();
                cloud.y = random.nextInt(groundY - 100);
            }
        }

        // Update obstacles
        for (Rectangle obstacle : obstacles) {
            obstacle.x -= 5;
        }
        obstacles.removeIf(obstacle -> obstacle.x + obstacle.width < 0);

        // Update birds
        for (Bird bird : birds) {
            bird.x -= 6;
            bird.updateFlap();
        }
        birds.removeIf(bird -> bird.x + BIRD_WIDTH < 0);

        // Add new obstacles
        if (obstacles.isEmpty() || obstacles.get(obstacles.size() - 1).x < getWidth() - 300) {
            if (random.nextDouble() < 0.7) { // 70% chance for cactus
                int height = 30 + random.nextInt(50);
                obstacles.add(new Rectangle(
                    getWidth(),
                    groundY - height + 50,
                    OBSTACLE_WIDTH,
                    height
                ));
            } else { // 30% chance for bird
                birds.add(new Bird(
                    getWidth(),
                    groundY - 100 - random.nextInt(100)
                ));
            }
        }

        // Update stars at night
        if (isNight) {
            for (Star star : stars) {
                star.twinkle();
            }
        }

        // Check collisions
        Rectangle dinoRect = new Rectangle(DINO_X_RATIO, dinoY, 40, dinoHeight);
        
        // Check cactus collisions
        for (Rectangle obstacle : obstacles) {
            if (dinoRect.intersects(obstacle)) {
                gameOver = true;
                gameTimer.stop();
                break;
            }
        }
        
        // Check bird collisions
        for (Bird bird : birds) {
            Rectangle birdRect = new Rectangle(bird.x, bird.y, BIRD_WIDTH, BIRD_HEIGHT);
            if (dinoRect.intersects(birdRect)) {
                gameOver = true;
                gameTimer.stop();
                break;
            }
        }

        // Update score
        score++;
    }

    private void resetGame() {
        dinoY = groundY;
        dinoVelocity = 0;
        dinoHeight = NORMAL_HEIGHT;
        isJumping = false;
        isDucking = false;
        obstacles.clear();
        birds.clear();
        score = 0;
        gameOver = false;
        timeOfDay = 0;
        isNight = false;
        initializeStars();
        gameTimer.start();
    }

    private void returnToLauncher() {
        gameTimer.stop();
        dispose();
        launcher.setVisible(true);
    }

    private void drawGame(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw sky with day/night cycle
        Color skyStart = isNight ? NIGHT_SKY_START : DAY_SKY_START;
        Color skyEnd = isNight ? NIGHT_SKY_END : DAY_SKY_END;
        
        GradientPaint skyGradient = new GradientPaint(
            0, 0, skyStart,
            0, groundY, skyEnd
        );
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, getWidth(), groundY);

        // Draw stars at night
        if (isNight) {
            for (Star star : stars) {
                g2d.setColor(new Color(1f, 1f, 1f, star.brightness));
                g2d.fillOval(star.x, star.y, 2, 2);
            }
        }

        // Draw clouds
        for (Cloud cloud : clouds) {
            g2d.setColor(isNight ? 
                new Color(cloud.color.getRed(), cloud.color.getGreen(), cloud.color.getBlue(), 120) : 
                cloud.color);
            g2d.fillOval(cloud.x, cloud.y, cloud.width, cloud.height);
        }

        // Draw ground with texture
        g2d.setColor(GROUND_COLOR);
        g2d.fillRect(0, groundY, getWidth(), getHeight() - groundY);
        
        // Add some grass details
        g2d.setColor(new Color(150, 200, 130));
        for (int x = 0; x < getWidth(); x += 15) {
            int grassHeight = 2 + random.nextInt(4);
            g2d.drawLine(x, groundY, x, groundY + grassHeight);
        }

        // Draw obstacles as cacti
        for (Rectangle obstacle : obstacles) {
            // Shadow
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fillRoundRect(obstacle.x + 3, obstacle.y + 3, 
                            obstacle.width, obstacle.height, 8, 8);
            drawObstacle(g2d, obstacle);
        }

        // Draw birds
        for (Bird bird : birds) {
            bird.draw(g2d);
        }

        // Draw dino
        drawDino(g2d);

        // Draw scores
        g2d.setFont(SCORE_FONT);
        g2d.setColor(isNight ? Color.WHITE : SCORE_COLOR);
        
        // Draw current score in top right
        String scoreText = "Score: " + score;
        g2d.drawString(scoreText, getWidth() - 150, 40);

        // Draw high score centered at top
        String highScoreText = "High Score: " + highScore;
        FontMetrics fm = g2d.getFontMetrics();
        int highScoreX = (getWidth() - fm.stringWidth(highScoreText)) / 2;
        
        // Draw shadow for better visibility
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.drawString(highScoreText, highScoreX + 2, 42);
        
        // Draw actual high score text
        g2d.setColor(isNight ? Color.WHITE : SCORE_COLOR);
        g2d.drawString(highScoreText, highScoreX, 40);

        if (gameOver) {
            drawGameOver(g2d);
        }
    }

    private void drawDino(Graphics2D g2d) {
        // Shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(DINO_X_RATIO + 3, dinoY + 3, 40, dinoHeight, 10, 10);

        // Body
        g2d.setColor(DINO_COLOR);
        g2d.fillRoundRect(DINO_X_RATIO, dinoY, 40, dinoHeight, 10, 10);

        // Neck and head
        g2d.fillRoundRect(DINO_X_RATIO + 25, dinoY - 15, 20, 25, 8, 8);
        g2d.fillOval(DINO_X_RATIO + 35, dinoY - 25, 25, 25);

        // Eyes
        g2d.setColor(Color.WHITE);
        g2d.fillOval(DINO_X_RATIO + 45, dinoY - 20, 10, 10); // Main eye white
        g2d.setColor(Color.BLACK);
        g2d.fillOval(DINO_X_RATIO + 47, dinoY - 18, 6, 6);   // Main eye pupil
        
        // Eyebrow
        g2d.setStroke(new BasicStroke(2));
        g2d.drawArc(DINO_X_RATIO + 44, dinoY - 24, 12, 8, 0, 180);

        // Nostril
        g2d.fillOval(DINO_X_RATIO + 55, dinoY - 15, 3, 3);

        // Smile - changes to determined expression when jumping
        if (isJumping) {
            // Determined expression
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(DINO_X_RATIO + 45, dinoY - 10, DINO_X_RATIO + 55, dinoY - 10);
        } else {
            // Happy smile
            g2d.setStroke(new BasicStroke(2));
            g2d.drawArc(DINO_X_RATIO + 45, dinoY - 15, 12, 8, 180, 180);
        }

        // Spikes on back
        int[] spikeX = new int[]{
            DINO_X_RATIO + 30, DINO_X_RATIO + 25, DINO_X_RATIO + 20, 
            DINO_X_RATIO + 15, DINO_X_RATIO + 10
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
                g2d.fillRoundRect(DINO_X_RATIO + 5, dinoY + 45, 12, 20, 6, 6);
            } else {
                g2d.fillRoundRect(DINO_X_RATIO + 5, dinoY + 45, 12, 15, 6, 6);
            }
            
            // Right leg
            if (!isLeftLegForward) {
                g2d.fillRoundRect(DINO_X_RATIO + 25, dinoY + 45, 12, 20, 6, 6);
            } else {
                g2d.fillRoundRect(DINO_X_RATIO + 25, dinoY + 45, 12, 15, 6, 6);
            }
        } else {
            g2d.setColor(DINO_COLOR);
            g2d.fillRoundRect(DINO_X_RATIO + 5, dinoY + 45, 12, 15, 6, 6);
            g2d.fillRoundRect(DINO_X_RATIO + 25, dinoY + 45, 12, 15, 6, 6);
        }
    }

    private void drawObstacle(Graphics2D g2d, Rectangle obstacle) {
        // Draw cactus instead of rectangle
        int x = obstacle.x;
        int y = obstacle.y;
        int width = obstacle.width;
        int height = obstacle.height;

        // Main stem
        g2d.setColor(CACTUS_COLOR);
        g2d.fillRoundRect(x, y, width/2, height, 5, 5);

        // Side branches (if tall enough)
        if (height > 40) {
            // Left branch
            g2d.fillRoundRect(x - width/4, y + height/3, width/2, height/3, 5, 5);
            
            // Right branch
            g2d.fillRoundRect(x + width/4, y + height/2, width/2, height/3, 5, 5);
        }

        // Add spikes
        g2d.setColor(new Color(20, 80, 50));
        for (int i = 0; i < height; i += 5) {
            // Left spikes
            g2d.drawLine(x, y + i, x - 3, y + i + 2);
            // Right spikes
            g2d.drawLine(x + width/2, y + i, x + width/2 + 3, y + i + 2);
        }
    }

    private void drawGameOver(Graphics2D g2d) {
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

        // Add high score achievement if applicable
        if (score > highScore) {
            highScore = score;
            saveHighScore();
            
            // Draw "New High Score!" message
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            String newHighScoreText = "New High Score!";
            fm = g2d.getFontMetrics();
            textX = (getWidth() - fm.stringWidth(newHighScoreText)) / 2;
            g2d.setColor(new Color(255, 215, 0)); // Gold color
            g2d.drawString(newHighScoreText, textX, getHeight() / 2 + 80);
        }
    }

    private void updateGameDimensions() {
        groundY = (int)(getHeight() * GROUND_HEIGHT_RATIO);
        dinoY = groundY;
        
        // Update positions of existing game elements
        updateGameElements();
        repaint();
    }

    private void updateGameElements() {
        // Update obstacles positions relative to new ground height
        for (Rectangle obstacle : obstacles) {
            int oldBottom = obstacle.y + obstacle.height;
            obstacle.y = groundY - obstacle.height + 50;
        }
        
        // Update birds positions
        for (Bird bird : birds) {
            // Keep birds at their relative height ratio
            double heightRatio = (double)(bird.y) / groundY;
            bird.y = (int)(heightRatio * groundY);
        }
        
        // Update clouds
        for (Cloud cloud : clouds) {
            // Keep clouds at their relative height ratio
            double heightRatio = (double)(cloud.y) / groundY;
            cloud.y = (int)(heightRatio * groundY);
        }
    }

    private void initializeStars() {
        stars.clear();
        for (int i = 0; i < 50; i++) {
            stars.add(new Star(
                random.nextInt(getWidth()),
                random.nextInt(groundY - 50)
            ));
        }
    }

    private void loadHighScore() {
        try {
            File file = new File("dino_high_score.dat");
            if (file.exists()) {
                try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
                    int version = in.readInt();
                    if (version == HIGH_SCORE_FILE_VERSION) {
                        highScore = in.readInt();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading high score: " + e.getMessage());
        }
    }

    private void saveHighScore() {
        try {
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream("dino_high_score.dat"))) {
                out.writeInt(HIGH_SCORE_FILE_VERSION);
                out.writeInt(highScore);
            }
        } catch (IOException e) {
            System.err.println("Error saving high score: " + e.getMessage());
        }
    }
} 