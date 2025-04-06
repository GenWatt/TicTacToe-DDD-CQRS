package com.example.demo.application.event;

import com.example.demo.application.dto.GameEndedResponseDto;
import com.example.demo.application.dto.MakeMoveResponseDto;
import com.example.demo.domain.event.GameEndedByDisconnectionEvent;
import com.example.demo.domain.event.GameEndedEvent;
import com.example.demo.domain.event.MovePlayedEvent;
import com.example.demo.infrastructure.websocket.service.WebSocketMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventHandler {

    private final WebSocketMessageService webSocketMessageService;

    @Async
    @EventListener
    public void handleMovePlayedEvent(MovePlayedEvent event) {
        log.info("Move played in game {}: Player {} moved to ({},{}), Next player: {}",
                event.getGameId(), event.getPlayerId(), event.getMove().getX(), event.getMove().getY(),
                event.getNextPlayerId());

        MakeMoveResponseDto responseDto = new MakeMoveResponseDto(
                event.getBoard(),
                event.getMove(),
                event.getNextPlayerId());

        webSocketMessageService.sendToPlayers(event.getPlayerIds(), "PLAY_MOVE", responseDto)
                .subscribe().with(
                        success -> log.info("Successfully sent PLAY_MOVE message to players {}", event.getPlayerIds()),
                        failure -> log.error("Failed to send PLAY_MOVE message to players {}: {}",
                                event.getPlayerIds(), failure.getMessage()));
    }

    @Async
    @EventListener
    public void handleGameEndedEvent(GameEndedEvent event) {
        log.info("Game {} ended. Winner: {}", event.getGame().getId().getId(),
                event.getWinnerId() != null ? event.getWinnerId() : "Draw");

        handleGameEnd(event);
    }

    @Async
    @EventListener
    public void handleGameEndedByDisconnectionEvent(GameEndedByDisconnectionEvent event) {
        log.info("Game {} ended by disconnection. Winner: {}", event.getGame().getId().getId(),
                event.getWinnerId() != null ? event.getWinnerId() : "Draw");

        handleGameEnd(event);
    }

    private void handleGameEnd(GameEndedEvent event) {
        GameEndedResponseDto responseDto = new GameEndedResponseDto(
                event.getGame().getBoard(),
                event.getWinnerId(),
                event.getGame().getState());

        webSocketMessageService.sendToPlayers(event.getGame().getPlayerIds(), "GAME_ENDED", responseDto)
                .subscribe().with(
                        success -> log.info("Successfully sent GAME_ENDED message to players {}",
                                event.getGame().getPlayerIds()),
                        failure -> log.error("Failed to send GAME_ENDED message to players {}: {}",
                                event.getGame().getPlayerIds(), failure.getMessage()));
    }
}