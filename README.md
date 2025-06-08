# Game Center

A web-based game center featuring Chess and Tic-Tac-Toe games built with Spring Boot.

## Features

- Tic-Tac-Toe game with AI opponent
- Chess game with move validation
- Modern, responsive UI
- Session-based game state management

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Local Development

1. Clone the repository
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Visit `http://localhost:8081` in your browser

## Deployment Options

### Option 1: Deploy to Render (Recommended)

1. Fork this repository to your GitHub account
2. Create a new Web Service on Render
3. Connect your GitHub repository
4. Use the following settings:
   - Build Command: `mvn clean install`
   - Start Command: `java -jar target/gamecenter-1.0-SNAPSHOT.jar`
   - Select Java 17
5. Click "Create Web Service"

### Option 2: Deploy to Railway

1. Fork this repository
2. Create a new project on Railway
3. Add your GitHub repository
4. Railway will automatically detect the Java configuration
5. Deploy!

### Option 3: Manual VPS Deployment

1. SSH into your server
2. Install Java 17 and Maven
3. Clone the repository
4. Build: `mvn clean install`
5. Run: `java -jar target/gamecenter-1.0-SNAPSHOT.jar`
6. (Optional) Set up Nginx as a reverse proxy

## Environment Variables

- `PORT`: Port number (default: 8081)
- `JAVA_OPTS`: Java runtime options

## Support

For issues or questions, please open a GitHub issue. 