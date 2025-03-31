package com.example.demo.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.example.demo.application.handler.CreateGameCommandHandler;
import com.example.demo.domain.repository.GameRepository;

@Configuration
public class CommandHandlerConfiguration {

    @Bean
    public CreateGameCommandHandler createGameCommandHandler(@Lazy GameRepository gameRepository) {
        return new CreateGameCommandHandler(gameRepository);
    }
}