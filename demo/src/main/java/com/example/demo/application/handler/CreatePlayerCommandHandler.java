package com.example.demo.application.handler;

import com.example.demo.application.command.CreatePlayerCommand;
import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.valueObject.Username;
import com.example.demo.infrastructure.presistence.repository.PlayerRepositoryImpl;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreatePlayerCommandHandler implements CommandHandler<CreatePlayerCommand, Player> {
    private final PlayerRepositoryImpl playerRepository;

    public Uni<Player> handle(CreatePlayerCommand command) {
        Username username = command.getUsername();

        Player player = Player.create(username);
        return playerRepository.save(player);
    }
}