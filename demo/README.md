# Tic Tac Toe Spring Boot Application

## Overview
This is a Spring Boot application that implements a Tic Tac Toe game. The application provides a backend service with WebSocket support for real-time game interactions.

## Technologies
- Java 21
- Spring Boot 3.4.4
- Hibernate Reactive 2.4.5
- PostgreSQL (with Vertx PostgreSQL Client)
- Mutiny for reactive programming
- WebSockets for real-time communication
- JWT Authentication
- Flyway for database migrations

## Prerequisites
- JDK 21
- Maven
- PostgreSQL database

## Building the Application
```bash
mvn clean install
```

## Running the Application
```bash
mvn spring-boot:run
```

## Project Structure
- `src/main/java/com/example/demo` - Application source code
    - `api/controller` - REST and WebSocket controllers
    - (Other packages organized by feature)

## Features
- Real-time game play using WebSockets
- User authentication with JWT
- Reactive database operations with Hibernate Reactive
- PostgreSQL database for data persistence

## Configuration
The application uses environment variables for configuration. Create a `.env` file in the project root or set the following environment variables (`.env.sample`):
- Database connection details
- JWT secret
- Other application-specific configurations

