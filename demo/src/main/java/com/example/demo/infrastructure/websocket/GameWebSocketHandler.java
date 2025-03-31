package com.example.demo.infrastructure.websocket;

import com.example.demo.domain.service.GameService;
import com.example.demo.domain.service.MatchmakingService;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.message.JoinMatchmakingMessage;
import com.example.demo.infrastructure.websocket.message.MessageType;
import com.example.demo.infrastructure.websocket.message.PlayMoveMessage;
import com.example.demo.infrastructure.websocket.message.WebSocketResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, PlayerId> sessionToPlayerId = new ConcurrentHashMap<>();
    private final Map<PlayerId, WebSocketSession> playerIdToSession = new ConcurrentHashMap<>();

    private final GameService gameService;
    private final MatchmakingService matchmakingService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("New WebSocket connection: {}", session.getId());
        sessions.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        PlayerId playerId = sessionToPlayerId.get(sessionId);

        if (playerId != null) {
            matchmakingService.cancelQueue(playerId);
            playerIdToSession.remove(playerId);
            sessionToPlayerId.remove(sessionId);
        }

        sessions.remove(sessionId);
        log.info("WebSocket connection closed: {}", sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message: {}", payload);

        try {
            // Parse incoming WebSocket message
            MessageType messageType = determineMessageType(payload);

            switch (messageType) {
                case JOIN_MATCHMAKING:
                    JoinMatchmakingMessage joinMessage = objectMapper.readValue(payload, JoinMatchmakingMessage.class);
                    handleJoinMatchmaking(session, joinMessage);
                    break;
                case LEAVE_GAME:

                    break;
                case LEAVE_MATCHMAKING:

                    break;
                case PLAY_MOVE:
                    PlayMoveMessage playMoveMessage = objectMapper.readValue(payload, PlayMoveMessage.class);
                    handlePlayMove(session, playMoveMessage);
                    break;
                default:
                    // log.warn("Unknown message type: {}", webSocketMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
            sendErrorMessage(session, "Invalid message format");
        }
    }

    private void handleJoinMatchmaking(WebSocketSession session, JoinMatchmakingMessage message) throws IOException {
        PlayerId playerId = PlayerId.from(message.getPlayerId());
        sessionToPlayerId.put(session.getId(), playerId);
        playerIdToSession.put(playerId, session);

        matchmakingService.queuePlayer(playerId);
    }

    private void handlePlayMove(WebSocketSession session, PlayMoveMessage message) throws IOException {
        PlayerId playerId = sessionToPlayerId.get(session.getId());
        if (playerId == null) {
            sendErrorMessage(session, "Player not authenticated");
            return;
        }

        GameId gameId = GameId.from(message.getGameId());
        Move move = Move.create(message.getX(), message.getY(), playerId);

        gameService.makeMove(gameId, playerId, move)
                .subscribe().with(
                        unused -> log.info("Move made successfully"),
                        error -> {
                            log.error("Error making move", error);
                        });
        // log.info("Player {} made move {} in game {}", playerId, move, gameId);
    }

    private MessageType determineMessageType(String payload) throws IOException {
        // Extract just the type field for preliminary processing
        Map<String, Object> jsonMap = objectMapper.readValue(payload, Map.class);
        String typeStr = (String) jsonMap.get("type");
        return MessageType.valueOf(typeStr);
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) throws IOException {
        sendMessage(session, new WebSocketResponse("ERROR", errorMessage));
    }

    private void sendMessage(WebSocketSession session, WebSocketResponse message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    public void sendToPlayer(PlayerId playerId, WebSocketResponse message) {
        WebSocketSession session = playerIdToSession.get(playerId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message);
            } catch (IOException e) {
                log.error("Failed to send message to player: {}", playerId, e);
            }
        }
    }
}