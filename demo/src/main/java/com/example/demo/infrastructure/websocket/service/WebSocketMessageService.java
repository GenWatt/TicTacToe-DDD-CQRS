package com.example.demo.infrastructure.websocket.service;

import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.WebSocketResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageService {

    private final WebSocketSessionService sessionService;
    private final ObjectMapper objectMapper;

    /**
     * Sends a typed response to a player with automatic JSON serialization.
     *
     * @param playerId    The player to send the message to
     * @param messageType The type of message
     * @param payload     The payload object (will be serialized to JSON)
     */
    public void sendToPlayer(PlayerId playerId, String messageType, Object payload) {
        WebSocketResponse response = createResponse(messageType, payload);
        if (response != null) {
            sessionService.sendToPlayer(playerId, response);
        }
    }

    /**
     * Sends a typed response to multiple players with automatic JSON serialization.
     *
     * @param playerIds   Collection of player IDs to send to
     * @param messageType The type of message
     * @param payload     The payload object (will be serialized to JSON)
     */
    public void sendToPlayers(Collection<PlayerId> playerIds, String messageType, Object payload) {
        WebSocketResponse response = createResponse(messageType, payload);

        if (response != null) {
            playerIds.forEach(playerId -> sessionService.sendToPlayer(playerId, response));
        }
    }

    /**
     * Executes an action with proper exception handling
     */
    public <T> void withErrorHandling(T data, Consumer<T> action, String operation) {
        try {
            action.accept(data);
        } catch (Exception e) {
            log.error("Error during {}", operation, e);
        }
    }

    private WebSocketResponse createResponse(String messageType, Object payload) {
        try {
            if (payload instanceof String) {
                return new WebSocketResponse(messageType, (String) payload);
            } else {
                String serialized = objectMapper.writeValueAsString(payload);
                return new WebSocketResponse(messageType, serialized);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message payload of type {}", messageType, e);
            return null;
        }
    }
}