package com.example.demo.infrastructure.websocket.handler;

import com.example.demo.domain.service.GameService;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.MessageType;
import com.example.demo.infrastructure.websocket.message.PlayMoveMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@MessageTypeHandler(MessageType.PLAY_MOVE)
@RequiredArgsConstructor
public class PlayMoveHandler implements WebSocketMessageHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GameService gameService;
    private final WebSocketSessionService sessionService;

    @Override
    public void handle(WebSocketSession session, String payload) throws Exception {
        log.info("Handling play move message: {}", payload);
        PlayMoveMessage message = objectMapper.readValue(payload, PlayMoveMessage.class);
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
