const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const scoreElement = document.getElementById('score');
const highScoreElement = document.getElementById('highScore');

// Set canvas size
canvas.width = 800;
canvas.height = 400;

// Game constants
const GROUND_HEIGHT = canvas.height * 0.75;
const DINO_WIDTH = 40;
const DINO_HEIGHT = 50;
const DINO_X = 100;
const DUCK_HEIGHT = 30;
const JUMP_FORCE = -15;
const GRAVITY = 0.8;
const CACTUS_WIDTH = 30;
const BIRD_WIDTH = 40;
const BIRD_HEIGHT = 30;

// Colors
const GROUND_COLOR = '#C8E6B4';
const CACTUS_COLOR = '#285040';
const DINO_COLOR = '#32CD32';
const SKY_COLORS = {
    day: { start: '#87CEEB', end: '#E0F6FF' },
    night: { start: '#191970', end: '#000088' }
};

// Game state
let dino = {
    x: DINO_X,
    y: GROUND_HEIGHT,
    width: DINO_WIDTH,
    height: DINO_HEIGHT,
    velocity: 0,
    isJumping: false,
    isDucking: false
};

let gameState = {
    score: 0,
    highScore: localStorage.getItem('highScore') || 0,
    isGameOver: false,
    obstacles: [],
    birds: [],
    clouds: [],
    stars: [],
    timeOfDay: 0,
    isNight: false,
    animationFrame: 0
};

// Initialize stars
function initStars() {
    gameState.stars = [];
    for (let i = 0; i < 50; i++) {
        gameState.stars.push({
            x: Math.random() * canvas.width,
            y: Math.random() * (GROUND_HEIGHT - 50),
            brightness: Math.random()
        });
    }
}

// Initialize clouds
function initClouds() {
    for (let i = 0; i < 3; i++) {
        gameState.clouds.push({
            x: Math.random() * canvas.width,
            y: Math.random() * (GROUND_HEIGHT - 100),
            width: 60 + Math.random() * 40,
            height: 30 + Math.random() * 20,
            speed: 1 + Math.random()
        });
    }
}

// Event listeners
document.addEventListener('keydown', (event) => {
    if (event.code === 'Space') {
        event.preventDefault();
        if (gameState.isGameOver) {
            resetGame();
        } else if (!dino.isJumping) {
            dino.velocity = JUMP_FORCE;
            dino.isJumping = true;
        }
    } else if (event.code === 'ArrowDown') {
        event.preventDefault();
        dino.isDucking = true;
        if (!dino.isJumping) {
            dino.height = DUCK_HEIGHT;
        }
    }
});

document.addEventListener('keyup', (event) => {
    if (event.code === 'ArrowDown') {
        dino.isDucking = false;
        if (!dino.isJumping) {
            dino.height = DINO_HEIGHT;
        }
    }
});

// Game functions
function updateDino() {
    if (dino.isJumping) {
        dino.velocity += GRAVITY;
        dino.y += dino.velocity;

        if (dino.y >= GROUND_HEIGHT) {
            dino.y = GROUND_HEIGHT;
            dino.isJumping = false;
            dino.velocity = 0;
            dino.height = dino.isDucking ? DUCK_HEIGHT : DINO_HEIGHT;
        }
    }
}

function addObstacle() {
    if (Math.random() < 0.7) { // 70% chance for cactus
        gameState.obstacles.push({
            x: canvas.width,
            y: GROUND_HEIGHT,
            width: CACTUS_WIDTH,
            height: 30 + Math.random() * 50,
            type: 'cactus'
        });
    } else { // 30% chance for bird
        gameState.birds.push({
            x: canvas.width,
            y: GROUND_HEIGHT - 100 - Math.random() * 100,
            width: BIRD_WIDTH,
            height: BIRD_HEIGHT,
            wingUp: false,
            wingTimer: 0
        });
    }
}

function updateObstacles() {
    const speed = 5 + gameState.score / 100;

    gameState.obstacles = gameState.obstacles.filter(obstacle => {
        obstacle.x -= speed;
        return obstacle.x + obstacle.width > 0;
    });

    gameState.birds = gameState.birds.filter(bird => {
        bird.x -= speed;
        bird.wingTimer++;
        if (bird.wingTimer > 15) {
            bird.wingUp = !bird.wingUp;
            bird.wingTimer = 0;
        }
        return bird.x + bird.width > 0;
    });

    if (gameState.obstacles.length === 0 && gameState.birds.length === 0) {
        addObstacle();
    }
}

function updateClouds() {
    gameState.clouds.forEach(cloud => {
        cloud.x -= 1;
        if (cloud.x + cloud.width < 0) {
            cloud.x = canvas.width;
            cloud.y = Math.random() * (GROUND_HEIGHT - 100);
        }
    });
}

function checkCollisions() {
    const dinoHitbox = {
        x: dino.x + 5,
        y: dino.y + 5,
        width: dino.width - 10,
        height: dino.height - 10
    };

    for (const obstacle of gameState.obstacles) {
        if (checkCollision(dinoHitbox, obstacle)) {
            gameOver();
            return;
        }
    }

    for (const bird of gameState.birds) {
        if (checkCollision(dinoHitbox, bird)) {
            gameOver();
            return;
        }
    }
}

function checkCollision(rect1, rect2) {
    return rect1.x < rect2.x + rect2.width &&
           rect1.x + rect1.width > rect2.x &&
           rect1.y < rect2.y + rect2.height &&
           rect1.y + rect1.height > rect2.y;
}

function drawDino() {
    // Body
    ctx.fillStyle = DINO_COLOR;
    ctx.fillRect(dino.x, dino.y, dino.width, dino.height);

    // Head
    ctx.fillRect(dino.x + 25, dino.y - 15, 20, 25);
    ctx.beginPath();
    ctx.arc(dino.x + 47, dino.y - 12, 12, 0, Math.PI * 2);
    ctx.fill();

    // Eye
    ctx.fillStyle = 'white';
    ctx.beginPath();
    ctx.arc(dino.x + 50, dino.y - 12, 5, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = 'black';
    ctx.beginPath();
    ctx.arc(dino.x + 51, dino.y - 12, 3, 0, Math.PI * 2);
    ctx.fill();

    // Expression
    ctx.strokeStyle = 'black';
    ctx.lineWidth = 2;
    if (dino.isJumping) {
        // Determined expression
        ctx.beginPath();
        ctx.moveTo(dino.x + 45, dino.y - 8);
        ctx.lineTo(dino.x + 55, dino.y - 8);
        ctx.stroke();
    } else {
        // Happy smile
        ctx.beginPath();
        ctx.arc(dino.x + 50, dino.y - 8, 6, 0, Math.PI);
        ctx.stroke();
    }
}

function drawCactus(obstacle) {
    ctx.fillStyle = CACTUS_COLOR;
    
    // Main stem
    ctx.fillRect(obstacle.x, obstacle.y - obstacle.height, 
                obstacle.width/2, obstacle.height);

    if (obstacle.height > 40) {
        // Side branches
        ctx.fillRect(obstacle.x - obstacle.width/4, 
                    obstacle.y - obstacle.height/1.5, 
                    obstacle.width/2, obstacle.height/3);
        ctx.fillRect(obstacle.x + obstacle.width/4, 
                    obstacle.y - obstacle.height/2, 
                    obstacle.width/2, obstacle.height/3);
    }

    // Spikes
    ctx.strokeStyle = '#1A4030';
    ctx.lineWidth = 1;
    for (let i = 0; i < obstacle.height; i += 5) {
        // Left spikes
        ctx.beginPath();
        ctx.moveTo(obstacle.x, obstacle.y - i);
        ctx.lineTo(obstacle.x - 3, obstacle.y - i + 2);
        ctx.stroke();
        // Right spikes
        ctx.beginPath();
        ctx.moveTo(obstacle.x + obstacle.width/2, obstacle.y - i);
        ctx.lineTo(obstacle.x + obstacle.width/2 + 3, obstacle.y - i + 2);
        ctx.stroke();
    }
}

function drawBird(bird) {
    ctx.fillStyle = '#FF4500';
    
    // Body
    ctx.beginPath();
    ctx.ellipse(bird.x + bird.width/2, bird.y + bird.height/2, 
                bird.width/2, bird.height/3, 0, 0, Math.PI * 2);
    ctx.fill();

    // Wings
    const wingHeight = bird.wingUp ? -10 : 10;
    ctx.beginPath();
    ctx.moveTo(bird.x + bird.width/3, bird.y + bird.height/2);
    ctx.quadraticCurveTo(bird.x + bird.width/2, bird.y + wingHeight,
                        bird.x + bird.width*2/3, bird.y + bird.height/2);
    ctx.stroke();
}

function drawGame() {
    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Draw sky
    const gradient = ctx.createLinearGradient(0, 0, 0, GROUND_HEIGHT);
    const colors = gameState.isNight ? SKY_COLORS.night : SKY_COLORS.day;
    gradient.addColorStop(0, colors.start);
    gradient.addColorStop(1, colors.end);
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, canvas.width, GROUND_HEIGHT);

    // Draw stars at night
    if (gameState.isNight) {
        gameState.stars.forEach(star => {
            ctx.fillStyle = `rgba(255, 255, 255, ${star.brightness})`;
            ctx.fillRect(star.x, star.y, 2, 2);
        });
    }

    // Draw clouds
    gameState.clouds.forEach(cloud => {
        ctx.fillStyle = gameState.isNight ? 
            'rgba(255, 255, 255, 0.3)' : 'rgba(255, 255, 255, 0.8)';
        ctx.beginPath();
        ctx.arc(cloud.x + cloud.width/2, cloud.y + cloud.height/2,
                cloud.width/2, 0, Math.PI * 2);
        ctx.fill();
    });

    // Draw ground
    ctx.fillStyle = GROUND_COLOR;
    ctx.fillRect(0, GROUND_HEIGHT, canvas.width, canvas.height - GROUND_HEIGHT);

    // Draw grass
    ctx.strokeStyle = '#90C070';
    ctx.lineWidth = 1;
    for (let x = 0; x < canvas.width; x += 15) {
        const height = 2 + Math.random() * 4;
        ctx.beginPath();
        ctx.moveTo(x, GROUND_HEIGHT);
        ctx.lineTo(x, GROUND_HEIGHT + height);
        ctx.stroke();
    }

    // Draw obstacles
    gameState.obstacles.forEach(drawCactus);
    gameState.birds.forEach(drawBird);

    // Draw dino
    drawDino();

    // Draw game over
    if (gameState.isGameOver) {
        ctx.fillStyle = 'rgba(0, 0, 0, 0.5)';
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        ctx.font = '48px Arial';
        ctx.fillStyle = '#FF2D2D';
        ctx.textAlign = 'center';
        ctx.fillText('Game Over!', canvas.width/2, canvas.height/2);

        ctx.font = '24px Arial';
        ctx.fillStyle = 'white';
        ctx.fillText('Press SPACE to restart', canvas.width/2, canvas.height/2 + 40);

        if (gameState.score > gameState.highScore) {
            ctx.fillStyle = '#FFD700';
            ctx.fillText('New High Score!', canvas.width/2, canvas.height/2 + 80);
        }
    }
}

function updateScore() {
    gameState.score++;
    scoreElement.textContent = `Score: ${gameState.score}`;
    
    if (gameState.score > gameState.highScore) {
        gameState.highScore = gameState.score;
        localStorage.setItem('highScore', gameState.highScore);
        highScoreElement.textContent = `High Score: ${gameState.highScore}`;
    }
}

function updateDayNightCycle() {
    gameState.timeOfDay += 0.1;
    if (gameState.timeOfDay >= 100) {
        gameState.isNight = !gameState.isNight;
        gameState.timeOfDay = 0;
        if (gameState.isNight) {
            initStars();
        }
    }
}

function gameOver() {
    gameState.isGameOver = true;
    if (gameState.score > gameState.highScore) {
        gameState.highScore = gameState.score;
        localStorage.setItem('highScore', gameState.highScore);
        highScoreElement.textContent = `High Score: ${gameState.highScore}`;
    }
}

function resetGame() {
    dino.y = GROUND_HEIGHT;
    dino.velocity = 0;
    dino.height = DINO_HEIGHT;
    dino.isJumping = false;
    dino.isDucking = false;

    gameState = {
        ...gameState,
        score: 0,
        isGameOver: false,
        obstacles: [],
        birds: [],
        timeOfDay: 0,
        isNight: false
    };

    scoreElement.textContent = 'Score: 0';
    initStars();
    initClouds();
}

function gameLoop() {
    if (!gameState.isGameOver) {
        updateDino();
        updateObstacles();
        updateClouds();
        updateDayNightCycle();
        checkCollisions();
        updateScore();
    }
    
    drawGame();
    gameState.animationFrame = requestAnimationFrame(gameLoop);
}

// Initialize game
initStars();
initClouds();
highScoreElement.textContent = `High Score: ${gameState.highScore}`;
gameLoop(); 