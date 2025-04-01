package com.example.demo.domain.event;

import com.example.demo.domain.aggregate.Game;
import com.example.demo.domain.valueObject.PlayerId;

import lombok.Getter;

@Getter
public class GameEndedByDisconnectionEvent extends DomainEvent {
    private final PlayerId winnerId;
    private final Game game;
    private final PlayerId disconnectedPlayerId;

    public GameEndedByDisconnectionEvent(PlayerId winnerId, Game game, PlayerId disconnectedPlayerId) {
        super();
        this.winnerId = winnerId;
        this.game = game;
        this.disconnectedPlayerId = disconnectedPlayerId;
    }
}
