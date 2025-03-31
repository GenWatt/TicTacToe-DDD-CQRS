package com.example.demo.infrastructure.service;

import com.example.demo.domain.repository.GameRepository;
import com.example.demo.domain.service.GameService;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Uni<Void> makeMove(GameId gameId, PlayerId playerId, Move move) {
        System.out.println("Game state before move: " + gameId);
        return gameRepository.findById(gameId)
                .onItem().transform(game -> {
                    System.out.println("Game state before move: " + gameId);
                    game.playMove(playerId, move);

                    return game;
                })
                .chain(game -> gameRepository.save(game))
                .onItem().invoke(game -> {
                    System.out.println("Game state after move: " + game.getDomainEvents());
                    game.getAndClearEvents().forEach(eventPublisher::publishEvent);
                })
                .onFailure().invoke(e -> log.error("Failed to make move", e))
                .replaceWithVoid();
    }

    @Override
    public Uni<Boolean> isPlayerInGame(GameId gameId, PlayerId playerId) {
        return gameRepository.findById(gameId)
                .onItem().transform(game -> game.getPlayerIds().contains(playerId))
                .onFailure().recoverWithItem(false);
    }
}