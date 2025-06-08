document.addEventListener('DOMContentLoaded', () => {
    const board = document.getElementById('chess-board');
    const turnIndicator = document.getElementById('turn-indicator');
    const moveHistory = document.getElementById('move-history');
    let selectedPiece = null;
    let currentPlayer = 'white';
    let moveCount = 1;
    let lastMove = null;

    // Chess piece Unicode characters
    const PIECES = {
        white: {
            king: '♔',
            queen: '♕',
            rook: '♖',
            bishop: '♗',
            knight: '♘',
            pawn: '♙'
        },
        black: {
            king: '♚',
            queen: '♛',
            rook: '♜',
            bishop: '♝',
            knight: '♞',
            pawn: '♟'
        }
    };

    // Initial board setup
    const INITIAL_BOARD = [
        ['rook', 'knight', 'bishop', 'queen', 'king', 'bishop', 'knight', 'rook'],
        ['pawn', 'pawn', 'pawn', 'pawn', 'pawn', 'pawn', 'pawn', 'pawn'],
        [null, null, null, null, null, null, null, null],
        [null, null, null, null, null, null, null, null],
        [null, null, null, null, null, null, null, null],
        [null, null, null, null, null, null, null, null],
        ['pawn', 'pawn', 'pawn', 'pawn', 'pawn', 'pawn', 'pawn', 'pawn'],
        ['rook', 'knight', 'bishop', 'queen', 'king', 'bishop', 'knight', 'rook']
    ];

    // Create the chess board
    function createBoard() {
        board.innerHTML = '';
        for (let row = 0; row < 8; row++) {
            for (let col = 0; col < 8; col++) {
                const square = document.createElement('div');
                square.className = `square ${(row + col) % 2 === 0 ? 'light' : 'dark'}`;
                square.dataset.row = row;
                square.dataset.col = col;

                // Add coordinates
                if (col === 0) {
                    const rankCoord = document.createElement('div');
                    rankCoord.className = 'coordinates rank-coord';
                    rankCoord.textContent = 8 - row;
                    square.appendChild(rankCoord);
                }
                if (row === 7) {
                    const fileCoord = document.createElement('div');
                    fileCoord.className = 'coordinates file-coord';
                    fileCoord.textContent = String.fromCharCode(97 + col);
                    square.appendChild(fileCoord);
                }

                // Add piece if exists in initial setup
                const piece = INITIAL_BOARD[row][col];
                if (piece) {
                    const pieceColor = row < 2 ? 'black' : 'white';
                    addPiece(square, piece, pieceColor);
                }

                square.addEventListener('click', handleSquareClick);
                board.appendChild(square);
            }
        }
    }

    function addPiece(square, pieceType, color) {
        const piece = document.createElement('div');
        piece.className = `piece ${color}`;
        piece.dataset.pieceType = pieceType;
        piece.dataset.color = color;
        piece.textContent = PIECES[color][pieceType];
        square.appendChild(piece);
    }

    function handleSquareClick(event) {
        const square = event.target.closest('.square');
        const piece = square.querySelector('.piece');

        // Clear previous highlights
        clearHighlights();

        if (selectedPiece) {
            const fromSquare = selectedPiece.parentElement;
            if (isValidMove(fromSquare, square)) {
                movePiece(fromSquare, square);
                selectedPiece = null;
                switchTurn();
            } else {
                selectedPiece = null;
            }
        } else if (piece && piece.dataset.color === currentPlayer) {
            selectedPiece = piece;
            square.classList.add('selected');
            showValidMoves(square);
        }
    }

    function isValidMove(fromSquare, toSquare) {
        const piece = fromSquare.querySelector('.piece');
        if (!piece) return false;

        const fromRow = parseInt(fromSquare.dataset.row);
        const fromCol = parseInt(fromSquare.dataset.col);
        const toRow = parseInt(toSquare.dataset.row);
        const toCol = parseInt(toSquare.dataset.col);
        const pieceType = piece.dataset.pieceType;
        const pieceColor = piece.dataset.color;

        // Check if target square has a piece of the same color
        const targetPiece = toSquare.querySelector('.piece');
        if (targetPiece && targetPiece.dataset.color === pieceColor) {
            return false;
        }

        // Piece-specific move validation
        switch (pieceType) {
            case 'pawn':
                return isValidPawnMove(fromRow, fromCol, toRow, toCol, pieceColor, !!targetPiece);
            case 'knight':
                return isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case 'bishop':
                return isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case 'rook':
                return isValidRookMove(fromRow, fromCol, toRow, toCol);
            case 'queen':
                return isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case 'king':
                return isValidKingMove(fromRow, fromCol, toRow, toCol);
            default:
                return false;
        }
    }

    function isValidPawnMove(fromRow, fromCol, toRow, toCol, color, isCapture) {
        const direction = color === 'white' ? -1 : 1;
        const startRow = color === 'white' ? 6 : 1;

        if (fromCol === toCol && !isCapture) {
            // Regular move
            if (toRow === fromRow + direction) return true;
            // First move - can move two squares
            if (fromRow === startRow && toRow === fromRow + 2 * direction) return true;
        } else if (isCapture && Math.abs(toCol - fromCol) === 1 && toRow === fromRow + direction) {
            // Capture move
            return true;
        }
        return false;
    }

    function isValidKnightMove(fromRow, fromCol, toRow, toCol) {
        const rowDiff = Math.abs(toRow - fromRow);
        const colDiff = Math.abs(toCol - fromCol);
        return (rowDiff === 2 && colDiff === 1) || (rowDiff === 1 && colDiff === 2);
    }

    function isValidBishopMove(fromRow, fromCol, toRow, toCol) {
        return Math.abs(toRow - fromRow) === Math.abs(toCol - fromCol);
    }

    function isValidRookMove(fromRow, fromCol, toRow, toCol) {
        return fromRow === toRow || fromCol === toCol;
    }

    function isValidQueenMove(fromRow, fromCol, toRow, toCol) {
        return isValidBishopMove(fromRow, fromCol, toRow, toCol) || 
               isValidRookMove(fromRow, fromCol, toRow, toCol);
    }

    function isValidKingMove(fromRow, fromCol, toRow, toCol) {
        return Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1;
    }

    function movePiece(fromSquare, toSquare) {
        const piece = fromSquare.querySelector('.piece');
        const capturedPiece = toSquare.querySelector('.piece');
        
        // Clear last move highlight
        if (lastMove) {
            document.querySelector(`[data-row="${lastMove.fromRow}"][data-col="${lastMove.fromCol}"]`).classList.remove('last-move');
            document.querySelector(`[data-row="${lastMove.toRow}"][data-col="${lastMove.toCol}"]`).classList.remove('last-move');
        }

        // Record move in algebraic notation
        const moveText = generateMoveText(fromSquare, toSquare, piece, capturedPiece);
        recordMove(moveText);

        // Update last move
        lastMove = {
            fromRow: parseInt(fromSquare.dataset.row),
            fromCol: parseInt(fromSquare.dataset.col),
            toRow: parseInt(toSquare.dataset.row),
            toCol: parseInt(toSquare.dataset.col)
        };

        // Highlight the move
        fromSquare.classList.add('last-move');
        toSquare.classList.add('last-move');

        // Handle capture
        if (capturedPiece) {
            if (capturedPiece.dataset.pieceType === 'king') {
                endGame(piece.dataset.color);
            }
            toSquare.removeChild(capturedPiece);
        }

        // Move the piece
        toSquare.appendChild(piece);

        // Handle pawn promotion
        if (piece.dataset.pieceType === 'pawn') {
            const row = parseInt(toSquare.dataset.row);
            if ((piece.dataset.color === 'white' && row === 0) || 
                (piece.dataset.color === 'black' && row === 7)) {
                promotePawn(piece);
            }
        }
    }

    function generateMoveText(fromSquare, toSquare, piece, capturedPiece) {
        const fromCol = String.fromCharCode(97 + parseInt(fromSquare.dataset.col));
        const fromRow = 8 - parseInt(fromSquare.dataset.row);
        const toCol = String.fromCharCode(97 + parseInt(toSquare.dataset.col));
        const toRow = 8 - parseInt(toSquare.dataset.row);
        
        let moveText = '';
        if (piece.dataset.pieceType !== 'pawn') {
            moveText += piece.dataset.pieceType.charAt(0).toUpperCase();
        }
        moveText += fromCol + fromRow;
        moveText += capturedPiece ? 'x' : '-';
        moveText += toCol + toRow;
        
        return moveText;
    }

    function recordMove(moveText) {
        const moveEntry = document.createElement('div');
        moveEntry.className = 'move-entry';
        if (currentPlayer === 'white') {
            moveEntry.textContent = `${moveCount}. ${moveText}`;
        } else {
            const lastEntry = moveHistory.lastElementChild;
            lastEntry.textContent += ` ${moveText}`;
            moveCount++;
            return;
        }
        moveHistory.appendChild(moveEntry);
        moveHistory.scrollTop = moveHistory.scrollHeight;
    }

    function promotePawn(pawn) {
        pawn.dataset.pieceType = 'queen';
        pawn.textContent = PIECES[pawn.dataset.color].queen;
    }

    function showValidMoves(square) {
        const piece = square.querySelector('.piece');
        if (!piece) return;

        const fromRow = parseInt(square.dataset.row);
        const fromCol = parseInt(square.dataset.col);

        // Check all squares
        document.querySelectorAll('.square').forEach(targetSquare => {
            const toRow = parseInt(targetSquare.dataset.row);
            const toCol = parseInt(targetSquare.dataset.col);

            if (isValidMove(square, targetSquare)) {
                targetSquare.classList.add('valid-move');
            }
        });
    }

    function clearHighlights() {
        document.querySelectorAll('.square').forEach(square => {
            square.classList.remove('selected', 'valid-move');
        });
    }

    function switchTurn() {
        currentPlayer = currentPlayer === 'white' ? 'black' : 'white';
        turnIndicator.textContent = `${currentPlayer.charAt(0).toUpperCase() + currentPlayer.slice(1)}'s Turn`;
    }

    function endGame(winner) {
        const gameOver = document.getElementById('game-over');
        const winnerText = document.getElementById('winner-text');
        winnerText.textContent = `${winner.charAt(0).toUpperCase() + winner.slice(1)} wins!`;
        gameOver.style.display = 'block';
    }

    // Initialize the game
    createBoard();
}); 