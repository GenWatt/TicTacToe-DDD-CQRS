package com.example.demo.domain.event;

import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.PlayerId;

import lombok.Getter;

@Getter
public class PlayerMatchedEvent extends DomainEvent {
    private final GameId gameId;
    private final PlayerId player1Id;
    private final PlayerId player2Id;

    public PlayerMatchedEvent(GameId gameId, PlayerId player1Id, PlayerId player2Id) {
        super();
        this.gameId = gameId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
    }
}