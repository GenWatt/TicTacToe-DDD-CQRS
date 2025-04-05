package com.example.demo.infrastructure.websocket.handler;

import org.springframework.web.socket.WebSocketSession;

import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMessageHandler<T> implements WebSocketMessageHandler {

    protected final ObjectMapper objectMapper;
    protected final WebSocketSessionService sessionService;
    private final Class<T> messageClass;

    @Override
    public Uni<Void> handle(WebSocketSession session, String payload) {
        return convertPayload(payload)
                .onItem().transformToUni(message -> handleMessage(session, message))
                .onFailure().invoke(error -> {
                    log.error("Error handling message: {}", error.getMessage());
                    sessionService.sendErrorMessage(session, "Error parsing message: " + error.getMessage());
                });
    }

    /**
     * Convert the payload to the required message type
     */
    protected Uni<T> convertPayload(String payload) {
        return Uni.createFrom().item(() -> {
            try {
                return objectMapper.readValue(payload, messageClass);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse message payload: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid message format", e);
            }
        });
    }

    /**
     * Handle the typed message
     */
    protected abstract Uni<Void> handleMessage(WebSocketSession session, T message);
}