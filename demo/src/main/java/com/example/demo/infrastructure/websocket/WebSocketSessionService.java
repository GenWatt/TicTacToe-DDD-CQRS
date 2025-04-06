package com.example.demo.infrastructure.websocket;

import com.example.demo.application.dto.ErrorResponseDto;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.message.WebSocketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketSessionService {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, PlayerId> sessionToPlayerId = new ConcurrentHashMap<>();
    private final Map<PlayerId, WebSocketSession> playerIdToSession = new ConcurrentHashMap<>();

    public Uni<Void> registerSession(WebSocketSession session) {
        return Uni.createFrom().item(() -> {
            log.info("Registering WebSocket session: {}", session.getId());
            sessions.put(session.getId(), session);
            return null;
        });
    }

    public Uni<Void> registerPlayer(WebSocketSession session, PlayerId playerId) {
        return Uni.createFrom().item(() -> {
            String sessionId = session.getId();
            sessionToPlayerId.put(sessionId, playerId);
            playerIdToSession.put(playerId, session);
            log.info("Player {} registered with session {}", playerId, sessionId);
            return null;
        });
    }

    public Uni<Void> removeSession(WebSocketSession session) {
        return Uni.createFrom().item(() -> {
            String sessionId = session.getId();
            PlayerId playerId = sessionToPlayerId.get(sessionId);

            if (playerId != null) {
                playerIdToSession.remove(playerId);
                sessionToPlayerId.remove(sessionId);
                log.info("Player {} unregistered from session {}", playerId, sessionId);
            }

            sessions.remove(sessionId);
            log.info("WebSocket session removed: {}", sessionId);
            return null;
        });
    }

    public Uni<PlayerId> getPlayerIdBySession(WebSocketSession session) {
        return Uni.createFrom().item(() -> sessionToPlayerId.get(session.getId()));
    }

    public Uni<WebSocketSession> getSessionByPlayerId(PlayerId playerId) {
        return Uni.createFrom().item(() -> playerIdToSession.get(playerId));
    }

    public Uni<Void> sendErrorMessage(WebSocketSession session, String errorMessage) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .message(errorMessage)
                .build();

        return Uni.createFrom().item(() -> {
            try {
                String json = objectMapper.writeValueAsString(errorResponse);
                return new WebSocketResponse("ERROR", json);
            } catch (Exception e) {
                log.error("Failed to create error message for session: {}", session.getId(), e);
                throw new RuntimeException("Failed to create error message", e);
            }
        }).chain(response -> sendMessage(session, response));
    }

    public Uni<Void> sendMessage(WebSocketSession session, WebSocketResponse message) {
        return Uni.createFrom().item(() -> {
            // Synchronize on the session object to prevent concurrent writes
            synchronized (session) {
                if (session.isOpen()) {
                    try {
                        String json = objectMapper.writeValueAsString(message);
                        session.sendMessage(new TextMessage(json));
                    } catch (Exception e) {
                        log.error("Failed to send message to session: {}", session.getId(), e);
                        throw new RuntimeException("Failed to send message", e);
                    }
                } else {
                    log.warn("Attempted to send message to closed session: {}", session.getId());
                }
            }
            return null;
        });
    }

    public Uni<Void> sendToPlayer(PlayerId playerId, WebSocketResponse message) {
        System.out.println("2222222222222222222Sending message to player: " + playerId + ", message: " + message);
        return getSessionByPlayerId(playerId)
                .onItem().ifNotNull().transformToUni(session -> {
                    log.info("Sending message to player {}: {}", playerId, message);
                    return sendMessage(session, message);
                })
                .replaceWithVoid();
    }
}