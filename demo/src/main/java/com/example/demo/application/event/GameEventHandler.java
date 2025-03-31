package com.example.demo.application.event;

import com.example.demo.application.dto.GameEndedResponseDto;
import com.example.demo.application.dto.MakeMoveResponseDto;
import com.example.demo.domain.dto.GameStateDto;
import com.example.demo.domain.event.GameEndedEvent;
import com.example.demo.domain.event.MovePlayedEvent;
import com.example.demo.domain.service.GameService;
import com.example.demo.infrastructure.websocket.GameWebSocketHandler;
import com.example.demo.infrastructure.websocket.message.WebSocketResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventHandler {
    private final GameWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    @EventListener
    public void handleMovePlayedEvent(MovePlayedEvent event) throws JsonProcessingException {
        log.info("Move played in game {}: Player {} moved to ({},{}), Next player: {}",
                event.getGameId(), event.getPlayerId(), event.getMove().getX(), event.getMove().getY(),
                event.getNextPlayerId());

        sendMovePlayedResponse(event);
    }

    private void sendMovePlayedResponse(MovePlayedEvent event) {
        try {
            MakeMoveResponseDto responseDto = new MakeMoveResponseDto(
                    event.getBoard(),
                    event.getMove(),
                    event.getNextPlayerId());

            String payload = objectMapper.writeValueAsString(responseDto);

            WebSocketResponse response = new WebSocketResponse("PLAY_MOVE", payload);
            event.getPlayerIds()
                    .forEach(playerId -> webSocketHandler.sendToPlayer(playerId, response));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Async
    @EventListener
    public void handleGameEndedEvent(GameEndedEvent event) {
        log.info("Game {} ended. Winner: {}", event.getGame().getId().getId(),
                event.getWinnerId() != null ? event.getWinnerId() : "Draw");

        try {
            GameEndedResponseDto responseDto = new GameEndedResponseDto(
                    event.getGame().getBoard(),
                    event.getWinnerId(),
                    event.getGame().getState());

            String payload = objectMapper.writeValueAsString(responseDto);

            WebSocketResponse response = new WebSocketResponse("GAME_ENDED", payload);

            log.info("Sending game ended notification to {} players", event.getGame().getPlayerIds().size());

            event.getGame().getPlayerIds().forEach(playerId -> {
                log.info("Sending game ended notification to player {}", playerId);
                webSocketHandler.sendToPlayer(playerId, response);
            });
        } catch (JsonProcessingException e) {
            log.error("Error serializing game ended response", e);
        }
    }
}