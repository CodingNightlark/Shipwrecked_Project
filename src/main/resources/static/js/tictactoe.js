class TicTacToe {
    constructor() {
        this.gameId = null;
        this.board = Array(9).fill(null);
        this.currentPlayer = 'X';
        this.gameEnded = false;
        
        // DOM elements
        this.gameBoard = document.getElementById('gameBoard');
        this.gameStatus = document.getElementById('gameStatus');
        this.resetButton = document.getElementById('resetButton');
        
        // Event listeners
        this.gameBoard.addEventListener('click', (e) => this.handleCellClick(e));
        this.resetButton.addEventListener('click', () => this.resetGame());
        
        // Initialize cells
        this.cells = Array.from(document.getElementsByClassName('cell'));
        
        // Start new game
        this.resetGame();
    }
    
    async handleCellClick(event) {
        const cell = event.target;
        if (!cell.classList.contains('cell')) return;
        
        const index = parseInt(cell.dataset.index);
        if (this.board[index] || this.gameEnded) return;
        
        try {
            const response = await fetch(`/api/tictactoe/${this.gameId}/move?position=${index}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
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
        this.currentPlayer = gameState.currentPlayer;
        
        // Update UI
        this.cells.forEach((cell, index) => {
            const value = this.board[index];
            cell.textContent = value || '';
            cell.classList.remove('x', 'o');
            if (value) {
                cell.classList.add(value.toLowerCase());
            }
        });
        
        if (gameState.winner) {
            this.gameStatus.textContent = `Player ${gameState.winner} wins!`;
            this.gameEnded = true;
        } else if (gameState.draw) {
            this.gameStatus.textContent = "It's a draw!";
            this.gameEnded = true;
        } else {
            this.gameStatus.textContent = `Player ${this.currentPlayer}'s turn`;
        }
    }
    
    async resetGame() {
        try {
            const response = await fetch('/api/tictactoe/new', {
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
            this.gameEnded = false;
            this.updateGameState(gameState);
        } catch (error) {
            console.error('Error starting new game:', error);
            this.gameStatus.textContent = 'Error starting game. Please try again.';
        }
    }
}

// Initialize game when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new TicTacToe();
}); 