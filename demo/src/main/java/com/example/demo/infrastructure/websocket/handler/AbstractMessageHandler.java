package com.example.demo.infrastructure.websocket.handler;

import org.springframework.web.socket.WebSocketSession;

import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMessageHandler<T> implements WebSocketMessageHandler {

    protected final ObjectMapper objectMapper;
    protected final WebSocketSessionService sessionService;
    private final Class<T> messageClass;

    @Override
    public void handle(WebSocketSession session, String payload) {
        try {
            T message = objectMapper.readValue(payload, messageClass);
            handleMessage(session, message);
        } catch (JsonProcessingException e) {
            log.error("Error processing message: {}", payload, e);
            sessionService.sendErrorMessage(session, "Invalid message format: " + e.getMessage());
        }
    }

    protected abstract void handleMessage(WebSocketSession session, T message);
}