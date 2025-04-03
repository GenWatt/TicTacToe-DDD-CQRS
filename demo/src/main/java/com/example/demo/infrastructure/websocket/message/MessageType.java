package com.example.demo.infrastructure.websocket.message;

public enum MessageType {
    JOIN_MATCHMAKING,
    LEAVE_MATCHMAKING,
    JOIN_GAME,
    LEAVE_GAME,
    PLAY_MOVE,
    CONNECTION_ESTABLISHED,
    CONNECTION_CLOSED
}
