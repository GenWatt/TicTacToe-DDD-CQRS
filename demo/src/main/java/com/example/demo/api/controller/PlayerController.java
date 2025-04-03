package com.example.demo.api.controller;

import com.example.demo.api.dto.CreatePlayerRequest;
import com.example.demo.api.dto.LoginPlayerRequest;
import com.example.demo.api.dto.PlayerResponse;
import com.example.demo.api.mapper.PlayerDtoMapper;
import com.example.demo.application.command.CreatePlayerCommand;
import com.example.demo.application.command.LoginPlayerCommand;
import com.example.demo.application.mediator.Mediator;
import com.example.demo.application.query.GetPlayerQuery;
import com.example.demo.domain.valueObject.PlayerId;
import com.example.demo.domain.valueObject.Username;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerDtoMapper playerDtoMapper;
    private final Mediator mediator;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<PlayerResponse> createPlayer(@RequestBody CreatePlayerRequest request) {
        CreatePlayerCommand command = new CreatePlayerCommand(
                Username.from(request.getUsername()));

        return mediator.send(command)
                .onItem().transform(playerDtoMapper::toResponse)
                .subscribeAsCompletionStage();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<PlayerResponse> getPlayer(@PathVariable() String id) {
        PlayerId playerId = PlayerId.from(id);
        GetPlayerQuery query = new GetPlayerQuery(playerId);

        return mediator.query(query)
                .onItem().transform(playerDtoMapper::toResponse)
                .subscribeAsCompletionStage();
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<PlayerResponse> login(@RequestBody LoginPlayerRequest request) {
        Username username = Username.from(request.getUsername());
        LoginPlayerCommand command = new LoginPlayerCommand(username);

        return mediator.send(command)
                .onItem().transform(playerDtoMapper::toResponse)
                .subscribeAsCompletionStage();
    }
}