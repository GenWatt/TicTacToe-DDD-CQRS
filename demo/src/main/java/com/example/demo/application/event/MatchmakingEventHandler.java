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
import com.example.demo.infrastructure.websocket.service.WebSocketMessageService;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.tuples.Tuple3;
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

    private final WebSocketMessageService wesocketMessageService;
    private final CreateGameCommandHandler createGameCommandHandler;
    private final PlayerRepository playerRepository;

    @Async
    @EventListener
    public void handlePlayerJoinedMatchmaking(PlayerJoinedEvent event) {
        log.info("Player {} joined matchmaking", event.getPlayerId());
        var response = InQueueResponseDto.builder()
                .message("You have joined the matchmaking queue")
                .build();

        wesocketMessageService.sendToPlayer(event.getPlayerId(), "IN_QUEUE", response);
    }

    @Async
    @EventListener
    public void handlePlayerMatched(PlayerMatchedEvent event) {
        log.info("Players {} and {} matched", event.getPlayer1Id(), event.getPlayer2Id());

        List<PlayerId> playerIds = List.of(event.getPlayer1Id(), event.getPlayer2Id());
        CreateGameCommand command = new CreateGameCommand(event.getGameId(), playerIds);

        playerRepository.findAllByIds(playerIds)
                .flatMap(players -> createGameCommandHandler.handle(command)
                        .map(game -> Tuple2.of(game, players)))
                .flatMap(tuple -> {
                    Game game = tuple.getItem1();
                    List<Player> players = tuple.getItem2();

                    return processMatch(game, players, event);
                })
                .subscribe().with(
                        success -> log.info(
                                "Successfully processed match for players {} and {}",
                                event.getPlayer1Id(), event.getPlayer2Id()),
                        error -> log.error("Error processing player match", error));
    }

    private Uni<Void> processMatch(Game game, List<Player> playerList, PlayerMatchedEvent event) {
        // Find players reactively with error handling
        Uni<Player> player1Uni = Uni.createFrom().item(() -> playerList.stream()
                .filter(p -> p.getId().equals(event.getPlayer1Id()))
                .findFirst()
                .orElse(null)).onItem().ifNull()
                .failWith(() -> new IllegalStateException("Player 1 not found"));

        Uni<Player> player2Uni = Uni.createFrom().item(() -> playerList.stream()
                .filter(p -> p.getId().equals(event.getPlayer2Id()))
                .findFirst()
                .orElse(null)).onItem().ifNull()
                .failWith(() -> new IllegalStateException("Player 2 not found"));

        // Combine players and then process
        return Uni.combine().all().unis(player1Uni, player2Uni)
                .asTuple()
                .flatMap(tuple -> {
                    Player player1 = tuple.getItem1();
                    Player player2 = tuple.getItem2();

                    // Assign player types
                    if (game.getCurrentPlayerMoveId().equals(player1.getId())) {
                        player1.setPlayerType(PlayerType.O);
                        player2.setPlayerType(PlayerType.X);
                    } else {
                        player1.setPlayerType(PlayerType.X);
                        player2.setPlayerType(PlayerType.O);
                    }

                    // Save both players reactively
                    return Uni.combine().all()
                            .unis(
                                    playerRepository.save(player1),
                                    playerRepository.save(player2))
                            .asTuple()
                            .map(savedTuple -> Tuple3.of(game, savedTuple.getItem1(),
                                    savedTuple.getItem2()));
                })
                .flatMap(tuple -> {
                    Game savedGame = tuple.getItem1();
                    Player savedPlayer1 = tuple.getItem2();
                    Player savedPlayer2 = tuple.getItem3();

                    log.info("Game {} created with players {} and {}", savedGame.getId(),
                            savedPlayer1.getPlayerType(), savedPlayer2.getPlayerType());
                    // Send match notifications to both players
                    return Uni.combine().all().unis(
                            Uni.createFrom().item(() -> {
                                sendPersonalizedMatchResponse(savedGame, savedPlayer1,
                                        savedPlayer2);
                                return null;
                            }),
                            Uni.createFrom().item(() -> {
                                sendPersonalizedMatchResponse(savedGame, savedPlayer2,
                                        savedPlayer1);
                                return null;
                            })).asTuple().replaceWithVoid();
                });
    }

    private void sendPersonalizedMatchResponse(Game game, Player player, Player opponent) {
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

        wesocketMessageService.sendToPlayer(player.getId(), "MATCH_FOUND", dto);
    }
}