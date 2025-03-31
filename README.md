# Tic Tac Toe

A real-time Tic Tac Toe game built with Java Spring Boot and Vue 3.

## Stack

**Backend:**

* Java 21
* Spring Boot 3.4.4
* Mutiny (Reactive programming)
* Reactive Hibernate
* Maven
* Docker
* WebSockets
* PostgreSQL
* DDD (Domain-Driven Design)
* CQRS (Command Query Responsibility Segregation)
* Events
* Clean Architecture

**Frontend:**

* Vue 3
* Pinia (State management)
* Axios (HTTP client)
* WebSockets
* TypeScript

## To Run

**Backend:**

1.  Ensure you have Java 21 installed.
2.  Navigate to the `backend` directory.
3.  Install dependencies using Maven: `mvn clean install`
4.  Run PostgreSQL.
5.  Execute database migrations located in `src/main/resources/db/migration`. (Use Flyway or Liquibase based on your setup)
6.  Run the Spring Boot application.

**Frontend:**

1.  Navigate to the `frontend` directory.
2.  Install dependencies using npm: `npm install`
3.  Run the Vue development server: `npm run dev`

## Description

This is a simple Tic Tac Toe game where users can:

* Set their username.
* Enter a matchmaking queue to find an opponent.
* Play a real-time Tic Tac Toe game using WebSockets.

## To Do

* Proper user registration.
* Implement comprehensive unit tests with JUnit.
* Add functionality to restart the game.
* Refactor the frontend codebase for improved maintainability.
* Implement robust error handling.
* Set up a GitHub Actions workflow for CI/CD.
