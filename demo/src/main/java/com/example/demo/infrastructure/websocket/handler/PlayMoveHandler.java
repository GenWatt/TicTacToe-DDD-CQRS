package com.example.demo.infrastructure.websocket.handler;

import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.MessageType;
import com.example.demo.infrastructure.websocket.message.PlayMoveMessage;
import com.example.demo.infrastructure.websocket.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@MessageTypeHandler(MessageType.PLAY_MOVE)
public class PlayMoveHandler extends AbstractMessageHandler<PlayMoveMessage> {

    private final GameService gameService;
    private final WebSocketSessionService sessionService;

    public PlayMoveHandler(
            ObjectMapper objectMapper,
            GameService gameService,
            WebSocketSessionService sessionService) {

        super(objectMapper, sessionService, PlayMoveMessage.class);
        this.gameService = gameService;
        this.sessionService = sessionService;
    }

    @Override
    public Uni<Void> handleMessage(WebSocketSession session, PlayMoveMessage message) {
        // Create a reactive chain for the entire operation
        return sessionService.getPlayerIdBySession(session)
                .onItem().ifNull().continueWith(() -> {
                    sessionService.sendErrorMessage(session, "Player not authenticated");
                    return null;
                })
                .onItem().ifNotNull().transformToUni(playerId -> processMove(session, message, playerId))
                .onFailure().invoke(error -> {
                    log.error("Error processing move: {}", error.getMessage());
                    sessionService.sendErrorMessage(session, "Error processing move: " + error.getMessage());
                });
    }

    private Uni<Void> processMove(WebSocketSession session, PlayMoveMessage message, PlayerId playerId) {
        GameId gameId = GameId.from(message.getGameId());
        Move move = Move.create(message.getX(), message.getY(), playerId);

        log.info("Player {} attempting move at ({},{}) in game {}",
                playerId, message.getX(), message.getY(), gameId);

        return gameService.makeMove(gameId, playerId, move)
                .onItem().invoke(() -> log.info("Move successfully made by player {} in game {}", playerId, gameId))
                .onFailure().invoke(error -> {
                    log.error("Failed to make move: {}", error.getMessage());
                    sessionService.sendErrorMessage(session, "Failed to make move: " + error.getMessage());
                });
    }

    @Override
    public MessageType getType() {
        return MessageType.PLAY_MOVE;
    }
}