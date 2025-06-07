import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChessGame extends JFrame {
    private JButton[][] squares;
    private JPanel boardPanel;
    private JLabel statusLabel;
    private String[][] board;
    private boolean[][] hasMoved;
    private boolean isWhiteTurn;
    private String lastMovedPiece;
    private int[] lastMoveFrom;
    private int[] lastMoveTo;
    private GameLauncher launcher;
    private javax.swing.Timer computerMoveTimer;
    
    // Selection tracking
    private JButton selectedSquare;
    private int selectedRow;
    private int selectedCol;
    
    // Drag and drop
    private Point dragStart;
    private JButton draggedPiece;
    
    // Constants
    private static final String EMPTY = "";
    private static final int COMPUTER_MOVE_DELAY = 750; // 750ms delay
    
    // Chess pieces
    private static final String[] WHITE_PIECES = {"R", "N", "B", "Q", "K", "B", "N", "R"};
    private static final String WHITE_PAWN = "P";
    private static final String[] BLACK_PIECES = {"r", "n", "b", "q", "k", "b", "n", "r"};
    private static final String BLACK_PAWN = "p";
    
    // Colors and fonts
    private static final Color LIGHT_SQUARE_COLOR = new Color(238, 238, 210); // Warm light beige
    private static final Color DARK_SQUARE_COLOR = new Color(118, 150, 86);   // Forest green
    private static final Color SELECTED_SQUARE_COLOR = new Color(186, 202, 68); // Highlight green
    private static final Color VALID_MOVE_COLOR = new Color(214, 214, 189, 200); // Semi-transparent highlight
    private static final Color BACKGROUND_COLOR = new Color(49, 46, 43); // Dark background
    private static final Color LABEL_COLOR = new Color(222, 184, 135); // Warm text color
    private static final Font CHESS_FONT = new Font("Arial", Font.BOLD, 36);
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 14);

    public ChessGame(GameLauncher launcher) {
        this.launcher = launcher;
        board = new String[8][8];
        hasMoved = new boolean[8][8];
        lastMoveFrom = new int[2];
        lastMoveTo = new int[2];
        lastMovedPiece = "";
        isWhiteTurn = true;  // White starts
        
        initializeBoard();
        initializeUI();
        
        computerMoveTimer = new javax.swing.Timer(COMPUTER_MOVE_DELAY, e -> {
            makeComputerMove();
            computerMoveTimer.stop();
        });
    }

    private void handleSquareClick(int row, int col) {
        System.out.println("Click detected at square [" + row + "," + col + "]");
        System.out.println("Current piece: " + board[row][col]);
        System.out.println("Is white's turn: " + isWhiteTurn);
        
        if (!isWhiteTurn) {
            System.out.println("Not white's turn - ignoring click");
            return;
        }

        // If no square is selected, select this one if it has a white piece
        if (selectedSquare == null) {
            System.out.println("No square currently selected");
            if (!board[row][col].isEmpty() && isWhitePiece(board[row][col])) {
                System.out.println("Selecting white piece: " + board[row][col]);
                selectedSquare = squares[row][col];
                selectedRow = row;
                selectedCol = col;
                showValidMoves(row, col);
            } else {
                System.out.println("Square empty or contains black piece - cannot select");
            }
        } else {
            System.out.println("Square already selected at [" + selectedRow + "," + selectedCol + "]");
            System.out.println("Attempting move to [" + row + "," + col + "]");
            // If a square was already selected, try to move the piece
            if (isValidMove(selectedSquare, squares[row][col])) {
                System.out.println("Move is valid - executing");
                makeMove(selectedRow, selectedCol, row, col);
                selectedSquare = null;
                resetAllSquares();
            } else {
                System.out.println("Move is invalid - deselecting");
                selectedSquare = null;
                resetAllSquares();
            }
        }
    }

    private void initializeBoard() {
        System.out.println("Initializing chess board");
        
        // Initialize white pieces at the bottom (rows 6-7)
        for (int col = 0; col < 8; col++) {
            board[7][col] = WHITE_PIECES[col];
            board[6][col] = WHITE_PAWN;
        }

        // Initialize empty squares
        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = EMPTY;
            }
        }

        // Initialize black pieces at the top (rows 0-1)
        for (int col = 0; col < 8; col++) {
            board[1][col] = BLACK_PAWN;
            board[0][col] = BLACK_PIECES[col];
        }

        // Initialize game state
        isWhiteTurn = true;
        selectedSquare = null;
        lastMovedPiece = "";
        lastMoveFrom[0] = lastMoveFrom[1] = -1;
        lastMoveTo[0] = lastMoveTo[1] = -1;

        System.out.println("Board initialization complete");
        printBoardState();
    }

    private void updateSquare(int row, int col) {
        squares[row][col].repaint();
        System.out.println("Updated square [" + row + "," + col + "] with piece: " + board[row][col]);
    }

    private void showValidMoves(int row, int col) {
        resetAllSquares();
        
        // Highlight valid moves
        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {
                if (isValidMove(squares[row][col], squares[toRow][toCol])) {
                    squares[toRow][toCol].setBackground(VALID_MOVE_COLOR);
                }
            }
        }
        
        // Highlight selected square
        squares[row][col].setBackground(SELECTED_SQUARE_COLOR);
    }

    private void highlightLegalMoves(int fromRow, int fromCol) {
        resetAllSquareColors();
        // Highlight source square
        squares[fromRow][fromCol].setBackground(new Color(186, 202, 68));

        // Check and highlight all possible legal moves
        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {
                if (isValidMove(squares[fromRow][fromCol], squares[toRow][toCol])) {
                    if (board[toRow][toCol].isEmpty()) {
                        squares[toRow][toCol].setBackground(new Color(186, 202, 68, 128));
                    } else {
                        squares[toRow][toCol].setBackground(new Color(202, 68, 68, 128));
                    }
                }
            }
        }
    }

    private void resetAllSquareColors() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 0) {
                    squares[i][j].setBackground(new Color(240, 217, 181));
                } else {
                    squares[i][j].setBackground(new Color(181, 136, 99));
                }
            }
        }
    }

    private void highlightTargetSquare(Point screenPoint) {
        JButton target = findButtonAt(screenPoint);
        if (target != null) {
            resetAllSquareColors();
            int[] pos = findButtonPosition(draggedPiece);
            if (pos != null) {
                squares[pos[0]][pos[1]].setBackground(new Color(186, 202, 68));
                highlightLegalMoves(pos[0], pos[1]);
            }
        }
    }

    private JButton findButtonAt(Point screenPoint) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JButton button = squares[row][col];
                Point buttonLocation = button.getLocationOnScreen();
                Rectangle bounds = new Rectangle(buttonLocation.x, buttonLocation.y,
                                              button.getWidth(), button.getHeight());
                if (bounds.contains(screenPoint)) {
                    return button;
                }
            }
        }
        return null;
    }

    private int[] findButtonPosition(JButton button) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (squares[row][col] == button) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    private void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        System.out.println("Executing move from [" + fromRow + "," + fromCol + 
                          "] to [" + toRow + "," + toCol + "]");
        System.out.println("Moving piece: " + board[fromRow][fromCol]);
        
        // Store move information
        lastMovedPiece = board[fromRow][fromCol];
        lastMoveFrom[0] = fromRow;
        lastMoveFrom[1] = fromCol;
        lastMoveTo[0] = toRow;
        lastMoveTo[1] = toCol;

        // Update hasMoved array
        hasMoved[fromRow][fromCol] = true;

        // Store captured piece before making the move
        String capturedPiece = board[toRow][toCol];
        if (!capturedPiece.isEmpty()) {
            System.out.println("Capturing piece: " + capturedPiece);
        }

        // Make the move
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = EMPTY;
        updateSquare(fromRow, fromCol);
        updateSquare(toRow, toCol);

        System.out.println("Move completed - updating game state");

        // Check for pawn promotion
        if (board[toRow][toCol].toLowerCase().equals("p") && (toRow == 0 || toRow == 7)) {
            System.out.println("Pawn promotion triggered");
            promotePawn(toRow, toCol);
        }

        // Check if a king was captured
        if (capturedPiece.toLowerCase().equals("k")) {
            System.out.println("King captured - game over");
            gameOver(isWhitePiece(capturedPiece));
            return;
        }

        // Switch turns
        isWhiteTurn = !isWhiteTurn;
        statusLabel.setText(isWhiteTurn ? "White's turn" : "Black's turn");
        System.out.println("Turn switched to: " + (isWhiteTurn ? "White" : "Black"));

        // If it's black's turn, start computer move timer
        if (!isWhiteTurn) {
            System.out.println("Starting computer move timer");
            computerMoveTimer.start();
        }
    }

    private void makeComputerMove() {
        System.out.println("Computer thinking about move...");
        
        // First, look for capturing moves
        List<Move> capturingMoves = new ArrayList<>();
        List<Move> normalMoves = new ArrayList<>();
        
        // Find all possible moves
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                if (!board[fromRow][fromCol].isEmpty() && !isWhitePiece(board[fromRow][fromCol])) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (isValidMove(squares[fromRow][fromCol], squares[toRow][toCol])) {
                                Move move = new Move(fromRow, fromCol, toRow, toCol);
                                // If target square has a white piece, it's a capturing move
                                if (!board[toRow][toCol].isEmpty() && isWhitePiece(board[toRow][toCol])) {
                                    // Calculate piece values for smarter capture decisions
                                    int capturedPieceValue = getPieceValue(board[toRow][toCol]);
                                    int movingPieceValue = getPieceValue(board[fromRow][fromCol]);
                                    move.score = capturedPieceValue - (movingPieceValue / 10.0);
                                    capturingMoves.add(move);
                                } else {
                                    normalMoves.add(move);
                                }
                            } 
                        }
                    }
                }
            }
        }

        // Make the best move available
        if (!capturingMoves.isEmpty()) {
            // Sort capturing moves by score (highest first)
            capturingMoves.sort((a, b) -> Double.compare(b.score, a.score));
            Move bestMove = capturingMoves.get(0);
            System.out.println("Computer making capturing move: " + 
                             board[bestMove.fromRow][bestMove.fromCol] + 
                             " takes " + board[bestMove.toRow][bestMove.toCol]);
            makeMove(bestMove.fromRow, bestMove.fromCol, bestMove.toRow, bestMove.toCol);
        } else if (!normalMoves.isEmpty()) {
            // Make a random non-capturing move
            Move randomMove = normalMoves.get(new Random().nextInt(normalMoves.size()));
            System.out.println("Computer making normal move: " + 
                             board[randomMove.fromRow][randomMove.fromCol]);
            makeMove(randomMove.fromRow, randomMove.fromCol, randomMove.toRow, randomMove.toCol);
        }
    }

    // Helper class to represent a move with a score
    private static class Move {
        int fromRow, fromCol, toRow, toCol;
        double score;  // Higher is better

        Move(int fromRow, int fromCol, int toRow, int toCol) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.score = 0;
        }
    }

    // Get the relative value of a piece for capture evaluation
    private int getPieceValue(String piece) {
        switch (piece.toLowerCase()) {
            case "p": return 1;  // Pawn
            case "n": // Knight
            case "b": return 3;  // Bishop
            case "r": return 5;  // Rook
            case "q": return 9;  // Queen
            case "k": return 100; // King (very high value to prioritize king capture)
            default: return 0;
        }
    }

    private void gameOver(boolean whiteKingCaptured) {
        // Stop the computer move timer
        computerMoveTimer.stop();

        // Create a custom dialog
        JDialog dialog = new JDialog(this, "Game Over!", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setUndecorated(true); // Remove window decorations for a modern look
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(44, 62, 80), 2));
 
        // Create the message panel with a gradient background
        JPanel messagePanel = new JPanel(new GridLayout(3, 1, 5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(236, 240, 241),
                                                    0, h, new Color(189, 195, 199));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        messagePanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        messagePanel.setOpaque(false);

        // Add game over message with enhanced styling
        JLabel titleLabel = new JLabel(whiteKingCaptured ? "Black Wins!" : "White Wins!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(new Color(44, 62, 80));

        JLabel messageLabel = new JLabel(whiteKingCaptured ? 
            "The white king has been captured!" : 
            "The black king has been captured!");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setForeground(new Color(52, 73, 94));

        messagePanel.add(titleLabel);
        messagePanel.add(messageLabel);

        // Create buttons panel with modern styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        // Custom button style
        class ModernButton extends JButton {
            ModernButton(String text) {
                super(text);
                setFont(new Font("Arial", Font.BOLD, 16));
                setForeground(Color.WHITE);
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setOpaque(true);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        setBackground(new Color(41, 128, 185));
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        setBackground(new Color(52, 152, 219));
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(41, 128, 185));
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        }

        // New Game button with modern styling
        ModernButton newGameButton = new ModernButton("New Game");
        newGameButton.setBackground(new Color(52, 152, 219));
        newGameButton.setPreferredSize(new Dimension(150, 40));
        newGameButton.addActionListener(e -> {
            dialog.dispose();
            resetGame();
        });

        // Return to Menu button with modern styling
        ModernButton menuButton = new ModernButton("Return to Menu");
        menuButton.setBackground(new Color(52, 152, 219));
        menuButton.setPreferredSize(new Dimension(150, 40));
        menuButton.addActionListener(e -> {
            dialog.dispose();
            returnToLauncher();
        });

        buttonPanel.add(newGameButton);
        buttonPanel.add(menuButton);
        messagePanel.add(buttonPanel);

        dialog.add(messagePanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Add a subtle shadow effect
        dialog.getRootPane().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(44, 62, 80), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        dialog.setVisible(true);
    }

    private void resetGame() {
        // Reset the board
        initializeBoard();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                updateSquare(i, j);
                hasMoved[i][j] = false;
            }
        }
        
        // Reset game state
        isWhiteTurn = true;
        lastMovedPiece = "";
        lastMoveFrom[0] = lastMoveFrom[1] = -1;
        lastMoveTo[0] = lastMoveTo[1] = -1;
        statusLabel.setText("White's turn");
    }

    private void returnToLauncher() {
        launcher.setVisible(true);
        dispose();
    }

    private void promotePawn(int row, int col) {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(this,
            "Choose promotion piece:",
            "Pawn Promotion",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        String promotedPiece;
        switch (choice) {
            case 0: promotedPiece = isWhiteTurn ? "Q" : "q"; break;
            case 1: promotedPiece = isWhiteTurn ? "R" : "r"; break;
            case 2: promotedPiece = isWhiteTurn ? "B" : "b"; break;
            case 3: promotedPiece = isWhiteTurn ? "N" : "n"; break;
            default: promotedPiece = isWhiteTurn ? "Q" : "q"; break;
        }
        board[row][col] = promotedPiece;
        updateSquare(row, col);
    }

    private boolean isWhitePiece(String piece) {
        if (piece.isEmpty()) return false;
        // White pieces are uppercase
        return Character.isUpperCase(piece.charAt(0));
    }

    private boolean isValidMove(JButton fromSquare, JButton toSquare) {
        int[] fromPos = findButtonPosition(fromSquare);
        int[] toPos = findButtonPosition(toSquare);
        
        if (fromPos == null || toPos == null) {
            System.out.println("Debug - Invalid positions detected");
            return false;
        }

        System.out.println("Checking move validity from [" + fromPos[0] + "," + fromPos[1] + 
                          "] to [" + toPos[0] + "," + toPos[1] + "]");
        
        int fromRow = fromPos[0];
        int fromCol = fromPos[1];
        int toRow = toPos[0];
        int toCol = toPos[1];

        // Can't move to the same square
        if (fromRow == toRow && fromCol == toCol) {
            System.out.println("Debug - Same square selected");
            return false;
        }

        String piece = board[fromRow][fromCol];
        String targetSquare = board[toRow][toCol];

        // Can't move empty square
        if (piece.isEmpty()) {
            System.out.println("Debug - Trying to move empty square");
            return false;
        }

        // Can't capture own pieces
        if (!targetSquare.isEmpty() && isWhitePiece(piece) == isWhitePiece(targetSquare)) {
            System.out.println("Debug - Cannot capture own piece");
            return false;
        }

        // Get piece type (lowercase)
        String pieceType = piece.toLowerCase();
        System.out.println("Debug - Piece type: " + pieceType);

        // Check piece-specific movement rules
        boolean validMove = switch (pieceType) {
            case "p" -> isValidPawnMove(fromRow, fromCol, toRow, toCol);
            case "r" -> isValidRookMove(fromRow, fromCol, toRow, toCol);
            case "n" -> isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case "b" -> isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case "q" -> isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case "k" -> isValidKingMove(fromRow, fromCol, toRow, toCol);
            default -> false;
        };

        System.out.println("Debug - Move validity: " + validMove);
        return validMove;
    }

    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol) {
        String piece = board[fromRow][fromCol];
        boolean isWhite = isWhitePiece(piece);
        
        // White pawns move up (negative row direction), black pawns move down
        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;  // Starting row for pawns
        
        System.out.println("Checking pawn move: " + piece + " from [" + fromRow + "," + fromCol + 
                         "] to [" + toRow + "," + toCol + "]");
        System.out.println("Is white: " + isWhite + ", direction: " + direction + 
                         ", start row: " + startRow);

        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);

        // Forward movement
        if (colDiff == 0) {
            // Single square forward
            if (rowDiff == direction && board[toRow][toCol].isEmpty()) {
                System.out.println("Valid single square forward move");
                return true;
            }
            
            // Double square forward from starting position
            if (fromRow == startRow && rowDiff == 2 * direction && 
                board[toRow][toCol].isEmpty() && board[fromRow + direction][toCol].isEmpty()) {
                System.out.println("Valid double square forward move");
                return true;
            }
        }
        // Diagonal capture
        else if (colDiff == 1 && rowDiff == direction) {
            // Regular capture
            if (!board[toRow][toCol].isEmpty() && 
                isWhitePiece(piece) != isWhitePiece(board[toRow][toCol])) {
                System.out.println("Valid diagonal capture");
                return true;
            }
            
            // En passant
            if (board[toRow][toCol].isEmpty() && // Target square is empty
                lastMovedPiece.toLowerCase().equals("p") && // Last moved piece was a pawn
                Math.abs(lastMoveFrom[0] - lastMoveTo[0]) == 2 && // It moved two squares
                lastMoveTo[0] == fromRow && // Adjacent to capturing pawn
                Math.abs(lastMoveTo[1] - fromCol) == 1) { // Horizontally adjacent
                System.out.println("Valid en passant capture");
                return true;
            }
        }

        System.out.println("Invalid pawn move");
        return false;
    }

    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        // L-shaped movement: (2,1) or (1,2)
        boolean isLShape = (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
        
        // Check if target square is empty or contains enemy piece
        if (isLShape) {
            return board[toRow][toCol].isEmpty() || 
                   isWhitePiece(board[fromRow][fromCol]) != isWhitePiece(board[toRow][toCol]);
        }
        return false;
    }

    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        // Must move diagonally
        if (rowDiff != colDiff) {
            return false;
        }

        // Check path for blocking pieces
        int rowStep = Integer.compare(toRow - fromRow, 0);
        int colStep = Integer.compare(toCol - fromCol, 0);
        
        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;
        
        while (currentRow != toRow) {
            if (!board[currentRow][currentCol].isEmpty()) {
                return false; // Path is blocked
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        // Target square must be empty or contain enemy piece
        return board[toRow][toCol].isEmpty() || 
               isWhitePiece(board[fromRow][fromCol]) != isWhitePiece(board[toRow][toCol]);
    }

    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Must move orthogonally (along ranks or files)
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }

        // Check path for blocking pieces
        int rowStep = Integer.compare(toRow - fromRow, 0);
        int colStep = Integer.compare(toCol - fromCol, 0);
        
        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;
        
        while (currentRow != toRow || currentCol != toCol) {
            if (!board[currentRow][currentCol].isEmpty()) {
                return false; // Path is blocked
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        // Target square must be empty or contain enemy piece
        return board[toRow][toCol].isEmpty() || 
               isWhitePiece(board[fromRow][fromCol]) != isWhitePiece(board[toRow][toCol]);
    }

    private boolean isValidQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Queen combines bishop and rook movements
        return isValidBishopMove(fromRow, fromCol, toRow, toCol) || 
               isValidRookMove(fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        System.out.println("Checking king move validity");
        
        // Calculate move distances
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        
        // Normal king move (one square in any direction)
        if (rowDiff <= 1 && colDiff <= 1) {
            System.out.println("Valid normal king move");
            return true;
        }
        
        // Check for castling
        if (rowDiff == 0 && colDiff == 2 && !hasMoved[fromRow][fromCol]) {
            System.out.println("Checking castling possibility");
            
            // Determine if it's kingside or queenside castling
            int rookCol = (toCol > fromCol) ? 7 : 0;
            int direction = (toCol > fromCol) ? 1 : -1;
            
            // Check if rook has moved
            if (hasMoved[fromRow][rookCol]) {
                System.out.println("Castling invalid - rook has moved");
                return false;
            }
            
            // Check if path is clear
            for (int col = fromCol + direction; col != rookCol; col += direction) {
                if (!board[fromRow][col].isEmpty()) {
                    System.out.println("Castling invalid - path not clear");
                    return false;
                }
            }
            
            System.out.println("Valid castling move");
            return true;
        }
        
        System.out.println("Invalid king move");
        return false;
    }

    private boolean isInCheck(boolean whiteKing) {
        // Find king's position
        int kingRow = -1, kingCol = -1;
        String kingPiece = whiteKing ? "K" : "k";
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col].equals(kingPiece)) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1) break;
        }

        // Check if any enemy piece can capture the king
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];
                if (!piece.isEmpty() && isWhitePiece(piece) != whiteKing) {
                    if (isValidMove(squares[row][col], squares[kingRow][kingCol])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void initializeUI() {
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create top panel for back button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);
        
        // Create modern back button
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
                
                // Add hover effect
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        setBackground(new Color(41, 128, 185));
                    }
                    public void mouseExited(MouseEvent e) {
                        setBackground(new Color(52, 152, 219));
                    }
                });
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
        backButton.addActionListener(e -> returnToLauncher());
        backButton.setPreferredSize(new Dimension(150, 35));
        
        topPanel.add(backButton, BorderLayout.WEST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Initialize the board panel
        boardPanel = new JPanel(new GridLayout(8, 8));
        boardPanel.setBorder(BorderFactory.createLineBorder(LABEL_COLOR, 3));

        // Create the squares with modern styling
        squares = new JButton[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = createStyledSquare(row, col);
                final int finalRow = row;
                final int finalCol = col;

                squares[row][col].addActionListener(e -> handleSquareClick(finalRow, finalCol));
                
                squares[row][col].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (!isWhiteTurn) return;
                        
                        String piece = board[finalRow][finalCol];
                        if (!piece.isEmpty() && isWhitePiece(piece)) {
                            selectedSquare = squares[finalRow][finalCol];
                            selectedRow = finalRow;
                            selectedCol = finalCol;
                            showValidMoves(finalRow, finalCol);
                        }
                    }
                    
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (selectedSquare != null) {
                            Point p = e.getLocationOnScreen();
                            
                            // Find which square we're over
                            for (int r = 0; r < 8; r++) {
                                for (int c = 0; c < 8; c++) {
                                    Rectangle bounds = squares[r][c].getBounds();
                                    bounds.setLocation(squares[r][c].getLocationOnScreen());
                                    
                                    if (bounds.contains(p)) {
                                        if (isValidMove(selectedSquare, squares[r][c])) {
                                            makeMove(selectedRow, selectedCol, r, c);
                                        }
                                        selectedSquare = null;
                                        resetAllSquares();
                                        return;
                                    }
                                }
                            }
                            
                            selectedSquare = null;
                            resetAllSquares();
                        }
                    }
                });

                boardPanel.add(squares[row][col]);
            }
        }

        // Add row labels (numbers)
        JPanel westPanel = new JPanel(new GridLayout(8, 1));
        westPanel.setBackground(BACKGROUND_COLOR);
        for (int i = 0; i < 8; i++) {
            JLabel label = new JLabel(String.valueOf(8 - i), SwingConstants.CENTER);
            label.setFont(LABEL_FONT);
            label.setForeground(LABEL_COLOR);
            westPanel.add(label);
        }

        // Add column labels (letters)
        JPanel southPanel = new JPanel(new GridLayout(1, 8));
        southPanel.setBackground(BACKGROUND_COLOR);
        for (int i = 0; i < 8; i++) {
            JLabel label = new JLabel(String.valueOf((char)('A' + i)), SwingConstants.CENTER);
            label.setFont(LABEL_FONT);
            label.setForeground(LABEL_COLOR);
            southPanel.add(label);
        }

        // Create status panel with gradient background
        JPanel statusPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(69, 66, 63),
                                                    0, h, new Color(49, 46, 43));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        statusPanel.setOpaque(false);

        // Style the status label
        statusLabel = new JLabel("White's turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setForeground(LABEL_COLOR);
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        // Add all components to the main panel
        JPanel boardWithLabels = new JPanel(new BorderLayout(5, 5));
        boardWithLabels.setBackground(BACKGROUND_COLOR);
        boardWithLabels.add(boardPanel, BorderLayout.CENTER);
        boardWithLabels.add(westPanel, BorderLayout.WEST);
        boardWithLabels.add(southPanel, BorderLayout.SOUTH);

        mainPanel.add(boardWithLabels, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private void resetAllSquares() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 0) {
                    squares[i][j].setBackground(LIGHT_SQUARE_COLOR);
                } else {
                    squares[i][j].setBackground(DARK_SQUARE_COLOR);
                }
            }
        }
    }

    private JButton createStyledSquare(int row, int col) {
        JButton square = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Draw background
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw piece
                String piece = board[row][col];
                if (!piece.isEmpty()) {
                    g2d.setFont(CHESS_FONT);
                    g2d.setColor(isWhitePiece(piece) ? Color.WHITE : Color.BLACK);
                    FontMetrics fm = g2d.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(piece)) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                    g2d.drawString(piece, x, y);
                }
            }
        };

        square.setPreferredSize(new Dimension(60, 60));
        square.setBorderPainted(false);
        square.setFocusPainted(false);
        square.setContentAreaFilled(false);
        
        // Set initial background color
        if ((row + col) % 2 == 0) {
            square.setBackground(LIGHT_SQUARE_COLOR);
        } else {
            square.setBackground(DARK_SQUARE_COLOR);
        }

        return square;
    }

    private void printBoardState() {
        System.out.println("\nCurrent Board State:");
        System.out.println("  A B C D E F G H");
        for (int i = 0; i < 8; i++) {
            System.out.print((8-i) + " ");
            for (int j = 0; j < 8; j++) {
                String piece = board[i][j].isEmpty() ? "." : board[i][j];
                System.out.print(piece + " ");
            }
            System.out.println(8-i);
        }
        System.out.println("  A B C D E F G H\n");
    }
} 