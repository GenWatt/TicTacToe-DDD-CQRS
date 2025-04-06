package com.example.demo.application.event;

import com.example.demo.application.command.CreateGameCommand;
import com.example.demo.application.dto.ErrorResponseDto;
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
import lombok.AllArgsConstructor;
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

	private final WebSocketMessageService websocketMessageService;
	private final CreateGameCommandHandler createGameCommandHandler;
	private final PlayerRepository playerRepository;

	@Async
	@EventListener
	public void handlePlayerJoinedMatchmaking(PlayerJoinedEvent event) {
		log.info("Player {} joined matchmaking", event.getPlayerId());

		var response = InQueueResponseDto.builder()
				.message("You have joined the matchmaking queue")
				.build();

		websocketMessageService.sendToPlayer(event.getPlayerId(), "IN_QUEUE", response)
				.subscribe().with(
						success -> log.info("Successfully sent IN_QUEUE message to player {}", event.getPlayerId()),
						failure -> log.error("Failed to send IN_QUEUE message to player {}", event.getPlayerId(),
								failure));
	}

	@Async
	@EventListener
	public void handlePlayerMatched(PlayerMatchedEvent event) {
		log.info("Players {} and {} matched", event.getPlayer1Id(), event.getPlayer2Id());

		List<PlayerId> playerIds = List.of(event.getPlayer1Id(), event.getPlayer2Id());
		CreateGameCommand command = new CreateGameCommand(event.getGameId(), playerIds);

		createMatchAndNotifyPlayers(playerIds, command, event)
				.subscribe().with(
						success -> log.info(
								"Successfully processed match for players {} and {}",
								event.getPlayer1Id(), event.getPlayer2Id()),
						error -> {
							log.error("Error processing player match", error);
							ErrorResponseDto commandError = new ErrorResponseDto("Failed to create game");
							websocketMessageService.sendToPlayers(playerIds, "ERROR", commandError);
						});
	}

	private Uni<Void> createMatchAndNotifyPlayers(List<PlayerId> playerIds, CreateGameCommand command,
			PlayerMatchedEvent event) {
		return playerRepository.findAllByIds(playerIds)
				.flatMap(players -> createGameCommandHandler.handle(command)
						.map(game -> Tuple2.of(game, players)))
				.flatMap(tuple -> processMatch(tuple.getItem1(), tuple.getItem2(), event));
	}

	private Uni<Void> processMatch(Game game, List<Player> playerList, PlayerMatchedEvent event) {
		return findPlayersFromList(playerList, event)
				.flatMap(playerTuple -> assignPlayerTypes(game, playerTuple))
				.flatMap(this::savePlayers)
				.flatMap(this::notifyPlayers);
	}

	private Uni<Tuple2<Player, Player>> findPlayersFromList(List<Player> playerList, PlayerMatchedEvent event) {
		return Uni.combine().all().unis(
				findPlayerById(playerList, event.getPlayer1Id(), "Player 1"),
				findPlayerById(playerList, event.getPlayer2Id(), "Player 2")).asTuple();
	}

	private Uni<Player> findPlayerById(List<Player> playerList, PlayerId id, String playerLabel) {
		return Uni.createFrom().item(() -> playerList.stream()
				.filter(p -> p.getId().equals(id))
				.findFirst()
				.orElse(null)).onItem().ifNull()
				.failWith(() -> new IllegalStateException(playerLabel + " not found"));
	}

	private Uni<GameWithPlayers> assignPlayerTypes(Game game, Tuple2<Player, Player> playerTuple) {
		Player player1 = playerTuple.getItem1();
		Player player2 = playerTuple.getItem2();

		boolean player1GoesFirst = game.getCurrentPlayerMoveId().equals(player1.getId());
		player1.setPlayerType(player1GoesFirst ? PlayerType.O : PlayerType.X);
		player2.setPlayerType(player1GoesFirst ? PlayerType.X : PlayerType.O);

		return Uni.createFrom().item(new GameWithPlayers(game, player1, player2));
	}

	private Uni<GameWithPlayers> savePlayers(GameWithPlayers gameWithPlayers) {
		return Uni.combine().all().unis(
				playerRepository.save(gameWithPlayers.player1),
				playerRepository.save(gameWithPlayers.player2))
				.asTuple().map(tuple -> new GameWithPlayers(
						gameWithPlayers.game,
						tuple.getItem1(),
						tuple.getItem2()));
	}

	private Uni<Void> notifyPlayers(GameWithPlayers gameWithPlayers) {
		Game game = gameWithPlayers.game;
		Player player1 = gameWithPlayers.player1;
		Player player2 = gameWithPlayers.player2;

		log.info("Game {} created with players {} and {}", game.getId(),
				player1.getPlayerType(), player2.getPlayerType());

		MatchFoundResponseDto player1Dto = createMatchResponseDto(game, player1, player2);
		MatchFoundResponseDto player2Dto = createMatchResponseDto(game, player2, player1);

		// Add a small delay to ensure WebSocket sessions are properly registered
		return Uni.createFrom().voidItem()
				.onItem().delayIt().by(java.time.Duration.ofMillis(50))
				.chain(() -> Uni.combine().all().unis(
						websocketMessageService.sendToPlayer(player1.getId(), "MATCH_FOUND", player1Dto),
						websocketMessageService.sendToPlayer(player2.getId(), "MATCH_FOUND", player2Dto))
						.discardItems());
	}

	private MatchFoundResponseDto createMatchResponseDto(Game game, Player player, Player opponent) {
		return MatchFoundResponseDto.builder()
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
	}

	@AllArgsConstructor
	private static class GameWithPlayers {
		private final Game game;
		private final Player player1;
		private final Player player2;
	}
}