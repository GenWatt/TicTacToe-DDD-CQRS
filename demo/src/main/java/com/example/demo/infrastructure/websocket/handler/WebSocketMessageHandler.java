package com.example.demo.infrastructure.websocket.handler;

import org.springframework.web.socket.WebSocketSession;

import com.example.demo.infrastructure.websocket.message.MessageType;

public interface WebSocketMessageHandler {
    void handle(WebSocketSession session, String payload);

    MessageType getType();
}
