package com.example.demo.application.handler;

import org.springframework.stereotype.Component;

import com.example.demo.application.command.LoginPlayerCommand;
import com.example.demo.domain.aggregate.Player;
import com.example.demo.domain.repository.PlayerRepository;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginPlayerCommandHandler {
    private final PlayerRepository playerRepository;

    public Uni<Player> handle(LoginPlayerCommand command) {
        if (command.getUsername() == null) {
            return Uni.createFrom().failure(new IllegalArgumentException("Username is required"));
        }

        return playerRepository.findByUsername(command.getUsername())
                .onItem().ifNull().failWith(new IllegalArgumentException("Player not found"))
                .onItem().transform(player -> player);
    }
}