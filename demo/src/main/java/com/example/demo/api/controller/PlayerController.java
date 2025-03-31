package com.example.demo.api.controller;

import com.example.demo.api.dto.CreatePlayerRequest;
import com.example.demo.api.dto.LoginPlayerRequest;
import com.example.demo.api.dto.PlayerResponse;
import com.example.demo.api.mapper.PlayerDtoMapper;
import com.example.demo.application.command.CreatePlayerCommand;
import com.example.demo.application.command.LoginPlayerCommand;
import com.example.demo.application.handler.CreatePlayerCommandHandler;
import com.example.demo.application.handler.GetPlayerQueryHandler;
import com.example.demo.application.handler.LoginPlayerCommandHandler;
import com.example.demo.application.query.GetPlayerQuery;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.domain.valueObject.Username;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final CreatePlayerCommandHandler createPlayerCommandHandler;
    private final GetPlayerQueryHandler getPlayerQueryHandler;
    private final LoginPlayerCommandHandler loginPlayerCommandHandler;
    private final PlayerDtoMapper playerDtoMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<PlayerResponse> createPlayer(@RequestBody CreatePlayerRequest request) {
        CreatePlayerCommand command = new CreatePlayerCommand(
                Username.from(request.getUsername()));
        System.out.println("Creating player with command: " + command);
        return createPlayerCommandHandler.handle(command)
                .onItem().transform(playerDtoMapper::toResponse)
                .subscribeAsCompletionStage();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<PlayerResponse> getPlayer(@PathVariable() UUID id) {
        PlayerId playerId = PlayerId.from(id);
        GetPlayerQuery query = new GetPlayerQuery(playerId);

        return getPlayerQueryHandler.handle(query)
                .onItem().transform(playerDtoMapper::toResponse)
                .subscribeAsCompletionStage();
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<PlayerResponse> login(@RequestBody LoginPlayerRequest request) {
        Username username = Username.from(request.getUsername());
        LoginPlayerCommand command = new LoginPlayerCommand(username);

        return loginPlayerCommandHandler.handle(command)
                .onItem().transform(playerDtoMapper::toResponse)
                .subscribeAsCompletionStage();
    }
}