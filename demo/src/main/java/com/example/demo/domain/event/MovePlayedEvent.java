package com.example.demo.domain.event;

import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;

import lombok.Getter;

@Getter
public class MovePlayedEvent extends DomainEvent {
    private final GameId gameId;
    private final PlayerId playerId;
    private final Move move;

    public MovePlayedEvent(GameId gameId, PlayerId playerId, Move move) {
        super();
        this.gameId = gameId;
        this.playerId = playerId;
        this.move = move;
    }
}
