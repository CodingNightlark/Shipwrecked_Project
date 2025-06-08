class ChessGame {
    constructor() {
        this.gameId = null;
        this.board = Array(8).fill(null).map(() => Array(8).fill(null));
        this.selectedSquare = null;
        this.isWhiteTurn = true;
        this.gameEnded = false;
        this.moveHistory = [];
        
        // Chess pieces Unicode characters
        this.pieces = {
            'K': '♔', 'Q': '♕', 'R': '♖', 'B': '♗', 'N': '♘', 'P': '♙', // White pieces
            'k': '♚', 'q': '♛', 'r': '♜', 'b': '♝', 'n': '♞', 'p': '♟' // Black pieces
        };
        
        // DOM elements
        this.boardElement = document.getElementById('chessBoard');
        this.statusElement = document.getElementById('gameStatus');
        this.resetButton = document.getElementById('resetButton');
        this.undoButton = document.getElementById('undoButton');
        
        // Event listeners
        this.resetButton.addEventListener('click', () => this.resetGame());
        this.undoButton.addEventListener('click', () => this.undoMove());
        
        // Initialize the game
        this.initializeBoard();
        this.createBoardUI();
        this.resetGame();
    }
    
    initializeBoard() {
        // Initialize black pieces
        const blackPieces = ['r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'];
        for (let col = 0; col < 8; col++) {
            this.board[0][col] = blackPieces[col];
            this.board[1][col] = 'p';
        }
        
        // Initialize empty squares
        for (let row = 2; row < 6; row++) {
            for (let col = 0; col < 8; col++) {
                this.board[row][col] = null;
            }
        }
        
        // Initialize white pieces
        const whitePieces = ['R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'];
        for (let col = 0; col < 8; col++) {
            this.board[7][col] = whitePieces[col];
            this.board[6][col] = 'P';
        }
    }
    
    createBoardUI() {
        this.boardElement.innerHTML = '';
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                const square = document.createElement('div');
                square.className = `square ${(row + col) % 2 === 0 ? 'light' : 'dark'}`;
                square.dataset.row = row;
                square.dataset.col = col;
                square.addEventListener('click', (e) => this.handleSquareClick(e));
                this.boardElement.appendChild(square);
            }
        }
    }
    
    updateBoardUI() {
        const squares = this.boardElement.children;
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                const square = squares[row * 8 + col];
                const piece = this.board[row][col];
                square.textContent = piece ? this.pieces[piece] : '';
            }
        }
    }
    
    async handleSquareClick(event) {
        const square = event.target;
        const row = parseInt(square.dataset.row);
        const col = parseInt(square.dataset.col);
        
        if (this.gameEnded) return;
        
        // If no square is selected, select this one if it has a piece of the current player
        if (!this.selectedSquare) {
            if (this.board[row][col] && this.isCurrentPlayerPiece(row, col)) {
                this.selectedSquare = { row, col };
                this.highlightValidMoves(row, col);
            }
        } else {
            // If a square was already selected, try to move the piece
            const fromRow = this.selectedSquare.row;
            const fromCol = this.selectedSquare.col;
            
            if (this.isValidMove(fromRow, fromCol, row, col)) {
                await this.makeMove(fromRow, fromCol, row, col);
            }
            
            // Clear selection and highlights
            this.selectedSquare = null;
            this.clearHighlights();
        }
    }
    
    isCurrentPlayerPiece(row, col) {
        const piece = this.board[row][col];
        return piece && (this.isWhiteTurn ? piece === piece.toUpperCase() : piece === piece.toLowerCase());
    }
    
    highlightValidMoves(row, col) {
        this.clearHighlights();
        
        // Highlight selected square
        const squares = this.boardElement.children;
        squares[row * 8 + col].classList.add('selected');
        
        // Highlight valid moves
        for (let toRow = 0; toRow < 8; toRow++) {
            for (let toCol = 0; toCol < 8; toCol++) {
                if (this.isValidMove(row, col, toRow, toCol)) {
                    const square = squares[toRow * 8 + toCol];
                    square.classList.add(this.board[toRow][toCol] ? 'capture-move' : 'valid-move');
                }
            }
        }
    }
    
    clearHighlights() {
        const squares = this.boardElement.children;
        for (const square of squares) {
            square.classList.remove('selected', 'valid-move', 'capture-move');
        }
    }
    
    isValidMove(fromRow, fromCol, toRow, toCol) {
        const piece = this.board[fromRow][fromCol];
        if (!piece) return false;
        
        // Can't capture your own pieces
        const targetPiece = this.board[toRow][toCol];
        if (targetPiece && this.isCurrentPlayerPiece(toRow, toCol)) return false;
        
        const pieceType = piece.toLowerCase();
        
        switch (pieceType) {
            case 'p': return this.isValidPawnMove(fromRow, fromCol, toRow, toCol);
            case 'r': return this.isValidRookMove(fromRow, fromCol, toRow, toCol);
            case 'n': return this.isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case 'b': return this.isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case 'q': return this.isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case 'k': return this.isValidKingMove(fromRow, fromCol, toRow, toCol);
            default: return false;
        }
    }
    
    isValidPawnMove(fromRow, fromCol, toRow, toCol) {
        const direction = this.isWhiteTurn ? -1 : 1;
        const startRow = this.isWhiteTurn ? 6 : 1;
        
        // Basic one square move
        if (fromCol === toCol && toRow === fromRow + direction && !this.board[toRow][toCol]) {
            return true;
        }
        
        // Initial two square move
        if (fromCol === toCol && fromRow === startRow && toRow === fromRow + 2 * direction &&
            !this.board[fromRow + direction][fromCol] && !this.board[toRow][toCol]) {
            return true;
        }
        
        // Capture
        if (Math.abs(fromCol - toCol) === 1 && toRow === fromRow + direction && 
            this.board[toRow][toCol] && !this.isCurrentPlayerPiece(toRow, toCol)) {
            return true;
        }
        
        return false;
    }
    
    isValidRookMove(fromRow, fromCol, toRow, toCol) {
        if (fromRow !== toRow && fromCol !== toCol) return false;
        return this.isPathClear(fromRow, fromCol, toRow, toCol);
    }
    
    isValidKnightMove(fromRow, fromCol, toRow, toCol) {
        const rowDiff = Math.abs(toRow - fromRow);
        const colDiff = Math.abs(toCol - fromCol);
        return (rowDiff === 2 && colDiff === 1) || (rowDiff === 1 && colDiff === 2);
    }
    
    isValidBishopMove(fromRow, fromCol, toRow, toCol) {
        if (Math.abs(toRow - fromRow) !== Math.abs(toCol - fromCol)) return false;
        return this.isPathClear(fromRow, fromCol, toRow, toCol);
    }
    
    isValidQueenMove(fromRow, fromCol, toRow, toCol) {
        return this.isValidRookMove(fromRow, fromCol, toRow, toCol) ||
               this.isValidBishopMove(fromRow, fromCol, toRow, toCol);
    }
    
    isValidKingMove(fromRow, fromCol, toRow, toCol) {
        return Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1;
    }
    
    isPathClear(fromRow, fromCol, toRow, toCol) {
        const rowDir = fromRow === toRow ? 0 : (toRow - fromRow) / Math.abs(toRow - fromRow);
        const colDir = fromCol === toCol ? 0 : (toCol - fromCol) / Math.abs(toCol - fromCol);
        
        let row = fromRow + rowDir;
        let col = fromCol + colDir;
        
        while (row !== toRow || col !== toCol) {
            if (this.board[row][col]) return false;
            row += rowDir;
            col += colDir;
        }
        
        return true;
    }
    
    async makeMove(fromRow, fromCol, toRow, toCol) {
        try {
            const response = await fetch(`/api/chess/${this.gameId}/move`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    fromRow, fromCol, toRow, toCol
                })
            });
            
            if (!response.ok) {
                throw new Error('Invalid move');
            }
            
            const gameState = await response.json();
            this.updateGameState(gameState);
        } catch (error) {
            console.error('Error making move:', error);
        }
    }
    
    updateGameState(gameState) {
        this.board = gameState.board;
        this.isWhiteTurn = gameState.isWhiteTurn;
        this.gameEnded = gameState.gameEnded;
        
        this.updateBoardUI();
        this.updateStatus(gameState);
    }
    
    updateStatus(gameState) {
        if (gameState.gameEnded) {
            if (gameState.winner) {
                this.statusElement.textContent = `${gameState.winner} wins!`;
            } else {
                this.statusElement.textContent = "It's a draw!";
            }
        } else {
            this.statusElement.textContent = `${this.isWhiteTurn ? 'White' : 'Black'}'s turn`;
        }
    }
    
    async resetGame() {
        try {
            const response = await fetch('/api/chess/new', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to start new game');
            }
            
            const gameState = await response.json();
            this.gameId = gameState.gameId;
            this.updateGameState(gameState);
        } catch (error) {
            console.error('Error starting new game:', error);
            this.statusElement.textContent = 'Error starting game. Please try again.';
        }
    }
    
    async undoMove() {
        if (this.moveHistory.length === 0) return;
        
        try {
            const response = await fetch(`/api/chess/${this.gameId}/undo`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to undo move');
            }
            
            const gameState = await response.json();
            this.updateGameState(gameState);
        } catch (error) {
            console.error('Error undoing move:', error);
        }
    }
}

// Initialize game when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new ChessGame();
}); 