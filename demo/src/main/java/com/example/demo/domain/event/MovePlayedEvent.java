package com.example.demo.domain.event;

import java.util.List;

import com.example.demo.domain.valueObject.Board;
import com.example.demo.domain.valueObject.GameId;
import com.example.demo.domain.valueObject.Move;
import com.example.demo.domain.valueObject.PlayerId;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MovePlayedEvent extends DomainEvent {
    private final GameId gameId;
    private final PlayerId playerId;
    private final PlayerId nextPlayerId;
    private final List<PlayerId> playerIds;
    private final Move move;
    private final Board board;
}
