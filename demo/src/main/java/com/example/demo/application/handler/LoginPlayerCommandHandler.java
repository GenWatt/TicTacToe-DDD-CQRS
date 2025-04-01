package com.example.demo.application.handler;

import org.springframework.stereotype.Component;

import com.example.demo.application.command.LoginPlayerCommand;
import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.exception.PlayerNotFoundException;
import com.example.demo.domain.repository.PlayerRepository;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginPlayerCommandHandler implements CommandHandler<LoginPlayerCommand, Player> {
    private final PlayerRepository playerRepository;

    public Uni<Player> handle(LoginPlayerCommand command) {
        return playerRepository.findByUsername(command.getUsername())
                .onItem().ifNull().failWith(new PlayerNotFoundException(command.getUsername()))
                .onItem().transform(player -> player);
    }
}