package com.example.demo.infrastructure.websocket.handler;

import com.example.demo.domain.service.GameService;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.MessageType;
import com.example.demo.infrastructure.websocket.message.PlayMoveMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public void handleMessage(WebSocketSession session, PlayMoveMessage message) {
        PlayerId playerId = sessionService.getPlayerIdBySession(session);

        if (playerId == null) {
            sessionService.sendErrorMessage(session, "Player not authenticated");
            return;
        }

        GameId gameId = GameId.from(message.getGameId());
        Move move = Move.create(message.getX(), message.getY(), playerId);

        gameService.makeMove(gameId, playerId, move)
                .subscribe().with(
                        unused -> log.info("Move made successfully"),
                        error -> log.error("Error making move", error));
    }

    @Override
    public MessageType getType() {
        return MessageType.PLAY_MOVE;
    }
}
