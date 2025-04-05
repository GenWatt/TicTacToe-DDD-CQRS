package com.example.demo.infrastructure.websocket.handler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.example.demo.domain.repository.GameRepository;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.MessageType;
import com.example.demo.infrastructure.websocket.service.MatchmakingService;

import io.smallrye.mutiny.Uni;
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
    public Uni<Void> handle(WebSocketSession session, String payload) {
        // Create a reactive chain that handles the entire connection closing process
        return sessionService.getPlayerIdBySession(session)
                .onItem().ifNotNull().transformToUni(playerId ->
                // Process player disconnection
                processPlayerDisconnection(playerId)
                        // Always remove the session at the end
                        .onTermination().invoke(() -> sessionService.removeSession(session)
                                .subscribe().with(
                                        unused -> log.info("WebSocket connection closed for session: {}",
                                                session.getId()),
                                        error -> log.error("Error removing session: {}", session.getId(), error))))
                .onItem().ifNull().continueWith(() -> {
                    log.warn("No player ID associated with session: {}", session.getId());
                    // If no player ID, just remove the session
                    sessionService.removeSession(session)
                            .subscribe().with(
                                    unused -> log.info("WebSocket connection closed for session: {}", session.getId()),
                                    error -> log.error("Error removing session: {}", session.getId(), error));
                    return null;
                })
                .replaceWithVoid();
    }

    /**
     * Process a player disconnection in a reactive manner.
     * 
     * @param playerId The ID of the player that disconnected
     * @return Uni<Void> that completes when processing is done
     */
    private Uni<Void> processPlayerDisconnection(PlayerId playerId) {
        log.info("Processing disconnection for player: {}", playerId);

        // Use the reactive cancelQueue method directly
        return matchmakingService.cancelQueue(playerId)
                .onItem().invoke(wasInMatchmaking -> {
                    if (wasInMatchmaking) {
                        log.info("Player {} removed from matchmaking queue", playerId);
                    } else {
                        log.info("Player {} not in matchmaking, checking active games", playerId);
                    }
                })
                .chain(wasInMatchmaking ->
                // Always check for active games
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
                        .onItem().ifNull().continueWith(() -> {
                            log.info("No active game found for player {}", playerId);
                            return null;
                        })
                        .replaceWithVoid());
    }

    @Override
    public MessageType getType() {
        return MessageType.CONNECTION_CLOSED;
    }
}