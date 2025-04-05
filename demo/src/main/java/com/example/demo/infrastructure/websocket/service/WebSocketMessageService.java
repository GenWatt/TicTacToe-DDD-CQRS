package com.example.demo.infrastructure.websocket.service;

import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.WebSocketResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

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
     * @return Uni<Void> that completes when the message is sent
     */
    public void sendToPlayer(PlayerId playerId, String messageType, Object payload) {
        log.info("Preparing to send {} message to player {}", messageType, playerId);
        createResponse(messageType, payload)
                .flatMap(response -> sessionService.sendToPlayer(playerId, response))
                .onFailure().invoke(error -> log.error("Failed to send {} message to player {}: {}",
                        messageType, playerId, error.getMessage()))
                .onFailure().recoverWithNull()
                .subscribe().with(
                        success -> log.debug("Successfully sent {} message to player {}", messageType, playerId),
                        error -> log.error("Error in subscription when sending to player {}: {}", playerId,
                                error.getMessage()));
    }

    /**
     * Sends a typed response to multiple players with automatic JSON serialization.
     *
     * @param playerIds   Collection of player IDs to send to
     * @param messageType The type of message
     * @param payload     The payload object (will be serialized to JSON)
     */
    public void sendToPlayers(Collection<PlayerId> playerIds, String messageType, Object payload) {
        log.info("Preparing to send {} message to {} players", messageType, playerIds.size());
        createResponse(messageType, payload)
                .flatMap(response -> {
                    // Create a Uni for each player and combine them
                    return Uni.join().all(
                            playerIds.stream()
                                    .map(playerId -> sessionService.sendToPlayer(playerId, response)
                                            .onFailure()
                                            .invoke(error -> log.error("Failed to send {} message to player {}: {}",
                                                    messageType, playerId, error.getMessage()))
                                            .onFailure().recoverWithNull())
                                    .toList())
                            .andCollectFailures()
                            .replaceWithVoid();
                })
                .subscribe().with(
                        success -> log.debug("Successfully sent {} message to all players", messageType),
                        error -> log.error("Error in subscription when sending to multiple players: {}",
                                error.getMessage()));
    }

    /**
     * Creates a WebSocketResponse from a message type and payload.
     * 
     * @param messageType The type of message
     * @param payload     The payload to serialize
     * @return Uni<WebSocketResponse> with the created response
     */
    private Uni<WebSocketResponse> createResponse(String messageType, Object payload) {
        return Uni.createFrom().item(() -> {
            try {
                if (payload instanceof String) {
                    return new WebSocketResponse(messageType, (String) payload);
                } else {
                    String serialized = objectMapper.writeValueAsString(payload);
                    log.debug("Serialized payload for {}: {}", messageType, serialized);
                    return new WebSocketResponse(messageType, serialized);
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize message payload of type {}", messageType, e);
                throw new RuntimeException("Failed to serialize message payload of type " + messageType, e);
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }
}