package com.example.demo.domain.event;

import com.example.demo.domain.aggregate.Game;
import com.example.demo.domain.valueObject.PlayerId;

import lombok.Getter;

@Getter
public class GameEndedEvent extends DomainEvent {

    private final PlayerId winnerId;
    private final Game game;

    public GameEndedEvent(PlayerId winnerId, Game game) {
        super();
        this.winnerId = winnerId;
        this.game = game;
    }
}
