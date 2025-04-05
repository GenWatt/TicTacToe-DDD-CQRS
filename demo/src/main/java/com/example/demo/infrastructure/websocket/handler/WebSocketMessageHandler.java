package com.example.demo.infrastructure.websocket.handler;

import org.springframework.web.socket.WebSocketSession;

import com.example.demo.infrastructure.websocket.message.MessageType;

import io.smallrye.mutiny.Uni;

public interface WebSocketMessageHandler {
    /**
     * Handle a WebSocket message
     * 
     * @param session the WebSocket session
     * @param payload the message payload
     * @return Uni<Void> that completes when processing is done
     */
    Uni<Void> handle(WebSocketSession session, String payload);

    /**
     * Get the message type this handler can process
     * 
     * @return the message type
     */
    MessageType getType();
}