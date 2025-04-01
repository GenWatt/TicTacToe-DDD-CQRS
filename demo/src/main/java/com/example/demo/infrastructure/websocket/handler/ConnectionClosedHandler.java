package com.example.demo.infrastructure.websocket.handler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.example.demo.domain.repository.GameRepository;
import com.example.demo.domain.service.MatchmakingService;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.MessageType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@MessageTypeHandler(MessageType.CONNECTION_CLOSED)
@RequiredArgsConstructor
public class ConnectionClosedHandler implements WebSocketMessageHandler {

    private final WebSocketSessionService sessionService;
    private final MatchmakingService matchmakingService;
    private final GameRepository gameRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void handle(WebSocketSession session, String payload) throws Exception {
        PlayerId playerId = sessionService.getPlayerIdBySession(session);

        if (playerId == null) {
            log.warn("No player ID associated with session: {}", session.getId());
            sessionService.removeSession(session);
            return;
        }

        log.info("Processing disconnection for player: {}", playerId);

        // Cancel matchmaking if the player is in the queue.
        boolean wasInMatchmaking = matchmakingService.cancelQueue(playerId);
        if (wasInMatchmaking) {
            log.info("Player {} removed from matchmaking queue", playerId);
        } else {
            log.info("Player {} not in matchmaking, checking active games", playerId);
        }

        // Chain the operations: find the game, disconnect the player, update the game,
        // and publish events.
        gameRepository.findInProgressGameByPlayerId(playerId)
                .onItem().ifNotNull().invoke(game -> {
                    log.info("Player {} found in game {}", playerId, game.getId());
                    game.playerDisconnect(playerId);
                })
                .onItem().ifNotNull().transformToUni(game -> gameRepository.save(game)
                        .onItem().invoke(savedGame -> {
                            log.info("Game {} updated after player disconnect", savedGame.getId());
                            savedGame.getAndClearEvents().forEach(event -> {
                                log.info("Publishing event: {}", event.getClass().getSimpleName());
                                eventPublisher.publishEvent(event);
                            });
                        }))
                .subscribe().with(
                        unused -> {
                            /* no-op */ },
                        error -> log.error("Error processing disconnection for player {}", playerId, error));

        // Always clean up the session.
        sessionService.removeSession(session);
        log.info("WebSocket connection closed for session: {}", session.getId());
    }

    @Override
    public MessageType getType() {
        return MessageType.CONNECTION_CLOSED;
    }
}
