package com.example.demo.infrastructure.websocket.handler;

import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.JoinMatchmakingMessage;
import com.example.demo.infrastructure.websocket.message.MessageType;
import com.example.demo.infrastructure.websocket.service.MatchmakingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@MessageTypeHandler(MessageType.JOIN_MATCHMAKING)
public class JoinMatchmakingHandler extends AbstractMessageHandler<JoinMatchmakingMessage> {

    private final MatchmakingService matchmakingService;
    private final WebSocketSessionService sessionService;

    public JoinMatchmakingHandler(
            ObjectMapper objectMapper,
            MatchmakingService matchmakingService,
            WebSocketSessionService sessionService) {
        super(objectMapper, sessionService, JoinMatchmakingMessage.class);

        this.matchmakingService = matchmakingService;
        this.sessionService = sessionService;
    }

    @Override
    public Uni<Void> handleMessage(WebSocketSession session, JoinMatchmakingMessage message) {
        PlayerId playerId = PlayerId.from(message.getPlayerId());
        log.info("Handling JOIN_MATCHMAKING for player: {}", playerId);
        // Create a reactive chain for registration and queueing
        return sessionService.registerPlayer(session, playerId)
                .chain(() -> matchmakingService.queuePlayer(playerId))
                .onItem().invoke(() -> log.info("Player {} joined matchmaking queue", playerId))
                .onFailure().invoke(error -> {
                    log.error("Failed to queue player {}: {}", playerId, error.getMessage());
                    sessionService.sendErrorMessage(session, "Failed to join matchmaking: " + error.getMessage());
                })
                .replaceWithVoid();
    }

    @Override
    public MessageType getType() {
        return MessageType.JOIN_MATCHMAKING;
    }
}