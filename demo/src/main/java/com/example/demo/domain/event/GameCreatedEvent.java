package com.example.demo.domain.event;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.PlayerId;

import lombok.Getter;

@Getter
public class GameCreatedEvent extends DomainEvent {
    private final GameId gameId;
    private final List<PlayerId> playerIds = new ArrayList<>();

    public GameCreatedEvent(GameId gameId, List<PlayerId> playerIds) {
        super();
        this.gameId = gameId;
        this.playerIds.addAll(playerIds);
    }
}