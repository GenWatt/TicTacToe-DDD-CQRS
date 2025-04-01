package com.example.demo.application.event;

import com.example.demo.application.command.CreateGameCommand;
import com.example.demo.application.dto.InQueueResponseDto;
import com.example.demo.application.dto.MatchFoundResponseDto;
import com.example.demo.application.dto.PlayerDto;
import com.example.demo.application.handler.CreateGameCommandHandler;
import com.example.demo.domain.aggregate.Game;
import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.event.PlayerJoinedEvent;
import com.example.demo.domain.event.PlayerMatchedEvent;
import com.example.demo.domain.repository.PlayerRepository;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.domain.valueObject.PlayerType;
import com.example.demo.infrastructure.websocket.WebSocketSessionService;
import com.example.demo.infrastructure.websocket.message.WebSocketResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchmakingEventHandler {

    private final WebSocketSessionService webSocketHandler;
    private final CreateGameCommandHandler createGameCommandHandler;
    private final PlayerRepository playerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    @EventListener
    public void handlePlayerJoinedMatchmaking(PlayerJoinedEvent event) {
        log.info("Player {} joined matchmaking", event.getPlayerId());
        var response = InQueueResponseDto.builder()
                .message("You have joined the matchmaking queue")
                .build();

        String payload = null;

        try {
            payload = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Error serializing InQueueResponseDto", e);
        }

        WebSocketResponse responseMessage = new WebSocketResponse("IN_QUEUE", payload);
        webSocketHandler.sendToPlayer(event.getPlayerId(), responseMessage);
    }

    @Async
    @EventListener
    public void handlePlayerMatched(PlayerMatchedEvent event) {
        log.info("Players {} and {} matched", event.getPlayer1Id(), event.getPlayer2Id());

        List<PlayerId> players = List.of(event.getPlayer1Id(), event.getPlayer2Id());
        CreateGameCommand command = new CreateGameCommand(event.getGameId(), players);

        // First, fetch players' details
        playerRepository.findAllByIds(players)
                .subscribe().with(playerList -> {
                    // Then create the game
                    createGameCommandHandler.handle(command)
                            .subscribe().with(game -> {
                                try {
                                    // Find the players in the list
                                    var player1 = playerList.stream()
                                            .filter(p -> p.getId().equals(event.getPlayer1Id()))
                                            .findFirst()
                                            .orElseThrow();

                                    var player2 = playerList.stream()
                                            .filter(p -> p.getId().equals(event.getPlayer2Id()))
                                            .findFirst()
                                            .orElseThrow();

                                    if (game.getCurrentPlayerMoveId().equals(player1.getId())) {
                                        player1.setPlayerType(PlayerType.O);
                                        player2.setPlayerType(PlayerType.X);
                                    } else {
                                        player1.setPlayerType(PlayerType.X);
                                        player2.setPlayerType(PlayerType.O);
                                    }

                                    // Save the players with their assigned types
                                    playerRepository.save(player1).subscribe();
                                    playerRepository.save(player2).subscribe();

                                    // Send player1 their personalized view
                                    sendPersonalizedMatchResponse(game, player1, player2);

                                    // Send player2 their personalized view
                                    sendPersonalizedMatchResponse(game, player2, player1);

                                } catch (Exception e) {
                                    log.error("Error creating match notification", e);
                                }
                            });
                });
    }

    private void sendPersonalizedMatchResponse(Game game, Player player, Player opponent) {
        try {
            var dto = MatchFoundResponseDto.builder()
                    .gameId(game.getId().getId().toString())
                    .gameState(game.getState())
                    .board(game.getBoard())
                    .moves(game.getMoves())
                    .currentPlayerMoveId(game.getCurrentPlayerMoveId().getId().toString())
                    .you(PlayerDto.builder()
                            .playerId(player.getId().getId())
                            .username(player.getUsername().getUsername())
                            .playerType(player.getPlayerType())
                            .build())
                    .opponent(PlayerDto.builder()
                            .playerId(opponent.getId().getId())
                            .username(opponent.getUsername().getUsername())
                            .playerType(opponent.getPlayerType())
                            .build())
                    .build();

            String payload = objectMapper.writeValueAsString(dto);
            webSocketHandler.sendToPlayer(player.getId(), new WebSocketResponse("MATCH_FOUND", payload));
        } catch (JsonProcessingException e) {
            log.error("Error serializing match data for player {}", player.getId(), e);
        }
    }
}