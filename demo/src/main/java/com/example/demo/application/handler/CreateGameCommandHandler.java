package com.example.demo.application.handler;

import com.example.demo.application.command.CreateGameCommand;
import com.example.demo.domain.aggregate.Game;
import com.example.demo.domain.repository.GameRepository;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateGameCommandHandler {
    private final GameRepository gameRepository;

    public Uni<Game> handle(CreateGameCommand command) {
        if (command.getPlayers().isEmpty()) {
            return Uni.createFrom().failure(new IllegalArgumentException("No players provided"));
        }

        if (command.getGameId() == null) {
            return Uni.createFrom().failure(new IllegalArgumentException("Game ID is required"));
        }

        Game game = Game.create(command.getGameId(), command.getPlayers());

        return gameRepository.save(game);
    }
}