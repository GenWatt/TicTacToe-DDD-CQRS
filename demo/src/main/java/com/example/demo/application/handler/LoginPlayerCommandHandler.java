package com.example.demo.application.handler;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.application.command.LoginPlayerCommand;
import com.example.demo.application.dto.LoginResult;
import com.example.demo.domain.exception.PlayerNotFoundException;
import com.example.demo.domain.repository.PlayerRepository;
import com.example.demo.infrastructure.security.JwtTokenProvider;

import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginPlayerCommandHandler implements CommandHandler<LoginPlayerCommand, LoginResult> {
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public Uni<LoginResult> handle(LoginPlayerCommand command) {
        return playerRepository.findByUsername(command.getUsername())
                .onItem().ifNull().failWith(new PlayerNotFoundException(command.getUsername()))
                .onItem().transform(player -> {
                    if (!passwordEncoder.matches(command.getPassword(), player.getPassword())) {
                        throw new BadCredentialsException("Invalid password");
                    }

                    String token = jwtTokenProvider.createToken(player.getId(), player.getUsername().getUsername());
                    return new LoginResult(player, token);
                });
    }
}