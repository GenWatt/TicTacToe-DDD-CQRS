package com.example.demo.infrastructure.websocket;

import com.example.demo.application.dto.ErrorResponseDto;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.message.WebSocketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
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

    public void registerSession(WebSocketSession session) {
        log.info("Registering WebSocket session: {}", session.getId());
        sessions.put(session.getId(), session);
    }

    public void registerPlayer(WebSocketSession session, PlayerId playerId) {
        String sessionId = session.getId();

        sessionToPlayerId.put(sessionId, playerId);
        playerIdToSession.put(playerId, session);
        log.info("Player {} registered with session {}", playerId, sessionId);
    }

    public void removeSession(WebSocketSession session) {
        String sessionId = session.getId();
        PlayerId playerId = sessionToPlayerId.get(sessionId);

        if (playerId != null) {
            playerIdToSession.remove(playerId);
            sessionToPlayerId.remove(sessionId);
            log.info("Player {} unregistered from session {}", playerId, sessionId);
        }

        sessions.remove(sessionId);
        log.info("WebSocket session removed: {}", sessionId);
    }

    public PlayerId getPlayerIdBySession(WebSocketSession session) {
        return sessionToPlayerId.get(session.getId());
    }

    public WebSocketSession getSessionByPlayerId(PlayerId playerId) {
        return playerIdToSession.get(playerId);
    }

    public void sendErrorMessage(WebSocketSession session, String errorMessage) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .message(errorMessage)
                .build();

        try {
            String json = objectMapper.writeValueAsString(errorResponse);
            sendMessage(session, new WebSocketResponse("ERROR", json));
        } catch (Exception e) {
            log.error("Failed to send error message to session: {}", session.getId(), e);
        }
    }

    public void sendMessage(WebSocketSession session, WebSocketResponse message) {
        if (session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.error("Failed to send message to session: {}", session.getId(), e);
            }
        }
    }

    public void sendToPlayer(PlayerId playerId, WebSocketResponse message) {
        WebSocketSession session = getSessionByPlayerId(playerId);
        if (session != null) {
            sendMessage(session, message);
        } else {
            log.warn("No session found for player: {}", playerId);
        }
    }
}